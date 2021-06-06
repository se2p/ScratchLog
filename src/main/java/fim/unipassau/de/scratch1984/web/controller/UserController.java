package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.MailService;
import fim.unipassau.de.scratch1984.application.service.TokenService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.spring.authentication.CustomAuthenticationProvider;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.validation.EmailValidator;
import fim.unipassau.de.scratch1984.util.validation.PasswordValidator;
import fim.unipassau.de.scratch1984.util.validation.UsernameValidator;
import fim.unipassau.de.scratch1984.web.dto.TokenDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
     * The mail service to use for sending emails.
     */
    private final MailService mailService;

    /**
     * The token service to use for generating tokens.
     */
    private final TokenService tokenService;

    /**
     * The custom authentication provider to use for user authentication.
     */
    private final CustomAuthenticationProvider authenticationProvider;

    /**
     * The session locale resolver to user for language support.
     */
    private final LocaleResolver localeResolver;

    /**
     * String corresponding to the login page.
     */
    private static final String LOGIN = "login";

    /**
     * String corresponding to redirecting to the index page.
     */
    private static final String INDEX = "redirect:/";

    /**
     * String corresponding to redirecting to the error page.
     */
    private static final String ERROR = "redirect:/error";

    /**
     * String corresponding to the profile page.
     */
    private static final String PROFILE = "profile";

    /**
     * String corresponding to the profile edit page.
     */
    private static final String PROFILE_EDIT = "profile-edit";

    /**
     * Constructs a new user controller with the given dependencies.
     *
     * @param userService The {@link UserService} to use.
     * @param mailService The {@link MailService} to use.
     * @param tokenService The {@link TokenService} to use.
     * @param authenticationProvider The {@link CustomAuthenticationProvider} to use.
     * @param localeResolver The locale resolver to use.
     */
    @Autowired
    public UserController(final UserService userService, final MailService mailService, final TokenService tokenService,
                          final CustomAuthenticationProvider authenticationProvider,
                          final LocaleResolver localeResolver) {
        this.userService = userService;
        this.mailService = mailService;
        this.tokenService = tokenService;
        this.authenticationProvider = authenticationProvider;
        this.localeResolver = localeResolver;
    }

    /**
     * Tries to authenticate the participant with the given secret. On a successful authentication, the participant is
     * redirected to the corresponding experiment page. If an error occurred during authentication, the user is
     * redirected to the error page instead.
     *
     * @param id The id of the experiment in which the user is participating.
     * @param secret The user's secret.
     * @param httpServletRequest The servlet request.
     * @param httpServletResponse The servlet response.
     * @return The experiment page on success, or the error page, otherwise.
     */
    @GetMapping("/authenticate")
    public String authenticateUser(@RequestParam("id") final String id, @RequestParam("secret") final String secret,
                                   final HttpServletRequest httpServletRequest,
                                   final HttpServletResponse httpServletResponse) {
        if (id == null || id.trim().isBlank() || secret == null || secret.trim().isBlank()) {
            logger.error("Cannot authenticate participant with id or secret null or blank!");
            return ERROR;
        }

        int experimentId = parseId(id);
        UserDTO authenticated;

        if (experimentId < Constants.MIN_ID) {
            logger.debug("Cannot authenticate user with invalid experiment id " + id + "!");
            return ERROR;
        }

        try {
            authenticated = userService.authenticateUser(secret);
        } catch (NotFoundException e) {
            return ERROR;
        }

        if (!userService.existsParticipant(authenticated.getId(), experimentId)) {
            logger.error("No participation entry could be found for the user with username "
                    + authenticated.getUsername() + " and experiment with id " + id + "!");
            return ERROR;
        }

        clearSecurityContext(httpServletRequest);
        updateSecurityContext(authenticated, httpServletRequest);
        localeResolver.setLocale(httpServletRequest, httpServletResponse,
                getLocaleFromLanguage(authenticated.getLanguage()));
        return "redirect:/experiment?id=" + experimentId;
    }

    /**
     * Tries to authenticate the user with the given credentials. On a successful authentication, the user is redirected
     * to the index page. If an error occurred during authentication, the user stays on the login page and an error
     * message is displayed.
     *
     * @param userDTO The {@link UserDTO} containing the login credentials.
     * @param model The model used for saving error messages on a failed authentication.
     * @param httpServletRequest The servlet request.
     * @param httpServletResponse The servlet response.
     * @param bindingResult The binding result for returning information on invalid user input.
     * @return The login page, if an authentication error occurred, or redirect to the index page.
     */
    @PostMapping(path = "/login")
    public String loginUser(@ModelAttribute("userDTO") final UserDTO userDTO, final Model model,
                            final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse,
                            final BindingResult bindingResult) {
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
            return LOGIN;
        }

        try {
            UserDTO authenticated = userService.loginUser(userDTO);

            if (!authenticated.isActive()) {
                logger.debug("Tried to log in inactive user with username " + userDTO.getUsername() + ".");
                model.addAttribute("error", resourceBundle.getString("activate_first"));
                return LOGIN;
            }

            clearSecurityContext(httpServletRequest);
            updateSecurityContext(userDTO, httpServletRequest);
            localeResolver.setLocale(httpServletRequest, httpServletResponse,
                    getLocaleFromLanguage(userDTO.getLanguage()));
        } catch (NotFoundException e) {
            logger.error("Failed to log in user with username " + userDTO.getUsername() + ".", e);
            model.addAttribute("error", resourceBundle.getString("authentication_error"));
            return LOGIN;
        }

        return INDEX;
    }

    /**
     * Invalidates the currently authenticated user's session and redirects them to the index page, or the error page,
     * if no such user exists in the database, or no proper spring security authentication can be found.
     *
     * @param httpServletRequest The {@link HttpServletRequest} containing the user session.
     * @return The index page on success, or the error page otherwise.
     */
    @PostMapping("/logout")
    @Secured("ROLE_ADMIN")
    public String logoutUser(final HttpServletRequest httpServletRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            logger.error("Can't logout an unauthenticated user!");
            return ERROR;
        }

        if (!userService.existsUser(authentication.getName())) {
            logger.error("Can't find user with username " + authentication.getName() + " in the database!");
            return ERROR;
        }

        clearSecurityContext(httpServletRequest);
        return INDEX;
    }

    /**
     * Returns the profile page of the user with the given username, or the authenticated user's own profile page, if
     * no parameter was passed. If no entry for the username can be found in the database, the user is redirected to
     * error page instead, and the user's session invalidated, if the user tried to access their own profile page.
     *
     * @param username The username to search for.
     * @param model The model used for saving the user information.
     * @param httpServletRequest The {@link HttpServletRequest} containing the user session.
     * @return The profile page on success, or the error page otherwise.
     */
    @GetMapping("/profile")
    @Secured("ROLE_ADMIN")
    public String getProfile(@RequestParam(value = "name", required = false) final String username, final Model model,
                             final HttpServletRequest httpServletRequest) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            logger.error("Can't show the profile page for an unauthenticated user!");
            return ERROR;
        }

        if (username == null || username.trim().isBlank()) {
            try {
                UserDTO userDTO = userService.getUser(authentication.getName());
                model.addAttribute("userDTO", userDTO);
                model.addAttribute("language",
                        resourceBundle.getString(userDTO.getLanguage().toString().toLowerCase()));
                return PROFILE;
            } catch (NotFoundException e) {
                clearSecurityContext(httpServletRequest);
                return ERROR;
            }
        }

        try {
            UserDTO userDTO = userService.getUser(username);
            model.addAttribute("userDTO", userDTO);
            model.addAttribute("language", resourceBundle.getString(userDTO.getLanguage().toString().toLowerCase()));
            return PROFILE;
        } catch (NotFoundException e) {
            return ERROR;
        }
    }

    /**
     * Returns the profile edit page of the user with the given username, or the authenticated user's own profile edit
     * page, if no parameter was passed. If no entry can be found in the database, the user is redirected to the error
     * page instead, and the user's session invalidated, if the user tried to access their own profile edit page.
     *
     * @param username The username to search for.
     * @param model The model used for saving the user information.
     * @param httpServletRequest The {@link HttpServletRequest} containing the user session.
     * @return The profile edit page on success, or the error page otherwise.
     */
    @GetMapping("/edit")
    @Secured("ROLE_ADMIN")
    public String getEditProfileForm(@RequestParam(value = "name", required = false) final String username,
                                     final Model model, final HttpServletRequest httpServletRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            logger.error("Can't show the profile page for an unauthenticated user!");
            return ERROR;
        }

        if (username == null || username.trim().isBlank()) {
            try {
                UserDTO userDTO = userService.getUser(authentication.getName());
                model.addAttribute("userDTO", userDTO);
                return PROFILE_EDIT;
            } catch (NotFoundException e) {
                clearSecurityContext(httpServletRequest);
                return ERROR;
            }
        }

        try {
            UserDTO userDTO = userService.getUser(username);
            model.addAttribute("userDTO", userDTO);
            return PROFILE_EDIT;
        } catch (NotFoundException e) {
            return ERROR;
        }
    }

    /**
     * Updates the user information with the values given in the {@link UserDTO} and redirects to corresponding user
     * page on success. If the input form data is invalid, the current page is returned instead to display the error
     * messages. If the current user changed their own profile, the security context is updated to save the potentially
     * new authentication data.
     *
     * @param userDTO The user dto containing the input data.
     * @param bindingResult The binding result for returning information on invalid user input.
     * @param httpServletRequest The servlet request.
     * @param httpServletResponse The servlet response.
     * @return The profile edit page, if the input is invalid, or the profile page on success.
     */
    @PostMapping("/update")
    @Secured("ROLE_ADMIN")
    public String updateUser(@ModelAttribute("userDTO") final UserDTO userDTO, final BindingResult bindingResult,
                             final HttpServletRequest httpServletRequest,
                             final HttpServletResponse httpServletResponse) {
        if (userDTO.getUsername() == null || userDTO.getEmail() == null || userDTO.getNewPassword() == null
                || userDTO.getConfirmPassword() == null || userDTO.getPassword() == null) {
            logger.error("The new username, email and passwords should never be null, but only empty strings!");
            return ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        UserDTO findOldUser;

        try {
            findOldUser = userService.getUserById(userDTO.getId());
        } catch (NotFoundException e) {
            return ERROR;
        }

        validateUpdateUsername(userDTO, findOldUser, bindingResult, resourceBundle);
        validateUpdateEmail(userDTO, findOldUser, bindingResult, resourceBundle);
        validateUpdatePassword(userDTO, findOldUser, bindingResult, resourceBundle);

        if (bindingResult.hasErrors()) {
            return PROFILE_EDIT;
        }

        String username = findOldUser.getUsername();

        if (!findOldUser.getUsername().equals(userDTO.getUsername())) {
            findOldUser.setUsername(userDTO.getUsername());
        }

        boolean sent = false;

        if (!findOldUser.getEmail().equals(userDTO.getEmail())) {
            sent = updateEmail(userDTO.getEmail(), userDTO.getId(), httpServletRequest, resourceBundle);
        }

        if (!userDTO.getNewPassword().trim().isBlank()) {
            findOldUser.setPassword(userService.encodePassword(userDTO.getNewPassword()));
        }

        findOldUser.setLanguage(userDTO.getLanguage());
        UserDTO updated = userService.updateUser(findOldUser);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (username.equals(authentication.getName())) {
            clearSecurityContext(httpServletRequest);
            updateSecurityContext(userDTO, httpServletRequest);
        }

        localeResolver.setLocale(httpServletRequest, httpServletResponse, getLocaleFromLanguage(userDTO.getLanguage()));

        if (sent) {
            return "redirect:/users/profile?update=true&name=" + updated.getUsername();
        }

        return "redirect:/users/profile?name=" + updated.getUsername();
    }

    /**
     * Deletes the user with the given id and all related participant data. If no corresponding user entity can be
     * found, the user is redirected to the error page. If the user is trying to delete the last administrator, they
     * see an error message instead.
     *
     * @param id The id of the user to be deleted.
     * @return The index page on success, or the profile or error page.
     */
    @GetMapping("/delete")
    @Secured("ROLE_ADMIN")
    public String deleteUser(@RequestParam("id") final String id) {
        if (id == null) {
            logger.debug("Cannot delete user with id null!");
            return ERROR;
        }

        int userId = parseId(id);

        if (userId < Constants.MIN_ID) {
            logger.debug("Cannot delete user with invalid id " + id + "!");
            return ERROR;
        }

        UserDTO userDTO = userService.getUserById(userId);

        if (userDTO.getRole().equals(UserDTO.Role.ADMIN) && userService.isLastAdmin()) {
            return "redirect:/users/profile?lastAdmin=true";
        }

        userService.deleteUser(userDTO.getId());
        return "redirect:/?success=true";
    }

    /**
     * Validates the username passed in the {@link UserDTO} on updating the given user.
     *
     * @param userDTO The user dto containing the new user information.
     * @param findOldUser The user dto containing the old user information.
     * @param bindingResult The binding result for returning information on invalid user input.
     * @param resourceBundle The resource bundle for error translation.
     */
    private void validateUpdateUsername(final UserDTO userDTO, final UserDTO findOldUser,
                                     final BindingResult bindingResult, final ResourceBundle resourceBundle) {
        if (!findOldUser.getUsername().equals(userDTO.getUsername())) {
            String usernameValidation = UsernameValidator.validate(userDTO.getUsername());

            if (usernameValidation != null) {
                bindingResult.addError(createFieldError("userDTO", "username", usernameValidation, resourceBundle));
            } else if (userService.existsUser(userDTO.getUsername())) {
                bindingResult.addError(createFieldError("userDTO", "username", "username_exists", resourceBundle));
            }
        }
    }

    /**
     * Validates the email passed in the {@link UserDTO} on updating the given user.
     *
     * @param userDTO The user dto containing the new user information.
     * @param findOldUser The user dto containing the old user information.
     * @param bindingResult The binding result for returning information on invalid user input.
     * @param resourceBundle The resource bundle for error translation.
     */
    private void validateUpdateEmail(final UserDTO userDTO, final UserDTO findOldUser,
                                        final BindingResult bindingResult, final ResourceBundle resourceBundle) {
        if (findOldUser.getEmail() != null && userDTO.getEmail().trim().isBlank()) {
            bindingResult.addError(createFieldError("userDTO", "email", "empty_string", resourceBundle));
        }

        if (!userDTO.getEmail().trim().isBlank() && !userDTO.getEmail().equals(findOldUser.getEmail())) {
            String emailValidation = EmailValidator.validate(userDTO.getEmail());

            if (emailValidation != null) {
                bindingResult.addError(createFieldError("userDTO", "email", emailValidation, resourceBundle));
            } else if (userService.existsEmail(userDTO.getEmail())) {
                bindingResult.addError(createFieldError("userDTO", "email", "email_exists", resourceBundle));
            }
        }
    }

    /**
     * Validates the passwords passed in the {@link UserDTO} on updating the given user.
     *
     * @param userDTO The user dto containing the new user information.
     * @param findOldUser The user dto containing the old user information.
     * @param bindingResult The binding result for returning information on invalid user input.
     * @param resourceBundle The resource bundle for error translation.
     */
    private void validateUpdatePassword(final UserDTO userDTO, final UserDTO findOldUser,
                                     final BindingResult bindingResult, final ResourceBundle resourceBundle) {
        if (!userDTO.getNewPassword().trim().isBlank() || !userDTO.getConfirmPassword().trim().isBlank()) {
            String passwordValidation = PasswordValidator.validate(userDTO.getNewPassword(),
                    userDTO.getConfirmPassword());

            if (passwordValidation != null) {
                bindingResult.addError(createFieldError("userDTO", "newPassword", passwordValidation, resourceBundle));
            }

            if (userDTO.getPassword().trim().isBlank()) {
                bindingResult.addError(createFieldError("userDTO", "password", "enter_password", resourceBundle));
            } else if (!userService.matchesPassword(userDTO.getPassword(), findOldUser.getPassword())) {
                bindingResult.addError(createFieldError("userDTO", "password", "invalid_password",
                        resourceBundle));
            }
        }
    }

    /**
     * Generates a new token for the user with the given id with the given email as metadata when a user tries to
     * tries to update their email address. An email is sent to the new address in which the user is asked to confirm
     * the change.
     *
     * @param email The new email address.
     * @param id The id of the user for whom the token is to be generated.
     * @param httpServletRequest The servlet request.
     * @param resourceBundle The resource bundle for message translations.
     * @return {@code true} if a token was created and the email sent, or {@code false} otherwise.
     */
    private boolean updateEmail(final String email, final int id, final HttpServletRequest httpServletRequest,
                                final ResourceBundle resourceBundle) {
        TokenDTO tokenDTO;

        try {
            tokenDTO = tokenService.generateToken(TokenDTO.Type.CHANGE_EMAIL, email, id);
        } catch (NotFoundException e) {
            return false;
        }

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(httpServletRequest).replacePath(null).build()
                .toUriString();
        String tokenUrl = baseUrl + "/token?value=" + tokenDTO.getValue();
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("baseUrl", baseUrl);
        templateModel.put("token", tokenUrl);
        return sendEmail(email, null, null, null, resourceBundle.getString("change_email_subject"),
                templateModel, "change-email.html");
    }

    /**
     * Sends the given email template to the given addresses. If the {@link MailService} fails to send the message
     * three consecutive times, it stops.
     *
     * @param to The recipient of the email.
     * @param cc The address to which a copy of this email should be send.
     * @param bcc The address to which a blind copy of this email should be send.
     * @param replyTo The reply to address.
     * @param subject The subject of this email.
     * @param templateModel The template model containing additional properties.
     * @param template The name of the mail template to use.
     * @return {@code true} if the email was sent successfully, or {@code false} otherwise.
     */
    private boolean sendEmail(final String to, final String cc, final String bcc, final String replyTo,
                           final String subject, final Map<String, Object> templateModel,
                           final String template) {
        int tries = 0;

        while (tries < Constants.MAX_EMAIL_TRIES) {
            try {
                mailService.sendTemplateMessage(to, cc, bcc, replyTo, subject, templateModel, template);
                return true;
            } catch (MessagingException e) {
                tries++;
                logger.error("Failed to send message to address " + to + " on try #" + tries + "!", e);
            }
        }

        return false;
    }

    /**
     * Checks, whether the given input string matches the general requirements and returns a custom error message
     * string if it does not, or {@code null} if everything is fine.
     *
     * @param input The input string to check.
     * @return The custom error message string or {@code null}.
     */
    private String validateInput(final String input) {
        if (input == null || input.trim().isBlank()) {
            return "empty_string";
        }

        if (input.length() > Constants.SMALL_FIELD) {
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

    /**
     * Returns the proper {@link Locale} based on the user's preferred language settings.
     *
     * @param language The user's preferred language.
     * @return The corresponding locale, or English as a default value.
     */
    private Locale getLocaleFromLanguage(final UserDTO.Language language) {
        if (language == UserDTO.Language.GERMAN) {
            return Locale.GERMAN;
        }
        return Locale.ENGLISH;
    }

    /**
     * Returns the corresponding int value of the given id, or -1, if the id is not a number.
     *
     * @param id The id in its string representation.
     * @return The corresponding int value, or -1.
     */
    private int parseId(final String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

}
