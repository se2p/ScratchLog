package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.MailService;
import fim.unipassau.de.scratch1984.application.service.ParticipantService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.Secrets;
import fim.unipassau.de.scratch1984.util.validation.EmailValidator;
import fim.unipassau.de.scratch1984.util.validation.UsernameValidator;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.annotation.Secured;
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

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
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

        if (sendEmail(userDTO.getEmail(), userLanguage.getString("participant_email_subject"), templateModel,
                "participant-email")) {
            return "redirect:/experiment?id=" + id;
        } else {
            return ERROR;
        }
    }

    /**
     * Sends the given email template to the given addresses. If the {@link MailService} fails to send the message
     * three consecutive times, it stops.
     *
     * @param to The recipient of the email.
     * @param subject The subject of this email.
     * @param templateModel The template model containing additional properties.
     * @param template The name of the mail template to use.
     * @return {@code true} if the email was sent successfully, or {@code false} otherwise.
     */
    private boolean sendEmail(final String to, final String subject, final Map<String, Object> templateModel,
                              final String template) {
        int tries = 0;

        while (tries < Constants.MAX_EMAIL_TRIES) {
            try {
                mailService.sendTemplateMessage(to, null, null, null, subject, templateModel, template);
                return true;
            } catch (MessagingException e) {
                tries++;
                logger.error("Failed to send message to address " + to + " on try #" + tries + "!", e);
            }
        }

        return false;
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
