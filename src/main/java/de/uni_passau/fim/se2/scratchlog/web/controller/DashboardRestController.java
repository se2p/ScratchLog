/*
 * This file is part of ScratchLog.
 * Licenced under the GPL v3.0 or later.
 *
 * SPDX-FileCopyrightText: 2021-2026 Scratchlog contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.uni_passau.fim.se2.scratchlog.web.controller;

import de.uni_passau.fim.se2.scratchlog.application.service.DashboardService;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ResourceEventSpecific;
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
     * The dashboard service to use for retrieving information displayed on the experiment dashboard.
     */
    private final DashboardService dashboardService;

    /**
     * String corresponding to the id request parameter.
     */
    private static final String ID = "id";

    /**
     * String corresponding to the users request parameter.
     */
    private static final String USERS = "users";

    /**
     * String corresponding to the event request parameter.
     */
    private static final String EVENT = "event";

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
     * @param experimentId The id of the experiment.
     * @return The experiment data.
     * @throws IllegalArgumentException if the passed id is invalid.
     */
    @GetMapping("")
    public DashboardService.ExperimentDataDto getExperimentData(@RequestParam(ID) final int experimentId) {
        return dashboardService.getExperimentData(experimentId);
    }

    /**
     * Retrieves the ids and usernames of all participants of the experiment with the given id.
     *
     * @param experimentId The id of the experiment.
     * @return The participant information.
     * @throws IllegalArgumentException if the passed id is invalid.
     */
    @GetMapping("/participants")
    public List<DashboardService.ParticipantIdName> getParticipantData(@RequestParam(ID) final int experimentId) {
        return dashboardService.getParticipants(experimentId);
    }

    /**
     * Returns the number of executions per minute of the given block event for the experiment and users with the given
     * ids.
     *
     * @param experimentId The id of the experiment.
     * @param userIds The ids of the users.
     * @param event The event of interest.
     * @return A list containing an array for each user with the number of executions.
     */
    @GetMapping("/event/block")
    public List<Integer[]> getBlockEventData(@RequestParam(ID) final int experimentId,
                                             @RequestParam(USERS) final List<Integer> userIds,
                                             @RequestParam(EVENT) final BlockEventSpecific event) {
        return dashboardService.getBlockEventCountData(userIds, experimentId, event);
    }

    /**
     * Returns the number of executions per minute of the given click event for the experiment and users with the given
     * ids.
     *
     * @param experimentId The id of the experiment.
     * @param userIds The ids of the users.
     * @param event The event of interest.
     * @return A list containing an array for each user with the number of executions.
     */
    @GetMapping("/event/click")
    public List<Integer[]> getClickEventData(@RequestParam(ID) final int experimentId,
                                             @RequestParam(USERS) final List<Integer> userIds,
                                             @RequestParam(EVENT) final ClickEventSpecific event) {
        return dashboardService.getClickEventCountData(userIds, experimentId, event);
    }

    /**
     * Returns the number of executions per minute of the given resource event for the experiment and users with the
     * given ids.
     *
     * @param experimentId The id of the experiment.
     * @param userIds The ids of the users.
     * @param event The event of interest.
     * @return A list containing an array for each user with the number of executions.
     */
    @GetMapping("/event/resource")
    public List<Integer[]> getResourceEventData(@RequestParam(ID) final int experimentId,
                                                @RequestParam(USERS) final List<Integer> userIds,
                                                @RequestParam(EVENT) final ResourceEventSpecific event) {
        return dashboardService.getResourceEventCountData(userIds, experimentId, event);
    }

    /**
     * Returns the total number of executions of specific click and block event for the experiment and users with the
     * given ids.
     *
     * @param experimentId The id of the experiment.
     * @param userIds The ids of the users.
     * @return A list containing an array for each user with the number of executions.
     */
    @GetMapping("/event/counts")
    public List<Integer[]> getEventCounts(
        @RequestParam(ID) final int experimentId, @RequestParam(USERS) final List<Integer> userIds
    ) {
        return dashboardService.getEventCountData(userIds, experimentId);
    }

}
