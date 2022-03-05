package fim.unipassau.de.scratch1984.web.controller;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.MailService;
import fim.unipassau.de.scratch1984.application.service.ParticipantService;
import fim.unipassau.de.scratch1984.application.service.TokenService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.spring.authentication.CustomAuthenticationProvider;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.util.validation.EmailValidator;
import fim.unipassau.de.scratch1984.util.validation.PasswordValidator;
import fim.unipassau.de.scratch1984.util.validation.UsernameValidator;
import fim.unipassau.de.scratch1984.web.dto.PasswordDTO;
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
     * The participant service to use for participant management.
     */
    private final ParticipantService participantService;

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
     * String corresponding to the profile page.
     */
    private static final String PROFILE = "profile";

    /**
     * String corresponding to the profile edit page.
     */
    private static final String PROFILE_EDIT = "profile-edit";

    /**
     * String corresponding to the password page.
     */
    private static final String PASSWORD = "password";

    /**
     * String corresponding to the add user page.
     */
    private static final String USER = "user";

    /**
     * String corresponding to the password reset page.
     */
    private static final String PASSWORD_RESET = "password-reset";

    /**
     * String corresponding to the userDTO model attribute.
     */
    private static final String USER_DTO = "userDTO";

    /**
     * Constructs a new user controller with the given dependencies.
     *
     * @param userService The {@link UserService} to use.
     * @param participantService The {@link ParticipantService} to use.
     * @param mailService The {@link MailService} to use.
     * @param tokenService The {@link TokenService} to use.
     * @param authenticationProvider The {@link CustomAuthenticationProvider} to use.
     * @param localeResolver The locale resolver to use.
     */
    @Autowired
    public UserController(final UserService userService, final ParticipantService participantService,
                          final MailService mailService, final TokenService tokenService,
                          final CustomAuthenticationProvider authenticationProvider,
                          final LocaleResolver localeResolver) {
        this.userService = userService;
        this.participantService = participantService;
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
            return Constants.ERROR;
        }

        int experimentId = parseId(id);
        UserDTO authenticated;

        if (experimentId < Constants.MIN_ID) {
            logger.debug("Cannot authenticate user with invalid experiment id " + id + "!");
            return Constants.ERROR;
        }

        try {
            authenticated = userService.authenticateUser(secret);
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }

        if (!userService.existsParticipant(authenticated.getId(), experimentId)) {
            logger.error("No participation entry could be found for the user with username "
                    + authenticated.getUsername() + " and experiment with id " + id + "!");
            return Constants.ERROR;
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
    public String loginUser(@ModelAttribute(USER_DTO) final UserDTO userDTO, final Model model,
                            final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse,
                            final BindingResult bindingResult) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        String usernameValidation = validateInput(userDTO.getUsername());
        String passwordValidation = validateInput(userDTO.getPassword());

        if (usernameValidation != null) {
            bindingResult.addError(createFieldError(USER_DTO, "username", usernameValidation, resourceBundle));
        }
        if (passwordValidation != null) {
            bindingResult.addError(createFieldError(USER_DTO, "password", passwordValidation, resourceBundle));
        }

        if (bindingResult.hasErrors()) {
            return LOGIN;
        }

        try {
            UserDTO findUser = userService.getUser(userDTO.getUsername());

            if (!findUser.isActive()) {
                logger.debug("Tried to log in inactive user with username " + userDTO.getUsername() + ".");
                model.addAttribute("error", resourceBundle.getString("activate_first"));
                return LOGIN;
            } else if (findUser.getAttempts() >= Constants.MAX_LOGIN_ATTEMPTS) {
                findUser.setActive(false);
                userService.updateUser(findUser);
                tokenService.generateToken(TokenDTO.Type.DEACTIVATED, "", findUser.getId());
                logger.info("Deactivated account of user with username " + userDTO.getUsername()
                        + " due to exceeding the maximum number of login attempts!");
                model.addAttribute("error", resourceBundle.getString("account_deactivated"));
                return LOGIN;
            }

            if (userService.loginUser(userDTO)) {
                clearSecurityContext(httpServletRequest);
                updateSecurityContext(findUser, httpServletRequest);
                localeResolver.setLocale(httpServletRequest, httpServletResponse,
                        getLocaleFromLanguage(findUser.getLanguage()));
                return INDEX;
            } else {
                model.addAttribute("error", resourceBundle.getString("authentication_error"));
                return LOGIN;
            }
        } catch (NotFoundException e) {
            logger.error("Failed to log in user with username " + userDTO.getUsername() + ".", e);
            model.addAttribute("error", resourceBundle.getString("authentication_error"));
            return LOGIN;
        }
    }

    /**
     * Invalidates the currently authenticated user's session and redirects them to the index page, or the error page,
     * if no such user exists in the database, or no proper spring security authentication can be found.
     *
     * @param httpServletRequest The {@link HttpServletRequest} containing the user session.
     * @return The index page on success, or the error page otherwise.
     */
    @GetMapping("/logout")
    @Secured(Constants.ROLE_PARTICIPANT)
    public String logoutUser(final HttpServletRequest httpServletRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            logger.error("Can't logout an unauthenticated user!");
            return Constants.ERROR;
        }

        if (!userService.existsUser(authentication.getName())) {
            logger.error("Can't find user with username " + authentication.getName() + " in the database!");
            return Constants.ERROR;
        }

        clearSecurityContext(httpServletRequest);
        return INDEX;
    }

    /**
     * Returns the user page for adding a new user.
     *
     * @param userDTO The {@link UserDTO} used to save the new user data.
     * @return The user page.
     */
    @GetMapping("/add")
    @Secured(Constants.ROLE_ADMIN)
    public String getAddUser(final UserDTO userDTO) {
        return USER;
    }

    /**
     * Adds a new user with values passed in the given user dto to the database and creates a registration token for the
     * new user. Finally, an email is sent to the new user asking them to complete their registration. If the parameters
     * passed are invalid, or no email could be sent, the user is redirected to the error page instead. If the given
     * username or email do not match the requirements or exist already, the user returns to the add user page where
     * corresponding error messages are displayed.
     *
     * @param userDTO The {@link UserDTO} used to save the new user data.
     * @param bindingResult The binding result for returning information on invalid user input.
     * @return The index page on success, or the error page or user page otherwise.
     */
    @PostMapping("/add")
    @Secured(Constants.ROLE_ADMIN)
    public String addUser(@ModelAttribute(USER_DTO) final UserDTO userDTO, final BindingResult bindingResult) {
        if (userDTO.getId() != null) {
            logger.error("Cannot add new user with id not null!");
            return Constants.ERROR;
        } else if (userDTO.getLanguage() == null || userDTO.getRole() == null || userDTO.getEmail() == null) {
            logger.error("Cannot add new user with language, role, or email null!");
            return Constants.ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        String emailValidation = EmailValidator.validate(userDTO.getEmail());
        String usernameValidation = UsernameValidator.validate(userDTO.getUsername());

        if (emailValidation != null) {
            bindingResult.addError(createFieldError(USER_DTO, "email", emailValidation, resourceBundle));
        } else if (userService.existsEmail(userDTO.getEmail())) {
            bindingResult.addError(createFieldError(USER_DTO, "email", "email_exists", resourceBundle));
        }

        if (usernameValidation != null) {
            bindingResult.addError(createFieldError(USER_DTO, "username", usernameValidation, resourceBundle));
        } else if (userService.existsUser(userDTO.getUsername())) {
            bindingResult.addError(createFieldError(USER_DTO, "username", "username_exists", resourceBundle));
        }

        if (bindingResult.hasErrors()) {
            return USER;
        }

        UserDTO saved = userService.saveUser(userDTO);

        if (!Constants.MAIL_SERVER) {
            return "redirect:/users/profile?name=" + saved.getUsername();
        } else {
            TokenDTO tokenDTO = tokenService.generateToken(TokenDTO.Type.REGISTER, null, saved.getId());

            if (sendEmail(userDTO.getEmail(), tokenDTO.getValue(), "password_set", "password-set-email.html",
                    resourceBundle)) {
                return "redirect:/?success=true";
            } else {
                return Constants.ERROR;
            }
        }
    }

    /**
     * Generates a password reset token for the given user and sends an email to complete the password reset. If the
     * passed parameters are invalid, the user is redirected to the error page instead. If no user with matching
     * username and email could be found, the user returns to the password reset page where an error message is
     * displayed.
     *
     * @param userDTO The {@link UserDTO} used to save the new user data.
     * @param model The {@link Model} used to store the error message.
     * @return The index page on success, or the error page or password reset page otherwise.
     */
    @PostMapping("/reset")
    public String passwordReset(@ModelAttribute(USER_DTO) final UserDTO userDTO, final Model model) {
        if (userDTO.getUsername() == null || userDTO.getEmail() == null || userDTO.getUsername().trim().isBlank()
                || userDTO.getEmail().trim().isBlank()) {
            logger.error("Cannot reset password for user with username or email null or blank!");
            return Constants.ERROR;
        } else if (userDTO.getUsername().length() > Constants.SMALL_FIELD
                || userDTO.getEmail().length() > Constants.LARGE_FIELD) {
            logger.error("Cannot reset password for user with input username or email too long!");
            return Constants.ERROR;
        } else if (!Constants.MAIL_SERVER) {
            logger.warn("Cannot reset password without a mail server!");
            return Constants.ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());

        try {
            UserDTO findUsername = userService.getUser(userDTO.getUsername());
            UserDTO findEmail = userService.getUserByEmail(userDTO.getEmail());

            if (findEmail.equals(findUsername)) {
                TokenDTO tokenDTO = tokenService.generateToken(TokenDTO.Type.FORGOT_PASSWORD, null, findEmail.getId());

                if (sendEmail(userDTO.getEmail(), tokenDTO.getValue(), "password_set", "password-set-email.html",
                        resourceBundle)) {
                    return "redirect:/?success=true";
                }
            }

            return Constants.ERROR;
        } catch (NotFoundException e) {
            model.addAttribute("error", resourceBundle.getString("user_not_found"));
            return PASSWORD_RESET;
        }
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
    @Secured(Constants.ROLE_PARTICIPANT)
    public String getProfile(@RequestParam(value = "name", required = false) final String username, final Model model,
                             final HttpServletRequest httpServletRequest) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            logger.error("Can't show the profile page for an unauthenticated user!");
            return Constants.ERROR;
        }

        UserDTO userDTO;
        HashMap<Integer, String> experiments = new HashMap<>();

        if (username == null || username.trim().isBlank() || !httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            try {
                userDTO = userService.getUser(authentication.getName());
            } catch (NotFoundException e) {
                clearSecurityContext(httpServletRequest);
                return Constants.ERROR;
            }
        } else {
            try {
                userDTO = userService.getUser(username);
            } catch (NotFoundException e) {
                return Constants.ERROR;
            }
        }

        if (userDTO.getRole().equals(UserDTO.Role.PARTICIPANT)) {
            experiments = participantService.getExperimentInfoForParticipant(userDTO.getId());
        }

        model.addAttribute("experiments", experiments);
        model.addAttribute(USER_DTO, userDTO);
        model.addAttribute("passwordDTO", new PasswordDTO());
        model.addAttribute("language",
                resourceBundle.getString(userDTO.getLanguage().toString().toLowerCase()));
        return PROFILE;
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
    @Secured(Constants.ROLE_PARTICIPANT)
    public String getEditProfileForm(@RequestParam(value = "name", required = false) final String username,
                                     final Model model, final HttpServletRequest httpServletRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            logger.error("Can't show the profile page for an unauthenticated user!");
            return Constants.ERROR;
        }

        if (username == null || username.trim().isBlank() || !httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            try {
                UserDTO userDTO = userService.getUser(authentication.getName());
                model.addAttribute(USER_DTO, userDTO);
                return PROFILE_EDIT;
            } catch (NotFoundException e) {
                clearSecurityContext(httpServletRequest);
                return Constants.ERROR;
            }
        }

        try {
            UserDTO userDTO = userService.getUser(username);
            model.addAttribute(USER_DTO, userDTO);
            return PROFILE_EDIT;
        } catch (NotFoundException e) {
            return Constants.ERROR;
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
    @Secured(Constants.ROLE_PARTICIPANT)
    public String updateUser(@ModelAttribute(USER_DTO) final UserDTO userDTO, final BindingResult bindingResult,
                             final HttpServletRequest httpServletRequest,
                             final HttpServletResponse httpServletResponse) {
        if (userDTO.getEmail() == null) {
            logger.error("The new email should never be null, but only an empty string!");
            return Constants.ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        UserDTO findOldUser;

        try {
            findOldUser = userService.getUserById(userDTO.getId());
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }

        if (!httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            if (!findOldUser.equals(userDTO)) {
                logger.error("Participant with id " + userDTO.getId() + " tried to edit the profile of user with id "
                        + findOldUser.getId() + "!");
                return Constants.ERROR;
            } else if (userDTO.getUsername() != null) {
                logger.error("Participant with id " + userDTO.getId() + " tried to change their username!");
                return Constants.ERROR;
            }
        }

        if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            validateUpdateUsername(userDTO, findOldUser, bindingResult, resourceBundle);
        }

        validateUpdateEmail(userDTO, findOldUser, bindingResult, resourceBundle);

        if (userDTO.getNewPassword() != null || userDTO.getConfirmPassword() != null) {
            validateUpdatePassword(userDTO, findOldUser, bindingResult, resourceBundle);
        }

        if (bindingResult.hasErrors()) {
            return PROFILE_EDIT;
        }

        String username = findOldUser.getUsername();

        if (!username.equals(userDTO.getUsername()) && httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            findOldUser.setUsername(userDTO.getUsername());
        }

        boolean sent = false;

        if (!findOldUser.getEmail().equals(userDTO.getEmail())) {
            if (Constants.MAIL_SERVER) {
                sent = updateEmail(userDTO.getEmail(), userDTO.getId(), resourceBundle);
            } else {
                findOldUser.setEmail(userDTO.getEmail());
            }
        }

        if (userDTO.getNewPassword() != null && !userDTO.getNewPassword().trim().isBlank()) {
            findOldUser.setPassword(userService.encodePassword(userDTO.getNewPassword()));
        }

        findOldUser.setLanguage(userDTO.getLanguage());
        UserDTO updated = userService.updateUser(findOldUser);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (username.equals(authentication.getName())) {
            clearSecurityContext(httpServletRequest);
            updateSecurityContext(updated, httpServletRequest);
            localeResolver.setLocale(httpServletRequest, httpServletResponse,
                    getLocaleFromLanguage(updated.getLanguage()));
        }

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
     * @param passwordDTO The {@link PasswordDTO} containing the input password.
     * @param id The id of the user to be deleted.
     * @param httpServletRequest The servlet request.
     * @return The index page on success, or the profile or error page.
     */
    @PostMapping("/delete")
    @Secured(Constants.ROLE_ADMIN)
    public String deleteUser(@ModelAttribute("passwordDTO") final PasswordDTO passwordDTO,
                             @RequestParam("id") final String id, final HttpServletRequest httpServletRequest) {
        if (id == null || passwordDTO.getPassword() == null) {
            logger.error("Cannot delete user with id null or input password null!");
            return Constants.ERROR;
        }

        int userId = parseId(id);

        if (userId < Constants.MIN_ID) {
            logger.error("Cannot delete user with invalid id " + id + "!");
            return Constants.ERROR;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getName() == null) {
            logger.error("User with authentication name null tried to delete user with id " + userId + "!");
            return Constants.ERROR;
        }

        try {
            UserDTO currentUser = userService.getUser(authentication.getName());
            UserDTO userDTO = userService.getUserById(userId);

            if ((passwordDTO.getPassword().length() > Constants.SMALL_FIELD)
                    || (!userService.matchesPassword(passwordDTO.getPassword(), currentUser.getPassword()))) {
                return "redirect:/users/profile?invalid=true&name=" + userDTO.getUsername();
            } else if (userDTO.getRole().equals(UserDTO.Role.ADMIN) && userService.isLastAdmin()) {
                return "redirect:/users/profile?lastAdmin=true";
            }

            userService.deleteUser(userDTO.getId());

            if (currentUser.equals(userDTO)) {
                clearSecurityContext(httpServletRequest);
            }

            return "redirect:/?success=true";
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Activates or deactivates the user account of the participant with the given id. If the account is being
     * deactivated, the participant's secret is set to null to prevent the user from participating in any experiments.
     * If the operation was successful, the user is redirected to the profile page. If anything went wrong, the user is
     * redirected to the error page instead.
     *
     * @param id The participant's id.
     * @return The participant's profile page on success, or the error page, otherwise.
     */
    @GetMapping("/active")
    @Secured(Constants.ROLE_ADMIN)
    public String changeActiveStatus(@RequestParam("id") final String id) {
        if (id == null) {
            logger.debug("Cannot change active status of user with id null!");
            return Constants.ERROR;
        }

        int userId = parseId(id);

        if (userId < Constants.MIN_ID) {
            logger.debug("Cannot change active status of user with invalid id " + id + "!");
            return Constants.ERROR;
        }

        try {
            UserDTO userDTO = userService.getUserById(userId);

            if (userDTO.getRole().equals(UserDTO.Role.ADMIN)) {
                logger.error("Cannot deactivate an administrator profile!");
                return Constants.ERROR;
            }

            if (userDTO.isActive()) {
                userDTO.setActive(false);
                userDTO.setSecret(null);
            } else {
                userDTO.setActive(true);
            }

            userService.updateUser(userDTO);
            return "redirect:/users/profile?name=" + userDTO.getUsername();
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }
    }

    /**
     * Retrieves the password page to (re)set the password of the user with the given id. If the user could not be
     * found or the passed id is invalid, the user is redirected to the error page instead.
     *
     * @param id The id of the user whose password is to be reset.
     * @param model The user dto containing the old user information.
     * @param httpServletRequest The servlet request.
     * @return The password page on success, or the error page otherwise.
     */
    @GetMapping("/forgot")
    @Secured(Constants.ROLE_ADMIN)
    public String getPasswordResetForm(@RequestParam("id") final String id, final Model model,
                                       final HttpServletRequest httpServletRequest) {
        if (id == null) {
            logger.debug("Cannot reset password for user with id null!");
            return Constants.ERROR;
        }

        int userId = parseId(id);

        if (userId < Constants.MIN_ID) {
            logger.debug("Cannot reset password for user with invalid id " + id + "!");
            return Constants.ERROR;
        }

        try {
            UserDTO userDTO = userService.getUserById(userId);
            if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
                model.addAttribute(USER_DTO, userDTO);
                return PASSWORD;
            }
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }

        return Constants.ERROR;
    }

    /**
     * Changes the current password of the given user to the specified new password, if it meets the requirements.
     *
     * @param userDTO The {@link UserDTO} containing the password inputs.
     * @param bindingResult The binding result for returning information on invalid user input.
     * @param httpServletRequest The servlet request.
     * @return The user profile page on success, or the error page otherwise.
     */
    @PostMapping("/forgot")
    @Secured(Constants.ROLE_ADMIN)
    public String passwordReset(@ModelAttribute(USER_DTO) final UserDTO userDTO, final BindingResult bindingResult,
                                final HttpServletRequest httpServletRequest) {
        if (userDTO.getPassword() == null || userDTO.getNewPassword() == null || userDTO.getConfirmPassword() == null) {
            logger.error("The new passwords should never be null, but only empty strings!");
            return Constants.ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        UserDTO findOldUser;

        try {
            findOldUser = userService.getUserById(userDTO.getId());
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }

        if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getName() == null) {
                logger.error("Cannot reset the password for user " + userDTO.getId() + " with authentication with name "
                        + "null!");
                return Constants.ERROR;
            }

            UserDTO admin;

            try {
                admin = userService.getUser(authentication.getName());
            } catch (NotFoundException e) {
                return Constants.ERROR;
            }

            if (userDTO.getNewPassword().trim().isBlank()) {
                bindingResult.addError(createFieldError(USER_DTO, "newPassword", "empty_string", resourceBundle));
            }

            validateUpdatePassword(userDTO, admin, bindingResult, resourceBundle);

            if (bindingResult.hasErrors()) {
                return PASSWORD;
            }

            findOldUser.setPassword(userService.encodePassword(userDTO.getNewPassword()));
            userService.saveUser(findOldUser);
            return "redirect:/users/profile?name=" + findOldUser.getUsername();
        }

        return Constants.ERROR;
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
                bindingResult.addError(createFieldError(USER_DTO, "username", usernameValidation, resourceBundle));
            } else if (userService.existsUser(userDTO.getUsername())) {
                bindingResult.addError(createFieldError(USER_DTO, "username", "username_exists", resourceBundle));
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
            bindingResult.addError(createFieldError(USER_DTO, "email", "empty_string", resourceBundle));
        }

        if (!userDTO.getEmail().trim().isBlank() && !userDTO.getEmail().equals(findOldUser.getEmail())) {
            String emailValidation = EmailValidator.validate(userDTO.getEmail());

            if (emailValidation != null) {
                bindingResult.addError(createFieldError(USER_DTO, "email", emailValidation, resourceBundle));
            } else if (userService.existsEmail(userDTO.getEmail())) {
                bindingResult.addError(createFieldError(USER_DTO, "email", "email_exists", resourceBundle));
            }
        }
    }

    /**
     * Validates the passwords passed in the {@link UserDTO} on updating the given user.
     *
     * @param userDTO The user dto containing the new user information.
     * @param matchPassword The user dto containing the current password the input password should match.
     * @param bindingResult The binding result for returning information on invalid user input.
     * @param resourceBundle The resource bundle for error translation.
     */
    private void validateUpdatePassword(final UserDTO userDTO, final UserDTO matchPassword,
                                     final BindingResult bindingResult, final ResourceBundle resourceBundle) {
        if (!userDTO.getNewPassword().trim().isBlank() || !userDTO.getConfirmPassword().trim().isBlank()) {
            String passwordValidation = PasswordValidator.validate(userDTO.getNewPassword(),
                    userDTO.getConfirmPassword());

            if (passwordValidation != null) {
                bindingResult.addError(createFieldError(USER_DTO, "newPassword", passwordValidation, resourceBundle));
            }

            if (userDTO.getPassword() == null || userDTO.getPassword().trim().isBlank()) {
                bindingResult.addError(createFieldError(USER_DTO, "password", "enter_password", resourceBundle));
            } else if (!userService.matchesPassword(userDTO.getPassword(), matchPassword.getPassword())) {
                bindingResult.addError(createFieldError(USER_DTO, "password", "invalid_password",
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
     * @param resourceBundle The resource bundle for message translations.
     * @return {@code true} if a token was created and the email sent, or {@code false} otherwise.
     */
    private boolean updateEmail(final String email, final int id, final ResourceBundle resourceBundle) {
        TokenDTO tokenDTO;

        try {
            tokenDTO = tokenService.generateToken(TokenDTO.Type.CHANGE_EMAIL, email, id);
        } catch (NotFoundException e) {
            return false;
        }

        return sendEmail(email, tokenDTO.getValue(), "change_email_subject", "change-email.html", resourceBundle);
    }

    /**
     * Sends an email is with the given subject and template to the given email address.
     *
     * @param email The email address.
     * @param value The token value to identify the user.
     * @param subject The email subject.
     * @param template The email template to be sent.
     * @param resourceBundle The resource bundle for message translation.
     * @return {@code true} if the email was sent, or {@code false} otherwise.
     */
    private boolean sendEmail(final String email, final String value, final String subject, final String template,
                              final ResourceBundle resourceBundle) {
        String tokenUrl = Constants.BASE_URL + "/token?value=" + value;
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("baseUrl", Constants.BASE_URL);
        templateModel.put("token", tokenUrl);
        return mailService.sendEmail(email, resourceBundle.getString(subject), templateModel, template);
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
