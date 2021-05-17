package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The controller for the homepage of the project.
 */
@Controller
public class HomeController {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    /**
     * Loads the index page containing basic information about the project.
     *
     * @return The index page.
     */
    @GetMapping("/")
    public String getIndexPage() {
        return "index";
    }

    /**
     * Loads the login page for user authentication.
     *
     * @param userDTO The {@link UserDTO} user for authentication.
     * @return The login page.
     */
    @GetMapping("/login")
    public String getLoginPage(final UserDTO userDTO) {
        return "login";
    }

}
