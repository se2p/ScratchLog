package fim.unipassau.de.scratch1984.application;

import fim.unipassau.de.scratch1984.application.service.SearchService;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentSearchProjection;
import fim.unipassau.de.scratch1984.persistence.projection.UserProjection;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchServiceTest {

    @InjectMocks
    private SearchService searchService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExperimentRepository experimentRepository;

    private static final String PARTICIPANT = "PARTICIPANT";
    private static final String LANGUAGE = "ENGLISH";
    private static final String USERNAME1 = "user1";
    private static final String USERNAME2 = "user2";
    private static final String USERNAME3 = "user3";
    private static final String EMAIL1 = "part1@test.de";
    private static final String EMAIL2 = "part2@test.de";
    private static final String EMAIL3 = "part3@test.de";
    private static final String TITLE1 = "experiment1";
    private static final String TITLE2 = "experiment2";
    private static final String QUERY = "user";
    private static final String SUGGESTION_QUERY = "r";
    private static final int ID = 1;
    private final List<UserProjection> users = addUserSuggestions();
    private final List<ExperimentSearchProjection> experiments = addExperimentSuggestions();

    @Test
    public void testGetSearchSuggestions() {
        when(userRepository.findUserSuggestions(SUGGESTION_QUERY)).thenReturn(users);
        when(experimentRepository.findExperimentSuggestions(SUGGESTION_QUERY)).thenReturn(experiments);
        List<String[]> suggestions = searchService.getSearchSuggestions(SUGGESTION_QUERY);
        assertAll(
                () -> assertEquals(5, suggestions.size()),
                () -> assertTrue(suggestions.stream().anyMatch(suggestion -> suggestion[1].equals(TITLE1))),
                () -> assertTrue(suggestions.stream().anyMatch(suggestion -> suggestion[1].equals(TITLE2))),
                () -> assertTrue(suggestions.stream().anyMatch(suggestion -> suggestion[0].equals(USERNAME1))),
                () -> assertTrue(suggestions.stream().anyMatch(suggestion -> suggestion[0].equals(USERNAME2))),
                () -> assertTrue(suggestions.stream().anyMatch(suggestion -> suggestion[0].equals(USERNAME3)))
        );
        verify(userRepository).findUserSuggestions(SUGGESTION_QUERY);
        verify(experimentRepository).findExperimentSuggestions(SUGGESTION_QUERY);
    }

    @Test
    public void testGetSearchSuggestionsNone() {
        List<String[]> suggestions = searchService.getSearchSuggestions(SUGGESTION_QUERY);
        assertEquals(0, suggestions.size());
        verify(userRepository).findUserSuggestions(SUGGESTION_QUERY);
        verify(experimentRepository).findExperimentSuggestions(SUGGESTION_QUERY);
    }

    @Test
    public void testGetUserSuggestions() {
        when(userRepository.findParticipantSuggestions(QUERY, ID)).thenReturn(users);
        List<String[]> userInfo = searchService.getUserSuggestions(QUERY, ID);
        String[] firstUser = userInfo.get(0);
        String[] secondUser = userInfo.get(1);
        String[] thirdUser = userInfo.get(2);
        assertAll(
                () -> assertEquals(3, userInfo.size()),
                () -> assertEquals(USERNAME1, firstUser[0]),
                () -> assertEquals(EMAIL1, firstUser[1]),
                () -> assertEquals(USERNAME2, secondUser[0]),
                () -> assertEquals(EMAIL2, secondUser[1]),
                () -> assertEquals(USERNAME3, thirdUser[0]),
                () -> assertEquals(EMAIL3, thirdUser[1])
        );
        verify(userRepository).findParticipantSuggestions(QUERY, ID);
    }

    @Test
    public void testGetUserSuggestionsNone() {
        List<String[]> userInfo = searchService.getUserSuggestions(QUERY, ID);
        assertEquals(0, userInfo.size());
        verify(userRepository).findParticipantSuggestions(QUERY, ID);
    }

    @Test
    public void testGetUserDeleteSuggestions() {
        when(userRepository.findDeleteParticipantSuggestions(QUERY, ID)).thenReturn(users);
        List<String[]> userInfo = searchService.getUserDeleteSuggestions(QUERY, ID);
        String[] firstUser = userInfo.get(0);
        String[] secondUser = userInfo.get(1);
        String[] thirdUser = userInfo.get(2);
        assertAll(
                () -> assertEquals(3, userInfo.size()),
                () -> assertEquals(USERNAME1, firstUser[0]),
                () -> assertEquals(EMAIL1, firstUser[1]),
                () -> assertEquals(USERNAME2, secondUser[0]),
                () -> assertEquals(EMAIL2, secondUser[1]),
                () -> assertEquals(USERNAME3, thirdUser[0]),
                () -> assertEquals(EMAIL3, thirdUser[1])
        );
        verify(userRepository).findDeleteParticipantSuggestions(QUERY, ID);
    }

    @Test
    public void testGetUserDeleteSuggestionsNone() {
        List<String[]> userInfo = searchService.getUserDeleteSuggestions(QUERY, ID);
        assertEquals(0, userInfo.size());
        verify(userRepository).findDeleteParticipantSuggestions(QUERY, ID);
    }

    private List<UserProjection> addUserSuggestions() {
        List<UserProjection> projections = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            int id = i + 1;

            UserProjection projection = new UserProjection() {
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
                    return "part" + id + "@test.de";
                }

                @Override
                public String getRole() {
                    return PARTICIPANT;
                }
            };

            projections.add(projection);
        }

        return projections;
    }

    private List<ExperimentSearchProjection> addExperimentSuggestions() {
        List<ExperimentSearchProjection> projections = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            int id = i + 1;

            ExperimentSearchProjection projection = new ExperimentSearchProjection() {
                @Override
                public Integer getId() {
                    return id;
                }

                @Override
                public String getTitle() {
                    return "experiment" + id;
                }

                @Override
                public String getDescription() {
                    return "description" + id;
                }
            };

            projections.add(projection);
        }

        return projections;
    }
}
