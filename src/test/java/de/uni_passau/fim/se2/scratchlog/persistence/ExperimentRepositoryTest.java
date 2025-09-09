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
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.ExperimentTableProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.CourseRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExperimentRepositoryTest extends AbstractScratchLogTest {

    @Autowired
    private ExperimentRepository repository;

    @Autowired
    private CourseRepository courseRepository;

    private static final String SHORT_QUERY = "Exp";
    private static final String TITLE_QUERY = "Experiment";
    private static final String NO_RESULTS = "description";
    private static final int LIMIT = 5;
    private static final int SMALL_LIMIT = 2;

    private PageRequest pageRequest;

    private User user = new User("user", "email", Role.PARTICIPANT, Language.ENGLISH, "password", "secret");

    private Course course1;
    private Course course2;

    private Experiment experiment1;
    private Experiment experiment2;
    private Experiment experiment3;
    private Experiment experiment4;
    private Experiment experiment5;
    private Experiment experiment6;
    @Autowired
    private ExperimentRepository experimentRepository;

    @BeforeEach
    void setup() {
        pageRequest = PageRequest.of(0, Constants.PAGE_SIZE);

        user = entityUtilService.generateUser("user");

        course1 = entityUtilService.generateCourse("Course 1");
        course1.setActive(true);
        course1 = courseRepository.save(course1);
        course2 = entityUtilService.generateCourse("Course 2");
        course2.setActive(true);
        course2 = courseRepository.save(course2);

        experiment1 = entityUtilService.addExperimentToCourse(course1, "Experiment 1").getExperiment();
        experiment2 = entityUtilService.addExperimentToCourse(course1, "Experiment 2").getExperiment();
        experiment3 = entityUtilService.addExperimentToCourse(course2, "Experiment 3").getExperiment();
        experiment4 = entityUtilService.generateExperiment("Experiment 4");
        experiment5 = entityUtilService.generateExperiment("Exp 5");
        experiment6 = entityUtilService.generateExperiment("Exp 6");

        entityUtilService.addUsersToExperiment(experiment1, user);
        entityUtilService.addUsersToExperiment(experiment2, user);
        entityUtilService.addUsersToExperiment(experiment3, user);
        entityUtilService.addUsersToExperiment(experiment4, user);
    }

    @AfterEach
    void tearDown() {
        courseRepository.deleteAll();
        experimentRepository.deleteAll();
    }

    @Test
    void testUpdateStatusById() {
        repository.updateStatusById(experiment1.getId(), true);
        final Experiment experiment = repository.findById(experiment1.getId()).orElseThrow();
        assertTrue(experiment.isActive());
    }

    @Test
    void testUpdateStatusByIdFalse() {
        repository.updateStatusById(experiment2.getId(), false);
        final Experiment experiment = repository.findById(experiment2.getId()).orElseThrow();
        assertFalse(experiment.isActive());
    }

    @Test
    void testFindExperimentSuggestions() {
        List<ExperimentTableProjection> experiments = repository.findExperimentSuggestions(SHORT_QUERY, LIMIT);
        assertAll(
                () -> assertEquals(5, experiments.size()),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment1.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment2.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment3.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment4.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment5.getTitle())))
        );
    }

    @Test
    void testFindExperimentSuggestionsLessThan5() {
        List<ExperimentTableProjection> experiments = repository.findExperimentSuggestions(TITLE_QUERY, LIMIT);
        assertAll(
                () -> assertEquals(4, experiments.size()),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment1.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment2.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment3.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment4.getTitle()))),
                () -> assertFalse(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment5.getTitle())))
        );
    }

    @Test
    void testFindExperimentSuggestionsNoResults() {
        List<ExperimentTableProjection> experiments = repository.findExperimentSuggestions(NO_RESULTS, LIMIT);
        assertTrue(experiments.isEmpty());
    }

    @Test
    void testFindCourseExperimentSuggestions() {
        List<ExperimentTableProjection> experiments = repository.findCourseExperimentSuggestions(TITLE_QUERY,
                course1.getId(), LIMIT);
        assertAll(
                () -> assertEquals(2, experiments.size()),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment3.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment4.getTitle())))
        );
    }

    @Test
    void testFindCourseExperimentSuggestionsLimit() {
        List<ExperimentTableProjection> experiments = repository.findCourseExperimentSuggestions(SHORT_QUERY,
                course2.getId(), SMALL_LIMIT);
        assertAll(
                () -> assertEquals(2, experiments.size()),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment2.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment1.getTitle())))
        );
    }

    @Test
    void testFindCourseExperimentSuggestionsNoResults() {
        List<ExperimentTableProjection> experiments = repository.findCourseExperimentSuggestions(NO_RESULTS,
                course1.getId(), LIMIT);
        assertTrue(experiments.isEmpty());
    }

    @Test
    void testFindCourseExperimentDeleteSuggestions() {
        List<ExperimentTableProjection> experiments = repository.findCourseExperimentDeleteSuggestions(TITLE_QUERY,
                course1.getId(), LIMIT);
        assertAll(
                () -> assertEquals(2, experiments.size()),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment1.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment2.getTitle())))
        );
    }

    @Test
    void testFindCourseExperimentDeleteSuggestionsNoResults() {
        List<ExperimentTableProjection> experiments = repository.findCourseExperimentDeleteSuggestions(NO_RESULTS,
                course1.getId(), LIMIT);
        assertTrue(experiments.isEmpty());
    }

    @Test
    void testFindExperimentResults() {
        List<ExperimentTableProjection> experiments = repository.findExperimentResults(SHORT_QUERY, LIMIT, 0);
        assertAll(
                () -> assertEquals(5, experiments.size()),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment1.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment2.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment3.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment4.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment5.getTitle())))
        );
    }

    @Test
    void testFindExperimentResultsOffset() {
        List<ExperimentTableProjection> experiments = repository.findExperimentResults(SHORT_QUERY, LIMIT, 2);
        assertAll(
                () -> assertEquals(4, experiments.size()),
                () -> assertFalse(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment1.getTitle()))),
                () -> assertFalse(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment2.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment3.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment4.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment5.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment6.getTitle())))
        );
    }

    @Test
    void testFindExperimentResultsAll() {
        List<ExperimentTableProjection> experiments = repository.findExperimentResults(SHORT_QUERY,
                Constants.PAGE_SIZE, 0);
        assertAll(
                () -> assertEquals(6, experiments.size()),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment1.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment2.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment3.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment4.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment5.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment6.getTitle())))
        );
    }

    @Test
    void testFindExperimentResultsNoResults() {
        assertTrue(repository.findExperimentSuggestions(NO_RESULTS, LIMIT).isEmpty());
    }

    @Test
    void testGetExperimentResultCount() {
        assertEquals(6, repository.getExperimentResultsCount(SHORT_QUERY));
    }

    @Test
    void testGetExperimentResultCount4() {
        assertEquals(4, repository.getExperimentResultsCount(TITLE_QUERY));
    }

    @Test
    void testGetExperimentResultCountZero() {
        assertEquals(0, repository.getExperimentResultsCount(NO_RESULTS));
    }

    @Test
    void testFindExperimentsByParticipant() {
        Page<ExperimentTableProjection> projections = repository.findExperimentsByParticipant(user.getId(),
                pageRequest);
        assertAll(
                () -> assertEquals(4, projections.getNumberOfElements()),
                () -> assertTrue(projections.stream().anyMatch(e -> Objects.equals(e.getId(), experiment1.getId()))),
                () -> assertTrue(projections.stream().anyMatch(e -> Objects.equals(e.getId(), experiment2.getId()))),
                () -> assertTrue(projections.stream().anyMatch(e -> Objects.equals(e.getId(), experiment3.getId()))),
                () -> assertTrue(projections.stream().anyMatch(e -> Objects.equals(e.getId(), experiment4.getId())))
        );
    }

    @Test
    void testFindExperimentsByParticipantPageSizeTooSmall() {
        pageRequest = PageRequest.of(0, 3);
        Page<ExperimentTableProjection> projections = repository.findExperimentsByParticipant(user.getId(),
                pageRequest);
        assertAll(
                () -> assertEquals(3, projections.getNumberOfElements()),
                () -> assertTrue(projections.stream().anyMatch(e -> Objects.equals(e.getId(), experiment1.getId()))),
                () -> assertTrue(projections.stream().anyMatch(e -> Objects.equals(e.getId(), experiment2.getId()))),
                () -> assertTrue(projections.stream().anyMatch(e -> Objects.equals(e.getId(), experiment3.getId()))),
                () -> assertFalse(projections.stream().anyMatch(e -> Objects.equals(e.getId(), experiment4.getId())))
        );
    }

    @Test
    void testFindExperimentsByParticipantNoUser() {
        Page<ExperimentTableProjection> projections = repository.findExperimentsByParticipant(5, pageRequest);
        assertEquals(0, projections.getNumberOfElements());
    }

    @Test
    void testGetParticipantPageCount() {
        assertEquals(4, repository.getParticipantPageCount(user.getId()));
    }

    @Test
    void testGetParticipantPageCountZero() {
        assertEquals(0, repository.getParticipantPageCount(5));
    }
}
