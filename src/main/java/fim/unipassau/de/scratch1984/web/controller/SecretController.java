package fim.unipassau.de.scratch1984.web.controller;

import com.opencsv.CSVWriter;
import fim.unipassau.de.scratch1984.application.exception.IncompleteDataException;
import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.util.ApplicationProperties;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.NumberParser;
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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(SecretController.class);

    /**
     * The user service to use for user management.
     */
    private final UserService userService;

    /**
     * The experiment service to use for experiment management.
     */
    private final ExperimentService experimentService;

    /**
     * String corresponding to the secret page.
     */
    private static final String SECRET = "secret";

    /**
     * Constructs a new secret controller with the given dependencies.
     *
     * @param userService The {@link UserService} to use.
     * @param experimentService The {@link ExperimentService} to use.
     */
    @Autowired
    public SecretController(final UserService userService, final ExperimentService experimentService) {
        this.userService = userService;
        this.experimentService = experimentService;
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
            LOGGER.error("Cannot display secret for user or experiment id null or blank!");
            return Constants.ERROR;
        }

        int userId = NumberParser.parseNumber(user);
        int experimentId = NumberParser.parseNumber(experiment);

        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            LOGGER.error("Cannot display secret for user with invalid id " + user + " or experiment with invalid id "
                    + experiment + "!");
            return Constants.ERROR;
        }

        try {
            if (isInactive(experimentId)) {
                model.addAttribute("inactive", true);
                addModelInfo(new ArrayList<>(), experimentId, model);
            } else {
                UserDTO userDTO = userService.getUserById(userId);

                if (userDTO.getSecret() == null) {
                    LOGGER.error("Cannot display newly created secret for user " + userDTO.getId() + " as the user's "
                            + "secret is null!");
                    return Constants.ERROR;
                }

                addModelInfo(List.of(userDTO), experimentId, model);
            }

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
            LOGGER.error("Cannot display secrets for experiment id null or blank!");
            return Constants.ERROR;
        }

        int experimentId = NumberParser.parseNumber(experiment);

        if (experimentId < Constants.MIN_ID) {
            LOGGER.error("Cannot display secrets for experiment with invalid id " + experiment + "!");
            return Constants.ERROR;
        }

        try {
            if (isInactive(experimentId)) {
                model.addAttribute("inactive", true);
                addModelInfo(new ArrayList<>(), experimentId, model);
            } else {
                List<UserDTO> reactivatedAccounts = userService.findUnfinishedUsers(experimentId);
                addModelInfo(reactivatedAccounts, experimentId, model);
            }

            return SECRET;
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Retrieves all participation links for users participating in the experiment with the given id or only that for
     * the particular user with the given id and makes the information available for download in a CSV file.
     *
     * @param experiment The id of the experiment.
     * @param user The (optional) id of the user.
     * @param httpServletResponse The {@link HttpServletResponse} returning the file.
     * @throws IncompleteDataException if the passed experiment id is invalid.
     * @throws RuntimeException if an {@link IOException} occurred.
     */
    @GetMapping("/csv")
    @Secured(Constants.ROLE_ADMIN)
    public void downloadParticipationLinks(@RequestParam("experiment") final String experiment,
                                           @RequestParam(required = false, value = "user") final String user,
                                           final HttpServletResponse httpServletResponse) {
        if (experiment == null || experiment.trim().isBlank()) {
            throw new IncompleteDataException("Cannot download participation links for experiment id null or blank!");
        }

        int experimentId = NumberParser.parseNumber(experiment);

        if (experimentId < Constants.MIN_ID) {
            throw new IncompleteDataException("Cannot download participation links for experiment with invalid id "
                    + experiment + "!");
        }

        List<String[]> users = prepareCSVData(experimentId, user);

        try {
            String fileName = user == null ? "experiment_" + experimentId : "experiment_" + experimentId + "_user_"
                    + user;
            httpServletResponse.setContentType("text/csv");
            httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".csv");
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            CSVWriter csvWriter = new CSVWriter(httpServletResponse.getWriter());
            csvWriter.writeAll(users);
        } catch (IOException e) {
            throw new RuntimeException("Could not download participation links due to IOException!", e);
        }
    }

    /**
     * Checks whether the experiment with the given id is currently active.
     *
     * @param experimentId The id of the experiment.
     * @return {@code true} if the experiment is inactive or {@code false otherwise}.
     */
    private boolean isInactive(final int experimentId) {
        return !experimentService.getExperiment(experimentId).isActive();
    }

    /**
     * Prepares the data to be written to the CSV file for the experiment with the given id. If the passed user string
     * represents a valid user id, only the information of that single user is returned. Otherwise, information on all
     * users who have not yet finished the experiment is returned.
     *
     * @param experimentId The id of the experiment.
     * @param user The id of the user or null.
     * @return A list of string arrays containing the information.
     * @throws IncompleteDataException if the passed user id is invalid.
     */
    private List<String[]> prepareCSVData(final int experimentId, final String user) {
        if (user != null) {
            int userId = NumberParser.parseNumber(user);

            if (userId < Constants.MIN_ID) {
                throw new IncompleteDataException("Cannot download participation link for user with invalid id "
                        + user + "!");
            }
        }

        List<UserDTO> users = user != null ? List.of(userService.getUserById(NumberParser.parseNumber(user)))
                : userService.findUnfinishedUsers(experimentId);
        return transformUserData(users, experimentId);
    }

    /**
     * Returns a list of string arrays containing the id, username and participation link of each user passed in the
     * given list for the CSV file.
     *
     * @param userDTOS The list of {@link UserDTO}.
     * @param experimentId The id of the experiment in which the users are participating.
     * @return A list of string arrays containing the information.
     */
    private List<String[]> transformUserData(final List<UserDTO> userDTOS, final int experimentId) {
        String experimentUrl = ApplicationProperties.BASE_URL + ApplicationProperties.CONTEXT_PATH
                + "/users/authenticate?id=" + experimentId + "&secret=";
        List<String[]> users = new ArrayList<>();
        String[] header = new String[]{"id", "name", "participation link"};
        users.add(header);
        userDTOS.forEach(userDTO -> users.add(new String[]{String.valueOf(userDTO.getId()), userDTO.getUsername(),
                experimentUrl + userDTO.getSecret()}));
        return users;
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
        String experimentUrl = ApplicationProperties.BASE_URL + ApplicationProperties.CONTEXT_PATH
                + "/users/authenticate?id=" + experimentId + "&secret=";
        model.addAttribute("users", userDTOS);
        model.addAttribute("link", experimentUrl);
        model.addAttribute("experiment", experimentId);
    }

}
