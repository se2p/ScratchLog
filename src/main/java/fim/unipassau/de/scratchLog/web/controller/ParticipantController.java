/*
 * Copyright (C) 2023 ScratchLog contributors
 *
 * This file is part of ScratchLog.
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ScratchLog. If not, see <http://www.gnu.org/licenses/>.
 */

package fim.unipassau.de.scratchLog.web.controller;

import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.application.service.ExperimentService;
import fim.unipassau.de.scratchLog.application.service.MailService;
import fim.unipassau.de.scratchLog.application.service.PageService;
import fim.unipassau.de.scratchLog.application.service.ParticipantService;
import fim.unipassau.de.scratchLog.application.service.UserService;
import fim.unipassau.de.scratchLog.persistence.entity.Participant;
import fim.unipassau.de.scratchLog.util.ApplicationProperties;
import fim.unipassau.de.scratchLog.util.Constants;
import fim.unipassau.de.scratchLog.util.FieldErrorHandler;
import fim.unipassau.de.scratchLog.util.MarkdownHandler;
import fim.unipassau.de.scratchLog.util.NumberParser;
import fim.unipassau.de.scratchLog.util.Secrets;
import fim.unipassau.de.scratchLog.util.enums.Language;
import fim.unipassau.de.scratchLog.util.enums.Role;
import fim.unipassau.de.scratchLog.web.dto.ExperimentDTO;
import fim.unipassau.de.scratchLog.web.dto.ParticipantDTO;
import fim.unipassau.de.scratchLog.web.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantController.class);

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
     * The page service to use for retrieving pageable tables.
     */
    private final PageService pageService;

    /**
     * The mail service to use for sending emails.
     */
    private final MailService mailService;

    /**
     * String corresponding to redirecting to the experiment page.
     */
    private static final String REDIRECT_EXPERIMENT = "redirect:/experiment?id=";

    /**
     * String corresponding to the experiment id parameter.
     */
    private static final String EXPERIMENT_PARAM = "&experiment=";

    /**
     * String corresponding to the parameter indicating the user id for the Scratch GUI.
     */
    private static final String USER_ID_PARAM = "?uid=";

    /**
     * String corresponding to the parameter indicating the experiment id for the Scratch GUI.
     */
    private static final String EXPERIMENT_ID_PARAM = "&expid=";

    /**
     * String corresponding to the parameter indicating the secret for the Scratch GUI.
     */
    private static final String SECRET_PARAM = "&secret=";

    /**
     * String corresponding to the parameter indicating a restart of an experiment.
     */
    private static final String RESTART_PARAM = "&restart=true";

    /**
     * String corresponding to the id request parameter.
     */
    private static final String ID = "id";

    /**
     * String corresponding to the error model attribute.
     */
    private static final String ERROR = "error";

    /**
     * Constructs a new participant controller with the given dependencies.
     *
     * @param experimentService The experiment service to use.
     * @param userService The user service to use.
     * @param participantService The participant service to use.
     * @param pageService The page service to use.
     * @param mailService The mail service to use.
     */
    @Autowired
    public ParticipantController(final UserService userService, final ExperimentService experimentService,
                                 final ParticipantService participantService, final PageService pageService,
                                 final MailService mailService) {
        this.userService = userService;
        this.experimentService = experimentService;
        this.participantService = participantService;
        this.pageService = pageService;
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
    @Secured(Constants.ROLE_ADMIN)
    public String getParticipantForm(@RequestParam(value = ID) final String experimentId, final Model model) {
        if (experimentId == null || experimentId.trim().isBlank()) {
            LOGGER.error("Cannot add new participant for experiment with id null or blank!");
            return Constants.ERROR;
        }

        int id = NumberParser.parseNumber(experimentId);

        if (id < Constants.MIN_ID) {
            LOGGER.error("Cannot add new participant for experiment with invalid id " + id + "!");
            return Constants.ERROR;
        }

        try {
            ExperimentDTO experimentDTO = experimentService.getExperiment(id);

            if (!experimentDTO.isActive() || experimentDTO.isCourseExperiment()) {
                return Constants.ERROR;
            }
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }

        int lastId = userService.findLastId() + 1;
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("participant" + lastId);
        model.addAttribute("experiment", id);
        model.addAttribute("userDTO", userDTO);

        return "participant";
    }

    /**
     * Creates a new user with the given values and adds a participant relation for the given experiment. If the
     * creation was successful, an email is sent to the specified email address of the new user containing a link
     * to participate in the experiment, and the experiment page is returned. If anything went wrong during the process,
     * the user is redirected to the error page instead.
     *
     * @param experimentId The id of the experiment in which the user should participate.
     * @param userDTO The {@link UserDTO} containing the username, email and language of the user to be created.
     * @param model The model to use.
     * @param bindingResult The {@link BindingResult} for returning information on invalid user input.
     * @return The experiment page on success, or the error page otherwise.
     */
    @PostMapping("/add")
    @Secured(Constants.ROLE_ADMIN)
    public String addParticipant(@RequestParam(value = "expId") final String experimentId,
                                 @ModelAttribute("userDTO") final UserDTO userDTO, final Model model,
                                 final BindingResult bindingResult) {
        if (userDTO.getUsername() == null || userDTO.getEmail() == null || experimentId == null) {
            LOGGER.error("The new username, email and experiment id cannot be null!");
            return Constants.ERROR;
        }

        if (userDTO.getId() != null) {
            LOGGER.error("Cannot create new user if the id is not null!");
            return Constants.ERROR;
        }

        int id = NumberParser.parseNumber(experimentId);

        if (id < Constants.MIN_ID) {
            LOGGER.error("Cannot add new participant for experiment with invalid id " + id + "!");
            return Constants.ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        validateUsername(userDTO.getUsername(), bindingResult, resourceBundle);
        validateUpdateEmail(userDTO.getEmail(), bindingResult, resourceBundle);

        if (bindingResult.hasErrors()) {
            model.addAttribute("experiment", id);
            return "participant";
        }

        String secret = Secrets.generateRandomBytes(Constants.SECRET_LENGTH);
        userDTO.setRole(Role.PARTICIPANT);
        userDTO.setSecret(secret);
        userDTO.setLastLogin(LocalDateTime.now());
        UserDTO saved = userService.saveUser(userDTO);

        try {
            participantService.saveParticipant(saved.getId(), id);
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }

        String experimentUrl = ApplicationProperties.BASE_URL + ApplicationProperties.CONTEXT_PATH
                + "/users/authenticate?id=" + id + "&secret=" + secret;
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("baseUrl", ApplicationProperties.BASE_URL + ApplicationProperties.CONTEXT_PATH);
        templateModel.put("secret", experimentUrl);
        ResourceBundle userLanguage = ResourceBundle.getBundle("i18n/messages",
                getLocaleFromLanguage(userDTO.getLanguage()));

        if (!ApplicationProperties.MAIL_SERVER) {
            return "redirect:/secret" + "?user=" + saved.getId() + EXPERIMENT_PARAM + id;
        } else if (mailService.sendEmail(userDTO.getEmail(), userLanguage.getString("participant_email_subject"),
                templateModel, "participant-email")) {
            return REDIRECT_EXPERIMENT + id;
        } else {
            return Constants.ERROR;
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
    @Secured(Constants.ROLE_ADMIN)
    public String deleteParticipant(@RequestParam("participant") final String participant,
                                    @RequestParam(ID) final String id, final Model model) {
        if (participant == null || id == null || participant.trim().isBlank()
                || participant.length() > Constants.LARGE_FIELD) {
            LOGGER.error("Cannot delete participant with invalid id or input string!");
            return Constants.ERROR;
        }

        int experimentId = NumberParser.parseNumber(id);

        if (experimentId < Constants.MIN_ID) {
            LOGGER.error("Cannot delete a participant for experiment with invalid id " + id + "!");
            return Constants.ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        ExperimentDTO experimentDTO;
        UserDTO userDTO = userService.getUserByUsernameOrEmail(participant);

        try {
            experimentDTO = experimentService.getExperiment(experimentId);
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }

        if (userDTO == null) {
            model.addAttribute(ERROR, resourceBundle.getString("user_not_found"));
            addModelInfo(experimentDTO, model);
            return "experiment";
        } else if (!userDTO.getRole().equals(Role.PARTICIPANT)) {
            model.addAttribute(ERROR, resourceBundle.getString("user_not_participant"));
        } else if (!userService.existsParticipant(userDTO.getId(), experimentId)) {
            model.addAttribute(ERROR, resourceBundle.getString("no_participant_entry"));
        } else if (!experimentDTO.isActive()) {
            model.addAttribute(ERROR, resourceBundle.getString("experiment_closed"));
        }

        if (model.getAttribute(ERROR) != null) {
            addModelInfo(experimentDTO, model);
            return "experiment";
        }

        try {
            if (!participantService.simultaneousParticipation(userDTO.getId())) {
                userDTO.setSecret(null);
                userDTO.setActive(false);
                userService.updateUser(userDTO);
            }

            participantService.deleteParticipant(userDTO.getId(), experimentId);
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }

        return REDIRECT_EXPERIMENT + experimentId;
    }

    /**
     * Starts the experiment with the given id for the currently authenticated user, if they are registered as
     * participants and have not yet finished the experiment. If these requirements are not met, no corresponding
     * participant could be found, or the user is an administrator, they are redirected to the error page instead.
     *
     * @param id The experiment id.
     * @param httpServletRequest The servlet request.
     * @return Opens the scratch GUI on success, or redirects to the error page otherwise.
     */
    @GetMapping("/start")
    @Secured(Constants.ROLE_PARTICIPANT)
    public String startExperiment(@RequestParam(ID) final String id, final HttpServletRequest httpServletRequest) {
        int experimentId = NumberParser.parseId(id);

        if (experimentId < Constants.MIN_ID) {
            LOGGER.error("Cannot start experiment with invalid id " + id + "!");
            return Constants.ERROR;
        }

        if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            LOGGER.error("An administrator tried to participate in the experiment with id " + id + "!");
            return Constants.ERROR;
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getName() == null) {
                LOGGER.error("Cannot start the experiment for a user with authentication with name null!");
                return Constants.ERROR;
            }

            try {
                UserDTO userDTO = userService.getUser(authentication.getName());
                ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);
                ParticipantDTO participantDTO = participantService.getParticipant(experimentId, userDTO.getId());

                if (isInvalidStartExperiment(userDTO, experimentDTO, participantDTO)) {
                    return Constants.ERROR;
                }

                if (participantDTO.getStart() == null) {
                    participantDTO.setStart(LocalDateTime.now());

                    if (participantService.updateParticipant(participantDTO)) {
                        return "redirect:" + experimentDTO.getGuiURL() + USER_ID_PARAM + participantDTO.getUser()
                                + EXPERIMENT_ID_PARAM + participantDTO.getExperiment() + SECRET_PARAM
                                + userDTO.getSecret();
                    } else {
                        LOGGER.error("Failed to update the starting time of participant with user id "
                                + participantDTO.getUser() + " for experiment with id " + participantDTO.getExperiment()
                                + "!");
                        return Constants.ERROR;
                    }
                } else {
                    return "redirect:" + experimentDTO.getGuiURL() + USER_ID_PARAM + participantDTO.getUser()
                            + EXPERIMENT_ID_PARAM + participantDTO.getExperiment() + SECRET_PARAM + userDTO.getSecret()
                            + RESTART_PARAM;
                }
            } catch (NotFoundException e) {
                return Constants.ERROR;
            }
        }
    }

    /**
     * Stops the experiment with the given id for the user with the given id and secret, if the passed information is
     * valid. If the passed parameters are invalid or no corresponding participant could be found, the user is
     * redirected to the error page instead.
     *
     * @param experiment The experiment id.
     * @param user The user id.
     * @param secret The user's secret.
     * @param httpServletRequest The servlet request.
     * @return Redirects to the finish page on success, or to the error page otherwise.
     */
    @GetMapping("/stop")
    public String stopExperiment(@RequestParam("experiment") final String experiment,
                                 @RequestParam("user") final String user, @RequestParam("secret") final String secret,
                                 final HttpServletRequest httpServletRequest) {
        int experimentId = NumberParser.parseId(experiment);
        int userId = NumberParser.parseId(user);

        if (isInvalidPassedParams(experimentId, userId, secret, "stop", true)) {
            return Constants.ERROR;
        }

        try {
            UserDTO userDTO = userService.getUserById(userId);
            ParticipantDTO participantDTO = participantService.getParticipant(experimentId, userId);

            if (participantDTO.getStart() == null || participantDTO.getEnd() != null) {
                LOGGER.error("Cannot end experiment for participant with id " + participantDTO.getUser()
                        + " and experiment " + participantDTO.getExperiment() + " with invalid starting time "
                        + participantDTO.getStart() + " or finishing time " + participantDTO.getEnd() + "!");
                clearSecurityContext(httpServletRequest);
                return Constants.ERROR;
            }

            participantDTO.setEnd(LocalDateTime.now());

            if (participantService.simultaneousParticipation(userId)
                    && participantService.updateParticipant(participantDTO)) {
                clearSecurityContext(httpServletRequest);
                return "redirect:/finish?user=" + userId + EXPERIMENT_PARAM + experimentId + SECRET_PARAM + secret;
            } else if (participantService.updateParticipant(participantDTO)) {
                userDTO.setActive(false);
                userService.saveUser(userDTO);
                clearSecurityContext(httpServletRequest);
                return "redirect:/finish?user=" + userId + EXPERIMENT_PARAM + experimentId + SECRET_PARAM + secret;
            } else {
                LOGGER.error("Failed to update the finishing time of participant with user id "
                        + participantDTO.getUser() + " for experiment with id " + participantDTO.getExperiment() + "!");
                clearSecurityContext(httpServletRequest);
                return Constants.ERROR;
            }
        } catch (NotFoundException e) {
            clearSecurityContext(httpServletRequest);
            return Constants.ERROR;
        }
    }

    /**
     * Restarts an already finished experiment with the given id for the participant with the given id if a
     * corresponding participant relation exists. In this case, the ending time of the user is reset to null. If the
     * passed parameters are invalid, the user is an administrator, or no corresponding participant exists, the user is
     * redirected to the error page instead.
     *
     * @param experiment The id of the experiment.
     * @param user The id of the user.
     * @param secret The user's secret.
     * @return Opens the Scratch GUI on success, or redirects to the error page.
     */
    @GetMapping("/restart")
    public String restartExperiment(@RequestParam("experiment") final String experiment,
                                    @RequestParam("user") final String user,
                                    @RequestParam("secret") final String secret) {
        int experimentId = NumberParser.parseId(experiment);
        int userId = NumberParser.parseId(user);

        if (isInvalidPassedParams(experimentId, userId, secret, "restart", false)) {
            return Constants.ERROR;
        }

        try {
            UserDTO userDTO = userService.getUserById(userId);
            ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);
            ParticipantDTO participantDTO = participantService.getParticipant(experimentId, userId);

            if (participantDTO.getStart() == null || participantDTO.getEnd() == null) {
                LOGGER.error("Cannot restart experiment for user " + userId + " and experiment " + experimentId
                        + " with start or end time null!");
                return Constants.ERROR;
            }

            participantDTO.setEnd(null);

            if (!participantService.updateParticipant(participantDTO)) {
                LOGGER.error("Could not reset the ending time for user " + userId + " and experiment " + experimentId);
                return Constants.ERROR;
            } else {
                userDTO.setActive(true);
                userService.updateUser(userDTO);
                return "redirect:" + experimentDTO.getGuiURL() + USER_ID_PARAM + participantDTO.getUser()
                        + EXPERIMENT_ID_PARAM + participantDTO.getExperiment() + SECRET_PARAM + secret + RESTART_PARAM;
            }
        } catch (NotFoundException e) {
            return Constants.ERROR;
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

        int last = pageService.getLastParticipantPage(experimentDTO.getId()) + 1;

        Page<Participant> participants = pageService.getParticipantPage(experimentDTO.getId(),
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
        String usernameValidation = FieldErrorHandler.validateUsername(username, bindingResult, resourceBundle);

        if (usernameValidation == null && userService.existsUser(username)) {
            FieldErrorHandler.addFieldError(bindingResult, "userDTO", "username", "username_exists", resourceBundle);
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
        String emailValidation = FieldErrorHandler.validateEmail(email, bindingResult, resourceBundle);

        if (emailValidation == null && userService.existsEmail(email)) {
            FieldErrorHandler.addFieldError(bindingResult, "userDTO", "email", "email_exists", resourceBundle);
        }
    }

    /**
     * Returns the proper {@link Locale} based on the user's preferred language settings.
     *
     * @param language The user's preferred language.
     * @return The corresponding locale, or English as a default value.
     */
    private Locale getLocaleFromLanguage(final Language language) {
        if (language == Language.GERMAN) {
            return Locale.GERMAN;
        }
        return Locale.ENGLISH;
    }

    /**
     * Checks, if the passed user, experiment and participant fulfill the requirements to start the experiment. If the
     * experiment or the user are deactivated, the user secret is null or the participant has already finished the
     * experiment, the user is not allowed to start the experiment.
     *
     * @param userDTO The {@link UserDTO} containing the user information to check.
     * @param experimentDTO The {@link ExperimentDTO} containing the experiment information to check.
     * @param participantDTO The {@link ParticipantDTO} containing the participant information to check.
     * @return {@code true} if the user is not allowed to start the experiment or {@code false} otherwise.
     */
    private boolean isInvalidStartExperiment(final UserDTO userDTO, final ExperimentDTO experimentDTO,
                                             final ParticipantDTO participantDTO) {
        if (!experimentDTO.isActive()) {
            LOGGER.error("Cannot start experiment with id " + experimentDTO.getId() + " for user with id "
                    + userDTO.getId() + " since the experiment is closed!");
            return true;
        } else if (!userDTO.isActive() || userDTO.getSecret() == null) {
            LOGGER.error("Cannot start experiment for user with id " + userDTO.getId() + " since their account "
                    + "is inactive or their secret null!");
            return true;
        } else if (participantDTO.getEnd() != null) {
            LOGGER.error("The user with id " + userDTO.getId() + " tried to start the experiment with id "
                    + experimentDTO.getId() + " even though they have already finished it!");
            return true;
        }

        return false;
    }

    /**
     * Checks if the passed ids and secret are valid parameters and match a participant and user in the database.
     *
     * @param experimentId The id of the experiment.
     * @param userId The id of the user.
     * @param secret The user's secret.
     * @param method The type of method from which this method is called.
     * @param userActive Boolean indicating whether the user account should be active.
     * @return {@code true} if the passed parameters are invalid or {@code false} otherwise.
     */
    private boolean isInvalidPassedParams(final int experimentId, final int userId, final String secret,
                                          final String method, final boolean userActive) {
        if (experimentId < Constants.MIN_ID || userId < Constants.MIN_ID) {
            LOGGER.error("Cannot " + method + " experiment with invalid experiment id " + experimentId
                    + " or invalid user id " + userId + "!");
            return true;
        } else if (secret == null || secret.isBlank()) {
            LOGGER.error("Cannot " + method + " experiment with secret null or blank!");
            return true;
        } else {
            return participantService.isInvalidParticipant(userId, experimentId, secret, userActive);
        }
    }

}
