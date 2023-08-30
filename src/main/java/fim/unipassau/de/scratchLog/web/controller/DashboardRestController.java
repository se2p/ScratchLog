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

import fim.unipassau.de.scratchLog.application.service.DashboardService;
import fim.unipassau.de.scratchLog.util.Constants;
import fim.unipassau.de.scratchLog.util.NumberParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The REST controller for retrieving data to be displayed on the experiment dashboard.
 */
@RestController
@RequestMapping("/dashboard/data")
public class DashboardRestController {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardRestController.class);

    /**
     * The dashboard service to use for retrieving information displayed on the experiment dashboard.
     */
    private final DashboardService dashboardService;

    /**
     * String corresponding to the id request parameter.
     */
    private static final String ID = "id";

    /**
     * Constructs a new dashboard REST controller with the given dependencies.
     *
     * @param dashboardService The {@link DashboardService} to use.
     */
    @Autowired
    public DashboardRestController(final DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Retrieves the experiment data, i.e. how many participants the experiment has and how many of them have started or
     * finished it, to be displayed on the dashboard page.
     *
     * @param id The id of the experiment.
     * @return The experiment data.
     * @throws IllegalArgumentException if the passed id is invalid.
     */
    @GetMapping("")
    public String[] getExperimentData(@RequestParam(ID) final String id) {
        int experimentId = parseExperimentId(id);
        return dashboardService.getExperimentData(experimentId);
    }

    /**
     * Retrieves the ids and usernames of all participants of the experiment with the given id.
     *
     * @param id The id of the experiment.
     * @return The participant information.
     * @throws IllegalArgumentException if the passed id is invalid.
     */
    @GetMapping("/participants")
    public List<String[]> getParticipantData(@RequestParam(ID) final String id) {
        int experimentId = parseExperimentId(id);
        return dashboardService.getParticipants(experimentId);
    }

    /**
     * Parses the given id string to an integer.
     *
     * @param id The string representation of the id.
     * @return The integer representation of the id.
     * @throws IllegalArgumentException if the passed id could not be parsed into a number or is an invalid id.
     */
    private int parseExperimentId(final String id) {
        int experimentId = NumberParser.parseId(id);

        if (experimentId < Constants.MIN_ID) {
            LOGGER.error("Cannot retrieve data for experiment dashboard with invalid experiment id " + id + "!");
            throw new IllegalArgumentException("Cannot retrieve data for experiment dashboard with invalid experiment "
                    + "id " + id + "!");
        }

        return experimentId;
    }

}
