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

import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestResultState;

import java.util.Map;

/**
 * The test results for a user program.
 *
 * @param userId The ID of a user.
 * @param username The name of a user.
 * @param blockEventId The ID of the block event (ie program) these test results are for.
 * @param testResults A map of test case name to result.
 */
public record UserProgramTestResults(
    int userId,
    String username,
    int blockEventId,
    Map<String, TestResultState> testResults
) {
}
