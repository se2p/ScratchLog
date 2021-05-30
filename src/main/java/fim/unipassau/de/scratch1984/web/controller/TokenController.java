package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.TokenService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.web.dto.TokenDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

/**
 * The controller for token management.
 */
@Controller
@RequestMapping(value = "/token")
public class TokenController {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);

    /**
     * The token service to use for generating tokens.
     */
    private final TokenService tokenService;

    /**
     * The user service to use for user management.
     */
    private final UserService userService;

    /**
     * String corresponding to redirecting to the error page.
     */
    private static final String ERROR = "redirect:/error";

    /**
     * Constructs a new token controller with the given dependencies.
     *
     * @param tokenService The {@link TokenService} to use.
     * @param userService The {@link UserService} to use.
     */
    @Autowired
    public TokenController(final TokenService tokenService, final UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    /**
     * Retrieves the token with the given value from the database and performs actions based on the token type. If the
     * token type corresponds to updating a user's email address, the user's address is updated to the new value. If the
     * token has already expired, the user is redirected to the index page and informed about the expiration. If no
     * token could be found or update actions performed, the user is redirected to the error page instead.
     *
     * @param token The token value to search for.
     * @return The index page to display status messages, or the error page.
     */
    @GetMapping()
    public String validateToken(@RequestParam("value") final String token) {
        TokenDTO tokenDTO;

        try {
            tokenDTO = tokenService.findToken(token);
        } catch (NotFoundException e) {
            return ERROR;
        }

        LocalDateTime localDateTime = LocalDateTime.now();

        if (localDateTime.isAfter(tokenDTO.getExpirationDate())) {
            logger.debug("The token for the user with id " + tokenDTO.getUser() + " has already expired!");
            return "redirect:/?error=true";
        }

        if (tokenDTO.getType() == TokenDTO.Type.CHANGE_EMAIL) {
            try {
                userService.updateEmail(tokenDTO.getUser(), tokenDTO.getMetadata());
                tokenService.deleteToken(tokenDTO.getValue());
            } catch (NotFoundException e) {
                return ERROR;
            }
        }

        return "redirect:/?success=true";
    }

}
