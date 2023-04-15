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
package fim.unipassau.de.scratchLog.web;

import fim.unipassau.de.scratchLog.StringCreator;
import fim.unipassau.de.scratchLog.application.service.SearchService;
import fim.unipassau.de.scratchLog.persistence.projection.CourseTableProjection;
import fim.unipassau.de.scratchLog.persistence.projection.ExperimentTableProjection;
import fim.unipassau.de.scratchLog.persistence.projection.UserProjection;
import fim.unipassau.de.scratchLog.util.Constants;
import fim.unipassau.de.scratchLog.util.enums.Role;
import fim.unipassau.de.scratchLog.web.controller.SearchController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchControllerTest {

    @InjectMocks
    private SearchController searchController;

    @Mock
    private SearchService searchService;

    @Mock
    private Model model;

    private static final String QUERY = "query";
    private static final String LONG_QUERY = StringCreator.createLongString(104);
    private static final String BLANK = "  ";
    private static final String SEARCH = "search";
    private static final String ERROR = "redirect:/error";
    private static final int COUNT = 25;
    private List<UserProjection> users;
    private List<ExperimentTableProjection> experiments;
    private List<CourseTableProjection> courses;

    @Test
    public void testGetSearchPage() {
        users = getUsers(Constants.PAGE_SIZE);
        experiments = getExperiments(Constants.PAGE_SIZE);
        when(searchService.getUserCount(QUERY)).thenReturn(COUNT);
        when(searchService.getExperimentCount(QUERY)).thenReturn(COUNT);
        when(searchService.getCourseCount(QUERY)).thenReturn(COUNT);
        when(searchService.getUserList(QUERY, Constants.PAGE_SIZE)).thenReturn(users);
        when(searchService.getExperimentList(QUERY, Constants.PAGE_SIZE)).thenReturn(experiments);
        when(searchService.getCourseList(QUERY, Constants.PAGE_SIZE)).thenReturn(courses);
        assertEquals(SEARCH, searchController.getSearchPage(QUERY, model));
        verify(searchService).getUserCount(QUERY);
        verify(searchService).getExperimentCount(QUERY);
        verify(searchService).getCourseCount(QUERY);
        verify(searchService).getUserList(QUERY, Constants.PAGE_SIZE);
        verify(searchService).getExperimentList(QUERY, Constants.PAGE_SIZE);
        verify(searchService).getCourseList(QUERY, Constants.PAGE_SIZE);
        verify(model, times(8)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetSearchPageQueryBlank() {
        assertEquals(SEARCH, searchController.getSearchPage(BLANK, model));
        verify(searchService, never()).getUserCount(anyString());
        verify(searchService, never()).getExperimentCount(anyString());
        verify(searchService, never()).getCourseCount(anyString());
        verify(searchService, never()).getUserList(anyString(), anyInt());
        verify(searchService, never()).getExperimentList(anyString(), anyInt());
        verify(searchService, never()).getCourseList(anyString(), anyInt());
        verify(model, times(8)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetSearchPageQueryTooLong() {
        assertEquals(ERROR, searchController.getSearchPage(LONG_QUERY, model));
        verify(searchService, never()).getUserCount(anyString());
        verify(searchService, never()).getExperimentCount(anyString());
        verify(searchService, never()).getCourseCount(anyString());
        verify(searchService, never()).getUserList(anyString(), anyInt());
        verify(searchService, never()).getExperimentList(anyString(), anyInt());
        verify(searchService, never()).getCourseList(anyString(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetSearchPageQueryNull() {
        assertEquals(ERROR, searchController.getSearchPage(null, model));
        verify(searchService, never()).getUserCount(anyString());
        verify(searchService, never()).getExperimentCount(anyString());
        verify(searchService, never()).getCourseCount(anyString());
        verify(searchService, never()).getUserList(anyString(), anyInt());
        verify(searchService, never()).getExperimentList(anyString(), anyInt());
        verify(searchService, never()).getCourseList(anyString(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    private List<UserProjection> getUsers(int number) {
        List<UserProjection> userProjections = new ArrayList<>();

        for (int i = 0; i < number; i++) {
            int id = i + 1;

            UserProjection user = new UserProjection() {
                @Override
                public Integer getId() {
                    return id;
                }

                @Override
                public String getUsername() {
                    return "user" + id;
                }

                @Override
                public String getEmail() {
                    return "user" + id + "@user.de";
                }

                @Override
                public Role getRole() {
                    return id == 1 ? Role.ADMIN : Role.PARTICIPANT;
                }
            };

            userProjections.add(user);
        }

        return userProjections;
    }

    private List<ExperimentTableProjection> getExperiments(int number) {
        List<ExperimentTableProjection> experiments = new ArrayList<>();

        for (int i = 0; i < number; i++) {
            int id = i + 1;

            ExperimentTableProjection experiment = new ExperimentTableProjection() {
                @Override
                public Integer getId() {
                    return id;
                }

                @Override
                public String getTitle() {
                    return "title" + id;
                }

                @Override
                public String getDescription() {
                    return "description" + id;
                }

                @Override
                public boolean isActive() {
                    return false;
                }
            };

            experiments.add(experiment);
        }

        return experiments;
    }
}
