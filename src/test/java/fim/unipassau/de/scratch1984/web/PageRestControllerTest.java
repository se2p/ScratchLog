package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.service.PageService;
import fim.unipassau.de.scratch1984.util.enums.Language;
import fim.unipassau.de.scratch1984.util.enums.Role;
import fim.unipassau.de.scratch1984.web.controller.PageRestController;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PageRestControllerTest {

    @InjectMocks
    private PageRestController pageRestController;

    @Mock
    private PageService pageService;

    @Mock
    private HttpServletResponse response;

    private final String ID_STRING = "1";
    private final int ID = 1;
    private final int LAST_PAGE = 3;
    private MockedStatic<SecurityContextHolder> securityContextHolder;
    private final UserDTO userDTO = new UserDTO("participant", "email", Role.PARTICIPANT, Language.ENGLISH, "password",
            "");

    @BeforeEach
    public void setup() {
        userDTO.setId(ID);
        securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    public void cleanup() {
        securityContextHolder.close();
    }

    @Test
    public void testGetLastCourseParticipantPage() {
        when(pageService.getLastParticipantCoursePage(ID)).thenReturn(LAST_PAGE);
        assertEquals(LAST_PAGE - 1, pageRestController.getLastCourseParticipantPage(ID_STRING, response));
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(pageService).getLastParticipantCoursePage(ID);
    }

    @Test
    public void testGetLastCourseParticipantPageInvalidId() {
        assertEquals(-1, pageRestController.getLastCourseParticipantPage("0", response));
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(pageService, never()).getLastParticipantCoursePage(anyInt());
    }

    @Test
    public void testGetLastCourseExperimentPage() {
        when(pageService.getLastCourseExperimentPage(ID)).thenReturn(LAST_PAGE);
        assertEquals(LAST_PAGE - 1, pageRestController.getLastCourseExperimentPage(ID_STRING, response));
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(pageService).getLastCourseExperimentPage(ID);
    }

    @Test
    public void testGetLastCourseExperimentPageInvalidId() {
        assertEquals(-1, pageRestController.getLastCourseExperimentPage(null, response));
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(pageService, never()).getLastCourseExperimentPage(anyInt());
    }

}
