package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.TokenService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.validation.PasswordValidator;
import fim.unipassau.de.scratch1984.web.dto.TokenDTO;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ResourceBundle;

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
     * String corresponding to the register page.
     */
    private static final String PASSWORD_SET = "password-set";

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
     * @param model The {@link Model} used to store information..
     * @return The index page to display status messages, or the error page.
     */
    @GetMapping()
    public String validateToken(@RequestParam("value") final String token, final Model model) {
        TokenDTO tokenDTO;

        try {
            tokenDTO = tokenService.findToken(token);
        } catch (NotFoundException e) {
            return Constants.ERROR;
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
                return Constants.ERROR;
            }
        } else if (tokenDTO.getType() == TokenDTO.Type.REGISTER || tokenDTO.getType()
                == TokenDTO.Type.FORGOT_PASSWORD) {
            UserDTO userDTO = userService.getUserById(tokenDTO.getUser());
            model.addAttribute("userDTO", userDTO);
            model.addAttribute("token", tokenDTO.getValue());
            return PASSWORD_SET;
        }

        return "redirect:/?success=true";
    }

    /**
     * Completes the user registration by validating the password input and retrieving the token with the given value.
     * Once the new password has been set and the user account has been activated, the token is deleted from the
     * database. If the passed parameters are invalid or no corresponding user or token could be found, the user is
     * redirected to the error page instead. If the password input was invalid, the register page is returned to display
     * a corresponding error message.
     *
     * @param userDTO The {@link UserDTO} containing the user information.
     * @param token The token value to search for.
     * @param bindingResult The {@link BindingResult} for returning information on invalid user input.
     * @param model The {@link Model} used to store information.
     * @return The index page on success, or the error page.
     */
    @PostMapping("/password")
    public String registerUser(@ModelAttribute("userDTO") final UserDTO userDTO,
                               @RequestParam("value") final String token, final BindingResult bindingResult,
                               final Model model) {
        if (userDTO == null || userDTO.getId() == null || userDTO.getPassword() == null
                || userDTO.getConfirmPassword() == null) {
            logger.error("Cannot register user with id null, or passwords null!");
            return Constants.ERROR;
        } else if (token == null || token.trim().isBlank()) {
            logger.error("Cannot register user with token null or blank!");
            return Constants.ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        String passwordValidation = PasswordValidator.validate(userDTO.getPassword(), userDTO.getConfirmPassword());

        if (passwordValidation != null) {
            bindingResult.addError(createFieldError("userDTO", "password", passwordValidation, resourceBundle));
            model.addAttribute("token", token);
            return PASSWORD_SET;
        }

        try {
            TokenDTO tokenDTO = tokenService.findToken(token);
            UserDTO user = userService.getUserById(userDTO.getId());
            user.setPassword(userService.encodePassword(userDTO.getPassword()));
            user.setActive(true);
            userService.saveUser(user);
            tokenService.deleteToken(tokenDTO.getValue());
            return "redirect:/?success=true";
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Creates a new field error with the given parameters.
     *
     * @param objectName The name of the object.
     * @param field The field to which the error applies.
     * @param error The error message string.
     * @param resourceBundle The resource bundle to retrieve the error message in the current language.
     * @return The new field error.
     */
    private FieldError createFieldError(final String objectName, final String field, final String error,
                                        final ResourceBundle resourceBundle) {
        return new FieldError(objectName, field, resourceBundle.getString(error));
    }

}
