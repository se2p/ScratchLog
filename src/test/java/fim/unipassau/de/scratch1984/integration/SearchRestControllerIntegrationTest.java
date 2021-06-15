package fim.unipassau.de.scratch1984.integration;

import fim.unipassau.de.scratch1984.application.service.SearchService;
import fim.unipassau.de.scratch1984.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratch1984.web.controller.SearchRestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
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
    private static final String QUERY = "participant";
    private static final String BLANK = "   ";
    private static final String QUERY_PARAM = "query";
    private static final String ID_PARAM = "id";
    private static final int ID = 1;
    private List<String[]> userData;
    private final String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";
    private final HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
    private final CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

    @BeforeEach
    public void setup() {
        addUserData(5);
    }

    @Test
    public void testGetUserSuggestions() throws Exception {
        when(searchService.getUserSuggestions(QUERY, ID)).thenReturn(userData);
        mvc.perform(get("/search/user")
                .param(QUERY_PARAM, QUERY)
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
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
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
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
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
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
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(searchService, never()).getUserSuggestions(anyString(), anyInt());
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
