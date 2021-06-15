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
     * The user service to use for user management.
     */
    private final SearchService searchService;

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
     * Retrieves a list of up to five usernames and emails where one of the two contain the search query string.
     *
     * @param query The username or email to search for.
     * @param id The experiment id.
     * @return A list of usernames and emails, or an empty list, if no entries could be found.
     */
    @GetMapping("/user")
    @Secured("ROLE_ADMIN")
    public List<String[]> getUserSuggestions(@RequestParam("query") final String query,
                                             @RequestParam("id") final String id) {
        if (query == null || query.trim().isBlank() || id == null || id.trim().isBlank()) {
            return new ArrayList<>();
        }

        int experimentId = parseNumber(id);

        if (experimentId < Constants.MIN_ID) {
            return new ArrayList<>();
        }

        return searchService.getUserSuggestions(query, experimentId);
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
