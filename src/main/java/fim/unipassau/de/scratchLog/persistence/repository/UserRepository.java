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

package fim.unipassau.de.scratchLog.persistence.repository;

import fim.unipassau.de.scratchLog.persistence.entity.User;
import fim.unipassau.de.scratchLog.persistence.projection.UserProjection;
import fim.unipassau.de.scratchLog.util.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * A repository providing functionality for retrieving user data.
 */
public interface UserRepository extends JpaRepository<User, Integer> {

     /**
     * Checks, whether a user with the given username already exists in the database.
     *
     * @param username The username to search for.
     * @return {@code true} iff the username already exists.
     */
    boolean existsByUsername(String username);

    /**
     * Checks, whether a user with the given email already exists in the database.
     *
     * @param email The email to search for.
     * @return {@code true} iff the email already exists.
     */
    boolean existsByEmail(String email);

    /**
     * Returns the user identified by the given username, if one exists.
     *
     * @param username The username to search for.
     * @return The user data or {@code null}, if no user could be found.
     */
    Optional<User> findUserByUsername(String username);

    /**
     * Returns the user with the given secret, if one exists.
     *
     * @param secret The secret to search for.
     * @return The user data, or {@code null}, if no user could be found.
     */
    Optional<User> findUserBySecret(String secret);

    /**
     * Returns the user identified by the given username or email address, if one exists.
     *
     * @param username The username to search for.
     * @param email The email to search for.
     * @return The user data, or {@code null}, if no user could be found.
     */
    Optional<User> findUserByUsernameOrEmail(String username, String email);

    /**
     * Returns the user identified by the given id, if one exists.
     *
     * @param id The id to search for.
     * @return The user data or {@code null}, if no user could be found.
     */
    Optional<User> findById(int id);

    /**
     * Returns the user with the given email, if one exists.
     *
     * @param email The email to search for.
     * @return The user data or {@code null}, if no user could be found.
     */
    Optional<User> findByEmail(String email);

    /**
     * Returns a list of users with the given role, or an empty list, if no such user exists.
     *
     * @param role The user role to search for.
     * @return A list of users.
     */
    List<User> findAllByRole(Role role);

    /**
     * Returns a list of users with the given role who have last logged in before the given date and time, or an empty
     * list, if no such user exists.
     *
     * @param role The user role to search for.
     * @param lastLogin The last login time to search for.
     * @return A list of users.
     */
    List<User> findAllByRoleAndLastLoginBefore(Role role, LocalDateTime lastLogin);

    /**
     * Returns the user with the highest user id currently existing in the database.
     *
     * @return The user.
     */
    Optional<User> findFirstByOrderByIdDesc();

    /**
     * Returns a list of the first users up to the given limit whose email or username contain the given query value.
     *
     * @param query The username or email to search for.
     * @param limit The maximum number of results to return.
     * @return A list of {@link UserProjection}s.
     */
    @Query(nativeQuery = true, value = "SELECT u.* FROM `user` AS u WHERE (u.username LIKE CONCAT('%', :query, '%') "
            + "OR u.email LIKE CONCAT('%', :query, '%')) LIMIT :limit")
    List<UserProjection> findUserSuggestions(@Param("query") String query, @Param("limit") int limit);

    /**
     * Returns a list of at most as many users as the given limit with the given offset whose email or username contain
     * the given query value.
     *
     * @param query The username or email to search for.
     * @param limit The maximum amount of results to be returned.
     * @param offset The offset used to return new results.
     * @return A list of {@link UserProjection}s.
     */
    @Query(nativeQuery = true, value = "SELECT u.* FROM `user` AS u WHERE (u.username LIKE CONCAT('%', :query, '%') "
            + "OR u.email LIKE CONCAT('%', :query, '%')) LIMIT :limit OFFSET :offset")
    List<UserProjection> findUserResults(@Param("query") String query, @Param("limit") int limit,
                                         @Param("offset") int offset);

    /**
     * Returns the number of users whose email or username contain the given query value.
     *
     * @param query The username or email to search for.
     * @return The number of user results.
     */
    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM `user` AS u WHERE (u.username LIKE "
            + "CONCAT('%', :query, '%') OR u.email LIKE CONCAT('%', :query, '%'))")
    int getUserResultsCount(@Param("query") String query);

    /**
     * Returns a list of the first users whose email or username contain the given query value up to the given limit who
     * are not already participating in an experiment.
     *
     * @param query The username or email to search for.
     * @param experiment The id of the experiment.
     * @param limit The maximum number of results to return.
     * @return A list of users.
     */
    @Query(nativeQuery = true, value = "SELECT u.* FROM `user` AS u WHERE (u.username LIKE CONCAT('%', :query, '%') "
            + "OR u.email LIKE CONCAT('%', :query, '%')) AND u.role = 'PARTICIPANT' AND u.id NOT IN "
            + "(SELECT p.user_id FROM participant AS p WHERE p.experiment_id = :id) LIMIT :limit")
    List<UserProjection> findParticipantSuggestions(@Param("query") String query, @Param("id") int experiment,
                                                    @Param("limit") int limit);

    /**
     * Returns a list of the first users whose email or username contain the given query value up to the given limit who
     * are not already participating in an experiment but are participant of the course with the given id.
     *
     * @param query The username or email to search for.
     * @param experiment The id of the experiment.
     * @param course The id of the course.
     * @param limit The maximum number of results to return.
     * @return A list of users.
     */
    @Query(nativeQuery = true, value = "SELECT u.* FROM `user` AS u WHERE (u.username LIKE CONCAT('%', :query, '%') "
            + "OR u.email LIKE CONCAT('%', :query, '%')) AND u.role = 'PARTICIPANT' AND u.id NOT IN "
            + "(SELECT p.user_id FROM participant AS p WHERE p.experiment_id = :experiment) AND u.id IN "
            + "(SELECT c.user_id from course_participant AS c WHERE c.course_id = :course) LIMIT :limit")
    List<UserProjection> findParticipantSuggestions(@Param("query") String query, @Param("experiment") int experiment,
                                                    @Param("course") int course, @Param("limit") int limit);

    /**
     * Returns a list of the first users whose email or username contain the given query value up to the given limit who
     * are participating in the given experiment.
     *
     * @param query The username or email to search for.
     * @param experiment The id of the experiment.
     * @param limit The maximum number of results to return.
     * @return A list of users.
     */
    @Query(nativeQuery = true, value = "SELECT u.* FROM `user` AS u WHERE (u.username LIKE CONCAT('%', :query, '%') "
            + "OR u.email LIKE CONCAT('%', :query, '%')) AND u.id IN (SELECT p.user_id FROM participant AS p WHERE "
            + "p.experiment_id = :id) LIMIT :limit")
    List<UserProjection> findDeleteParticipantSuggestions(@Param("query") String query, @Param("id") int experiment,
                                                          @Param("limit") int limit);

    /**
     * Returns a list of the first users up to the given limit whose email or username contain the given query value and
     * who are not yet participating in the course with the given id.
     *
     * @param query The username or email to search for.
     * @param course The id of the course.
     * @param limit The maximum number of results to return.
     * @return A list of {@link UserProjection}s.
     */
    @Query(nativeQuery = true, value = "SELECT u.* FROM `user` AS u WHERE (u.username LIKE CONCAT('%', :query, '%') "
            + "OR u.email LIKE CONCAT('%', :query, '%')) AND u.role = 'PARTICIPANT' AND u.id NOT IN "
            + "(SELECT p.user_id FROM course_participant AS p WHERE p.course_id = :id) LIMIT :limit")
    List<UserProjection> findCourseParticipantSuggestions(@Param("query") String query, @Param("id") int course,
                                                          @Param("limit") int limit);

    /**
     * Returns a list of the first users up to the given limit whose email or username contain the given query value who
     * are participating in the given course.
     *
     * @param query The username or email to search for.
     * @param course The id of the course.
     * @param limit The maximum number of results to return.
     * @return A list of {@link UserProjection}s.
     */
    @Query(nativeQuery = true, value = "SELECT u.* FROM `user` AS u WHERE (u.username LIKE CONCAT('%', :query, '%') "
            + "OR u.email LIKE CONCAT('%', :query, '%')) AND u.id IN (SELECT p.user_id FROM course_participant AS p "
            + "WHERE p.course_id = :id) LIMIT :limit")
    List<UserProjection> findDeleteCourseParticipantSuggestions(@Param("query") String query, @Param("id") int course,
                                                                @Param("limit") int limit);

    /**
     * Returns an optional {@link UserProjection} containing information on the username starting with the given name
     * value and ending with a digit, if existent. If more than one match is found, the one that occurs last in
     * alphabetical order is returned.
     *
     * @param name The username pattern to search for.
     * @return The user projection containing information or an empty {@link Optional}.
     */
    @Query(nativeQuery = true, value = "SELECT u.* FROM `user` AS u WHERE u.username LIKE CONCAT(:name, '%') AND "
            + "RIGHT(u.username, 1) IN (0, 1, 2, 3, 4, 5, 6, 7, 8, 9) ORDER BY LENGTH(u.username) DESC, u.username "
            + "DESC LIMIT 1")
    Optional<UserProjection> findLastUsername(@Param("name") String name);

}
