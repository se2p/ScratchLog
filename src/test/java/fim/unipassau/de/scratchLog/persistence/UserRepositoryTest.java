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

import fim.unipassau.de.scratchLog.persistence.entity.Course;
import fim.unipassau.de.scratchLog.persistence.entity.CourseParticipant;
import fim.unipassau.de.scratchLog.persistence.entity.Experiment;
import fim.unipassau.de.scratchLog.persistence.entity.Participant;
import fim.unipassau.de.scratchLog.persistence.entity.User;
import fim.unipassau.de.scratchLog.persistence.projection.UserProjection;
import fim.unipassau.de.scratchLog.persistence.repository.UserRepository;
import fim.unipassau.de.scratchLog.util.Constants;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private static final String LANGUAGE = "ENGLISH";
    private static final String ADMIN1 = "admin1";
    private static final String USERNAME_SEARCH = "user";
    private static final String EMAIL_SEARCH = "test";
    private static final String USER_SEARCH = "2";
    private static final String QUERY = "a";
    private static final String GUI_URL = "scratch";
    private static final int LIMIT = 5;
    private static final LocalDateTime DATE = LocalDateTime.now();
    private User user1 = new User(ADMIN1, "admin1@test.de", Role.ADMIN, Language.ENGLISH, "admin1", "secret1");
    private User user2 = new User("admin2", "admin2@test.com", Role.ADMIN, Language.ENGLISH, "admin2", "secret2");
    private User user3 = new User("user1", "part1@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", null);
    private User user4 = new User("user2", "part2@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", "secret4");
    private User user5 = new User("user3", "part3@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", null);
    private User user6 = new User("user4", "part4@test.de", Role.ADMIN, Language.ENGLISH, "user", null);
    private User user7 = new User("user5", "part5@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", null);
    private User user8 = new User("user6", "part6@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", null);
    private User user9 = new User("part1", "part7@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", null);
    private User user10 = new User("part2", "part8@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", null);
    private User user11 = new User("part3", "part9@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", null);
    private User user12 = new User("part4", "part10@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", null);
    private User user13 = new User("part5", "part11@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", null);
    private User user14 = new User("part6", "part12@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", null);
    private User user15 = new User("part7", "part13@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", null);
    private User user16 = new User("part8", "part14@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", null);
    private User user17 = new User("user17", "part17@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", null);
    private Experiment experiment1 = new Experiment(null, "My Experiment", "Some description", "", "", true, false,
            GUI_URL);
    private Experiment experiment2 = new Experiment(null, "New Experiment", "Some description", "", "", true, false,
            GUI_URL);
    private Course course1 = new Course(null, "Course 1", "Description 1", "", false, DATE);
    private Course course2 = new Course(null, "Course 2", "Description 2", "", false, DATE);
    private Course course3 = new Course(null, "Course 3", "Description 3", "", false, DATE);
    private Participant participant1 = new Participant(user3, experiment1, null, null);
    private Participant participant2 = new Participant(user12, experiment1, null, null);
    private Participant participant3 = new Participant(user13, experiment1, null, null);
    private Participant participant4 = new Participant(user14, experiment1, null, null);
    private Participant participant5 = new Participant(user15, experiment1, null, null);
    private Participant participant6 = new Participant(user16, experiment2, null, null);
    private CourseParticipant courseParticipant1 = new CourseParticipant(user3, course1, DATE);
    private CourseParticipant courseParticipant2 = new CourseParticipant(user3, course2, DATE);
    private CourseParticipant courseParticipant3 = new CourseParticipant(user4, course1, DATE);
    private CourseParticipant courseParticipant4 = new CourseParticipant(user5, course1, DATE);
    private CourseParticipant courseParticipant5 = new CourseParticipant(user6, course1, DATE);

    @BeforeEach
    public void setup() {
        user1.setLastLogin(LocalDateTime.now());
        user2.setLastLogin(LocalDateTime.now());
        user3.setLastLogin(LocalDateTime.now());
        user4.setLastLogin(LocalDateTime.now());
        user5.setLastLogin(LocalDateTime.now());
        user6.setLastLogin(LocalDateTime.now());
        user7.setLastLogin(LocalDateTime.now());
        user8.setLastLogin(LocalDateTime.now());
        user9.setLastLogin(LocalDateTime.now());
        user10.setLastLogin(LocalDateTime.now());
        user11.setLastLogin(LocalDateTime.now());
        user12.setLastLogin(LocalDateTime.now());
        user13.setLastLogin(LocalDateTime.now());
        user14.setLastLogin(LocalDateTime.now());
        user15.setLastLogin(LocalDateTime.now());
        user16.setLastLogin(LocalDateTime.now());
        user17.setLastLogin(LocalDateTime.now());
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
        user17 = entityManager.persist(user17);
        course1 = entityManager.persist(course1);
        course2 = entityManager.persist(course2);
        course3 = entityManager.persist(course3);
        experiment1 = entityManager.persist(experiment1);
        experiment2 = entityManager.persist(experiment2);
        participant1 = entityManager.persist(participant1);
        participant2 = entityManager.persist(participant2);
        participant3 = entityManager.persist(participant3);
        participant4 = entityManager.persist(participant4);
        participant5 = entityManager.persist(participant5);
        participant6 = entityManager.persist(participant6);
        courseParticipant1 = entityManager.persist(courseParticipant1);
        courseParticipant2 = entityManager.persist(courseParticipant2);
        courseParticipant3 = entityManager.persist(courseParticipant3);
        courseParticipant4 = entityManager.persist(courseParticipant4);
        courseParticipant5 = entityManager.persist(courseParticipant5);
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
                () -> assertEquals(user1.getId(), userRepository.findUserByUsername(ADMIN1).get().getId()),
                () -> assertEquals(Optional.empty(), userRepository.findUserByUsername(ADMIN1 + 1))
        );
    }

    @Test
    public void testFindUserByRole() {
        List<User> admins = userRepository.findAllByRole(Role.ADMIN);
        assertEquals(3, admins.size());
    }

    @Test
    public void testFindUserSuggestions() {
        List<UserProjection> users = userRepository.findUserSuggestions(USER_SEARCH, LIMIT);
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
        List<UserProjection> users = userRepository.findUserSuggestions(LANGUAGE, LIMIT);
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
                () -> assertEquals(17, users.size()),
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
        assertEquals(17, userRepository.getUserResultsCount(QUERY));
    }

    @Test
    public void testGetUserResultCountUser() {
        assertEquals(7, userRepository.getUserResultsCount(USERNAME_SEARCH));
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
        List<UserProjection> users = userRepository.findParticipantSuggestions(USERNAME_SEARCH, experiment1.getId(),
                LIMIT);
        assertAll(
                () -> assertEquals(5, users.size()),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user4.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user5.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user7.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user8.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user17.getUsername()))),
                () -> assertFalse(users.stream().anyMatch(user -> user.getUsername().equals(user6.getUsername()))),
                () -> assertFalse(users.stream().anyMatch(user -> user.getUsername().equals(user3.getUsername())))
        );
    }

    @Test
    public void testFindParticipantSuggestionsEmail() {
        List<UserProjection> users = userRepository.findParticipantSuggestions(EMAIL_SEARCH, experiment1.getId(),
                LIMIT);
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
        List<UserProjection> users = userRepository.findParticipantSuggestions(ADMIN1, experiment1.getId(), LIMIT);
        assertTrue(users.isEmpty());
    }

    @Test
    public void testFindParticipantSuggestionsCourse() {
        List<UserProjection> users = userRepository.findParticipantSuggestions(EMAIL_SEARCH, experiment1.getId(),
                course1.getId(), LIMIT);
        assertAll(
                () -> assertEquals(2, users.size()),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user4.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user5.getUsername()))),
                () -> assertFalse(users.stream().anyMatch(user -> user.getUsername().equals(user6.getUsername()))),
                () -> assertFalse(users.stream().anyMatch(user -> user.getUsername().equals(user3.getUsername())))
        );
    }

    @Test
    public void testFindParticipantSuggestionsCourseEmpty() {
        assertTrue(userRepository.findParticipantSuggestions(EMAIL_SEARCH, experiment1.getId(), course3.getId(),
                LIMIT).isEmpty());
    }

    @Test
    public void testFindDeleteParticipantSuggestions() {
        Participant participant = new Participant(user4, experiment1, null, null);
        entityManager.persist(participant);
        List<UserProjection> users = userRepository.findDeleteParticipantSuggestions(EMAIL_SEARCH, experiment1.getId(),
                LIMIT);
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
        List<UserProjection> users = userRepository.findDeleteParticipantSuggestions(EMAIL_SEARCH, experiment2.getId(),
                LIMIT);
        assertAll(
                () -> assertEquals(1, users.size()),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user16.getUsername())))
        );
    }

    @Test
    public void testFindDeleteParticipantSuggestionsEmpty() {
        List<UserProjection> users = userRepository.findDeleteParticipantSuggestions(EMAIL_SEARCH, 100, LIMIT);
        assertTrue(users.isEmpty());
    }

    @Test
    public void testFindCourseParticipantSuggestions() {
        List<UserProjection> users = userRepository.findCourseParticipantSuggestions(USERNAME_SEARCH, course1.getId(),
                LIMIT);
        assertAll(
                () -> assertEquals(3, users.size()),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user7.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user8.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user17.getUsername())))
        );
    }

    @Test
    public void testFindCourseParticipantSuggestionsLimit() {
        List<UserProjection> users = userRepository.findCourseParticipantSuggestions(EMAIL_SEARCH, course1.getId(),
                LIMIT);
        assertAll(
                () -> assertEquals(LIMIT, users.size()),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user7.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user8.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user9.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user10.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user11.getUsername())))
        );
    }

    @Test
    public void testFindCourseParticipantSuggestionsEmpty() {
        assertTrue(userRepository.findCourseParticipantSuggestions(ADMIN1, course1.getId(), LIMIT).isEmpty());
    }

    @Test
    public void testFindDeleteCourseParticipantSuggestions() {
        List<UserProjection> users = userRepository.findDeleteCourseParticipantSuggestions(USERNAME_SEARCH,
                course2.getId(), LIMIT);
        assertAll(
                () -> assertEquals(1, users.size()),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user3.getUsername())))
        );
    }

    @Test
    public void testFindDeleteCourseParticipantSuggestionsLimit() {
        List<UserProjection> users = userRepository.findDeleteCourseParticipantSuggestions(USERNAME_SEARCH,
                course1.getId(), 3);
        assertAll(
                () -> assertEquals(3, users.size()),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user3.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user4.getUsername()))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user5.getUsername())))
        );
    }

    @Test
    public void testFindDeleteCourseParticipantSuggestionsEmpty() {
        assertTrue(userRepository.findDeleteCourseParticipantSuggestions(EMAIL_SEARCH, course3.getId(),
                LIMIT).isEmpty());
    }

    @Test
    public void testFindLastUsername() {
        Optional<UserProjection> user = userRepository.findLastUsername(USERNAME_SEARCH);
        Optional<UserProjection> participant = userRepository.findLastUsername("part");
        assertAll(
                () -> assertTrue(user.isPresent()),
                () -> assertTrue(participant.isPresent()),
                () -> assertEquals(USERNAME_SEARCH + "17", user.get().getUsername()),
                () -> assertEquals("part8", participant.get().getUsername())
        );
    }

    @Test
    public void testFindLastUsernameDeleteUser() {
        entityManager.remove(user17);
        Optional<UserProjection> user = userRepository.findLastUsername(USERNAME_SEARCH);
        assertAll(
                () -> assertTrue(user.isPresent()),
                () -> assertEquals(USERNAME_SEARCH + "6", user.get().getUsername())
        );
    }

    @Test
    public void testFindLastUsernameAddUser() {
        String username = "user18";
        User user = new User(username, "part6@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", null);
        user.setLastLogin(LocalDateTime.now());
        user = entityManager.persist(user);
        Optional<UserProjection> findUser = userRepository.findLastUsername(USERNAME_SEARCH);
        entityManager.remove(user);
        assertAll(
                () -> assertTrue(findUser.isPresent()),
                () -> assertEquals(username, findUser.get().getUsername())
        );
    }

}
