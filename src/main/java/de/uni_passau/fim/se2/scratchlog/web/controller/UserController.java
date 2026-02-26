/*
 * Copyright (C) 2023 ScratchLog contributors
 *
 * This file is part of ScratchLog.
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ScratchLog. If not, see <http://www.gnu.org/licenses/>.
 */

package de.uni_passau.fim.se2.scratchlog.web.controller;

import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.service.MailService;
import de.uni_passau.fim.se2.scratchlog.application.service.ParticipantService;
import de.uni_passau.fim.se2.scratchlog.application.service.TokenService;
import de.uni_passau.fim.se2.scratchlog.application.service.UserService;
import de.uni_passau.fim.se2.scratchlog.spring.authentication.CustomAuthenticationProvider;
import de.uni_passau.fim.se2.scratchlog.util.ApplicationProperties;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.util.FieldErrorHandler;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import de.uni_passau.fim.se2.scratchlog.util.enums.TokenType;
import de.uni_passau.fim.se2.scratchlog.util.validation.EmailValidator;
import de.uni_passau.fim.se2.scratchlog.util.validation.PasswordValidator;
import de.uni_passau.fim.se2.scratchlog.util.validation.StringValidator;
import de.uni_passau.fim.se2.scratchlog.util.validation.UsernameValidator;
import de.uni_passau.fim.se2.scratchlog.web.dto.CsvFileDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.PasswordDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.TokenDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserBulkDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    /**
     * The field name of the CSV file input when adding users through CSV.
     */
    private static final String FIELD_CSV_ADD_FILE = "file";

    /**
     * The global application config.
     */
    private final ApplicationProperties applicationProperties;

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
    private final Optional<MailService> mailService;

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
     * String corresponding to the page for adding users in bulk.
     */
    private static final String USERS_ADD = "users-add";

    /**
     * String corresponding to the userDTO model attribute.
     */
    private static final String USER_DTO = "userDTO";

    /**
     * String corresponding to the error model attribute.
     */
    private static final String ERROR = "error";

    /**
     * Constructs a new user controller with the given dependencies.
     *
     * @param applicationProperties The {@link ApplicationProperties} to use.
     * @param userService The {@link UserService} to use.
     * @param participantService The {@link ParticipantService} to use.
     * @param mailService The {@link MailService} to use.
     * @param tokenService The {@link TokenService} to use.
     * @param authenticationProvider The {@link CustomAuthenticationProvider} to use.
     * @param localeResolver The locale resolver to use.
     */
    @Autowired
    public UserController(final ApplicationProperties applicationProperties,
                          final UserService userService, final ParticipantService participantService,
                          final Optional<MailService> mailService, final TokenService tokenService,
                          final CustomAuthenticationProvider authenticationProvider,
                          final LocaleResolver localeResolver) {
        this.applicationProperties = applicationProperties;
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
     * @param experimentId The id of the experiment in which the user is participating.
     * @param secret The user's secret.
     * @param httpServletRequest The servlet request.
     * @param httpServletResponse The servlet response.
     * @return The experiment page on success, or the error page, otherwise.
     */
    @GetMapping("/authenticate")
    public String authenticateUser(@RequestParam("id") final int experimentId,
                                   @RequestParam("secret") final String secret,
                                   final HttpServletRequest httpServletRequest,
                                   final HttpServletResponse httpServletResponse) {
        if (secret == null || secret.trim().isBlank()) {
            LOGGER.error("Cannot authenticate participant with id or secret null or blank!");
            return Constants.ERROR;
        }

        UserDTO authenticated;
        try {
            authenticated = userService.authenticateUser(secret);
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }

        if (!userService.existsParticipant(authenticated.getId(), experimentId)) {
            LOGGER.error(
                "No participation entry could be found for the user with username {} and experiment with id {}!",
                authenticated.getUsername(),
                experimentId
            );
            return Constants.ERROR;
        } else if (httpServletRequest.isUserInRole(Constants.ROLE_PARTICIPANT)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authenticated.getUsername().equals(authentication.getName())) {
                LOGGER.error("Cannot authenticate participant with different username!");
                return Constants.ERROR;
            }
        }

        clearSecurityContext(httpServletRequest);
        updateSecurityContext(authenticated, httpServletRequest);
        localeResolver.setLocale(httpServletRequest, httpServletResponse,
            authenticated.getLanguage().toLocale());
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
        String usernameValidation = StringValidator.validate(userDTO.getUsername(), Constants.SMALL_FIELD);
        String passwordValidation = StringValidator.validate(userDTO.getPassword(), Constants.SMALL_FIELD);

        if (usernameValidation != null) {
            FieldErrorHandler.addFieldError(bindingResult, USER_DTO, "username", usernameValidation, resourceBundle);
        }
        if (passwordValidation != null) {
            FieldErrorHandler.addFieldError(bindingResult, USER_DTO, "password", passwordValidation, resourceBundle);
        }

        if (bindingResult.hasErrors()) {
            return LOGIN;
        }

        try {
            UserDTO findUser = userService.getUser(userDTO.getUsername());

            if (!findUser.isActive()) {
                LOGGER.debug("Tried to log in inactive user with username {}.", userDTO.getUsername());
                model.addAttribute(ERROR, resourceBundle.getString("activate_first"));
                return LOGIN;
            } else if (findUser.getAttempts() >= Constants.MAX_LOGIN_ATTEMPTS) {
                findUser.setActive(false);
                userService.updateUser(findUser);
                tokenService.generateToken(TokenType.DEACTIVATED, "", findUser.getId());
                LOGGER.info(
                    "Deactivated account of user with username {}"
                    + "due to exceeding the maximum number of login attempts!",
                    userDTO.getUsername()
                );
                model.addAttribute(ERROR, resourceBundle.getString("account_deactivated"));
                return LOGIN;
            }

            if (userService.loginUser(userDTO)) {
                clearSecurityContext(httpServletRequest);
                updateSecurityContext(findUser, httpServletRequest);
                localeResolver.setLocale(
                    httpServletRequest,
                    httpServletResponse,
                    findUser.getLanguage() != null
                        ? findUser.getLanguage().toLocale()
                        : Constants.DEFAULT_LANGUAGE.toLocale());

                return INDEX;
            } else {
                model.addAttribute(ERROR, resourceBundle.getString("authentication_error"));
                return LOGIN;
            }
        } catch (NotFoundException e) {
            LOGGER.error("Failed to log in user with username {}.", userDTO.getUsername(), e);
            model.addAttribute(ERROR, resourceBundle.getString("authentication_error"));
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
            LOGGER.error("Can't logout an unauthenticated user!");
            return Constants.ERROR;
        }

        if (!userService.existsUser(authentication.getName())) {
            LOGGER.error("Can't find user with username {} in the database!", authentication.getName());
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
            LOGGER.error("Cannot add new user with id not null!");
            return Constants.ERROR;
        } else if (userDTO.getLanguage() == null || userDTO.getRole() == null || userDTO.getEmail() == null) {
            LOGGER.error("Cannot add new user with language, role, or email null!");
            return Constants.ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        validateEmail(userDTO.getEmail(), bindingResult, resourceBundle);
        validateUpdateUsername(userDTO.getUsername(), bindingResult, resourceBundle);

        if (bindingResult.hasErrors()) {
            return USER;
        }

        userDTO.setLastLogin(LocalDateTime.now());
        UserDTO saved = userService.saveUser(userDTO);

        if (!applicationProperties.useMail()) {
            return "redirect:/users/profile?name=" + saved.getUsername();
        } else {
            TokenDTO tokenDTO = tokenService.generateToken(TokenType.REGISTER, null, saved.getId());

            if (sendEmail(userDTO.getEmail(), tokenDTO.getValue(), "password_set", "password-set-email.html",
                    resourceBundle)) {
                return "redirect:/?success=true";
            } else {
                return Constants.ERROR;
            }
        }
    }

    /**
     * Returns the 'Add Users in Bulk' page for adding a number of new users, or the home page in case mailing is
     * enabled.
     *
     * @param userBulkDTO The {@link UserBulkDTO} used to save the information.
     * @return The add participants page.
     */
    @GetMapping("/bulk")
    @Secured(Constants.ROLE_ADMIN)
    public String getAddUsersInBulk(final UserBulkDTO userBulkDTO) {
        if (applicationProperties.useMail()) {
            return INDEX;
        }

        return USERS_ADD;
    }

    /**
     * Adds multiple users in bulk to the database according to the data in {@code userBulkDTO}. If not starting at one,
     * the user id is used as distinction in the usernames. If starting at one, starts the numbering at one if possible,
     * else starts numbering at the current maximum number plus one. Returns a CSV of all the added users with their
     * randomly chosen passwords on success, or the error page for invalid inputs.
     *
     * @param userBulkDTO The {@link UserBulkDTO} containing the necessary information.
     * @param bindingResult The {@link BindingResult} to return information on an invalid username pattern.
     * @return A CSV response of the added users, or the add participants or error page otherwise.
     */
    @PostMapping("/bulk")
    @Secured(Constants.ROLE_ADMIN)
    public Object addUsersInBulk(final UserBulkDTO userBulkDTO, final BindingResult bindingResult) {
        if (userBulkDTO.getUsername() == null || userBulkDTO.getLanguage() == null) {
            LOGGER.error("Cannot add participants with username or language null!");
            return Constants.ERROR;
        } else if (
            userBulkDTO.getAmount() < 1 || userBulkDTO.getAmount() > applicationProperties.getMaxUserBulkImportCount()
        ) {
            LOGGER.error("Cannot add an illegal number of {} participants!", userBulkDTO.getAmount());
            return Constants.ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        String usernameValidation = FieldErrorHandler.validateUsername(userBulkDTO.getUsername(), bindingResult,
                resourceBundle);

        if (usernameValidation != null) {
            return USERS_ADD;
        }

        List<UserDTO> addedUsers = userService.addUsersInBulk(userBulkDTO);
        String csv = userService.generateUsernamePasswordCsv(addedUsers);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users.csv\"")
            .body(csv);
    }

    /**
     * Returns the page for creating new users from a CSV file and prepares the model for a file upload.
     *
     * @return The 'add users via CSV' page.
     */
    @GetMapping("/csv")
    @Secured(Constants.ROLE_ADMIN)
    public String getAddUsersViaCSV(Model model) {
        model.addAttribute("fileDTO", new CsvFileDTO());
        return "users-csv";
    }

    /**
     * Creates new users in the database with the information provided by the given CSV file. Another CSV file
     * containing information about the passwords generated for each user is returned. If the passed file is invalid,
     * users could not be added or the file could not be parsed correctly, the 'add users via CSV' page is returned,
     * where a corresponding error message is displayed.
     *
     * @param fileDTO The file containing the user information.
     * @param bindingResult The {@link BindingResult} used to store information on errors.
     * @return The CSV file containing information on the created users on success, or the 'add users via CSV' page
     *         otherwise.
     */
    @PostMapping("/csv")
    @Secured(Constants.ROLE_ADMIN)
    public Object addUsersViaCSV(@Valid @ModelAttribute("fileDTO") CsvFileDTO fileDTO,
                                 final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "users-csv";
        }

        MultipartFile file = fileDTO.getFile();
        List<UserDTO> users;

        try {
            users = userService.parseUserListCsv(file);
        } catch (IOException e) {
            LOGGER.error("Error parsing CSV file!", e);
            bindingResult.rejectValue(FIELD_CSV_ADD_FILE, "csv_error");
            return "users-csv";
        }

        if (!isValidUserInfo(users, bindingResult)) {
            return "users-csv";
        }

        users.stream().parallel().forEach(userService::completeUserInformation);
        userService.saveUsers(users);
        String csv = userService.generateUsernamePasswordCsv(users);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users.csv\"")
            .body(csv);
    }

    /**
     * Generates a password reset token for the given user and sends an email to complete the password reset. If the
     * passed parameters are invalid or no user with matching username and email could be found, nothing happens.
     *
     * @param userDTO The {@link UserDTO} used to save the new user data.
     * @return The index page displaying further information.
     */
    @PostMapping("/reset")
    public String passwordReset(@ModelAttribute(USER_DTO) final UserDTO userDTO) {
        if (userDTO.getUsername() == null || userDTO.getEmail() == null || userDTO.getUsername().trim().isBlank()
                || userDTO.getEmail().trim().isBlank()) {
            LOGGER.error("Cannot reset password for user with username or email null or blank!");
            return Constants.ERROR;
        } else if (userDTO.getUsername().length() > Constants.SMALL_FIELD
                || userDTO.getEmail().length() > Constants.LARGE_FIELD) {
            LOGGER.error("Cannot reset password for user with input username or email too long!");
            return Constants.ERROR;
        } else if (!applicationProperties.useMail()) {
            LOGGER.warn("Cannot reset password without a mail server!");
            return Constants.ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());

        try {
            UserDTO findUsername = userService.getUser(userDTO.getUsername());
            UserDTO findEmail = userService.getUserByEmail(userDTO.getEmail());

            if (findEmail.equals(findUsername)) {
                TokenDTO tokenDTO = tokenService.generateToken(TokenType.FORGOT_PASSWORD, null, findEmail.getId());
                sendEmail(userDTO.getEmail(), tokenDTO.getValue(), "password_set", "password-set-email.html",
                        resourceBundle);
            }

            return "redirect:/?info=true";
        } catch (NotFoundException e) {
            return "redirect:/?info=true";
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
            LOGGER.error("Can't show the profile page for an unauthenticated user!");
            return Constants.ERROR;
        }

        UserDTO userDTO;
        Map<Integer, String> experiments = new HashMap<>();

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

        if (userDTO.getRole().equals(Role.PARTICIPANT)) {
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
            LOGGER.error("Can't show the profile page for an unauthenticated user!");
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
            LOGGER.error("The new email should never be null, but only an empty string!");
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
                LOGGER.error(
                    "Participant with id {} tried to edit the profile of user with id {}!",
                    userDTO.getId(), findOldUser.getId()
                );
                return Constants.ERROR;
            } else if (userDTO.getUsername() != null) {
                LOGGER.error("Participant with id {} tried to change their username!", userDTO.getId());
                return Constants.ERROR;
            }
        }

        if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)
                && !findOldUser.getUsername().equals(userDTO.getUsername())) {
            validateUpdateUsername(userDTO.getUsername(), bindingResult, resourceBundle);
        }

        validateUpdateEmail(userDTO, findOldUser, bindingResult, resourceBundle);

        if (userDTO.getNewPassword() != null || userDTO.getConfirmPassword() != null) {
            if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
                UserDTO admin = userService.getUser(httpServletRequest.getUserPrincipal().getName());
                validateUpdatePassword(userDTO, admin.getPassword(), findOldUser.getPassword(), bindingResult,
                        resourceBundle);

                // If the admin changed their own password, delete the associated ADMIN_WITH_RANDOM_PASSWORD token.
                if (admin.equals(findOldUser)) {
                    tokenService.deleteRandomPasswordToken(admin.getId());
                }
            } else {
                validateUpdatePassword(userDTO, findOldUser.getPassword(), findOldUser.getPassword(), bindingResult,
                        resourceBundle);
            }
        }

        if (bindingResult.hasErrors()) {
            return PROFILE_EDIT;
        }

        String username = findOldUser.getUsername();

        if (!username.equals(userDTO.getUsername()) && httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            findOldUser.setUsername(userDTO.getUsername());
        }

        boolean sent = false;

        if (!userDTO.getEmail().trim().isBlank() && !userDTO.getEmail().equals(findOldUser.getEmail())) {
            if (applicationProperties.useMail()) {
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
                updated.getLanguage().toLocale());
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
     * @param userId The id of the user to be deleted.
     * @param httpServletRequest The servlet request.
     * @return The index page on success, or the profile or error page.
     */
    @PostMapping("/delete")
    @Secured(Constants.ROLE_ADMIN)
    public String deleteUser(@ModelAttribute("passwordDTO") final PasswordDTO passwordDTO,
                             @RequestParam("id") final int userId, final HttpServletRequest httpServletRequest) {
        if (passwordDTO.getPassword() == null) {
            LOGGER.error("Cannot delete user with id null or input password null!");
            return Constants.ERROR;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getName() == null) {
            LOGGER.error("User with authentication name null tried to delete user with id {}!", userId);
            return Constants.ERROR;
        }

        try {
            UserDTO currentUser = userService.getUser(authentication.getName());
            UserDTO userDTO = userService.getUserById(userId);

            if ((passwordDTO.getPassword().length() > Constants.SMALL_FIELD)
                    || (!userService.matchesPassword(passwordDTO.getPassword(), currentUser.getPassword()))) {
                return "redirect:/users/profile?invalid=true&name=" + userDTO.getUsername();
            } else if (userDTO.getRole().equals(Role.ADMIN) && userService.isLastAdmin()) {
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
     * @param userId The participant's id.
     * @return The participant's profile page on success, or the error page, otherwise.
     */
    @GetMapping("/active")
    @Secured(Constants.ROLE_ADMIN)
    public String changeActiveStatus(@RequestParam("id") final int userId) {
        try {
            UserDTO userDTO = userService.getUserById(userId);

            if (userDTO.getRole().equals(Role.ADMIN) && userDTO.isActive()) {
                LOGGER.error("Cannot deactivate an administrator profile!");
                return Constants.ERROR;
            }

            if (userDTO.isActive()) {
                userDTO.setActive(false);
                userDTO.setSecret(null);
            } else {
                userDTO.setActive(true);
                userDTO.setAttempts(0);
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
     * @param userId The id of the user whose password is to be reset.
     * @param model The user dto containing the old user information.
     * @param httpServletRequest The servlet request.
     * @return The password page on success, or the error page otherwise.
     */
    @GetMapping("/forgot")
    @Secured(Constants.ROLE_ADMIN)
    public String getPasswordResetForm(@RequestParam("id") final int userId, final Model model,
                                       final HttpServletRequest httpServletRequest) {
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
            LOGGER.error("The new passwords should never be null, but only empty strings!");
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
                LOGGER.error(
                    "Cannot reset the password for user {} with authentication with name null!", userDTO.getId()
                );
                return Constants.ERROR;
            }

            UserDTO admin;

            try {
                admin = userService.getUser(authentication.getName());
            } catch (NotFoundException e) {
                return Constants.ERROR;
            }

            if (userDTO.getNewPassword().trim().isBlank()) {
                FieldErrorHandler.addFieldError(bindingResult, USER_DTO, "newPassword", "empty_string", resourceBundle);
            }

            validateUpdatePassword(userDTO, admin.getPassword(), findOldUser.getPassword(), bindingResult,
                    resourceBundle);

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
     * Validates that the given username is a valid username and does not yet exist in the database.
     *
     * @param username The user username to be checked.
     * @param bindingResult The {@link BindingResult} for returning information on invalid user input.
     * @param resourceBundle The {@link ResourceBundle} for error fetching error messages in the correct language.
     */
    private void validateUpdateUsername(final String username, final BindingResult bindingResult,
                                        final ResourceBundle resourceBundle) {
        String usernameValidation = FieldErrorHandler.validateUsername(username, bindingResult, resourceBundle);

        if (usernameValidation == null && userService.existsUser(username)) {
            FieldErrorHandler.addFieldError(bindingResult, USER_DTO, "username", "username_exists", resourceBundle);
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
            FieldErrorHandler.addFieldError(bindingResult, USER_DTO, "email", "empty_string", resourceBundle);
        }

        if (!userDTO.getEmail().trim().isBlank() && !userDTO.getEmail().equals(findOldUser.getEmail())) {
            validateEmail(userDTO.getEmail(), bindingResult, resourceBundle);
        }
    }

    /**
     * Validates whether the given email is valid and not yet present in the database.
     *
     * @param email The email to be checked.
     * @param bindingResult The {@link BindingResult} for returning information on invalid user input.
     * @param resourceBundle The {@link ResourceBundle} for error fetching error messages in the correct language.
     */
    private void validateEmail(final String email, final BindingResult bindingResult,
                                     final ResourceBundle resourceBundle) {
        String emailValidation = FieldErrorHandler.validateEmail(email, bindingResult, resourceBundle);

        if (emailValidation == null && userService.existsEmail(email)) {
            FieldErrorHandler.addFieldError(bindingResult, USER_DTO, "email", "email_exists", resourceBundle);
        }
    }

    /**
     * Validates the passwords passed in the {@link UserDTO} on updating the given user.
     *
     * @param userDTO The user dto containing the new user information.
     * @param matchPassword The password the input password should match.
     * @param oldPassword The user's old password.
     * @param bindingResult The binding result for returning information on invalid user input.
     * @param resourceBundle The resource bundle for error translation.
     */
    private void validateUpdatePassword(final UserDTO userDTO, final String matchPassword, final String oldPassword,
                                        final BindingResult bindingResult, final ResourceBundle resourceBundle) {
        if (!userDTO.getNewPassword().trim().isBlank() || !userDTO.getConfirmPassword().trim().isBlank()) {
            String passwordValidation = PasswordValidator.validate(userDTO.getNewPassword(),
                    userDTO.getConfirmPassword());

            if (passwordValidation != null) {
                FieldErrorHandler.addFieldError(bindingResult, USER_DTO, "newPassword", passwordValidation,
                        resourceBundle);
            }

            if (userDTO.getPassword() == null || userDTO.getPassword().trim().isBlank()) {
                FieldErrorHandler.addFieldError(bindingResult, USER_DTO, "password", "enter_password", resourceBundle);
            } else if (!userService.matchesPassword(userDTO.getPassword(), matchPassword)) {
                FieldErrorHandler.addFieldError(bindingResult, USER_DTO, "password", "invalid_password",
                        resourceBundle);
            } else if (Objects.equals(oldPassword, userDTO.getNewPassword())) {
                FieldErrorHandler.addFieldError(bindingResult, USER_DTO, "newPassword", "old_password", resourceBundle);
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
            tokenDTO = tokenService.generateToken(TokenType.CHANGE_EMAIL, email, id);
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
        if (mailService.isEmpty()) {
            LOGGER.debug("Cannot send emails when mailing is disabled!");
            return false;
        }
        String tokenUrl = applicationProperties.getApplicationUrl() + "/token?value=" + value;
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("applicationName", applicationProperties.getApplicationName());
        templateModel.put("baseUrl", applicationProperties.getApplicationUrl());
        templateModel.put("token", tokenUrl);
        return mailService.get().sendEmail(email, resourceBundle.getString(subject), templateModel, template);
    }

    /**
     * Checks, whether the given list of DTOs contains valid usernames and emails. If the size of the list is more than
     * the maximum allowed size, it is considered invalid as well.
     *
     * @param users The list of users.
     * @param bindingResult The {@link BindingResult} to add errors to.
     * @return {@code true} if all user information is valid, or {@code false} otherwise.
     */
    private boolean isValidUserInfo(final List<UserDTO> users, final BindingResult bindingResult) {
        List<String> invalidAttributes = new ArrayList<>();
        List<String> invalidPasswords = new ArrayList<>();

        if (users.size() > applicationProperties.getMaxUserBulkImportCount()) {
            bindingResult.rejectValue(FIELD_CSV_ADD_FILE, "max_users",
                new Object[]{ applicationProperties.getMaxUserBulkImportCount() }, null);
            return false;
        }

        users.forEach(userDTO -> checkValidUserInfo(userDTO, invalidAttributes, invalidPasswords));

        if (!invalidAttributes.isEmpty()) {
            bindingResult.rejectValue(FIELD_CSV_ADD_FILE, "invalid_attributes",
                new Object[]{ invalidAttributes }, null);
            return false;
        }
        if (!invalidPasswords.isEmpty()) {
            bindingResult.rejectValue(FIELD_CSV_ADD_FILE, "invalid_passwords",
                new Object[]{ invalidPasswords }, null);
            return false;
        }

        Set<String> existingAttributes = userService.findAlreadyExistingByUsernameOrEmail(users);
        if (!existingAttributes.isEmpty()) {
            bindingResult.rejectValue(FIELD_CSV_ADD_FILE, "existing_attributes",
                new Object[]{ existingAttributes }, null);
            return false;
        }

        return containsDuplicateUsernamesOrEmails(users, bindingResult);
    }

    /**
     * Checks, if the username, password and email address of the given user meet the requirements and cannot be found
     * in the database.
     *
     * @param userDTO The DTO containing the information to check.
     * @param invalid A list used to store all invalid usernames and emails.
     * @param passwords A list used to store all usernames with invalid passwords.
     */
    private void checkValidUserInfo(final UserDTO userDTO, final List<String> invalid, final List<String> passwords) {
        if (UsernameValidator.validate(userDTO.getUsername()) != null) {
            invalid.add(userDTO.getUsername());
        }
        if (userDTO.getEmail() != null && EmailValidator.validate(userDTO.getEmail()) != null) {
            invalid.add(userDTO.getEmail());
        }
        if (userDTO.getPassword() != null
                && PasswordValidator.validate(userDTO.getPassword(), userDTO.getPassword()) != null) {
            passwords.add(userDTO.getUsername());
        }
    }

    /**
     * Checks if the usernames and emails contained in the given list of users are unique. If not, a corresponding error
     * message is added to the binding result.
     *
     * @param users The list of users.
     * @param bindingResult The {@link BindingResult} to add errors to.
     * @return {@code true} if no duplicate entries exist, or {@code false} otherwise.
     */
    private boolean containsDuplicateUsernamesOrEmails(final List<UserDTO> users, final BindingResult bindingResult) {
        Set<String> names = new HashSet<>();
        Set<String> emails = new HashSet<>();
        users.forEach(userDTO -> {
            names.add(userDTO.getUsername());
            emails.add(userDTO.getEmail());
        });

        if (names.size() < users.size()) {
            bindingResult.rejectValue(FIELD_CSV_ADD_FILE, "duplicate_usernames");
            return false;
        } else if (emails.size() < users.size() && !users.stream().allMatch(userDTO -> userDTO.getEmail() == null)) {
            bindingResult.rejectValue(FIELD_CSV_ADD_FILE, "duplicate_emails");
            return false;
        }

        return true;
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
