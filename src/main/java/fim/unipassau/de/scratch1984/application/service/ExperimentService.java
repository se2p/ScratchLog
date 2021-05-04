package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service providing methods related to experiments.
 */
@Service
public class ExperimentService {

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
     * Checks, whether any experiment with the given name exists in the database.
     *
     * @param name The name to search for.
     * @return {@code true} if an experiment exists, or {@code false} if not.
     */
    public boolean existsExperiment(final String name) {
        return false;
    }

    /**
     * Creates a new experiment with the given parameters in the database.
     *
     * @param experimentDTO The dto containing the experiment information to set.
     * @return The newly created experiment, if the information was persisted, or {@code null} if not.
     */
    public ExperimentDTO saveExperiment(final ExperimentDTO experimentDTO) {
        return null;
    }

    /**
     * Returns the experiment with the specified name. If no such experiment exists, returns {@code null}.
     *
     * @param name The name to search for.
     * @return The experiment, if it exists, {@code null} if no experiment with that name exists.
     */
    public ExperimentDTO getExperiment(final String name) {
        return null;
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
     * Deletes the experiment with the given name from the database, if any such experiment exists.
     *
     * @param name The name to search for.
     * @return {@code true} if the deletion was successful, or {@code false} if not.
     */
    public boolean deleteExperiment(final String name) {
        return false;
    }

    /**
     * Creates a {@link Experiment} with the given information of the {@link ExperimentDTO}.
     *
     * @param experimentDTO The dto containing the information.
     * @return The new experiment containing the information passed in the DTO.
     */
    private Experiment createExperiment(final ExperimentDTO experimentDTO) {
        return null;
    }

    /**
     * Creates a {@link ExperimentDTO} with the given information from the {@link Experiment}.
     *
     * @param experiment The experiment object containing the information.
     * @return The new experiment DTO containing the information passed in the experiment object.
     */
    private ExperimentDTO createExperimentDTO(final Experiment experiment) {
        return null;
    }

}
