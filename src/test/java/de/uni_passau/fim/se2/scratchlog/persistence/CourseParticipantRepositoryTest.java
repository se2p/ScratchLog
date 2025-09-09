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
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.CourseParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CourseParticipantRepositoryTest extends AbstractScratchLogTest {

    @Autowired
    private CourseParticipantRepository courseParticipantRepository;

    private Course course1;
    private Course course2;
    private Course course3;

    @BeforeEach
    void setUp() {
        final List<User> users = entityUtilService.generateUsers("CPR", 3);

        course1 = entityUtilService.generateCourse("Course 1");
        course2 = entityUtilService.generateCourse("Course 2");
        course3 = entityUtilService.generateCourse("Course 3");

        entityUtilService.addUsersToCourse(course1, users.subList(0, 3));
        entityUtilService.addUsersToCourse(course2, List.of(users.getLast()));
    }

    @Test
    void testGetCourseParticipantRowCount() {
        assertAll(
                () -> assertEquals(3, courseParticipantRepository.getCourseParticipantRowCount(course1.getId())),
                () -> assertEquals(1, courseParticipantRepository.getCourseParticipantRowCount(course2.getId())),
                () -> assertEquals(0, courseParticipantRepository.getCourseParticipantRowCount(course3.getId()))
        );
    }

}
