package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.service.PageService;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.NumberParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(PageRestController.class);

    /**
     * The page service to use for retrieving page information.
     */
    private final PageService pageService;

    /**
     * Constructs a new page REST controller with the given dependencies.
     *
     * @param pageService The {@link PageService} to use.
     */
    @Autowired
    public PageRestController(final PageService pageService) {
        this.pageService = pageService;
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
            LOGGER.error("Cannot retrieve last course participant page for invalid course id " + id + "!");
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
            LOGGER.error("Cannot retrieve last course experiment page for invalid course id " + id + "!");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return -1;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        return pageService.getLastCourseExperimentPage(courseId) - 1;
    }

}
