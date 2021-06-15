package fim.unipassau.de.scratch1984.application;

import fim.unipassau.de.scratch1984.application.service.SearchService;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
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
    private static final String QUERY = "user";
    private static final int ID = 1;
    private final User user1 = new User(USERNAME1, EMAIL1, PARTICIPANT, LANGUAGE, "user", null);
    private final User user2 = new User(USERNAME2, EMAIL2, PARTICIPANT, LANGUAGE, "user", null);
    private final User user3 = new User(USERNAME3, EMAIL3, PARTICIPANT, LANGUAGE, "user", null);
    private List<User> users;

    @BeforeEach
    public void setup() {
        users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        users.add(user3);
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
}
