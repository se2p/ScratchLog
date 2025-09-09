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
import de.uni_passau.fim.se2.scratchlog.persistence.projection.CourseTableProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.CourseRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseRepositoryTest extends AbstractScratchLogTest {

    @Autowired
    private CourseRepository courseRepository;

    private static final String SHORT_QUERY = "Course";
    private static final String TITLE_QUERY = "Course 1";
    private static final String NO_RESULTS = "blubb";
    private static final int LIMIT = 3;
    private static final int SEARCH_LIMIT = 5;

    private PageRequest pageRequest;

    private User user1;
    private User user2;

    private Course course1;
    private Course course2;
    private Course course3;
    private Course course4;

    @BeforeEach
    void setUp() {
        pageRequest = PageRequest.of(0, LIMIT);

        final List<User> users = entityUtilService.generateUsers("user", 2);
        user1 = users.getFirst();
        user2 = users.getLast();

        course1 = entityUtilService.generateCourse("Course 1");
        course2 = entityUtilService.generateCourse("Course 2");
        course3 = entityUtilService.generateCourse("Course 3");
        course4 = entityUtilService.generateCourse("Course 4");

        entityUtilService.addUsersToCourse(course1, List.of(user1, user2));
        entityUtilService.addUsersToCourse(course2, List.of(user1));
        entityUtilService.addUsersToCourse(course3, List.of(user1));
        entityUtilService.addUsersToCourse(course4, List.of(user1));
    }

    @AfterEach
    void tearDown() {
        courseRepository.deleteAll();
    }

    @Test
    void testFindCourseSuggestions() {
        List<CourseTableProjection> projections = courseRepository.findCourseSuggestions(SHORT_QUERY, SEARCH_LIMIT);
        assertAll(
                () -> assertEquals(4, projections.size()),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course1.getTitle()))),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course2.getTitle()))),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course3.getTitle()))),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course4.getTitle())))
        );
    }

    @Test
    void testFindCourseSuggestionsTitleQuery() {
        List<CourseTableProjection> projections = courseRepository.findCourseSuggestions(TITLE_QUERY, SEARCH_LIMIT);
        assertAll(
                () -> assertEquals(1, projections.size()),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course1.getTitle())))
        );
    }

    @Test
    void testFindCourseSuggestionsNoSuggestions() {
        assertTrue(courseRepository.findCourseSuggestions(NO_RESULTS, SEARCH_LIMIT).isEmpty());
    }

    @Test
    void testFindCourseResults() {
        List<CourseTableProjection> projections = courseRepository.findCourseResults(SHORT_QUERY, LIMIT, 0);
        assertAll(
                () -> assertEquals(3, projections.size()),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course1.getTitle()))),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course2.getTitle()))),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course3.getTitle())))
        );
    }

    @Test
    void testFindCourseResultsAll() {
        List<CourseTableProjection> projections = courseRepository.findCourseResults(SHORT_QUERY, SEARCH_LIMIT, 0);
        assertAll(
                () -> assertEquals(4, projections.size()),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course1.getTitle()))),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course2.getTitle()))),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course3.getTitle()))),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course4.getTitle())))
        );
    }

    @Test
    void testFindCourseResultsOffset() {
        List<CourseTableProjection> projections = courseRepository.findCourseResults(SHORT_QUERY, LIMIT, 2);
        assertAll(
                () -> assertEquals(2, projections.size()),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course4.getTitle()))),
                () -> assertTrue(projections.stream().anyMatch(projection
                        -> projection.getTitle().equals(course3.getTitle())))
        );
    }

    @Test
    void testFindCourseResultsNone() {
        assertTrue(courseRepository.findCourseResults(SHORT_QUERY, LIMIT, 5).isEmpty());
    }

    @Test
    void testGetCourseResultsCount() {
        assertAll(
                () -> assertEquals(4, courseRepository.getCourseResultsCount(SHORT_QUERY)),
                () -> assertEquals(1, courseRepository.getCourseResultsCount(TITLE_QUERY)),
                () -> assertEquals(0, courseRepository.getCourseResultsCount(NO_RESULTS))
        );
    }

    @Test
    void testFindCoursesByParticipant() {
        Page<CourseTableProjection> page = courseRepository.findCoursesByParticipant(user1.getId(), pageRequest);
        assertAll(
                () -> assertEquals(LIMIT, page.getNumberOfElements()),
                () -> assertTrue(page.stream().anyMatch(projection
                        -> projection.getTitle().equals(course1.getTitle()))),
                () -> assertTrue(page.stream().anyMatch(projection
                        -> projection.getTitle().equals(course2.getTitle()))),
                () -> assertTrue(page.stream().anyMatch(projection -> projection.getTitle().equals(course3.getTitle())))
        );
    }

    @Test
    void testFindCoursesByParticipantOffset() {
        pageRequest = PageRequest.of(1, LIMIT);
        Page<CourseTableProjection> page = courseRepository.findCoursesByParticipant(user1.getId(), pageRequest);
        assertAll(
                () -> assertEquals(1, page.getNumberOfElements()),
                () -> assertTrue(page.stream().anyMatch(projection -> projection.getTitle().equals(course4.getTitle())))
        );
    }

    @Test
    void testFindCoursesByParticipantTooFewEntries() {
        Page<CourseTableProjection> page = courseRepository.findCoursesByParticipant(user2.getId(), pageRequest);
        assertAll(
                () -> assertEquals(1, page.getNumberOfElements()),
                () -> assertEquals(course1.getTitle(), page.stream().findFirst().get().getTitle())
        );
    }

    @Test
    void testGetParticipantPageCount() {
        assertAll(
                () -> assertEquals(4, courseRepository.getParticipantPageCount(user1.getId())),
                () -> assertEquals(1, courseRepository.getParticipantPageCount(user2.getId()))
        );
    }

}
