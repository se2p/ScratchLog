package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.UserService;
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

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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
    private static final String INDEX = "index";
    private static final String LOGIN = "login";
    private static final String PASSWORD_RESET = "password-reset";
    private static final String FINISH = "experiment-finish";
    private static final String CURRENT = "3";
    private static final String LAST = "5";
    private static final String BLANK = "   ";
    private static final String ID_STRING = "1";
    private static final String THANKS = "thanks";
    private static final int LAST_PAGE = 4;
    private static final int ID = 1;
    private static final ExperimentDTO experimentDTO = new ExperimentDTO(ID, "My Experiment", "description",
            "info", "postscript", true);
    private static final UserDTO userDTO = new UserDTO("participant", "email", UserDTO.Role.PARTICIPANT,
            UserDTO.Language.ENGLISH, "password", "");
    private final Page<ExperimentTableProjection> experimentPage = new PageImpl<>(getExperimentProjections(5));

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
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        when(experimentService.getLastPage()).thenReturn(1);
        assertEquals(INDEX, homeController.getIndexPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(httpServletRequest, never()).isUserInRole(Constants.ROLE_PARTICIPANT);
        verify(experimentService).getExperimentPage(any(PageRequest.class));
        verify(experimentService).getLastPage();
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetIndexPageParticipant() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(false);
        when(httpServletRequest.isUserInRole(Constants.ROLE_PARTICIPANT)).thenReturn(true);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(experimentService.getExperimentParticipantPage(any(PageRequest.class),
                anyInt())).thenReturn(experimentPage);
        when(experimentService.getLastExperimentPage(userDTO.getId())).thenReturn(1);
        assertEquals(INDEX, homeController.getIndexPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_PARTICIPANT);
        verify(userService).getUser(userDTO.getUsername());
        verify(experimentService).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(experimentService).getLastExperimentPage(userDTO.getId());
        verify(model, times(3)).addAttribute(anyString(), any());
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
        verify(experimentService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(experimentService, never()).getLastExperimentPage(anyInt());
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
        verify(experimentService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(experimentService, never()).getLastExperimentPage(anyInt());
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
        verify(experimentService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(experimentService, never()).getLastExperimentPage(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetIndexPageNoAdmin() {
        assertEquals(INDEX, homeController.getIndexPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(experimentService, never()).getLastPage();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPage() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(true);
        when(experimentService.getLastPage()).thenReturn(LAST_PAGE);
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        assertEquals(INDEX, homeController.getNextPage(CURRENT, httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService).getLastPage();
        verify(experimentService).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPageInvalidCurrentEqualLast() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(true);
        when(experimentService.getLastPage()).thenReturn(LAST_PAGE);
        assertEquals(Constants.ERROR, homeController.getNextPage(LAST, httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService).getLastPage();
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPageUserParticipant() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(experimentService.getLastExperimentPage(userDTO.getId())).thenReturn(LAST_PAGE);
        when(experimentService.getExperimentParticipantPage(any(PageRequest.class),
                anyInt())).thenReturn(experimentPage);
        assertEquals(INDEX, homeController.getNextPage(CURRENT, httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getLastPage();
        verify(authentication, times(2)).getName();
        verify(userService).getUser(userDTO.getUsername());
        verify(experimentService).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(experimentService).getLastExperimentPage(userDTO.getId());
    }

    @Test
    public void testGetNextPageUserParticipantCurrentEqualsLast() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(experimentService.getLastExperimentPage(userDTO.getId())).thenReturn(LAST_PAGE);
        assertEquals(Constants.ERROR, homeController.getNextPage(LAST, httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getLastPage();
        verify(authentication, times(2)).getName();
        verify(userService).getUser(userDTO.getUsername());
        verify(experimentService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(experimentService).getLastExperimentPage(userDTO.getId());
    }

    @Test
    public void testGetNextPageUserParticipantNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, homeController.getNextPage(CURRENT, httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getLastPage();
        verify(authentication, times(2)).getName();
        verify(userService).getUser(userDTO.getUsername());
        verify(experimentService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(experimentService, never()).getLastExperimentPage(anyInt());
    }

    @Test
    public void testGetNextPageUserParticipantAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Constants.ERROR, homeController.getNextPage(CURRENT, httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getLastPage();
        verify(authentication).getName();
        verify(userService, never()).getUser(anyString());
        verify(experimentService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(experimentService, never()).getLastExperimentPage(anyInt());
    }

    @Test
    public void testGetNextPageUserParticipantAuthenticationNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(Constants.ERROR, homeController.getNextPage(CURRENT, httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getLastPage();
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(experimentService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(experimentService, never()).getLastExperimentPage(anyInt());
    }

    @Test
    public void testGetNextPageInvalidCurrent() {
        assertEquals(Constants.ERROR, homeController.getNextPage(BLANK, httpServletRequest, model));
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(experimentService, never()).getLastPage();
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPageInvalidCurrentNull() {
        assertEquals(Constants.ERROR, homeController.getNextPage(null, httpServletRequest, model));
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(experimentService, never()).getLastPage();
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPrevious() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(true);
        when(experimentService.getLastPage()).thenReturn(LAST_PAGE);
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        assertEquals(INDEX, homeController.getPreviousPage(CURRENT, httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService).getLastPage();
        verify(experimentService).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousLastSmallerCurrent() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(true);
        when(experimentService.getLastPage()).thenReturn(1);
        assertEquals(Constants.ERROR, homeController.getPreviousPage(CURRENT, httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService).getLastPage();
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousParticipant() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(experimentService.getLastExperimentPage(userDTO.getId())).thenReturn(LAST_PAGE);
        when(experimentService.getExperimentParticipantPage(any(PageRequest.class),
                anyInt())).thenReturn(experimentPage);
        assertEquals(INDEX, homeController.getPreviousPage(CURRENT, httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getLastPage();
        verify(authentication, times(2)).getName();
        verify(experimentService).getLastExperimentPage(userDTO.getId());
        verify(experimentService).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(userService).getUser(userDTO.getUsername());
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousParticipantLastSmallerCurrent() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(experimentService.getLastExperimentPage(userDTO.getId())).thenReturn(1);
        assertEquals(Constants.ERROR, homeController.getPreviousPage(CURRENT, httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getLastPage();
        verify(authentication, times(2)).getName();
        verify(experimentService).getLastExperimentPage(userDTO.getId());
        verify(experimentService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(userService).getUser(userDTO.getUsername());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousParticipantNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, homeController.getPreviousPage(CURRENT, httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getLastPage();
        verify(authentication, times(2)).getName();
        verify(experimentService, never()).getLastExperimentPage(anyInt());
        verify(experimentService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(userService).getUser(userDTO.getUsername());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousParticipantAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Constants.ERROR, homeController.getPreviousPage(CURRENT, httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getLastPage();
        verify(authentication).getName();
        verify(experimentService, never()).getLastExperimentPage(anyInt());
        verify(experimentService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(userService, never()).getUser(anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousParticipantAuthenticationNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(Constants.ERROR, homeController.getPreviousPage(CURRENT, httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getLastPage();
        verify(authentication, never()).getName();
        verify(experimentService, never()).getLastExperimentPage(anyInt());
        verify(experimentService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(userService, never()).getUser(anyString());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousCurrent0() {
        assertEquals(Constants.ERROR, homeController.getPreviousPage("0", httpServletRequest, model));
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(experimentService, never()).getLastPage();
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(authentication, never()).getName();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousCurrentNull() {
        assertEquals(Constants.ERROR, homeController.getPreviousPage(null, httpServletRequest, model));
        verify(httpServletRequest, never()).isUserInRole(anyString());
        verify(experimentService, never()).getLastPage();
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(authentication, never()).getName();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetFirstPage() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(true);
        when(experimentService.getLastPage()).thenReturn(LAST_PAGE);
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        assertEquals(INDEX, homeController.getFirstPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService).getLastPage();
        verify(experimentService).getExperimentPage(any(PageRequest.class));
        verify(userService, never()).getUser(anyString());
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetFirstPageParticipant() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(experimentService.getLastExperimentPage(userDTO.getId())).thenReturn(LAST_PAGE);
        when(experimentService.getExperimentParticipantPage(any(PageRequest.class),
                anyInt())).thenReturn(experimentPage);
        assertEquals(INDEX, homeController.getFirstPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getLastPage();
        verify(authentication, times(2)).getName();
        verify(userService).getUser(userDTO.getUsername());
        verify(experimentService).getLastExperimentPage(userDTO.getId());
        verify(experimentService).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetFirstPageParticipantNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, homeController.getFirstPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getLastPage();
        verify(authentication, times(2)).getName();
        verify(userService).getUser(userDTO.getUsername());
        verify(experimentService, never()).getLastExperimentPage(anyInt());
        verify(experimentService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetFirstPageParticipantAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Constants.ERROR, homeController.getFirstPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getLastPage();
        verify(authentication).getName();
        verify(userService, never()).getUser(anyString());
        verify(experimentService, never()).getLastExperimentPage(anyInt());
        verify(experimentService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetFirstPageParticipantAuthenticationNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(Constants.ERROR, homeController.getFirstPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getLastPage();
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(experimentService, never()).getLastExperimentPage(anyInt());
        verify(experimentService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetLastPage() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(true);
        when(experimentService.getLastPage()).thenReturn(LAST_PAGE);
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        assertEquals(INDEX, homeController.getLastPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService).getLastPage();
        verify(experimentService).getExperimentPage(any(PageRequest.class));
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetLastPageParticipant() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(experimentService.getLastExperimentPage(userDTO.getId())).thenReturn(LAST_PAGE);
        when(experimentService.getExperimentParticipantPage(any(PageRequest.class),
                anyInt())).thenReturn(experimentPage);
        assertEquals(INDEX, homeController.getLastPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getLastPage();
        verify(authentication, times(2)).getName();
        verify(userService).getUser(userDTO.getUsername());
        verify(experimentService).getLastExperimentPage(userDTO.getId());
        verify(experimentService).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetLastPageParticipantNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, homeController.getLastPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getLastPage();
        verify(authentication, times(2)).getName();
        verify(userService).getUser(userDTO.getUsername());
        verify(experimentService, never()).getLastExperimentPage(anyInt());
        verify(experimentService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetLastPageParticipantAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Constants.ERROR, homeController.getLastPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getLastPage();
        verify(authentication).getName();
        verify(userService, never()).getUser(anyString());
        verify(experimentService, never()).getLastExperimentPage(anyInt());
        verify(experimentService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetLastPageParticipantAuthenticationNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(Constants.ERROR, homeController.getLastPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_ADMIN);
        verify(experimentService, never()).getLastPage();
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(experimentService, never()).getLastExperimentPage(anyInt());
        verify(experimentService, never()).getExperimentParticipantPage(any(PageRequest.class), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetLoginPage() {
        assertEquals(LOGIN, homeController.getLoginPage(new UserDTO()));
    }

    @Test
    public void testGetExperimentFinishPage() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(FINISH, homeController.getExperimentFinishPage(ID_STRING, model));
        verify(experimentService).getExperiment(ID);
        verify(model).addAttribute(THANKS, experimentDTO.getPostscript());
    }

    @Test
    public void testGetExperimentFinishPagePostscriptNull() {
        experimentDTO.setPostscript(null);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(FINISH, homeController.getExperimentFinishPage(ID_STRING, model));
        verify(experimentService).getExperiment(ID);
        verify(model).addAttribute(anyString(), anyString());
    }

    @Test
    public void testGetExperimentFinishPageNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, homeController.getExperimentFinishPage(ID_STRING, model));
        verify(experimentService).getExperiment(ID);
        verify(model, never()).addAttribute(anyString(), anyString());
    }

    @Test
    public void testGetExperimentFinishPageExperimentIdInvalid() {
        assertEquals(Constants.ERROR, homeController.getExperimentFinishPage(BLANK, model));
        verify(experimentService, never()).getExperiment(anyInt());
        verify(model, never()).addAttribute(anyString(), anyString());
    }

    @Test
    public void testGetExperimentFinishPageExperimentIdNull() {
        assertEquals(Constants.ERROR, homeController.getExperimentFinishPage(null, model));
        verify(experimentService, never()).getExperiment(anyInt());
        verify(model, never()).addAttribute(anyString(), anyString());
    }

    @Test
    public void testGetResetPage() {
        assertEquals(PASSWORD_RESET, homeController.getResetPage(new UserDTO()));
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
}
