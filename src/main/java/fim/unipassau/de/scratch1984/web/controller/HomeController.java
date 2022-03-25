package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentTableProjection;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.NumberParser;
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
            Page<ExperimentTableProjection> experimentPage = experimentService.getExperimentPage(PageRequest.of(0,
                    Constants.PAGE_SIZE));
            int lastPage = experimentService.getLastPage();
            addModelInfo(experimentPage, 1, lastPage, model);
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
                addModelInfo(experimentPage, 1, lastPage, model);
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
     * @param httpServletRequest The servlet request.
     * @param model The model to store the loaded information in.
     * @param currentPage The page currently being displayed.
     * @return The index page on success, or the error page otherwise.
     */
    @GetMapping("/next")
    @Secured(Constants.ROLE_PARTICIPANT)
    public String getNextPage(@RequestParam(PAGE) final String currentPage,
                              final HttpServletRequest httpServletRequest, final Model model) {
        if (currentPage == null) {
            return Constants.ERROR;
        }

        int current = NumberParser.parseNumber(currentPage);
        int last;
        Page<ExperimentTableProjection> experimentPage;

        if (current <= -1) {
            return Constants.ERROR;
        }

        if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            last = experimentService.getLastPage();

            if (current >= last) {
                return Constants.ERROR;
            }

            experimentPage = experimentService.getExperimentPage(PageRequest.of(current, Constants.PAGE_SIZE));
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || authentication.getName() == null) {
                logger.error("Can't show the participant experiment page for an unauthenticated user!");
                return Constants.ERROR;
            }

            try {
                UserDTO userDTO = userService.getUser(authentication.getName());
                last = experimentService.getLastExperimentPage(userDTO.getId());

                if (current >= last) {
                    return Constants.ERROR;
                }

                experimentPage = experimentService.getExperimentParticipantPage(PageRequest.of(current,
                        Constants.PAGE_SIZE), userDTO.getId());
            } catch (NotFoundException e) {
                return Constants.ERROR;
            }
        }

        current++;
        addModelInfo(experimentPage, current, last, model);
        return INDEX;
    }

    /**
     * Loads the previous experiment page from the database. If the current page is the last page, the error page is
     * displayed instead.
     *
     * @param httpServletRequest The servlet request.
     * @param model The model to store the loaded information in.
     * @param currentPage The page currently being displayed.
     * @return The index page on success, or the error page otherwise.
     */
    @GetMapping("/previous")
    @Secured(Constants.ROLE_PARTICIPANT)
    public String getPreviousPage(@RequestParam(PAGE) final String currentPage,
                                  final HttpServletRequest httpServletRequest, final Model model) {
        if (currentPage == null) {
            return Constants.ERROR;
        }

        int current = NumberParser.parseNumber(currentPage);
        int last;
        Page<ExperimentTableProjection> experimentPage;

        if (current <= 1) {
            return Constants.ERROR;
        }

        if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            last = experimentService.getLastPage();

            if (last < current) {
                return Constants.ERROR;
            }

            experimentPage = experimentService.getExperimentPage(PageRequest.of(current - 2, Constants.PAGE_SIZE));
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || authentication.getName() == null) {
                logger.error("Can't show the participant experiment page for an unauthenticated user!");
                return Constants.ERROR;
            }

            try {
                UserDTO userDTO = userService.getUser(authentication.getName());
                last = experimentService.getLastExperimentPage(userDTO.getId());

                if (last < current) {
                    return Constants.ERROR;
                }

                experimentPage = experimentService.getExperimentParticipantPage(
                        PageRequest.of(current - 2, Constants.PAGE_SIZE), userDTO.getId());
            } catch (NotFoundException e) {
                return Constants.ERROR;
            }
        }

        current--;
        addModelInfo(experimentPage, current, last, model);
        return INDEX;
    }

    /**
     * Loads the first experiment page from the database.
     *
     * @param httpServletRequest The servlet request.
     * @param model The model to store the loaded information in.
     * @return The index page on success, or the error page otherwise.
     */
    @GetMapping("/first")
    @Secured(Constants.ROLE_PARTICIPANT)
    public String getFirstPage(final HttpServletRequest httpServletRequest, final Model model) {
        int last;
        Page<ExperimentTableProjection> experimentPage;

        if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            last = experimentService.getLastPage();
            experimentPage = experimentService.getExperimentPage(PageRequest.of(0,
                    Constants.PAGE_SIZE));
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || authentication.getName() == null) {
                logger.error("Can't show the participant experiment page for an unauthenticated user!");
                return Constants.ERROR;
            }

            try {
                UserDTO userDTO = userService.getUser(authentication.getName());
                experimentPage = experimentService.getExperimentParticipantPage(
                        PageRequest.of(0, Constants.PAGE_SIZE), userDTO.getId());
                last = experimentService.getLastExperimentPage(userDTO.getId());
            } catch (NotFoundException e) {
                return Constants.ERROR;
            }
        }

        addModelInfo(experimentPage, 1, last, model);
        return INDEX;
    }

    /**
     * Loads the last experiment page from the database.
     *
     * @param httpServletRequest The servlet request.
     * @param model The model to store the loaded information in.
     * @return The index page on success, or the error page otherwise.
     */
    @GetMapping("/last")
    @Secured(Constants.ROLE_PARTICIPANT)
    public String getLastPage(final HttpServletRequest httpServletRequest, final Model model) {
        int last;
        Page<ExperimentTableProjection> experimentPage;

        if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            last = experimentService.getLastPage();
            experimentPage = experimentService.getExperimentPage(PageRequest.of(--last,
                    Constants.PAGE_SIZE));
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || authentication.getName() == null) {
                logger.error("Can't show the participant experiment page for an unauthenticated user!");
                return Constants.ERROR;
            }

            try {
                UserDTO userDTO = userService.getUser(authentication.getName());
                last = experimentService.getLastExperimentPage(userDTO.getId());
                experimentPage = experimentService.getExperimentParticipantPage(
                        PageRequest.of(--last, Constants.PAGE_SIZE), userDTO.getId());
            } catch (NotFoundException e) {
                return Constants.ERROR;
            }
        }

        last++;
        addModelInfo(experimentPage, last, last, model);
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

        int experimentId = NumberParser.parseNumber(id);
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
        return Constants.MAIL_SERVER ? "password-reset" : Constants.ERROR;
    }

    /**
     * Adds the required page numbers and experiment information to the {@link Model} to display the experiment table on
     * the index page.
     *
     * @param experiments The current experiment page.
     * @param currentPage The number of the current page.
     * @param lastPage The number of the last page.
     * @param model The model used to save the info.
     */
    private void addModelInfo(final Page<ExperimentTableProjection> experiments, final int currentPage,
                              final int lastPage, final Model model) {
        model.addAttribute(EXPERIMENTS, experiments);
        model.addAttribute(PAGE, currentPage);
        model.addAttribute(LAST_PAGE, lastPage);
    }

}
