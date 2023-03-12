package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.CourseService;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.PageService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.entity.CourseParticipant;
import fim.unipassau.de.scratch1984.persistence.projection.CourseExperimentProjection;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.FieldErrorHandler;
import fim.unipassau.de.scratch1984.util.MarkdownHandler;
import fim.unipassau.de.scratch1984.util.NumberParser;
import fim.unipassau.de.scratch1984.util.PageUtils;
import fim.unipassau.de.scratch1984.util.validation.StringValidator;
import fim.unipassau.de.scratch1984.web.dto.CourseDTO;
import fim.unipassau.de.scratch1984.web.dto.PasswordDTO;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * The controller for managing courses.
 */
@Controller
@RequestMapping(value = "/course")
public class CourseController {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CourseController.class);

    /**
     * The course service to use for course management.
     */
    private final CourseService courseService;

    /**
     * The experiment service to use for experiment management.
     */
    private final ExperimentService experimentService;

    /**
     * The user service to use for user management.
     */
    private final UserService userService;

    /**
     * The page service to use for retrieving pageable tables.
     */
    private final PageService pageService;

    /**
     * String corresponding to the id request parameter.
     */
    private static final String ID = "id";

    /**
     * String corresponding to the courseDTO request parameter.
     */
    private static final String COURSE_DTO = "courseDTO";

    /**
     * String corresponding to the error model attribute.
     */
    private static final String ERROR = "error";

    /**
     * Constructs a new course controller with the given dependencies.
     *
     * @param courseService The {@link CourseService} to use.
     * @param experimentService The {@link ExperimentService} to use.
     * @param userService The {@link UserService} to use.
     * @param pageService The {@link PageService} to use.
     */
    @Autowired
    public CourseController(final CourseService courseService, final ExperimentService experimentService,
                            final UserService userService, final PageService pageService) {
        this.courseService = courseService;
        this.experimentService = experimentService;
        this.userService = userService;
        this.pageService = pageService;
    }

    /**
     * Returns the course page displaying the information available for the course with the given id. If the request
     * parameter passed is invalid, no entry can be found in the database, the user is redirected to the error page
     * instead.
     *
     * @param id The id of the course.
     * @param model The {@link Model} to hold the information.
     * @param httpServletRequest The {@link HttpServletRequest} for retrieving information about the user.
     * @return The course page on success, or the error page otherwise.
     */
    @GetMapping
    @Secured(Constants.ROLE_PARTICIPANT)
    public String getCourse(@RequestParam(ID) final String id, final Model model,
                            final HttpServletRequest httpServletRequest) {
        int courseId = NumberParser.parseId(id);

        if (courseId < Constants.MIN_ID) {
            LOGGER.error("Cannot return the course page with an invalid id parameter!");
            return Constants.ERROR;
        }

        try {
            CourseDTO courseDTO = courseService.getCourse(courseId);
            addModelInfo(model, courseDTO, httpServletRequest.isUserInRole(Constants.ROLE_ADMIN));
            return "course";
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Returns the form used to create or edit a course.
     *
     * @param courseDTO The {@link CourseDTO} in which the information will be stored.
     * @return A new empty form.
     */
    @GetMapping("/create")
    @Secured(Constants.ROLE_ADMIN)
    public String getCourseForm(@ModelAttribute(COURSE_DTO) final CourseDTO courseDTO) {
        return "course-edit";
    }

    /**
     * Returns the course edit page for the course with the given id. If no entry can be found in the database, the user
     * is redirected to the error page instead.
     *
     * @param id The id to search for.
     * @param model The {@link Model} to hold the information.
     * @return The course edit page on success, or the error page otherwise.
     */
    @GetMapping("/edit")
    @Secured(Constants.ROLE_ADMIN)
    public String getEditCourseForm(@RequestParam(ID) final String id, final Model model) {
        int courseId = NumberParser.parseId(id);

        if (courseId < Constants.MIN_ID) {
            LOGGER.error("Cannot return the course edit page with an invalid id parameter!");
            return Constants.ERROR;
        }

        try {
            CourseDTO courseDTO = courseService.getCourse(courseId);
            model.addAttribute("courseDTO", courseDTO);
            return "course-edit";
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Creates a new course or updates an existing one with the information given in the {@link CourseDTO} and redirects
     * to the corresponding course page on success. If the input form data is invalid, the current page is returned
     * instead to display the error messages.
     *
     * @param courseDTO The course dto containing the input data.
     * @param bindingResult The {@link BindingResult} for returning information on invalid user input.
     * @return The course edit page, if the input is invalid, or the course page on success.
     */
    @PostMapping("/update")
    @Secured(Constants.ROLE_ADMIN)
    public String updateCourse(@ModelAttribute(COURSE_DTO) final CourseDTO courseDTO,
                               final BindingResult bindingResult) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        FieldErrorHandler.validateCourseInput(courseDTO.getTitle(), courseDTO.getDescription(), courseDTO.getContent(),
                bindingResult, resourceBundle);

        if (existsCourseTitle(courseDTO.getId(), courseDTO.getTitle())) {
            LOGGER.error("Cannot save the course as a course with the title " + courseDTO.getTitle()
                    + " already exists!");
            FieldErrorHandler.addTitleExistsError(bindingResult, COURSE_DTO, resourceBundle);
        }

        if (bindingResult.hasErrors()) {
            return "course-edit";
        }

        courseDTO.setLastChanged(LocalDateTime.now());
        int savedId = courseService.saveCourse(courseDTO);
        return "redirect:/course?id=" + savedId;
    }

    /**
     * Deletes the course with the given id and all experiments associated with it. On success, the user is redirected
     * to the index page. If the entered password was invalid, the user returns to the course page where a corresponding
     * error message is displayed. If anything else goes wrong, the user is redirected to the error page.
     *
     * @param passwordDTO The {@link PasswordDTO} containing the password necessary to perform the delete operation.
     * @param id The id of the course to be deleted.
     * @return The index page on success, the course page if the password was invalid, or the error page.
     */
    @PostMapping("/delete")
    @Secured(Constants.ROLE_ADMIN)
    public String deleteCourse(@ModelAttribute("passwordDTO") final PasswordDTO passwordDTO,
                               @RequestParam(ID) final String id) {
        if (id == null || passwordDTO.getPassword() == null) {
            LOGGER.error("Cannot delete course with id null or input password null!");
            return Constants.ERROR;
        }

        int courseId = NumberParser.parseId(id);

        if (courseId < Constants.MIN_ID) {
            LOGGER.error("Cannot delete the course with invalid id " + id + "!");
            return Constants.ERROR;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getName() == null) {
            LOGGER.error("An unauthenticated user tried to delete the course with id " + courseId + "!");
            return Constants.ERROR;
        }

        try {
            UserDTO currentUser = userService.getUser(authentication.getName());

            if ((passwordDTO.getPassword().length() > Constants.SMALL_FIELD)
                    || (!userService.matchesPassword(passwordDTO.getPassword(), currentUser.getPassword()))) {
                return "redirect:/course?invalid=true&id=" + courseId;
            }

            courseService.deleteCourse(courseId);
            return "redirect:/?success=true";
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Changes the course status to the given request parameter value. If the passed id or status values are invalid, or
     * no corresponding course exists in the database, the user is redirected to the error page instead.
     *
     * @param id The id of the course.
     * @param status The new status of the course.
     * @param model The {@link Model} to hold the information.
     * @return The course page on success, or the error page otherwise.
     */
    @GetMapping("/status")
    @Secured(Constants.ROLE_ADMIN)
    public String changeCourseStatus(@RequestParam("stat") final String status, @RequestParam(ID) final String id,
                                     final Model model) {
        int courseId = NumberParser.parseId(id);

        if (courseId < Constants.MIN_ID || status == null) {
            LOGGER.error("Cannot change the status of the course with invalid id or status parameters!");
            return Constants.ERROR;
        }

        try {
            CourseDTO courseDTO;

            if (status.equals("open")) {
                courseDTO = courseService.changeCourseStatus(true, courseId);
            } else if (status.equals("close")) {
                courseDTO = courseService.changeCourseStatus(false, courseId);
            } else {
                LOGGER.debug("Cannot return the corresponding course page for requested status change " + status + "!");
                return Constants.ERROR;
            }

            addModelInfo(model, courseDTO, true);
            return "course";
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Adds the user with the given username or email as a participant to the course with the given id. If the add
     * parameter is specified, the user is also added as a participant to all experiments offered in the course. If no
     * corresponding course could be found, the user is redirected to the error page instead. If the given username or
     * email is invalid, the user is returned to the course page where a corresponding error message is displayed.
     *
     * @param participant The username or email to search for.
     * @param add Whether the user should be added as a participant to all experiments.
     * @param id The id of the course.
     * @param model The {@link Model} used to store information on errors.
     * @return The updated course page on success, the course page displaying an error message, or the error page.
     */
    @GetMapping("/participant/add")
    @Secured(Constants.ROLE_ADMIN)
    public String addParticipant(@RequestParam("participant") final String participant,
                                 @RequestParam(required = false, name = "add") final String add,
                                 @RequestParam("id") final String id,
                                 final Model model) {
        int courseId = NumberParser.parseId(id);
        CourseDTO courseDTO = getActiveCourseDTO(courseId);

        if (courseDTO == null) {
            LOGGER.error("Cannot add a new participant with an invalid course id parameter!");
            return Constants.ERROR;
        }

        if (checkReturnCoursePage(courseId, participant, true, true, model)) {
            addModelInfo(model, courseDTO, true);
            return "course";
        }

        try {
            int userId = courseService.saveCourseParticipant(courseId, participant);

            if (add != null) {
                courseService.addParticipantToCourseExperiments(courseId, userId);
            }

            return "redirect:/course?id=" + courseId;
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Removes the user with the given username or email as a participant of the course with the given id and of all
     * experiments offered in the course. If no corresponding course could be found, the user is redirected to the error
     * page instead. If the given username or email is invalid, the user is returned to the course page where a
     * corresponding error message is displayed.
     *
     * @param participant The username or email to search for.
     * @param id The id of the course.
     * @param model The {@link Model} used to store information on errors.
     * @return The updated course page on success, the course page displaying an error message, or the error page.
     */
    @GetMapping("/participant/delete")
    @Secured(Constants.ROLE_ADMIN)
    public String deleteParticipant(@RequestParam("participant") final String participant,
                                    @RequestParam("id") final String id, final Model model) {
        int courseId = NumberParser.parseId(id);
        CourseDTO courseDTO = getActiveCourseDTO(courseId);

        if (courseDTO == null) {
            LOGGER.error("Cannot delete a participant with an invalid course id parameter!");
            return Constants.ERROR;
        }

        if (checkReturnCoursePage(courseId, participant, false, true, model)) {
            addModelInfo(model, courseDTO, true);
            return "course";
        }

        try {
            courseService.deleteCourseParticipant(courseId, participant);
            return "redirect:/course?id=" + courseId;
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Deletes the experiment course entry for the experiment with the given title and the course with the given id. If
     * the title input does not meet the requirements, no corresponding experiment could be found, or the experiment not
     * part of the course, the course page is returned to display a corresponding error message. If no course with the
     * given id could be found, or something went wrong when trying to persist the change, the user is redirected to the
     * error page instead.
     *
     * @param title The title of the experiment to be removed.
     * @param id The id of the course.
     * @param model The {@link Model} to store information on errors.
     * @return The updated course page on success, the course page displaying an error message, or the error page.
     */
    @GetMapping("/experiment/delete")
    @Secured(Constants.ROLE_ADMIN)
    public String deleteExperiment(@RequestParam("title") final String title, @RequestParam("id") final String id,
                                   final Model model) {
        int courseId = NumberParser.parseId(id);
        CourseDTO courseDTO = getActiveCourseDTO(courseId);

        if (courseDTO == null) {
            LOGGER.error("Cannot remove an experiment with an invalid course id parameter!");
            return Constants.ERROR;
        }

        if (checkReturnCoursePage(courseId, title, false, false, model)) {
            addModelInfo(model, courseDTO, true);
            return "course";
        }

        courseService.deleteCourseExperiment(courseId, title);
        return "redirect:/course?id=" + courseId;
    }

    /**
     * Retrieves the course participant page for the given course and page number, if the provided numbers are valid.
     *
     * @param id The course id.
     * @param pageNumber The number of the page to be retrieved.
     * @return The retrieved participant page information.
     */
    @GetMapping("/page/participant")
    @Secured(Constants.ROLE_ADMIN)
    public ModelAndView getParticipantPage(@RequestParam("id") final String id,
                                           @RequestParam("page") final String pageNumber) {
        int courseId = NumberParser.parseId(id);

        if (courseId < Constants.MIN_ID) {
            LOGGER.error("Cannot fetch course participant page for invalid course id " + id + "!");
            return new ModelAndView(Constants.ERROR);
        }

        int lastPage = pageService.getLastParticipantCoursePage(courseId);
        int page = NumberParser.parseId(pageNumber);

        if (PageUtils.isInvalidPageNumber(page, lastPage)) {
            LOGGER.error("Cannot fetch course participant page for invalid page number " + pageNumber
                    + " with last page " + lastPage + "!");
            return new ModelAndView(Constants.ERROR);
        }

        Page<CourseParticipant> participants = pageService.getParticipantCoursePage(courseId, PageRequest.of(page,
                Constants.PAGE_SIZE));
        return getParticipantModelView(participants, page, lastPage, courseId);
    }

    /**
     * Retrieves the course experiment page for the given course and page number, if the provided numbers are valid.
     *
     * @param id The course id.
     * @param pageNumber The number of the page to be retrieved.
     * @return The retrieved experiment page information.
     */
    @GetMapping("/page/experiment")
    @Secured(Constants.ROLE_PARTICIPANT)
    public ModelAndView getExperimentPage(@RequestParam("id") final String id,
                                          @RequestParam("page") final String pageNumber) {
        int courseId = NumberParser.parseId(id);

        if (courseId < Constants.MIN_ID) {
            LOGGER.error("Cannot fetch course experiment page for invalid course id " + courseId + "!");
            return new ModelAndView(Constants.ERROR);
        }

        int lastPage = pageService.getLastCourseExperimentPage(courseId);
        int page = NumberParser.parseId(pageNumber);

        if (PageUtils.isInvalidPageNumber(page, lastPage)) {
            LOGGER.error("Cannot fetch course experiment page for invalid page number " + pageNumber
                    + " with last page " + lastPage + "!");
            return new ModelAndView(Constants.ERROR);
        }

        Page<CourseExperimentProjection> experiments = pageService.getCourseExperimentPage(PageRequest.of(page,
                Constants.PAGE_SIZE), courseId);
        return getExperimentModelView(experiments, page, lastPage, courseId);
    }

    /**
     * Checks whether a different course with the same title already exists in the database.
     *
     * @param id The id of current course, potentially {@code null}.
     * @param title The title of the current course to check for uniqueness.
     * @return {@code true} if another course with the same title exists, or {@code false} otherwise.
     */
    private boolean existsCourseTitle(final Integer id, final String title) {
        return id == null ? courseService.existsCourse(title) : courseService.existsCourse(id, title);
    }

    /**
     * Retrieves the {@link CourseDTO} for the course with the given id from the database. If no corresponding course
     * could be found, {@code null} is returned instead.
     *
     * @param courseId The id of the course.
     * @return The corresponding course dto, or null.
     */
    private CourseDTO getActiveCourseDTO(final int courseId) {
        if (courseId < Constants.MIN_ID) {
            return null;
        }

        try {
            CourseDTO courseDTO = courseService.getCourse(courseId);
            return courseDTO.isActive() ? courseDTO : null;
        } catch (NotFoundException e) {
            return null;
        }
    }

    /**
     * Checks, whether the course page should be returned when adding or removing an experiment or participant, e.g.
     * when the user input was invalid.
     *
     * @param courseId The id of the course.
     * @param input The user input.
     * @param isAdd Whether an add or remove operation is performed.
     * @param isUser Whether a user or experiment is added or removed.
     * @param model The {@link Model} used to store error messages.
     * @return {@code true} if the course page should be returned, ore {@code false} otherwise.
     */
    private boolean checkReturnCoursePage(final int courseId, final String input, final boolean isAdd,
                                          final boolean isUser, final Model model) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());

        if (!isValidInput(input, Constants.LARGE_FIELD, model, resourceBundle)) {
            return true;
        }

        validateUserInput(courseId, input, isAdd, isUser, resourceBundle, model);
        return model.getAttribute(ERROR) != null;
    }

    /**
     * Verifies, that the given input is a valid input string.
     *
     * @param input The string to check.
     * @param maxLength The allowed maximum length of the string.
     * @param model The {@link Model} used to store an error message on invalid input.
     * @param resourceBundle The {@link ResourceBundle} for fetching the error message in the desired language.
     * @return {@code true} if the input is valid, or {@code false} otherwise.
     */
    private boolean isValidInput(final String input, final int maxLength, final Model model,
                                 final ResourceBundle resourceBundle) {
        String validate = StringValidator.validate(input, maxLength);

        if (validate != null) {
            model.addAttribute(ERROR, resourceBundle.getString(validate));
            return false;
        }

        return true;
    }

    /**
     * Checks, whether the given user input is valid based on the operation to be performed for a user or experiment and
     * adds corresponding error messages to the given model, if the input is invalid.
     *
     * @param courseId The id of the course.
     * @param input The user input.
     * @param isAdd Whether an add or remove operation is performed.
     * @param isUser Whether a user or experiment is added or removed.
     * @param resourceBundle The {@link ResourceBundle} for fetching the error message in the desired language.
     * @param model The {@link Model} used to store error messages.
     */
    private void validateUserInput(final int courseId, final String input, final boolean isAdd, final boolean isUser,
                                   final ResourceBundle resourceBundle, final Model model) {
        if (isUser) {
            validateParticipantInput(courseId, input, isAdd, resourceBundle, model);
        } else {
            validateExperimentInput(courseId, input, resourceBundle, model);
        }
    }

    /**
     * Checks, whether a user with the given username or email exists who is not yet part of the course with the given
     * id, if the user is to be added to the course, or who is part of the course, if the user is to be deleted. If this
     * is not the case, a corresponding error message is added to the model.
     *
     * @param courseId The id of the course.
     * @param input The username or email.
     * @param isAdd Whether the user is to be added or removed.
     * @param resourceBundle The {@link ResourceBundle} for fetching the error message in the desired language.
     * @param model The {@link Model} used to store the error message.
     */
    private void validateParticipantInput(final int courseId, final String input, final boolean isAdd,
                                          final ResourceBundle resourceBundle, final Model model) {
        UserDTO userDTO = userService.getUserByUsernameOrEmail(input);

        if (userDTO == null) {
            model.addAttribute(ERROR, resourceBundle.getString("user_not_found"));
        } else if (!userDTO.getRole().equals(UserDTO.Role.PARTICIPANT)) {
            model.addAttribute(ERROR, resourceBundle.getString("user_not_participant"));
        } else if (isAdd && courseService.existsCourseParticipant(courseId, input)) {
            model.addAttribute(ERROR, resourceBundle.getString("course_participant_exists"));
        } else if (!isAdd && !courseService.existsCourseParticipant(courseId, input)) {
            model.addAttribute(ERROR, resourceBundle.getString("course_participant_not_found"));
        }
    }

    /**
     * Checks, whether an experiment with the given title exists that is part of the course with the given id. If this
     * is not the case, a corresponding error message is added to the model.
     *
     * @param courseId The id of the course.
     * @param title The title of the experiment.
     * @param resourceBundle The {@link ResourceBundle} for fetching the error message in the desired language.
     * @param model The {@link Model} used to store the error message.
     */
    private void validateExperimentInput(final int courseId, final String title, final ResourceBundle resourceBundle,
                                         final Model model) {
        if (!experimentService.existsExperiment(title)) {
            model.addAttribute(ERROR, resourceBundle.getString("experiment_not_found"));
        } else if (!courseService.existsCourseExperiment(courseId, title)) {
            model.addAttribute(ERROR, resourceBundle.getString("course_experiment_not_found"));
        }
    }

    /**
     * Adds the given {@link CourseDTO} to the {@link Model} to display the information contained in the DTO to the
     * user. A {@link PasswordDTO} is added for handling operations requiring a password to be performed, e.g. delete.
     * The course content text is sanitized using the {@link MarkdownHandler} before displaying it. Additionally,
     * the required information to displayed in course experiment and course participant tables is retrieved and added
     * to the model. As only administrators are allowed to see the course participant information, this information is
     * not retrieved for participants.
     *
     * @param model The model used to save the information.
     * @param addParticipants Whether to retrieve the course participant information.
     * @param courseDTO The course DTO containing the information to be displayed.
     */
    private void addModelInfo(final Model model, final CourseDTO courseDTO, final boolean addParticipants) {
        if (courseDTO.getContent() != null) {
            courseDTO.setContent(MarkdownHandler.toHtml(courseDTO.getContent()));
        }

        Page<CourseExperimentProjection> experiments = pageService.getCourseExperimentPage(PageRequest.of(0,
                Constants.PAGE_SIZE), courseDTO.getId());
        int lastExperimentPage = pageService.getLastCourseExperimentPage(courseDTO.getId());
        model.addAttribute("courseDTO", courseDTO);
        model.addAttribute("experiments", experiments);
        model.addAttribute("experimentPage", 0);
        model.addAttribute("lastExperimentPage", lastExperimentPage - 1);
        model.addAttribute("passwordDTO", new PasswordDTO());
        addParticipantInfo(model, courseDTO.getId(), addParticipants);
    }

    /**
     * Adds the required information for the course participant table to the given model. The page containing the
     * participant information and the last page value are only retrieved if the information is supposed to be displayed
     * on the course page, meaning that the requesting user is an administrator.
     *
     * @param model The {@link Model} to use for storing the information.
     * @param courseId The id of the course.
     * @param addParticipants Whether to add the course participant information or not.
     */
    private void addParticipantInfo(final Model model, final int courseId, final boolean addParticipants) {
        model.addAttribute("participantPage", 0);

        if (addParticipants) {
            Page<CourseParticipant> participants = pageService.getParticipantCoursePage(courseId,
                    PageRequest.of(0, Constants.PAGE_SIZE));
            model.addAttribute("participants", participants);
            model.addAttribute("lastParticipantPage", pageService.getLastParticipantCoursePage(courseId) - 1);
        } else {
            model.addAttribute("participants", new ArrayList<>());
            model.addAttribute("lastParticipantPage", 0);
        }
    }

    /**
     * Adds the required information for updating the course participant table on the course page.
     *
     * @param participants The current participant page.
     * @param currentPage The current participant page number.
     * @param lastPage The last participant page.
     * @param courseId The id of the course.
     * @return The {@link ModelAndView} used to store the information.
     */
    private ModelAndView getParticipantModelView(final Page<CourseParticipant> participants, final int currentPage,
                                                 final int lastPage, final int courseId) {
        CourseDTO courseDTO = courseService.getCourse(courseId);

        if (courseDTO.getContent() != null) {
            courseDTO.setContent(MarkdownHandler.toHtml(courseDTO.getContent()));
        }

        ModelAndView mv = new ModelAndView("course::course_participant_table");
        mv.addObject("courseDTO", courseDTO);
        mv.addObject("participants", participants);
        mv.addObject("participantPage", currentPage);
        mv.addObject("lastParticipantPage", lastPage - 1);
        return mv;
    }

    /**
     * Adds the required information for updating the course experiment table on the course page.
     *
     * @param experiments The current experiment page.
     * @param currentPage The current experiment page number.
     * @param lastPage The last experiment page.
     * @param courseId The id of the course.
     * @return The {@link ModelAndView} used to store the information.
     */
    private ModelAndView getExperimentModelView(final Page<CourseExperimentProjection> experiments,
                                                final int currentPage, final int lastPage, final int courseId) {
        CourseDTO courseDTO = courseService.getCourse(courseId);

        if (courseDTO.getContent() != null) {
            courseDTO.setContent(MarkdownHandler.toHtml(courseDTO.getContent()));
        }

        ModelAndView mv = new ModelAndView("course::course_experiment_table");
        mv.addObject("courseDTO", courseDTO);
        mv.addObject("experiments", experiments);
        mv.addObject("experimentPage", currentPage);
        mv.addObject("lastExperimentPage", lastPage - 1);
        return mv;
    }

}
