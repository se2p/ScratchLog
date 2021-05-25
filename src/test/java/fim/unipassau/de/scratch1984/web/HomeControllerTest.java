package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.web.controller.HomeController;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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

    private static final String ADMIN = "ROLE_ADMIN";
    private static final String INDEX = "index";
    private static final String LOGIN = "login";
    private static final String ERROR = "redirect:/error";
    private static final String CURRENT = "3";
    private static final String LAST = "4";
    private static final String BLANK = "   ";
    private static final String INVALID_NUMBER = "-5";
    private final Page<Experiment> experimentPage = new PageImpl<>(getExperiments(5));

    @Test
    public void testGetIndexPage() {
        when(httpServletRequest.isUserInRole(ADMIN)).thenReturn(true);
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        when(experimentService.getLastPage()).thenReturn(1);
        String returnString = homeController.getIndexPage(httpServletRequest, model);
        assertEquals(INDEX, returnString);
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService).getExperimentPage(any(PageRequest.class));
        verify(experimentService).getLastPage();
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetIndexPageNoAdmin() {
        String returnString = homeController.getIndexPage(httpServletRequest, model);
        assertEquals(INDEX, returnString);
        verify(httpServletRequest).isUserInRole(ADMIN);
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(experimentService, never()).getLastPage();
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPage() {
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        String returnString = homeController.getNextPage(CURRENT, LAST, model);
        assertEquals(INDEX, returnString);
        verify(experimentService).getExperimentPage(any(PageRequest.class));
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPageInvalidCurrent() {
        String returnString = homeController.getNextPage(BLANK, LAST, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPageInvalidLast() {
        String returnString = homeController.getNextPage(CURRENT, INVALID_NUMBER, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPageInvalidCurrentEqualLast() {
        String returnString = homeController.getNextPage(LAST, LAST, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPageInvalidCurrentNull() {
        String returnString = homeController.getNextPage(null, LAST, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetNextPageInvalidLastNull() {
        String returnString = homeController.getNextPage(CURRENT, null, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPrevious() {
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        String returnString = homeController.getPreviousPage(CURRENT, LAST, model);
        assertEquals(INDEX, returnString);
        verify(experimentService).getExperimentPage(any(PageRequest.class));
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousCurrent0() {
        String returnString = homeController.getPreviousPage("0", LAST, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousLastInvalid() {
        String returnString = homeController.getPreviousPage(CURRENT, INVALID_NUMBER, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousLastSmallerCurrent() {
        String returnString = homeController.getPreviousPage(CURRENT, "2", model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousCurrentNull() {
        String returnString = homeController.getPreviousPage(null, LAST, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetPreviousLastNull() {
        String returnString = homeController.getPreviousPage(CURRENT, null, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetFirstPage() {
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        String returnString = homeController.getFirstPage(LAST, model);
        assertEquals(INDEX, returnString);
        verify(experimentService).getExperimentPage(any(PageRequest.class));
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetFirstPageLastInvalid() {
        String returnString = homeController.getFirstPage(INVALID_NUMBER, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetFirstPageLastNull() {
        String returnString = homeController.getFirstPage(null, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetLastPage() {
        when(experimentService.getExperimentPage(any(PageRequest.class))).thenReturn(experimentPage);
        String returnString = homeController.getLastPage(LAST, model);
        assertEquals(INDEX, returnString);
        verify(experimentService).getExperimentPage(any(PageRequest.class));
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetLastPageLastInvalid() {
        String returnString = homeController.getLastPage(INVALID_NUMBER, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetLastPageLastNull() {
        String returnString = homeController.getLastPage(null, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperimentPage(any(PageRequest.class));
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetLoginPage() {
        String returnString = homeController.getLoginPage(new UserDTO());
        assertEquals(LOGIN, returnString);
    }

    private List<Experiment> getExperiments(int number) {
        List<Experiment> experiments = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            experiments.add(new Experiment(i, "Experiment " + i, "Description for experiment " + i, "", false));
        }
        return experiments;
    }
}
