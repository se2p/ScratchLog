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

package fim.unipassau.de.scratchLog.application;

import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.application.service.CodeService;
import fim.unipassau.de.scratchLog.persistence.entity.BlockEvent;
import fim.unipassau.de.scratchLog.persistence.entity.Experiment;
import fim.unipassau.de.scratchLog.persistence.entity.Participant;
import fim.unipassau.de.scratchLog.persistence.entity.User;
import fim.unipassau.de.scratchLog.persistence.projection.BlockEventJSONProjection;
import fim.unipassau.de.scratchLog.persistence.projection.BlockEventProjection;
import fim.unipassau.de.scratchLog.persistence.projection.BlockEventXMLProjection;
import fim.unipassau.de.scratchLog.persistence.repository.BlockEventRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratchLog.persistence.repository.UserRepository;
import fim.unipassau.de.scratchLog.util.Constants;
import fim.unipassau.de.scratchLog.util.enums.BlockEventSpecific;
import fim.unipassau.de.scratchLog.util.enums.BlockEventType;
import fim.unipassau.de.scratchLog.util.enums.Language;
import fim.unipassau.de.scratchLog.util.enums.Role;
import fim.unipassau.de.scratchLog.web.dto.Sb3ZipDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CodeServiceTest {

    @InjectMocks
    private CodeService codeService;

    @Mock
    private BlockEventRepository blockEventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExperimentRepository experimentRepository;

    private static final int ID = 1;
    private final User user = new User("participant", "email", Role.PARTICIPANT, Language.GERMAN, "password", "secret");
    private final Experiment experiment = new Experiment(ID, "title", "description", "info", "postscript", true,
            false, "scratch");
    private final Participant participant = new Participant(user, experiment, LocalDateTime.now(), null);
    private final BlockEvent blockEvent = new BlockEvent(user, experiment, LocalDateTime.now(), BlockEventType.CREATE,
            BlockEventSpecific.CREATE, "sprite", "", "xml", "json.txt");
    private final Sb3ZipDTO sb3ZipDTO = new Sb3ZipDTO(ID, ID, "secret", "file", new byte[]{1, 2, 3},
            LocalDateTime.now().plusMinutes(12));
    private final List<BlockEventJSONProjection> jsonProjections = getJsonProjections(2);
    private final List<BlockEventXMLProjection> xmlProjections = getXmlProjections(2);
    private final Page<BlockEventProjection> blockEventProjections = new PageImpl<>(getBlockEventProjections(5));
    private final PageRequest pageRequest = PageRequest.of(0, Constants.PAGE_SIZE);
    private BlockEventJSONProjection projection = new BlockEventJSONProjection() {
        @Override
        public Integer getId() {
            return 1;
        }

        @Override
        public String getCode() {
            return "json.txt";
        }

        @Override
        public LocalDateTime getDate() {
            return LocalDateTime.now();
        }

        @Override
        public String getEvent() {
            return "event";
        }
    };

    @BeforeEach
    public void setup() {
        user.setId(ID);
        user.setActive(true);
        experiment.setActive(true);
    }

    @Test
    public void testFindJsonById() {
        when(blockEventRepository.findById(ID)).thenReturn(Optional.of(blockEvent));
        assertEquals(blockEvent.getCode(), codeService.findJsonById(ID));
        verify(blockEventRepository).findById(ID);
    }

    @Test
    public void testFindJsonByIdJsonNull() {
        blockEvent.setCode(null);
        when(blockEventRepository.findById(ID)).thenReturn(Optional.of(blockEvent));
        assertThrows(IllegalArgumentException.class,
                () -> codeService.findJsonById(ID)
        );
        verify(blockEventRepository).findById(ID);
    }

    @Test
    public void testFindJsonByIdEmpty() {
        when(blockEventRepository.findById(ID)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> codeService.findJsonById(ID)
        );
        verify(blockEventRepository).findById(ID);
    }

    @Test
    public void testFindJsonByIdInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> codeService.findJsonById(0)
        );
        verify(blockEventRepository, never()).findById(anyInt());
    }

    @Test
    public void testFindFirstJSON() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findFirstByUserAndExperimentAndCodeIsNotNullOrderByDateDesc(user, experiment))
                .thenReturn(projection);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertEquals(projection.getCode(), codeService.findFirstJSON(ID, ID));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findFirstByUserAndExperimentAndCodeIsNotNullOrderByDateDesc(user, experiment);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
    }

    @Test
    public void testFindFirstJSONProjectionNull() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertNull(codeService.findFirstJSON(ID, ID));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findFirstByUserAndExperimentAndCodeIsNotNullOrderByDateDesc(user, experiment);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
    }

    @Test
    public void testFindFirstJSONParticipantNull() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findFirstByUserAndExperimentAndCodeIsNotNullOrderByDateDesc(user, experiment))
                .thenReturn(projection);
        assertNull(codeService.findFirstJSON(ID, ID));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findFirstByUserAndExperimentAndCodeIsNotNullOrderByDateDesc(user, experiment);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
    }

    @Test
    public void testFindFirstJSONUserInactive() {
        user.setActive(false);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findFirstByUserAndExperimentAndCodeIsNotNullOrderByDateDesc(user, experiment))
                .thenReturn(projection);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertNull(codeService.findFirstJSON(ID, ID));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findFirstByUserAndExperimentAndCodeIsNotNullOrderByDateDesc(user, experiment);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
    }

    @Test
    public void testFindFirstJSONExperimentInactive() {
        experiment.setActive(false);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findFirstByUserAndExperimentAndCodeIsNotNullOrderByDateDesc(user, experiment))
                .thenReturn(projection);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(Optional.of(participant));
        assertNull(codeService.findFirstJSON(ID, ID));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findFirstByUserAndExperimentAndCodeIsNotNullOrderByDateDesc(user, experiment);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
    }

    @Test
    public void testFindFirstJSONEntityNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findFirstByUserAndExperimentAndCodeIsNotNullOrderByDateDesc(user, experiment))
                .thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> codeService.findFirstJSON(ID, ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findFirstByUserAndExperimentAndCodeIsNotNullOrderByDateDesc(user, experiment);
        verify(participantRepository, never()).findByUserAndExperiment(any(), any());
    }

    @Test
    public void testFindFirstJSONInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class,
                () -> codeService.findFirstJSON(ID, 0)
        );
        verify(userRepository, never()).getReferenceById(anyInt());
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(blockEventRepository, never()).findFirstByUserAndExperimentAndCodeIsNotNullOrderByDateDesc(any(), any());
        verify(participantRepository, never()).findByUserAndExperiment(any(), any());
    }

    @Test
    public void testFindFirstJSONInvalidUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> codeService.findFirstJSON(-1, ID)
        );
        verify(userRepository, never()).getReferenceById(anyInt());
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(blockEventRepository, never()).findFirstByUserAndExperimentAndCodeIsNotNullOrderByDateDesc(any(), any());
        verify(participantRepository, never()).findByUserAndExperiment(any(), any());
    }

    @Test
    public void testGetJsonForUser() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user,
                experiment)).thenReturn(jsonProjections);
        List<BlockEventJSONProjection> projections = codeService.getJsonForUser(ID, ID);
        assertAll(
                () -> assertEquals(2, projections.size()),
                () -> assertEquals(jsonProjections, projections),
                () -> assertEquals(0, projections.get(0).getId()),
                () -> assertEquals("json0", projections.get(0).getCode()),
                () -> assertEquals(1, projections.get(1).getId()),
                () -> assertEquals("json1", projections.get(1).getCode())
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user, experiment);
    }

    @Test
    public void testGetJsonForUserEntityNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user,
                experiment)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> codeService.getJsonForUser(ID, ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user, experiment);
    }

    @Test
    public void testGetJsonForUserNoEntry() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        assertThrows(NotFoundException.class,
                () -> codeService.getJsonForUser(ID, ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user, experiment);
    }

    @Test
    public void testGetJsonForUserInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class,
                () -> codeService.getJsonForUser(ID, 0)
        );
        verify(userRepository, never()).getReferenceById(anyInt());
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(blockEventRepository, never()).findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(any(), any());
    }

    @Test
    public void testGetJsonForUserInvalidUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> codeService.getJsonForUser(-1, ID)
        );
        verify(userRepository, never()).getReferenceById(anyInt());
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(blockEventRepository, never()).findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(any(), any());
    }

    @Test
    public void testGetFilteredJsons() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user,
                experiment)).thenReturn(jsonProjections);
        assertEquals(jsonProjections, codeService.getFilteredJsons(ID, ID, 0, 0, 0, Optional.empty()));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user, experiment);
    }

    @Test
    public void testGetFilteredJsonsMaxAllowedTime() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user,
                experiment)).thenReturn(getJsonProjectionsWithCustomTimeDifference());
        List<BlockEventJSONProjection> filteredProjections = codeService.getFilteredJsons(ID, ID, ID, 0, 0,
                Optional.of(sb3ZipDTO));
        assertAll(
                () -> assertEquals(5, filteredProjections.size()),
                () -> assertEquals(1, filteredProjections.get(0).getId()),
                () -> assertEquals(2, filteredProjections.get(1).getId()),
                () -> assertEquals(3, filteredProjections.get(2).getId()),
                () -> assertEquals(3, filteredProjections.get(3).getId()),
                () -> assertEquals(4, filteredProjections.get(4).getId())
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user, experiment);
    }

    @Test
    public void testGetFilteredJsonsNoFinalProject() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user,
                experiment)).thenReturn(jsonProjections);
        List<BlockEventJSONProjection> filteredProjections = codeService.getFilteredJsons(ID, ID, ID, 0, 0,
                Optional.empty());
        assertAll(
                () -> assertEquals(2, filteredProjections.size()),
                () -> assertEquals(0, filteredProjections.get(0).getId()),
                () -> assertEquals(1, filteredProjections.get(1).getId())
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user, experiment);
    }

    @Test
    public void testGetFilteredJsonsSingleJson() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user,
                experiment)).thenReturn(getJsonProjections(1));
        List<BlockEventJSONProjection> filteredProjections = codeService.getFilteredJsons(ID, ID, ID, 0, 0,
                Optional.empty());
        assertAll(
                () -> assertEquals(1, filteredProjections.size()),
                () -> assertEquals(0, filteredProjections.get(0).getId())
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user, experiment);
    }

    @Test
    public void testGetFilteredJsonsStartStop() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user,
                experiment)).thenReturn(getJsonProjections(5));
        List<BlockEventJSONProjection> filteredProjections = codeService.getFilteredJsons(ID, ID, 0, 2, 3,
                Optional.empty());
        assertAll(
                () -> assertEquals(2, filteredProjections.size()),
                () -> assertEquals(1, filteredProjections.get(0).getId()),
                () -> assertEquals(2, filteredProjections.get(1).getId())
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user, experiment);
    }

    @Test
    public void testGetFilteredJsonsStartStopInvalidEnd() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user,
                experiment)).thenReturn(jsonProjections);
        assertThrows(IllegalArgumentException.class,
                () -> codeService.getFilteredJsons(ID, ID, 0, 1, 5, Optional.empty())
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user, experiment);
    }

    @Test
    public void testGetFilteredJsonsStartStopStartEqualsEnd() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user,
                experiment)).thenReturn(jsonProjections);
        assertThrows(IllegalArgumentException.class,
                () -> codeService.getFilteredJsons(ID, ID, 0, 1, 0, Optional.empty())
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user, experiment);
    }

    @Test
    public void testGetFilteredJsonsNoJson() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        assertTrue(codeService.getFilteredJsons(ID, ID, 0, 1, 0, Optional.empty()).isEmpty());
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(user, experiment);
    }

    @Test
    public void testGetXMLForUser() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByXmlIsNotNullAndUserAndExperiment(user,
                experiment)).thenReturn(xmlProjections);
        List<BlockEventXMLProjection> projections = codeService.getXMLForUser(ID, ID);
        assertAll(
                () -> assertEquals(2, projections.size()),
                () -> assertEquals(xmlProjections, projections),
                () -> assertEquals(0, projections.get(0).getId()),
                () -> assertEquals("xml0", projections.get(0).getXml()),
                () -> assertEquals(1, projections.get(1).getId()),
                () -> assertEquals("xml1", projections.get(1).getXml())
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByXmlIsNotNullAndUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetXMLForUserEntityNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByXmlIsNotNullAndUserAndExperiment(user,
                experiment)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> codeService.getXMLForUser(ID, ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByXmlIsNotNullAndUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetXMLForUserNoEntry() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        assertThrows(NotFoundException.class,
                () -> codeService.getXMLForUser(ID, ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByXmlIsNotNullAndUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetXMLForUserInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class,
                () -> codeService.getXMLForUser(ID, -5)
        );
        verify(userRepository, never()).getReferenceById(anyInt());
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(blockEventRepository, never()).findAllByXmlIsNotNullAndUserAndExperiment(any(), any());
    }

    @Test
    public void testGetXMLForUserInvalidUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> codeService.getXMLForUser(0, ID)
        );
        verify(userRepository, never()).getReferenceById(anyInt());
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(blockEventRepository, never()).findAllByXmlIsNotNullAndUserAndExperiment(any(), any());
    }

    @Test
    public void testGetCodesForUser() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByUserAndExperimentAndXmlIsNotNull(any(), any(),
                any(PageRequest.class))).thenReturn(blockEventProjections);
        Page<BlockEventProjection> page = codeService.getCodesForUser(ID, ID, pageRequest);
        assertAll(
                () -> assertEquals(blockEventProjections.getTotalElements(), page.getTotalElements()),
                () -> assertEquals(blockEventProjections.stream().findFirst(), page.stream().findFirst()),
                () -> assertEquals(blockEventProjections.getSize(), page.getSize())
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByUserAndExperimentAndXmlIsNotNull(any(), any(), any(PageRequest.class));
    }

    @Test
    public void testGetCodesForUserEntityNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByUserAndExperimentAndXmlIsNotNull(any(), any(),
                any(PageRequest.class))).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> codeService.getCodesForUser(ID, ID, pageRequest)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByUserAndExperimentAndXmlIsNotNull(any(), any(), any(PageRequest.class));
    }

    @Test
    public void testGetCodesForUserInvalidPageSize() {
        PageRequest invalid = PageRequest.of(0, Constants.PAGE_SIZE + 2);
        assertThrows(IllegalArgumentException.class,
                () -> codeService.getCodesForUser(ID, ID, invalid)
        );
        verify(userRepository, never()).getReferenceById(anyInt());
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(blockEventRepository, never()).findAllByUserAndExperimentAndXmlIsNotNull(any(), any(),
                any(PageRequest.class));
    }

    @Test
    public void testGetCodesForUserInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class,
                () -> codeService.getCodesForUser(ID, 0, pageRequest)
        );
        verify(userRepository, never()).getReferenceById(anyInt());
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(blockEventRepository, never()).findAllByUserAndExperimentAndXmlIsNotNull(any(), any(),
                any(PageRequest.class));
    }

    @Test
    public void testGetCodesForUserInvalidUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> codeService.getCodesForUser(-1, ID, pageRequest)
        );
        verify(userRepository, never()).getReferenceById(anyInt());
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(blockEventRepository, never()).findAllByUserAndExperimentAndXmlIsNotNull(any(), any(),
                any(PageRequest.class));
    }

    private List<BlockEventJSONProjection> getJsonProjections(int number) {
        List<BlockEventJSONProjection> projections = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            final int id = i;
            projections.add(new BlockEventJSONProjection() {
                @Override
                public Integer getId() {
                    return id;
                }

                @Override
                public String getCode() {
                    return "json" + id;
                }

                @Override
                public LocalDateTime getDate() {
                    return LocalDateTime.now();
                }

                @Override
                public String getEvent() {
                    return "event";
                }
            });
        }
        return projections;
    }

    private List<BlockEventJSONProjection> getJsonProjectionsWithCustomTimeDifference() {
        List<BlockEventJSONProjection> projections = new ArrayList<>();

        projections.add(new BlockEventJSONProjection() {
            @Override
            public Integer getId() {
                return 1;
            }

            @Override
            public String getCode() {
                return "json" + 1;
            }

            @Override
            public LocalDateTime getDate() {
                return LocalDateTime.now();
            }

            @Override
            public String getEvent() {
                return "event";
            }
        });

        projections.add(new BlockEventJSONProjection() {
            @Override
            public Integer getId() {
                return 2;
            }

            @Override
            public String getCode() {
                return "json" + 2;
            }

            @Override
            public LocalDateTime getDate() {
                return LocalDateTime.now().plusMinutes(Constants.MAX_ALLOWED_BREAK_FACTOR + 1);
            }

            @Override
            public String getEvent() {
                return "event";
            }
        });

        projections.add(new BlockEventJSONProjection() {
            @Override
            public Integer getId() {
                return 3;
            }

            @Override
            public String getCode() {
                return "json" + 3;
            }

            @Override
            public LocalDateTime getDate() {
                return LocalDateTime.now().plusMinutes(Constants.MAX_ALLOWED_BREAK_FACTOR
                        + Constants.MAX_ALLOWED_BREAK_FACTOR + 1);
            }

            @Override
            public String getEvent() {
                return "event";
            }
        });

        projections.add(new BlockEventJSONProjection() {
            @Override
            public Integer getId() {
                return 4;
            }

            @Override
            public String getCode() {
                return "json" + 4;
            }

            @Override
            public LocalDateTime getDate() {
                return LocalDateTime.now().plusMinutes(Constants.MAX_ALLOWED_BREAK_FACTOR
                        + Constants.MAX_ALLOWED_BREAK_FACTOR + 2);
            }

            @Override
            public String getEvent() {
                return "event";
            }
        });

        return projections;
    }


    private List<BlockEventXMLProjection> getXmlProjections(int number) {
        List<BlockEventXMLProjection> projections = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            final int id = i;
            projections.add(new BlockEventXMLProjection() {
                @Override
                public Integer getId() {
                    return id;
                }

                @Override
                public String getXml() {
                    return "xml" + id;
                }
            });
        }
        return projections;
    }


    private List<BlockEventProjection> getBlockEventProjections(int number) {
        List<BlockEventProjection> projections = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            final int id = i;
            projections.add(new BlockEventProjection() {
                @Override
                public Integer getId() {
                    return id;
                }

                @Override
                public String getXml() {
                    return "xml" + id;
                }

                @Override
                public String getCode() {
                    return "code" + id;
                }

                @Override
                public LocalDateTime getDate() {
                    return null;
                }

                @Override
                public String getSprite() {
                    return "sprite";
                }
            });
        }
        return projections;
    }

}
