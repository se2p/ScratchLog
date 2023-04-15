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

import fim.unipassau.de.scratchLog.persistence.entity.Course;
import fim.unipassau.de.scratchLog.persistence.entity.CourseParticipant;
import fim.unipassau.de.scratchLog.persistence.entity.CourseParticipantId;
import fim.unipassau.de.scratchLog.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * A repository providing functionality for retrieving participant information for a course.
 */
public interface CourseParticipantRepository extends JpaRepository<CourseParticipant, CourseParticipantId> {

    /**
     * Checks, whether a course participation for the given course and user exists in the database.
     *
     * @param course The {@link Course} to search for.
     * @param user The {@link User} to search for.
     * @return {@code true} iff an entry already exists.
     */
    boolean existsByCourseAndUser(Course course, User user);

    /**
     * Returns a list of all {@link CourseParticipant}s containing the ids of all users participating in the given
     * course.
     *
     * @param course The {@link Course} to search for.
     * @return A list of all course participants.
     */
    List<CourseParticipant> findAllByCourse(Course course);

    /**
     * Returns a page of {@link CourseParticipant}s for the given course, if any entries exist.
     *
     * @param course The {@link Course} to search for.
     * @param pageable The {@link Pageable} to use.
     * @return The course participant page.
     */
    Page<CourseParticipant> findAllByCourse(Course course, Pageable pageable);

    /**
     * Returns the number of course participant rows for the given course.
     *
     * @param id The course id to search for.
     * @return The number of rows.
     */
    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM course_participant AS c WHERE c.course_id = :id")
    int getCourseParticipantRowCount(@Param("id") int id);

}
