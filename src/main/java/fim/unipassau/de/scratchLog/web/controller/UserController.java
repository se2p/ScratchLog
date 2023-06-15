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

package fim.unipassau.de.scratchLog.web.controller;

import com.opencsv.bean.CsvToBeanBuilder;
import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.application.service.MailService;
import fim.unipassau.de.scratchLog.application.service.ParticipantService;
import fim.unipassau.de.scratchLog.application.service.TokenService;
import fim.unipassau.de.scratchLog.application.service.UserService;
import fim.unipassau.de.scratchLog.spring.authentication.CustomAuthenticationProvider;
import fim.unipassau.de.scratchLog.util.ApplicationProperties;
import fim.unipassau.de.scratchLog.util.Constants;
import fim.unipassau.de.scratchLog.util.CustomPasswordGenerator;
import fim.unipassau.de.scratchLog.util.FieldErrorHandler;
import fim.unipassau.de.scratchLog.util.NumberParser;
import fim.unipassau.de.scratchLog.util.enums.Language;
import fim.unipassau.de.scratchLog.util.enums.Role;
import fim.unipassau.de.scratchLog.util.enums.TokenType;
import fim.unipassau.de.scratchLog.util.validation.EmailValidator;
import fim.unipassau.de.scratchLog.util.validation.PasswordValidator;
import fim.unipassau.de.scratchLog.util.validation.StringValidator;
import fim.unipassau.de.scratchLog.util.validation.UsernameValidator;
import fim.unipassau.de.scratchLog.web.dto.PasswordDTO;
import fim.unipassau.de.scratchLog.web.dto.TokenDTO;
import fim.unipassau.de.scratchLog.web.dto.UserBulkDTO;
import fim.unipassau.de.scratchLog.web.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

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
     * String corresponding to the add participants page.
     */
    private static final String PARTICIPANTS_ADD = "participants-add";

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
            LOGGER.error("Cannot authenticate participant with id or secret null or blank!");
            return Constants.ERROR;
        }

        int experimentId = NumberParser.parseNumber(id);
        UserDTO authenticated;

        if (experimentId < Constants.MIN_ID) {
            LOGGER.debug("Cannot authenticate user with invalid experiment id " + id + "!");
            return Constants.ERROR;
        }

        try {
            authenticated = userService.authenticateUser(secret);
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }

        if (!userService.existsParticipant(authenticated.getId(), experimentId)) {
            LOGGER.error("No participation entry could be found for the user with username "
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
                LOGGER.debug("Tried to log in inactive user with username " + userDTO.getUsername() + ".");
                model.addAttribute(ERROR, resourceBundle.getString("activate_first"));
                return LOGIN;
            } else if (findUser.getAttempts() >= Constants.MAX_LOGIN_ATTEMPTS) {
                findUser.setActive(false);
                userService.updateUser(findUser);
                tokenService.generateToken(TokenType.DEACTIVATED, "", findUser.getId());
                LOGGER.info("Deactivated account of user with username " + userDTO.getUsername()
                        + " due to exceeding the maximum number of login attempts!");
                model.addAttribute(ERROR, resourceBundle.getString("account_deactivated"));
                return LOGIN;
            }

            if (userService.loginUser(userDTO)) {
                clearSecurityContext(httpServletRequest);
                updateSecurityContext(findUser, httpServletRequest);
                localeResolver.setLocale(httpServletRequest, httpServletResponse,
                        getLocaleFromLanguage(findUser.getLanguage()));
                return INDEX;
            } else {
                model.addAttribute(ERROR, resourceBundle.getString("authentication_error"));
                return LOGIN;
            }
        } catch (NotFoundException e) {
            LOGGER.error("Failed to log in user with username " + userDTO.getUsername() + ".", e);
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
            LOGGER.error("Can't find user with username " + authentication.getName() + " in the database!");
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

        if (!ApplicationProperties.MAIL_SERVER) {
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
     * Returns the add participants page for adding a number of new participants.
     *
     * @param userBulkDTO The {@link UserBulkDTO} used to save the information.
     * @return The add participants page.
     */
    @GetMapping("/bulk")
    @Secured(Constants.ROLE_ADMIN)
    public String getAddParticipants(final UserBulkDTO userBulkDTO) {
        if (ApplicationProperties.MAIL_SERVER) {
            return INDEX;
        }

        return PARTICIPANTS_ADD;
    }

    /**
     * Adds the given amount of participants to the database if the numbered username doesn't yet exist. For any
     * username that already exists in the database, the corresponding username is saved to a list. If the given
     * username pattern is invalid or a pre-existing username has been found, the user returns to the add participants
     * page where corresponding information is displayed. If the any necessary information passed is invalid, the user
     * is redirected to the error page instead.
     *
     * @param userBulkDTO The {@link UserBulkDTO} containing the necessary information.
     * @param bindingResult The {@link BindingResult} to return information on an invalid username pattern.
     * @param model The {@link Model} used to store information on existing usernames.
     * @return The index page on success, or the add participants or error page otherwise.
     */
    @PostMapping("/bulk")
    @Secured(Constants.ROLE_ADMIN)
    public String addParticipants(final UserBulkDTO userBulkDTO, final BindingResult bindingResult, final Model model) {
        if (userBulkDTO.getUsername() == null || userBulkDTO.getLanguage() == null) {
            LOGGER.error("Cannot add participants with username or language null!");
            return Constants.ERROR;
        } else if (userBulkDTO.getAmount() < 1 || userBulkDTO.getAmount() > Constants.MAX_ADD_PARTICIPANTS) {
            LOGGER.error("Cannot add an illegal number of " + userBulkDTO.getAmount() + " participants!");
            return Constants.ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());
        String usernameValidation = FieldErrorHandler.validateUsername(userBulkDTO.getUsername(), bindingResult,
                resourceBundle);

        if (usernameValidation != null) {
            return PARTICIPANTS_ADD;
        }

        int number = userBulkDTO.isStartAtOne() ? userService.findValidNumberForUsername(userBulkDTO.getUsername())
                : userService.findLastId() + 1;
        List<String> invalidUsernames = new ArrayList<>();

        for (int i = 0; i < userBulkDTO.getAmount(); i++) {
            String username = userBulkDTO.getUsername() + number;

            if (userService.existsUser(username)) {
                invalidUsernames.add(username);
            } else {
                UserDTO userDTO = new UserDTO(userBulkDTO.getUsername() + number, null, Role.PARTICIPANT,
                        userBulkDTO.getLanguage(), null, null);
                userDTO.setActive(true);
                userDTO.setLastLogin(LocalDateTime.now());
                userService.saveUser(userDTO);
            }

            number++;
        }

        if (invalidUsernames.isEmpty()) {
            return "redirect:/?success=true";
        } else {
            model.addAttribute(ERROR, invalidUsernames);
            return PARTICIPANTS_ADD;
        }
    }

    /**
     * Returns the CSV participants page to create new users from a CSV file.
     *
     * @return The CSV participants page.
     */
    @GetMapping("/csv")
    @Secured(Constants.ROLE_ADMIN)
    public String getCSVParticipants() {
        return "participants-csv";
    }

    /**
     * Creates new users in the database with the information provided by the given CSV file. Another CSV file
     * containing information about the passwords generated for each user is returned. If the passed file is invalid,
     * users could not be added or the file could not be parsed correctly, the CSV participants page is returned where
     * a corresponding error message is displayed.
     *
     * @param file The file containing the user information.
     * @param model The {@link Model} used to store information on errors.
     * @return The CSV file containing information on the created users on success, or the CSV participants page
     * otherwise.
     */
    @PostMapping("/csv")
    @Secured(Constants.ROLE_ADMIN)
    public Object addCSVParticipants(@RequestParam("file") final MultipartFile file, final Model model) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());

        if (isInvalidFile(file, model, resourceBundle)) {
            return "participants-csv";
        }

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<UserDTO> users = new CsvToBeanBuilder<UserDTO>(reader).withType(UserDTO.class).build().parse();

            if (isValidUserInfo(users, model, resourceBundle)) {
                Random random = new Random();
                StringBuilder builder = new StringBuilder("username, password" + System.lineSeparator());
                users.forEach(userDTO -> completeUserInformation(userDTO, random, builder));
                userService.saveUsers(users);
                return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users.csv"
                        + "\"").body(builder.toString());
            } else {
                return "participants-csv";
            }
        } catch (IOException e) {
            LOGGER.error("Error parsing CSV file!", e);
            model.addAttribute(ERROR, resourceBundle.getString("csv_error"));
            return "participants-csv";
        }
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
        } else if (!ApplicationProperties.MAIL_SERVER) {
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
                LOGGER.error("Participant with id " + userDTO.getId() + " tried to edit the profile of user with id "
                        + findOldUser.getId() + "!");
                return Constants.ERROR;
            } else if (userDTO.getUsername() != null) {
                LOGGER.error("Participant with id " + userDTO.getId() + " tried to change their username!");
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
            if (ApplicationProperties.MAIL_SERVER) {
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
            LOGGER.error("Cannot delete user with id null or input password null!");
            return Constants.ERROR;
        }

        int userId = NumberParser.parseNumber(id);

        if (userId < Constants.MIN_ID) {
            LOGGER.error("Cannot delete user with invalid id " + id + "!");
            return Constants.ERROR;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getName() == null) {
            LOGGER.error("User with authentication name null tried to delete user with id " + userId + "!");
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
     * @param id The participant's id.
     * @return The participant's profile page on success, or the error page, otherwise.
     */
    @GetMapping("/active")
    @Secured(Constants.ROLE_ADMIN)
    public String changeActiveStatus(@RequestParam("id") final String id) {
        if (id == null) {
            LOGGER.debug("Cannot change active status of user with id null!");
            return Constants.ERROR;
        }

        int userId = NumberParser.parseNumber(id);

        if (userId < Constants.MIN_ID) {
            LOGGER.debug("Cannot change active status of user with invalid id " + id + "!");
            return Constants.ERROR;
        }

        try {
            UserDTO userDTO = userService.getUserById(userId);

            if (userDTO.getRole().equals(Role.ADMIN)) {
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
            LOGGER.debug("Cannot reset password for user with id null!");
            return Constants.ERROR;
        }

        int userId = NumberParser.parseNumber(id);

        if (userId < Constants.MIN_ID) {
            LOGGER.debug("Cannot reset password for user with invalid id " + id + "!");
            return Constants.ERROR;
        }

        try {
            UserDTO userDTO = userService.getUserById(userId);

            if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN) && !userDTO.getRole().equals(Role.ADMIN)) {
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

        if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN) && !findOldUser.getRole().equals(Role.ADMIN)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication.getName() == null) {
                LOGGER.error("Cannot reset the password for user " + userDTO.getId() + " with authentication with name "
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
        String tokenUrl = ApplicationProperties.BASE_URL + ApplicationProperties.CONTEXT_PATH + "/token?value=" + value;
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("baseUrl", ApplicationProperties.BASE_URL + ApplicationProperties.CONTEXT_PATH);
        templateModel.put("token", tokenUrl);
        return mailService.sendEmail(email, resourceBundle.getString(subject), templateModel, template);
    }

    /**
     * Checks, whether the given file is a valid CSV file. If the file is empty or is not a CSV file, a corresponding
     * error message is added to the given model to be displayed to the user.
     *
     * @param file The file to be checked.
     * @param model The {@link Model} used to store error messages.
     * @param resourceBundle The {@link ResourceBundle} used to display error messages in the desired language.
     * @return {@code true} if the file is invalid or {@code false} otherwise.
     */
    private boolean isInvalidFile(final MultipartFile file, final Model model, final ResourceBundle resourceBundle) {
        if (file.isEmpty()) {
            LOGGER.error("Cannot upload empty CSV file!");
            model.addAttribute(ERROR, resourceBundle.getString("file_empty"));
            return true;
        } else if (file.getContentType() == null || !file.getContentType().equals("text/csv")) {
            LOGGER.error("Cannot upload file with invalid content type " + file.getContentType() + "!");
            model.addAttribute(ERROR, resourceBundle.getString("file_type"));
            return true;
        } else if (file.getOriginalFilename() == null || !file.getOriginalFilename().endsWith(".csv")) {
            LOGGER.error("Cannot upload file with invalid filename " + file.getOriginalFilename() + "!");
            model.addAttribute(ERROR, resourceBundle.getString("csv_file_name"));
            return true;
        }

        return false;
    }

    /**
     * Checks, whether the given list of DTOs contains valid usernames and emails. If the size of the list is more than
     * the maximum allowed size, it is considered invalid as well.
     *
     * @param users The list of users.
     * @param model The {@link Model} used to store error information.
     * @param resourceBundle The {@link ResourceBundle} used to display error messages in the desired language.
     * @return {@code true} if all user information is valid, or {@code false} otherwise.
     */
    private boolean isValidUserInfo(final List<UserDTO> users, final Model model, final ResourceBundle resourceBundle) {
        List<String> invalidAttributes = new ArrayList<>();
        List<String> existingAttributes = new ArrayList<>();

        if (users.size() > Constants.MAX_ADD_PARTICIPANTS) {
            LOGGER.error("Cannot add an invalid number of participants " + users.size() + " from CSV!");
            model.addAttribute(ERROR, resourceBundle.getString("max_participants"));
            return false;
        }

        users.forEach(userDTO -> checkValidUserInfo(userDTO, invalidAttributes, existingAttributes));

        if (!invalidAttributes.isEmpty()) {
            LOGGER.error("Cannot create users from CSV with invalid usernames or emails!");
            model.addAttribute(ERROR, resourceBundle.getString("invalid_attributes") + " " + invalidAttributes);
            return false;
        } else if (!existingAttributes.isEmpty()) {
            LOGGER.error("Cannot create users from CSV with existing usernames or emails!");
            model.addAttribute(ERROR, resourceBundle.getString("existing_attributes") + " " + existingAttributes);
            return false;
        }

        return true;
    }

    /**
     * Checks, if the username and email address of the given user meet the requirements and cannot be found in the
     * database.
     *
     * @param userDTO The DTO containing the information to check.
     * @param invalid A list used to store all invalid usernames and emails.
     * @param existing A list used to store all usernames and emails that already exist.
     */
    private void checkValidUserInfo(final UserDTO userDTO, final List<String> invalid, final List<String> existing) {
        userDTO.setRole(Role.PARTICIPANT);

        if (UsernameValidator.validate(userDTO.getUsername()) != null) {
            invalid.add(userDTO.getUsername());
        } else if (userService.existsUser(userDTO.getUsername())) {
            existing.add(userDTO.getUsername());
        }
        if (userDTO.getEmail() != null) {
            if (EmailValidator.validate(userDTO.getEmail()) != null) {
                invalid.add(userDTO.getEmail());
            } else if (userService.existsEmail(userDTO.getEmail())) {
                existing.add(userDTO.getEmail());
            }
        }
    }

    /**
     * Sets all the required attributes for user information retrieved from a CSV file to subsequently be persisted.
     * This includes the generation of a new password for the user, which is then appended to the given string builder
     * to be returned later.
     *
     * @param userDTO The user to be added.
     * @param random Instance used to generating a random number for the password length.
     * @param builder The {@link StringBuilder} used to store the information.
     */
    private void completeUserInformation(final UserDTO userDTO, final Random random, final StringBuilder builder) {
        int randomLength = random.nextInt(Constants.PASSWORD_MIN * 2 - Constants.PASSWORD_MIN) + Constants.PASSWORD_MIN;
        String password = CustomPasswordGenerator.generatePassword(randomLength);
        userDTO.setPassword(userService.encodePassword(password));
        userDTO.setConfirmPassword(password);
        userDTO.setActive(true);
        userDTO.setLastLogin(LocalDateTime.now());
        builder.append(userDTO.getUsername()).append(", ").append(userDTO.getConfirmPassword()).append(
                System.lineSeparator());
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
    private Locale getLocaleFromLanguage(final Language language) {
        if (language == Language.GERMAN) {
            return Locale.GERMAN;
        }
        return Locale.ENGLISH;
    }

}
