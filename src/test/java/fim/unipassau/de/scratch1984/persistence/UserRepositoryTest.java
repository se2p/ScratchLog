package fim.unipassau.de.scratch1984.persistence;

import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.projection.UserProjection;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.util.Constants;
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
    private static final String USER_SEARCH = "2";
    private static final String QUERY = "a";
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
    private User user12 = new User("part4", "part10@test.de", ROLE_PARTICIPANT, LANGUAGE, "user", null);
    private User user13 = new User("part5", "part11@test.de", ROLE_PARTICIPANT, LANGUAGE, "user", null);
    private User user14 = new User("part6", "part12@test.de", ROLE_PARTICIPANT, LANGUAGE, "user", null);
    private User user15 = new User("part7", "part13@test.de", ROLE_PARTICIPANT, LANGUAGE, "user", null);
    private User user16 = new User("part8", "part14@test.de", ROLE_PARTICIPANT, LANGUAGE, "user", null);
    private Experiment experiment1 = new Experiment(null, "My Experiment", "Some description", "", "", true);
    private Experiment experiment2 = new Experiment(null, "New Experiment", "Some description", "", "", true);
    private Participant participant1 = new Participant(user3, experiment1, null, null);
    private Participant participant2 = new Participant(user12, experiment1, null, null);
    private Participant participant3 = new Participant(user13, experiment1, null, null);
    private Participant participant4 = new Participant(user14, experiment1, null, null);
    private Participant participant5 = new Participant(user15, experiment1, null, null);
    private Participant participant6 = new Participant(user16, experiment2, null, null);

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
        user12 = entityManager.persist(user12);
        user13 = entityManager.persist(user13);
        user14 = entityManager.persist(user14);
        user15 = entityManager.persist(user15);
        user16 = entityManager.persist(user16);
        experiment1 = entityManager.persist(experiment1);
        experiment2 = entityManager.persist(experiment2);
        participant1 = entityManager.persist(participant1);
        participant2 = entityManager.persist(participant2);
        participant3 = entityManager.persist(participant3);
        participant4 = entityManager.persist(participant4);
        participant5 = entityManager.persist(participant5);
        participant6 = entityManager.persist(participant6);
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
    public void testFindUserSuggestions() {
        List<UserProjection> users = userRepository.findUserSuggestions(USER_SEARCH);
        assertAll(
                () -> assertEquals(4, users.size()),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user2.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user4.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user10.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user14.getUsername())))
        );
    }

    @Test
    public void testFindUserSuggestionsEmpty() {
        List<UserProjection> users = userRepository.findUserSuggestions(LANGUAGE);
        assertTrue(users.isEmpty());
    }

    @Test
    public void testFindUserResults() {
        List<UserProjection> users = userRepository.findUserResults(QUERY, Constants.PAGE_SIZE, 0);
        assertAll(
                () -> assertEquals(10, users.size()),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user1.getUsername())))
        );
    }

    @Test
    public void testFindUserResultsOffset() {
        List<UserProjection> users = userRepository.findUserResults(QUERY, Constants.PAGE_SIZE, 2);
        assertAll(
                () -> assertEquals(10, users.size()),
                () -> assertFalse(users.stream().anyMatch(user -> user.getUsername().equals(user1.getUsername()))),
                () -> assertFalse(users.stream().anyMatch(user -> user.getUsername().equals(user2.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user3.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user4.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user5.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user6.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user7.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user8.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user9.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user10.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user11.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user12.getUsername())))
        );
    }

    @Test
    public void testFindUserResultsAll() {
        List<UserProjection> users = userRepository.findUserResults(QUERY, 20, 0);
        assertAll(
                () -> assertEquals(16, users.size()),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user1.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user2.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user3.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user4.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user5.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user6.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user7.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user8.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user9.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user10.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user11.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user12.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user13.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user14.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user15.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user16.getUsername())))
        );
    }

    @Test
    public void testFindUserResultsEmpty() {
        assertTrue(userRepository.findUserResults(LANGUAGE, Constants.PAGE_SIZE, 0).isEmpty());
    }

    @Test
    public void testGetUserResultCount() {
        assertEquals(16, userRepository.getUserResultsCount(QUERY));
    }

    @Test
    public void testGetUserResultCountUser() {
        assertEquals(6, userRepository.getUserResultsCount(USERNAME_SEARCH));
    }

    @Test
    public void testGetUserResultCountAdmin() {
        assertEquals(1, userRepository.getUserResultsCount(ADMIN1));
    }

    @Test
    public void testGetUserResultCountZero() {
        assertEquals(0, userRepository.getUserResultsCount(LANGUAGE));
    }

    @Test
    public void testFindParticipantSuggestionsUsername() {
        List<UserProjection> users = userRepository.findParticipantSuggestions(USERNAME_SEARCH, experiment1.getId());
        assertAll(
                () -> assertEquals(4, users.size()),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user4.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user5.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user7.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user8.getUsername()))),
                () -> assertFalse(users.stream().anyMatch(user -> user.getUsername().equals(user6.getUsername()))),
                () -> assertFalse(users.stream().anyMatch(user -> user.getUsername().equals(user3.getUsername())))
        );
    }

    @Test
    public void testFindParticipantSuggestionsEmail() {
        List<UserProjection> users = userRepository.findParticipantSuggestions(EMAIL_SEARCH, experiment1.getId());
        assertAll(
                () -> assertEquals(5, users.size()),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user4.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user5.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user7.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user8.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user9.getUsername()))),
                () -> assertFalse(users.stream().anyMatch(user -> user.getUsername().equals(user6.getUsername()))),
                () -> assertFalse(users.stream().anyMatch(user -> user.getUsername().equals(user3.getUsername())))
        );
    }

    @Test
    public void testFindParticipantSuggestionsEmpty() {
        List<UserProjection> users = userRepository.findParticipantSuggestions(ADMIN1, experiment1.getId());
        assertTrue(users.isEmpty());
    }

    @Test
    public void testFindDeleteParticipantSuggestions() {
        Participant participant = new Participant(user4, experiment1, null, null);
        entityManager.persist(participant);
        List<UserProjection> users = userRepository.findDeleteParticipantSuggestions(EMAIL_SEARCH, experiment1.getId());
        assertAll(
                () -> assertEquals(5, users.size()),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user3.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user4.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user12.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user13.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user14.getUsername()))),
                () -> assertFalse(users.stream().anyMatch(user -> user.getUsername().equals(user16.getUsername())))
        );
    }

    @Test
    public void testFindDeleteParticipantSuggestionsOne() {
        List<UserProjection> users = userRepository.findDeleteParticipantSuggestions(EMAIL_SEARCH, experiment2.getId());
        assertAll(
                () -> assertEquals(1, users.size()),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user16.getUsername())))
        );
    }

    @Test
    public void testFindDeleteParticipantSuggestionsEmpty() {
        List<UserProjection> users = userRepository.findDeleteParticipantSuggestions(EMAIL_SEARCH, 100);
        assertTrue(users.isEmpty());
    }

}
