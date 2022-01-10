package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.service.SearchService;
import fim.unipassau.de.scratch1984.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * The REST controller used for retrieving search suggestions.
 */
@RestController
@RequestMapping(value = "/search")
public class SearchRestController {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(SearchRestController.class);

    /**
     * The search service to use for search query management.
     */
    private final SearchService searchService;

    /**
     * String corresponding to the query request parameter.
     */
    private static final String QUERY = "query";

    /**
     * String corresponding to the id request parameter.
     */
    private static final String ID = "id";

    /**
     * String corresponding to the page request parameter.
     */
    private static final String PAGE = "page";

    /**
     * Constructs a new search REST controller with the given dependencies.
     *
     * @param searchService The search service to use.
     */
    @Autowired
    public SearchRestController(final SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Retrieves a list of up to five usernames and emails where one of the two contains the search query string or up
     * to five experiment ids and titles where the title contains the query string.
     *
     * @param query The username, email, or title to search for.
     * @return A list of matching suggestions, or an empty list, if no entries could be found.
     */
    @GetMapping("/suggestions")
    @Secured(Constants.ROLE_ADMIN)
    public List<String[]> getSearchSuggestions(@RequestParam(QUERY) final String query) {
        if (query == null || query.trim().isBlank()) {
            return new ArrayList<>();
        }

        return searchService.getSearchSuggestions(query);
    }

    /**
     * Retrieves a list of additional user results for the search page where the username or email contain the search
     * query and the given page parameter is used to compute the offset of results to be retrieved from the database. If
     * the passed parameters are invalid, an empty list is returned instead.
     *
     * @param query The username or email to search for.
     * @param page The current user result page used to compute the offset.
     * @return A list of matching suggestions, or an empty list, if no entries could be found.
     */
    @GetMapping("users")
    @Secured(Constants.ROLE_ADMIN)
    public List<String[]> getMoreUsers(@RequestParam(QUERY) final String query,
                                       @RequestParam(PAGE) final String page) {
        if (invalidParams(query, page)) {
            return new ArrayList<>();
        }

        int pageNumber = parseNumber(page);
        return searchService.getNextUsers(query, pageNumber);
    }

    /**
     * Retrieves a list of additional experiment results for the search page where the title contains the search query
     * and the given page parameter is used to compute the offset of results to be retrieved from the database. If the
     * passed parameters are invalid, an empty list is returned instead.
     *
     * @param query The title to search for.
     * @param page The current experiment result page used to compute the offset.
     * @return A list of matching suggestions, or an empty list, if no entries could be found.
     */
    @GetMapping("/experiments")
    @Secured(Constants.ROLE_ADMIN)
    public List<String[]> getMoreExperiments(@RequestParam(QUERY) final String query,
                                             @RequestParam(PAGE) final String page) {
        if (invalidParams(query, page)) {
            return new ArrayList<>();
        }

        int pageNumber = parseNumber(page);
        return searchService.getNextExperiments(query, pageNumber);
    }

    /**
     * Retrieves a list of up to five usernames and emails where one of the two contain the search query string and who
     * are not already participating in the experiment with the given id.
     *
     * @param query The username or email to search for.
     * @param id The experiment id.
     * @return A list of usernames and emails, or an empty list, if no entries could be found.
     */
    @GetMapping("/user")
    @Secured(Constants.ROLE_ADMIN)
    public List<String[]> getUserSuggestions(@RequestParam(QUERY) final String query,
                                             @RequestParam(ID) final String id) {
        if (invalidParams(query, id)) {
            return new ArrayList<>();
        }

        int experimentId = parseNumber(id);
        return searchService.getUserSuggestions(query, experimentId);
    }

    /**
     * Retrieves a list of up to five usernames and emails where one of the two contain the search query string and
     * where the user is participating in the experiment with the given id.
     *
     * @param query The username or email to search for.
     * @param id The experiment id.
     * @return A list of usernames and emails, or an empty list, if no entries could be found.
     */
    @GetMapping("/delete")
    @Secured(Constants.ROLE_ADMIN)
    public List<String[]> getDeleteUserSuggestions(@RequestParam(QUERY) final String query,
                                                   @RequestParam(ID) final String id) {
        if (invalidParams(query, id)) {
            return new ArrayList<>();
        }

        int experimentId = parseNumber(id);
        return searchService.getUserDeleteSuggestions(query, experimentId);
    }

    /**
     * Checks, whether the given query and id parameters are invalid.
     *
     * @param query The query string to check.
     * @param id The id to check.
     * @return {@code true} if one of the parameters is invalid, or {@code false} otherwise.
     */
    private boolean invalidParams(final String query, final String id) {
        if (query == null || query.trim().isBlank() || id == null || id.trim().isBlank()) {
            return true;
        }

        return parseNumber(id) < Constants.MIN_ID;
    }

    /**
     * Returns the corresponding int value of the given id, or -1, if the id is not a number.
     *
     * @param id The id in its string representation.
     * @return The corresponding int value, or -1.
     */
    private int parseNumber(final String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

}
