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
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The controller for managing the dashboard.
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);

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
     * @param id The experiment id.
     * @param model The {@link Model} used to store the experiment id.
     * @return The dashboard page on success, or the error page otherwise.
     */
    @GetMapping("")
    @Secured(Constants.ROLE_ADMIN)
    public String getDashboard(@RequestParam("id") final String id, final Model model) {
        int experimentId = NumberParser.parseId(id);

        if (experimentId < Constants.MIN_ID) {
            LOGGER.error("Cannot return dashboard for experiment with invalid id " + id + "!");
            return Constants.ERROR;
        }

        if (dashboardService.existsExperiment(experimentId)) {
            model.addAttribute("experiment", experimentId);
            return "dashboard";
        } else {
            return Constants.ERROR;
        }
    }

}
