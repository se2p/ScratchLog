package de.uni_passau.fim.se2.scratchlog.integration;

import de.uni_passau.fim.se2.scratchlog.AbstractScratchLogControllerTest;
import de.uni_passau.fim.se2.scratchlog.application.service.UserService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Course;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.CourseExperiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.CourseParticipant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Participant;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.CourseParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.testing_utils.DtoUtil;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

@Transactional
public class CourseControllerIntegrationTest2 extends AbstractScratchLogControllerTest {

    private static final String PARAM_ID = "id";
    private static final String ATTR_FILE_DTO = "fileDTO";
    private static final String ATTR_FILE = "file";

    private static final String VIEW_COURSE = "course";
    private static final String VIEW_COURSE_REDIRECT = "redirect:/course?id=";
    private static final String VIEW_ERROR = "redirect:/error";

    private static final int CSV_PARTICIPANTS_AMOUNT = 3;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseParticipantRepository courseParticipantRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    private Course course;
    private String courseIdString;
    private List<UserDTO> userDTOs;

    @BeforeEach
    public void setup() {
        course = entityUtilService.generateCourse("Test Course");
        courseIdString = course.getId().toString();

        userDTOs = IntStream.range(0, CSV_PARTICIPANTS_AMOUNT)
            .mapToObj(i -> "Test User " + i)
            .map(DtoUtil::generateUserDTO)
            .toList();
    }

    @Test
    public void testAddParticipantsFromCSV() throws Exception {
        List<String> usernames = prepareUsers(userDTOs);
        mvc.perform(multipart("/course/participant/add-csv")
                .file(csvForUsernames(usernames))
                .param(PARAM_ID, courseIdString))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name(VIEW_COURSE_REDIRECT + course.getId()));

        List<CourseParticipant> participants = courseParticipantRepository.findAllByCourse(course);
        assertThat(participants)
            .hasSize(CSV_PARTICIPANTS_AMOUNT)
            .extracting(p -> p.getUser().getUsername())
            .containsExactlyInAnyOrderElementsOf(usernames);
    }

    @Test
    public void testAddParticipantsFromCSVAddToExperiments() throws Exception {
        CourseExperiment courseExperiment = entityUtilService.addExperimentToCourse(course, "Test Experiment");
        List<String> usernames = prepareUsers(userDTOs);
        mvc.perform(multipart("/course/participant/add-csv")
                .file(csvForUsernames(usernames))
                .param(PARAM_ID, courseIdString)
                .param("addToExperiments", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name(VIEW_COURSE_REDIRECT + course.getId()));

        List<Participant> participants = participantRepository.findAllByExperiment(courseExperiment.getExperiment());
        assertThat(participants)
            .hasSize(CSV_PARTICIPANTS_AMOUNT)
            .extracting(p -> p.getUser().getUsername())
            .containsExactlyInAnyOrderElementsOf(usernames);
    }

    @Test
    public void testAddParticipantsFromCSVAdmin() throws Exception {
        userDTOs.getFirst().setRole(Role.ADMIN);
        List<String> usernames = prepareUsers(userDTOs);
        mvc.perform(multipart("/course/participant/add-csv")
                .file(csvForUsernames(usernames))
                .param(PARAM_ID, courseIdString))
            .andExpect(status().isOk())
            .andExpect(view().name(VIEW_COURSE))
            .andExpect(model().attributeHasFieldErrorCode(ATTR_FILE_DTO, ATTR_FILE, "invalid_usernames"));
    }

    @Test
    public void testAddParticipantsFromCSVUserDoesNotExist() throws Exception {
        mvc.perform(multipart("/course/participant/add-csv")
                .file(csvForUsernames(List.of("userthatdoesnotexist")))
                .param(PARAM_ID, courseIdString))
            .andExpect(status().isOk())
            .andExpect(view().name(VIEW_COURSE))
            .andExpect(model().attributeHasFieldErrorCode(ATTR_FILE_DTO, ATTR_FILE, "invalid_usernames"));
    }

    @Test
    public void testAddParticipantsFromCSVInvalidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(ATTR_FILE, "invalid.txt", "text/plain", new byte[]{});
        mvc.perform(multipart("/course/participant/add-csv")
                .file(file)
                .param(PARAM_ID, courseIdString))
            .andExpect(status().isOk())
            .andExpect(view().name(VIEW_COURSE))
            .andExpect(model().attributeHasFieldErrors(ATTR_FILE_DTO, ATTR_FILE));
    }

    @Test
    public void testAddParticipantsFromCSVInvalidId() throws Exception {
        mvc.perform(multipart("/course/participant/add-csv")
                .file(csvForUsernames(prepareUsers(userDTOs)))
                .param(PARAM_ID, "invalid"))
            .andExpect(status().is4xxClientError())
            .andExpect(view().name(VIEW_ERROR));
    }

    private List<String> prepareUsers(List<UserDTO> userDTOs) {
        return userService.saveUsers(userDTOs).stream().map(UserDTO::getUsername).toList();
    }

    private MockMultipartFile csvForUsernames(List<String> usernames) {
        String content = "username" + System.lineSeparator() + String.join(System.lineSeparator(), usernames);
        return new MockMultipartFile("file", "participants.csv", "text/csv", content.getBytes(StandardCharsets.UTF_8));
    }

}
