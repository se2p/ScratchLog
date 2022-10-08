package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.application.exception.IncompleteDataException;
import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.ExperimentData;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentProjection;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentDataRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
     * The experiment data repository to use for database queries related to participant numbers.
     */
    private final ExperimentDataRepository experimentDataRepository;

    /**
     * Constructs an experiment service with the given dependencies.
     *
     * @param experimentRepository The experiment repository to use.
     * @param experimentDataRepository The experiment data repository to use.
     */
    @Autowired
    public ExperimentService(final ExperimentRepository experimentRepository,
                             final ExperimentDataRepository experimentDataRepository) {
        this.experimentRepository = experimentRepository;
        this.experimentDataRepository = experimentDataRepository;
    }

    /**
     * Checks, whether any experiment with the given title exists in the database.
     *
     * @param title The title to search for.
     * @return {@code true} if an experiment exists, or {@code false} if not.
     */
    @Transactional
    public boolean existsExperiment(final String title) {
        if (title == null || title.trim().isBlank()) {
            return false;
        }

        return experimentRepository.existsByTitle(title);
    }

    /**
     * Checks, whether any experiment with the given title exists in the database where the id does not match the
     * given id.
     *
     * @param title The title to search for.
     * @param id The id to compare to.
     * @return {@code true} if such an experiment exists, or {@code false} if not.
     */
    @Transactional
    public boolean existsExperiment(final String title, final int id) {
        if (title == null || title.trim().isBlank() || id < Constants.MIN_ID) {
            return false;
        }

        Experiment experiment = experimentRepository.findByTitle(title);

        if (experiment == null) {
            return false;
        } else {
            return experiment.getId() != id;
        }
    }

    /**
     * Checks, whether any experiment with the given id exists in the database whose project.
     *
     * @param id The id to search for.
     * @return {@code true} if such an experiment exists, or {@code false} if not.
     */
    @Transactional
    public boolean hasProjectFile(final int id) {
        if (id < Constants.MIN_ID) {
            return false;
        }

        return experimentRepository.existsByIdAndProjectIsNotNull(id);
    }

    /**
     * Creates a new experiment or updates an existing one with the given parameters in the database. If the information
     * could not be persisted correctly, a {@link StoreException} is thrown instead.
     *
     * @param experimentDTO The dto containing the experiment information to set.
     * @return The newly created experiment, if the information was persisted.
     */
    @Transactional
    public ExperimentDTO saveExperiment(final ExperimentDTO experimentDTO) {
        if (experimentDTO.getTitle() == null || experimentDTO.getTitle().trim().isBlank()) {
            logger.error("Cannot save experiment with empty title!");
            throw new IncompleteDataException("Cannot save experiment with empty title!");
        } else if (experimentDTO.getDescription() == null || experimentDTO.getDescription().trim().isBlank()) {
            logger.error("Cannot save experiment with empty description!");
            throw new IncompleteDataException("Cannot save experiment with empty description!");
        } else if (experimentDTO.getGuiURL() == null || experimentDTO.getGuiURL().trim().isBlank()) {
            logger.error("Cannot save experiment with empty GUI-URL!");
            throw new IncompleteDataException("Cannot save experiment with empty description!");
        }

        Experiment experiment = createExperiment(experimentDTO);

        if (experimentDTO.getId() != null) {
            Optional<Experiment> exists = experimentRepository.findById(experimentDTO.getId());
            exists.ifPresent(value -> experiment.setProject(value.getProject()));
        }

        Experiment saved = experimentRepository.save(experiment);

        if (saved.getId() == null) {
            logger.error("Failed to store experiment with title " + experimentDTO.getTitle());
            throw new StoreException("Failed to store experiment with title " + experimentDTO.getTitle());
        }

        return createExperimentDTO(saved);
    }

    /**
     * Returns the experiment with the specified id. If no such experiment exists, returns {@code null}.
     *
     * @param id The id to search for.
     * @return The experiment, if it exists, {@code null} if no experiment with that id exists.
     */
    @Transactional
    public ExperimentDTO getExperiment(final int id) {
        if (id < Constants.MIN_ID) {
            logger.error("Cannot search for experiment with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot search for experiment with invalid id " + id + "!");
        }

        Experiment experiment = experimentRepository.findById(id);

        if (experiment == null) {
            logger.error("Could not find experiment with id " + id + " in the database");
            throw new NotFoundException("Could not find experiment with id " + id + " in the database!");
        }

        return createExperimentDTO(experiment);
    }

    /**
     * Deletes the experiment with the given id from the database, if any such experiment exists.
     *
     * @param id The id to search for.
     */
    @Transactional
    public void deleteExperiment(final int id) {
        if (id < Constants.MIN_ID) {
            logger.error("Cannot delete experiment with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot delete experiment with invalid id " + id + "!");
        }

        experimentRepository.deleteById(id);
    }

    /**
     * Changes the status of the experiment with the given id to the given status value.
     *
     * @param status The new status.
     * @param id The experiment id.
     * @return The updated experiment data.
     */
    @Transactional
    public ExperimentDTO changeExperimentStatus(final boolean status, final int id) {
        if (!experimentRepository.existsById(id)) {
            logger.error("Could not update the status for non-existent experiment with id " + id + "!");
            throw new NotFoundException("Could not update the status for non-existent experiment with id " + id + "!");
        }

        experimentRepository.updateStatusById(id, status);
        Experiment experiment = experimentRepository.findById(id);
        return createExperimentDTO(experiment);
    }

    /**
     * Retrieves the experiment data for the experiment with the given ID as a list of string arrays. If the id is
     * invalid, an {@link IllegalArgumentException} is thrown instead.
     *
     * @param id The experiment ID.
     * @return The list of string arrays.
     */
    @Transactional
    public List<String[]> getExperimentData(final int id) {
        if (id < Constants.MIN_ID) {
            logger.error("Cannot retrieve experiment data for experiment with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot retrieve experiment data for experiment with invalid id " + id
                    + "!");
        }

        ExperimentData experimentData = experimentDataRepository.findByExperiment(id);
        List<String[]> list = new ArrayList<>();
        String[] header = {"experiment", "participants", "started", "finished"};
        list.add(header);

        if (experimentData != null) {
            String[] data = {experimentData.getExperiment().toString(),
                    String.valueOf(experimentData.getParticipants()), String.valueOf(experimentData.getStarted()),
                    String.valueOf(experimentData.getFinished())};
            list.add(data);
        }

        return list;
    }

    /**
     * Uploads the given byte array representing an sb3 project that is to be loaded when starting an experiment with
     * the given id. If the parameters are invalid, an {@link IllegalArgumentException} is thrown instead. If no
     * experiment with the corresponding id could be found, a {@link NotFoundException} is thrown.
     *
     * @param id The experiment ID.
     * @param project The sb3 project to upload.
     */
    @Transactional
    public void uploadSb3Project(final int id, final byte[] project) {
        if (project == null) {
            logger.error("Cannot upload sb3 project null!");
            throw new IllegalArgumentException("Cannot upload sb3 project null!");
        } else if (id < Constants.MIN_ID) {
            logger.error("Cannot upload sb3 project for experiment with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot upload sb3 project for experiment with invalid id " + id + "!");
        }

        try {
            Experiment experiment = experimentRepository.getOne(id);
            experiment.setProject(project);
            experimentRepository.save(experiment);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find experiment with id " + id + " when trying to upload an sb3 project!", e);
            throw new NotFoundException("Could not find experiment with id " + id + " when trying to upload an sb3 "
                    + "project!", e);
        }
    }

    /**
     * Deletes the current sb3 project for the experiment with the given id. If the parameters are invalid, an
     * {@link IllegalArgumentException} is thrown instead. If no experiment with the corresponding id could be found, a
     * {@link NotFoundException} is thrown.
     *
     * @param id The experiment ID.
     */
    @Transactional
    public void deleteSb3Project(final int id) {
        if (id < Constants.MIN_ID) {
            logger.error("Cannot delete sb3 project for experiment with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot delete sb3 project for experiment with invalid id " + id + "!");
        }

        try {
            Experiment experiment = experimentRepository.getOne(id);
            experiment.setProject(null);
            experimentRepository.save(experiment);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find experiment with id " + id + " when trying to delete an sb3 project!", e);
            throw new NotFoundException("Could not find experiment with id " + id + " when trying to delete an sb3 "
                    + "project!", e);
        }
    }

    /**
     * Retrieves an {@link ExperimentProjection} containing the experiment id and the current sb3 file from the
     * database. If no experiment with the corresponding id could be found, a {@link NotFoundException} is thrown.
     *
     * @param id The experiment ID.
     * @return The experiment projection.
     */
    @Transactional
    public ExperimentProjection getSb3File(final int id) {
        if (id < Constants.MIN_ID) {
            logger.error("Cannot retrieve sb3 project for experiment with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot retrieve sb3 project for experiment with invalid id " + id
                    + "!");
        }

        Optional<ExperimentProjection> projection = experimentRepository.findExperimentById(id);

        if (projection.isEmpty()) {
            logger.error("Could not find experiment with " + id + " when trying to retrieve its sb3 file!");
            throw new NotFoundException("Could not find experiment with " + id + " when trying to retrieve its sb3 "
                    + "file!");
        } else if (!projection.get().isActive()) {
            logger.error("Tried to retrieve the sb3 file for inactive experiment " + id + "!");
            throw new NotFoundException("Tried to retrieve the sb3 file for inactive experiment " + id + "!");
        }

        return projection.get();
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
        if (experimentDTO.getPostscript() != null && !experimentDTO.getPostscript().trim().isBlank()) {
            experiment.setPostscript(experimentDTO.getPostscript());
        }
        if (experimentDTO.getInfo() != null) {
            experiment.setInfo(experimentDTO.getInfo());
        }
        if (experimentDTO.getGuiURL() != null) {
            experiment.setGuiURL(experimentDTO.getGuiURL());
        }

        experiment.setActive(experimentDTO.isActive());
        experiment.setCourseExperiment(experimentDTO.isCourseExperiment());
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
        if (experiment.getPostscript() != null) {
            experimentDTO.setPostscript(experiment.getPostscript());
        }
        if (experiment.getInfo() != null) {
            experimentDTO.setInfo((experiment.getInfo()));
        }
        if (experiment.getGuiURL() != null) {
            experimentDTO.setGuiURL(experiment.getGuiURL());
        }

        experimentDTO.setActive(experiment.isActive());
        experimentDTO.setCourseExperiment(experiment.isCourseExperiment());
        return experimentDTO;
    }

}
