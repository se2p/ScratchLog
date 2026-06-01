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

import de.uni_passau.fim.se2.scratchlog.util.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Injects some global model attributes into each controller.
 */
@ControllerAdvice(annotations = Controller.class)
public class ApplicationPropertiesAdvice {

    /**
     * The application properties.
     */
    private final ApplicationProperties applicationProperties;

    /**
     * Autowiring constructor.
     *
     * @param applicationProperties The {@link ApplicationProperties}.
     */
    @Autowired
    public ApplicationPropertiesAdvice(final ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Injects the application properties as {@code applicationConfig} into the model for all controllers.
     *
     * @return The global application config.
     */
    @ModelAttribute("applicationConfig")
    public final ApplicationProperties getApplicationProperties() {
        return applicationProperties;
    }

}
