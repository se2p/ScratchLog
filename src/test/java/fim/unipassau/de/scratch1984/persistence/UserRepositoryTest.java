package fim.unipassau.de.scratch1984.persistence;

import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_PARTICIPANT = "PARTICIPANT";
    private static final String LANGUAGE = "ENGLISH";
    private static final String ADMIN1 = "admin1";
    private static final String USERNAME_SEARCH = "user";
    private static final String EMAIL_SEARCH = "test";
    private User user1 = new User(ADMIN1, "admin1@test.de", ROLE_ADMIN, LANGUAGE, "admin1", "secret1");
    private User user2 = new User("admin2", "admin2@test.com", ROLE_ADMIN, LANGUAGE, "admin2", "secret2");
    private User user3 = new User("user1", "part1@test.de", ROLE_PARTICIPANT, LANGUAGE, "user", null);
    private User user4 = new User("user2", "part2@test.de", ROLE_PARTICIPANT, LANGUAGE, "user", "secret4");
    private User user5 = new User("user3", "part3@test.de", ROLE_PARTICIPANT, LANGUAGE, "user", null);
    private User user6 = new User("user4", "part4@test.de", ROLE_ADMIN, LANGUAGE, "user", null);
    private User user7 = new User("user5", "part5@test.de", ROLE_PARTICIPANT, LANGUAGE, "user", null);
    private User user8 = new User("user6", "part6@test.de", ROLE_PARTICIPANT, LANGUAGE, "user", null);
    private User user9 = new User("part1", "part7@test.de", ROLE_PARTICIPANT, LANGUAGE, "user", null);
    private User user10 = new User("part2", "part8@test.de", ROLE_PARTICIPANT, LANGUAGE, "user", null);
    private User user11 = new User("part3", "part9@test.de", ROLE_PARTICIPANT, LANGUAGE, "user", null);
    private Experiment experiment = new Experiment(null, "My Experiment", "Some description", "", true);
    private Participant participant;

    @BeforeEach
    public void setup() {
        user1 = entityManager.persist(user1);
        user2 = entityManager.persist(user2);
        user3 = entityManager.persist(user3);
        user4 = entityManager.persist(user4);
        user5 = entityManager.persist(user5);
        user6 = entityManager.persist(user6);
        user7 = entityManager.persist(user7);
        user8 = entityManager.persist(user8);
        user9 = entityManager.persist(user9);
        user10 = entityManager.persist(user10);
        user11 = entityManager.persist(user11);
        experiment = entityManager.persist(experiment);
        participant = new Participant(user3, experiment, null, null);
        participant = entityManager.persist(participant);
    }

    @Test
    public void testExistsByUsername() {
        assertAll(
                () -> assertTrue(userRepository.existsByUsername(ADMIN1)),
                () -> assertFalse(userRepository.existsByUsername(ADMIN1 + 1))
        );
    }

    @Test
    public void testFindUserByUsername() {
        assertAll(
                () -> assertEquals(user1.getId(), userRepository.findUserByUsername(ADMIN1).getId()),
                () -> assertNull(userRepository.findUserByUsername(ADMIN1 + 1))
        );
    }

    @Test
    public void testFindUserByRole() {
        List<User> admins = userRepository.findAllByRole(ROLE_ADMIN);
        assertEquals(3, admins.size());
    }

    @Test
    public void testFindParticipantSuggestionsUsername() {
        List<User> users = userRepository.findParticipantSuggestions(USERNAME_SEARCH, experiment.getId());
        assertAll(
                () -> assertEquals(3, users.size()),
                () -> assertTrue(users.contains(user5)),
                () -> assertTrue(users.contains(user7)),
                () -> assertTrue(users.contains(user8)),
                () -> assertFalse(users.contains(user6)),
                () -> assertFalse(users.contains(user4)),
                () -> assertFalse(users.contains(user3))
        );
    }

    @Test
    public void testFindParticipantSuggestionsEmail() {
        List<User> users = userRepository.findParticipantSuggestions(EMAIL_SEARCH, experiment.getId());
        assertAll(
                () -> assertEquals(5, users.size()),
                () -> assertTrue(users.contains(user5)),
                () -> assertTrue(users.contains(user7)),
                () -> assertTrue(users.contains(user8)),
                () -> assertTrue(users.contains(user9)),
                () -> assertTrue(users.contains(user10)),
                () -> assertFalse(users.contains(user6)),
                () -> assertFalse(users.contains(user4)),
                () -> assertFalse(users.contains(user3))
        );
    }

    @Test
    public void testFindParticipantSuggestionsEmpty() {
        List<User> users = userRepository.findParticipantSuggestions(ADMIN1, experiment.getId());
        assertTrue(users.isEmpty());
    }

}
