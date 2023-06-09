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

import com.opencsv.CSVWriter;
import fim.unipassau.de.scratchLog.application.exception.IncompleteDataException;
import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.application.service.CourseService;
import fim.unipassau.de.scratchLog.application.service.EventService;
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
import fim.unipassau.de.scratchLog.util.PageUtils;
import fim.unipassau.de.scratchLog.util.Secrets;
import fim.unipassau.de.scratchLog.util.enums.Language;
import fim.unipassau.de.scratchLog.util.enums.Role;
import fim.unipassau.de.scratchLog.util.validation.StringValidator;
import fim.unipassau.de.scratchLog.web.dto.ExperimentDTO;
import fim.unipassau.de.scratchLog.web.dto.ParticipantDTO;
import fim.unipassau.de.scratchLog.web.dto.PasswordDTO;
import fim.unipassau.de.scratchLog.web.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentController.class);

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
    private final MailService mailService;

    /**
     * The event service to use for event management.
     */
    private final EventService eventService;

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
     * @param experimentService The {@link ExperimentService} to use.
     * @param userService The {@link UserService} to use.
     * @param courseService The {@link CourseService} to use.
     * @param participantService The {@link ParticipantService} to use.
     * @param pageService The {@link PageService} to use.
     * @param mailService The {@link MailService} to use.
     * @param eventService The {@link EventService} to use.
     */
    @Autowired
    public ExperimentController(final ExperimentService experimentService, final UserService userService,
                                final CourseService courseService, final ParticipantService participantService,
                                final PageService pageService, final MailService mailService,
                                final EventService eventService) {
        this.experimentService = experimentService;
        this.userService = userService;
        this.courseService = courseService;
        this.participantService = participantService;
        this.pageService = pageService;
        this.mailService = mailService;
        this.eventService = eventService;
    }

    /**
     * Returns the experiment page displaying the information available for the experiment with the given id. If the
     * request parameter passed is invalid, no entry can be found in the database, the user profile is inactive, or no
     * participant entry could be found for a participant, the user is redirected to the error page instead.
     *
     * @param id The id of the experiment.
     * @param model The model to hold the information.
     * @param httpServletRequest The servlet request.
     * @return The experiment page on success, or the error page otherwise.
     */
    @GetMapping
    @Secured(Constants.ROLE_PARTICIPANT)
    public String getExperiment(@RequestParam(ID) final String id, final Model model,
                                final HttpServletRequest httpServletRequest) {
        int experimentId = NumberParser.parseId(id);

        if (experimentId < Constants.MIN_ID) {
            LOGGER.error("Cannot return the experiment page with an invalid id parameter!");
            return Constants.ERROR;
        }

        try {
            ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);

            if (!httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserDTO userDTO = userService.getUser(authentication.getName());

                if (!userDTO.isActive()) {
                    LOGGER.debug("Cannot display experiment page for user with id " + userDTO.getId() + " since their "
                            + "account is inactive!");
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
            LOGGER.error("Could not retrieve experiment page!", e);
            return Constants.ERROR;
        }
    }

    /**
     * Returns the form used to create or edit an experiment.
     *
     * @param course The ID of the course to which the new experiment should be added, if applicable.
     * @param model The {@link Model} used to store the information.
     * @return A new empty form.
     */
    @GetMapping("/create")
    @Secured(Constants.ROLE_ADMIN)
    public String getExperimentForm(@RequestParam(required = false, name = "course") final String course,
                                    final Model model) {
        ExperimentDTO experimentDTO = new ExperimentDTO();

        if (course != null) {
            int courseId = NumberParser.parseId(course);

            if (courseId < Constants.MIN_ID) {
                return Constants.ERROR;
            } else {
                experimentDTO.setCourse(courseId);
                experimentDTO.setCourseExperiment(true);
            }
        }

        model.addAttribute("experimentDTO", experimentDTO);
        return EXPERIMENT_EDIT;
    }

    /**
     * Returns the experiment edit page for the experiment with the given id. If no entry can be found in the database,
     * the user is redirected to the error page instead.
     *
     * @param id The id to search for.
     * @param model The model to hold the information.
     * @return The experiment edit page on success, or the error page otherwise.
     */
    @GetMapping("/edit")
    @Secured(Constants.ROLE_ADMIN)
    public String getEditExperimentForm(@RequestParam(ID) final String id, final Model model) {
        int experimentId = NumberParser.parseId(id);

        if (experimentId < Constants.MIN_ID) {
            LOGGER.error("Cannot return the experiment edit page with an invalid id parameter!");
            return Constants.ERROR;
        }

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
            saved = experimentService.saveExperiment(experimentDTO);

            if (isErrorSavingCourseExperiment(experimentDTO.getCourse(), saved.getId())) {
                return Constants.ERROR;
            }
        } else {
            saved = experimentService.saveExperiment(experimentDTO);
        }

        return REDIRECT_EXPERIMENT + saved.getId();
    }

    /**
     * Deletes the experiment with the given id from the database and redirects to the index page on success.
     *
     * @param passwordDTO The {@link PasswordDTO} containing the input password.
     * @param id The id of the experiment.
     * @return The index page.
     */
    @PostMapping("/delete")
    @Secured(Constants.ROLE_ADMIN)
    public String deleteExperiment(@ModelAttribute("passwordDTO") final PasswordDTO passwordDTO,
                                   @RequestParam(ID) final String id) {
        if (id == null || passwordDTO.getPassword() == null) {
            LOGGER.error("Cannot delete experiment with id null or input password null!");
            return Constants.ERROR;
        }

        int experimentId = NumberParser.parseId(id);

        if (experimentId < Constants.MIN_ID) {
            LOGGER.error("Cannot delete the experiment with invalid id " + id + "!");
            return Constants.ERROR;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getName() == null) {
            LOGGER.error("An unauthenticated user tried to delete experiment with id " + experimentId + "!");
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
     * @param id The id of the experiment.
     * @param status The new status of the experiment.
     * @param model The model to hold the information.
     * @return The experiment page.
     */
    @GetMapping("/status")
    @Secured(Constants.ROLE_ADMIN)
    public String changeExperimentStatus(@RequestParam("stat") final String status, @RequestParam(ID) final String id,
                                         final Model model) {
        int experimentId = NumberParser.parseId(id);

        if (experimentId < Constants.MIN_ID || status == null) {
            LOGGER.error("Cannot change the status of the experiment with invalid id or status parameters!");
            return Constants.ERROR;
        }

        try {
            ExperimentDTO experimentDTO;

            if (status.equals("open")) {
                experimentDTO = experimentService.changeExperimentStatus(true, experimentId);
                List<UserDTO> userDTOS = userService.reactivateUserAccounts(experimentId);

                if (!ApplicationProperties.MAIL_SERVER) {
                    return REDIRECT_SECRET_LIST + experimentId;
                } else {
                    userDTOS.forEach(userDTO -> sendEmail(userDTO, id));
                }
            } else if (status.equals("close")) {
                experimentDTO = experimentService.changeExperimentStatus(false, experimentId);
                participantService.deactivateParticipantAccounts(experimentId);
            } else {
                LOGGER.debug("Cannot return the corresponding experiment page for requested status change " + status
                        + "!");
                return Constants.ERROR;
            }

            addModelInfo(0, experimentDTO, model);
            return EXPERIMENT;
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Searches for a user whose name or email address match the given search string. If a user could be found, they are
     * added as a participant to the given experiment. If no experiment with the corresponding id could be found or the
     * id is invalid, the user is redirected to the error page instead.
     *
     * @param id The id of the experiment.
     * @param search The username or email address to search for.
     * @param model The model used to store the error messages.
     * @return The experiment page on success, or the error page otherwise.
     */
    @RequestMapping("/search")
    @Secured(Constants.ROLE_ADMIN)
    public String searchForUser(@RequestParam("participant") final String search, @RequestParam(ID) final String id,
                                final Model model) {
        int experimentId = NumberParser.parseId(id);

        if (experimentId < Constants.MIN_ID) {
            LOGGER.error("Cannot search for a user to add as participant with an invalid experiment id!");
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

        if (!isValidSearch(search, experimentDTO, resourceBundle, model)) {
            return EXPERIMENT;
        }

        UserDTO userDTO = userService.getUserByUsernameOrEmail(search);
        validateUser(userDTO, experimentDTO, resourceBundle, model);

        if (model.getAttribute(ERROR) != null) {
            addModelInfo(0, experimentDTO, model);
            return EXPERIMENT;
        }

        try {
            String secret = userDTO.getSecret() == null ? Secrets.generateRandomBytes(Constants.SECRET_LENGTH)
                    : userDTO.getSecret();
            userDTO.setSecret(secret);
            UserDTO saved = userService.updateUser(userDTO);
            participantService.saveParticipant(saved.getId(), experimentId);
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }

        if (!ApplicationProperties.MAIL_SERVER) {
            return REDIRECT_SECRET + userDTO.getId() + EXPERIMENT_PARAM + experimentId;
        } else if (sendEmail(userDTO, id)) {
            return REDIRECT_EXPERIMENT + id;
        } else {
            return Constants.ERROR;
        }
    }

    /**
     * Loads the participant page with the given page number for the experiment with the given id, if the provided
     * numbers are valid.
     *
     * @param id The experiment id.
     * @param pageNumber The number of the page to be retrieved.
     * @param model The {@link Model} used to store the information.
     * @return The experiment page on success, or the error page otherwise.
     */
    @GetMapping("/page")
    @Secured(Constants.ROLE_ADMIN)
    public String getPage(@RequestParam(ID) final String id, @RequestParam(PAGE) final String pageNumber,
                          final Model model) {
        if (PageUtils.isInvalidParams(id, pageNumber)) {
            LOGGER.error("Cannot fetch participant page for invalid id " + id + " or invalid page number "
                    + pageNumber + "!");
            return Constants.ERROR;
        }

        int page = NumberParser.parseNumber(pageNumber);
        int experimentId = NumberParser.parseId(id);

        try {
            ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);
            return addModelInfo(page, experimentDTO, model) ? EXPERIMENT : Constants.ERROR;
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Retrieves all block event, resource event, block and resource event counts, codes and experiment data for the
     * given experiment and makes them available for download in a csv file.
     *
     * @param id The experiment id to search for.
     * @param httpServletResponse The servlet response returning the files.
     * @throws IncompleteDataException if the passed id is null or invalid.
     * @throws RuntimeException if an {@link IOException} occurs
     */
    @GetMapping("/csv")
    @Secured(Constants.ROLE_ADMIN)
    public void downloadCSVFile(@RequestParam(ID) final String id, final HttpServletResponse httpServletResponse) {
        if (id == null) {
            LOGGER.error("Cannot download CSV file for experiment with id null!");
            throw new IncompleteDataException("Cannot download CSV file for experiment with id null!");
        }

        int experimentId = NumberParser.parseId(id);

        if (experimentId < Constants.MIN_ID) {
            LOGGER.error("Cannot download CSV file for experiment with invalid id " + id + "!");
            throw new IncompleteDataException("Cannot download CSV file for experiment with invalid id " + id + "!");
        }

        try {
            httpServletResponse.setContentType("text/csv");
            httpServletResponse.setHeader("Content-Disposition", "attachment;filename=experiment_" + experimentId
                    + ".csv");
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            CSVWriter csvWriter = new CSVWriter(httpServletResponse.getWriter());

            List<String[]> blockEvents = eventService.getBlockEventData(experimentId);
            List<String[]> clickEvents = eventService.getClickEventData(experimentId);
            List<String[]> resourceEvents = eventService.getResourceEventData(experimentId);
            List<String[]> blockEventCounts = eventService.getBlockEventCount(experimentId);
            List<String[]> clickEventCounts = eventService.getClickEventCount(experimentId);
            List<String[]> resourceEventCounts = eventService.getResourceEventCount(experimentId);
            List<String[]> codesData = eventService.getCodesDataForExperiment(experimentId);
            List<String[]> experimentData = experimentService.getExperimentData(experimentId);

            csvWriter.writeAll(blockEvents);
            csvWriter.writeAll(clickEvents);
            csvWriter.writeAll(resourceEvents);
            csvWriter.writeAll(blockEventCounts);
            csvWriter.writeAll(clickEventCounts);
            csvWriter.writeAll(resourceEventCounts);
            csvWriter.writeAll(codesData);
            csvWriter.writeAll(experimentData);
        } catch (IOException e) {
            LOGGER.error("Could not download csv file due to IOException!", e);
            throw new RuntimeException("Could not download csv file due to IOException!");
        }
    }

    /**
     * Saves the content of the given sb3 file to the database for the experiment with the given id. If the file does
     * not meet the requirements, the user returns to the experiment page where an error message is displayed. If the
     * parameters are invalid, no corresponding experiment could be found, or an {@link IOException} occurred, the user
     * is redirected to the error page instead.
     *
     * @param id The experiment id to search for.
     * @param file The sb3 file to be uploaded.
     * @param model The model used to return error messages.
     * @return The experiment page on success, or if the file was invalid, or the error page otherwise.
     */
    @PostMapping("/upload")
    @Secured(Constants.ROLE_ADMIN)
    public String uploadProjectFile(@RequestParam("file") final MultipartFile file,
                                    @RequestParam(ID) final String id, final Model model) {
        if (id == null || file == null) {
            LOGGER.error("Cannot upload file for experiment with id null or with file null!");
            return Constants.ERROR;
        }

        int experimentId = NumberParser.parseId(id);

        if (experimentId < Constants.MIN_ID) {
            LOGGER.error("Cannot upload project file for experiment with invalid id " + id + "!");
            return Constants.ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());

        if (file.isEmpty()) {
            LOGGER.error("Cannot upload empty file for experiment with id " + id + "!");
            model.addAttribute(ERROR, resourceBundle.getString("file_empty"));
        } else if (file.getContentType() == null || !file.getContentType().equals("application/octet-stream")) {
            LOGGER.error("Cannot upload file with invalid content type " + file.getContentType() + "!");
            model.addAttribute(ERROR, resourceBundle.getString("file_type"));
        } else if (file.getOriginalFilename() == null || !file.getOriginalFilename().endsWith(Constants.SB3)) {
            LOGGER.error("Cannot upload file with invalid filename " + file.getOriginalFilename() + "!");
            model.addAttribute(ERROR, resourceBundle.getString("file_name"));
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
            LOGGER.error("Could not upload file due to IOException", e);
            return Constants.ERROR;
        }
    }

    /**
     * Deletes the sb3 file currently saved for the experiment with the given id. If the id is invalid, or no
     * corresponding experiment could be found, the user is redirected to the error page instead.
     *
     * @param id The experiment id to search for.
     * @return The experiment page on success, or the error page otherwise.
     */
    @GetMapping("/sb3")
    @Secured(Constants.ROLE_ADMIN)
    public String deleteProjectFile(@RequestParam(ID) final String id) {
        if (id == null) {
            LOGGER.error("Cannot delete file for experiment with id null!");
            return Constants.ERROR;
        }

        int experimentId = NumberParser.parseId(id);

        if (experimentId < Constants.MIN_ID) {
            LOGGER.error("Cannot delete project file for experiment with invalid id " + id + "!");
            return Constants.ERROR;
        }

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
    private boolean sendEmail(final UserDTO userDTO, final String experimentId) {
        if (userDTO.getEmail() == null) {
            LOGGER.error("Cannot send invitation mail to user with email null!");
            return false;
        }

        Map<String, Object> templateModel = getTemplateModel(experimentId, userDTO.getSecret());
        ResourceBundle userLanguage = ResourceBundle.getBundle("i18n/messages",
                getLocaleFromLanguage(userDTO.getLanguage()));

        if (!mailService.sendEmail(userDTO.getEmail(), userLanguage.getString("participant_email_subject"),
                templateModel, "participant-email")) {
            LOGGER.error("Could not send invitation mail to user with email " + userDTO.getEmail() + ".");
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
    private Map<String, Object> getTemplateModel(final String id, final String secret) {
        String experimentUrl = ApplicationProperties.BASE_URL + ApplicationProperties.CONTEXT_PATH
                + "/users/authenticate?id=" + id + "&secret=" + secret;
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("baseUrl", ApplicationProperties.BASE_URL + ApplicationProperties.CONTEXT_PATH);
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
     * @return {@code true}, if the current page number is lower than the last page number, or {@code false} otherwise.
     */
    private boolean addModelInfo(final int page, final ExperimentDTO experimentDTO, final Model model) {
        int last = pageService.getLastParticipantPage(experimentDTO.getId());

        if (page > last) {
            return false;
        }

        Page<Participant> participants = pageService.getParticipantPage(experimentDTO.getId(),
                PageRequest.of(page, Constants.PAGE_SIZE));
        model.addAttribute(PAGE, page);
        model.addAttribute("lastPage", last);
        model.addAttribute("participants", participants);
        addExperimentInfo(experimentDTO, model);
        return true;
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
                LOGGER.error("Experiment with same title exists!");
                FieldErrorHandler.addTitleExistsError(bindingResult, "experimentDTO", resourceBundle);
            }
        } else {
            if (experimentService.existsExperiment(experimentDTO.getTitle(), experimentDTO.getId())) {
                LOGGER.error("Experiment with same name but different id exists!");
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
            participantService.saveParticipants(experimentId, courseId);
            return false;
        } catch (Exception e) {
            LOGGER.error("Could not save course experiment!", e);
            experimentService.deleteExperiment(experimentId);
            return true;
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
