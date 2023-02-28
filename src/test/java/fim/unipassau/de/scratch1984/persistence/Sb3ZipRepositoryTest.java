package fim.unipassau.de.scratch1984.persistence;

import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Sb3Zip;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.Sb3ZipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
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

    private final Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
    private static final String GUI_URL = "scratch";
    private User user1 = new User("participant1", "part1@part.de", "PARTICIPANT", "GERMAN", "password", "secret1");
    private User user2 = new User("participant2", "part2@part.de", "PARTICIPANT", "GERMAN", "password", "secret2");
    private Experiment experiment1 = new Experiment(null, "experiment1", "description", "info", "postscript", true,
            false, GUI_URL);
    private Experiment experiment2 = new Experiment(null, "experiment2", "description", "info", "postscript", true,
            false, GUI_URL);
    private Sb3Zip sb3Zip1 = new Sb3Zip(user1, experiment1, timestamp, "zip1", new byte[]{1, 2, 3});
    private Sb3Zip sb3Zip2 = new Sb3Zip(user1, experiment1, timestamp, "zip2", new byte[]{1, 2, 3});
    private Sb3Zip sb3Zip3 = new Sb3Zip(user1, experiment1, timestamp, "zip3", new byte[]{1, 2, 3});
    private Sb3Zip sb3Zip4 = new Sb3Zip(user2, experiment1, timestamp, "zip4", new byte[]{1, 2, 3});
    private Sb3Zip sb3Zip5 = new Sb3Zip(user1, experiment2, timestamp, "zip5", new byte[]{1, 2, 3});

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
