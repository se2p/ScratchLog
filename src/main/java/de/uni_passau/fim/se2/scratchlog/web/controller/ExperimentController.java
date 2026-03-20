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

package de.uni_passau.fim.se2.scratchlog.web.controller;

import com.opencsv.CSVWriter;
import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.service.CourseService;
import de.uni_passau.fim.se2.scratchlog.application.service.ExperimentDataService;
import de.uni_passau.fim.se2.scratchlog.application.service.ExperimentService;
import de.uni_passau.fim.se2.scratchlog.application.service.MailService;
import de.uni_passau.fim.se2.scratchlog.application.service.PageService;
import de.uni_passau.fim.se2.scratchlog.application.service.ParticipantService;
import de.uni_passau.fim.se2.scratchlog.application.service.UserService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Participant;
import de.uni_passau.fim.se2.scratchlog.util.ApplicationProperties;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.util.FieldErrorHandler;
import de.uni_passau.fim.se2.scratchlog.util.MarkdownHandler;
import de.uni_passau.fim.se2.scratchlog.util.Secrets;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import de.uni_passau.fim.se2.scratchlog.util.validation.FiletypeValidator;
import de.uni_passau.fim.se2.scratchlog.util.validation.StringValidator;
import de.uni_passau.fim.se2.scratchlog.web.dto.ExperimentDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.ParticipantDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.PasswordDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * The controller for managing experiments.
 */
@Controller
@RequestMapping(value = "/experiment")
public class ExperimentController {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger log = LoggerFactory.getLogger(ExperimentController.class);

    /**
     * The global application config.
     */
    private final ApplicationProperties applicationProperties;

    /**
     * The user service to use for user management.
     */
    private final UserService userService;

    /**
     * The experiment service to use for experiment management.
     */
    private final ExperimentService experimentService;

    /**
     * The course service to use for course management.
     */
    private final CourseService courseService;

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
    private final Optional<MailService> mailService;

    /**
     * The experiment data service to use for retrieving experiment data.
     */
    private final ExperimentDataService experimentDataService;

    /**
     * String corresponding to the experiment page.
     */
    private static final String EXPERIMENT = "experiment";

    /**
     * String corresponding to redirecting to the experiment page.
     */
    private static final String REDIRECT_EXPERIMENT = "redirect:/experiment?id=";

    /**
     * String corresponding to redirecting to the secret page.
     */
    private static final String REDIRECT_SECRET_LIST = "redirect:/secret/list?experiment=";

    /**
     * String corresponding to redirecting to the secret page.
     */
    private static final String REDIRECT_SECRET = "redirect:/secret?user=";

    /**
     * String corresponding to the experiment id parameter.
     */
    private static final String EXPERIMENT_PARAM = "&experiment=";

    /**
     * String corresponding to the experiment edit page.
     */
    private static final String EXPERIMENT_EDIT = "experiment-edit";

    /**
     * String corresponding to the id request parameter.
     */
    private static final String ID = "id";

    /**
     * String corresponding to the page request parameter or model attribute.
     */
    private static final String PAGE = "page";

    /**
     * String corresponding to the error model attribute.
     */
    private static final String ERROR = "error";

    /**
     * Constructs a new experiment controller with the given dependencies.
     *
     * @param applicationProperties The {@link ApplicationProperties} to use.
     * @param experimentService The {@link ExperimentService} to use.
     * @param userService The {@link UserService} to use.
     * @param courseService The {@link CourseService} to use.
     * @param participantService The {@link ParticipantService} to use.
     * @param pageService The {@link PageService} to use.
     * @param mailService The {@link MailService} to use.
     * @param experimentDataService The {@link ExperimentDataService} to use.
     */
    @Autowired
    public ExperimentController(final ApplicationProperties applicationProperties,
                                final ExperimentService experimentService, final UserService userService,
                                final CourseService courseService, final ParticipantService participantService,
                                final PageService pageService, final Optional<MailService> mailService,
                                final ExperimentDataService experimentDataService) {
        this.applicationProperties = applicationProperties;
        this.experimentService = experimentService;
        this.userService = userService;
        this.courseService = courseService;
        this.participantService = participantService;
        this.pageService = pageService;
        this.mailService = mailService;
        this.experimentDataService = experimentDataService;
    }

    /**
     * Returns the experiment page displaying the information available for the experiment with the given id. If the
     * request parameter passed is invalid, no entry can be found in the database, the user profile is inactive, or no
     * participant entry could be found for a participant, the user is redirected to the error page instead.
     *
     * @param experimentId The id of the experiment.
     * @param model The model to hold the information.
     * @param httpServletRequest The servlet request.
     * @return The experiment page on success, or the error page otherwise.
     */
    @GetMapping
    @Secured(Constants.ROLE_PARTICIPANT)
    public String getExperiment(@RequestParam(ID) final int experimentId, final Model model,
                                final HttpServletRequest httpServletRequest) {
        try {
            ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);

            if (!httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserDTO userDTO = userService.getUser(authentication.getName());

                if (!userDTO.isActive()) {
                    log.debug(
                        "Cannot display experiment page for user with id {} since their account is inactive!",
                        userDTO.getId()
                    );
                    return Constants.ERROR;
                } else if (userDTO.getSecret() == null) {
                    model.addAttribute("secret", false);
                }

                ParticipantDTO participantDTO = participantService.getParticipant(experimentId, userDTO.getId());
                model.addAttribute("participant", participantDTO);
                addParticipantModelInfo(experimentDTO, model);
            } else {
                addModelInfo(0, experimentDTO, model);
            }

            return EXPERIMENT;
        } catch (NotFoundException e) {
            log.error("Could not retrieve experiment page!", e);
            return Constants.ERROR;
        }
    }

    /**
     * Returns the form used to create or edit an experiment.
     *
     * @param courseId The ID of the course to which the new experiment should be added, if applicable.
     * @param model The {@link Model} used to store the information.
     * @return A new empty form.
     */
    @GetMapping("/create")
    @Secured(Constants.ROLE_ADMIN)
    public String getExperimentForm(@RequestParam(required = false, name = "course") final Integer courseId,
                                    final Model model) {
        ExperimentDTO experimentDTO = new ExperimentDTO();

        if (courseId != null) {
            experimentDTO.setCourse(courseId);
            experimentDTO.setCourseExperiment(true);
        }

        model.addAttribute("experimentDTO", experimentDTO);
        return EXPERIMENT_EDIT;
    }

    /**
     * Returns the experiment edit page for the experiment with the given id. If no entry can be found in the database,
     * the user is redirected to the error page instead.
     *
     * @param experimentId The id to search for.
     * @param model The model to hold the information.
     * @return The experiment edit page on success, or the error page otherwise.
     */
    @GetMapping("/edit")
    @Secured(Constants.ROLE_ADMIN)
    public String getEditExperimentForm(@RequestParam(ID) final int experimentId, final Model model) {
        try {
            ExperimentDTO findExperiment = experimentService.getExperiment(experimentId);
            model.addAttribute("experimentDTO", findExperiment);
            return EXPERIMENT_EDIT;
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Creates a new experiment or updates an existing one with the information given in the {@link ExperimentDTO}
     * and redirects to corresponding experiment page on success. If the input form data is invalid, the current page is
     * returned instead to display the error messages.
     *
     * @param experimentDTO The experiment dto containing the input data.
     * @param bindingResult The binding result for returning information on invalid user input.
     * @return The experiment edit page, if the input is invalid, or experiment page on success.
     */
    @PostMapping("/update")
    @Secured(Constants.ROLE_ADMIN)
    public String editExperiment(@ModelAttribute("experimentDTO") final ExperimentDTO experimentDTO,
                                 final BindingResult bindingResult) {
        if (experimentDTO.getCourse() != null && !courseService.existsActiveCourse(experimentDTO.getCourse())) {
            return Constants.ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        FieldErrorHandler.validateExperimentInput(experimentDTO.getTitle(), experimentDTO.getDescription(),
                experimentDTO.getInfo(), bindingResult, resourceBundle);
        checkFieldErrors(experimentDTO, bindingResult, resourceBundle);

        if (bindingResult.hasErrors()) {
            return EXPERIMENT_EDIT;
        }

        ExperimentDTO saved;

        if (experimentDTO.getCourse() != null) {
            experimentDTO.setActive(true);
            saved = experimentService.updateExperiment(experimentDTO);

            if (isErrorSavingCourseExperiment(experimentDTO.getCourse(), saved.getId())) {
                return Constants.ERROR;
            }
        } else {
            saved = experimentService.updateExperiment(experimentDTO);
        }

        return REDIRECT_EXPERIMENT + saved.getId();
    }

    /**
     * Deletes the experiment with the given id from the database and redirects to the index page on success.
     *
     * @param passwordDTO The {@link PasswordDTO} containing the input password.
     * @param experimentId The id of the experiment.
     * @return The index page.
     */
    @PostMapping("/delete")
    @Secured(Constants.ROLE_ADMIN)
    public String deleteExperiment(@ModelAttribute("passwordDTO") final PasswordDTO passwordDTO,
                                   @RequestParam(ID) final int experimentId) {
        if (passwordDTO.getPassword() == null) {
            log.error("Cannot delete experiment with password null!");
            return Constants.ERROR;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getName() == null) {
            log.error("An unauthenticated user tried to delete experiment with id {}!", experimentId);
            return Constants.ERROR;
        }

        try {
            UserDTO currentUser = userService.getUser(authentication.getName());

            if ((passwordDTO.getPassword().length() > Constants.SMALL_FIELD)
                    || (!userService.matchesPassword(passwordDTO.getPassword(), currentUser.getPassword()))) {
                return "redirect:/experiment?invalid=true&id=" + experimentId;
            }

            experimentService.deleteExperiment(experimentId);
            return "redirect:/?success=true";
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Changes the experiment status to the given request parameter value. If the experiment is being reopened, new
     * invitation mails are send out to all participants who haven't yet started the experiment and who are not
     * currently participating in a different one. If the passed id or status values are invalid, or no corresponding
     * experiment exists in the database, the user is redirected to the error page instead.
     *
     * @param experimentId The id of the experiment.
     * @param status The new status of the experiment.
     * @param model The model to hold the information.
     * @return The experiment page.
     */
    @GetMapping("/status")
    @Secured(Constants.ROLE_ADMIN)
    public String changeExperimentStatus(@RequestParam("stat") final String status,
                                         @RequestParam(ID) final int experimentId,
                                         final Model model) {
        if (status == null) {
            log.error("Cannot change the status of the experiment with invalid status parameters!");
            return Constants.ERROR;
        }

        try {
            ExperimentDTO experimentDTO;

            if (status.equals("open")) {
                experimentDTO = experimentService.getExperiment(experimentId);

                if (experimentDTO.isCourseExperiment() && !courseService.isActiveCourse(experimentId)) {
                    ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                            LocaleContextHolder.getLocale());
                    model.addAttribute(ERROR, resourceBundle.getString("course_inactive"));
                    addModelInfo(0, experimentDTO, model);
                    return EXPERIMENT;
                }

                experimentDTO = experimentService.changeExperimentStatus(true, experimentId);
                List<UserDTO> userDTOS = userService.reactivateUserAccounts(experimentId);

                if (!applicationProperties.useMail()) {
                    return REDIRECT_SECRET_LIST + experimentId;
                } else {
                    userDTOS.forEach(userDTO -> sendEmail(userDTO, experimentId));
                }
            } else if (status.equals("close")) {
                experimentDTO = experimentService.changeExperimentStatus(false, experimentId);
                participantService.deactivateParticipantAccounts(experimentId);
            } else {
                log.debug("Cannot return the corresponding experiment page for requested status change {}!", status);
                return Constants.ERROR;
            }

            addModelInfo(0, experimentDTO, model);
            return EXPERIMENT;
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Searches for users whose name or email address match the given search string. If all user could be found, they
     * are added as participants to the given experiment. If no experiment with the corresponding id could be found or
     * the id is invalid, the user is redirected to the error page instead.
     *
     * @param experimentId The id of the experiment.
     * @param participants The usernames or email addresses to search for and add to the experiment.
     * @param model The model used to store the error messages.
     * @return The experiment page on success, or the error page otherwise.
     */
    @PostMapping("/add")
    @Secured(Constants.ROLE_ADMIN)
    public String addParticipants(final @RequestParam List<String> participants,
                                  final @RequestParam(ID) int experimentId,
                                  final Model model) {
        if (participants.isEmpty()) {
            return Constants.ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
            LocaleContextHolder.getLocale());
        ExperimentDTO experimentDTO;

        try {
            experimentDTO = experimentService.getExperiment(experimentId);
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }

        // Validate that all participants are valid, and if not, redirect to the experiment page.
        for (String participant : participants) {
            if (!isValidSearch(participant, experimentDTO, resourceBundle, model)) {
                return EXPERIMENT;
            }

            UserDTO userDTO = userService.getUserByUsernameOrEmail(participant);
            validateUser(userDTO, experimentDTO, resourceBundle, model);
            if (model.getAttribute(ERROR) != null) {
                addModelInfo(0, experimentDTO, model);
                return EXPERIMENT;
            }
        }

        List<Integer> userIds = new ArrayList<>();
        for (String participant : participants) {

            UserDTO userDTO = userService.getUserByUsernameOrEmail(participant);
            try {
                String secret = userDTO.getSecret() == null ? Secrets.generateRandomBytes(Constants.SECRET_LENGTH)
                    : userDTO.getSecret();
                userDTO.setSecret(secret);
                UserDTO saved = userService.updateUser(userDTO);
                userIds.add(saved.getId());
            } catch (NotFoundException e) {
                return Constants.ERROR;
            }
        }

        try {
            participantService.addParticipants(userIds, experimentId);
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }

        // Send participation emails to the added participants, if mailing is configured.
        if (applicationProperties.useMail()) {
            for (String participant : participants) {
                UserDTO userDTO = userService.getUserByUsernameOrEmail(participant);

                // Send the email and show the error page if something went wrong.
                if (!sendEmail(userDTO, experimentId)) {
                    return Constants.ERROR;
                }
            }
        } else if (userIds.size() == 1) {
            return REDIRECT_SECRET + userIds.get(0) + EXPERIMENT_PARAM + experimentId;
        }

        return REDIRECT_EXPERIMENT + experimentId;
    }

    /**
     * Loads the participant page with the given page number for the experiment with the given id, if the provided
     * numbers are valid.
     *
     * @param experimentId The experiment id.
     * @param page The number of the page to be retrieved.
     * @param model The {@link Model} used to store the information.
     * @return The experiment page on success, or the error page otherwise.
     */
    @GetMapping("/page")
    @Secured(Constants.ROLE_ADMIN)
    public String getPage(@RequestParam(ID) final int experimentId, @RequestParam(PAGE) final int page,
                          final Model model) {
        try {
            ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);
            addModelInfo(page, experimentDTO, model);
            return EXPERIMENT;
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Retrieves all block, click and resource event data for the given experiment and saves the information in a CSV
     * file.
     *
     * @param experimentId The experiment id to search for.
     * @param httpServletResponse The servlet response returning the file.
     * @throws RuntimeException if an {@link IOException} occurs.
     */
    @GetMapping("/csv")
    @Secured(Constants.ROLE_ADMIN)
    public void downloadCSVFile(
        @RequestParam(ID) final int experimentId, final HttpServletResponse httpServletResponse
    ) throws IOException {
        httpServletResponse.setContentType("text/csv");
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename=experiment_" + experimentId
                + ".csv");
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);

        try (PrintWriter pw = httpServletResponse.getWriter()) {
            experimentDataService.getEventDataCsv(experimentId, pw);
        }
    }

    /**
     * Adds participants to the given experiment provided as usernames in a CSV file. If the passed file experiment id
     * are invalid, the user is redirected to the error page instead. If the file is not a CSV file, the provided
     * usernames are invalid or the file could not be parsed correctly, the user returns to the experiment page where a
     * corresponding error message is displayed.
     *
     * @param file The file containing the user information.
     * @param experimentId The id of the experiment to which the users should be added.
     * @param model The model used to return error messages.
     * @return The experiment page on success or if an error message should be displayed, or the error page otherwise.
     */
    @PostMapping("/csv")
    @Secured(Constants.ROLE_ADMIN)
    public String addParticipantsFromCSV(@RequestParam("file") final MultipartFile file,
                                         @RequestParam(ID) final int experimentId, final Model model) {
        if (file == null) {
            log.error("Cannot add participants from CSV for experiment with file null!");
            return Constants.ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
            LocaleContextHolder.getLocale());
        ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);

        try {
            List<UserDTO> users = userService.parseUserListCsv(file);
            List<String> invalidUsernames = userService.getInvalidParticipantUsernames(users);
            if (invalidUsernames.isEmpty()) {
                if (experimentDTO.isCourseExperiment()) {
                    courseService.saveCourseParticipants(
                        courseService.getCourseIdForExperiment(experimentId), users, false);
                }
                participantService.saveParticipantsFromCSV(experimentId, users);
            } else {
                model.addAttribute(ERROR, resourceBundle.getString("invalid_usernames") + " " + invalidUsernames);
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute(ERROR, resourceBundle.getString(e.getMessage()));
        } catch (IOException e) {
            log.error("Error parsing CSV file!", e);
            model.addAttribute(ERROR, resourceBundle.getString("csv_error"));
        }

        addModelInfo(0, experimentDTO, model);
        return EXPERIMENT;
    }

    /**
     * Analysis the stored code data for all users in the experiment with the given id using LitterBox and saves the
     * information in a CSV file.
     *
     * @param experimentId The id of the experiment.
     * @param httpServletResponse The servlet response returning the file.
     * @throws RuntimeException if an {@link IOException} occurs.
     */
    @GetMapping("/analysis")
    @Secured(Constants.ROLE_ADMIN)
    public void downloadLitterBoxAnalysis(@RequestParam(ID) final int experimentId,
                                          final HttpServletResponse httpServletResponse) {
        try {
            httpServletResponse.setContentType("text/csv");
            httpServletResponse.setHeader("Content-Disposition", "attachment;filename=experiment_litterbox_"
                    + experimentId + ".csv");
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            CSVWriter csvWriter = new CSVWriter(httpServletResponse.getWriter());
            List<String[]> results = experimentDataService.getLitterBoxAnalysisResults(experimentId);
            csvWriter.writeAll(results);
            csvWriter.flush();
        } catch (IOException e) {
            log.error("Could not download LitterBox analysis results due to IOException!", e);
            throw new RuntimeException("Could not download LitterBox analysis results due to IOException!");
        }
    }

    /**
     * Saves the content of the given sb3 file to the database for the experiment with the given id. If the file does
     * not meet the requirements, the user returns to the experiment page where an error message is displayed. If the
     * parameters are invalid, no corresponding experiment could be found, or an {@link IOException} occurred, the user
     * is redirected to the error page instead.
     *
     * @param experimentId The experiment id to search for.
     * @param file The sb3 file to be uploaded.
     * @param model The model used to return error messages.
     * @return The experiment page on success, or if the file was invalid, or the error page otherwise.
     */
    @PostMapping("/upload")
    @Secured(Constants.ROLE_ADMIN)
    public String uploadProjectFile(@RequestParam("file") final MultipartFile file,
                                    @RequestParam(ID) final int experimentId, final Model model) {
        if (file == null) {
            log.error("Cannot upload file for experiment with file null!");
            return Constants.ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        String fileValidation = FiletypeValidator.validate(file, "application/octet-stream", Constants.SB3);

        if (fileValidation != null) {
            log.error("Could not upload sb3 file due to invalid filetype or empty file!");
            model.addAttribute(ERROR, resourceBundle.getString(fileValidation));
        }

        if (model.getAttribute(ERROR) != null) {
            ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);
            addModelInfo(0, experimentDTO, model);
            return EXPERIMENT;
        }

        try {
            experimentService.uploadSb3Project(experimentId, file.getBytes());
            return REDIRECT_EXPERIMENT + experimentId;
        } catch (NotFoundException e) {
            return Constants.ERROR;
        } catch (IOException e) {
            log.error("Could not upload file due to IOException", e);
            return Constants.ERROR;
        }
    }

    /**
     * Deletes the sb3 file currently saved for the experiment with the given id. If the id is invalid, or no
     * corresponding experiment could be found, the user is redirected to the error page instead.
     *
     * @param experimentId The experiment id to search for.
     * @return The experiment page on success, or the error page otherwise.
     */
    @GetMapping("/sb3")
    @Secured(Constants.ROLE_ADMIN)
    public String deleteProjectFile(@RequestParam(ID) final int experimentId) {
        try {
            experimentService.deleteSb3Project(experimentId);
            return REDIRECT_EXPERIMENT + experimentId;
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Sends an email with a participation link for the experiment with the given id to the email address of the given
     * {@link UserDTO}.
     *
     * @param userDTO The user to whom the email should be sent.
     * @param experimentId The id of the experiment in which the user is participating.
     * @return {@code true} if the message has been sent successfully or {@code false} otherwise.
     */
    private boolean sendEmail(final UserDTO userDTO, final int experimentId) {
        if (mailService.isEmpty()) {
            log.debug("Cannot send emails when mailing is disabled!");
            return false;
        }
        if (userDTO.getEmail() == null) {
            log.error("Cannot send invitation mail to user with email null!");
            return false;
        }

        Map<String, Object> templateModel = getTemplateModel(experimentId, userDTO.getSecret());
        ResourceBundle userLanguage = ResourceBundle.getBundle("i18n/messages",
            (userDTO.getLanguage() != null ? userDTO.getLanguage().toLocale() : Constants.DEFAULT_LANGUAGE.toLocale()));

        if (!mailService.get().sendEmail(userDTO.getEmail(), userLanguage.getString("participant_email_subject"),
                templateModel, "participant-email")) {
            log.error("Could not send invitation mail to user with email {}.", userDTO.getEmail());
            return false;
        }

        return true;
    }

    /**
     * Creates a {@link Map} containing the base URL of the application and the link to the experiment with the given id
     * and the generated secret for the user that are going to be used in the experiment invitation email template.
     *
     * @param id The id of the experiment.
     * @param secret The user's secret.
     * @return The map containing the base URL and the experiment URL.
     */
    private Map<String, Object> getTemplateModel(final int id, final String secret) {
        String experimentUrl = applicationProperties.getApplicationUrl()
                + "/users/authenticate?id=" + id + "&secret=" + secret;
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("applicationName", applicationProperties.getApplicationName());
        templateModel.put("baseUrl", applicationProperties.getApplicationUrl());
        templateModel.put("secret", experimentUrl);
        return templateModel;
    }

    /**
     * Retrieves the current participant page information from the database and adds the page to the {@link Model} along
     * with the {@link ExperimentDTO} and the last page.
     *
     * @param page The number of the current participant page to be retrieved.
     * @param experimentDTO The current experiment dto.
     * @param model The {@link Model} used to save the information.
     */
    private void addModelInfo(final int page, final ExperimentDTO experimentDTO, final Model model) {
        int last = pageService.getLastParticipantPage(experimentDTO.getId());
        Page<Participant> participants = pageService.getParticipantPage(experimentDTO.getId(), page);

        model.addAttribute(PAGE, page);
        model.addAttribute("lastPage", last);
        model.addAttribute("participants", participants);
        addExperimentInfo(experimentDTO, model);
    }

    /**
     * Adds the required information for the participant view of the experiment page to the given model.
     *
     * @param experimentDTO The {@link ExperimentDTO} containing information on the experiment.
     * @param model The {@link Model} used to store the information.
     */
    private void addParticipantModelInfo(final ExperimentDTO experimentDTO, final Model model) {
        addExperimentInfo(experimentDTO, model);
        model.addAttribute(PAGE, 0);
        model.addAttribute("lastPage", 0);
        model.addAttribute("participants", new ArrayList<>());
    }

    /**
     * Adds the required information about the experiment to the model.
     *
     * @param experimentDTO The {@link ExperimentDTO} containing information on the experiment.
     * @param model The {@link Model} used to store the information.
     */
    private void addExperimentInfo(final ExperimentDTO experimentDTO, final Model model) {
        if (experimentDTO.getInfo() != null) {
            experimentDTO.setInfo(MarkdownHandler.toHtml(experimentDTO.getInfo()));
        }
        if (experimentService.hasProjectFile(experimentDTO.getId())) {
            model.addAttribute("project", true);
        }

        model.addAttribute("experimentDTO", experimentDTO);
        model.addAttribute("passwordDTO", new PasswordDTO());
    }

    /**
     * Checks, if the input contained in the given experiment dto is valid. If not, a corresponding field error is added
     * to the given binding result to be displayed on the experiment edit page.
     *
     * @param experimentDTO The {@link ExperimentDTO} to check.
     * @param bindingResult The {@link BindingResult} for returning information on invalid user input.
     * @param resourceBundle The {@link ResourceBundle} for fetching the error message in the desired language.
     */
    private void checkFieldErrors(final ExperimentDTO experimentDTO, final BindingResult bindingResult,
                                  final ResourceBundle resourceBundle) {
        if (experimentDTO.getPostscript() != null && !experimentDTO.getPostscript().trim().isBlank()) {
            if (experimentDTO.getPostscript().length() > Constants.SMALL_AREA) {
                FieldErrorHandler.addFieldError(bindingResult, "experimentDTO", "postscript", "long_string",
                        resourceBundle);
            }
        }

        if (experimentDTO.getId() == null) {
            if (experimentService.existsExperiment(experimentDTO.getTitle())) {
                log.error("Experiment with same title exists!");
                FieldErrorHandler.addTitleExistsError(bindingResult, "experimentDTO", resourceBundle);
            }
        } else {
            if (experimentService.existsExperiment(experimentDTO.getTitle(), experimentDTO.getId())) {
                log.error("Experiment with same name but different id exists!");
                FieldErrorHandler.addTitleExistsError(bindingResult, "experimentDTO", resourceBundle);
            }
        }
    }

    /**
     * Tries to add the experiment with the given id to the course with the given id and adds all users as experiment
     * participants who are part of that course.
     *
     * @param courseId The id of the course.
     * @param experimentId The id of the experiment.
     * @return {@code false} if the operation was successful, or {@code true} if an error occurred.
     */
    private boolean isErrorSavingCourseExperiment(final int courseId, final int experimentId) {
        try {
            courseService.saveCourseExperiment(courseId, experimentId);
            participantService.addAllCourseParticipantsToExperiment(experimentId, courseId);
            return false;
        } catch (Exception e) {
            log.error("Could not save course experiment!", e);
            experimentService.deleteExperiment(experimentId);
            return true;
        }
    }

    /**
     * Checks whether the given search string respects the chosen input restrictions. If not, a corresponding error
     * message is added to the given model.
     *
     * @param search The search string.
     * @param experimentDTO The {@link ExperimentDTO} to be added to the model.
     * @param resourceBundle The {@link ResourceBundle} from which the error message is taken.
     * @param model The {@link Model} to store the error messages.
     * @return {@code true} if the search input is valid or {@code false} otherwise.
     */
    private boolean isValidSearch(final String search, final ExperimentDTO experimentDTO,
                                  final ResourceBundle resourceBundle, final Model model) {
        String validateSearch = StringValidator.validate(search, Constants.LARGE_FIELD);

        if (validateSearch != null) {
            model.addAttribute(ERROR, resourceBundle.getString(validateSearch));
            addModelInfo(0, experimentDTO, model);
            return false;
        }

        return true;
    }

    /**
     * Verifies that the given {@link UserDTO} can be added as a participant to the given experiment. If the user does
     * not satisfy the conditions, a corresponding error message is added to the given model instead.
     *
     * @param userDTO The user to be added to the experiment.
     * @param experimentDTO The experiment to which the user is to be added as a participant.
     * @param resourceBundle The {@link ResourceBundle} from which the error messages are taken.
     * @param model The {@link Model} to store the error messages.
     */
    private void validateUser(final UserDTO userDTO, final ExperimentDTO experimentDTO,
                              final ResourceBundle resourceBundle, final Model model) {
        if (userDTO == null) {
            model.addAttribute(ERROR, resourceBundle.getString("user_not_found"));
        } else if (!userDTO.getRole().equals(Role.PARTICIPANT)) {
            model.addAttribute(ERROR, resourceBundle.getString("user_not_participant"));
        } else if (userService.existsParticipant(userDTO.getId(), experimentDTO.getId())) {
            model.addAttribute(ERROR, resourceBundle.getString("participant_entry"));
        } else if (!experimentDTO.isActive()) {
            model.addAttribute(ERROR, resourceBundle.getString("experiment_closed"));
        } else if (experimentDTO.isCourseExperiment() && !courseService.existsCourseParticipant(experimentDTO.getId(),
                userDTO.getId())) {
            model.addAttribute(ERROR, resourceBundle.getString("course_participant_not_found"));
        }
    }

}
