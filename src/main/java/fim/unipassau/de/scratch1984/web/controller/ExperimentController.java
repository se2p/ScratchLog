package fim.unipassau.de.scratch1984.web.controller;

import com.opencsv.CSVWriter;
import fim.unipassau.de.scratch1984.application.exception.IncompleteDataException;
import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.EventService;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.MailService;
import fim.unipassau.de.scratch1984.application.service.ParticipantService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.MarkdownHandler;
import fim.unipassau.de.scratch1984.util.Secrets;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    private static final Logger logger = LoggerFactory.getLogger(ExperimentController.class);

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
     * String corresponding to the experiment edit page.
     */
    private static final String EXPERIMENT_EDIT = "experiment-edit";

    /**
     * String corresponding to redirecting to the error page.
     */
    private static final String ERROR = "redirect:/error";

    /**
     * Constructs a new experiment controller with the given dependencies.
     *
     * @param experimentService The experiment service to use.
     * @param userService The user service to use.
     * @param participantService The participant service to use.
     * @param mailService The mail service to use.
     * @param eventService The event service to use.
     */
    @Autowired
    public ExperimentController(final ExperimentService experimentService, final UserService userService,
                                final ParticipantService participantService, final MailService mailService,
                                final EventService eventService) {
        this.experimentService = experimentService;
        this.userService = userService;
        this.participantService = participantService;
        this.mailService = mailService;
        this.eventService = eventService;
    }

    /**
     * Returns the experiment page displaying the information available for the experiment with the given id. If the
     * request parameter passed is invalid, no entry can be found in the database, or the user does not have sufficient
     * rights to see the page, the user is redirected to the error page instead.
     *
     * @param id The id of the experiment.
     * @param model The model to hold the information.
     * @param httpServletRequest The servlet request.
     * @return The experiment page on success, or the error page otherwise.
     */
    @GetMapping
    @Secured("ROLE_PARTICIPANT")
    public String getExperiment(@RequestParam("id") final String id, final Model model,
                                final HttpServletRequest httpServletRequest) {
        int experimentId = parseId(id);

        if (experimentId < Constants.MIN_ID) {
            return ERROR;
        }

        boolean participant = false;

        if (!httpServletRequest.isUserInRole("ROLE_ADMIN")) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            try {
                UserDTO userDTO = userService.getUser(authentication.getName());

                if (!userDTO.isActive() || userDTO.getSecret() == null) {
                    logger.debug("Cannot display experiment page for user with id " + userDTO.getId() + " since their "
                            + "account is inactive or their secret null!");
                    return ERROR;
                }

                ParticipantDTO participantDTO = participantService.getParticipant(experimentId, userDTO.getId());

                if (participantDTO.getStart() != null || participantDTO.getEnd() != null) {
                    logger.debug("Cannot display experiment page for user with id " + userDTO.getId() + " since they "
                            + "already started or finished the experiment!");
                    return ERROR;
                }

                participant = true;
            } catch (NotFoundException e) {
                logger.error("Can't find user with username " + authentication.getName() + " in the database!", e);
                return ERROR;
            }
        }

        try {
            ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);

            if (!experimentDTO.isActive() && participant) {
                logger.error("Cannot display experiment page of experiment with id " + experimentId + " for "
                        + "participants as the experiment is closed!");
                return ERROR;
            }
            if (participant) {
                model.addAttribute("participant", true);
            }

            addModelInfo(0, experimentDTO, model);
            return EXPERIMENT;
        } catch (NotFoundException e) {
            return ERROR;
        }
    }

    /**
     * Returns the form used to create or edit an experiment.
     *
     * @param experimentDTO The {@link ExperimentDTO} in which the information will be stored.
     * @return A new empty form.
     */
    @GetMapping("/create")
    @Secured("ROLE_ADMIN")
    public String getExperimentForm(@ModelAttribute("experimentDTO") final ExperimentDTO experimentDTO) {
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
    @Secured("ROLE_ADMIN")
    public String getEditExperimentForm(@RequestParam("id") final String id, final Model model) {
        int experimentId = parseId(id);

        if (experimentId < Constants.MIN_ID) {
            return ERROR;
        }

        try {
            ExperimentDTO findExperiment = experimentService.getExperiment(experimentId);
            model.addAttribute("experimentDTO", findExperiment);
            return EXPERIMENT_EDIT;
        } catch (NotFoundException e) {
            return ERROR;
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
    @Secured("ROLE_ADMIN")
    public String editExperiment(@ModelAttribute("experimentDTO") final ExperimentDTO experimentDTO,
                                 final BindingResult bindingResult) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        String titleValidation = validateInput(experimentDTO.getTitle(), Constants.LARGE_FIELD);
        String descriptionValidation = validateInput(experimentDTO.getDescription(), Constants.SMALL_AREA);

        if (titleValidation != null) {
            bindingResult.addError(createFieldError("title", titleValidation, resourceBundle));
        }
        if (descriptionValidation != null) {
            bindingResult.addError(createFieldError("description", descriptionValidation,
                    resourceBundle));
        }
        if (experimentDTO.getInfo().length() > Constants.LARGE_AREA) {
            bindingResult.addError(createFieldError("info", "long_string", resourceBundle));
        }
        if (experimentDTO.getPostscript() != null && !experimentDTO.getPostscript().trim().isBlank()) {
            if (experimentDTO.getPostscript().length() > Constants.SMALL_AREA) {
                bindingResult.addError(createFieldError("postscript", "long_string", resourceBundle));
            }
        }
        if (experimentDTO.getId() == null) {
            if (experimentService.existsExperiment(experimentDTO.getTitle())) {
                logger.error("Experiment with same title exists!");
                bindingResult.addError(createFieldError("title", "title_exists", resourceBundle));
            }
        } else {
            if (experimentService.existsExperiment(experimentDTO.getTitle(), experimentDTO.getId())) {
                logger.error("Experiment with same name but different id exists!");
                bindingResult.addError(createFieldError("title", "title_exists", resourceBundle));
            }
        }

        if (bindingResult.hasErrors()) {
            return EXPERIMENT_EDIT;
        }

        ExperimentDTO saved = experimentService.saveExperiment(experimentDTO);

        return REDIRECT_EXPERIMENT + saved.getId();
    }

    /**
     * Deletes the experiment with the given id from the database and redirects to the index page on success.
     *
     * @param id The id of the experiment.
     * @return The index page.
     */
    @GetMapping("/delete")
    @Secured("ROLE_ADMIN")
    public String deleteExperiment(@RequestParam("id") final String id) {
        int experimentId = parseId(id);

        if (experimentId < Constants.MIN_ID) {
            return ERROR;
        }

        experimentService.deleteExperiment(experimentId);
        return "redirect:/?success=true";
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
     * @param httpServletRequest The {@link HttpServletRequest}.
     * @return The experiment page.
     */
    @GetMapping("/status")
    @Secured("ROLE_ADMIN")
    public String changeExperimentStatus(@RequestParam("stat") final String status, @RequestParam("id") final String id,
                                         final Model model, final HttpServletRequest httpServletRequest) {
        int experimentId = parseId(id);

        if (experimentId < Constants.MIN_ID || status == null) {
            return ERROR;
        }

        try {
            ExperimentDTO experimentDTO;

            if (status.equals("open")) {
                experimentDTO = experimentService.changeExperimentStatus(true, experimentId);
                List<UserDTO> userDTOS = userService.reactivateUserAccounts(experimentId);

                for (UserDTO userDTO : userDTOS) {
                    Map<String, Object> templateModel = getTemplateModel(id, userDTO.getSecret(), httpServletRequest);
                    ResourceBundle userLanguage = ResourceBundle.getBundle("i18n/messages",
                            getLocaleFromLanguage(userDTO.getLanguage()));

                    if (!mailService.sendEmail(userDTO.getEmail(), userLanguage.getString("participant_email_subject"),
                            templateModel, "participant-email")) {
                        logger.error("Could not send invitation mail to user with email " + userDTO.getEmail() + ".");
                    }
                }
            } else if (status.equals("close")) {
                experimentDTO = experimentService.changeExperimentStatus(false, experimentId);
                participantService.deactivateParticipantAccounts(experimentId);
            } else {
                logger.debug("Cannot return the corresponding experiment page for requested status change " + status
                        + "!");
                return ERROR;
            }

            addModelInfo(0, experimentDTO, model);
        } catch (NotFoundException e) {
            return ERROR;
        }

        return EXPERIMENT;
    }

    /**
     * Searches for a user whose name or email address match the given search string. If a user could be found, they are
     * added as a participant to the given experiment. If no experiment with the corresponding id could be found or the
     * id is invalid, the user is redirected to the error page instead.
     *
     * @param id The id of the experiment.
     * @param search The username or email address to search for.
     * @param model The model used to store the error messages.
     * @param httpServletRequest The servlet request.
     * @return The experiment page on success, or the error page otherwise.
     */
    @RequestMapping("/search")
    @Secured("ROLE_ADMIN")
    public String searchForUser(@RequestParam("participant") final String search, @RequestParam("id") final String id,
                                final Model model, final HttpServletRequest httpServletRequest) {
        int experimentId = parseId(id);

        if (experimentId < Constants.MIN_ID) {
            return ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        String validateSearch = validateInput(search, Constants.LARGE_FIELD);
        ExperimentDTO experimentDTO;

        try {
            experimentDTO = experimentService.getExperiment(experimentId);
        } catch (NotFoundException e) {
            return ERROR;
        }

        if (validateSearch != null) {
            model.addAttribute("error", resourceBundle.getString(validateSearch));
            addModelInfo(0, experimentDTO, model);
            return EXPERIMENT;
        }

        UserDTO userDTO = userService.getUserByUsernameOrEmail(search);

        if (userDTO == null) {
            model.addAttribute("error", resourceBundle.getString("user_not_found"));
            addModelInfo(0, experimentDTO, model);
            return EXPERIMENT;
        } else if (!userDTO.getRole().equals(UserDTO.Role.PARTICIPANT)) {
            model.addAttribute("error", resourceBundle.getString("user_not_participant"));
        } else if (userService.existsParticipant(userDTO.getId(), experimentId)) {
            model.addAttribute("error", resourceBundle.getString("participant_entry"));
        } else if (userDTO.getSecret() != null) {
            model.addAttribute("error", resourceBundle.getString("user_participating"));
        } else if (!experimentDTO.isActive()) {
            model.addAttribute("error", resourceBundle.getString("experiment_closed"));
        }

        if (model.getAttribute("error") != null) {
            addModelInfo(0, experimentDTO, model);
            return EXPERIMENT;
        }

        String secret;

        try {
            secret = Secrets.generateRandomBytes(Constants.SECRET_LENGTH);
            userDTO.setSecret(secret);
            UserDTO saved = userService.updateUser(userDTO);
            participantService.saveParticipant(saved.getId(), experimentId);
        } catch (NotFoundException e) {
            return ERROR;
        }

        Map<String, Object> templateModel = getTemplateModel(id, secret, httpServletRequest);
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
     * Loads the the next participant page from the database. If the current page is the last page, or no experiment
     * with a corresponding id could be found in the database, the error page is displayed instead.
     *
     * @param id The experiment id.
     * @param model The model to store the loaded information in.
     * @param currentPage The page currently being displayed.
     * @return The experiment page on success, or the error page otherwise.
     */
    @GetMapping("/next")
    @Secured("ROLE_ADMIN")
    public String getNextPage(@RequestParam("id") final String id, @RequestParam("page") final String currentPage,
                              final Model model) {
        if (currentPage == null) {
            return ERROR;
        }

        int current = parseNumber(currentPage);
        int experimentId = parseId(id);

        if (experimentId < Constants.MIN_ID || current <= -1) {
            return ERROR;
        }

        try {
            ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);
            if (!addModelInfo(current, experimentDTO, model)) {
                return ERROR;
            }
        } catch (NotFoundException e) {
            return ERROR;
        }

        return EXPERIMENT;
    }

    /**
     * Loads the the previous participant page from the database. If the current page is the last page, or no experiment
     * with a corresponding id could be found in the database, the error page is displayed instead.
     *
     * @param id The experiment id.
     * @param model The model to store the loaded information in.
     * @param currentPage The page currently being displayed.
     * @return The index page on success, or the error page otherwise.
     */
    @GetMapping("/previous")
    @Secured("ROLE_ADMIN")
    public String getPreviousPage(@RequestParam("id") final String id, @RequestParam("page") final String currentPage,
                                  final Model model) {
        if (currentPage == null) {
            return ERROR;
        }

        int current = parseNumber(currentPage);
        int experimentId = parseId(id);

        if (experimentId < Constants.MIN_ID || current <= -1) {
            return ERROR;
        }

        try {
            ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);
            if (!addModelInfo(current - 2, experimentDTO, model)) {
                return ERROR;
            }
        } catch (NotFoundException e) {
            return ERROR;
        }

        return EXPERIMENT;
    }

    /**
     * Loads the the first participant page from the database. If the passed id is invalid, or no experiment with the
     * corresponding id could be found, the error page is displayed instead.
     *
     * @param id The experiment id.
     * @param model The model to store the loaded information in.
     * @return The index page on success, or the error page otherwise.
     */
    @GetMapping("/first")
    @Secured("ROLE_ADMIN")
    public String getFirstPage(@RequestParam("id") final String id, final Model model) {
        int experimentId = parseId(id);

        if (experimentId < Constants.MIN_ID) {
            return ERROR;
        }

        try {
            ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);
            addModelInfo(0, experimentDTO, model);
        } catch (NotFoundException e) {
            return ERROR;
        }

        return EXPERIMENT;
    }

    /**
     * Loads the the last participant page from the database. If the passed experiment id is invalid, or no experiment
     * with the corresponding id could be found, the error page is displayed instead.
     *
     * @param id The experiment id.
     * @param model The model to store the loaded information in.
     * @return The index page on success, or the error page otherwise.
     */
    @GetMapping("/last")
    @Secured("ROLE_ADMIN")
    public String getLastPage(@RequestParam("id") final String id, final Model model) {
        int experimentId = parseId(id);

        if (experimentId < Constants.MIN_ID) {
            return ERROR;
        }

        try {
            ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);
            int page = experimentService.getLastParticipantPage(experimentId);
            addModelInfo(page, experimentDTO, model);
        } catch (NotFoundException e) {
            return ERROR;
        }

        return EXPERIMENT;
    }

    /**
     * Retrieves all block event, resource event, block and resource event counts, codes and experiment data for the
     * given experiment and makes them available for download in a csv file. If the id is invalid an
     * {@link IncompleteDataException} is thrown instead. If an {@link IOException} occurs, a {@link RuntimeException}
     * is thrown.
     *
     * @param id The experiment id to search for.
     * @param httpServletResponse The servlet response returning the files.
     */
    @GetMapping("/csv")
    @Secured("ROLE_ADMIN")
    public void downloadCSVFile(@RequestParam("id") final String id, final HttpServletResponse httpServletResponse) {
        if (id == null) {
            logger.error("Cannot download CSV file for experiment with id null!");
            throw new IncompleteDataException("Cannot download CSV file for experiment with id null!");
        }

        int experimentId = parseId(id);

        if (experimentId < Constants.MIN_ID) {
            logger.error("Cannot download CSV file for experiment with invalid id " + id + "!");
            throw new IncompleteDataException("Cannot download CSV file for experiment with invalid id " + id + "!");
        }

        try {
            httpServletResponse.setContentType("text/csv");
            httpServletResponse.setHeader("Content-Disposition", "attachment;filename=experiment_" + experimentId
                    + ".csv");
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            CSVWriter csvWriter = new CSVWriter(httpServletResponse.getWriter());

            List<String[]> blockEvents = eventService.getBlockEventData(experimentId);
            List<String[]> resourceEvents = eventService.getResourceEventData(experimentId);
            List<String[]> blockEventCounts = eventService.getBlockEventCount(experimentId);
            List<String[]> resourceEventCounts = eventService.getResourceEventCount(experimentId);
            List<String[]> codesData = eventService.getCodesDataForExperiment(experimentId);
            List<String[]> experimentData = experimentService.getExperimentData(experimentId);

            csvWriter.writeAll(blockEvents);
            csvWriter.writeAll(resourceEvents);
            csvWriter.writeAll(blockEventCounts);
            csvWriter.writeAll(resourceEventCounts);
            csvWriter.writeAll(codesData);
            csvWriter.writeAll(experimentData);
        } catch (IOException e) {
            logger.error("Could not download csv file due to IOException!", e);
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
    @Secured("ROLE_ADMIN")
    public String uploadProjectFile(@RequestParam("file") final MultipartFile file,
                                    @RequestParam("id") final String id, final Model model) {
        if (id == null || file == null) {
            logger.error("Cannot upload file for experiment with id null or with file null!");
            return ERROR;
        }

        int experimentId = parseId(id);

        if (experimentId < Constants.MIN_ID) {
            logger.error("Cannot upload project file for experiment with invalid id " + id + "!");
            return ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());

        if (file.isEmpty()) {
            logger.error("Cannot upload empty file for experiment with id " + id + "!");
            model.addAttribute("error", resourceBundle.getString("file_empty"));
        } else if (file.getContentType() == null || !file.getContentType().equals("application/octet-stream")) {
            logger.error("Cannot upload file with invalid content type " + file.getContentType() + "!");
            model.addAttribute("error", resourceBundle.getString("file_type"));
        } else if (file.getOriginalFilename() == null || !file.getOriginalFilename().endsWith(Constants.SB3)) {
            logger.error("Cannot upload file with invalid filename " + file.getOriginalFilename() + "!");
            model.addAttribute("error", resourceBundle.getString("file_name"));
        }

        if (model.getAttribute("error") != null) {
            ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);
            addModelInfo(0, experimentDTO, model);
            return EXPERIMENT;
        }

        try {
            experimentService.uploadSb3Project(experimentId, file.getBytes());
            return REDIRECT_EXPERIMENT + experimentId;
        } catch (NotFoundException e) {
            return ERROR;
        } catch (IOException e) {
            logger.error("Could not upload file due to IOException", e);
            return ERROR;
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
    @Secured("ROLE_ADMIN")
    public String deleteProjectFile(@RequestParam("id") final String id) {
        if (id == null) {
            logger.error("Cannot delete file for experiment with id null!");
            return ERROR;
        }

        int experimentId = parseId(id);

        if (experimentId < Constants.MIN_ID) {
            logger.error("Cannot delete project file for experiment with invalid id " + id + "!");
            return ERROR;
        }

        try {
            experimentService.deleteSb3Project(experimentId);
            return REDIRECT_EXPERIMENT + experimentId;
        } catch (NotFoundException e) {
            return ERROR;
        }
    }

    /**
     * Creates a {@link Map} containing the base URL of the application and the link to the experiment with the given id
     * and the generated secret for the user that are going to be used in the experiment invitation email template.
     *
     * @param id The id of the experiment.
     * @param secret The user's secret.
     * @param httpServletRequest The servlet request.
     * @return The map containing the base URL and the experiment URL.
     */
    private Map<String, Object> getTemplateModel(final String id, final String secret,
                                                 final HttpServletRequest httpServletRequest) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(httpServletRequest).replacePath(null).build()
                .toUriString();
        String experimentUrl = baseUrl + "/users/authenticate?id=" + id + "&secret=" + secret;
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("baseUrl", baseUrl);
        templateModel.put("secret", experimentUrl);
        return templateModel;
    }

    /**
     * Retrieves the current participant page information from the database and adds the page to the {@link Model} along
     * with the {@link ExperimentDTO} and the last page.
     *
     * @param page The number of the current participant page to be retrieved.
     * @param experimentDTO The current experiment dto.
     * @param model The model used to save the information.
     * @return {@code true}, if the current page number is lower than the last page number, or {@code false} otherwise.
     */
    private boolean addModelInfo(final int page, final ExperimentDTO experimentDTO, final Model model) {
        if (experimentDTO.getInfo() != null) {
            experimentDTO.setInfo(MarkdownHandler.toHtml(experimentDTO.getInfo()));
        }

        int last = experimentService.getLastParticipantPage(experimentDTO.getId()) + 1;

        if (page >= last) {
            return false;
        }

        Page<Participant> participants = participantService.getParticipantPage(experimentDTO.getId(),
                PageRequest.of(page, Constants.PAGE_SIZE));

        if (experimentService.hasProjectFile(experimentDTO.getId())) {
            model.addAttribute("project", true);
        }

        model.addAttribute("page", page + 1);
        model.addAttribute("lastPage", last);
        model.addAttribute("experimentDTO", experimentDTO);
        model.addAttribute("participants", participants);
        return true;
    }

    /**
     * Parses the given string to a number, or returns -1, if the id is null or an invalid number.
     *
     * @param id The id to check.
     * @return The number corresponding to the id, if it is a valid number, or -1 otherwise.
     */
    private int parseId(final String id) {
        if (id == null) {
            logger.debug("Cannot return the corresponding experiment page for experiment with id null!");
            return -1;
        }

        int experimentId = parseNumber(id);

        if (experimentId < Constants.MIN_ID) {
            logger.debug("Cannot return the corresponding experiment page for experiment with invalid id "
                    + experimentId + "!");
            return -1;
        }

        return experimentId;
    }

    /**
     * Returns the corresponding int value of the given id, or -1, if the id is not a number.
     *
     * @param id The id in its string representation.
     * @return The corresponding int value, or -1.
     */
    private int parseNumber(final String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Checks, whether the given input string matches the general requirements and returns a custom error message
     * string if the it does not, or {@code null} if everything is fine.
     *
     * @param input The input string to check.
     * @param maxLength The maximum string length allowed for the field.
     * @return The custom error message string or {@code null}.
     */
    private String validateInput(final String input, final int maxLength) {
        if (input == null || input.trim().isBlank()) {
            return "empty_string";
        }

        if (input.length() > maxLength) {
            return "long_string";
        }

        return null;
    }

    /**
     * Creates a new field error with the given parameters.
     *
     * @param field The field to which the error applies.
     * @param error The error message string.
     * @param resourceBundle The resource bundle to retrieve the error message in the current language.
     * @return The new field error.
     */
    private FieldError createFieldError(final String field, final String error, final ResourceBundle resourceBundle) {
        return new FieldError("experimentDTO", field, resourceBundle.getString(error));
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

}
