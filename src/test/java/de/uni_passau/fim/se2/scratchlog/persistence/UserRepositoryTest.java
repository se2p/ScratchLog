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

package de.uni_passau.fim.se2.scratchlog.persistence;

import de.uni_passau.fim.se2.scratchlog.AbstractScratchLogTest;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Course;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Participant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.UserProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryTest extends AbstractScratchLogTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    private static final String LANGUAGE = "ENGLISH";
    private static final String ADMIN1 = "admin_1";
    private static final String USERNAME_SEARCH = "user";
    private static final String EMAIL_SEARCH = "example";
    private static final String USER_SEARCH = "2";
    private static final String QUERY = "a";
    private static final int LIMIT = 5;

    private List<User> users;
    private Experiment experiment1;
    private Experiment experiment2;
    private Course course1;
    private Course course2;
    private Course course3;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        users = entityUtilService.generateUsers("admin", 2);
        users.get(0).setRole(Role.ADMIN);
        users.get(1).setRole(Role.ADMIN);
        users = userRepository.saveAll(users);
        users.addAll(entityUtilService.generateUsers("user", 7));
        users.addAll(entityUtilService.generateUsers("part", 8));

        course1 = entityUtilService.generateCourse("Course 1");
        course2 = entityUtilService.generateCourse("Course 2");
        course3 = entityUtilService.generateCourse("Course 3");

        experiment1 = entityUtilService.generateExperiment("My Experiment");
        experiment2 = entityUtilService.generateExperiment("New Experiment");

        entityUtilService.addUsersToExperiment(experiment1, List.of(user(3), user(12), user(13), user(14), user(15), user(16)));
        entityUtilService.addUsersToExperiment(experiment2, List.of(user(16)));
        entityUtilService.addUsersToCourse(course1, List.of(user(3), user(4), user(5), user(6)));
        entityUtilService.addUsersToCourse(course2, List.of(user(3)));
    }

    private User user(final int id) {
        return users.get(id - 1);
    }

    @Test
    void testExistsByUsername() {
        assertAll(
                () -> assertTrue(userRepository.existsByUsername(users.get(0).getUsername())),
                () -> assertFalse(userRepository.existsByUsername(ADMIN1 + 1))
        );
    }

    @Test
    void testFindUserByUsername() {
        assertAll(
                () -> assertEquals(user(1).getId(), userRepository.findUserByUsername(users.get(0).getUsername()).orElseThrow().getId()),
                () -> assertEquals(Optional.empty(), userRepository.findUserByUsername(ADMIN1 + 1))
        );
    }

    @Test
    void testFindUserByRole() {
        List<User> admins = userRepository.findAllByRole(Role.ADMIN);
        assertEquals(2, admins.size());
    }

    @Test
    void testFindUserSuggestions() {
        List<UserProjection> users = userRepository.findUserSuggestions(USER_SEARCH, LIMIT);
        assertThat(users.stream().map(UserProjection::getUsername))
            // we have at least 3 users with 2 in the given name, plus maybe some with 2 in UUID prefix
            .hasSizeBetween(3, LIMIT)
            .allMatch(username -> username.contains(USER_SEARCH));
    }

    @Test
    void testFindUserSuggestionsEmpty() {
        List<UserProjection> users = userRepository.findUserSuggestions(LANGUAGE, LIMIT);
        assertTrue(users.isEmpty());
    }

    @Test
    void testFindUserResults() {
        List<UserProjection> users = userRepository.findUserResults(QUERY, Constants.PAGE_SIZE, 0);
        assertAll(
                () -> assertEquals(10, users.size()),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUsername().equals(user(1).getUsername())))
        );
    }

    @Test
    void testFindUserResultsOffset() {
        List<UserProjection> users = userRepository.findUserResults(QUERY, Constants.PAGE_SIZE, 2);
        assertThat(users).hasSize(10)
                .noneMatch(user -> user.getUsername().equals(user(1).getUsername()));
    }

    @Test
    void testFindUserResultsAll() {
        List<UserProjection> users = userRepository.findUserResults(QUERY, 20, 0);
        assertThat(users).hasSize(17);
    }

    @Test
    void testFindUserResultsEmpty() {
        assertTrue(userRepository.findUserResults(LANGUAGE, Constants.PAGE_SIZE, 0).isEmpty());
    }

    @Test
    void testGetUserResultCount() {
        assertEquals(17, userRepository.getUserResultsCount(QUERY));
    }

    @Test
    void testGetUserResultCountUser() {
        assertEquals(7, userRepository.getUserResultsCount(USERNAME_SEARCH));
    }

    @Test
    void testGetUserResultCountAdmin() {
        assertEquals(1, userRepository.getUserResultsCount(ADMIN1));
    }

    @Test
    void testGetUserResultCountZero() {
        assertEquals(0, userRepository.getUserResultsCount(LANGUAGE));
    }

    @Test
    void testFindParticipantSuggestionsUsername() {
        List<UserProjection> users = userRepository.findParticipantSuggestions(USERNAME_SEARCH, experiment1.getId(),
                LIMIT);
        assertThat(users).hasSize(5);
        assertThat(users.stream().map(UserProjection::getUsername)).containsExactlyInAnyOrderElementsOf(
            Stream.of(4, 5, 6, 7, 8).map(id -> user(id).getUsername()).toList()
        );
    }

    @Test
    void testFindParticipantSuggestionsEmail() {
        List<UserProjection> users = userRepository.findParticipantSuggestions(EMAIL_SEARCH, experiment1.getId(),
                LIMIT);
        assertThat(users).hasSize(5);
    }

    @Test
    void testFindParticipantSuggestionsEmpty() {
        List<UserProjection> users = userRepository.findParticipantSuggestions(ADMIN1, experiment1.getId(), LIMIT);
        assertTrue(users.isEmpty());
    }

    @Test
    void testFindParticipantSuggestionsCourse() {
        List<UserProjection> users = userRepository.findParticipantSuggestions(EMAIL_SEARCH, experiment1.getId(),
                course1.getId(), LIMIT);
        assertThat(users).hasSize(3);
        assertThat(users.stream().map(UserProjection::getUsername)).containsExactlyInAnyOrder(
            user(4).getUsername(), user(5).getUsername(), user(6).getUsername()
        );
    }

    @Test
    void testFindParticipantSuggestionsCourseEmpty() {
        assertTrue(userRepository.findParticipantSuggestions(EMAIL_SEARCH, experiment1.getId(), course3.getId(),
                LIMIT).isEmpty());
    }

    @Test
    void testFindDeleteParticipantSuggestions() {
        Participant participant = new Participant(user(4), experiment1, null, null);
        participantRepository.save(participant);

        List<UserProjection> users = userRepository.findDeleteParticipantSuggestions(EMAIL_SEARCH, experiment1.getId(),
                LIMIT);
        assertThat(users).hasSize(5).noneMatch(user -> user.getUsername().equals(user(16).getUsername()));
    }

    @Test
    void testFindDeleteParticipantSuggestionsOne() {
        List<UserProjection> users = userRepository.findDeleteParticipantSuggestions(EMAIL_SEARCH, experiment2.getId(),
                LIMIT);
        assertThat(users)
            .hasSize(1)
            .allMatch(user -> user.getUsername().equals(user(16).getUsername()));
    }

    @Test
    void testFindDeleteParticipantSuggestionsEmpty() {
        List<UserProjection> users = userRepository.findDeleteParticipantSuggestions(EMAIL_SEARCH, 100, LIMIT);
        assertTrue(users.isEmpty());
    }

    @Test
    void testFindCourseParticipantSuggestions() {
        List<UserProjection> users = userRepository.findCourseParticipantSuggestions(USERNAME_SEARCH, course1.getId(),
                LIMIT);
        assertThat(users).hasSize(3);
        assertThat(users.stream().map(UserProjection::getUsername)).containsExactlyInAnyOrder(
            user(7).getUsername(), user(8).getUsername(), user(9).getUsername()
        );
    }

    @Test
    void testFindCourseParticipantSuggestionsLimit() {
        List<UserProjection> users = userRepository.findCourseParticipantSuggestions(EMAIL_SEARCH, course1.getId(),
                LIMIT);
        assertThat(users).hasSize(LIMIT);
        assertThat(users.stream().map(UserProjection::getUsername)).containsExactlyInAnyOrderElementsOf(
            Stream.of(7, 8, 9, 10, 11).map(id -> user(id).getUsername()).toList()
        );
    }

    @Test
    void testFindCourseParticipantSuggestionsEmpty() {
        assertTrue(userRepository.findCourseParticipantSuggestions(ADMIN1, course1.getId(), LIMIT).isEmpty());
    }

    @Test
    void testFindDeleteCourseParticipantSuggestions() {
        List<UserProjection> users = userRepository.findDeleteCourseParticipantSuggestions(USERNAME_SEARCH,
                course2.getId(), LIMIT);
        assertThat(users)
            .hasSize(1)
            .allMatch(user -> user.getUsername().equals(user(3).getUsername()));
    }

    @Test
    void testFindDeleteCourseParticipantSuggestionsLimit() {
        List<UserProjection> users = userRepository.findDeleteCourseParticipantSuggestions(USERNAME_SEARCH,
                course1.getId(), 3);
        assertThat(users).hasSize(3);
        assertThat(users.stream().map(UserProjection::getUsername)).containsExactly(
            user(3).getUsername(), user(4).getUsername(), user(5).getUsername()
        );
    }

    @Test
    void testFindDeleteCourseParticipantSuggestionsEmpty() {
        assertTrue(userRepository.findDeleteCourseParticipantSuggestions(EMAIL_SEARCH, course3.getId(),
                LIMIT).isEmpty());
    }

    @Test
    void testFindLastUsername() {
        final String usernamePrefix = getUserPrefix(user(3));
        final String participantPrefix = getUserPrefix(user(16));

        Optional<UserProjection> user = userRepository.findLastUsername(usernamePrefix);
        Optional<UserProjection> participant = userRepository.findLastUsername(participantPrefix);

        assertAll(
                () -> assertTrue(user.isPresent()),
                () -> assertTrue(participant.isPresent()),
                () -> assertThat(user.orElseThrow().getUsername()).endsWith("user_7"),
                () -> assertThat(participant.orElseThrow().getUsername()).endsWith("part_8")
        );
    }

    @Test
    void testFindLastUsernameDeleteUser() {
        userRepository.delete(user(9));

        final String usernamePrefix = getUserPrefix(user(9));
        Optional<UserProjection> user = userRepository.findLastUsername(usernamePrefix);
        assertAll(
                () -> assertTrue(user.isPresent()),
                () -> assertThat(user.orElseThrow().getUsername()).endsWith("user_6")
        );
    }

    @Test
    void testFindLastUsernameAddUser() {
        String username = "user18";
        User user = new User(username, "part6@test.de", Role.PARTICIPANT, Language.ENGLISH, "user", null);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        Optional<UserProjection> findUser = userRepository.findLastUsername(USERNAME_SEARCH);
        assertAll(
                () -> assertTrue(findUser.isPresent()),
                () -> assertEquals(username, findUser.get().getUsername())
        );
    }

    private String getUserPrefix(final User user) {
        // UUID_name_NUMBER -> remove NUMBER at the end
        return Arrays.stream(user.getUsername().split("_")).limit(2).collect(Collectors.joining("_"));
    }

}
