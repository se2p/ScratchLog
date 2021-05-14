package fim.unipassau.de.scratch1984.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The controller for the homepage of the project.
 */
@Controller
public class HomeController {

    /**
     * Loads the index page containing basic information about the project.
     *
     * @return The index page.
     */
    @GetMapping("/")
    public String getIndexPage() {
        return "index";
    }

}
