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

package de.uni_passau.fim.se2.scratchlog.persistence.repository;

import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestResult;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.TestCaseCountSummary;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface TestResultRepository extends JpaRepository<TestResult, Integer> {

    @Modifying
    @Transactional
    void deleteAllByProject_Id(int projectId);

    @EntityGraph(attributePaths = {"testCase"})
    List<TestResult> findAllByProjectIdIn(Set<Integer> projectIds);

    /**
     * Counts how many test case results exist for the given block events.
     *
     * @param blockEventIds Some block events.
     * @return The number of test case results grouped by block event and kind of result.
     */
    @Query("""
            select new de.uni_passau.fim.se2.scratchlog.persistence.projection.TestCaseCountSummary(
                t.project.id,
                t.result,
                count(*)
            )
            from TestResult t
            where t.project.id in :blockEventIds
            group by t.project.id, t.result
            """)
    List<TestCaseCountSummary> getTestResultSummaries(Set<Integer> blockEventIds);

}
