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
import de.uni_passau.fim.se2.scratchlog.persistence.entity.BlockEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.ClickEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.EventCount;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.ResourceEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.EventCountRepository;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.ResourceEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ResourceEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventCountRepositoryTest extends AbstractScratchLogTest {

    @Autowired
    private EventCountRepository eventCountRepository;

    private User user1;
    private User user2;
    private Experiment experiment1;
    private Experiment experiment2;

    private BlockEvent blockEvent1;
    private BlockEvent blockEvent2;

    private ClickEvent clickEvent1;
    private ClickEvent clickEvent2;

    private ResourceEvent resourceEvent1;
    private ResourceEvent resourceEvent2;

    @BeforeEach
    void setup() {
        user1 = entityUtilService.generateUser("participant1");
        user2 = entityUtilService.generateUser("participant2");
        experiment1 = entityUtilService.generateExperiment("experiment1");
        experiment2 = entityUtilService.generateExperiment("experiment2");

        blockEvent1 = eventUtilService.generateBlockEvent(user1, experiment1, BlockEventType.CREATE, BlockEventSpecific.CREATE);
        blockEvent2 = eventUtilService.generateBlockEvent(user1, experiment1, BlockEventType.DRAG, BlockEventSpecific.ENDDRAG);
        eventUtilService.generateBlockEvent(user1, experiment2, BlockEventType.MOVE, BlockEventSpecific.MOVE);
        eventUtilService.generateBlockEvent(user2, experiment1, BlockEventType.CLICK, BlockEventSpecific.GREENFLAG);
        eventUtilService.generateBlockEvent(user1, experiment1, BlockEventType.DRAG, BlockEventSpecific.ENDDRAG);

        clickEvent1 = eventUtilService.generateClickEvent(user1, experiment1, ClickEventType.ICON, ClickEventSpecific.GREENFLAG);
        clickEvent2 = eventUtilService.generateClickEvent(user1, experiment1, ClickEventType.CODE, ClickEventSpecific.STACKCLICK);
        eventUtilService.generateClickEvent(user1, experiment1, ClickEventType.CODE, ClickEventSpecific.STACKCLICK);
        eventUtilService.generateClickEvent(user1, experiment2, ClickEventType.CODE, ClickEventSpecific.STACKCLICK);
        eventUtilService.generateClickEvent(user2, experiment1, ClickEventType.ICON, ClickEventSpecific.GREENFLAG);

        resourceEvent1 = eventUtilService.generateResourceEvent(user1, experiment1, ResourceEventType.ADD, ResourceEventSpecific.ADD_SOUND);
        resourceEvent2 = eventUtilService.generateResourceEvent(user1, experiment1, ResourceEventType.DELETE, ResourceEventSpecific.DELETE_SOUND);
        eventUtilService.generateResourceEvent(user1, experiment1, ResourceEventType.ADD, ResourceEventSpecific.ADD_SOUND);
        eventUtilService.generateResourceEvent(user1, experiment2, ResourceEventType.ADD, ResourceEventSpecific.ADD_COSTUME);
        eventUtilService.generateResourceEvent(user2, experiment1, ResourceEventType.ADD, ResourceEventSpecific.ADD_COSTUME);
    }

    @Test
    void testFindAllBlockEventsByUserAndExperiment() {
        List<EventCount> eventCounts = eventCountRepository.findAllBlockEventsByUserAndExperiment(user1.getId(),
                experiment1.getId());
        assertAll(
                () -> assertEquals(2, eventCounts.size()),
                () -> assertEquals(blockEvent1.getEvent().toString(), eventCounts.getFirst().getEvent()),
                () -> assertEquals(1, eventCounts.getFirst().getCount()),
                () -> assertEquals(blockEvent2.getEvent().toString(), eventCounts.get(1).getEvent()),
                () -> assertEquals(2, eventCounts.get(1).getCount())
        );
    }

    @Test
    void testFindAllBlockEventsByUserAndExperimentNoEntries() {
        List<EventCount> eventCounts = eventCountRepository.findAllBlockEventsByUserAndExperiment(user2.getId(),
                experiment2.getId());
        assertTrue(eventCounts.isEmpty());
    }

    @Test
    void testFindBlockEventCountByUserAndExperiment() {
        Optional<EventCount> counts = eventCountRepository.findBlockEventCountByUserAndExperiment(user1.getId(),
                experiment1.getId(), BlockEventSpecific.ENDDRAG.toString());
        assertAll(
                () -> assertTrue(counts.isPresent()),
                () -> assertEquals(2, counts.get().getCount())
        );
    }

    @Test
    void testFindBlockEventCountByUserAndExperimentNoEvents() {
        Optional<EventCount> counts = eventCountRepository.findBlockEventCountByUserAndExperiment(user1.getId(),
                experiment1.getId(), BlockEventSpecific.DELETE.toString());
        assertTrue(counts.isEmpty());
    }

    @Test
    void testFindAllClickEventsByUserAndExperiment() {
        List<EventCount> eventCounts = eventCountRepository.findAllClickEventsByUserAndExperiment(user1.getId(),
                experiment1.getId());
        assertAll(
                () -> assertEquals(2, eventCounts.size()),
                () -> assertEquals(clickEvent1.getEvent().toString(), eventCounts.getFirst().getEvent()),
                () -> assertEquals(1, eventCounts.getFirst().getCount()),
                () -> assertEquals(clickEvent2.getEvent().toString(), eventCounts.get(1).getEvent()),
                () -> assertEquals(2, eventCounts.get(1).getCount())
        );
    }

    @Test
    void testFindAllClickEventsByUserAndExperimentNoEntries() {
        List<EventCount> eventCounts = eventCountRepository.findAllClickEventsByUserAndExperiment(user2.getId(),
                experiment2.getId());
        assertTrue(eventCounts.isEmpty());
    }

    @Test
    void testFindClickEventCountByUserAndExperiment() {
        Optional<EventCount> counts = eventCountRepository.findClickEventCountByUserAndExperiment(user1.getId(),
                experiment1.getId(), ClickEventSpecific.GREENFLAG.toString());
        assertAll(
                () -> assertTrue(counts.isPresent()),
                () -> assertEquals(1, counts.get().getCount())
        );
    }

    @Test
    void testFindAllResourceEventsByUserIdAndExperimentId() {
        List<EventCount> eventCounts = eventCountRepository.findAllResourceEventsByUserIdAndExperimentId(user1.getId(),
                experiment1.getId());
        assertAll(
                () -> assertEquals(2, eventCounts.size()),
                () -> assertEquals(resourceEvent1.getEvent().toString(), eventCounts.getFirst().getEvent()),
                () -> assertEquals(2, eventCounts.getFirst().getCount()),
                () -> assertEquals(resourceEvent2.getEvent().toString(), eventCounts.get(1).getEvent()),
                () -> assertEquals(1, eventCounts.get(1).getCount())
        );
    }

    @Test
    void testFindAllResourceEventsByUserIdAndExperimentIdNoEntries() {
        List<EventCount> eventCounts = eventCountRepository.findAllResourceEventsByUserIdAndExperimentId(user2.getId(),
                experiment2.getId());
        assertTrue(eventCounts.isEmpty());
    }
}
