/*
 * This file is part of ScratchLog.
 * Licenced under the GPL v3.0 or later.
 *
 * SPDX-FileCopyrightText: 2021-2026 Scratchlog contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.uni_passau.fim.se2.scratchlog.application;

import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.service.PageService;
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
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PageServiceTest {

    @InjectMocks
    private PageService pageService;

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private ExperimentDataRepository experimentDataRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseParticipantRepository courseParticipantRepository;

    @Mock
    private CourseExperimentRepository courseExperimentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BlockEventRepository blockEventRepository;

    private static final int ID = 1;
    private final ExperimentData experimentData = new ExperimentData(ID, 5, 3, 2);
    private final Course course = new Course(ID, "My course", "Description", "no", false, LocalDateTime.now());
    private final int pageNumber = 0;
    private final PageRequest pageRequest = PageRequest.of(pageNumber, Constants.PAGE_SIZE);
    private Page<ExperimentTableProjection> experimentPage;
    private Page<CourseTableProjection> coursePage;
    private Page<CourseExperimentProjection> courseExperimentPage;
    private Page<CourseParticipant> courseParticipantPage;
    private final List<Participant> participantList = getParticipants(5);
    private final Page<Participant> participants = new PageImpl<>(participantList);
    private final User user = new User("participant", "email", Role.PARTICIPANT, Language.GERMAN, "password", "secret");
    private final Experiment experiment = new Experiment(ID, "title", "description", "info", "postscript", true,
        false, "scratch");
    private final Page<BlockEventProjection> blockEventProjections = new PageImpl<>(getBlockEventProjections(5));

    @Test
    public void testGetExperimentPage() {
        List<ExperimentTableProjection> experiments = getExperimentProjections(5);
        experimentPage = new PageImpl<>(experiments);
        when(experimentRepository.findAllProjectedBy(any(PageRequest.class))).thenReturn(experimentPage);
        Page<ExperimentTableProjection> getPage = pageService.getExperimentPage(pageNumber);
        assertAll(
                () -> assertEquals(experimentPage.getTotalElements(), getPage.getTotalElements()),
                () -> assertEquals(experimentPage.stream().findFirst(), getPage.stream().findFirst()),
                () -> assertEquals(experimentPage.getSize(), getPage.getSize())
        );
        verify(experimentRepository).findAllProjectedBy(any(PageRequest.class));
    }

    @Test
    public void testGetExperimentPageEmpty() {
        experimentPage = new PageImpl<>(new ArrayList<>());
        when(experimentRepository.findAllProjectedBy(any(PageRequest.class))).thenReturn(experimentPage);
        Page<ExperimentTableProjection> getPage = pageService.getExperimentPage(pageNumber);
        assertTrue(getPage.isEmpty());
        verify(experimentRepository).findAllProjectedBy(any(PageRequest.class));
    }


    @Test
    public void testGetCoursePage() {
        List<CourseTableProjection> courses = getCourseProjections(3);
        coursePage = new PageImpl<>(courses);
        when(courseRepository.findAllProjectedBy(any(PageRequest.class))).thenReturn(coursePage);
        Page<CourseTableProjection> getPage = pageService.getCoursePage(pageNumber);
        assertAll(
                () -> assertEquals(coursePage.getTotalElements(), getPage.getTotalElements()),
                () -> assertEquals(coursePage.stream().findFirst(), getPage.stream().findFirst()),
                () -> assertEquals(coursePage.getSize(), getPage.getSize())
        );
        verify(courseRepository).findAllProjectedBy(any(PageRequest.class));
    }


    @Test
    public void testGetCourseExperimentPage() {
        courseExperimentPage = new PageImpl<>(getCourseExperiments(2));
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(courseExperimentRepository.findAllProjectedByCourse(pageRequest, course)).thenReturn(courseExperimentPage);
        Page<CourseExperimentProjection> getPage = pageService.getCourseExperimentPage(ID, pageNumber);
        assertAll(
                () -> assertEquals(courseExperimentPage.getTotalElements(), getPage.getTotalElements()),
                () -> assertEquals(courseExperimentPage.stream().findFirst(), getPage.stream().findFirst()),
                () -> assertEquals(courseExperimentPage.getSize(), getPage.getSize())
        );
        verify(courseRepository).getReferenceById(ID);
        verify(courseExperimentRepository).findAllProjectedByCourse(pageRequest, course);
    }

    @Test
    public void testGetCourseExperimentPageEmpty() {
        courseExperimentPage = new PageImpl<>(new ArrayList<>());
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(courseExperimentRepository.findAllProjectedByCourse(pageRequest, course)).thenReturn(courseExperimentPage);
        assertTrue(pageService.getCourseExperimentPage(ID, pageNumber).isEmpty());
        verify(courseRepository).getReferenceById(ID);
        verify(courseExperimentRepository).findAllProjectedByCourse(pageRequest, course);
    }

    @Test
    public void testGetExperimentParticipantPage() {
        List<ExperimentTableProjection> experiments = getExperimentProjections(4);
        experimentPage = new PageImpl<>(experiments);
        when(experimentRepository.findExperimentsByParticipant(anyInt(),
                any(PageRequest.class))).thenReturn(experimentPage);
        Page<ExperimentTableProjection> getPage = pageService.getExperimentParticipantPage(pageNumber, ID);
        assertAll(
                () -> assertEquals(experimentPage.getTotalElements(), getPage.getTotalElements()),
                () -> assertEquals(experimentPage.stream().findFirst(), getPage.stream().findFirst()),
                () -> assertEquals(experimentPage.getSize(), getPage.getSize())
        );
        verify(experimentRepository).findExperimentsByParticipant(anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetCourseParticipantPage() {
        List<CourseTableProjection> courses = getCourseProjections(1);
        coursePage = new PageImpl<>(courses);
        when(courseRepository.findCoursesByParticipant(ID, pageRequest)).thenReturn(coursePage);
        Page<CourseTableProjection> getPage = pageService.getCourseParticipantPage(ID, pageNumber);
        assertAll(
                () -> assertEquals(coursePage.getTotalElements(), getPage.getTotalElements()),
                () -> assertEquals(coursePage.stream().findFirst(), getPage.stream().findFirst()),
                () -> assertEquals(coursePage.getSize(), getPage.getSize())
        );
        verify(courseRepository).findCoursesByParticipant(ID, pageRequest);
    }

    @Test
    public void testGetParticipantPage() {
        when(participantRepository.findAllByExperiment(any(), any(PageRequest.class))).thenReturn(participants);
        assertEquals(participants, pageService.getParticipantPage(ID, pageNumber));
        verify(participantRepository).findAllByExperiment(any(), any(PageRequest.class));
        verify(experimentRepository).getReferenceById(ID);
    }

    @Test
    public void testGetParticipantPageNotFound() {
        when(participantRepository.findAllByExperiment(any(), any(PageRequest.class)))
                .thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> pageService.getParticipantPage(ID, pageNumber)
        );
        verify(participantRepository).findAllByExperiment(any(), any(PageRequest.class));
        verify(experimentRepository).getReferenceById(ID);
    }

    @Test
    public void testGetParticipantCoursePage() {
        List<CourseParticipant> participants = getCourseParticipants(3);
        courseParticipantPage = new PageImpl<>(participants);
        when(courseRepository.getReferenceById(ID)).thenReturn(course);
        when(courseParticipantRepository.findAllByCourse(any(),
                any(PageRequest.class))).thenReturn(courseParticipantPage);
        assertEquals(courseParticipantPage, pageService.getParticipantCoursePage(ID, pageNumber));
        verify(courseRepository).getReferenceById(ID);
        verify(courseParticipantRepository).findAllByCourse(any(), any(PageRequest.class));
    }

    @Test
    public void testGetParticipantCoursePageNotFound() {
        when(courseParticipantRepository.findAllByCourse(any(),
                any(PageRequest.class))).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> pageService.getParticipantCoursePage(ID, pageNumber)
        );
        verify(courseRepository).getReferenceById(ID);
        verify(courseParticipantRepository).findAllByCourse(any(), any(PageRequest.class));
    }

    @Test
    public void testGetLastExperimentPage() {
        when(experimentRepository.count()).thenReturn((long) Constants.PAGE_SIZE);
        assertEquals(1, pageService.getLastExperimentPage());
        verify(experimentRepository).count();
    }

    @Test
    public void testGetLastExperimentPage5() {
        when(experimentRepository.count()).thenReturn((long) 50);
        assertEquals(5, pageService.getLastExperimentPage());
        verify(experimentRepository).count();
    }

    @Test
    public void testGetLastExperimentPage6() {
        when(experimentRepository.count()).thenReturn((long) 51);
        assertEquals(6, pageService.getLastExperimentPage());
        verify(experimentRepository).count();
    }

    @Test
    public void testGetLastCoursePage() {
        when(courseRepository.count()).thenReturn((long) Constants.PAGE_SIZE);
        assertEquals(1, pageService.getLastCoursePage());
        verify(courseRepository).count();
    }

    @Test
    public void testGetLastCourseExperimentPage() {
        when(courseExperimentRepository.getCourseExperimentRowCount(ID)).thenReturn(51);
        assertEquals(6, pageService.getLastCourseExperimentPage(ID));
        verify(courseExperimentRepository).getCourseExperimentRowCount(ID);
    }

    @Test
    public void testGetLastExperimentPageForUser() {
        when(experimentRepository.getParticipantPageCount(ID)).thenReturn(Constants.PAGE_SIZE);
        assertEquals(1, pageService.getLastExperimentPageForUser(ID));
        verify(experimentRepository).getParticipantPageCount(ID);
    }

    @Test
    public void testGetLastCoursePageForUser() {
        when(courseRepository.getParticipantPageCount(ID)).thenReturn(Constants.PAGE_SIZE);
        assertEquals(1, pageService.getLastCoursePageForUser(ID));
        verify(courseRepository).getParticipantPageCount(ID);
    }

    @Test
    public void testGetLastParticipantPage() {
        when(experimentDataRepository.findByExperiment(ID)).thenReturn(Optional.of(experimentData));
        assertEquals(0, pageService.getLastParticipantPage(ID));
        verify(experimentDataRepository).findByExperiment(ID);
    }

    @Test
    public void testGetLastParticipantPage3() {
        experimentData.setParticipants(40);
        when(experimentDataRepository.findByExperiment(ID)).thenReturn(Optional.of(experimentData));
        assertEquals(3, pageService.getLastParticipantPage(ID));
        verify(experimentDataRepository).findByExperiment(ID);
    }

    @Test
    public void testGetLastParticipantPage4() {
        experimentData.setParticipants(41);
        when(experimentDataRepository.findByExperiment(ID)).thenReturn(Optional.of(experimentData));
        assertEquals(4, pageService.getLastParticipantPage(ID));
        verify(experimentDataRepository).findByExperiment(ID);
    }

    @Test
    public void testGetLastParticipantPageNull() {
        assertEquals(0, pageService.getLastParticipantPage(ID));
        verify(experimentDataRepository).findByExperiment(ID);
    }

    @Test
    public void testGetLastParticipantCoursePage() {
        when(courseParticipantRepository.getCourseParticipantRowCount(ID)).thenReturn(Constants.PAGE_SIZE + 1);
        assertEquals(2, pageService.getLastParticipantCoursePage(ID));
        verify(courseParticipantRepository).getCourseParticipantRowCount(ID);
    }

    @Test
    public void testGetCodesForUser() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByUserAndExperimentAndXmlIsNotNull(any(), any(),
            any(PageRequest.class))).thenReturn(blockEventProjections);
        Page<BlockEventProjection> page = pageService.getPaginatedCodesForUser(ID, ID, pageNumber);
        assertAll(
            () -> assertEquals(blockEventProjections.getTotalElements(), page.getTotalElements()),
            () -> assertEquals(blockEventProjections.stream().findFirst(), page.stream().findFirst()),
            () -> assertEquals(blockEventProjections.getSize(), page.getSize())
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByUserAndExperimentAndXmlIsNotNull(any(), any(), any(PageRequest.class));
    }

    @Test
    public void testGetCodesForUserEntityNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(blockEventRepository.findAllByUserAndExperimentAndXmlIsNotNull(any(), any(),
            any(PageRequest.class))).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
            () -> pageService.getPaginatedCodesForUser(ID, ID, pageNumber)
        );
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(blockEventRepository).findAllByUserAndExperimentAndXmlIsNotNull(any(), any(), any(PageRequest.class));
    }

    private List<ExperimentTableProjection> getExperimentProjections(int number) {
        List<ExperimentTableProjection> experiments = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            int finalI = i;
            ExperimentTableProjection projection = new ExperimentTableProjection() {
                @Override
                public Integer getId() {
                    return finalI;
                }

                @Override
                public String getTitle() {
                    return "Experiment " + finalI;
                }

                @Override
                public String getDescription() {
                    return "Description for experiment " + finalI;
                }

                @Override
                public boolean isActive() {
                    return false;
                }
            };
            experiments.add(projection);
        }
        return experiments;
    }

    private List<CourseExperimentProjection> getCourseExperiments(int number) {
        List<CourseExperimentProjection> experiments = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            int id = i + 1;
            CourseExperimentProjection projection = () -> new ExperimentTableProjection() {
                @Override
                public Integer getId() {
                    return id;
                }

                @Override
                public String getTitle() {
                    return "Experiment " + id;
                }

                @Override
                public String getDescription() {
                    return "Some description";
                }

                @Override
                public boolean isActive() {
                    return false;
                }
            };
            experiments.add(projection);
        }
        return experiments;
    }

    private List<CourseTableProjection> getCourseProjections(int number) {
        List<CourseTableProjection> courses = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            int finalI = i;
            CourseTableProjection projection = new CourseTableProjection() {
                @Override
                public Integer getId() {
                    return finalI;
                }

                @Override
                public String getTitle() {
                    return "Course" + finalI;
                }

                @Override
                public String getDescription() {
                    return "Description for course " + finalI;
                }

                @Override
                public boolean isActive() {
                    return false;
                }
            };
            courses.add(projection);
        }
        return courses;
    }

    private List<CourseParticipant> getCourseParticipants(int number) {
        List<CourseParticipant> participants = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            User user = new User();
            user.setId(i + 1);
            Course course = new Course();
            course.setId(i + 1);
            participants.add(new CourseParticipant(user, course, LocalDateTime.now()));
        }
        return participants;
    }

    private List<Participant> getParticipants(int number) {
        List<Participant> participants = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            User user = new User();
            user.setId(i + 1);
            Experiment experiment = new Experiment();
            experiment.setId(i + 1);
            experiment.setTitle("Title " + i);
            participants.add(new Participant(user, experiment, LocalDateTime.now(), null));
        }
        return participants;
    }

    private List<BlockEventProjection> getBlockEventProjections(int number) {
        List<BlockEventProjection> projections = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            final int id = i;
            projections.add(new BlockEventProjection() {
                @Override
                public Integer getId() {
                    return id;
                }

                @Override
                public String getXml() {
                    return "xml" + id;
                }

                @Override
                public String getCode() {
                    return "code" + id;
                }

                @Override
                public LocalDateTime getDate() {
                    return null;
                }

                @Override
                public String getSprite() {
                    return "sprite";
                }
            });
        }
        return projections;
    }
}
