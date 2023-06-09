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
package fim.unipassau.de.scratchLog.persistence;

import fim.unipassau.de.scratchLog.persistence.entity.Experiment;
import fim.unipassau.de.scratchLog.persistence.entity.Sb3Zip;
import fim.unipassau.de.scratchLog.persistence.entity.User;
import fim.unipassau.de.scratchLog.persistence.repository.Sb3ZipRepository;
import fim.unipassau.de.scratchLog.util.enums.Language;
import fim.unipassau.de.scratchLog.util.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class Sb3ZipRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private Sb3ZipRepository sb3ZipRepository;

    private final LocalDateTime date = LocalDateTime.now();
    private static final String GUI_URL = "scratch";
    private User user1 = new User("participant1", "part1@part.de", Role.PARTICIPANT, Language.GERMAN, "password", "secret1");
    private User user2 = new User("participant2", "part2@part.de", Role.PARTICIPANT, Language.GERMAN, "password", "secret2");
    private Experiment experiment1 = new Experiment(null, "experiment1", "description", "info", "postscript", true,
            false, GUI_URL);
    private Experiment experiment2 = new Experiment(null, "experiment2", "description", "info", "postscript", true,
            false, GUI_URL);
    private Sb3Zip sb3Zip1 = new Sb3Zip(user1, experiment1, date, "zip1", new byte[]{1, 2, 3});
    private Sb3Zip sb3Zip2 = new Sb3Zip(user1, experiment1, date, "zip2", new byte[]{1, 2, 3});
    private Sb3Zip sb3Zip3 = new Sb3Zip(user1, experiment1, date, "zip3", new byte[]{1, 2, 3});
    private Sb3Zip sb3Zip4 = new Sb3Zip(user2, experiment1, date, "zip4", new byte[]{1, 2, 3});
    private Sb3Zip sb3Zip5 = new Sb3Zip(user1, experiment2, date, "zip5", new byte[]{1, 2, 3});

    @BeforeEach
    public void setup() {
        user1.setLastLogin(LocalDateTime.now());
        user2.setLastLogin(LocalDateTime.now());
        user1 = testEntityManager.persist(user1);
        user2 = testEntityManager.persist(user2);
        experiment1 = testEntityManager.persist(experiment1);
        experiment2 = testEntityManager.persist(experiment2);
        sb3Zip1 = testEntityManager.persist(sb3Zip1);
        sb3Zip2 = testEntityManager.persist(sb3Zip2);
        sb3Zip3 = testEntityManager.persist(sb3Zip3);
        sb3Zip4 = testEntityManager.persist(sb3Zip4);
        sb3Zip5 = testEntityManager.persist(sb3Zip5);
    }

    @Test
    public void testFindAllIdsByUserAndExperiment() {
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
    public void testFindAllIdsByUserAndExperimentNoEntries() {
        List<Integer> zipIds = sb3ZipRepository.findAllIdsByUserAndExperiment(user2, experiment2);
        assertTrue(zipIds.isEmpty());
    }
}
