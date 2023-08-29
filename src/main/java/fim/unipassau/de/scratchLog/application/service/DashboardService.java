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

package fim.unipassau.de.scratchLog.application.service;

import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.persistence.entity.ExperimentData;
import fim.unipassau.de.scratchLog.persistence.repository.ExperimentDataRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratchLog.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * A service providing methods related to the experiment dashboard.
 */
@Service
public class DashboardService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardService.class);

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
    public DashboardService(final ExperimentRepository experimentRepository,
                            final ExperimentDataRepository experimentDataRepository) {
        this.experimentRepository = experimentRepository;
        this.experimentDataRepository = experimentDataRepository;
    }

    /**
     * Checks whether an experiment with the given id exists in the database.
     *
     * @param id The id to search for.
     * @return {@code true} if a corresponding experiment exists or {@code false} otherwise.
     * @throws IllegalArgumentException if the passed id is invalid.
     */
    public boolean existsExperiment(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot check if experiment exists with invalid id " + id + "!");
        }

        return experimentRepository.existsById(id);
    }

    /**
     * Retrieves the experiment data, i.e. how many participants the experiment has and how many of them started and
     * finished it, from the database.
     *
     * @param id The id of the experiment.
     * @return The corresponding experiment data.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding experiment data could be found.
     */
    public String[] getExperimentData(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot fetch experiment data for experiment with invalid id " + id
                    + "!");
        }

        Optional<ExperimentData> experimentData = experimentDataRepository.findByExperiment(id);

        if (experimentData.isEmpty()) {
            LOGGER.error("Could not find experiment data for experiment with id " + id + "!");
            throw new NotFoundException("Could not find experiment data for experiment with id " + id + "!");
        }

        return new String[]{String.valueOf(experimentData.get().getParticipants()),
                String.valueOf(experimentData.get().getStarted()), String.valueOf(experimentData.get().getFinished())};
    }

}
