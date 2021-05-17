package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.spring.authentication.CustomAuthenticationProvider;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ResourceBundle;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

/**
 * The controller for user management.
 */
@Controller
@RequestMapping(value = "/users")
public class UserController {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * The user service to use for user management.
     */
    private final UserService userService;

    /**
     * The custom authentication provider to use for user authentication.
     */
    private final CustomAuthenticationProvider authenticationProvider;

    /**
     * The maximum length for a user input.
     */
    private static final int LONG_INPUT = 50;

    /**
     * Constructs a new user controller with the given dependencies.
     *
     * @param userService The user service to use.
     * @param authenticationProvider The authentication provider to use.
     */
    @Autowired
    public UserController(final UserService userService, final CustomAuthenticationProvider authenticationProvider) {
        this.userService = userService;
        this.authenticationProvider = authenticationProvider;
    }

    /**
     * Tries to authenticate the user with the given credentials. On a successful authentication, the user is redirected
     * to the index page. If an error occurred during authentication, the user stays on the login page and an error
     * message is displayed.
     *
     * @param userDTO The {@link UserDTO} containing the login credentials.
     * @param model The model used for saving error messages on a failed authentication.
     * @param httpServletRequest The servlet request.
     * @param bindingResult The binding result for returning information on invalid user input.
     * @return The login page, if an authentication error occurred, or redirect to the index page.
     */
    @PostMapping(path = "/login")
    public String loginUser(@ModelAttribute("userDTO") final UserDTO userDTO, final Model model,
                            final HttpServletRequest httpServletRequest, final BindingResult bindingResult) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        String usernameValidation = validateInput(userDTO.getUsername());
        String passwordValidation = validateInput(userDTO.getPassword());

        if (usernameValidation != null) {
            bindingResult.addError(createFieldError("userDTO", "username", usernameValidation, resourceBundle));
        }
        if (passwordValidation != null) {
            bindingResult.addError(createFieldError("userDTO", "password", passwordValidation, resourceBundle));
        }

        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            UserDTO authenticated = userService.loginUser(userDTO);

            if (!authenticated.isActive()) {
                logger.debug("Tried to log in inactive user with username " + userDTO.getUsername() + ".");
                model.addAttribute("error", resourceBundle.getString("activate_first"));
                return "login";
            }

            clearSecurityContext(httpServletRequest);
            updateSecurityContext(userDTO, httpServletRequest);
        } catch (NotFoundException e) {
            logger.error("Failed to log in user with username " + userDTO.getUsername() + ".", e);
            model.addAttribute("error", resourceBundle.getString("authentication_error"));
            return "login";
        }

        return "redirect:/";
    }

    /**
     * Invalidates the currently authenticated user's session and redirects them to the index page, or throws a
     * {@link NotFoundException}, if no such user exists in the database, or an {@link IllegalStateException}, if no
     * proper spring security authentication can be found.
     *
     * @param httpServletRequest The {@link HttpServletRequest} containing the user session.
     * @return The index page.
     */
    @PostMapping("/logout")
    @Secured("ROLE_ADMIN")
    public String logoutUser(final HttpServletRequest httpServletRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            logger.error("Can't logout an unauthenticated user!");
            throw new IllegalStateException("Can't logout an unauthenticated user!");
        }

        if (!userService.existsUser(authentication.getName())) {
            logger.error("Can't find user with username " + authentication.getName() + " in the database!");
            throw new NotFoundException("Can't find user with username " + authentication.getName()
                    + " in the database!");
        }

        clearSecurityContext(httpServletRequest);
        return "redirect:/";
    }

    /**
     * Checks, whether the given input string matches the general requirements and returns a custom error message
     * string if the input does not meet the requirements, or {@code null} if everything is fine.
     *
     * @param input The input string to check.
     * @return The custom error message string or {@code null}.
     */
    private String validateInput(final String input) {
        if (input == null || input.trim().isBlank()) {
            return "empty_string";
        }

        if (input.length() > LONG_INPUT) {
            return "long_string";
        }

        return null;
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

    /**
     * Clears the current security context and invalidates the http session on user login and logout.
     *
     * @param httpServletRequest The {@link HttpServletRequest} request containing the current user session.
     */
    private void clearSecurityContext(final HttpServletRequest httpServletRequest) {
        SecurityContextHolder.clearContext();
        HttpSession session = httpServletRequest.getSession(false);

        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Updates the spring security context on user login with the given information.
     *
     * @param userDTO The {@link UserDTO} containing the user credentials.
     * @param httpServletRequest The {@link HttpServletRequest} containing the current session.
     */
    private void updateSecurityContext(final UserDTO userDTO, final HttpServletRequest httpServletRequest) {
        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(userDTO.getUsername(),
                userDTO.getPassword());
        Authentication auth = authenticationProvider.authenticate(authReq);
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);
        HttpSession session = httpServletRequest.getSession(true);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, sc);
    }

}
