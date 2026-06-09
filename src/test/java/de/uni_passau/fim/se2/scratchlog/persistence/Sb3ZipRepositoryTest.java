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

package de.uni_passau.fim.se2.scratchlog.persistence;

import de.uni_passau.fim.se2.scratchlog.AbstractScratchLogTest;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Sb3Zip;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.Sb3ZipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Sb3ZipRepositoryTest extends AbstractScratchLogTest {

    @Autowired
    private Sb3ZipRepository sb3ZipRepository;

    private User user1;
    private User user2;
    private Experiment experiment1;
    private Experiment experiment2;
    private Sb3Zip sb3Zip1;
    private Sb3Zip sb3Zip2;
    private Sb3Zip sb3Zip3;
    private Sb3Zip sb3Zip4;
    private Sb3Zip sb3Zip5;

    @BeforeEach
    void setup() {
        user1 = entityUtilService.generateUser("participant1");
        user2 = entityUtilService.generateUser("participant2");

        experiment1 = entityUtilService.generateExperiment("experiment1");
        experiment2 = entityUtilService.generateExperiment("experiment2");

        sb3Zip1 = entityUtilService.generateSb3Zip(user1, experiment1);
        sb3Zip2 = entityUtilService.generateSb3Zip(user1, experiment1);
        sb3Zip3 = entityUtilService.generateSb3Zip(user1, experiment1);
        sb3Zip4 = entityUtilService.generateSb3Zip(user2, experiment1);
        sb3Zip5 = entityUtilService.generateSb3Zip(user1, experiment2);
    }

    @Test
    void testFindAllIdsByUserAndExperiment() {
        List<Integer> zipIds = sb3ZipRepository.findAllIdsByUserAndExperiment(user1, experiment1);
        assertAll(
                () -> assertEquals(3, zipIds.size()),
                () -> assertTrue(zipIds.contains(sb3Zip1.getId())),
                () -> assertTrue(zipIds.contains(sb3Zip2.getId())),
                () -> assertTrue(zipIds.contains(sb3Zip3.getId())),
                () -> assertFalse(zipIds.contains(sb3Zip4.getId())),
                () -> assertFalse(zipIds.contains(sb3Zip5.getId()))
        );
    }

    @Test
    void testFindAllIdsByUserAndExperimentNoEntries() {
        List<Integer> zipIds = sb3ZipRepository.findAllIdsByUserAndExperiment(user2, experiment2);
        assertTrue(zipIds.isEmpty());
    }
}
