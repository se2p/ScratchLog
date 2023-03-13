package fim.unipassau.de.scratch1984.persistence;

import fim.unipassau.de.scratch1984.persistence.entity.BlockEvent;
import fim.unipassau.de.scratch1984.persistence.entity.ClickEvent;
import fim.unipassau.de.scratch1984.persistence.entity.EventCount;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.ResourceEvent;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.EventCountRepository;
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
    private User user1 = new User("participant1", "part1@part.de", "PARTICIPANT", "GERMAN", "password", "secret1");
    private User user2 = new User("participant2", "part2@part.de", "PARTICIPANT", "GERMAN", "password", "secret2");
    private Experiment experiment1 = new Experiment(null, "experiment1", "description", "info", "postscript", true,
            false, GUI_URL);
    private Experiment experiment2 = new Experiment(null, "experiment2", "description", "info", "postscript", true,
            false, GUI_URL);
    private BlockEvent blockEvent1 = new BlockEvent(user1, experiment1, date, "CREATE", "CREATE", "Figur1",
            null, "xml", "json");
    private BlockEvent blockEvent2 = new BlockEvent(user1, experiment1, date, "DRAG", "ENDDRAG", "Figur1",
            null, null, null);
    private BlockEvent blockEvent3 = new BlockEvent(user1, experiment2, date, "MOVE", "MOVE", "Figur1",
            null, null, null);
    private BlockEvent blockEvent4 = new BlockEvent(user2, experiment1, date, "CLICK", "GREENFLAG", "Figur1",
            null, null, null);
    private BlockEvent blockEvent5 = new BlockEvent(user1, experiment1, date, "DRAG", "ENDDRAG", "Figur1",
            null, null, null);
    private ClickEvent clickEvent1 = new ClickEvent(user1, experiment1, date, "ICON", "GREENFLAG", "");
    private ClickEvent clickEvent2 = new ClickEvent(user1, experiment1, date, "CODE", "STACKCLICK", "");
    private ClickEvent clickEvent3 = new ClickEvent(user1, experiment1, date, "CODE", "STACKCLICK", "");
    private ClickEvent clickEvent4 = new ClickEvent(user1, experiment2, date, "CODE", "STACKCLICK", "");
    private ClickEvent clickEvent5 = new ClickEvent(user2, experiment1, date, "ICON", "GREENFLAG", "");
    private ResourceEvent resourceEvent1 = new ResourceEvent(user1, experiment1, date, "ADD", "ADD_SOUND", "Miau",
            "hash", "wav", 0);
    private ResourceEvent resourceEvent2 = new ResourceEvent(user1, experiment1, date, "DELETE", "DELETE_SOUND",
            "Miau", "hash", "wav", null);
    private ResourceEvent resourceEvent3 = new ResourceEvent(user1, experiment1, date, "ADD", "ADD_SOUND", "Miau2",
            "hash", "wav", 0);
    private ResourceEvent resourceEvent4 = new ResourceEvent(user1, experiment2, date, "ADD", "ADD_COSTUME", "image",
            "hash", "jpg", 0);
    private ResourceEvent resourceEvent5 = new ResourceEvent(user2, experiment1, date, "ADD", "ADD_COSTUME", "image",
            "hash", "jpg", 0);

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
                () -> assertEquals(blockEvent1.getEvent(), eventCounts.get(0).getEvent()),
                () -> assertEquals(1, eventCounts.get(0).getCount()),
                () -> assertEquals(blockEvent2.getEvent(), eventCounts.get(1).getEvent()),
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
                () -> assertEquals(clickEvent1.getEvent(), eventCounts.get(0).getEvent()),
                () -> assertEquals(1, eventCounts.get(0).getCount()),
                () -> assertEquals(clickEvent2.getEvent(), eventCounts.get(1).getEvent()),
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
                () -> assertEquals(resourceEvent1.getEvent(), eventCounts.get(0).getEvent()),
                () -> assertEquals(2, eventCounts.get(0).getCount()),
                () -> assertEquals(resourceEvent2.getEvent(), eventCounts.get(1).getEvent()),
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
