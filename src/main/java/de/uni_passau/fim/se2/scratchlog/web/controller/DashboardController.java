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

package de.uni_passau.fim.se2.scratchlog.web.controller;

import de.uni_passau.fim.se2.scratchlog.application.service.DashboardService;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ResourceEventSpecific;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The controller for managing the dashboard.
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    /**
     * The dashboard service to use for retrieving information displayed on the experiment dashboard.
     */
    private final DashboardService dashboardService;

    /**
     * Constructs a new dashboard REST controller with the given dependencies.
     *
     * @param dashboardService The {@link DashboardService} to use.
     */
    @Autowired
    public DashboardController(final DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Retrieves the dashboard page for the experiment with the given id. If no corresponding experiment could be found,
     * or the id is invalid, the user is returned to the error page instead.
     *
     * @param experimentId The experiment id.
     * @param model The {@link Model} used to store the experiment id.
     * @return The dashboard page on success, or the error page otherwise.
     */
    @GetMapping("")
    @Secured(Constants.ROLE_ADMIN)
    public String getDashboard(@RequestParam("id") final int experimentId, final Model model) {
        if (dashboardService.existsExperiment(experimentId) && dashboardService.existsParticipants(experimentId)) {
            model.addAttribute("experiment", experimentId);
            model.addAttribute("blockEvents", getBlockEvents());
            model.addAttribute("blockEvent", BlockEventSpecific.CREATE.toString());
            model.addAttribute("clickEvent", ClickEventSpecific.GREENFLAG.toString());
            model.addAttribute("resourceEvent", ResourceEventSpecific.ADD_COSTUME.toString());
            model.addAttribute("radarValues", getRadarChartEvents());
            return "dashboard";
        } else {
            return Constants.ERROR;
        }
    }

    /**
     * Retrieves all possible block events as a list of strings, excluding those that have been moved to click events.
     *
     * @return The list of block events.
     */
    private List<String> getBlockEvents() {
        BlockEventSpecific[] events = BlockEventSpecific.values();
        List<String> blockEvents = new ArrayList<>();
        Arrays.stream(events).forEach(event -> {
            if (event != BlockEventSpecific.GREENFLAG && event != BlockEventSpecific.STOPALL
                    && event != BlockEventSpecific.STACKCLICK) {
                blockEvents.add(event.toString());
            }
        });
        return blockEvents;
    }

    /**
     * Returns the events whose statistics should be displayed in the radar chart of the dashboard.
     *
     * @return The list of events.
     */
    private List<String> getRadarChartEvents() {
        List<String> radarEvents = new ArrayList<>();
        radarEvents.add(BlockEventSpecific.CREATE.toString());
        radarEvents.add(BlockEventSpecific.MOVE.toString());
        radarEvents.add(BlockEventSpecific.DELETE.toString());
        radarEvents.add(ClickEventSpecific.GREENFLAG.toString());
        radarEvents.add(ClickEventSpecific.STOPALL.toString());
        radarEvents.add(ClickEventSpecific.STACKCLICK.toString());
        return radarEvents;
    }

}
