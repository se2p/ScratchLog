package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * A service providing methods related to search queries.
 */
@Service
public class SearchService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    /**
     * The user repository to use for database queries related to user data.
     */
    private final UserRepository userRepository;

    /**
     * The experiment repository to use for database queries related to experiment data.
     */
    private final ExperimentRepository experimentRepository;

    /**
     * Constructs a search service with the given dependencies.
     *
     * @param userRepository The user repository to use.
     * @param experimentRepository The experiment repository to use.
     */
    @Autowired
    public SearchService(final UserRepository userRepository, final ExperimentRepository experimentRepository) {
        this.userRepository = userRepository;
        this.experimentRepository = experimentRepository;
    }

    /**
     * Retrieves a list of up to five usernames and emails where one of the two contain the search query string and
     * where the corresponding user is not already participating in the experiment with the given id.
     *
     * @param query The username or email to search for.
     * @param id The experiment id.
     * @return A list of usernames and emails, or an empty list, if no entries could be found.
     */
    @Transactional
    public List<String[]> getUserSuggestions(final String query, final int id) {
        List<User> users = userRepository.findParticipantSuggestions(query, id);
        return addUserInfo(users);
    }

    /**
     * Retrieves a list of up to five usernames and emails where one of the two contain the search query string and
     * where the corresponding user is participating in the experiment with the given id.
     *
     * @param query The username or email to search for.
     * @param id The experiment id.
     * @return A list of usernames and emails, or an empty list, if no entries could be found.
     */
    @Transactional
    public List<String[]> getUserDeleteSuggestions(final String query, final int id) {
        List<User> users = userRepository.findDeleteParticipantSuggestions(query, id);
        return addUserInfo(users);
    }

    /**
     * Returns a list of all usernames and emails of the users in the given list.
     *
     * @param users The list of users.
     * @return A list of usernames and emails, or an empty list.
     */
    private List<String[]> addUserInfo(final List<User> users) {
        List<String[]> userInfo = new ArrayList<>();

        if (users.isEmpty()) {
            return userInfo;
        }

        for (User user : users) {
            String[] addInfo = new String[] {user.getUsername(), user.getEmail()};
            userInfo.add(addInfo);
        }

        return userInfo;
    }

}
