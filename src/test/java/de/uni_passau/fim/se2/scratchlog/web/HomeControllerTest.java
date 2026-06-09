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

package de.uni_passau.fim.se2.scratchlog.web;

import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.service.ExperimentService;
import de.uni_passau.fim.se2.scratchlog.application.service.PageService;
import de.uni_passau.fim.se2.scratchlog.application.service.ParticipantService;
import de.uni_passau.fim.se2.scratchlog.application.service.TokenService;
import de.uni_passau.fim.se2.scratchlog.application.service.UserService;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.CourseTableProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.ExperimentTableProjection;
import de.uni_passau.fim.se2.scratchlog.spring.configuration.SecurityTestConfig;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import de.uni_passau.fim.se2.scratchlog.web.controller.HomeController;
import de.uni_passau.fim.se2.scratchlog.web.dto.ExperimentDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(HomeController.class)
@Import(SecurityTestConfig.class)
public class HomeControllerTest extends AbstractControllerTest {

    @Autowired
    private HomeController homeController;

    @MockitoBean
    private ExperimentService experimentService;

    @MockitoBean
    private PageService pageService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ParticipantService participantService;

    @MockitoBean
    private TokenService tokenService;

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
    private static final String INDEX_COURSE = "index::course_table";
    private static final String LOGIN = "login";
    private static final String PASSWORD_RESET = "password-reset";
    private static final String FINISH = "experiment-finish";
    private static final int CURRENT = 3;
    private static final int LAST = 5;
    private static final String BLANK = "   ";
    private static final String SECRET = "secret";
    private static final String THANKS = "thanks";
    private static final String EXPERIMENTS = "experiments";
    private static final String EXPERIMENT_PAGE = "experimentPage";
    private static final String LAST_EXPERIMENT_PAGE = "lastExperimentPage";
    private static final String COURSES = "courses";
    private static final String COURSE_PAGE = "coursePage";
    private static final String LAST_COURSE_PAGE = "lastCoursePage";
    private static final int LAST_PAGE = 4;
    private static final int PAGE = 3;
    private static final int ID = 1;
    private static final ExperimentDTO experimentDTO = new ExperimentDTO(ID, "My Experiment", "description",
            "info", "postscript", true, false, GUI_URL);
    private static final UserDTO userDTO = new UserDTO("participant", "email", Role.PARTICIPANT, Language.ENGLISH, "password",
            "");
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
        when(httpServletRequest.isUserInRole(Constants.ROLE_PARTICIPANT)).thenReturn(true);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getExperimentPage(anyInt())).thenReturn(experimentPage);
        when(pageService.getCoursePage(anyInt())).thenReturn(coursePage);
        assertEquals(INDEX, homeController.getIndexPage(httpServletRequest, model));
        verify(httpServletRequest, times(1)).isUserInRole(Constants.ROLE_ADMIN);
        verify(httpServletRequest).isUserInRole(Constants.ROLE_PARTICIPANT);
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService).getExperimentPage(anyInt());
        verify(pageService).getCoursePage(anyInt());
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
        when(pageService.getExperimentParticipantPage(anyInt(), anyInt())).thenReturn(experimentPage);
        when(pageService.getCourseParticipantPage(anyInt(), anyInt())).thenReturn(coursePage);
        assertEquals(INDEX, homeController.getIndexPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_PARTICIPANT);
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService).getExperimentParticipantPage(anyInt(), anyInt());
        verify(pageService).getCourseParticipantPage(anyInt(), anyInt());
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetIndexPageUserNotFound() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_PARTICIPANT)).thenReturn(true);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, homeController.getIndexPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_PARTICIPANT);
        verify(userService).getUser(userDTO.getUsername());
        verify(pageService, never()).getExperimentParticipantPage(anyInt(), anyInt());
        verify(pageService, never()).getCourseParticipantPage(anyInt(), anyInt());
        verify(pageService, never()).getLastExperimentPageForUser(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetIndexPageUserAuthenticationNameNull() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_PARTICIPANT)).thenReturn(true);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Constants.ERROR, homeController.getIndexPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_PARTICIPANT);
        verify(userService, never()).getUser(anyString());
        verify(pageService, never()).getExperimentParticipantPage(anyInt(), anyInt());
        verify(pageService, never()).getLastExperimentPageForUser(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetIndexPageUserAuthenticationNull() {
        when(httpServletRequest.isUserInRole(Constants.ROLE_PARTICIPANT)).thenReturn(true);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(Constants.ERROR, homeController.getIndexPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_PARTICIPANT);
        verify(authentication, never()).getName();
        verify(userService, never()).getUser(anyString());
        verify(pageService, never()).getExperimentParticipantPage(anyInt(), anyInt());
        verify(pageService, never()).getLastExperimentPageForUser(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetIndexPageUnauthenticated() {
        assertEquals(INDEX, homeController.getIndexPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(Constants.ROLE_PARTICIPANT);
        verify(pageService, never()).getExperimentPage(anyInt());
        verify(pageService, never()).getLastExperimentPage();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetCoursePage() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(true);
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastCoursePage()).thenReturn(LAST_PAGE);
        when(pageService.getCoursePage(anyInt())).thenReturn(coursePage);
        ModelAndView mv = homeController.getCoursePage(CURRENT, httpServletRequest);
        assertAll(
                () -> assertEquals(INDEX_COURSE, mv.getViewName()),
                () -> assertEquals(coursePage, mv.getModel().get(COURSES)),
                () -> assertEquals(PAGE, mv.getModel().get(COURSE_PAGE)),
                () -> assertEquals(LAST_PAGE - 1, mv.getModel().get(LAST_COURSE_PAGE))
        );
    }

    @Test
    public void testGetCoursePageParticipant() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastCoursePageForUser(userDTO.getId())).thenReturn(LAST_PAGE);
        when(pageService.getCourseParticipantPage(anyInt(), anyInt())).thenReturn(coursePage);
        ModelAndView mv = homeController.getCoursePage(CURRENT, httpServletRequest);
        assertAll(
                () -> assertEquals(INDEX_COURSE, mv.getViewName()),
                () -> assertEquals(coursePage, mv.getModel().get(COURSES)),
                () -> assertEquals(PAGE, mv.getModel().get(COURSE_PAGE)),
                () -> assertEquals(LAST_PAGE - 1, mv.getModel().get(LAST_COURSE_PAGE))
        );
    }

    @Test
    public void testGetCoursePageParticipantNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        assertThrows(ResponseStatusException.class, () -> homeController.getCoursePage(CURRENT, httpServletRequest));
    }

    @Test
    public void testGetExperimentPage() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPageForUser(userDTO.getId())).thenReturn(LAST_PAGE);
        when(pageService.getExperimentParticipantPage(anyInt(), anyInt())).thenReturn(experimentPage);
        ModelAndView mv = homeController.getExperimentPage(CURRENT, httpServletRequest);
        assertAll(
                () -> assertEquals(INDEX_EXPERIMENT, mv.getViewName()),
                () -> assertEquals(experimentPage, mv.getModel().get(EXPERIMENTS)),
                () -> assertEquals(PAGE, mv.getModel().get(EXPERIMENT_PAGE)),
                () -> assertEquals(PAGE, mv.getModel().get(LAST_EXPERIMENT_PAGE))
        );
    }

    @Test
    public void testGetExperimentPageAdmin() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)).thenReturn(true);
        when(pageService.getLastExperimentPage()).thenReturn(LAST_PAGE);
        when(pageService.getExperimentPage(anyInt())).thenReturn(experimentPage);
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        ModelAndView mv = homeController.getExperimentPage(CURRENT, httpServletRequest);
        assertAll(
                () -> assertEquals(INDEX_EXPERIMENT, mv.getViewName()),
                () -> assertEquals(experimentPage, mv.getModel().get(EXPERIMENTS)),
                () -> assertEquals(PAGE, mv.getModel().get(EXPERIMENT_PAGE)),
                () -> assertEquals(PAGE, mv.getModel().get(LAST_EXPERIMENT_PAGE))
        );
    }

    @Test
    public void testGetExperimentPageUserNotFound() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        assertThrows(ResponseStatusException.class, () -> homeController.getExperimentPage(CURRENT, httpServletRequest));
    }

    @Test
    public void testGetExperimentPageAuthenticationNameNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertThrows(ResponseStatusException.class, () -> homeController.getExperimentPage(CURRENT, httpServletRequest));
    }

    @Test
    public void testGetExperimentPageAuthenticationNull() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertThrows(ResponseStatusException.class, () -> homeController.getExperimentPage(CURRENT, httpServletRequest));
    }

    @Test
    public void testGetExperimentPageBiggerLast() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPageForUser(userDTO.getId())).thenReturn(CURRENT - 1);
        when(pageService.getExperimentParticipantPage(anyInt(), anyInt())).thenReturn(experimentPage);
        ModelAndView mv = homeController.getExperimentPage(CURRENT, httpServletRequest);
        assertAll(
            () -> assertEquals(INDEX_EXPERIMENT, mv.getViewName()),
            () -> assertEquals(experimentPage, mv.getModel().get(EXPERIMENTS)),
            () -> assertEquals(CURRENT, mv.getModel().get(EXPERIMENT_PAGE)),
            () -> assertEquals(CURRENT - 2, mv.getModel().get(LAST_EXPERIMENT_PAGE))
        );
    }

    @Test
    public void testGetExperimentPageSmallerZero() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userDTO.getUsername());
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(pageService.getLastExperimentPageForUser(userDTO.getId())).thenReturn(CURRENT);
        when(pageService.getExperimentParticipantPage(anyInt(), anyInt())).thenReturn(experimentPage);
        ModelAndView mv = homeController.getExperimentPage(-1, httpServletRequest);
        assertAll(
            () -> assertEquals(INDEX_EXPERIMENT, mv.getViewName()),
            () -> assertEquals(experimentPage, mv.getModel().get(EXPERIMENTS)),
            () -> assertEquals(-1, mv.getModel().get(EXPERIMENT_PAGE)),
            () -> assertEquals(CURRENT - 1, mv.getModel().get(LAST_EXPERIMENT_PAGE))
        );
    }

    @Test
    public void testGetLoginPage() {
        assertEquals(LOGIN, homeController.getLoginPage(new UserDTO()));
    }

    @Test
    public void testGetExperimentFinishPage() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(FINISH, homeController.getExperimentFinishPage(ID, ID, SECRET, model));
        verify(participantService).isInvalidParticipant(ID, ID, SECRET, false);
        verify(experimentService).getExperiment(ID);
        verify(model).addAttribute(THANKS, experimentDTO.getPostscript());
        verify(model).addAttribute("user", ID);
        verify(model).addAttribute("experiment", ID);
        verify(model).addAttribute(SECRET, SECRET);
    }

    @Test
    public void testGetExperimentFinishPagePostscriptNull() {
        experimentDTO.setPostscript(null);
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        assertEquals(FINISH, homeController.getExperimentFinishPage(ID, ID, SECRET, model));
        verify(participantService).isInvalidParticipant(ID, ID, SECRET, false);
        verify(experimentService).getExperiment(ID);
        verify(model, times(4)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetExperimentFinishPageNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, homeController.getExperimentFinishPage(ID, ID, SECRET, model));
        verify(participantService).isInvalidParticipant(ID, ID, SECRET, false);
        verify(experimentService).getExperiment(ID);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetExperimentFinishPageInvalidParticipant() {
        when(participantService.isInvalidParticipant(ID, ID, SECRET, false)).thenReturn(true);
        assertEquals(Constants.ERROR, homeController.getExperimentFinishPage(ID, ID, SECRET, model));
        verify(participantService).isInvalidParticipant(ID, ID, SECRET, false);
        verify(experimentService, never()).getExperiment(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetExperimentFinishPageSecretBlank() {
        assertEquals(Constants.ERROR, homeController.getExperimentFinishPage(ID, ID, BLANK, model));
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString(), anyBoolean());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetExperimentFinishPageSecretNull() {
        assertEquals(Constants.ERROR, homeController.getExperimentFinishPage(ID, ID, null, model));
        verify(participantService, never()).isInvalidParticipant(anyInt(), anyInt(), anyString(), anyBoolean());
        verify(experimentService, never()).getExperiment(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResetPage() {
        setMailServer(true);
        assertEquals(PASSWORD_RESET, homeController.getResetPage(new UserDTO()));
    }

    @Test
    public void testGetResetPageNoMailServer() {
        setMailServer(false);
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
