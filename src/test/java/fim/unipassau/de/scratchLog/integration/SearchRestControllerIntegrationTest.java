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
package fim.unipassau.de.scratchLog.integration;

import fim.unipassau.de.scratchLog.application.service.SearchService;
import fim.unipassau.de.scratchLog.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratchLog.web.controller.SearchRestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(SearchRestController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class SearchRestControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SearchService searchService;

    private static final String ID_STRING = "1";
    private static final String PAGE_STRING = "2";
    private static final String QUERY = "participant";
    private static final String BLANK = "   ";
    private static final String QUERY_PARAM = "query";
    private static final String ID_PARAM = "id";
    private static final String PAGE_PARAM = "page";
    private static final String PARTICIPANT = "PARTICIPANT";
    private static final int ID = 1;
    private static final int PAGE = 2;
    private List<String[]> userData;
    private List<String[]> userProjectionData;
    private List<String[]> experimentData;
    private List<String[]> experimentTableData;
    private List<String[]> courseTableData;

    @BeforeEach
    public void setup() {
        addUserData(5);
        addExperimentData(3);
        experimentData.addAll(userData);
        addUserProjectionData(3);
        addExperimentTableData(2);
        addCourseTableData(2);
    }

    @Test
    public void testGetSearchSuggestions() throws Exception {
        when(searchService.getSearchSuggestions(QUERY)).thenReturn(experimentData);
        mvc.perform(get("/search/suggestions")
                .param(QUERY_PARAM, QUERY)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].[0]").value("experiment0"))
                .andExpect(jsonPath("$.[0].[1]").value("description0"))
                .andExpect(jsonPath("$.[3].[0]").value("participant0"))
                .andExpect(jsonPath("$.[3].[1]").value("participant0@participant.de"))
                .andExpect(jsonPath("$.length()").value(8));
        verify(searchService).getSearchSuggestions(QUERY);
    }

    @Test
    public void testGetSearchSuggestionsQueryBlank() throws Exception {
        mvc.perform(get("/search/suggestions")
                .param(QUERY_PARAM, BLANK)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(searchService, never()).getSearchSuggestions(anyString());
    }

    @Test
    public void testGetMoreUsers() throws Exception {
        when(searchService.getNextUsers(QUERY, PAGE)).thenReturn(userProjectionData);
        mvc.perform(get("/search/users")
                        .param(QUERY_PARAM, QUERY)
                        .param(PAGE_PARAM, PAGE_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$.[0].[0]").value("0"))
                .andExpect(jsonPath("$.[0].[1]").value("participant0"))
                .andExpect(jsonPath("$.[0].[2]").value("participant0@participant.de"))
                .andExpect(jsonPath("$.[0].[3]").value(PARTICIPANT))
                .andExpect(jsonPath("$.[1].[0]").value("1"))
                .andExpect(jsonPath("$.[1].[1]").value("participant1"))
                .andExpect(jsonPath("$.[1].[2]").value("participant1@participant.de"))
                .andExpect(jsonPath("$.[1].[3]").value(PARTICIPANT))
                .andExpect(jsonPath("$.[2].[0]").value("2"))
                .andExpect(jsonPath("$.[2].[1]").value("participant2"))
                .andExpect(jsonPath("$.[2].[2]").value("participant2@participant.de"))
                .andExpect(jsonPath("$.[2].[3]").value(PARTICIPANT));
        verify(searchService).getNextUsers(QUERY, PAGE);
    }

    @Test
    public void testGetMoreUsersInvalidPage() throws Exception {
        mvc.perform(get("/search/users")
                        .param(QUERY_PARAM, QUERY)
                        .param(PAGE_PARAM, "-1")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(searchService, never()).getNextUsers(anyString(), anyInt());
    }

    @Test
    public void testGetMoreUsersPageBlank() throws Exception {
        mvc.perform(get("/search/users")
                        .param(QUERY_PARAM, QUERY)
                        .param(PAGE_PARAM, BLANK)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(searchService, never()).getNextUsers(anyString(), anyInt());
    }

    @Test
    public void testGetMoreUsersQueryBlank() throws Exception {
        mvc.perform(get("/search/users")
                        .param(QUERY_PARAM, BLANK)
                        .param(PAGE_PARAM, PAGE_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(searchService, never()).getNextUsers(anyString(), anyInt());
    }

    @Test
    public void testGetMoreExperiments() throws Exception {
        when(searchService.getNextExperiments(QUERY, PAGE)).thenReturn(experimentTableData);
        mvc.perform(get("/search/experiments")
                        .param(QUERY_PARAM, QUERY)
                        .param(PAGE_PARAM, PAGE_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].[0]").value("0"))
                .andExpect(jsonPath("$.[0].[1]").value("experiment0"))
                .andExpect(jsonPath("$.[0].[2]").value("description0"))
                .andExpect(jsonPath("$.[1].[0]").value("1"))
                .andExpect(jsonPath("$.[1].[1]").value("experiment1"))
                .andExpect(jsonPath("$.[1].[2]").value("description1"));
        verify(searchService).getNextExperiments(QUERY, PAGE);
    }

    @Test
    public void testGetMoreExperimentsInvalidPage() throws Exception {
        mvc.perform(get("/search/experiments")
                        .param(QUERY_PARAM, QUERY)
                        .param(PAGE_PARAM, "0")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(searchService, never()).getNextExperiments(anyString(), anyInt());
    }

    @Test
    public void testGetMoreExperimentsPageNaN() throws Exception {
        mvc.perform(get("/search/experiments")
                        .param(QUERY_PARAM, QUERY)
                        .param(PAGE_PARAM, "a")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(searchService, never()).getNextExperiments(anyString(), anyInt());
    }

    @Test
    public void testGetMoreExperimentsQueryBlank() throws Exception {
        mvc.perform(get("/search/experiments")
                        .param(QUERY_PARAM, BLANK)
                        .param(PAGE_PARAM, PAGE_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(searchService, never()).getNextExperiments(anyString(), anyInt());
    }

    @Test
    public void testGetMoreCourses() throws Exception {
        when(searchService.getNextCourses(QUERY, PAGE)).thenReturn(courseTableData);
        mvc.perform(get("/search/courses")
                        .param(QUERY_PARAM, QUERY)
                        .param(PAGE_PARAM, PAGE_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].[0]").value("0"))
                .andExpect(jsonPath("$.[0].[1]").value("course0"))
                .andExpect(jsonPath("$.[0].[2]").value("description0"))
                .andExpect(jsonPath("$.[1].[0]").value("1"))
                .andExpect(jsonPath("$.[1].[1]").value("course1"))
                .andExpect(jsonPath("$.[1].[2]").value("description1"));
        verify(searchService).getNextCourses(QUERY, PAGE);
    }

    @Test
    public void testGetMoreCoursesQueryBlank() throws Exception {
        mvc.perform(get("/search/courses")
                        .param(QUERY_PARAM, BLANK)
                        .param(PAGE_PARAM, PAGE_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(searchService, never()).getNextCourses(anyString(), anyInt());
    }

    @Test
    public void testGetUserSuggestions() throws Exception {
        when(searchService.getUserSuggestions(QUERY, ID)).thenReturn(userData);
        mvc.perform(get("/search/user")
                .param(QUERY_PARAM, QUERY)
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].[0]").value("participant0"))
                .andExpect(jsonPath("$.[0].[1]").value("participant0@participant.de"))
                .andExpect(jsonPath("$.length()").value(5));
        verify(searchService).getUserSuggestions(QUERY, ID);
    }

    @Test
    public void testGetUserSuggestionsInvalidId() throws Exception {
        mvc.perform(get("/search/user")
                .param(QUERY_PARAM, QUERY)
                .param(ID_PARAM, "0")
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(searchService, never()).getUserSuggestions(anyString(), anyInt());
    }

    @Test
    public void testGetUserSuggestionsIdBlank() throws Exception {
        mvc.perform(get("/search/user")
                .param(QUERY_PARAM, QUERY)
                .param(ID_PARAM, BLANK)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(searchService, never()).getUserSuggestions(anyString(), anyInt());
    }

    @Test
    public void testGetUserSuggestionsQueryBlank() throws Exception {
        mvc.perform(get("/search/user")
                .param(QUERY_PARAM, BLANK)
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(searchService, never()).getUserSuggestions(anyString(), anyInt());
    }

    @Test
    public void testGetDeleteUserSuggestions() throws Exception {
        when(searchService.getUserDeleteSuggestions(QUERY, ID)).thenReturn(userData);
        mvc.perform(get("/search/delete")
                .param(QUERY_PARAM, QUERY)
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].[0]").value("participant0"))
                .andExpect(jsonPath("$.[0].[1]").value("participant0@participant.de"))
                .andExpect(jsonPath("$.length()").value(5));
        verify(searchService).getUserDeleteSuggestions(QUERY, ID);
    }

    @Test
    public void testGetDeleteUserSuggestionsInvalidParams() throws Exception {
        mvc.perform(get("/search/delete")
                .param(QUERY_PARAM, BLANK)
                .param(ID_PARAM, "0")
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(searchService, never()).getUserDeleteSuggestions(anyString(), anyInt());
    }

    @Test
    public void testGetCourseExperimentSuggestions() throws Exception {
        when(searchService.getCourseExperimentSuggestions(QUERY, ID)).thenReturn(experimentTableData);
        mvc.perform(get("/search/course/experiment")
                        .param(QUERY_PARAM, QUERY)
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].[0]").value("0"))
                .andExpect(jsonPath("$.[0].[1]").value("experiment0"))
                .andExpect(jsonPath("$.[0].[2]").value("description0"))
                .andExpect(jsonPath("$.[1].[0]").value("1"))
                .andExpect(jsonPath("$.[1].[1]").value("experiment1"))
                .andExpect(jsonPath("$.[1].[2]").value("description1"));
        verify(searchService).getCourseExperimentSuggestions(QUERY, ID);
    }

    @Test
    public void testGetCourseExperimentSuggestionsInvalidParams() throws Exception {
        mvc.perform(get("/search/course/experiment")
                        .param(QUERY_PARAM, QUERY)
                        .param(ID_PARAM, BLANK)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(searchService, never()).getCourseExperimentSuggestions(anyString(), anyInt());
    }

    @Test
    public void testGetCourseParticipantSuggestions() throws Exception {
        when(searchService.getCourseParticipantSuggestions(QUERY, ID)).thenReturn(userData);
        mvc.perform(get("/search/course/participant")
                        .param(QUERY_PARAM, QUERY)
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$.[0].[0]").value("participant0"))
                .andExpect(jsonPath("$.[0].[1]").value("participant0@participant.de"));
        verify(searchService).getCourseParticipantSuggestions(QUERY, ID);
    }

    @Test
    public void testGetCourseParticipantSuggestionsInvalidParams() throws Exception {
        mvc.perform(get("/search/course/participant")
                        .param(QUERY_PARAM, QUERY)
                        .param(ID_PARAM, "0")
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(searchService, never()).getCourseParticipantSuggestions(anyString(), anyInt());
    }

    @Test
    public void testGetCourseExperimentDeleteSuggestions() throws Exception {
        when(searchService.getCourseExperimentDeleteSuggestions(QUERY, ID)).thenReturn(experimentTableData);
        mvc.perform(get("/search/course/delete/experiment")
                        .param(QUERY_PARAM, QUERY)
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].[0]").value("0"))
                .andExpect(jsonPath("$.[0].[1]").value("experiment0"))
                .andExpect(jsonPath("$.[0].[2]").value("description0"))
                .andExpect(jsonPath("$.[1].[0]").value("1"))
                .andExpect(jsonPath("$.[1].[1]").value("experiment1"))
                .andExpect(jsonPath("$.[1].[2]").value("description1"));
        verify(searchService).getCourseExperimentDeleteSuggestions(QUERY, ID);
    }

    @Test
    public void testGetCourseExperimentDeleteSuggestionsInvalidParams() throws Exception {
        mvc.perform(get("/search/course/delete/experiment")
                        .param(QUERY_PARAM, BLANK)
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(searchService, never()).getCourseExperimentDeleteSuggestions(anyString(), anyInt());
    }

    @Test
    public void testGetCourseParticipantDeleteSuggestions() throws Exception {
        when(searchService.getCourseParticipantDeleteSuggestions(QUERY, ID)).thenReturn(userData);
        mvc.perform(get("/search/course/delete/participant")
                        .param(QUERY_PARAM, QUERY)
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$.[0].[0]").value("participant0"))
                .andExpect(jsonPath("$.[0].[1]").value("participant0@participant.de"));
        verify(searchService).getCourseParticipantDeleteSuggestions(QUERY, ID);
    }

    @Test
    public void testGetCourseParticipantDeleteSuggestionsInvalidParams() throws Exception {
        mvc.perform(get("/search/course/delete/participant")
                        .param(QUERY_PARAM, QUERY)
                        .param(ID_PARAM, BLANK)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(searchService, never()).getCourseParticipantDeleteSuggestions(anyString(), anyInt());
    }

    private void addUserData(int number) {
        userData = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            String user = "participant" + i;
            String[] userInfo = new String[]{user, user + "@participant.de"};
            userData.add(userInfo);
        }
    }

    private void addExperimentData(int number) {
        experimentData = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            String[] userInfo = new String[]{"experiment" + i, "description" + i};
            experimentData.add(userInfo);
        }
    }

    private void addUserProjectionData(int number) {
        userProjectionData = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            String user = "participant" + i;
            String[] userInfo = new String[]{String.valueOf(i), user, user + "@participant.de", PARTICIPANT};
            userProjectionData.add(userInfo);
        }
    }

    private void addExperimentTableData(int number) {
        experimentTableData = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            String[] userInfo = new String[]{String.valueOf(i), "experiment" + i, "description" + i};
            experimentTableData.add(userInfo);
        }
    }

    private void addCourseTableData(int number) {
        courseTableData = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            String[] courseInfo = new String[]{String.valueOf(i), "course" + i, "description" + i};
            courseTableData.add(courseInfo);
        }
    }

}
