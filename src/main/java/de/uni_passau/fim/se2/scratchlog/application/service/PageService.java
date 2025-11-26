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

package de.uni_passau.fim.se2.scratchlog.application.service;

import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Course;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.CourseParticipant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.ExperimentData;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Participant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.BlockEventProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.CourseExperimentProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.CourseTableProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.ExperimentTableProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.BlockEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.CourseExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.CourseParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.CourseRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentDataRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * A service providing methods related to loading information to be displayed in tables with multiple pages.
 */
@Service
public class PageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageService.class);

    private final ExperimentRepository experimentRepository;

    private final ExperimentDataRepository experimentDataRepository;

    private final ParticipantRepository participantRepository;

    private final CourseRepository courseRepository;

    private final CourseParticipantRepository courseParticipantRepository;

    private final CourseExperimentRepository courseExperimentRepository;

    private final UserRepository userRepository;

    private final BlockEventRepository blockEventRepository;

    @Autowired
    public PageService(final ExperimentRepository experimentRepository,
                       final ExperimentDataRepository experimentDataRepository,
                       final ParticipantRepository participantRepository,
                       final CourseRepository courseRepository,
                       final CourseParticipantRepository courseParticipantRepository,
                       final CourseExperimentRepository courseExperimentRepository,
                       final UserRepository userRepository,
                       final BlockEventRepository blockEventRepository) {
        this.experimentRepository = experimentRepository;
        this.experimentDataRepository = experimentDataRepository;
        this.participantRepository = participantRepository;
        this.courseRepository = courseRepository;
        this.courseParticipantRepository = courseParticipantRepository;
        this.courseExperimentRepository = courseExperimentRepository;
        this.userRepository = userRepository;
        this.blockEventRepository = blockEventRepository;
    }

    /**
     * Returns an experiment {@link Page} of the given page number.
     * If {@code page} is < 0, returns the first page. If it's greater than the last page number, returns the last page.
     *
     * @param page The page number to retrieve.
     * @return The experiment page.
     */
    public Page<ExperimentTableProjection> getExperimentPage(final int page) {
        Pageable pageable = pageNumberToClampedPageRequest(page, getLastExperimentPage())
            .withSort(Sort.by("id").descending());
        Page<ExperimentTableProjection> experiments = experimentRepository.findAllProjectedBy(pageable);

        if (experiments.isEmpty()) {
            LOGGER.info(
                "Could not find any experiments for the page with page size {}, current page: {} and offset {}!",
                pageable.getPageSize(), pageable.getPageNumber(), pageable.getOffset()
            );
        }

        return experiments;
    }

    /**
     * Returns a courses {@link Page} of the given page number.
     * If {@code page} is < 0, returns the first page. If it's greater than the last page number, returns the last page.
     *
     * @param page The page number to retrieve.
     * @return The course page.
     */
    public Page<CourseTableProjection> getCoursePage(final int page) {
        Pageable pageable = pageNumberToClampedPageRequest(page, getLastCoursePage())
            .withSort(Sort.by("id").descending());
        Page<CourseTableProjection> courses = courseRepository.findAllProjectedBy(pageable);

        if (courses.isEmpty()) {
            LOGGER.info(
                "Could not find any courses for the page with page size of {}, current page of {} and offset of {}!",
                pageable.getPageSize(), pageable.getPageNumber(), pageable.getOffset()
            );
        }

        return courses;
    }

    /**
     * Returns the {@link Page} of {@link CourseExperimentProjection}s with the given page number.
     * If {@code page} is < 0, returns the first page. If it's greater than the last page number, returns the last page.
     *
     * @param page The page number to retrieve.
     * @param courseId The id of the course.
     * @return The course experiment page.
     */
    public Page<CourseExperimentProjection> getCourseExperimentPage(final int courseId, final int page) {
        Course course = courseRepository.getReferenceById(courseId);

        Pageable pageable = pageNumberToClampedPageRequest(page, getLastCourseExperimentPage(courseId));
        Page<CourseExperimentProjection> experiments = courseExperimentRepository.findAllProjectedByCourse(pageable,
                course);

        if (experiments.isEmpty()) {
            LOGGER.info(
                "Could not find any course experiments for the page with size {}, current page: {} and offset {}!",
                pageable.getPageSize(), pageable.getPageNumber(), pageable.getOffset()
            );
        }

        return experiments;
    }

    /**
     * Returns the {@link Page} of {@link ExperimentTableProjection}s with the given pager number.
     * If {@code page} is < 0, returns the first page. If it's greater than the last page number, returns the last page.
     *
     * @param page the page number to retrieve.
     * @param userId The user id to search for.
     * @return The page of {@link ExperimentTableProjection}s.
     */
    public Page<ExperimentTableProjection> getExperimentParticipantPage(final int userId, final int page) {
        Pageable pageable = pageNumberToClampedPageRequest(page, getLastExperimentPageForUser(userId));
        return experimentRepository.findExperimentsByParticipant(userId, pageable);
    }

    /**
     * Returns the {@link Page} of {@link CourseTableProjection}s with the given page number.
     * If {@code page} is < 0, returns the first page. If it's greater than the last page number, returns the last page.
     *
     * @param page The page number to retrieve.
     * @param userId The user id to search for.
     * @return The page of {@link CourseTableProjection}s.
     * @throws IllegalArgumentException if the passed id is invalid.
     */
    public Page<CourseTableProjection> getCourseParticipantPage(final int userId, final int page) {
        Pageable pageable = pageNumberToClampedPageRequest(page, getLastCoursePageForUser(userId));
        return courseRepository.findCoursesByParticipant(userId, pageable);
    }

    /**
     * Retrieves a {@link Page} of participants for the experiment with the given id.
     * If {@code page} is < 0, returns the first page. If it's greater than the last page number, returns the last page.
     *
     * @param id The experiment id.
     * @param page The page number to retrieve.
     * @return The participant page.
     * @throws NotFoundException if no corresponding experiment entry could be found.
     */
    public Page<Participant> getParticipantPage(final int id, final int page) {
        Experiment experiment = experimentRepository.getReferenceById(id);

        try {
            Pageable pageable = pageNumberToClampedPageRequest(page, getLastParticipantPage(id))
                .withSort(Sort.by("user").descending());
            return participantRepository.findAllByExperiment(experiment, pageable);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find experiment with id {} in the database!", id, e);
            throw new NotFoundException("Could not find experiment with id " + id + " in the database!", e);
        }
    }

    /**
     * Retrieves a {@link Page} of {@link CourseParticipant}s for the course with the given id.
     * If {@code page} is < 0, returns the first page. If it's greater than the last page number, returns the last page.
     *
     * @param id The course id.
     * @param page The page number to retrieve.
     * @return The course participant page.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding course entry could be found.
     */
    public Page<CourseParticipant> getParticipantCoursePage(final int id, final int page) {
        Course course = courseRepository.getReferenceById(id);

        try {
            Pageable pageable = pageNumberToClampedPageRequest(page, getLastParticipantCoursePage(id))
                .withSort(Sort.by("added").descending());
            return courseParticipantRepository.findAllByCourse(course, pageable);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find course with id {} in the database!", id, e);
            throw new NotFoundException("Could not find course with id " + id + " in the database!", e);
        }
    }

    /**
     * Retrieves a {@link Page} of {@link BlockEventProjection}s for the user with the given ID during the experiment
     * with the given ID.
     *
     * @param userId The user ID.
     * @param experimentId The experiment ID.
     * @param page The page number to retrieve.
     * @return The page of block event projections.
     * @throws NotFoundException if no corresponding user or experiment could be found.
     */
    public Page<BlockEventProjection> getPaginatedCodesForUser(final int userId, final int experimentId,
                                                               final int page) {
        User user = userRepository.getReferenceById(userId);
        Experiment experiment = experimentRepository.getReferenceById(experimentId);

        try {
            return blockEventRepository.findAllByUserAndExperimentAndXmlIsNotNull(user, experiment,
                PageRequest.of(page, Constants.PAGE_SIZE, Sort.by("date").ascending()));
        } catch (EntityNotFoundException e) {
            LOGGER.error(
                "Could not find block event projections for user with id {} or experiment with id {}!",
                userId, experimentId, e
            );
            throw new NotFoundException("Could not find block event projections for user with id " + userId
                + " or experiment with id " + experimentId + "!", e);
        }
    }

    /**
     * Returns the number of the last page for the experiment pagination.
     *
     * @return The last page value.
     */
    public int getLastExperimentPage() {
        return computeLastPage((int) experimentRepository.count()) + 1;
    }

    /**
     * Returns the number of the last page for the course pagination.
     *
     * @return The last page value.
     */
    public int getLastCoursePage() {
        return computeLastPage((int) courseRepository.count()) + 1;
    }

    /**
     * Returns the number of the last experiment page for the course with the given id.
     *
     * @param courseId The id of the course.
     * @return The last page value.
     */
    public int getLastCourseExperimentPage(final int courseId) {
        int rows = courseExperimentRepository.getCourseExperimentRowCount(courseId);
        return computeLastPage(rows) + 1;
    }

    /**
     * Returns the number of the last page for the participant experiment pagination.
     *
     * @param userId The user id of the participant.
     * @return The last page value.
     */
    public int getLastExperimentPageForUser(final int userId) {
        int rows = experimentRepository.getParticipantPageCount(userId);
        return computeLastPage(rows) + 1;
    }

    /**
     * Returns the number of the last page for the participant course pagination.
     *
     * @param userId The user id of the participant.
     * @return The last page value.
     */
    public int getLastCoursePageForUser(final int userId) {
        int rows = courseRepository.getParticipantPageCount(userId);
        return computeLastPage(rows) + 1;
    }

    /**
     * Returns the number of the last page for the participant pagination for the experiment with the given id.
     *
     * @param id The experiment id to search for.
     * @return The last page value.
     */
    public int getLastParticipantPage(final int id) {
        Optional<ExperimentData> experimentData = experimentDataRepository.findByExperiment(id);

        if (experimentData.isEmpty()) {
            return 0;
        } else {
            int participants = experimentData.get().getParticipants();
            return computeLastPage(participants);
        }
    }

    /**
     * Returns the number of the last participant page for the course with the given id.
     *
     * @param id The id of the course.
     * @return The last page value.
     */
    public int getLastParticipantCoursePage(final int id) {
        int rows = courseParticipantRepository.getCourseParticipantRowCount(id);
        return computeLastPage(rows) + 1;
    }

    /**
     * Clamps the given page number between 0 and {@code lastPageNumber} and returns it as a {@link PageRequest}.
     *
     * @param pageNumber The page number to clamp.
     * @param lastPageNumber The last possible page number which will be used as the maximum for clamping.
     * @return A page request with the clamped page number and a page size of {@code Constants.PAGE_SIZE}.
     */
    private PageRequest pageNumberToClampedPageRequest(final int pageNumber, final int lastPageNumber) {
        return PageRequest.of(Math.clamp(pageNumber, 0, lastPageNumber), Constants.PAGE_SIZE);
    }

    /**
     * Returns the number of the last page for the given amount of elements.
     *
     * @param elements The number of elements.
     * @return The last page.
     */
    private int computeLastPage(final int elements) {
        if (elements <= Constants.PAGE_SIZE) {
            return 0;
        } else if (elements % Constants.PAGE_SIZE == 0) {
            return elements / Constants.PAGE_SIZE - 1;
        } else {
            return elements / Constants.PAGE_SIZE;
        }
    }

}
