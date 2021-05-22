package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.MarkdownHandler;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
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
     * The experiment service to use for management.
     */
    private final ExperimentService experimentService;

    /**
     * String corresponding to redirecting to the error page.
     */
    private static final String ERROR = "redirect:/error";

    /**
     * Constructs a new experiment controller with the given dependencies.
     *
     * @param experimentService The experiment service to use.
     */
    @Autowired
    public ExperimentController(final ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    /**
     * Returns the experiment page displaying the information available for the experiment with the given id. If the
     * request parameter passed is invalid, or no entry can be found in the database, the user is redirected to the
     * error page instead.
     *
     * @param id The id of the experiment.
     * @param model The model to hold the information.
     * @return The experiment page on success, or the error page otherwise.
     */
    @GetMapping
    @Secured("ROLE_PARTICIPANT")
    public String getExperiment(@RequestParam("id") final String id, final Model model) {
        int experimentId = parseId(id);

        if (experimentId == -1) {
            return ERROR;
        }

        try {
            ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);
            if (experimentDTO.getInfo() != null) {
                experimentDTO.setInfo(MarkdownHandler.toHtml(experimentDTO.getInfo()));
            }
            model.addAttribute("experimentDTO", experimentDTO);
            return "experiment";
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
        return "experiment-edit";
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

        if (experimentId == -1) {
            return ERROR;
        }

        try {
            ExperimentDTO findExperiment = experimentService.getExperiment(experimentId);
            model.addAttribute("experimentDTO", findExperiment);
            return "experiment-edit";
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
            return "experiment-edit";
        }

        ExperimentDTO saved = experimentService.saveExperiment(experimentDTO);

        return "redirect:/experiment?id=" + saved.getId();
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

        if (experimentId == -1) {
            return ERROR;
        }

        experimentService.deleteExperiment(experimentId);
        return "redirect:/?success=true";
    }

    /**
     * Changes the experiment status to the given request parameter value. If the passed id or status values are
     * invalid, or no corresponding experiment exists in the database, the user is redirected to the error page instead.
     *
     * @param id The id of the experiment.
     * @param status The new status of the experiment.
     * @param model The model to hold the information.
     * @return The experiment page.
     */
    @GetMapping("/status")
    @Secured("ROLE_ADMIN")
    public String changeExperimentStatus(@RequestParam("stat") final String status,
                                         @RequestParam("id") final String id, final Model model) {
        int experimentId = parseId(id);

        if (experimentId == -1 || status == null) {
            return ERROR;
        }

        try {
            ExperimentDTO experimentDTO;

            if (status.equals("open")) {
                experimentDTO = experimentService.changeExperimentStatus(true, experimentId);
            } else if (status.equals("close")) {
                experimentDTO = experimentService.changeExperimentStatus(false, experimentId);
            } else {
                logger.debug("Cannot return the corresponding experiment page for requested status change " + status
                        + "!");
                return ERROR;
            }

            if (experimentDTO.getInfo() != null) {
                experimentDTO.setInfo(MarkdownHandler.toHtml(experimentDTO.getInfo()));
            }

            model.addAttribute("experimentDTO", experimentDTO);
        } catch (NotFoundException e) {
            return ERROR;
        }

        return "experiment";
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

        int experimentId = parseExperimentId(id);

        if (experimentId <= 0) {
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
    private int parseExperimentId(final String id) {
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

}
