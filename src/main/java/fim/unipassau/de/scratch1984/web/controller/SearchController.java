package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.service.SearchService;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentTableProjection;
import fim.unipassau.de.scratch1984.persistence.projection.UserProjection;
import fim.unipassau.de.scratch1984.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * The controller for search result management.
 */
@Controller
@RequestMapping("/search/result")
public class SearchController {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    /**
     * The search service to use for search query management.
     */
    private final SearchService searchService;

    /**
     * String corresponding to the search page.
     */
    private static final String SEARCH = "search";

    /**
     * Constructs a new search controller with the given dependencies.
     *
     * @param searchService The search service to use.
     */
    @Autowired
    public SearchController(final SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Retrieves the user and experiment results for the given query string with a maximum amount of results equal to
     * the default page size. If the passed parameters are invalid, the user is redirected to the error page instead.
     * If the query string is blank, no results are retrieved.
     *
     * @param query The query string.
     * @param model The model to add the information to.
     * @return The search page on success, or the error page otherwise.
     */
    @GetMapping("")
    @Secured(Constants.ROLE_ADMIN)
    public String getSearchPage(@RequestParam("query") final String query, final Model model) {
        if (query == null || query.length() > Constants.LARGE_FIELD) {
            logger.error("Cannot search for results for query string null or query string too long!");
            return Constants.ERROR;
        }

        if (query.trim().isBlank()) {
            addModelInfo(new ArrayList<>(), new ArrayList<>(), 0, 0, "", model);
        } else {
            int limit = Constants.PAGE_SIZE;
            int userCount = searchService.getUserCount(query);
            int experimentCount = searchService.getExperimentCount(query);
            List<UserProjection> users = searchService.getUserList(query, limit);
            List<ExperimentTableProjection> experiments = searchService.getExperimentList(query, limit);
            addModelInfo(users, experiments, userCount, experimentCount, query, model);
        }

        return SEARCH;
    }

    /**
     * Adds the given information to the {@link Model}.
     *
     * @param users The {@link UserProjection} results.
     * @param experiments The {@link ExperimentTableProjection} results.
     * @param userCount The number of user results for the given query string.
     * @param experimentCount The number of experiment results for the fiven query string.
     * @param query The query string.
     * @param model The model used to save the information.
     */
    private void addModelInfo(final List<UserProjection> users, final List<ExperimentTableProjection> experiments,
                              final int userCount, final int experimentCount, final String query, final Model model) {
        model.addAttribute("users", users);
        model.addAttribute("experiments", experiments);
        model.addAttribute("userCount", userCount);
        model.addAttribute("experimentCount", experimentCount);
        model.addAttribute("limit", Constants.PAGE_SIZE);
        model.addAttribute("query", query);
    }

}
