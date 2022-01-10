package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.service.SearchService;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentTableProjection;
import fim.unipassau.de.scratch1984.persistence.projection.UserProjection;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.controller.SearchController;
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
    private static final String LONG_QUERY = "queeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"
            + "eeeeeeeeeeeeeeeeeeeeeeeeeery";
    private static final String BLANK = "  ";
    private static final String ADMIN = "ADMIN";
    private static final String PARTICIPANT = "PARTICIPANT";
    private static final String SEARCH = "search";
    private static final String ERROR = "redirect:/error";
    private static final int COUNT = 25;
    private List<UserProjection> users;
    private List<ExperimentTableProjection> experiments;

    @Test
    public void testGetSearchPage() {
        users = getUsers(Constants.PAGE_SIZE);
        experiments = getExperiments(Constants.PAGE_SIZE);
        when(searchService.getUserCount(QUERY)).thenReturn(COUNT);
        when(searchService.getExperimentCount(QUERY)).thenReturn(COUNT);
        when(searchService.getUserList(QUERY, Constants.PAGE_SIZE)).thenReturn(users);
        when(searchService.getExperimentList(QUERY, Constants.PAGE_SIZE)).thenReturn(experiments);
        assertEquals(SEARCH, searchController.getSearchPage(QUERY, model));
        verify(searchService).getUserCount(QUERY);
        verify(searchService).getExperimentCount(QUERY);
        verify(searchService).getUserList(QUERY, Constants.PAGE_SIZE);
        verify(searchService).getExperimentList(QUERY, Constants.PAGE_SIZE);
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetSearchPageQueryBlank() {
        assertEquals(SEARCH, searchController.getSearchPage(BLANK, model));
        verify(searchService, never()).getUserCount(anyString());
        verify(searchService, never()).getExperimentCount(anyString());
        verify(searchService, never()).getUserList(anyString(), anyInt());
        verify(searchService, never()).getExperimentList(anyString(), anyInt());
        verify(model, times(6)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetSearchPageQueryTooLong() {
        assertEquals(ERROR, searchController.getSearchPage(LONG_QUERY, model));
        verify(searchService, never()).getUserCount(anyString());
        verify(searchService, never()).getExperimentCount(anyString());
        verify(searchService, never()).getUserList(anyString(), anyInt());
        verify(searchService, never()).getExperimentList(anyString(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetSearchPageQueryNull() {
        assertEquals(ERROR, searchController.getSearchPage(null, model));
        verify(searchService, never()).getUserCount(anyString());
        verify(searchService, never()).getExperimentCount(anyString());
        verify(searchService, never()).getUserList(anyString(), anyInt());
        verify(searchService, never()).getExperimentList(anyString(), anyInt());
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
                public String getRole() {
                    return id == 1 ? ADMIN : PARTICIPANT;
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
