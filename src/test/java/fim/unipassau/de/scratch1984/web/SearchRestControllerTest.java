package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.service.SearchService;
import fim.unipassau.de.scratch1984.web.controller.SearchRestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchRestControllerTest {

    @InjectMocks
    private SearchRestController searchRestController;

    @Mock
    private SearchService searchService;

    private static final String ID_STRING = "1";
    private static final String PAGE_STRING = "2";
    private static final String QUERY = "participant";
    private static final String BLANK = "   ";
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
        addUserProjectionData(2);
        addExperimentTableData(2);
        addCourseTableData(3);
    }

    @Test
    public void testGetSearchSuggestions() {
        when(searchService.getSearchSuggestions(QUERY)).thenReturn(experimentData);
        List<String[]> data = searchRestController.getSearchSuggestions(QUERY);
        assertEquals(8, data.size());
        verify(searchService).getSearchSuggestions(QUERY);
    }

    @Test
    public void testGetSearchSuggestionsQueryBlank() {
        assertTrue(searchRestController.getSearchSuggestions(BLANK).isEmpty());
        verify(searchService, never()).getSearchSuggestions(anyString());
    }

    @Test
    public void testGetSearchSuggestionsQueryNull() {
        assertTrue(searchRestController.getSearchSuggestions(null).isEmpty());
        verify(searchService, never()).getSearchSuggestions(anyString());
    }

    @Test
    public void testGetMoreUsers() {
        when(searchService.getNextUsers(QUERY, PAGE)).thenReturn(userProjectionData);
        assertEquals(2, searchRestController.getMoreUsers(QUERY, PAGE_STRING).size());
        verify(searchService).getNextUsers(QUERY, PAGE);
    }

    @Test
    public void testGetMoreUsersInvalidPage() {
        assertTrue(searchRestController.getMoreUsers(QUERY, "0").isEmpty());
        verify(searchService, never()).getNextUsers(anyString(), anyInt());
    }

    @Test
    public void testGetMoreExperiments() {
        when(searchService.getNextExperiments(QUERY, PAGE)).thenReturn(experimentTableData);
        assertEquals(2, searchRestController.getMoreExperiments(QUERY, PAGE_STRING).size());
        verify(searchService).getNextExperiments(QUERY, PAGE);
    }

    @Test
    public void testGetMoreExperimentsPageBlank() {
        assertTrue(searchRestController.getMoreExperiments(QUERY, BLANK).isEmpty());
        verify(searchService, never()).getNextExperiments(anyString(), anyInt());
    }

    @Test
    public void testGetMoreExperimentsPageNull() {
        assertTrue(searchRestController.getMoreExperiments(QUERY, null).isEmpty());
        verify(searchService, never()).getNextExperiments(anyString(), anyInt());
    }

    @Test
    public void testGetMoreCourses() {
        when(searchService.getNextCourses(QUERY, PAGE)).thenReturn(courseTableData);
        assertEquals(3, searchRestController.getMoreCourses(QUERY, PAGE_STRING).size());
        verify(searchService).getNextCourses(QUERY, PAGE);
    }

    @Test
    public void testGetMoreCoursesPageBlank() {
        assertTrue(searchRestController.getMoreCourses(QUERY, BLANK).isEmpty());
        verify(searchService, never()).getNextCourses(anyString(), anyInt());
    }

    @Test
    public void testGetMoreCoursesPageNull() {
        assertTrue(searchRestController.getMoreCourses(QUERY, null).isEmpty());
        verify(searchService, never()).getNextCourses(anyString(), anyInt());
    }

    @Test
    public void testGetUserSuggestions() {
        when(searchService.getUserSuggestions(QUERY, ID)).thenReturn(userData);
        List<String[]> data = searchRestController.getUserSuggestions(QUERY, ID_STRING);
        assertEquals(5, data.size());
        verify(searchService).getUserSuggestions(anyString(), anyInt());
    }

    @Test
    public void testGetUserSuggestionsInvalidId() {
        assertTrue(searchRestController.getUserSuggestions(QUERY, "0").isEmpty());
        verify(searchService, never()).getUserSuggestions(anyString(), anyInt());
    }

    @Test
    public void testGetUserSuggestionsIdBlank() {
        assertTrue(searchRestController.getUserSuggestions(QUERY, BLANK).isEmpty());
        verify(searchService, never()).getUserSuggestions(anyString(), anyInt());
    }

    @Test
    public void testGetUserSuggestionsIdNull() {
        assertTrue(searchRestController.getUserSuggestions(QUERY, null).isEmpty());
        verify(searchService, never()).getUserSuggestions(anyString(), anyInt());
    }

    @Test
    public void testGetUserSuggestionsQueryBlank() {
        assertTrue(searchRestController.getUserSuggestions(BLANK, ID_STRING).isEmpty());
        verify(searchService, never()).getUserSuggestions(anyString(), anyInt());
    }

    @Test
    public void testGetUserSuggestionsQueryNull() {
        assertTrue(searchRestController.getUserSuggestions(null, ID_STRING).isEmpty());
        verify(searchService, never()).getUserSuggestions(anyString(), anyInt());
    }

    @Test
    public void testGetDeleteUserSuggestions() {
        when(searchService.getUserDeleteSuggestions(QUERY, ID)).thenReturn(userData);
        List<String[]> data = searchRestController.getDeleteUserSuggestions(QUERY, ID_STRING);
        assertEquals(5, data.size());
        verify(searchService).getUserDeleteSuggestions(anyString(), anyInt());
    }

    @Test
    public void testGetDeleteUserSuggestionsInvalidParams() {
        assertTrue(searchRestController.getDeleteUserSuggestions(BLANK, "0").isEmpty());
        verify(searchService, never()).getUserDeleteSuggestions(anyString(), anyInt());
    }

    @Test
    public void testGetCourseExperimentSuggestions() {
        when(searchService.getCourseExperimentSuggestions(QUERY, ID)).thenReturn(experimentTableData);
        List<String[]> data = searchRestController.getCourseExperimentSuggestions(QUERY, ID_STRING);
        assertEquals(2, data.size());
        verify(searchService).getCourseExperimentSuggestions(QUERY, ID);
    }

    @Test
    public void testGetCourseExperimentSuggestionsInvalidParams() {
        assertTrue(searchRestController.getCourseExperimentSuggestions(null, ID_STRING).isEmpty());
        verify(searchService, never()).getCourseExperimentSuggestions(anyString(), anyInt());
    }

    @Test
    public void testGetCourseParticipantSuggestions() {
        when(searchService.getCourseParticipantSuggestions(QUERY, ID)).thenReturn(userData);
        assertEquals(5, searchRestController.getCourseParticipantSuggestions(QUERY, ID_STRING).size());
        verify(searchService).getCourseParticipantSuggestions(QUERY, ID);
    }

    @Test
    public void testGetCourseParticipantSuggestionsInvalidParams() {
        assertTrue(searchRestController.getCourseParticipantSuggestions(QUERY, BLANK).isEmpty());
        verify(searchService, never()).getCourseParticipantSuggestions(anyString(), anyInt());
    }

    @Test
    public void testGetCourseExperimentDeleteSuggestions() {
        when(searchService.getCourseExperimentDeleteSuggestions(QUERY, ID)).thenReturn(experimentTableData);
        List<String[]> data = searchRestController.getCourseExperimentDeleteSuggestions(QUERY, ID_STRING);
        assertEquals(2, data.size());
        verify(searchService).getCourseExperimentDeleteSuggestions(QUERY, ID);
    }

    @Test
    public void testGetCourseExperimentDeleteSuggestionsInvalidParams() {
        assertTrue(searchRestController.getCourseExperimentDeleteSuggestions(QUERY, QUERY).isEmpty());
        verify(searchService, never()).getCourseExperimentDeleteSuggestions(anyString(), anyInt());
    }

    @Test
    public void testGetCourseParticipantDeleteSuggestions() {
        when(searchService.getCourseParticipantDeleteSuggestions(QUERY, ID)).thenReturn(userData);
        assertEquals(5, searchRestController.getCourseParticipantDeleteSuggestions(QUERY, ID_STRING).size());
        verify(searchService).getCourseParticipantDeleteSuggestions(QUERY, ID);
    }

    @Test
    public void testGetCourseParticipantDeleteSuggestionsInvalidParams() {
        assertTrue(searchRestController.getCourseParticipantDeleteSuggestions(BLANK, ID_STRING).isEmpty());
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
            String[] experimentInfo = new String[]{String.valueOf(i), "experiment" + i, "description" + i};
            experimentTableData.add(experimentInfo);
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
