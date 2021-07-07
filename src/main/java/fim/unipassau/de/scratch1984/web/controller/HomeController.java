package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
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
     * String corresponding to redirecting to the error page.
     */
    private static final String ERROR = "redirect:/error";

    /**
     * String corresponding to the index page.
     */
    private static final String INDEX = "index";

    /**
     * Constructs a new home controller with the given dependencies.
     *
     * @param experimentService The experiment service to use.
     */
    @Autowired
    public HomeController(final ExperimentService experimentService) {
        this.experimentService = experimentService;
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
        if (httpServletRequest.isUserInRole("ROLE_ADMIN")) {
            Page<Experiment> experimentPage = experimentService.getExperimentPage(PageRequest.of(0,
                    Constants.PAGE_SIZE));
            model.addAttribute("experiments", experimentPage);
            model.addAttribute("page", 1);
            int lastPage = experimentService.getLastPage() + 1;
            model.addAttribute("lastPage", lastPage);
        }

        return INDEX;
    }

    /**
     * Loads the the next experiment page from the database. If the current page is the last page, the error page is
     * displayed instead.
     *
     * @param model The model to store the loaded information in.
     * @param currentPage The page currently being displayed.
     * @return The index page on success, or the error page otherwise.
     */
    @GetMapping("/next")
    @Secured("ROLE_ADMIN")
    public String getNextPage(@RequestParam("page") final String currentPage, final Model model) {
        if (currentPage == null) {
            return ERROR;
        }

        int current = parseStringValue(currentPage);
        int last = experimentService.getLastPage() + 1;

        if (current <= -1 || current >= last) {
            return ERROR;
        }

        Page<Experiment> experimentPage = experimentService.getExperimentPage(PageRequest.of(current,
                Constants.PAGE_SIZE));
        current++;
        model.addAttribute("experiments", experimentPage);
        model.addAttribute("page", current);
        model.addAttribute("lastPage", last);

        return INDEX;
    }

    /**
     * Loads the the previous experiment page from the database. If the current page is the last page, the error page is
     * displayed instead.
     *
     * @param model The model to store the loaded information in.
     * @param currentPage The page currently being displayed.
     * @return The index page on success, or the error page otherwise.
     */
    @GetMapping("/previous")
    @Secured("ROLE_ADMIN")
    public String getPreviousPage(@RequestParam("page") final String currentPage, final Model model) {
        if (currentPage == null) {
            return ERROR;
        }

        int current = parseStringValue(currentPage);
        int last = experimentService.getLastPage() + 1;

        if (current <= 1 || last < current) {
            return ERROR;
        }

        Page<Experiment> experimentPage = experimentService.getExperimentPage(PageRequest.of(current - 2,
                Constants.PAGE_SIZE));
        current--;
        model.addAttribute("experiments", experimentPage);
        model.addAttribute("page", current);
        model.addAttribute("lastPage", last);

        return INDEX;
    }

    /**
     * Loads the the first experiment page from the database.
     *
     * @param model The model to store the loaded information in.
     * @return The index page on success, or the error page otherwise.
     */
    @GetMapping("/first")
    @Secured("ROLE_ADMIN")
    public String getFirstPage(final Model model) {
        int last = experimentService.getLastPage() + 1;

        Page<Experiment> experimentPage = experimentService.getExperimentPage(PageRequest.of(0,
                Constants.PAGE_SIZE));
        model.addAttribute("experiments", experimentPage);
        model.addAttribute("page", 1);
        model.addAttribute("lastPage", last);

        return INDEX;
    }

    /**
     * Loads the the last experiment page from the database.
     *
     * @param model The model to store the loaded information in.
     * @return The index page on success, or the error page otherwise.
     */
    @GetMapping("/last")
    @Secured("ROLE_ADMIN")
    public String getLastPage(final Model model) {
        int last = experimentService.getLastPage() + 1;

        Page<Experiment> experimentPage = experimentService.getExperimentPage(PageRequest.of(--last,
                Constants.PAGE_SIZE));
        last++;
        model.addAttribute("experiments", experimentPage);
        model.addAttribute("page", last);
        model.addAttribute("lastPage", last);

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
            return ERROR;
        }

        int experimentId = parseStringValue(id);
        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());

        if (experimentId < Constants.MIN_ID) {
            logger.error("Cannot load experiment finish page for invalid id " + experimentId + "!");
            return ERROR;
        }

        try {
            ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);
            model.addAttribute("thanks", experimentDTO.getPostscript() != null ? experimentDTO.getPostscript()
                    : resourceBundle.getString("thanks"));
        } catch (NotFoundException e) {
            return ERROR;
        }

        return "experiment-finish";
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
