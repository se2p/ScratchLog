package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.service.PageService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.util.Constants;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PageRestControllerTest {

    @InjectMocks
    private PageRestController pageRestController;

    @Mock
    private PageService pageService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;


    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

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

    @Test
    public void testGetLastCoursePage() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastCoursePage(ID)).thenReturn(LAST_PAGE);
        assertEquals(LAST_PAGE - 1, pageRestController.getLastCoursePage(request, response));
        verify(request).isUserInRole(Constants.ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService).getLastCoursePage(ID);
    }

    @Test
    public void testGetLastCoursePageAdmin() {
        when(request.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(true);
        when(pageService.computeLastCoursePage()).thenReturn(LAST_PAGE);
        assertEquals(LAST_PAGE - 1, pageRestController.getLastCoursePage(request, response));
        verify(request).isUserInRole(Constants.ROLE_ADMIN);
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(pageService).computeLastCoursePage();
    }

    @Test
    public void testGetLastCoursePageAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(-1, pageRestController.getLastCoursePage(request, response));
        verify(request).isUserInRole(Constants.ROLE_ADMIN);
        verify(authentication).getName();
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(userService, never()).getUser(anyString());
        verify(pageService, never()).getLastCoursePage(anyInt());
    }

    @Test
    public void testGetLastCoursePageAuthenticationNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(-1, pageRestController.getLastCoursePage(request, response));
        verify(request).isUserInRole(Constants.ROLE_ADMIN);
        verify(authentication, never()).getName();
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(userService, never()).getUser(anyString());
        verify(pageService, never()).getLastCoursePage(anyInt());
    }

    @Test
    public void testGetLastExperimentPage() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPage(ID)).thenReturn(LAST_PAGE);
        assertEquals(LAST_PAGE - 1, pageRestController.getLastExperimentPage(request, response));
        verify(request).isUserInRole(Constants.ROLE_ADMIN);
        verify(authentication, times(2)).getName();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService).getLastExperimentPage(ID);
    }

    @Test
    public void testGetLastExperimentPageAdmin() {
        when(request.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(true);
        when(pageService.computeLastExperimentPage()).thenReturn(LAST_PAGE);
        assertEquals(LAST_PAGE - 1, pageRestController.getLastExperimentPage(request, response));
        verify(request).isUserInRole(Constants.ROLE_ADMIN);
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(pageService).computeLastExperimentPage();
    }

    @Test
    public void testGetLastExperimentPageNoAuthentication() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(-1, pageRestController.getLastExperimentPage(request, response));
        verify(request).isUserInRole(Constants.ROLE_ADMIN);
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(pageService, never()).getLastExperimentPage(anyInt());
    }

}
