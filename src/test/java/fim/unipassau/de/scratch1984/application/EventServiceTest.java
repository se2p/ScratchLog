package fim.unipassau.de.scratch1984.application;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.EventService;
import fim.unipassau.de.scratch1984.persistence.entity.CodesData;
import fim.unipassau.de.scratch1984.persistence.entity.EventCount;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventJSONProjection;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventProjection;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventXMLProjection;
import fim.unipassau.de.scratch1984.persistence.repository.BlockEventRepository;
import fim.unipassau.de.scratch1984.persistence.repository.CodesDataRepository;
import fim.unipassau.de.scratch1984.persistence.repository.EventCountRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ResourceEventRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.dto.BlockEventDTO;
import fim.unipassau.de.scratch1984.web.dto.CodesDataDTO;
import fim.unipassau.de.scratch1984.web.dto.EventCountDTO;
import fim.unipassau.de.scratch1984.web.dto.ResourceEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventCountRepository eventCountRepository;

    @Mock
    private BlockEventRepository blockEventRepository;

    @Mock
    private ResourceEventRepository resourceEventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private CodesDataRepository codesDataRepository;

    private static final int ID = 1;
    private final BlockEventDTO blockEventDTO = new BlockEventDTO(1, 1, LocalDateTime.now(),
            BlockEventDTO.BlockEventType.CHANGE, BlockEventDTO.BlockEvent.CHANGE, "sprite", "meta", "xml", "json");
    private final ResourceEventDTO resourceEventDTO = new ResourceEventDTO(1, 1, LocalDateTime.now(),
            ResourceEventDTO.ResourceEventType.ADD, ResourceEventDTO.ResourceEvent.ADD_SOUND, "name", "hash",
            "filetype", ResourceEventDTO.LibraryResource.TRUE);
    private final User user = new User("participant", "email", "PARTICIPANT", "GERMAN", "password", "secret");
    private final Experiment experiment = new Experiment(ID, "title", "description", "info", "postscript", true);
    private final Participant participant = new Participant(user, experiment, Timestamp.valueOf(LocalDateTime.now()), null);
    private final CodesData codesData = new CodesData(ID, ID, 15);
    private final List<EventCount> blockEvents = getEventCounts(8, "CREATE");
    private final List<EventCount> resourceEvents = getEventCounts(3, "RENAME");
    private final List<BlockEventXMLProjection> xmlProjections = getXmlProjections(2);
    private final List<BlockEventJSONProjection> jsonProjections = getJsonProjections(2);
    private final Page<BlockEventProjection> blockEventProjections = new PageImpl<>(getBlockEventProjections(5));
    private final PageRequest pageRequest = PageRequest.of(0, Constants.PAGE_SIZE);

    @BeforeEach
    public void setup() {
        user.setId(ID);
        resourceEventDTO.setLibraryResource(ResourceEventDTO.LibraryResource.TRUE);
    }

    @Test
    public void testSaveBlockEvent() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(participant);
        assertDoesNotThrow(
                () -> eventService.saveBlockEvent(blockEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(blockEventRepository).save(any());
    }

    @Test
    public void testSaveBlockEventParticipantNull() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        assertDoesNotThrow(
                () -> eventService.saveBlockEvent(blockEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(blockEventRepository, never()).save(any());
    }

    @Test
    public void testSaveBlockEventEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertDoesNotThrow(
                () -> eventService.saveBlockEvent(blockEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(blockEventRepository, never()).save(any());
    }

    @Test
    public void testSaveBlockEventConstraintViolation() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(participant);
        when(blockEventRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertDoesNotThrow(
                () -> eventService.saveBlockEvent(blockEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(blockEventRepository).save(any());
    }

    @Test
    public void testSaveResourceEvent() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(participant);
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository).save(any());
    }

    @Test
    public void testSaveResourceEventLibraryResourceFalse() {
        resourceEventDTO.setLibraryResource(ResourceEventDTO.LibraryResource.FALSE);
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(participant);
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository).save(any());
    }

    @Test
    public void testSaveResourceEventLibraryResourceUnknown() {
        resourceEventDTO.setLibraryResource(ResourceEventDTO.LibraryResource.UNKNOWN);
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(participant);
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository).save(any());
    }

    @Test
    public void testSaveResourceEventConstraintViolation() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenReturn(participant);
        when(resourceEventRepository.save(any())).thenThrow(ConstraintViolationException.class);
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository).save(any());
    }

    @Test
    public void testSaveResourceEventEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(participantRepository.findByUserAndExperiment(user, experiment)).thenThrow(EntityNotFoundException.class);
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository, never()).save(any());
    }

    @Test
    public void testSaveResourceEventParticipantNull() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        assertDoesNotThrow(
                () -> eventService.saveResourceEvent(resourceEventDTO)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).findByUserAndExperiment(user, experiment);
        verify(resourceEventRepository, never()).save(any());
    }

    @Test
    public void testGetBlockEventCounts() {
        when(eventCountRepository.findAllBlockEventsByUserAndExperiment(ID, ID)).thenReturn(blockEvents);
        List<EventCountDTO> eventCountDTOS = eventService.getBlockEventCounts(ID, ID);
        assertAll(
                () -> assertEquals(8, eventCountDTOS.size()),
                () -> assertEquals(1, eventCountDTOS.get(0).getUser()),
                () -> assertEquals(1, eventCountDTOS.get(0).getExperiment()),
                () -> assertEquals(0, eventCountDTOS.get(0).getCount()),
                () -> assertEquals("CREATE0", eventCountDTOS.get(0).getEvent()),
                () -> assertEquals(7, eventCountDTOS.get(7).getCount()),
                () -> assertEquals("CREATE7", eventCountDTOS.get(7).getEvent())
        );
        verify(eventCountRepository).findAllBlockEventsByUserAndExperiment(ID, ID);
    }

    @Test
    public void testGetBlockEventCountsInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class, () -> eventService.getBlockEventCounts(ID, 0));
        verify(eventCountRepository, never()).findAllBlockEventsByUserAndExperiment(anyInt(), anyInt());
    }

    @Test
    public void testGetBlockEventCountsInvalidUserId() {
        assertThrows(IllegalArgumentException.class, () -> eventService.getBlockEventCounts(-1, ID));
        verify(eventCountRepository, never()).findAllBlockEventsByUserAndExperiment(anyInt(), anyInt());
    }

    @Test
    public void testGetResourceEventCounts() {
        when(eventCountRepository.findAllResourceEventsByUserIdAndExperimentId(ID, ID)).thenReturn(resourceEvents);
        List<EventCountDTO> eventCountDTOS = eventService.getResourceEventCounts(ID, ID);
        assertAll(
                () -> assertEquals(3, eventCountDTOS.size()),
                () -> assertEquals(1, eventCountDTOS.get(0).getUser()),
                () -> assertEquals(1, eventCountDTOS.get(0).getExperiment()),
                () -> assertEquals(0, eventCountDTOS.get(0).getCount()),
                () -> assertEquals("RENAME0", eventCountDTOS.get(0).getEvent()),
                () -> assertEquals(2, eventCountDTOS.get(2).getCount()),
                () -> assertEquals("RENAME2", eventCountDTOS.get(2).getEvent())
        );
        verify(eventCountRepository).findAllResourceEventsByUserIdAndExperimentId(ID, ID);
    }

    @Test
    public void testGetResourceEventCountsInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class, () -> eventService.getResourceEventCounts(ID, -1));
        verify(eventCountRepository, never()).findAllResourceEventsByUserIdAndExperimentId(anyInt(), anyInt());
    }

    @Test
    public void testGetResourceEventCountsInvalidUserId() {
        assertThrows(IllegalArgumentException.class, () -> eventService.getResourceEventCounts(0, ID));
        verify(eventCountRepository, never()).findAllResourceEventsByUserIdAndExperimentId(anyInt(), anyInt());
    }

    @Test
    public void testGetJsonForUser() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByCodeIsNotNullAndUserAndExperiment(user,
                experiment)).thenReturn(jsonProjections);
        List<BlockEventJSONProjection> projections = eventService.getJsonForUser(ID, ID);
        assertAll(
                () -> assertEquals(2, projections.size()),
                () -> assertEquals(jsonProjections, projections),
                () -> assertEquals(0, projections.get(0).getId()),
                () -> assertEquals("json0", projections.get(0).getCode()),
                () -> assertEquals(1, projections.get(1).getId()),
                () -> assertEquals("json1", projections.get(1).getCode())
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(blockEventRepository).findAllByCodeIsNotNullAndUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetJsonForUserEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByCodeIsNotNullAndUserAndExperiment(user,
                experiment)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> eventService.getJsonForUser(ID, ID)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(blockEventRepository).findAllByCodeIsNotNullAndUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetJsonForUserNoEntry() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        assertThrows(NotFoundException.class,
                () -> eventService.getJsonForUser(ID, ID)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(blockEventRepository).findAllByCodeIsNotNullAndUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetJsonForUserInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class,
                () -> eventService.getJsonForUser(ID, 0)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(blockEventRepository, never()).findAllByCodeIsNotNullAndUserAndExperiment(any(), any());
    }

    @Test
    public void testGetJsonForUserInvalidUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> eventService.getJsonForUser(-1, ID)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(blockEventRepository, never()).findAllByCodeIsNotNullAndUserAndExperiment(any(), any());
    }

    @Test
    public void testGetXMLForUser() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByXmlIsNotNullAndUserAndExperiment(user,
                experiment)).thenReturn(xmlProjections);
        List<BlockEventXMLProjection> projections = eventService.getXMLForUser(ID, ID);
        assertAll(
                () -> assertEquals(2, projections.size()),
                () -> assertEquals(xmlProjections, projections),
                () -> assertEquals(0, projections.get(0).getId()),
                () -> assertEquals("xml0", projections.get(0).getXml()),
                () -> assertEquals(1, projections.get(1).getId()),
                () -> assertEquals("xml1", projections.get(1).getXml())
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(blockEventRepository).findAllByXmlIsNotNullAndUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetXMLForUserEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByXmlIsNotNullAndUserAndExperiment(user,
                experiment)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> eventService.getXMLForUser(ID, ID)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(blockEventRepository).findAllByXmlIsNotNullAndUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetXMLForUserNoEntry() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        assertThrows(NotFoundException.class,
                () -> eventService.getXMLForUser(ID, ID)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(blockEventRepository).findAllByXmlIsNotNullAndUserAndExperiment(user, experiment);
    }

    @Test
    public void testGetXMLForUserInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class,
                () -> eventService.getXMLForUser(ID, -5)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(blockEventRepository, never()).findAllByXmlIsNotNullAndUserAndExperiment(any(), any());
    }

    @Test
    public void testGetXMLForUserInvalidUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> eventService.getXMLForUser(0, ID)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(blockEventRepository, never()).findAllByXmlIsNotNullAndUserAndExperiment(any(), any());
    }

    @Test
    public void testGetCodesForUser() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByUserAndExperimentAndXmlIsNotNull(any(), any(),
                any(PageRequest.class))).thenReturn(blockEventProjections);
        Page<BlockEventProjection> page = eventService.getCodesForUser(ID, ID, pageRequest);
        assertAll(
                () -> assertEquals(blockEventProjections.getTotalElements(), page.getTotalElements()),
                () -> assertEquals(blockEventProjections.stream().findFirst(), page.stream().findFirst()),
                () -> assertEquals(blockEventProjections.getSize(), page.getSize())
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(blockEventRepository).findAllByUserAndExperimentAndXmlIsNotNull(any(), any(), any(PageRequest.class));
    }

    @Test
    public void testGetCodesForUserEntityNotFound() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(experimentRepository.getOne(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByUserAndExperimentAndXmlIsNotNull(any(), any(),
                any(PageRequest.class))).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> eventService.getCodesForUser(ID, ID, pageRequest)
        );
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(blockEventRepository).findAllByUserAndExperimentAndXmlIsNotNull(any(), any(), any(PageRequest.class));
    }

    @Test
    public void testGetCodesForUserInvalidPageSize() {
        PageRequest invalid = PageRequest.of(0, Constants.PAGE_SIZE + 2);
        assertThrows(IllegalArgumentException.class,
                () -> eventService.getCodesForUser(ID, ID, invalid)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(blockEventRepository, never()).findAllByUserAndExperimentAndXmlIsNotNull(any(), any(),
                any(PageRequest.class));
    }

    @Test
    public void testGetCodesForUserInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class,
                () -> eventService.getCodesForUser(ID, 0, pageRequest)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(blockEventRepository, never()).findAllByUserAndExperimentAndXmlIsNotNull(any(), any(),
                any(PageRequest.class));
    }

    @Test
    public void testGetCodesForUserInvalidUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> eventService.getCodesForUser(-1, ID, pageRequest)
        );
        verify(userRepository, never()).getOne(anyInt());
        verify(experimentRepository, never()).getOne(anyInt());
        verify(blockEventRepository, never()).findAllByUserAndExperimentAndXmlIsNotNull(any(), any(),
                any(PageRequest.class));
    }

    @Test
    public void testGetCodesData() {
        when(codesDataRepository.findByUserAndExperiment(ID, ID)).thenReturn(codesData);
        CodesDataDTO codesDataDTO = eventService.getCodesData(ID, ID);
        assertAll(
                () -> assertEquals(codesData.getUser(), codesDataDTO.getUser()),
                () -> assertEquals(codesData.getExperiment(), codesDataDTO.getExperiment()),
                () -> assertEquals(codesData.getCount(), codesDataDTO.getCount())
        );
        verify(codesDataRepository).findByUserAndExperiment(ID, ID);
    }

    @Test
    public void testGetCodesDataInvalidExperimentId() {
        assertThrows(IllegalArgumentException.class, () -> eventService.getCodesData(ID, -1));
        verify(codesDataRepository, never()).findByUserAndExperiment(anyInt(), anyInt());
    }

    @Test
    public void testGetCodesDataInvalidUserId() {
        assertThrows(IllegalArgumentException.class, () -> eventService.getCodesData(0, ID));
        verify(codesDataRepository, never()).findByUserAndExperiment(anyInt(), anyInt());
    }

    private List<EventCount> getEventCounts(int number, String event) {
        List<EventCount> eventCounts = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            eventCounts.add(new EventCount(1, 1, i, event + i));
        }
        return eventCounts;
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
            });
        }
        return projections;
    }
}
