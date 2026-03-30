package de.uni_passau.fim.se2.scratchlog.application;

import de.uni_passau.fim.se2.scratchlog.AbstractScratchLogTest;
import de.uni_passau.fim.se2.scratchlog.application.service.CourseService;
import de.uni_passau.fim.se2.scratchlog.application.service.UserService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Course;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.CourseExperiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.CourseParticipant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Participant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.CourseParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.testing_utils.DtoUtil;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
public class CourseServiceTest2 extends AbstractScratchLogTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseParticipantRepository courseParticipantRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    private Course course;
    private List<UserDTO> userDTOs;

    @BeforeEach
    public void setup() {
        course = entityUtilService.generateCourse("Test Course");
        userDTOs = List.of(DtoUtil.generateUserDTO("Test User 1"), DtoUtil.generateUserDTO("Test User 2"), DtoUtil.generateUserDTO("Test User 3"));
    }

    @Test
    public void testSaveCourseParticipants() {
        CourseExperiment courseExperiment = entityUtilService.addExperimentToCourse(course, "Test Experiment");
        userService.saveUsers(userDTOs);
        courseService.saveCourseParticipants(course.getId(), userDTOs, false);
        List<CourseParticipant> participants = courseParticipantRepository.findAllByCourse(course);
        assertThat(participants)
            .extracting(p -> p.getUser().getUsername())
            .containsExactlyInAnyOrderElementsOf(userDTOs.stream().map(UserDTO::getUsername).toList());
        // Assert that users were not added to course experiments.
        List<Participant> expParticipants = participantRepository.findAllByExperiment(courseExperiment.getExperiment());
        assertThat(expParticipants).isEmpty();
    }

    @Test
    public void testSaveCourseParticipantsAddToExperiments() {
        userService.saveUsers(userDTOs);
        CourseExperiment courseExperiment = entityUtilService.addExperimentToCourse(course, "Test Experiment");
        courseService.saveCourseParticipants(course.getId(), userDTOs, true);
        List<Participant> expParticipants = participantRepository.findAllByExperiment(courseExperiment.getExperiment());
        assertThat(expParticipants)
            .extracting(Participant::getUser)
            .noneMatch(u -> u.getSecret().isEmpty()) // Assert that secrets were set.
            .extracting(User::getUsername)
            .containsExactlyInAnyOrderElementsOf(userDTOs.stream().map(UserDTO::getUsername).toList());
    }

    @Test
    public void testSaveCourseParticipantsAdmin() {
        userDTOs.getFirst().setRole(Role.ADMIN);
        userService.saveUsers(userDTOs);
        assertThrows(IllegalArgumentException.class,
            () -> courseService.saveCourseParticipants(course.getId(), userDTOs, false));
    }

    @Test
    public void testSaveCourseParticipantsUsersDontExist() {
        // Users from userDTOs are not persisted.
        assertThrows(EntityNotFoundException.class,
            () -> courseService.saveCourseParticipants(course.getId(), userDTOs, false));
    }

    @Test
    public void testSaveCourseParticipantsInvalidCourse() {
        assertThrows(EntityNotFoundException.class, () -> courseService.saveCourseParticipants(-1, userDTOs, false));
    }

}
