package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.persistence.projection.ExperimentTableProjection;
import fim.unipassau.de.scratch1984.persistence.projection.UserProjection;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.util.Constants;
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
     * Retrieves a list of up to as many user projections as specified in the given limit where the username or email
     * contains the search query string.
     *
     * @param query The username or email to search for.
     * @param limit The maximum amount of results to return.
     * @return A list of matching {@link UserProjection}s, or an empty list, if no entries could be found.
     */
    @Transactional
    public List<UserProjection> getUserList(final String query, final int limit) {
        if (query == null || query.trim().isBlank()) {
            logger.error("Cannot search for users with invalid query string null or blank!");
            throw new IllegalArgumentException("Cannot search for users with invalid query string null or blank!");
        } else if (limit < Constants.PAGE_SIZE) {
            logger.error("Cannot search for users with invalid search result limit " + limit);
            throw new IllegalArgumentException("Cannot search for users with invalid search result limit " + limit);
        }

        return userRepository.findUserResults(query, limit, 0);
    }

    /**
     * Retrieves a list of up to as many experiment search suggestions as specified in the given limit where the title
     * matches the query string.
     *
     * @param query The title to search for.
     * @param limit The maximum amount of results to return.
     * @return A list of matching {@link ExperimentTableProjection}s, or an empty list, if no entries could be found.
     */
    @Transactional
    public List<ExperimentTableProjection> getExperimentList(final String query, final int limit) {
        if (query == null || query.trim().isBlank()) {
            logger.error("Cannot search for experiments with invalid query string null or blank!");
            throw new IllegalArgumentException("Cannot search for experiments with invalid query string null or "
                    + "blank!");
        } else if (limit < Constants.PAGE_SIZE) {
            logger.error("Cannot search for experiments with invalid search result limit " + limit);
            throw new IllegalArgumentException("Cannot search for experiments with invalid search result limit "
                    + limit);
        }

        return experimentRepository.findExperimentResults(query, limit, 0);
    }

    /**
     * Returns the number of users whose username or email contain the search query string.
     *
     * @param query The username or email to search for.
     * @return The number of matching user results.
     */
    @Transactional
    public int getUserCount(final String query) {
        if (query == null || query.trim().isBlank()) {
            logger.error("Cannot get the number of user results with invalid query string null or blank!");
            throw new IllegalArgumentException("Cannot get the number of user results with invalid query string null or"
                    + " blank!");
        }

        return userRepository.getUserResultsCount(query);
    }

    /**
     * Returns the number of experiments whose title contains the search query string.
     *
     * @param query The title to search for.
     * @return The number of matching experiment results.
     */
    @Transactional
    public int getExperimentCount(final String query) {
        if (query == null || query.trim().isBlank()) {
            logger.error("Cannot get the number of experiment results with invalid query string null or blank!");
            throw new IllegalArgumentException("Cannot get the number of experiment results with invalid query string "
                    + "null or blank!");
        }

        return experimentRepository.getExperimentResultsCount(query);
    }

    /**
     * Retrieves a list of up to five usernames and emails where one of the two contains the search query string or up
     * to five experiment ids and titles where the title contains the query string.
     *
     * @param query The username, email, or title to search for.
     * @return A list of matching suggestions, or an empty list, if no entries could be found.
     */
    @Transactional
    public List<String[]> getSearchSuggestions(final String query) {
        List<UserProjection> users = userRepository.findUserSuggestions(query);
        List<ExperimentTableProjection> experiments = experimentRepository.findExperimentSuggestions(query);
        List<String[]> suggestions = addExperimentInfo(experiments);
        suggestions.addAll(addUserInfo(users));
        return suggestions;
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
        List<UserProjection> users = userRepository.findParticipantSuggestions(query, id);
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
        List<UserProjection> users = userRepository.findDeleteParticipantSuggestions(query, id);
        return addUserInfo(users);
    }

    /**
     * Retrieves a list of additional user information for the search page with a maximum size as specified in the page
     * size constant with the computed offset where the username or email contain the search query string.
     *
     * @param query The username or email to search for.
     * @param page The current page used to compute the offset.
     * @return A list of ids, usernames, emails and roles, or an empty list, if no entries could be found.
     */
    @Transactional
    public List<String[]> getNextUsers(final String query, final int page) {
        int offset = Constants.PAGE_SIZE * page;
        List<UserProjection> users = userRepository.findUserResults(query, Constants.PAGE_SIZE, offset);
        return addUserProjectionInfo(users);
    }

    /**
     * Retrieves a list of additional experiment information for the search page with a maximum size as specified in
     * the page size constant with the computed offset where the title contains the query string.
     *
     * @param query The title to search for.
     * @param page The current page used to compute the offset.
     * @return A list of ids, titles and descriptions, or an empty list, if no entries could be found.
     */
    @Transactional
    public List<String[]> getNextExperiments(final String query, final int page) {
        int offset = Constants.PAGE_SIZE * page;
        List<ExperimentTableProjection> projections = experimentRepository.findExperimentResults(query,
                Constants.PAGE_SIZE, offset);
        return addExperimentTableInfo(projections);
    }

    /**
     * Returns a list of all usernames and emails of the users in the given list.
     *
     * @param users The list of {@link UserProjection}s.
     * @return A list of usernames and emails, or an empty list.
     */
    private List<String[]> addUserInfo(final List<UserProjection> users) {
        List<String[]> userInfo = new ArrayList<>();
        users.forEach(user -> userInfo.add(new String[] {user.getUsername(), user.getEmail()}));
        return userInfo;
    }

    /**
     * Returns a list of all experiment ids and titles of the experiments in the given list.
     *
     * @param experiments The list of {@link ExperimentTableProjection}s.
     * @return A list of experiment ids and titles, or an empty list.
     */
    private List<String[]> addExperimentInfo(final List<ExperimentTableProjection> experiments) {
        List<String[]> experimentInfo = new ArrayList<>();
        experiments.forEach(experiment -> experimentInfo.add(new String[] {String.valueOf(experiment.getId()),
                experiment.getTitle()}));
        return experimentInfo;
    }

    /**
     * Returns a list of all user information contained in the retrieved list.
     *
     * @param projections The list of {@link UserProjection}s.
     * @return A list of ids, usernames, emails, and roles or an empty list.
     */
    private List<String[]> addUserProjectionInfo(final List<UserProjection> projections) {
        List<String[]> userInfo = new ArrayList<>();
        projections.forEach(projection -> userInfo.add(new String[] {String.valueOf(projection.getId()),
                projection.getUsername(), projection.getEmail(), projection.getRole()}));
        return userInfo;
    }

    /**
     * Returns a list of the experiment information contained in the retrieved list.
     *
     * @param projections The list of {@link ExperimentTableProjection}s.
     * @return A list of experiment ids, titles, and descriptions or an empty list.
     */
    private List<String[]> addExperimentTableInfo(final List<ExperimentTableProjection> projections) {
        List<String[]> experimentTableInfo = new ArrayList<>();
        projections.forEach(projection -> experimentTableInfo.add(new String[] {String.valueOf(projection.getId()),
                projection.getTitle(), projection.getDescription()}));
        return experimentTableInfo;
    }

}
