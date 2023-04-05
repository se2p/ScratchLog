package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.PageService;
import fim.unipassau.de.scratch1984.application.service.ParticipantService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.projection.CourseTableProjection;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentTableProjection;
import fim.unipassau.de.scratch1984.util.ApplicationProperties;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.NumberParser;
import fim.unipassau.de.scratch1984.util.PageUtils;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ResourceBundle;

/**
 * The controller for the homepage of the project.
 */
@Controller
public class HomeController {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    /**
     * The experiment service to use for retrieving experiment information.
     */
    private final ExperimentService experimentService;

    /**
     * The page service to use for retrieving pageable tables.
     */
    private final PageService pageService;

    /**
     * The user service to use for retrieving user information.
     */
    private final UserService userService;

    /**
     * The participant service to use for participant management.
     */
    private final ParticipantService participantService;

    /**
     * String corresponding to the name of the model attribute containing the current page number or the corresponding
     * request parameter.
     */
    private static final String PAGE = "page";

    /**
     * Constructs a new home controller with the given dependencies.
     *
     * @param experimentService The {@link ExperimentService} to use.
     * @param pageService The {@link PageService} to use.
     * @param userService The {@link UserService} to use.
     * @param participantService The {@link ParticipantService} to use.
     */
    @Autowired
    public HomeController(final ExperimentService experimentService, final PageService pageService,
                          final UserService userService, final ParticipantService participantService) {
        this.experimentService = experimentService;
        this.pageService = pageService;
        this.userService = userService;
        this.participantService = participantService;
    }

    /**
     * Loads the index page containing basic information about the project. If the user is an administrator, a page
     * containing the latest experiments is loaded instead. If the user is a participant, a page containing the
     * experiments they are participating in is displayed instead.
     *
     * @param httpServletRequest The servlet request.
     * @param model The model to store the loaded information in.
     * @return The index page.
     */
    @GetMapping("/")
    public String getIndexPage(final HttpServletRequest httpServletRequest, final Model model) {
        if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            Page<ExperimentTableProjection> experimentPage = pageService.getExperimentPage(PageRequest.of(0,
                    Constants.PAGE_SIZE));
            Page<CourseTableProjection> coursePage = pageService.getCoursePage(PageRequest.of(0, Constants.PAGE_SIZE));
            int lastExperimentPage = pageService.computeLastExperimentPage();
            int lastCoursePage = pageService.computeLastCoursePage();
            addModelInfo(experimentPage, coursePage, 0, 0, lastExperimentPage - 1, lastCoursePage - 1, model);
        } else if (httpServletRequest.isUserInRole(Constants.ROLE_PARTICIPANT)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || authentication.getName() == null) {
                logger.error("Can't show the participant experiment page for an unauthenticated user!");
                return Constants.ERROR;
            }

            try {
                UserDTO userDTO = userService.getUser(authentication.getName());
                Page<ExperimentTableProjection> experimentPage = pageService.getExperimentParticipantPage(
                        PageRequest.of(0, Constants.PAGE_SIZE), userDTO.getId());
                Page<CourseTableProjection> coursePage = pageService.getCourseParticipantPage(
                        PageRequest.of(0, Constants.PAGE_SIZE), userDTO.getId());
                int lastExperimentPage = pageService.getLastExperimentPage(userDTO.getId());
                int lastCoursePage = pageService.getLastCoursePage(userDTO.getId());
                addModelInfo(experimentPage, coursePage, 0, 0, lastExperimentPage - 1, lastCoursePage - 1, model);
            } catch (NotFoundException e) {
                return Constants.ERROR;
            }
        }

        return "index";
    }

    /**
     * Retrieves the course page corresponding to the given page number for the current user, if the provided
     * information is valid. An administrator will get an overview over all courses while participants will only see
     * courses in which they are participating.
     *
     * @param pageNumber The number of the page to be retrieved.
     * @param httpServletRequest The {@link HttpServletRequest} containing providing information on the user's role.
     * @return The retrieved course page information.
     */
    @GetMapping("/page/course")
    @Secured(Constants.ROLE_PARTICIPANT)
    public ModelAndView getCoursePage(@RequestParam(PAGE) final String pageNumber,
                                      final HttpServletRequest httpServletRequest) {
        int page = getPageNumber(pageNumber);
        Pair<Integer, Integer> lastPageInformation = getLastPageCourses(httpServletRequest);

        if (lastPageInformation == null || PageUtils.isInvalidPageNumber(page, lastPageInformation.getFirst())) {
            return new ModelAndView(Constants.ERROR);
        }

        Page<CourseTableProjection> projections = getCoursePage(httpServletRequest, page,
                lastPageInformation.getSecond());
        return getCourseModelView(projections, page, lastPageInformation.getFirst() - 1);
    }

    /**
     * Retrieves the experiment page corresponding to the given page number for the current user if the provided
     * information is valid. An administrator will get an overview over all experiments while participants will only see
     * experiments in which they are participating.
     *
     * @param pageNumber The number of the page to be retrieved.
     * @param httpServletRequest The {@link HttpServletRequest} containing providing information on the user's role.
     * @return The retrieved experiment page information.
     */
    @GetMapping("/page/experiment")
    @Secured(Constants.ROLE_PARTICIPANT)
    public ModelAndView getExperimentPage(@RequestParam(PAGE) final String pageNumber,
                                          final HttpServletRequest httpServletRequest) {
        int page = getPageNumber(pageNumber);
        Pair<Integer, Integer> lastPageInformation = getLastPageExperiments(httpServletRequest);

        if (lastPageInformation == null || PageUtils.isInvalidPageNumber(page, lastPageInformation.getFirst())) {
            return new ModelAndView(Constants.ERROR);
        }

        Page<ExperimentTableProjection> projections = getExperimentPage(httpServletRequest, page,
                lastPageInformation.getSecond());
        return getExperimentModelView(projections, page, lastPageInformation.getFirst() - 1);
    }

    /**
     * Loads the login page for user authentication.
     *
     * @param userDTO The {@link UserDTO} user for authentication.
     * @return The login page.
     */
    @GetMapping("/login")
    public String getLoginPage(final UserDTO userDTO) {
        return "login";
    }

    /**
     * Loads the experiment finish page for the experiment with the current id.
     *
     * @param user The user id of the participant.
     * @param experiment The experiment id.
     * @param secret The user's secret.
     * @param model The model used to store the message to be displayed on the page.
     * @return The experiment finish page.
     */
    @GetMapping("/finish")
    public String getExperimentFinishPage(@RequestParam("user") final String user,
                                          @RequestParam("experiment") final String experiment,
                                          @RequestParam("secret") final String secret,
                                          final Model model) {
        int experimentId = NumberParser.parseId(experiment);
        int userId = NumberParser.parseId(user);
        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());

        if (isInvalidFinishParams(experimentId, userId, secret)) {
            return Constants.ERROR;
        }

        try {
            ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);
            model.addAttribute("thanks", experimentDTO.getPostscript() != null ? experimentDTO.getPostscript()
                    : resourceBundle.getString("thanks"));
            model.addAttribute("secret", secret);
            model.addAttribute("user", userId);
            model.addAttribute("experiment", experimentId);
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }

        return "experiment-finish";
    }

    /**
     * Loads the password reset page to reset a user password.
     *
     * @param userDTO The {@link UserDTO} user for resetting the password.
     * @return The password reset page.
     */
    @GetMapping("/reset")
    public String getResetPage(final UserDTO userDTO) {
        return ApplicationProperties.MAIL_SERVER ? "password-reset" : Constants.ERROR;
    }

    /**
     * Adds the required page numbers, experiment and course information to the {@link Model} to display the experiment
     * and course tables on the index page.
     *
     * @param experiments The current experiment page.
     * @param courses The current course page.
     * @param currentExperimentPage The number of the current experiment page.
     * @param currentCoursePage The number of the current course page.
     * @param lastExperimentPage The number of the last experiment page.
     * @param lastCoursePage The number of the last course page.
     * @param model The model used to save the info.
     */
    private void addModelInfo(final Page<ExperimentTableProjection> experiments,
                              final Page<CourseTableProjection> courses, final int currentExperimentPage,
                              final int currentCoursePage, final int lastExperimentPage, final int lastCoursePage,
                              final Model model) {
        model.addAttribute("experiments", experiments);
        model.addAttribute("courses", courses);
        model.addAttribute("experimentPage", currentExperimentPage);
        model.addAttribute("coursePage", currentCoursePage);
        model.addAttribute("lastExperimentPage", lastExperimentPage);
        model.addAttribute("lastCoursePage", lastCoursePage);
    }

    /**
     * Parses the given string to a number. If the string is not a valid number, -1 is returned.
     *
     * @param pageNumber The current page number represented as a string.
     * @return The page number, or -1.
     */
    private int getPageNumber(final String pageNumber) {
        if (pageNumber == null) {
            logger.error("Cannot return a page for page number null!");
            return -1;
        }

        int page = NumberParser.parseNumber(pageNumber);

        if (page <= -1) {
            logger.error("Cannot return a page for invalid page number " + pageNumber + "!");
        }

        return page;
    }

    /**
     * Retrieves the number of the last experiment page from the database along with the id of the current user, if the
     * user is not an admin. If no corresponding user data could be found {@code null} is returned instead.
     *
     * @param httpServletRequest The {@link HttpServletRequest} containing information on the user's role.
     * @return A {@link Pair} containing the last page number and potentially the user id.
     */
    private Pair<Integer, Integer> getLastPageExperiments(final HttpServletRequest httpServletRequest) {
        if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            return Pair.of(pageService.computeLastExperimentPage(), 0);
        } else {
            UserDTO userDTO = fetchUserInformation();
            return userDTO == null ? null : Pair.of(pageService.getLastExperimentPage(userDTO.getId()),
                    userDTO.getId());
        }
    }

    /**
     * Retrieves the number of the last course page from the database along with the id of the current user, if the
     * user is not an admin. If no corresponding user data could be found {@code null} is returned instead.
     *
     * @param httpServletRequest The {@link HttpServletRequest} containing information on the user's role.
     * @return A {@link Pair} containing the last page number and potentially the user id.
     */
    private Pair<Integer, Integer> getLastPageCourses(final HttpServletRequest httpServletRequest) {
        if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            return Pair.of(pageService.computeLastCoursePage(), 0);
        } else {
            UserDTO userDTO = fetchUserInformation();
            return userDTO == null ? null : Pair.of(pageService.getLastCoursePage(userDTO.getId()), userDTO.getId());
        }
    }

    /**
     * Retrieves the requested experiment page from the database depending on the current user's role.
     *
     * @param httpServletRequest The {@link HttpServletRequest} containing information on the user's role.
     * @param page The number of the page to be retrieved.
     * @param userId The user id to be used if the current user is not an admin.
     * @return The {@link Page} containing the experiment information.
     */
    private Page<ExperimentTableProjection> getExperimentPage(final HttpServletRequest httpServletRequest,
                                                              final int page, final int userId) {
        if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            return pageService.getExperimentPage(PageRequest.of(page, Constants.PAGE_SIZE));
        } else {
            return pageService.getExperimentParticipantPage(PageRequest.of(page, Constants.PAGE_SIZE), userId);
        }
    }

    /**
     * Retrieves the requested course page from the database depending on the current user's role.
     *
     * @param httpServletRequest The {@link HttpServletRequest} containing information on the user's role.
     * @param page The number of the page to be retrieved.
     * @param userId The user id to be used if the current user is not an admin.
     * @return The {@link Page} containing the course information.
     */
    private Page<CourseTableProjection> getCoursePage(final HttpServletRequest httpServletRequest, final int page,
                                                      final int userId) {
        if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            return pageService.getCoursePage(PageRequest.of(page, Constants.PAGE_SIZE));
        } else {
            return pageService.getCourseParticipantPage(PageRequest.of(page, Constants.PAGE_SIZE), userId);
        }
    }

    /**
     * Retrieves the user information of the current user. If the {@link Authentication} does not provide sufficient
     * information or no user with a corresponding username could be found, {@code null} is returned instead.
     *
     * @return The {@link UserDTO} containing the user information, or {@code null}.
     */
    private UserDTO fetchUserInformation() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            logger.error("Can't show the participant experiment page for an unauthenticated user!");
            return null;
        }

        try {
            return userService.getUser(authentication.getName());
        } catch (NotFoundException e) {
            return null;
        }
    }

    /**
     * Checks if the passed ids and secret are valid parameters and match a participant and user in the database.
     *
     * @param experimentId The id of the experiment.
     * @param userId The id of the user.
     * @param secret The user's secret.
     * @return {@code true} if the passed parameters are invalid or {@code false} otherwise.
     */
    private boolean isInvalidFinishParams(final int experimentId, final int userId, final String secret) {
        if (experimentId < Constants.MIN_ID || userId < Constants.MIN_ID) {
            logger.error("Cannot finish experiment with invalid experiment id " + experimentId + " or invalid user id "
                    + userId + "!");
            return true;
        } else if (secret == null || secret.isBlank()) {
            logger.error("Cannot finish experiment with secret null or blank!");
            return true;
        } else {
            return participantService.isInvalidParticipant(userId, experimentId, secret);
        }
    }

    /**
     * Adds the required information for updating the experiment table on the index page.
     *
     * @param experiments The current experiment page.
     * @param currentPage The current experiment page number.
     * @param lastPage The last experiment page.
     * @return The {@link ModelAndView} used to store the information.
     */
    private ModelAndView getExperimentModelView(final Page<ExperimentTableProjection> experiments,
                                                final int currentPage, final int lastPage) {
        ModelAndView mv = new ModelAndView("index::experiment_table");
        mv.addObject("experiments", experiments);
        mv.addObject("experimentPage", currentPage);
        mv.addObject("lastExperimentPage", lastPage);
        return mv;
    }

    /**
     * Adds the required information for updating the course table on the index page.
     *
     * @param courses The current course page.
     * @param currentPage The current course page number.
     * @param lastPage The last course page.
     * @return The {@link ModelAndView} used to store the information.
     */
    private ModelAndView getCourseModelView(final Page<CourseTableProjection> courses, final int currentPage,
                                            final int lastPage) {
        ModelAndView mv = new ModelAndView("index::course_table");
        mv.addObject("courses", courses);
        mv.addObject("coursePage", currentPage);
        mv.addObject("lastCoursePage", lastPage);
        return mv;
    }

}
