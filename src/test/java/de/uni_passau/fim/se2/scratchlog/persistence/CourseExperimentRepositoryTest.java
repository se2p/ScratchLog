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
import de.uni_passau.fim.se2.scratchlog.persistence.entity.CourseExperiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.CourseExperimentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CourseExperimentRepositoryTest extends AbstractScratchLogTest {

    private static final LocalDateTime DATE = LocalDateTime.now();

    @Autowired
    private CourseExperimentRepository courseExperimentRepository;

    private Course course1;
    private Course course2;
    private Course course3;

    @BeforeEach
    void setUp() {
        course1 = entityUtilService.generateCourse("Course 1");
        course2 = entityUtilService.generateCourse("Course 2");
        course3 = entityUtilService.generateCourse("Course 3");

        final Experiment experiment1 = entityUtilService.generateExperiment("Experiment 1");
        final Experiment experiment2 = entityUtilService.generateExperiment("Experiment 2");
        final Experiment experiment3 = entityUtilService.generateExperiment("Experiment 3");

        final CourseExperiment courseExperiment1 = new CourseExperiment(course1, experiment1, DATE);
        final CourseExperiment courseExperiment2 = new CourseExperiment(course1, experiment2, DATE);
        final CourseExperiment courseExperiment3 = new CourseExperiment(course2, experiment3, DATE);

        courseExperimentRepository.saveAll(List.of(courseExperiment1, courseExperiment2, courseExperiment3));
    }

    @Test
    void testGetCourseExperimentRowCount() {
        assertAll(
                () -> assertEquals(2, courseExperimentRepository.getCourseExperimentRowCount(course1.getId())),
                () -> assertEquals(1, courseExperimentRepository.getCourseExperimentRowCount(course2.getId())),
                () -> assertEquals(0, courseExperimentRepository.getCourseExperimentRowCount(course3.getId()))
        );
    }

}
