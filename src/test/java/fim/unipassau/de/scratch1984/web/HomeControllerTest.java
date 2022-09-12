package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.MailServerSetter;
import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.PageService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.projection.CourseTableProjection;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentTableProjection;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.controller.HomeController;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HomeControllerTest {

    @InjectMocks
    private HomeController homeController;

    @Mock
    private ExperimentService experimentService;

    @Mock
    private PageService pageService;

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private ResourceBundle resourceBundle;

    @Mock
    private LocaleResolver localeResolver;

    private MockedStatic<SecurityContextHolder> securityContextHolder;
    private static final String GUI_URL = "scratch";
    private static final String INDEX = "index";
    private static final String INDEX_EXPERIMENT = "index::experiment_table";
    private static final String LOGIN = "login";
    private static final String PASSWORD_RESET = "password-reset";
    private static final String FINISH = "experiment-finish";
    private static final String CURRENT = "3";
    private static final String LAST = "5";
    private static final String BLANK = "   ";
    private static final String ID_STRING = "1";
    private static final String THANKS = "thanks";
    private static final String EXPERIMENTS = "experiments";
    private static final String EXPERIMENT_PAGE = "experimentPage";
    private static final String LAST_EXPERIMENT_PAGE = "lastExperimentPage";
    private static final int LAST_PAGE = 4;
    private static final int ID = 1;
    private static final ExperimentDTO experimentDTO = new ExperimentDTO(ID, "My Experiment", "description",
            "info", "postscript", true, GUI_URL);
    private static final UserDTO userDTO = new UserDTO("participant", "email", UserDTO.Role.PARTICIPANT,
            UserDTO.Language.ENGLISH, "password", "");
    private final Page<ExperimentTableProjection> experimentPage = new PageImpl<>(getExperimentProjections(5));
    private final Page<CourseTableProjection> coursePage = new PageImpl<>(getCourseTableProjections(3));

    @BeforeEach
    public void setup() {
        experimentDTO.setPostscript("postscript");
        userDTO.setId(ID);
        securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    public void cleanup() {
        securityContextHolder.close();
    }

    @Test
    public void testGetIndexPage() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(true);
        when(pageService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        when(pageService.getCoursePage(any(PageRequest.class))).thenReturn(coursePage);
        when(pageService.computeLastExperimentPage()).thenReturn(1);
        when(pageService.computeLastCoursePage()).thenReturn(1);
        assertEquals(INDEX, homeController.getIndexPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(httpServletRequest, never()).isUserInRole(Constants.ROLE_PARTICIPANT);
        verify(pageService).getExperimentPage(any(PageRequest.class));
        verify(pageService).getCoursePage(any(PageRequest.class));
        verify(pageService).computeLastExperimentPage();
        verify(pageService).computeLastCoursePage();
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetIndexPageParticipant() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(false);
        when(httpServletRequest.isUserInRole(Constants.ROLE_PARTICIPANT)).thenReturn(true);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getExperimentParticipantPage(any(PageRequest.class),
                anyInt())).thenReturn(experimentPage);
        when(pageService.getCourseParticipantPage(any(PageRequest.class), anyInt())).thenReturn(coursePage);
        when(pageService.getLastExperimentPage(userDTO.getId())).thenReturn(1);
        when(pageService.getLastCoursePage(userDTO.getId())).thenReturn(1);
        assertEquals(INDEX, homeController.getIndexPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_PARTICIPANT);
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(pageService).getCourseParticipantPage(any(PageRequest.class), anyInt());
        verify(pageService).getLastExperimentPage(userDTO.getId());
        verify(pageService).getLastCoursePage(userDTO.getId());
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetIndexPageParticipantNotFound() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(false);
        when(httpServletRequest.isUserInRole(Constants.ROLE_PARTICIPANT)).thenReturn(true);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, homeController.getIndexPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_PARTICIPANT);
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(pageService, never()).getCourseParticipantPage(any(PageRequest.class), anyInt());
        verify(pageService, never()).getLastExperimentPage(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetIndexPageParticipantAuthenticationNameNull() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(false);
        when(httpServletRequest.isUserInRole(Constants.ROLE_PARTICIPANT)).thenReturn(true);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Constants.ERROR, homeController.getIndexPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_PARTICIPANT);
        verify(userService, never()).getUser(anyString());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(pageService, never()).getLastExperimentPage(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetIndexPageParticipantAuthenticationNull() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(false);
        when(httpServletRequest.isUserInRole(Constants.ROLE_PARTICIPANT)).thenReturn(true);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(Constants.ERROR, homeController.getIndexPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_PARTICIPANT);
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(pageService, never()).getLastExperimentPage(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetIndexPageNoAdmin() {
        assertEquals(INDEX, homeController.getIndexPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).getExperimentPage(any(PageRequest.class));
        verify(pageService, never()).computeLastExperimentPage();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPage() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(true);
        when(pageService.computeLastExperimentPage()).thenReturn(LAST_PAGE);
        when(pageService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        ModelAndView mv = homeController.getNextExperimentPage(CURRENT, httpServletRequest);
        assertAll(
                () -> assertEquals(INDEX_EXPERIMENT, mv.getViewName()),
                () -> assertEquals(experimentPage, mv.getModel().get(EXPERIMENTS)),
                () -> assertEquals(LAST_PAGE, mv.getModel().get(EXPERIMENT_PAGE)),
                () -> assertEquals(LAST_PAGE, mv.getModel().get(LAST_EXPERIMENT_PAGE))
        );
        verify(httpServletRequest, times(2)).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService).computeLastExperimentPage();
        verify(pageService).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
    }

    @Test
    public void testGetNextPageInvalidCurrentEqualLast() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(true);
        when(pageService.computeLastExperimentPage()).thenReturn(LAST_PAGE);
        assertEquals(Constants.ERROR, homeController.getNextExperimentPage(LAST, httpServletRequest).getViewName());
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService).computeLastExperimentPage();
        verify(pageService, never()).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
    }

    @Test
    public void testGetNextPageUserParticipant() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPage(userDTO.getId())).thenReturn(LAST_PAGE);
        when(pageService.getExperimentParticipantPage(any(PageRequest.class),
                anyInt())).thenReturn(experimentPage);
        ModelAndView mv = homeController.getNextExperimentPage(CURRENT, httpServletRequest);
        assertAll(
                () -> assertEquals(INDEX_EXPERIMENT, mv.getViewName()),
                () -> assertEquals(experimentPage, mv.getModel().get(EXPERIMENTS)),
                () -> assertEquals(LAST_PAGE, mv.getModel().get(EXPERIMENT_PAGE)),
                () -> assertEquals(LAST_PAGE, mv.getModel().get(LAST_EXPERIMENT_PAGE))
        );
        verify(httpServletRequest, times(2)).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).computeLastExperimentPage();
        verify(authentication, times(2)).getName();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(pageService).getLastExperimentPage(userDTO.getId());
    }

    @Test
    public void testGetNextPageUserParticipantCurrentEqualsLast() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPage(userDTO.getId())).thenReturn(LAST_PAGE);
        assertEquals(Constants.ERROR, homeController.getNextExperimentPage(LAST, httpServletRequest).getViewName());
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).computeLastExperimentPage();
        verify(authentication, times(2)).getName();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(pageService).getLastExperimentPage(userDTO.getId());
    }

    @Test
    public void testGetNextPageUserParticipantNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, homeController.getNextExperimentPage(LAST, httpServletRequest).getViewName());
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).computeLastExperimentPage();
        verify(authentication, times(2)).getName();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(pageService, never()).getLastExperimentPage(anyInt());
    }

    @Test
    public void testGetNextPageUserParticipantAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Constants.ERROR, homeController.getNextExperimentPage(LAST, httpServletRequest).getViewName());
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).computeLastExperimentPage();
        verify(authentication).getName();
        verify(userService, never()).getUser(anyString());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(pageService, never()).getLastExperimentPage(anyInt());
    }

    @Test
    public void testGetNextPageUserParticipantAuthenticationNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(Constants.ERROR, homeController.getNextExperimentPage(LAST, httpServletRequest).getViewName());
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).computeLastExperimentPage();
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(pageService, never()).getLastExperimentPage(anyInt());
    }

    @Test
    public void testGetNextPageInvalidCurrent() {
        assertEquals(Constants.ERROR, homeController.getNextExperimentPage("-1", httpServletRequest).getViewName());
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(pageService, never()).computeLastExperimentPage();
        verify(pageService, never()).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPageInvalidCurrentNull() {
        assertEquals(Constants.ERROR, homeController.getNextExperimentPage(null, httpServletRequest).getViewName());
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(pageService, never()).computeLastExperimentPage();
        verify(pageService, never()).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPrevious() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(true);
        when(pageService.computeLastExperimentPage()).thenReturn(LAST_PAGE);
        when(pageService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        ModelAndView mv = homeController.getPreviousExperimentPage(CURRENT, httpServletRequest);
        assertAll(
                () -> assertEquals(INDEX_EXPERIMENT, mv.getViewName()),
                () -> assertEquals(experimentPage, mv.getModel().get(EXPERIMENTS)),
                () -> assertEquals(2, mv.getModel().get(EXPERIMENT_PAGE)),
                () -> assertEquals(LAST_PAGE, mv.getModel().get(LAST_EXPERIMENT_PAGE))
        );
        verify(httpServletRequest, times(2)).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService).computeLastExperimentPage();
        verify(pageService).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
    }

    @Test
    public void testGetPreviousLastSmallerCurrent() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(true);
        when(pageService.computeLastExperimentPage()).thenReturn(1);
        assertEquals(Constants.ERROR, homeController.getPreviousExperimentPage(CURRENT,
                httpServletRequest).getViewName());
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService).computeLastExperimentPage();
        verify(pageService, never()).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
    }

    @Test
    public void testGetPreviousParticipant() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPage(userDTO.getId())).thenReturn(LAST_PAGE);
        when(pageService.getExperimentParticipantPage(any(PageRequest.class),
                anyInt())).thenReturn(experimentPage);
        ModelAndView mv = homeController.getPreviousExperimentPage(CURRENT, httpServletRequest);
        assertAll(
                () -> assertEquals(INDEX_EXPERIMENT, mv.getViewName()),
                () -> assertEquals(experimentPage, mv.getModel().get(EXPERIMENTS)),
                () -> assertEquals(2, mv.getModel().get(EXPERIMENT_PAGE)),
                () -> assertEquals(LAST_PAGE, mv.getModel().get(LAST_EXPERIMENT_PAGE))
        );
        verify(httpServletRequest, times(2)).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).computeLastExperimentPage();
        verify(authentication, times(2)).getName();
        verify(pageService).getLastExperimentPage(userDTO.getId());
        verify(pageService).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(userService).getUser(userDTO.getUsername());
    }

    @Test
    public void testGetPreviousParticipantLastSmallerCurrent() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPage(userDTO.getId())).thenReturn(1);
        assertEquals(Constants.ERROR, homeController.getPreviousExperimentPage(CURRENT,
                httpServletRequest).getViewName());
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).computeLastExperimentPage();
        verify(authentication, times(2)).getName();
        verify(pageService).getLastExperimentPage(userDTO.getId());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(userService).getUser(userDTO.getUsername());
    }

    @Test
    public void testGetPreviousParticipantNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, homeController.getPreviousExperimentPage(CURRENT,
                httpServletRequest).getViewName());
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).computeLastExperimentPage();
        verify(authentication, times(2)).getName();
        verify(pageService, never()).getLastExperimentPage(anyInt());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(userService).getUser(userDTO.getUsername());
    }

    @Test
    public void testGetPreviousParticipantAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Constants.ERROR, homeController.getPreviousExperimentPage(CURRENT,
                httpServletRequest).getViewName());
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).computeLastExperimentPage();
        verify(authentication).getName();
        verify(pageService, never()).getLastExperimentPage(anyInt());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(userService, never()).getUser(anyString());
    }

    @Test
    public void testGetPreviousParticipantAuthenticationNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(Constants.ERROR, homeController.getPreviousExperimentPage(CURRENT,
                httpServletRequest).getViewName());
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).computeLastExperimentPage();
        verify(authentication, never()).getName();
        verify(pageService, never()).getLastExperimentPage(anyInt());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(userService, never()).getUser(anyString());
    }

    @Test
    public void testGetPreviousCurrent0() {
        assertEquals(Constants.ERROR, homeController.getPreviousExperimentPage("0", httpServletRequest).getViewName());
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(pageService, never()).computeLastExperimentPage();
        verify(pageService, never()).getExperimentPage(any(PageRequest.class));
        verify(authentication, never()).getName();
    }

    @Test
    public void testGetPreviousCurrentNull() {
        assertEquals(Constants.ERROR, homeController.getPreviousExperimentPage(null, httpServletRequest).getViewName());
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(pageService, never()).computeLastExperimentPage();
        verify(pageService, never()).getExperimentPage(any(PageRequest.class));
        verify(authentication, never()).getName();
    }

    @Test
    public void testGetFirstPage() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(true);
        when(pageService.computeLastExperimentPage()).thenReturn(LAST_PAGE);
        when(pageService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        ModelAndView mv = homeController.getFirstExperimentPage(httpServletRequest);
        assertAll(
                () -> assertEquals(INDEX_EXPERIMENT, mv.getViewName()),
                () -> assertEquals(experimentPage, mv.getModel().get(EXPERIMENTS)),
                () -> assertEquals(1, mv.getModel().get(EXPERIMENT_PAGE)),
                () -> assertEquals(LAST_PAGE, mv.getModel().get(LAST_EXPERIMENT_PAGE))
        );
        verify(httpServletRequest, times(2)).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService).computeLastExperimentPage();
        verify(pageService).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
    }

    @Test
    public void testGetFirstPageParticipant() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPage(userDTO.getId())).thenReturn(LAST_PAGE);
        when(pageService.getExperimentParticipantPage(any(PageRequest.class),
                anyInt())).thenReturn(experimentPage);
        ModelAndView mv = homeController.getFirstExperimentPage(httpServletRequest);
        assertAll(
                () -> assertEquals(INDEX_EXPERIMENT, mv.getViewName()),
                () -> assertEquals(experimentPage, mv.getModel().get(EXPERIMENTS)),
                () -> assertEquals(1, mv.getModel().get(EXPERIMENT_PAGE)),
                () -> assertEquals(LAST_PAGE, mv.getModel().get(LAST_EXPERIMENT_PAGE))
        );
        verify(httpServletRequest, times(2)).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).computeLastExperimentPage();
        verify(authentication, times(2)).getName();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService).getLastExperimentPage(userDTO.getId());
        verify(pageService).getExperimentParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    public void testGetFirstPageParticipantNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, homeController.getFirstExperimentPage(httpServletRequest).getViewName());
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).computeLastExperimentPage();
        verify(authentication, times(2)).getName();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService, never()).getLastExperimentPage(anyInt());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    public void testGetFirstPageParticipantAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Constants.ERROR, homeController.getFirstExperimentPage(httpServletRequest).getViewName());
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).computeLastExperimentPage();
        verify(authentication).getName();
        verify(userService, never()).getUser(anyString());
        verify(pageService, never()).getLastExperimentPage(anyInt());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    public void testGetFirstPageParticipantAuthenticationNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(Constants.ERROR, homeController.getFirstExperimentPage(httpServletRequest).getViewName());
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).computeLastExperimentPage();
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(pageService, never()).getLastExperimentPage(anyInt());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    public void testGetLastPage() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(true);
        when(pageService.computeLastExperimentPage()).thenReturn(LAST_PAGE);
        when(pageService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        ModelAndView mv = homeController.getLastExperimentPage(httpServletRequest);
        assertAll(
                () -> assertEquals(INDEX_EXPERIMENT, mv.getViewName()),
                () -> assertEquals(experimentPage, mv.getModel().get(EXPERIMENTS)),
                () -> assertEquals(LAST_PAGE, mv.getModel().get(EXPERIMENT_PAGE)),
                () -> assertEquals(LAST_PAGE, mv.getModel().get(LAST_EXPERIMENT_PAGE))
        );
        verify(httpServletRequest, times(2)).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService).computeLastExperimentPage();
        verify(pageService).getExperimentPage(any(PageRequest.class));
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
    }

    @Test
    public void testGetLastPageParticipant() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPage(userDTO.getId())).thenReturn(LAST_PAGE);
        when(pageService.getExperimentParticipantPage(any(PageRequest.class),
                anyInt())).thenReturn(experimentPage);
        ModelAndView mv = homeController.getLastExperimentPage(httpServletRequest);
        assertAll(
                () -> assertEquals(INDEX_EXPERIMENT, mv.getViewName()),
                () -> assertEquals(experimentPage, mv.getModel().get(EXPERIMENTS)),
                () -> assertEquals(LAST_PAGE, mv.getModel().get(EXPERIMENT_PAGE)),
                () -> assertEquals(LAST_PAGE, mv.getModel().get(LAST_EXPERIMENT_PAGE))
        );
        verify(httpServletRequest, times(2)).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).computeLastExperimentPage();
        verify(authentication, times(2)).getName();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService).getLastExperimentPage(userDTO.getId());
        verify(pageService).getExperimentParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    public void testGetLastPageParticipantNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, homeController.getLastExperimentPage(httpServletRequest).getViewName());
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).computeLastExperimentPage();
        verify(authentication, times(2)).getName();
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService, never()).getLastExperimentPage(anyInt());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
    }

    @Test
    public void testGetLastPageParticipantAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Constants.ERROR, homeController.getLastExperimentPage(httpServletRequest).getViewName());
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).computeLastExperimentPage();
        verify(authentication).getName();
        verify(userService, never()).getUser(anyString());
        verify(pageService, never()).getLastExperimentPage(anyInt());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetLastPageParticipantAuthenticationNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(Constants.ERROR, homeController.getLastExperimentPage(httpServletRequest).getViewName());
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(pageService, never()).computeLastExperimentPage();
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(pageService, never()).getLastExperimentPage(anyInt());
        verify(pageService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetLoginPage() {
        assertEquals(LOGIN, homeController.getLoginPage(new UserDTO()));
    }

    @Test
    public void testGetExperimentFinishPage() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(FINISH, homeController.getExperimentFinishPage(ID_STRING, ID_STRING, model));
        verify(experimentService).getExperiment(ID);
        verify(model).addAttribute(THANKS, experimentDTO.getPostscript());
        verify(model).addAttribute("user", ID);
        verify(model).addAttribute("experiment", ID);
    }

    @Test
    public void testGetExperimentFinishPagePostscriptNull() {
        experimentDTO.setPostscript(null);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(FINISH, homeController.getExperimentFinishPage(ID_STRING, ID_STRING, model));
        verify(experimentService).getExperiment(ID);
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetExperimentFinishPageNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, homeController.getExperimentFinishPage(ID_STRING, ID_STRING, model));
        verify(experimentService).getExperiment(ID);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetExperimentFinishPageExperimentIdInvalid() {
        assertEquals(Constants.ERROR, homeController.getExperimentFinishPage("0", ID_STRING, model));
        verify(experimentService, never()).getExperiment(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetExperimentFinishPageUserIdInvalid() {
        assertEquals(Constants.ERROR, homeController.getExperimentFinishPage(ID_STRING, "a", model));
        verify(experimentService, never()).getExperiment(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetExperimentFinishPageExperimentIdNull() {
        assertEquals(Constants.ERROR, homeController.getExperimentFinishPage(null, ID_STRING, model));
        verify(experimentService, never()).getExperiment(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetExperimentFinishPageUserIdNull() {
        assertEquals(Constants.ERROR, homeController.getExperimentFinishPage(ID_STRING, null, model));
        verify(experimentService, never()).getExperiment(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetExperimentFinishPageExperimentIdBlank() {
        assertEquals(Constants.ERROR, homeController.getExperimentFinishPage(BLANK, ID_STRING, model));
        verify(experimentService, never()).getExperiment(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetExperimentFinishPageUserIdBlank() {
        assertEquals(Constants.ERROR, homeController.getExperimentFinishPage(ID_STRING, BLANK, model));
        verify(experimentService, never()).getExperiment(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResetPage() {
        MailServerSetter.setMailServer(true);
        assertEquals(PASSWORD_RESET, homeController.getResetPage(new UserDTO()));
    }

    @Test
    public void testGetResetPageNoMailServer() {
        MailServerSetter.setMailServer(false);
        assertEquals(Constants.ERROR, homeController.getResetPage(new UserDTO()));
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

    private List<CourseTableProjection> getCourseTableProjections(int number) {
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

}
