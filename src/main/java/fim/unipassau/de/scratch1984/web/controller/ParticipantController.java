package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.MailService;
import fim.unipassau.de.scratch1984.application.service.ParticipantService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.MarkdownHandler;
import fim.unipassau.de.scratch1984.util.Secrets;
import fim.unipassau.de.scratch1984.util.validation.EmailValidator;
import fim.unipassau.de.scratch1984.util.validation.UsernameValidator;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import fim.unipassau.de.scratch1984.web.dto.ParticipantDTO;
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
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The controller for managing participants.
 */
@Controller
@RequestMapping(value = "/participant")
public class ParticipantController {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(ParticipantController.class);

    /**
     * The user service to use for user management.
     */
    private final UserService userService;

    /**
     * The experiment service to use for experiment management.
     */
    private final ExperimentService experimentService;

    /**
     * The participant service to use for participant management.
     */
    private final ParticipantService participantService;

    /**
     * The mail service to use for sending emails.
     */
    private final MailService mailService;

    /**
     * String corresponding to redirecting to the error page.
     */
    private static final String ERROR = "redirect:/error";

    /**
     * String corresponding to the participant page.
     */
    private static final String PARTICIPANT = "participant";

    /**
     * String corresponding to the experiment page.
     */
    private static final String EXPERIMENT = "experiment";

    /**
     * String corresponding to redirecting to the experiment page.
     */
    private static final String REDIRECT_EXPERIMENT = "redirect:/experiment?id=";

    /**
     * Constructs a new participant controller with the given dependencies.
     *
     * @param experimentService The experiment service to use.
     * @param userService The user service to use.
     * @param participantService The participant service to use.
     * @param mailService The mail service to use.
     */
    @Autowired
    public ParticipantController(final UserService userService, final ExperimentService experimentService,
                                 final ParticipantService participantService, final MailService mailService) {
        this.userService = userService;
        this.experimentService = experimentService;
        this.participantService = participantService;
        this.mailService = mailService;
    }

    /**
     * Returns the participant page to add a new participant to the given experiment. If the experiment id passed in the
     * request parameter is invalid, the user is redirected to the error page instead.
     *
     * @param experimentId The experiment id.
     * @param model The model used for the id.
     * @return The participant page on success, or the error page otherwise.
     */
    @GetMapping("/add")
    @Secured("ROLE_ADMIN")
    public String getParticipantForm(@RequestParam(value = "id") final String experimentId, final Model model) {
        if (experimentId == null || experimentId.trim().isBlank()) {
            logger.error("Cannot add new participant for experiment with id null or blank!");
            return ERROR;
        }

        int id = parseId(experimentId);

        if (id < Constants.MIN_ID) {
            logger.error("Cannot add new participant for experiment with invalid id " + id + "!");
            return ERROR;
        }

        try {
            ExperimentDTO experimentDTO = experimentService.getExperiment(id);

            if (!experimentDTO.isActive()) {
                return ERROR;
            }
        } catch (NotFoundException e) {
            return ERROR;
        }

        int lastId = userService.findLastId() + 1;
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("participant" + lastId);
        model.addAttribute("experiment", id);
        model.addAttribute("userDTO", userDTO);

        return PARTICIPANT;
    }

    /**
     * Creates a new user with the given values and adds a participant relation for the given experiment. If the
     * creation was successful, the an email is sent to the specified email address of the new user containing a link
     * to participate in the experiment, and the experiment page is returned. If anything went wrong during the process,
     * the user is redirected to the error page instead.
     *
     * @param experimentId The id of the experiment in which the user should participate.
     * @param userDTO The {@link UserDTO} containing the username, email and language of the user to be created.
     * @param model The model to use.
     * @param bindingResult The {@link BindingResult} for returning information on invalid user input.
     * @param httpServletRequest The {@link HttpServletRequest} to get the application's base URL.
     * @return The experiment page on success, or the error page otherwise.
     */
    @PostMapping("/add")
    @Secured("ROLE_ADMIN")
    public String addParticipant(@RequestParam(value = "id") final String experimentId,
                                 @ModelAttribute("userDTO") final UserDTO userDTO, final Model model,
                                 final BindingResult bindingResult, final HttpServletRequest httpServletRequest) {
        if (userDTO.getUsername() == null || userDTO.getEmail() == null || experimentId == null) {
            logger.error("The new username, email and experiment id cannot be null!");
            return ERROR;
        }

        int id = parseId(experimentId);

        if (id < Constants.MIN_ID) {
            logger.error("Cannot add new participant for experiment with invalid id " + id + "!");
            return ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        validateUsername(userDTO.getUsername(), bindingResult, resourceBundle);
        validateUpdateEmail(userDTO.getEmail(), bindingResult, resourceBundle);

        if (bindingResult.hasErrors()) {
            model.addAttribute("experiment", id);
            return PARTICIPANT;
        }

        String secret = Secrets.generateRandomBytes(Constants.SECRET_LENGTH);
        userDTO.setRole(UserDTO.Role.PARTICIPANT);
        userDTO.setSecret(secret);
        UserDTO saved = userService.saveUser(userDTO);

        try {
            participantService.saveParticipant(saved.getId(), id);
        } catch (NotFoundException e) {
            return ERROR;
        }

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(httpServletRequest).replacePath(null).build()
                .toUriString();
        String experimentUrl = baseUrl + "/users/authenticate?id=" + id + "&secret=" + secret;
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("baseUrl", baseUrl);
        templateModel.put("secret", experimentUrl);
        ResourceBundle userLanguage = ResourceBundle.getBundle("i18n/messages",
                getLocaleFromLanguage(userDTO.getLanguage()));

        if (mailService.sendEmail(userDTO.getEmail(), userLanguage.getString("participant_email_subject"),
                templateModel, "participant-email")) {
            return REDIRECT_EXPERIMENT + id;
        } else {
            return ERROR;
        }
    }

    /**
     * Deletes the participant for the experiment with the given id whose username or email match the given input
     * string. If no experiment with the corresponding id can be found, or the request parameters do not meet the
     * requirements, the user is redirected to the error page instead. If no corresponding user can be found, or the
     * user is not a participant in the given experiment, the experiment page is returned to display an error message.
     *
     * @param participant The username or email to search for.
     * @param id The experiment id.
     * @param model The model used for the id.
     * @return A redirection to the experiment page on success, or the error or experiment page otherwise.
     */
    @GetMapping("/delete")
    @Secured("ROLE_ADMIN")
    public String deleteParticipant(@RequestParam("participant") final String participant,
                                    @RequestParam("id") final String id, final Model model) {
        if (participant == null || id == null || participant.trim().isBlank()
                || participant.length() > Constants.LARGE_FIELD) {
            logger.error("Cannot delete participant with invalid id or input string!");
            return ERROR;
        }

        int experimentId = parseId(id);

        if (experimentId < Constants.MIN_ID) {
            logger.error("Cannot delete a participant for experiment with invalid id " + id + "!");
            return ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        ExperimentDTO experimentDTO;
        UserDTO userDTO = userService.getUserByUsernameOrEmail(participant);

        try {
            experimentDTO = experimentService.getExperiment(experimentId);
        } catch (NotFoundException e) {
            return ERROR;
        }

        if (userDTO == null) {
            model.addAttribute("error", resourceBundle.getString("user_not_found"));
            addModelInfo(experimentDTO, model);
            return EXPERIMENT;
        } else if (!userDTO.getRole().equals(UserDTO.Role.PARTICIPANT)) {
            model.addAttribute("error", resourceBundle.getString("user_not_participant"));
        } else if (!userService.existsParticipant(userDTO.getId(), experimentId)) {
            model.addAttribute("error", resourceBundle.getString("no_participant_entry"));
        } else if (!experimentDTO.isActive()) {
            model.addAttribute("error", resourceBundle.getString("experiment_closed"));
        }

        if (model.getAttribute("error") != null) {
            addModelInfo(experimentDTO, model);
            return EXPERIMENT;
        }

        try {
            userDTO.setSecret(null);
            userDTO.setActive(false);
            UserDTO saved = userService.updateUser(userDTO);
            participantService.deleteParticipant(saved.getId(), experimentId);
        } catch (NotFoundException e) {
            return ERROR;
        }

        return REDIRECT_EXPERIMENT + experimentId;
    }

    /**
     * Starts the experiment with the given id for the currently authenticated user, if they are registered as
     * participants and have not yet started the experiment. If these requirements are not met, no corresponding
     * participant could be found, or the user is an administrator, they are redirected to the error page instead.
     *
     * @param id The experiment id.
     * @param httpServletRequest The servlet request.
     * @return Opens the scratch GUI on success, or redirects to the error page otherwise.
     */
    @GetMapping("/start")
    @Secured("ROLE_PARTICIPANT")
    public String startExperiment(@RequestParam("id") final String id, final HttpServletRequest httpServletRequest) {
        if (id == null) {
            logger.error("Cannot start experiment with invalid id null!");
            return ERROR;
        }

        int experimentId = parseId(id);

        if (experimentId < Constants.MIN_ID) {
            logger.error("Cannot start experiment with invalid id " + id + "!");
            return ERROR;
        }

        if (httpServletRequest.isUserInRole("ROLE_ADMIN")) {
            logger.error("An administrator tried to participate in the experiment with id " + id + "!");
            return ERROR;
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getName() == null) {
                logger.error("Cannot start the experiment for a user with authentication with name null!");
                return ERROR;
            }

            try {
                UserDTO userDTO = userService.getUser(authentication.getName());
                ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);
                ParticipantDTO participantDTO = participantService.getParticipant(experimentId, userDTO.getId());

                if (!experimentDTO.isActive()) {
                    logger.error("Cannot start experiment with id " + experimentId + " for user with id "
                            + userDTO.getId() + " since the experiment is closed!");
                    return ERROR;
                }
                if (!userDTO.isActive() || userDTO.getSecret() == null) {
                    logger.error("Cannot start experiment for user with id " + userDTO.getId() + " since their account "
                            + "is inactive or their secret null!");
                    return ERROR;
                }
                if (participantDTO.getStart() != null || participantDTO.getEnd() != null) {
                    logger.error("The user with id " + userDTO.getId() + " tried to start the experiment with id "
                            + experimentId + " even though they already started it once!");
                    return ERROR;
                }

                participantDTO.setStart(LocalDateTime.now());

                if (participantService.updateParticipant(participantDTO)) {
                    return "redirect:http://localhost:8601?uid=" + participantDTO.getUser() + "&expid="
                            + participantDTO.getExperiment();
                } else {
                    logger.error("Failed to update the starting time of participant with user id "
                            + participantDTO.getUser() + " for experiment with id " + participantDTO.getExperiment()
                            + "!");
                    return ERROR;
                }
            } catch (NotFoundException e) {
                return ERROR;
            }
        }
    }

    /**
     * Stops the experiment with the given id for the user with the given id, if a corresponding participant relation
     * exists
     * participants and have not yet started the experiment. If these requirements are not met, no corresponding
     * participant could be found, or the user is an administrator, they are redirected to the error page instead.
     *
     * @param experiment The experiment id.
     * @param user The user id.
     * @param httpServletRequest The servlet request.
     * @return Redirects to the finish page on success, or to the error page otherwise.
     */
    @GetMapping("/stop")
    public String stopExperiment(@RequestParam("experiment") final String experiment,
                                 @RequestParam("user") final String user, final HttpServletRequest httpServletRequest) {
        if (experiment == null || user == null) {
            logger.error("Cannot stop experiment with invalid user or experiment id null!");
            return ERROR;
        }

        int experimentId = parseId(experiment);
        int userId = parseId(user);

        if (experimentId < Constants.MIN_ID || userId < Constants.MIN_ID) {
            logger.error("Cannot stop experiment with invalid user id " + user + " or experiment id " + experiment
                    + " !");
            return ERROR;
        }

        if (httpServletRequest.isUserInRole("ROLE_ADMIN")) {
            logger.error("An administrator tried to finish the experiment with id " + experiment + " for user" + user
                    + "!");
            return ERROR;
        }

        try {
            UserDTO userDTO = userService.getUserById(userId);
            ParticipantDTO participantDTO = participantService.getParticipant(experimentId, userDTO.getId());

            if (participantDTO.getStart() == null || participantDTO.getEnd() != null) {
                logger.error("Cannot end experiment for participant with id " + participantDTO.getUser()
                        + " and experiment " + participantDTO.getExperiment() + " with invalid starting time "
                        + participantDTO.getStart() + " or finishing time " + participantDTO.getEnd() + "!");
                clearSecurityContext(httpServletRequest);
                return ERROR;
            }

            participantDTO.setEnd(LocalDateTime.now());
            userDTO.setActive(false);
            userDTO.setSecret(null);

            if (participantService.updateParticipant(participantDTO)) {
                userService.saveUser(userDTO);
                clearSecurityContext(httpServletRequest);
                return "redirect:/finish?id=" + experimentId;
            } else {
                logger.error("Failed to update the finishing time of participant with user id "
                        + participantDTO.getUser() + " for experiment with id " + participantDTO.getExperiment() + "!");
                clearSecurityContext(httpServletRequest);
                return ERROR;
            }
        } catch (NotFoundException e) {
            clearSecurityContext(httpServletRequest);
            return ERROR;
        }
    }

    /**
     * Clears the current security context and invalidates the http session on user login and logout.
     *
     * @param httpServletRequest The {@link HttpServletRequest} request containing the current user session.
     */
    private void clearSecurityContext(final HttpServletRequest httpServletRequest) {
        SecurityContextHolder.clearContext();
        HttpSession session = httpServletRequest.getSession(false);

        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Retrieves the first participant page information from the database and adds the page to the {@link Model} along
     * with the {@link ExperimentDTO} and the last page.
     *
     * @param experimentDTO The current experiment dto.
     * @param model The model used to save the information.
     */
    private void addModelInfo(final ExperimentDTO experimentDTO, final Model model) {
        if (experimentDTO.getInfo() != null) {
            experimentDTO.setInfo(MarkdownHandler.toHtml(experimentDTO.getInfo()));
        }

        int last = experimentService.getLastParticipantPage(experimentDTO.getId()) + 1;

        Page<Participant> participants = participantService.getParticipantPage(experimentDTO.getId(),
                PageRequest.of(0, Constants.PAGE_SIZE));
        model.addAttribute("page", 1);
        model.addAttribute("lastPage", last);
        model.addAttribute("experimentDTO", experimentDTO);
        model.addAttribute("participants", participants);
    }

    /**
     * Validates the given username when creating a new participant.
     *
     * @param username The username to be validated.
     * @param bindingResult The binding result for returning information on invalid user input.
     * @param resourceBundle The resource bundle for error translation.
     */
    private void validateUsername(final String username, final BindingResult bindingResult,
                                  final ResourceBundle resourceBundle) {
        String usernameValidation = UsernameValidator.validate(username);

        if (usernameValidation != null) {
            bindingResult.addError(createFieldError("userDTO", "username", usernameValidation, resourceBundle));
        } else if (userService.existsUser(username)) {
            bindingResult.addError(createFieldError("userDTO", "username", "username_exists", resourceBundle));
        }
    }

    /**
     * Validates the given email when creating a new participant.
     *
     * @param email The email to be validated.
     * @param bindingResult The binding result for returning information on invalid user input.
     * @param resourceBundle The resource bundle for error translation.
     */
    private void validateUpdateEmail(final String email, final BindingResult bindingResult,
                                     final ResourceBundle resourceBundle) {
        String emailValidation = EmailValidator.validate(email);

        if (emailValidation != null) {
            bindingResult.addError(createFieldError("userDTO", "email", emailValidation, resourceBundle));
        } else if (userService.existsEmail(email)) {
            bindingResult.addError(createFieldError("userDTO", "email", "email_exists", resourceBundle));
        }
    }

    /**
     * Creates a new field error with the given parameters.
     *
     * @param objectName The name of the object.
     * @param field The field to which the error applies.
     * @param error The error message string.
     * @param resourceBundle The resource bundle to retrieve the error message in the current language.
     * @return The new field error.
     */
    private FieldError createFieldError(final String objectName, final String field, final String error,
                                        final ResourceBundle resourceBundle) {
        return new FieldError(objectName, field, resourceBundle.getString(error));
    }

    /**
     * Returns the proper {@link Locale} based on the user's preferred language settings.
     *
     * @param language The user's preferred language.
     * @return The corresponding locale, or English as a default value.
     */
    private Locale getLocaleFromLanguage(final UserDTO.Language language) {
        if (language == UserDTO.Language.GERMAN) {
            return Locale.GERMAN;
        }
        return Locale.ENGLISH;
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

}
