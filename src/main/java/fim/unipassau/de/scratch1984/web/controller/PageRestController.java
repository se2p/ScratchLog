package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.service.PageService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.NumberParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The REST controller used for retrieving last page numbers.
 */
@RestController
@RequestMapping(value = "/pages")
public class PageRestController {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(PageRestController.class);

    /**
     * The page service to use for retrieving page information.
     */
    private final PageService pageService;

    /**
     * The user service to use for retrieving user information.
     */
    private final UserService userService;

    /**
     * Constructs a new page REST controller with the given dependencies.
     *
     * @param pageService The {@link PageService} to use.
     * @param userService The {@link UserService} to use.
     */
    @Autowired
    public PageRestController(final PageService pageService, final UserService userService) {
        this.pageService = pageService;
        this.userService = userService;
    }

    /**
     * Retrieves the number of the last course participant page for the course with the given id from the database. If
     * the given id is invalid, the response contains a corresponding error code.
     *
     * @param id The id of the course.
     * @param response The {@link HttpServletResponse} to be returned.
     * @return The number of the last course participant page, or -1, if the passed id is invalid.
     */
    @GetMapping("/course/participant")
    @Secured(Constants.ROLE_ADMIN)
    public int getLastCourseParticipantPage(@RequestParam("id") final String id, final HttpServletResponse response) {
        int courseId = NumberParser.parseId(id);

        if (courseId < Constants.MIN_ID) {
            logger.error("Cannot retrieve last course participant page for invalid course id " + id + "!");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return -1;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        return pageService.getLastParticipantCoursePage(courseId) - 1;
    }

    /**
     * Retrieves the number of the last course experiment page for the course with the given id from the database. If
     * the given id is invalid, the response contains a corresponding error code.
     *
     * @param id The id of the course.
     * @param response The {@link HttpServletResponse} to be returned.
     * @return The number of the last course experiment page, or -1, if the passed id is invalid.
     */
    @GetMapping("/course/experiment")
    @Secured(Constants.ROLE_PARTICIPANT)
    public int getLastCourseExperimentPage(@RequestParam("id") final String id, final HttpServletResponse response) {
        int courseId = NumberParser.parseId(id);

        if (courseId < Constants.MIN_ID) {
            logger.error("Cannot retrieve last course experiment page for invalid course id " + id + "!");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return -1;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        return pageService.getLastCourseExperimentPage(courseId) - 1;
    }

    /**
     * Retrieves the number of the last course page for the given user from the database. Administrators can see all
     * courses, while participants only see those courses in which they participate. If an error occurred when fetching
     * the last page number, the response contains a corresponding error code.
     *
     * @param request The {@link HttpServletRequest} containing information on the user.
     * @param response The {@link HttpServletResponse} to be returned.
     * @return The number of the last course page or -1, if no user information could be found.
     */
    @GetMapping("/home/course")
    @Secured(Constants.ROLE_PARTICIPANT)
    public int getLastCoursePage(final HttpServletRequest request, final HttpServletResponse response) {
        if (request.isUserInRole(Constants.ROLE_ADMIN)) {
            return pageService.computeLastCoursePage() - 1;
        } else {
            int userId = getUserId();

            if (userId < Constants.MIN_ID) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return -1;
            }

            return pageService.getLastCoursePage(userId) - 1;
        }
    }

    /**
     * Retrieves the number of the last experiment page for the given user from the database. Administrators can see all
     * experiments, while participants only see those experiments in which they participate. If an error occurred when
     * fetching the last page number, the response contains a corresponding error code.
     *
     * @param request The {@link HttpServletRequest} containing information on the user.
     * @param response The {@link HttpServletResponse} to be returned.
     * @return The number of the last experiment page or -1, if no user information could be found.
     */
    @GetMapping("/home/experiment")
    @Secured(Constants.ROLE_PARTICIPANT)
    public int getLastExperimentPage(final HttpServletRequest request, final HttpServletResponse response) {
        if (request.isUserInRole(Constants.ROLE_ADMIN)) {
            return pageService.computeLastExperimentPage() - 1;
        } else {
            int userId = getUserId();

            if (userId < Constants.MIN_ID) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return -1;
            }

            return pageService.getLastExperimentPage(userId) - 1;
        }
    }

    /**
     * Retrieves the id of the current user from the database.
     *
     * @return The user's id or -1, if no user information could be found.
     */
    private int getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            logger.error("Can't fetch the last page for an unauthenticated user!");
            return -1;
        }

        return userService.getUser(authentication.getName()).getId();
    }

}
