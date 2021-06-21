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
    private static final String QUERY = "participant";
    private static final String BLANK = "   ";
    private static final int ID = 1;
    private List<String[]> userData;

    @BeforeEach
    public void setup() {
        addUserData(5);
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

    private void addUserData(int number) {
        userData = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            String user = "participant" + i;
            String[] userInfo = new String[]{user, user + "@participant.de"};
            userData.add(userInfo);
        }
    }
}
