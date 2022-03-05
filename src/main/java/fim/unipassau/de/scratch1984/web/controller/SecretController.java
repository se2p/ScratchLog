package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * The controller responsible for displaying created user secrets if no mail server has been configured.
 */
@Controller
@RequestMapping("/secret")
public class SecretController {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(SecretController.class);

    /**
     * The user service to use for user management.
     */
    private final UserService userService;

    /**
     * String corresponding to the secret page.
     */
    private static final String SECRET = "secret";

    /**
     * Constructs a new secret controller with the given dependencies.
     *
     * @param userService The {@link UserService} to use.
     */
    @Autowired
    public SecretController(final UserService userService) {
        this.userService = userService;
    }

    /**
     * Generates the participant link for the given user based on the user's secret and returns the secret page, to
     * display this information. If no user with a corresponding id could be found, the user's secret is null or the
     * passed parameters are invalid, the error page is returned instead.
     *
     * @param user The id of the user whose participant link is to be displayed.
     * @param experiment The id of the experiment in which the user is participating.
     * @param model The {@link Model} to hold the information.
     * @return The secret page on success or the error page otherwise.
     */
    @GetMapping
    @Secured(Constants.ROLE_ADMIN)
    public String displaySecret(@RequestParam("user") final String user,
                                @RequestParam("experiment") final String experiment, final Model model) {
        if (user == null || experiment == null || user.trim().isBlank() || experiment.trim().isBlank()) {
            logger.error("Cannot display secret for user or experiment id null or blank!");
            return Constants.ERROR;
        }

        int userId = parseId(user);
        int experimentId = parseId(experiment);

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot display secret for user with invalid id " + user + " or experiment with invalid id "
                    + experiment + "!");
            return Constants.ERROR;
        }

        try {
            UserDTO userDTO = userService.getUserById(userId);

            if (userDTO.getSecret() == null) {
                logger.error("Cannot display newly created secret for user " + userDTO.getId() + " as the user's "
                        + "secret is null!");
                return Constants.ERROR;
            }

            addModelInfo(List.of(userDTO), experimentId, model);
            return SECRET;
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Finds all users who have not yet finished the given experiment and generates the basic participation link that
     * to display each user's individual participation link on the secret page. If no experiment with a corresponding id
     * could be found or passed parameter is invalid, the error page is returned instead.
     *
     * @param experiment The id of the experiment.
     * @param model The {@link Model} to hold the information.
     * @return The secret page on success or the error page otherwise.
     */
    @GetMapping("/list")
    @Secured(Constants.ROLE_ADMIN)
    public String displaySecrets(@RequestParam("experiment") final String experiment, final Model model) {
        if (experiment == null || experiment.trim().isBlank()) {
            logger.error("Cannot display secrets for experiment id null or blank!");
            return Constants.ERROR;
        }

        int experimentId = parseId(experiment);

        if (experimentId < Constants.MIN_ID) {
            logger.error("Cannot display secrets for experiment with invalid id " + experiment + "!");
            return Constants.ERROR;
        }

        try {
            List<UserDTO> reactivatedAccounts = userService.findUnfinishedUsers(experimentId);
            addModelInfo(reactivatedAccounts, experimentId, model);
            return SECRET;
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Returns the corresponding int value of the given id, or -1, if the id is not a number.
     *
     * @param id The id in its string representation.
     * @return The corresponding int value, or -1.
     */
    private int parseId(final String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Adds the passed arguments as information to the given model as well as the basic link used for participant
     * authentication.
     *
     * @param userDTOS The list of {@link UserDTO}s whose new authentication link is to be displayed.
     * @param experimentId The id of the experiment in which the users are participating.
     * @param model The {@link Model} to hold the information.
     */
    private void addModelInfo(final List<UserDTO> userDTOS, final int experimentId, final Model model) {
        String experimentUrl = Constants.BASE_URL + "/users/authenticate?id=" + experimentId + "&secret=";
        model.addAttribute("users", userDTOS);
        model.addAttribute("link", experimentUrl);
        model.addAttribute("experiment", experimentId);
    }

}
