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

package de.uni_passau.fim.se2.scratchlog.application.service.dto;

import java.util.List;

/**
 * Collection of test results for the users in an experiment.
 *
 * @param exerciseId The experiment.
 * @param testCaseNames The names of the test cases.
 * @param userProgramTestResults The test results for user programs.
 */
public record ExperimentTestResults(
    int exerciseId,
    List<String> testCaseNames,
    List<UserProgramTestResults> userProgramTestResults
) {
}
