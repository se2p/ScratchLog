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

package de.uni_passau.fim.se2.scratchlog.application;

import de.uni_passau.fim.se2.scratchlog.AbstractScratchLogTest;
import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.service.ExperimentDataService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.BlockEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.ClickEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.JsonEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Participant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.ResourceEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.BlockEventJSONProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.BlockEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ClickEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.JsonEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ResourceEventRepository;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.JsonEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.JsonEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import de.uni_passau.fim.se2.scratchlog.util.enums.ResourceEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ResourceEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExperimentDataServiceTest extends AbstractScratchLogTest {

    @Autowired
    private EntityManager entityManager;

    private ExperimentDataService experimentDataService;

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private BlockEventRepository blockEventRepository;

    @Mock
    private ClickEventRepository clickEventRepository;

    @Mock
    private ResourceEventRepository resourceEventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private JsonEventRepository jsonEventRepository;

    private static final int ID = 1;
    private final User user = new User("participant", "email", Role.PARTICIPANT, Language.GERMAN, "password", "secret");
    private final Experiment experiment = new Experiment(ID, "title", "description", "info", "postscript", true,
        false, "url");
    private static final String[] EVENT_DATA_HEADER = {"id", "user", "username", "experiment", "date", "eventType",
        "event", "spritename", "metadata", "xml", "json", "name", "md5", "filetype", "library", "content", "table"};
    private static final String[] ISSUE_HEADER = {"user", "issue id", "finder name", "translated finder name",
        "issue type", "severity", "actor name", "location", "hint", "costumes", "current costumes", "json",
        "timestamp"};
    private final Participant participant = new Participant(user, experiment, null, null);
    private final Stream<BlockEvent> blockEventData = getBlockEvents(3);
    private final Stream<ClickEvent> clickEventData = getClickEvents(2);
    private final Stream<ResourceEvent> resourceEventData = getResourceEvents(2);
    private final Stream<JsonEvent> jsonEventData = getJsonEvents(2);
    private final List<Participant> participants = List.of(participant, participant);

    @BeforeEach
    void setup() {
        experimentDataService = new ExperimentDataService(
            entityManager, blockEventRepository, clickEventRepository, resourceEventRepository,
            experimentRepository, participantRepository, jsonEventRepository
        );
        user.setId(ID);
    }

    @Test
    void testGetEventData() throws IOException {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByExperiment(experiment)).thenReturn(blockEventData);
        when(clickEventRepository.findAllByExperiment(experiment)).thenReturn(clickEventData);
        when(resourceEventRepository.findAllByExperiment(experiment)).thenReturn(resourceEventData);
        when(jsonEventRepository.findAllByExperiment(experiment)).thenReturn(jsonEventData);

        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            experimentDataService.getEventDataCsv(ID, pw);
            List<String> events = sw.toString().lines().toList();
            assertAll(
                () -> assertEquals(10, events.size()),
                () -> assertEquals(
                    Arrays.stream(EVENT_DATA_HEADER).map(s -> "\"" + s + "\"").collect(Collectors.joining(",")),
                    String.join(",", events.getFirst())
                )
            );
        }

        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByExperiment(experiment);
        verify(clickEventRepository).findAllByExperiment(experiment);
        verify(resourceEventRepository).findAllByExperiment(experiment);
        verify(jsonEventRepository).findAllByExperiment(experiment);
    }

    @Test
    public void testGetAnalyzedProgramDataCount() throws URISyntaxException, IOException {
        URI json = getClass().getClassLoader().getResource("json.txt").toURI();
        String jsonCode = Files.readString(Paths.get(json));
        BlockEventJSONProjection projection = new BlockEventJSONProjection() {
            @Override
            public Integer getId() {
                return 1;
            }

            @Override
            public String getCode() {
                return jsonCode;
            }

            @Override
            public LocalDateTime getDate() {
                return LocalDateTime.now();
            }

            @Override
            public String getEvent() {
                return "CREATE";
            }
        };
        List<BlockEventJSONProjection> blockEventJSONProjections = List.of(projection);
        assertEquals(3, experimentDataService.getAnalyzedProgramDataCount(blockEventJSONProjections).size());
    }

    @Test
    public void testGetAnalyzedProgramDataCountParsingError() {
        BlockEventJSONProjection projection = new BlockEventJSONProjection() {
            @Override
            public Integer getId() {
                return 1;
            }

            @Override
            public String getCode() {
                return "json";
            }

            @Override
            public LocalDateTime getDate() {
                return LocalDateTime.now();
            }

            @Override
            public String getEvent() {
                return "CREATE";
            }
        };
        List<BlockEventJSONProjection> blockEventJSONProjections = List.of(projection);
        assertThrows(RuntimeException.class,
            () -> experimentDataService.getAnalyzedProgramDataCount(blockEventJSONProjections)
        );
    }

    @Test
    public void testGetAnalyzedProgramDataCountNoJsons() {
        List<BlockEventJSONProjection> blockEventJSONProjections = new ArrayList<>();
        assertThrows(IllegalArgumentException.class,
            () -> experimentDataService.getAnalyzedProgramDataCount(blockEventJSONProjections)
        );
    }

    @Test
    public void testGetLitterBoxAnalysisResults() throws IOException, URISyntaxException {
        URI json = getClass().getClassLoader().getResource("json.txt").toURI();
        String jsonCode = Files.readString(Paths.get(json));
        BlockEventJSONProjection projection = new BlockEventJSONProjection() {
            @Override
            public Integer getId() {
                return 1;
            }

            @Override
            public String getCode() {
                return jsonCode;
            }

            @Override
            public LocalDateTime getDate() {
                return LocalDateTime.now();
            }

            @Override
            public String getEvent() {
                return "CREATE";
            }
        };
        List<BlockEventJSONProjection> blockEventJSONProjections = List.of(projection);
        when(experimentRepository.findById(ID)).thenReturn(Optional.of(experiment));
        when(participantRepository.findAllByExperiment(experiment)).thenReturn(participants);
        when(blockEventRepository.findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user,
            experiment)).thenReturn(blockEventJSONProjections);
        List<String[]> results = experimentDataService.getLitterBoxAnalysisResults(ID);
        assertAll(
            () -> assertFalse(results.isEmpty()),
            () -> assertEquals(Arrays.toString(ISSUE_HEADER), Arrays.toString(results.getFirst()))
        );
        verify(experimentRepository).findById(ID);
        verify(participantRepository).findAllByExperiment(experiment);
        verify(blockEventRepository, times(2)).findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user,
            experiment);
    }

    @Test
    public void testGetLitterBoxAnalysisResultsParsingError() {
        BlockEventJSONProjection projection = new BlockEventJSONProjection() {
            @Override
            public Integer getId() {
                return 1;
            }

            @Override
            public String getCode() {
                return "json";
            }

            @Override
            public LocalDateTime getDate() {
                return LocalDateTime.now();
            }

            @Override
            public String getEvent() {
                return "CREATE";
            }
        };
        List<BlockEventJSONProjection> blockEventJSONProjections = List.of(projection);
        when(experimentRepository.findById(ID)).thenReturn(Optional.of(experiment));
        when(participantRepository.findAllByExperiment(experiment)).thenReturn(participants);
        when(blockEventRepository.findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user,
            experiment)).thenReturn(blockEventJSONProjections);
        assertThrows(RuntimeException.class,
            () -> experimentDataService.getLitterBoxAnalysisResults(ID)
        );
        verify(experimentRepository).findById(ID);
        verify(participantRepository).findAllByExperiment(experiment);
        verify(blockEventRepository).findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user, experiment);
    }

    @Test
    public void testGetLitterBoxAnalysisResultsExperimentNotFound() {
        when(experimentRepository.findById(ID)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> experimentDataService.getLitterBoxAnalysisResults(ID)
        );
        verify(experimentRepository).findById(ID);
        verify(participantRepository, never()).findAllByExperiment(experiment);
        verify(blockEventRepository, never()).findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(any(), any());
    }

    private Stream<BlockEvent> getBlockEvents(int number) {
        List<BlockEvent> events = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            BlockEvent blockEvent = new BlockEvent(user, experiment, LocalDateTime.now(), BlockEventType.CLICK,
                BlockEventSpecific.STOPALL, "sprite", "meta", "xml" + i, "json.txt" + i);
            blockEvent.setId(i);
            events.add(blockEvent);
        }
        return events.stream();
    }

    private Stream<ClickEvent> getClickEvents(int number) {
        List<ClickEvent> events = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            ClickEvent clickEvent = new ClickEvent(user, experiment, LocalDateTime.now(),
                ClickEventType.BUTTON, ClickEventSpecific.CLOSE_DEBUGGER, "meta");
            clickEvent.setId(i);
            events.add(clickEvent);
        }
        return events.stream();
    }

    private Stream<ResourceEvent> getResourceEvents(int number) {
        List<ResourceEvent> events = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            ResourceEvent resourceEvent = new ResourceEvent(user, experiment, LocalDateTime.now(),
                ResourceEventType.ADD, ResourceEventSpecific.ADD_SOUND, "name", "hash", "type", i == 0 ? 1 : null);
            resourceEvent.setId(i);
            events.add(resourceEvent);
        }
        return events.stream();
    }

    private Stream<JsonEvent> getJsonEvents(int number) {
        List<JsonEvent> events = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            JsonEvent jsonEvent = new JsonEvent(user, experiment, LocalDateTime.now(),
                JsonEventType.LITTERBOX, JsonEventSpecific.LLM, "name", "content");
            jsonEvent.setId(i);
            events.add(jsonEvent);
        }
        return events.stream();
    }

}
