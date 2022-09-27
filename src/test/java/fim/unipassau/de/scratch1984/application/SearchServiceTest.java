package fim.unipassau.de.scratch1984.application;

import fim.unipassau.de.scratch1984.application.service.SearchService;
import fim.unipassau.de.scratch1984.persistence.projection.CourseTableProjection;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentTableProjection;
import fim.unipassau.de.scratch1984.persistence.projection.UserProjection;
import fim.unipassau.de.scratch1984.persistence.repository.CourseRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchServiceTest {

    @InjectMocks
    private SearchService searchService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ExperimentRepository experimentRepository;

    private static final String PARTICIPANT = "PARTICIPANT";
    private static final String USERNAME1 = "user1";
    private static final String USERNAME2 = "user2";
    private static final String USERNAME3 = "user3";
    private static final String EMAIL1 = "part1@test.de";
    private static final String EMAIL2 = "part2@test.de";
    private static final String EMAIL3 = "part3@test.de";
    private static final String TITLE1 = "experiment1";
    private static final String TITLE2 = "experiment2";
    private static final String TITLE3 = "Course 1";
    private static final String QUERY = "user";
    private static final String SUGGESTION_QUERY = "r";
    private static final String BLANK = "  ";
    private static final int ID = 1;
    private static final int LIMIT = Constants.PAGE_SIZE;
    private static final int COUNT = 25;
    private static final int PAGE = 1;
    private final List<UserProjection> users = addUserSuggestions();
    private final List<ExperimentTableProjection> experiments = addExperimentSuggestions();
    private final List<CourseTableProjection> courses = addCourseSuggestions();

    @Test
    public void testGetUserList() {
        when(userRepository.findUserResults(QUERY, LIMIT, 0)).thenReturn(users);
        List<UserProjection> userProjections = searchService.getUserList(QUERY, LIMIT);
        assertAll(
                () -> assertEquals(3, userProjections.size()),
                () -> assertTrue(userProjections.stream().anyMatch(user -> user.getId() == ID)),
                () -> assertTrue(userProjections.stream().anyMatch(user -> user.getId() == ID + 1)),
                () -> assertTrue(userProjections.stream().anyMatch(user -> user.getId() == ID + 2))
        );
        verify(userRepository).findUserResults(QUERY, LIMIT, 0);
    }

    @Test
    public void testGetUserListLimitInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getUserList(QUERY, 9)
        );
        verify(userRepository, never()).findUserResults(anyString(), anyInt(), anyInt());
    }

    @Test
    public void testGetUserListQueryBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getUserList(BLANK, LIMIT)
        );
        verify(userRepository, never()).findUserResults(anyString(), anyInt(), anyInt());
    }

    @Test
    public void testGetUserListQueryNull() {
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getUserList(null, LIMIT)
        );
        verify(userRepository, never()).findUserResults(anyString(), anyInt(), anyInt());
    }

    @Test
    public void testGetExperimentList() {
        when(experimentRepository.findExperimentResults(QUERY, LIMIT, 0)).thenReturn(experiments);
        List<ExperimentTableProjection> experimentList = searchService.getExperimentList(QUERY, LIMIT);
        assertAll(
                () -> assertEquals(2, experimentList.size()),
                () -> assertTrue(experimentList.stream().anyMatch(experiment -> experiment.getId() == ID)),
                () -> assertTrue(experimentList.stream().anyMatch(experiment -> experiment.getId() == ID + 1))
        );
        verify(experimentRepository).findExperimentResults(QUERY, LIMIT, 0);
    }

    @Test
    public void testGetExperimentListLimitInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getExperimentList(QUERY, 0)
        );
        verify(experimentRepository, never()).findExperimentResults(anyString(), anyInt(), anyInt());
    }

    @Test
    public void testGetExperimentListQueryBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getExperimentList(BLANK, LIMIT)
        );
        verify(experimentRepository, never()).findExperimentResults(anyString(), anyInt(), anyInt());
    }

    @Test
    public void testGetExperimentListQueryNull() {
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getExperimentList(null, LIMIT)
        );
        verify(experimentRepository, never()).findExperimentResults(anyString(), anyInt(), anyInt());
    }

    @Test
    public void testGetCourseList() {
        when(courseRepository.findCourseResults(QUERY, LIMIT, 0)).thenReturn(courses);
        List<CourseTableProjection> courseList = searchService.getCourseList(QUERY, LIMIT);
        assertAll(
                () -> assertEquals(1, courseList.size()),
                () -> assertTrue(courseList.stream().anyMatch(course -> course.getId() == ID))
        );
        verify(courseRepository).findCourseResults(QUERY, LIMIT, 0);
    }

    @Test
    public void testGetCourseListLimitInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getCourseList(QUERY, 0)
        );
        verify(courseRepository, never()).findCourseResults(anyString(), anyInt(), anyInt());
    }

    @Test
    public void testGetCourseListQueryBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getCourseList(BLANK, LIMIT)
        );
        verify(courseRepository, never()).findCourseResults(anyString(), anyInt(), anyInt());
    }

    @Test
    public void testGetCourseListQueryNull() {
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getCourseList(null, LIMIT)
        );
        verify(courseRepository, never()).findCourseResults(anyString(), anyInt(), anyInt());
    }

    @Test
    public void testGetUserCount() {
        when(userRepository.getUserResultsCount(QUERY)).thenReturn(COUNT);
        assertEquals(COUNT, searchService.getUserCount(QUERY));
        verify(userRepository).getUserResultsCount(QUERY);
    }

    @Test
    public void testGetUserCountQueryBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getUserCount(BLANK)
        );
        verify(userRepository, never()).getUserResultsCount(anyString());
    }

    @Test
    public void testGetUserCountQueryNull() {
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getUserCount(null)
        );
        verify(userRepository, never()).getUserResultsCount(anyString());
    }

    @Test
    public void testGetExperimentCount() {
        when(experimentRepository.getExperimentResultsCount(QUERY)).thenReturn(COUNT);
        assertEquals(COUNT, searchService.getExperimentCount(QUERY));
        verify(experimentRepository).getExperimentResultsCount(QUERY);
    }

    @Test
    public void testGetExperimentCountQueryBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getExperimentCount(BLANK)
        );
        verify(experimentRepository, never()).getExperimentResultsCount(anyString());
    }

    @Test
    public void testGetExperimentCountQueryNull() {
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getExperimentCount(null)
        );
        verify(experimentRepository, never()).getExperimentResultsCount(anyString());
    }

    @Test
    public void testGetCourseCount() {
        when(courseRepository.getCourseResultsCount(QUERY)).thenReturn(COUNT);
        assertEquals(COUNT, searchService.getCourseCount(QUERY));
        verify(courseRepository).getCourseResultsCount(QUERY);
    }

    @Test
    public void testGetCourseCountQueryBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getCourseCount(BLANK)
        );
        verify(courseRepository, never()).getCourseResultsCount(anyString());
    }

    @Test
    public void testGetCourseCountQueryNull() {
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getCourseCount(null)
        );
        verify(courseRepository, never()).getCourseResultsCount(anyString());
    }

    @Test
    public void testGetSearchSuggestions() {
        when(userRepository.findUserSuggestions(SUGGESTION_QUERY, Constants.MAX_SEARCH_RESULTS)).thenReturn(users);
        when(experimentRepository.findExperimentSuggestions(SUGGESTION_QUERY,
                Constants.MAX_SEARCH_RESULTS)).thenReturn(experiments);
        when(courseRepository.findCourseSuggestions(SUGGESTION_QUERY,
                Constants.MAX_SEARCH_RESULTS)).thenReturn(courses);
        List<String[]> suggestions = searchService.getSearchSuggestions(SUGGESTION_QUERY);
        assertAll(
                () -> assertEquals(6, suggestions.size()),
                () -> assertTrue(suggestions.stream().anyMatch(suggestion -> suggestion[2].equals(TITLE1))),
                () -> assertTrue(suggestions.stream().anyMatch(suggestion -> suggestion[2].equals(TITLE2))),
                () -> assertTrue(suggestions.stream().anyMatch(suggestion -> suggestion[1].equals(USERNAME1))),
                () -> assertTrue(suggestions.stream().anyMatch(suggestion -> suggestion[1].equals(USERNAME2))),
                () -> assertTrue(suggestions.stream().anyMatch(suggestion -> suggestion[1].equals(USERNAME3))),
                () -> assertTrue(suggestions.stream().anyMatch(suggestion -> suggestion[2].equals(TITLE3)))
        );
        verify(userRepository).findUserSuggestions(SUGGESTION_QUERY, Constants.MAX_SEARCH_RESULTS);
        verify(experimentRepository).findExperimentSuggestions(SUGGESTION_QUERY, Constants.MAX_SEARCH_RESULTS);
        verify(courseRepository).findCourseSuggestions(SUGGESTION_QUERY, Constants.MAX_SEARCH_RESULTS);
    }

    @Test
    public void testGetSearchSuggestionsNone() {
        List<String[]> suggestions = searchService.getSearchSuggestions(SUGGESTION_QUERY);
        assertEquals(0, suggestions.size());
        verify(userRepository).findUserSuggestions(SUGGESTION_QUERY, Constants.MAX_SEARCH_RESULTS);
        verify(experimentRepository).findExperimentSuggestions(SUGGESTION_QUERY, Constants.MAX_SEARCH_RESULTS);
        verify(courseRepository).findCourseSuggestions(SUGGESTION_QUERY, Constants.MAX_SEARCH_RESULTS);
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

    @Test
    public void testGetCourseExperimentSuggestions() {
        when(experimentRepository.findCourseExperimentSuggestions(SUGGESTION_QUERY, ID,
                Constants.MAX_SUGGESTION_RESULTS)).thenReturn(experiments);
        List<String[]> experimentInfo = searchService.getCourseExperimentSuggestions(SUGGESTION_QUERY, ID);
        String[] firstExperiment = experimentInfo.get(0);
        String[] secondExperiment = experimentInfo.get(1);
        assertAll(
                () -> assertEquals(2, experimentInfo.size()),
                () -> assertEquals("1", firstExperiment[1]),
                () -> assertEquals(TITLE1, firstExperiment[2]),
                () -> assertEquals("2", secondExperiment[1]),
                () -> assertEquals(TITLE2, secondExperiment[2])
        );
        verify(experimentRepository).findCourseExperimentSuggestions(SUGGESTION_QUERY, ID,
                Constants.MAX_SUGGESTION_RESULTS);
    }

    @Test
    public void testGetCourseExperimentSuggestionsNone() {
        assertEquals(0, searchService.getCourseExperimentSuggestions(SUGGESTION_QUERY, ID).size());
        verify(experimentRepository).findCourseExperimentSuggestions(SUGGESTION_QUERY, ID,
                Constants.MAX_SUGGESTION_RESULTS);
    }

    @Test
    public void testGetCourseParticipantSuggestions() {
        when(userRepository.findCourseParticipantSuggestions(QUERY, ID,
                Constants.MAX_SUGGESTION_RESULTS)).thenReturn(users);
        List<String[]> userInfo = searchService.getCourseParticipantSuggestions(QUERY, ID);
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
        verify(userRepository).findCourseParticipantSuggestions(QUERY, ID, Constants.MAX_SUGGESTION_RESULTS);
    }

    @Test
    public void testGetCourseParticipantSuggestionsNone() {
        assertEquals(0, searchService.getCourseParticipantSuggestions(QUERY, ID).size());
        verify(userRepository).findCourseParticipantSuggestions(QUERY, ID, Constants.MAX_SUGGESTION_RESULTS);
    }

    @Test
    public void testGetCourseExperimentDeleteSuggestions() {
        when(experimentRepository.findCourseExperimentDeleteSuggestions(SUGGESTION_QUERY, ID,
                Constants.MAX_SUGGESTION_RESULTS)).thenReturn(experiments);
        List<String[]> experimentInfo = searchService.getCourseExperimentDeleteSuggestions(SUGGESTION_QUERY, ID);
        String[] firstExperiment = experimentInfo.get(0);
        String[] secondExperiment = experimentInfo.get(1);
        assertAll(
                () -> assertEquals(2, experimentInfo.size()),
                () -> assertEquals("1", firstExperiment[1]),
                () -> assertEquals(TITLE1, firstExperiment[2]),
                () -> assertEquals("2", secondExperiment[1]),
                () -> assertEquals(TITLE2, secondExperiment[2])
        );
        verify(experimentRepository).findCourseExperimentDeleteSuggestions(SUGGESTION_QUERY, ID,
                Constants.MAX_SUGGESTION_RESULTS);
    }

    @Test
    public void testGetCourseExperimentDeleteSuggestionsNone() {
        assertEquals(0, searchService.getCourseExperimentDeleteSuggestions(SUGGESTION_QUERY, ID).size());
        verify(experimentRepository).findCourseExperimentDeleteSuggestions(SUGGESTION_QUERY, ID,
                Constants.MAX_SUGGESTION_RESULTS);
    }

    @Test
    public void testGetCourseParticipantDeleteSuggestions() {
        when(userRepository.findDeleteCourseParticipantSuggestions(QUERY, ID,
                Constants.MAX_SUGGESTION_RESULTS)).thenReturn(users);
        List<String[]> userInfo = searchService.getCourseParticipantDeleteSuggestions(QUERY, ID);
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
        verify(userRepository).findDeleteCourseParticipantSuggestions(QUERY, ID, Constants.MAX_SUGGESTION_RESULTS);
    }

    @Test
    public void testGetCourseParticipantDeleteSuggestionsNone() {
        assertEquals(0, searchService.getCourseParticipantDeleteSuggestions(QUERY, ID).size());
        verify(userRepository).findDeleteCourseParticipantSuggestions(QUERY, ID, Constants.MAX_SUGGESTION_RESULTS);
    }

    @Test
    public void testGetNextUsers() {
        when(userRepository.findUserResults(QUERY, LIMIT, PAGE * LIMIT)).thenReturn(users);
        List<String[]> userInfo = searchService.getNextUsers(QUERY, PAGE);
        String[] firstUser = userInfo.get(0);
        String[] secondUser = userInfo.get(1);
        String[] thirdUser = userInfo.get(2);
        assertAll(
                () -> assertEquals(3, userInfo.size()),
                () -> assertEquals("1", firstUser[0]),
                () -> assertEquals(USERNAME1, firstUser[1]),
                () -> assertEquals(EMAIL1, firstUser[2]),
                () -> assertEquals(PARTICIPANT, firstUser[3]),
                () -> assertEquals("2", secondUser[0]),
                () -> assertEquals(USERNAME2, secondUser[1]),
                () -> assertEquals(EMAIL2, secondUser[2]),
                () -> assertEquals(PARTICIPANT, secondUser[3]),
                () -> assertEquals("3", thirdUser[0]),
                () -> assertEquals(USERNAME3, thirdUser[1]),
                () -> assertEquals(EMAIL3, thirdUser[2]),
                () -> assertEquals(PARTICIPANT, thirdUser[3])
        );
        verify(userRepository).findUserResults(QUERY, LIMIT, PAGE * LIMIT);
    }

    @Test
    public void testGetNextExperiments() {
        when(experimentRepository.findExperimentResults(QUERY, LIMIT, PAGE * LIMIT)).thenReturn(experiments);
        List<String[]> experimentInfo = searchService.getNextExperiments(QUERY, PAGE);
        String[] firstExperiment = experimentInfo.get(0);
        String[] secondExperiment = experimentInfo.get(1);
        assertAll(
                () -> assertEquals(2, experimentInfo.size()),
                () -> assertEquals(experiments.get(0).getId().toString(), firstExperiment[0]),
                () -> assertEquals(experiments.get(0).getTitle(), firstExperiment[1]),
                () -> assertEquals(experiments.get(0).getDescription(), firstExperiment[2]),
                () -> assertEquals(experiments.get(1).getId().toString(), secondExperiment[0]),
                () -> assertEquals(experiments.get(1).getTitle(), secondExperiment[1]),
                () -> assertEquals(experiments.get(1).getDescription(), secondExperiment[2])
        );
        verify(experimentRepository).findExperimentResults(QUERY, LIMIT, PAGE * LIMIT);
    }

    @Test
    public void testGetNextCourses() {
        when(courseRepository.findCourseResults(QUERY, LIMIT, PAGE * LIMIT)).thenReturn(courses);
        List<String[]> courseInfo = searchService.getNextCourses(QUERY, PAGE);
        assertAll(
                () -> assertEquals(1, courseInfo.size()),
                () -> assertEquals(courses.get(0).getId().toString(), courseInfo.get(0)[0]),
                () -> assertEquals(courses.get(0).getTitle(), courseInfo.get(0)[1]),
                () -> assertEquals(courses.get(0).getDescription(), courseInfo.get(0)[2])
        );
        verify(courseRepository).findCourseResults(QUERY, LIMIT, PAGE * LIMIT);
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

    private List<ExperimentTableProjection> addExperimentSuggestions() {
        List<ExperimentTableProjection> projections = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            int id = i + 1;

            ExperimentTableProjection projection = new ExperimentTableProjection() {
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

                @Override
                public boolean isActive() {
                    return false;
                }
            };

            projections.add(projection);
        }

        return projections;
    }

    private List<CourseTableProjection> addCourseSuggestions() {
        List<CourseTableProjection> projections = new ArrayList<>();

        for (int i = 0; i < 1; i++) {
            int id = i + 1;

            CourseTableProjection projection = new CourseTableProjection() {
                @Override
                public Integer getId() {
                    return id;
                }

                @Override
                public String getTitle() {
                    return "Course " + id;
                }

                @Override
                public String getDescription() {
                    return "Description " + id;
                }

                @Override
                public boolean isActive() {
                    return false;
                }
            };

            projections.add(projection);
        }

        return projections;
    }

}
