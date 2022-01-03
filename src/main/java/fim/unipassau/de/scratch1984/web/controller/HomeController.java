package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentTableProjection;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
     * The user service to use for retrieving user information.
     */
    private final UserService userService;

    /**
     * String corresponding to the index page.
     */
    private static final String INDEX = "index";

    /**
     * String corresponding to the name of the model attribute containing the experiment page.
     */
    private static final String EXPERIMENTS = "experiments";

    /**
     * String corresponding to the name of the model attribute containing the current page number or the corresponding
     * request parameter.
     */
    private static final String PAGE = "page";

    /**
     * String corresponding to the name of the model attribute containing the number of the last page.
     */
    private static final String LAST_PAGE = "lastPage";

    /**
     * Constructs a new home controller with the given dependencies.
     *
     * @param experimentService The experiment service to use.
     * @param userService The user service to use.
     */
    @Autowired
    public HomeController(final ExperimentService experimentService, final UserService userService) {
        this.experimentService = experimentService;
        this.userService = userService;
    }

    /**
     * Loads the index page containing basic information about the project. If the user is an administrator, a page
     * containing the latest experiments is loaded instead.
     *
     * @param httpServletRequest The servlet request.
     * @param model The model to store the loaded information in.
     * @return The index page.
     */
    @GetMapping("/")
    public String getIndexPage(final HttpServletRequest httpServletRequest, final Model model) {
        if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            Page<ExperimentTableProjection> experimentPage = experimentService.getExperimentPage(PageRequest.of(0,
                    Constants.PAGE_SIZE));
            model.addAttribute(EXPERIMENTS, experimentPage);
            model.addAttribute(PAGE, 1);
            int lastPage = experimentService.getLastPage();
            model.addAttribute(LAST_PAGE, lastPage);
        } else if (httpServletRequest.isUserInRole(Constants.ROLE_PARTICIPANT)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || authentication.getName() == null) {
                logger.error("Can't show the participant experiment page for an unauthenticated user!");
                return Constants.ERROR;
            }

            try {
                UserDTO userDTO = userService.getUser(authentication.getName());
                Page<ExperimentTableProjection> experimentPage = experimentService.getExperimentParticipantPage(
                        PageRequest.of(0, Constants.PAGE_SIZE), userDTO.getId());
                int lastPage = experimentService.getLastExperimentPage(userDTO.getId());
                model.addAttribute(EXPERIMENTS, experimentPage);
                model.addAttribute(PAGE, 1);
                model.addAttribute(LAST_PAGE, lastPage);
            } catch (NotFoundException e) {
                return Constants.ERROR;
            }
        }

        return INDEX;
    }

    /**
     * Loads the next experiment page from the database. If the current page is the last page, the error page is
     * displayed instead.
     *
     * @param model The model to store the loaded information in.
     * @param currentPage The page currently being displayed.
     * @return The index page on success, or the error page otherwise.
     */
    @GetMapping("/next")
    @Secured(Constants.ROLE_ADMIN)
    public String getNextPage(@RequestParam(PAGE) final String currentPage, final Model model) {
        if (currentPage == null) {
            return Constants.ERROR;
        }

        int current = parseStringValue(currentPage);
        int last = experimentService.getLastPage();

        if (current <= -1 || current >= last) {
            return Constants.ERROR;
        }

        Page<ExperimentTableProjection> experimentPage = experimentService.getExperimentPage(PageRequest.of(current,
                Constants.PAGE_SIZE));
        current++;
        model.addAttribute(EXPERIMENTS, experimentPage);
        model.addAttribute(PAGE, current);
        model.addAttribute(LAST_PAGE, last);

        return INDEX;
    }

    /**
     * Loads the previous experiment page from the database. If the current page is the last page, the error page is
     * displayed instead.
     *
     * @param model The model to store the loaded information in.
     * @param currentPage The page currently being displayed.
     * @return The index page on success, or the error page otherwise.
     */
    @GetMapping("/previous")
    @Secured(Constants.ROLE_ADMIN)
    public String getPreviousPage(@RequestParam(PAGE) final String currentPage, final Model model) {
        if (currentPage == null) {
            return Constants.ERROR;
        }

        int current = parseStringValue(currentPage);
        int last = experimentService.getLastPage();

        if (current <= 1 || last < current) {
            return Constants.ERROR;
        }

        Page<ExperimentTableProjection> experimentPage = experimentService.getExperimentPage(PageRequest.of(current - 2,
                Constants.PAGE_SIZE));
        current--;
        model.addAttribute(EXPERIMENTS, experimentPage);
        model.addAttribute(PAGE, current);
        model.addAttribute(LAST_PAGE, last);

        return INDEX;
    }

    /**
     * Loads the first experiment page from the database.
     *
     * @param model The model to store the loaded information in.
     * @return The index page on success, or the error page otherwise.
     */
    @GetMapping("/first")
    @Secured(Constants.ROLE_ADMIN)
    public String getFirstPage(final Model model) {
        int last = experimentService.getLastPage();

        Page<ExperimentTableProjection> experimentPage = experimentService.getExperimentPage(PageRequest.of(0,
                Constants.PAGE_SIZE));
        model.addAttribute(EXPERIMENTS, experimentPage);
        model.addAttribute(PAGE, 1);
        model.addAttribute(LAST_PAGE, last);

        return INDEX;
    }

    /**
     * Loads the last experiment page from the database.
     *
     * @param model The model to store the loaded information in.
     * @return The index page on success, or the error page otherwise.
     */
    @GetMapping("/last")
    @Secured(Constants.ROLE_ADMIN)
    public String getLastPage(final Model model) {
        int last = experimentService.getLastPage();

        Page<ExperimentTableProjection> experimentPage = experimentService.getExperimentPage(PageRequest.of(--last,
                Constants.PAGE_SIZE));
        last++;
        model.addAttribute(EXPERIMENTS, experimentPage);
        model.addAttribute(PAGE, last);
        model.addAttribute(LAST_PAGE, last);

        return INDEX;
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
     * @param id The experiment id.
     * @param model The model used to store the message to be displayed on the page.
     * @return The experiment finish page.
     */
    @GetMapping("/finish")
    public String getExperimentFinishPage(@RequestParam("id") final String id, final Model model) {
        if (id == null) {
            logger.error("Cannot load experiment finish page of experiment with id null!");
            return Constants.ERROR;
        }

        int experimentId = parseStringValue(id);
        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());

        if (experimentId < Constants.MIN_ID) {
            logger.error("Cannot load experiment finish page for invalid id " + experimentId + "!");
            return Constants.ERROR;
        }

        try {
            ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);
            model.addAttribute("thanks", experimentDTO.getPostscript() != null ? experimentDTO.getPostscript()
                    : resourceBundle.getString("thanks"));
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
        return "password-reset";
    }

    /**
     * Returns the corresponding int value of the given string, or -1, if the value is not a number.
     *
     * @param value The value in its string representation.
     * @return The corresponding int value, or -1.
     */
    private int parseStringValue(final String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

}
