package fim.unipassau.de.scratch1984.integration;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratch1984.web.controller.HomeController;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(SpringExtension.class)
@WebMvcTest(HomeController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class HomeControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ExperimentService experimentService;

    private static final String INDEX = "index";
    private static final String FINISH = "experiment-finish";
    private static final String ERROR = "redirect:/error";
    private static final String CURRENT = "3";
    private static final String LAST = "4";
    private static final String BLANK = "   ";
    private static final String INVALID_NUMBER = "-5";
    private static final String PAGE_PARAM = "page";
    private static final String LAST_PAGE = "lastPage";
    private static final String EXPERIMENTS = "experiments";
    private static final String ID_STRING = "1";
    private static final String THANKS = "thanks";
    private static final String ID_PARAM = "id";
    private final int pageNum = 3;
    private final int lastPage = 3;
    private static final int ID = 1;
    private static final ExperimentDTO experimentDTO = new ExperimentDTO(ID, "My Experiment", "description",
            "info", "postscript", true);
    private final Page<Experiment> experimentPage = new PageImpl<>(getExperiments(5));
    private final String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";
    private final HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
    private final CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

    @AfterEach
    public void resetService() {
        reset(experimentService);
    }

    @Test
    public void testGetIndexPage() throws Exception {
        mvc.perform(get("/")
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(INDEX));
        verify(experimentService, never()).getLastPage();
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void testGetIndexPageAdmin() throws Exception {
        when(experimentService.getLastPage()).thenReturn(lastPage);
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        mvc.perform(get("/")
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_PAGE, is(lastPage + 1)))
                .andExpect(model().attribute(PAGE_PARAM, is(1)))
                .andExpect(view().name(INDEX));
        verify(experimentService).getLastPage();
        verify(experimentService).getExperimentPage(any(PageRequest.class));
    }

    @Test
    public void testGetNextPage() throws Exception {
        when(experimentService.getLastPage()).thenReturn(lastPage);
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        mvc.perform(get("/next")
                .param(PAGE_PARAM, CURRENT)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_PAGE, is(lastPage + 1)))
                .andExpect(model().attribute(PAGE_PARAM, is(pageNum + 1)))
                .andExpect(view().name(INDEX));
        verify(experimentService).getLastPage();
        verify(experimentService).getExperimentPage(any(PageRequest.class));
    }

    @Test
    public void testGetNextPageCurrentBiggerLast() throws Exception {
        when(experimentService.getLastPage()).thenReturn(2);
        mvc.perform(get("/next")
                .param(PAGE_PARAM, LAST)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).getLastPage();
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
    }

    @Test
    public void testGetNextPageParamInvalid() throws Exception {
        when(experimentService.getLastPage()).thenReturn(lastPage);
        mvc.perform(get("/next")
                .param(PAGE_PARAM, INVALID_NUMBER)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).getLastPage();
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
    }

    @Test
    public void testGetPreviousPage() throws Exception {
        when(experimentService.getLastPage()).thenReturn(lastPage);
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        mvc.perform(get("/previous")
                .param(PAGE_PARAM, CURRENT)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_PAGE, is(lastPage + 1)))
                .andExpect(model().attribute(PAGE_PARAM, is(pageNum - 1)))
                .andExpect(view().name(INDEX));
        verify(experimentService).getLastPage();
        verify(experimentService).getExperimentPage(any(PageRequest.class));
    }

    @Test
    public void testGetPreviousPageLastSmallerCurrent() throws Exception {
        when(experimentService.getLastPage()).thenReturn(2);
        mvc.perform(get("/previous")
                .param(PAGE_PARAM, LAST)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).getLastPage();
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
    }

    @Test
    public void testGetPreviousPageCurrentFirstPage() throws Exception {
        when(experimentService.getLastPage()).thenReturn(lastPage);
        mvc.perform(get("/previous")
                .param(PAGE_PARAM, "1")
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).getLastPage();
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
    }

    @Test
    public void testGetPreviousPageParamInvalid() throws Exception {
        mvc.perform(get("/previous")
                .param(PAGE_PARAM, BLANK)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(experimentService).getLastPage();
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
    }

    @Test
    public void testGetFirstPage() throws Exception {
        when(experimentService.getLastPage()).thenReturn(lastPage);
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        mvc.perform(get("/first")
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_PAGE, is(lastPage + 1)))
                .andExpect(model().attribute(PAGE_PARAM, is(1)))
                .andExpect(view().name(INDEX));
        verify(experimentService).getLastPage();
        verify(experimentService).getExperimentPage(any(PageRequest.class));
    }

    @Test
    public void testGetLastPage() throws Exception {
        when(experimentService.getLastPage()).thenReturn(lastPage);
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        mvc.perform(get("/last")
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(EXPERIMENTS, is(experimentPage)))
                .andExpect(model().attribute(LAST_PAGE, is(lastPage + 1)))
                .andExpect(model().attribute(PAGE_PARAM, is(lastPage + 1)))
                .andExpect(view().name(INDEX));
        verify(experimentService).getLastPage();
        verify(experimentService).getExperimentPage(any(PageRequest.class));
    }

    @Test
    public void testGetExperimentFinishPage() throws Exception {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        mvc.perform(get("/finish")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(model().attribute(THANKS, is(experimentDTO.getPostscript())))
                .andExpect(view().name(FINISH));
        verify(experimentService).getExperiment(ID);
    }

    @Test
    public void testGetExperimentFinishPageNotFound() throws Exception {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/finish")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute(THANKS, nullValue()))
                .andExpect(view().name(ERROR));
        verify(experimentService).getExperiment(ID);
    }

    @Test
    public void testGetExperimentFinishPageInvalidId() throws Exception {
        mvc.perform(get("/finish")
                .param(ID_PARAM, INVALID_NUMBER)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().attribute(THANKS, nullValue()))
                .andExpect(view().name(ERROR));
        verify(experimentService, never()).getExperiment(anyInt());
    }

    private List<Experiment> getExperiments(int number) {
        List<Experiment> experiments = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            experiments.add(new Experiment(i, "Experiment " + i, "Description for experiment " + i, "", "", false));
        }
        return experiments;
    }
}
