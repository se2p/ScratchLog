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

package de.uni_passau.fim.se2.scratchlog.application.service;

import de.uni_passau.fim.se2.scratchlog.application.exception.IncompleteDataException;
import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.ExperimentProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.web.dto.ExperimentDTO;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * A service providing methods related to experiments.
 */
@Service
public class ExperimentService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentService.class);

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
     * @throws IllegalArgumentException if the passed title is null or blank.
     */
    public boolean existsExperiment(final String title) {
        return experimentRepository.existsByTitle(title);
    }

    /**
     * Checks, whether any experiment with the given title exists in the database where the id does not match the
     * given id.
     *
     * @param title The title to search for.
     * @param id The id to compare to.
     * @return {@code true} if such an experiment exists, or {@code false} if not.
     * @throws IllegalArgumentException if the passed title is null or blank or the id is invalid.
     */
    public boolean existsExperiment(final String title, final int id) {
        Optional<Experiment> experiment = experimentRepository.findByTitle(title);

        if (experiment.isEmpty()) {
            return false;
        } else {
            return experiment.get().getId() != id;
        }
    }

    /**
     * Checks, whether any experiment with the given id exists in the database whose project.
     *
     * @param id The id to search for.
     * @return {@code true} if such an experiment exists, or {@code false} if not.
     * @throws IllegalArgumentException if the passed id is invalid.
     */
    public boolean hasProjectFile(final int id) {
        return experimentRepository.existsByIdAndProjectIsNotNull(id);
    }

    /**
     * Creates a new experiment or updates an existing one with the given parameters in the database.
     *
     * @param experimentDTO The dto containing the experiment information to set.
     * @return The newly created experiment, if the information was persisted.
     * @throws IncompleteDataException if the experiment title, description or GUI URL are null or blank.
     */
    @Transactional
    public ExperimentDTO saveExperiment(final ExperimentDTO experimentDTO) {
        if (experimentDTO.getTitle() == null || experimentDTO.getTitle().trim().isBlank()) {
            throw new IncompleteDataException("Cannot save experiment with empty title!");
        } else if (experimentDTO.getDescription() == null || experimentDTO.getDescription().trim().isBlank()) {
            throw new IncompleteDataException("Cannot save experiment with empty description!");
        } else if (experimentDTO.getGuiURL() == null || experimentDTO.getGuiURL().trim().isBlank()) {
            throw new IncompleteDataException("Cannot save experiment with empty GUI-URL!");
        }

        Experiment experiment = createExperiment(experimentDTO);

        if (experimentDTO.getId() != null) {
            Optional<Experiment> exists = experimentRepository.findById(experimentDTO.getId());
            exists.ifPresent(value -> experiment.setProject(value.getProject()));
        }

        Experiment saved = experimentRepository.save(experiment);
        return createExperimentDTO(saved);
    }

    /**
     * Returns the experiment with the specified id. If no such experiment exists, returns {@code null}.
     *
     * @param id The id to search for.
     * @return The experiment, if it exists, {@code null} if no experiment with that id exists.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding experiment could be found.
     */
    public ExperimentDTO getExperiment(final int id) {
        Experiment experiment = experimentRepository.findById(id);

        if (experiment == null) {
            LOGGER.error("Could not find experiment with id {} in the database", id);
            throw new NotFoundException("Could not find experiment with id " + id + " in the database!");
        }

        return createExperimentDTO(experiment);
    }

    /**
     * Deletes the experiment with the given id from the database, if any such experiment exists.
     *
     * @param id The id to search for.
     * @throws IllegalArgumentException if the passed id is invalid.
     */
    @Transactional
    public void deleteExperiment(final int id) {
        experimentRepository.deleteById(id);
    }

    /**
     * Changes the status of the experiment with the given id to the given status value.
     *
     * @param status The new status.
     * @param id The experiment id.
     * @return The updated experiment data.
     * @throws NotFoundException if no corresponding experiment could be found.
     */
    @Transactional
    public ExperimentDTO changeExperimentStatus(final boolean status, final int id) {
        if (!experimentRepository.existsById(id)) {
            LOGGER.error("Could not update the status for non-existent experiment with id {}!", id);
            throw new NotFoundException("Could not update the status for non-existent experiment with id " + id + "!");
        }

        experimentRepository.updateStatusById(id, status);
        Experiment experiment = experimentRepository.findById(id);
        return createExperimentDTO(experiment);
    }

    /**
     * Uploads the given byte array representing an sb3 project that is to be loaded when starting an experiment with
     * the given id.
     *
     * @param id The experiment ID.
     * @param project The sb3 project to upload.
     * @throws IllegalArgumentException if the passed project is null or the id is invalid.
     * @throws NotFoundException if no corresponding experiment could be found.
     */
    @Transactional
    public void uploadSb3Project(final int id, final byte[] project) {
        if (project == null) {
            throw new IllegalArgumentException("Cannot upload sb3 project null!");
        }

        try {
            Experiment experiment = experimentRepository.getReferenceById(id);
            experiment.setProject(project);
            experimentRepository.save(experiment);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find experiment with id {} when trying to upload an sb3 project!", id, e);
            throw new NotFoundException("Could not find experiment with id " + id + " when trying to upload an sb3 "
                    + "project!", e);
        }
    }

    /**
     * Deletes the current sb3 project for the experiment with the given id.
     *
     * @param id The experiment ID.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding experiment could be found.
     */
    @Transactional
    public void deleteSb3Project(final int id) {
        try {
            Experiment experiment = experimentRepository.getReferenceById(id);
            experiment.setProject(null);
            experimentRepository.save(experiment);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find experiment with id {} when trying to delete an sb3 project!", id, e);
            throw new NotFoundException("Could not find experiment with id " + id + " when trying to delete an sb3 "
                    + "project!", e);
        }
    }

    /**
     * Retrieves an {@link ExperimentProjection} containing the experiment id and the current sb3 file from the
     * database.
     *
     * @param id The experiment ID.
     * @param retrieveInactive Boolean indicating whether information on an inactive experiment should be returned.
     * @return The experiment projection.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding experiment could be found.
     */
    public ExperimentProjection getSb3File(final int id, final boolean retrieveInactive) {
        Optional<ExperimentProjection> projection = experimentRepository.findExperimentById(id);

        if (projection.isEmpty()) {
            LOGGER.error("Could not find experiment with {} when trying to retrieve its sb3 file!", id);
            throw new NotFoundException("Could not find experiment with " + id + " when trying to retrieve its sb3 "
                    + "file!");
        } else if (!projection.get().isActive() && !retrieveInactive) {
            LOGGER.error("Tried to retrieve the sb3 file for inactive experiment {}!", id);
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
        Experiment experiment = Experiment.builder()
                .title(experimentDTO.getTitle())
                .description(experimentDTO.getDescription())
                .guiURL(experimentDTO.getGuiURL())
                .active(experimentDTO.isActive())
                .build();

        if (experimentDTO.getId() != null) {
            experiment.setId(experimentDTO.getId());
        }
        if (experimentDTO.getPostscript() != null && !experimentDTO.getPostscript().trim().isBlank()) {
            experiment.setPostscript(experimentDTO.getPostscript());
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
        ExperimentDTO experimentDTO = ExperimentDTO.builder()
                .title(experiment.getTitle())
                .description(experiment.getDescription())
                .guiURL(experiment.getGuiURL())
                .active(experiment.isActive())
                .courseExperiment(experiment.isCourseExperiment())
                .build();

        if (experiment.getId() != null) {
            experimentDTO.setId(experiment.getId());
        }
        if (experiment.getPostscript() != null) {
            experimentDTO.setPostscript(experiment.getPostscript());
        }
        if (experiment.getInfo() != null) {
            experimentDTO.setInfo((experiment.getInfo()));
        }

        return experimentDTO;
    }

}
