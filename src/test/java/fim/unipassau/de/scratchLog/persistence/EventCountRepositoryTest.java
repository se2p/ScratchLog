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

import fim.unipassau.de.scratchLog.persistence.entity.BlockEvent;
import fim.unipassau.de.scratchLog.persistence.entity.ClickEvent;
import fim.unipassau.de.scratchLog.persistence.entity.EventCount;
import fim.unipassau.de.scratchLog.persistence.entity.Experiment;
import fim.unipassau.de.scratchLog.persistence.entity.ResourceEvent;
import fim.unipassau.de.scratchLog.persistence.entity.User;
import fim.unipassau.de.scratchLog.persistence.repository.EventCountRepository;
import fim.unipassau.de.scratchLog.util.enums.BlockEventSpecific;
import fim.unipassau.de.scratchLog.util.enums.BlockEventType;
import fim.unipassau.de.scratchLog.util.enums.ClickEventSpecific;
import fim.unipassau.de.scratchLog.util.enums.ClickEventType;
import fim.unipassau.de.scratchLog.util.enums.Language;
import fim.unipassau.de.scratchLog.util.enums.ResourceEventSpecific;
import fim.unipassau.de.scratchLog.util.enums.ResourceEventType;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class EventCountRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private EventCountRepository eventCountRepository;

    private final LocalDateTime date = LocalDateTime.now();
    private static final String GUI_URL = "scratch";
    private User user1 = new User("participant1", "part1@part.de", Role.PARTICIPANT, Language.GERMAN, "password", "secret1");
    private User user2 = new User("participant2", "part2@part.de", Role.PARTICIPANT, Language.GERMAN, "password", "secret2");
    private Experiment experiment1 = new Experiment(null, "experiment1", "description", "info", "postscript", true,
            false, GUI_URL);
    private Experiment experiment2 = new Experiment(null, "experiment2", "description", "info", "postscript", true,
            false, GUI_URL);
    private BlockEvent blockEvent1 = new BlockEvent(user1, experiment1, date, BlockEventType.CREATE,
            BlockEventSpecific.CREATE, "Figur1", null, "xml", "json");
    private BlockEvent blockEvent2 = new BlockEvent(user1, experiment1, date, BlockEventType.DRAG,
            BlockEventSpecific.ENDDRAG, "Figur1", null, null, null);
    private BlockEvent blockEvent3 = new BlockEvent(user1, experiment2, date, BlockEventType.MOVE,
            BlockEventSpecific.MOVE, "Figur1", null, null, null);
    private BlockEvent blockEvent4 = new BlockEvent(user2, experiment1, date, BlockEventType.CLICK,
            BlockEventSpecific.GREENFLAG, "Figur1", null, null, null);
    private BlockEvent blockEvent5 = new BlockEvent(user1, experiment1, date, BlockEventType.DRAG,
            BlockEventSpecific.ENDDRAG, "Figur1", null, null, null);
    private ClickEvent clickEvent1 = new ClickEvent(user1, experiment1, date, ClickEventType.ICON,
            ClickEventSpecific.GREENFLAG, "");
    private ClickEvent clickEvent2 = new ClickEvent(user1, experiment1, date, ClickEventType.CODE,
            ClickEventSpecific.STACKCLICK, "");
    private ClickEvent clickEvent3 = new ClickEvent(user1, experiment1, date, ClickEventType.CODE,
            ClickEventSpecific.STACKCLICK, "");
    private ClickEvent clickEvent4 = new ClickEvent(user1, experiment2, date, ClickEventType.CODE,
            ClickEventSpecific.STACKCLICK, "");
    private ClickEvent clickEvent5 = new ClickEvent(user2, experiment1, date, ClickEventType.ICON,
            ClickEventSpecific.GREENFLAG, "");
    private ResourceEvent resourceEvent1 = new ResourceEvent(user1, experiment1, date, ResourceEventType.ADD,
            ResourceEventSpecific.ADD_SOUND, "Miau", "hash", "wav", 0);
    private ResourceEvent resourceEvent2 = new ResourceEvent(user1, experiment1, date, ResourceEventType.DELETE,
            ResourceEventSpecific.DELETE_SOUND, "Miau", "hash", "wav", null);
    private ResourceEvent resourceEvent3 = new ResourceEvent(user1, experiment1, date, ResourceEventType.ADD,
            ResourceEventSpecific.ADD_SOUND, "Miau2", "hash", "wav", 0);
    private ResourceEvent resourceEvent4 = new ResourceEvent(user1, experiment2, date, ResourceEventType.ADD,
            ResourceEventSpecific.ADD_COSTUME, "image", "hash", "jpg", 0);
    private ResourceEvent resourceEvent5 = new ResourceEvent(user2, experiment1, date, ResourceEventType.ADD,
            ResourceEventSpecific.ADD_COSTUME, "image", "hash", "jpg", 0);

    @BeforeEach
    public void setup() {
        user1.setLastLogin(LocalDateTime.now());
        user2.setLastLogin(LocalDateTime.now());
        user1 = testEntityManager.persist(user1);
        user2 = testEntityManager.persist(user2);
        experiment1 = testEntityManager.persist(experiment1);
        experiment2 = testEntityManager.persist(experiment2);
        blockEvent1 = testEntityManager.persist(blockEvent1);
        blockEvent2 = testEntityManager.persist(blockEvent2);
        blockEvent3 = testEntityManager.persist(blockEvent3);
        blockEvent4 = testEntityManager.persist(blockEvent4);
        blockEvent5 = testEntityManager.persist(blockEvent5);
        clickEvent1 = testEntityManager.persist(clickEvent1);
        clickEvent2 = testEntityManager.persist(clickEvent2);
        clickEvent3 = testEntityManager.persist(clickEvent3);
        clickEvent4 = testEntityManager.persist(clickEvent4);
        clickEvent5 = testEntityManager.persist(clickEvent5);
        resourceEvent1 = testEntityManager.persist(resourceEvent1);
        resourceEvent2 = testEntityManager.persist(resourceEvent2);
        resourceEvent3 = testEntityManager.persist(resourceEvent3);
        resourceEvent4 = testEntityManager.persist(resourceEvent4);
        resourceEvent5 = testEntityManager.persist(resourceEvent5);
    }

    @Test
    public void testFindAllBlockEventsByUserAndExperiment() {
        List<EventCount> eventCounts = eventCountRepository.findAllBlockEventsByUserAndExperiment(user1.getId(),
                experiment1.getId());
        assertAll(
                () -> assertEquals(2, eventCounts.size()),
                () -> assertEquals(blockEvent1.getEvent().toString(), eventCounts.get(0).getEvent()),
                () -> assertEquals(1, eventCounts.get(0).getCount()),
                () -> assertEquals(blockEvent2.getEvent().toString(), eventCounts.get(1).getEvent()),
                () -> assertEquals(2, eventCounts.get(1).getCount())
        );
    }

    @Test
    public void testFindAllBlockEventsByUserAndExperimentNoEntries() {
        List<EventCount> eventCounts = eventCountRepository.findAllBlockEventsByUserAndExperiment(user2.getId(),
                experiment2.getId());
        assertTrue(eventCounts.isEmpty());
    }

    @Test
    public void testFindAllBlockEventsByExperiment() {
        List<EventCount> eventCounts = eventCountRepository.findAllBlockEventsByExperiment(experiment1.getId());
       assertEquals(3, eventCounts.size());
    }

    @Test
    public void testFindAllClickEventsByUserAndExperiment() {
        List<EventCount> eventCounts = eventCountRepository.findAllClickEventsByUserAndExperiment(user1.getId(),
                experiment1.getId());
        assertAll(
                () -> assertEquals(2, eventCounts.size()),
                () -> assertEquals(clickEvent1.getEvent().toString(), eventCounts.get(0).getEvent()),
                () -> assertEquals(1, eventCounts.get(0).getCount()),
                () -> assertEquals(clickEvent2.getEvent().toString(), eventCounts.get(1).getEvent()),
                () -> assertEquals(2, eventCounts.get(1).getCount())
        );
    }

    @Test
    public void testFindAllClickEventsByUserAndExperimentNoEntries() {
        List<EventCount> eventCounts = eventCountRepository.findAllClickEventsByUserAndExperiment(user2.getId(),
                experiment2.getId());
        assertTrue(eventCounts.isEmpty());
    }

    @Test
    public void testFindAllClickEventsByExperiment() {
        List<EventCount> eventCounts = eventCountRepository.findAllClickEventsByExperiment(experiment1.getId());
        assertEquals(3, eventCounts.size());
    }

    @Test
    public void testFindAllResourceEventsByUserIdAndExperimentId() {
        List<EventCount> eventCounts = eventCountRepository.findAllResourceEventsByUserIdAndExperimentId(user1.getId(),
                experiment1.getId());
        assertAll(
                () -> assertEquals(2, eventCounts.size()),
                () -> assertEquals(resourceEvent1.getEvent().toString(), eventCounts.get(0).getEvent()),
                () -> assertEquals(2, eventCounts.get(0).getCount()),
                () -> assertEquals(resourceEvent2.getEvent().toString(), eventCounts.get(1).getEvent()),
                () -> assertEquals(1, eventCounts.get(1).getCount())
        );
    }

    @Test
    public void testFindAllResourceEventsByUserIdAndExperimentIdNoEntries() {
        List<EventCount> eventCounts = eventCountRepository.findAllResourceEventsByUserIdAndExperimentId(user2.getId(),
                experiment2.getId());
        assertTrue(eventCounts.isEmpty());
    }

    @Test
    public void testFindAllResourceEventsByExperiment() {
        List<EventCount> eventCounts = eventCountRepository.findAllResourceEventsByExperiment(experiment1.getId());
        assertEquals(3, eventCounts.size());
    }
}
