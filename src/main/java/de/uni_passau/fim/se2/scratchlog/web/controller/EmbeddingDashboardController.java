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

import de.uni_passau.fim.se2.scratchlog.application.service.ParticipantService;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/embedding-dashboard")
public class EmbeddingDashboardController {

    private final ParticipantService participantService;

    @Autowired
    public EmbeddingDashboardController(final ParticipantService participantService) {
        this.participantService = participantService;
    }

    /**
     * Model init for the embedding dashboard page.
     *
     * @param experimentId The experiment id.
     * @param model The view model.
     * @return Redirect to the embedding dashboard page.
     */
    @GetMapping("")
    @Secured(Constants.ROLE_ADMIN)
    public String getDashboard(@RequestParam("id") final int experimentId, final Model model) {
        model.addAttribute("experiment", experimentId);
        model.addAttribute("participants", participantService.getActiveParticipantNames(experimentId));

        return "embedding-dashboard";
    }
}
