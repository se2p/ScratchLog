package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.CourseService;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.FieldErrorHandler;
import fim.unipassau.de.scratch1984.util.MarkdownHandler;
import fim.unipassau.de.scratch1984.util.NumberParser;
import fim.unipassau.de.scratch1984.web.dto.CourseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
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
    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    /**
     * The course service to use for course management.
     */
    private final CourseService courseService;

    /**
     * String corresponding to the id request parameter.
     */
    private static final String ID = "id";

    /**
     * String corresponding to the courseDTO request parameter.
     */
    private static final String COURSE_DTO = "courseDTO";

    /**
     * Constructs a new course controller with the given dependencies.
     *
     * @param courseService The {@link CourseService} to use.
     */
    @Autowired
    public CourseController(final CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Returns the course page displaying the information available for the course with the given id. If the request
     * parameter passed is invalid, no entry can be found in the database, the user is redirected to the error page
     * instead.
     *
     * @param id The id of the course.
     * @param model The {@link Model} to hold the information.
     * @param httpServletRequest The {@link HttpServletRequest}.
     * @return The course page on success, or the error page otherwise.
     */
    @GetMapping
    @Secured(Constants.ROLE_ADMIN)
    public String getCourse(@RequestParam(ID) final String id, final Model model,
                            final HttpServletRequest httpServletRequest) {
        int courseId = parseId(id);

        if (courseId < Constants.MIN_ID) {
            return Constants.ERROR;
        }

        try {
            CourseDTO courseDTO = courseService.getCourse(courseId);
            addCourseToModel(model, courseDTO);
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
        int courseId = parseId(id);

        if (courseId < Constants.MIN_ID) {
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
            logger.error("Cannot save the course as a course with the title " + courseDTO.getTitle()
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
     * Adds the given {@link CourseDTO} to the {@link Model} to display the information contained in the DTO to the
     * user. The course content text is sanitized using the {@link MarkdownHandler} before displaying it.
     *
     * @param model The model used to save the information.
     * @param courseDTO The course DTO containing the information to be displayed.
     */
    private void addCourseToModel(final Model model, final CourseDTO courseDTO) {
        if (courseDTO.getContent() != null) {
            courseDTO.setContent(MarkdownHandler.toHtml(courseDTO.getContent()));
        }

        model.addAttribute("courseDTO", courseDTO);
    }

    /**
     * Parses the given string to a number, or returns -1, if the id is null or an invalid number.
     *
     * @param id The id to check.
     * @return The number corresponding to the id, if it is a valid number, or -1 otherwise.
     */
    private int parseId(final String id) {
        if (id == null) {
            logger.error("Cannot parse id parameter with id null!");
            return -1;
        }

        int courseId = NumberParser.parseNumber(id);

        if (courseId < Constants.MIN_ID) {
            logger.error("Cannot return course information with an invalid id " + courseId + "!");
            return -1;
        }

        return courseId;
    }

}
