package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.web.controller.HomeController;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
    private Model model;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ResourceBundle resourceBundle;

    @Mock
    private LocaleResolver localeResolver;

    private static final String ADMIN = "ROLE_ADMIN";
    private static final String INDEX = "index";
    private static final String LOGIN = "login";
    private static final String FINISH = "experiment-finish";
    private static final String ERROR = "redirect:/error";
    private static final String CURRENT = "3";
    private static final String LAST = "4";
    private static final String BLANK = "   ";
    private static final String ID_STRING = "1";
    private static final String THANKS = "thanks";
    private static final int LAST_PAGE = 3;
    private static final int ID = 1;
    private static final ExperimentDTO experimentDTO = new ExperimentDTO(ID, "My Experiment", "description",
            "info", "postscript", true);
    private final Page<Experiment> experimentPage = new PageImpl<>(getExperiments(5));

    @BeforeEach
    public void setup() {
        experimentDTO.setPostscript("postscript");
    }

    @Test
    public void testGetIndexPage() {
        when(httpServletRequest.isUserInRole(ADMIN)).thenReturn(true);
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        when(experimentService.getLastPage()).thenReturn(1);
        assertEquals(INDEX, homeController.getIndexPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService).getExperimentPage(any(PageRequest.class));
        verify(experimentService).getLastPage();
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetIndexPageNoAdmin() {
        assertEquals(INDEX, homeController.getIndexPage(httpServletRequest, model));
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(experimentService, never()).getLastPage();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPage() {
        when(experimentService.getLastPage()).thenReturn(LAST_PAGE);
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        assertEquals(INDEX, homeController.getNextPage(CURRENT, model));
        verify(experimentService).getLastPage();
        verify(experimentService).getExperimentPage(any(PageRequest.class));
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPageInvalidCurrent() {
        when(experimentService.getLastPage()).thenReturn(LAST_PAGE);
        assertEquals(ERROR, homeController.getNextPage(BLANK, model));
        verify(experimentService).getLastPage();
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPageInvalidCurrentEqualLast() {
        when(experimentService.getLastPage()).thenReturn(LAST_PAGE);
        assertEquals(ERROR, homeController.getNextPage(LAST, model));
        verify(experimentService).getLastPage();
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPageInvalidCurrentNull() {
        assertEquals(ERROR, homeController.getNextPage(null, model));
        verify(experimentService, never()).getLastPage();
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPrevious() {
        when(experimentService.getLastPage()).thenReturn(LAST_PAGE);
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        assertEquals(INDEX, homeController.getPreviousPage(CURRENT, model));
        verify(experimentService).getLastPage();
        verify(experimentService).getExperimentPage(any(PageRequest.class));
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousCurrent0() {
        when(experimentService.getLastPage()).thenReturn(LAST_PAGE);
        assertEquals(ERROR, homeController.getPreviousPage("0", model));
        verify(experimentService).getLastPage();
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousLastSmallerCurrent() {
        when(experimentService.getLastPage()).thenReturn(1);
        assertEquals(ERROR, homeController.getPreviousPage(CURRENT, model));
        verify(experimentService).getLastPage();
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousCurrentNull() {
        assertEquals(ERROR, homeController.getPreviousPage(null, model));
        verify(experimentService, never()).getLastPage();
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetFirstPage() {
        when(experimentService.getLastPage()).thenReturn(LAST_PAGE);
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        assertEquals(INDEX, homeController.getFirstPage(model));
        verify(experimentService).getLastPage();
        verify(experimentService).getExperimentPage(any(PageRequest.class));
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetLastPage() {
        when(experimentService.getLastPage()).thenReturn(LAST_PAGE);
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        assertEquals(INDEX, homeController.getLastPage(model));
        verify(experimentService).getLastPage();
        verify(experimentService).getExperimentPage(any(PageRequest.class));
        verify(model, times(3)).addAttribute(anyString(), any());
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
        assertEquals(ERROR, homeController.getExperimentFinishPage(ID_STRING, model));
        verify(experimentService).getExperiment(ID);
        verify(model, never()).addAttribute(anyString(), anyString());
    }

    @Test
    public void testGetExperimentFinishPageExperimentIdInvalid() {
        assertEquals(ERROR, homeController.getExperimentFinishPage(BLANK, model));
        verify(experimentService, never()).getExperiment(anyInt());
        verify(model, never()).addAttribute(anyString(), anyString());
    }

    @Test
    public void testGetExperimentFinishPageExperimentIdNull() {
        assertEquals(ERROR, homeController.getExperimentFinishPage(null, model));
        verify(experimentService, never()).getExperiment(anyInt());
        verify(model, never()).addAttribute(anyString(), anyString());
    }

    private List<Experiment> getExperiments(int number) {
        List<Experiment> experiments = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            experiments.add(new Experiment(i, "Experiment " + i, "Description for experiment " + i, "", "", false));
        }
        return experiments;
    }
}
