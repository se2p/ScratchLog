package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.util.Constants;
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
        if (id == null) {
            logger.debug("Cannot return the corresponding experiment page for experiment with id null!");
            return ERROR;
        }

        int experimentId = parseExperimentId(id);

        if (experimentId == -1) {
            logger.debug("Cannot return the corresponding experiment page for experiment with invalid id "
                    + experimentId + "!");
            return ERROR;
        }

        if (model.getAttribute("experimentDTO") == null) {
            logger.info("Retrieving experiment with id " + experimentId + " from the database.");
            try {
                ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);
                model.addAttribute("experimentDTO", experimentDTO);
            } catch (NotFoundException e) {
                return ERROR;
            }
        }

        return "experiment";
    }

    /**
     * Returns the form used to create or edit an experiment.
     *
     * @param experimentDTO The {@link ExperimentDTO} in which the information will be stored.
     * @return A new empty form.
     */
    @GetMapping("/create")
    @Secured("ROLE_ADMIN")
    public String getExperimentForm(final ExperimentDTO experimentDTO) {
        return "experiment-edit";
    }

    /**
     * Creates a new experiment from the given {@link ExperimentDTO} and redirects to corresponding experiment page on
     * success. If the input form data is invalid, the current page is returned instead to display the error messages.
     *
     * @param experimentDTO The experiment dto containing the input data.
     * @param model The model to which the newly created dto is appended on success.
     * @param bindingResult The binding result for returning information on invalid user input.
     * @return The experiment edit page, if the input is invalid, or experiment page on success.
     */
    @PostMapping("/create")
    @Secured("ROLE_ADMIN")
    public String createExperiment(@ModelAttribute("experimentDTO") final ExperimentDTO experimentDTO,
                                   final Model model, final BindingResult bindingResult) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        String titleValidation = validateInput(experimentDTO.getTitle(), Constants.LARGE_FIELD);
        String descriptionValidation = validateInput(experimentDTO.getDescription(), Constants.SMALL_AREA);

        if (titleValidation != null) {
            bindingResult.addError(createFieldError("title", titleValidation, resourceBundle));
        } else if (experimentService.existsExperiment(experimentDTO.getTitle())) {
            bindingResult.addError(createFieldError("title", "title_exists", resourceBundle));
        }
        if (descriptionValidation != null) {
            bindingResult.addError(createFieldError("description", descriptionValidation,
                    resourceBundle));
        }
        if (experimentDTO.getInfo().length() > Constants.LARGE_AREA) {
            bindingResult.addError(createFieldError("info", "long_string", resourceBundle));
        }

        if (bindingResult.hasErrors()) {
            return "experiment-edit";
        }

        ExperimentDTO saved = experimentService.saveExperiment(experimentDTO);
        model.addAttribute("experimentDTO", saved);

        return "redirect:/experiment?id=" + saved.getId();
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
