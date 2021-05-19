package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.application.exception.IncompleteDataException;
import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.util.MarkdownHandler;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service providing methods related to experiments.
 */
@Service
public class ExperimentService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(ExperimentService.class);

    /**
     * The experiment repository to use for database queries related to experiment data.
     */
    private final ExperimentRepository experimentRepository;

    /**
     * Constructs an experiment service with the given dependencies.
     *
     * @param experimentRepository The experiment repository to use.
     */
    @Autowired
    public ExperimentService(final ExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
    }

    /**
     * Checks, whether any experiment with the given title exists in the database.
     *
     * @param title The title to search for.
     * @return {@code true} if an experiment exists, or {@code false} if not.
     */
    public boolean existsExperiment(final String title) {
        if (title == null || title.trim().isBlank()) {
            return false;
        }

        return experimentRepository.existsByTitle(title);
    }

    /**
     * Creates a new experiment with the given parameters in the database.
     *
     * @param experimentDTO The dto containing the experiment information to set.
     * @return The newly created experiment, if the information was persisted, or {@code null} if not.
     */
    public ExperimentDTO saveExperiment(final ExperimentDTO experimentDTO) {
        if (experimentDTO.getTitle() == null || experimentDTO.getTitle().trim().isBlank()) {
            logger.error("Cannot save experiment with empty title!");
            throw new IncompleteDataException("Cannot save experiment with empty title!");
        } else if (experimentDTO.getDescription() == null || experimentDTO.getDescription().trim().isBlank()) {
            logger.error("Cannot save experiment with empty description!");
            throw new IncompleteDataException("Cannot save experiment with empty description!");
        }

        Experiment experiment = experimentRepository.save(createExperiment(experimentDTO));

        if (experiment.getId() == null) {
            logger.error("Failed to store experiment with title " + experimentDTO.getTitle());
            throw new StoreException("Failed to store experiment with title " + experimentDTO.getTitle());
        }

        return createExperimentDTO(experiment);
    }

    /**
     * Returns the experiment with the specified id. If no such experiment exists, returns {@code null}.
     *
     * @param id The id to search for.
     * @return The experiment, if it exists, {@code null} if no experiment with that id exists.
     */
    public ExperimentDTO getExperiment(final int id) {
        Experiment experiment = experimentRepository.findById(id);

        if (experiment == null) {
            logger.error("Could not find experiment with id " + id + " in the database");
            throw new NotFoundException("Could not find experiment with id " + id + " in the database!");
        }

        return createExperimentDTO(experiment);
    }

    /**
     * Updates the information of the given experiment with the given values, or creates a new one, if no such
     * experiment exists.
     *
     * @param experimentDTO The dto containing the updated experiment information.
     * @return {@code true} if the information was persisted, or {@code false} if not.
     */
    public boolean updateExperiment(final ExperimentDTO experimentDTO) {
        return false;
    }

    /**
     * Deletes the experiment with the given title from the database, if any such experiment exists.
     *
     * @param title The title to search for.
     * @return {@code true} if the deletion was successful, or {@code false} if not.
     */
    public boolean deleteExperiment(final String title) {
        return false;
    }

    /**
     * Creates a {@link Experiment} with the given information of the {@link ExperimentDTO}.
     *
     * @param experimentDTO The dto containing the information.
     * @return The new experiment containing the information passed in the DTO.
     */
    private Experiment createExperiment(final ExperimentDTO experimentDTO) {
        Experiment experiment = new Experiment();

        if (experimentDTO.getId() != null) {
            experiment.setId(experimentDTO.getId());
        }
        if (experimentDTO.getTitle() != null) {
            experiment.setTitle(experimentDTO.getTitle());
        }
        if (experimentDTO.getDescription() != null) {
            experiment.setDescription(experimentDTO.getDescription());
        }
        if (experimentDTO.getInfo() != null) {
            experiment.setInfo(experimentDTO.getInfo());
        }

        return experiment;
    }

    /**
     * Creates a {@link ExperimentDTO} with the given information from the {@link Experiment}.
     *
     * @param experiment The experiment object containing the information.
     * @return The new experiment DTO containing the information passed in the experiment object.
     */
    private ExperimentDTO createExperimentDTO(final Experiment experiment) {
        ExperimentDTO experimentDTO = new ExperimentDTO();

        if (experiment.getId() != null) {
            experimentDTO.setId(experiment.getId());
        }
        if (experiment.getTitle() != null) {
            experimentDTO.setTitle(experiment.getTitle());
        }
        if (experiment.getDescription() != null) {
            experimentDTO.setDescription(experiment.getDescription());
        }
        if (experiment.getInfo() != null) {
            experimentDTO.setInfo(MarkdownHandler.toHtml(experiment.getInfo()));
        }

        return experimentDTO;
    }

}
