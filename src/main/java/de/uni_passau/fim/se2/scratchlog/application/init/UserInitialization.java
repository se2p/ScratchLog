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

package de.uni_passau.fim.se2.scratchlog.application.init;

import de.uni_passau.fim.se2.scratchlog.application.service.TokenService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.util.CustomPasswordGenerator;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Adds a first administrator on application startup if none can be found in the database.
 */
@Component
public class UserInitialization {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(UserInitialization.class);

    /**
     * The user repository to use for database queries related to user data.
     */
    private final UserRepository userRepository;

    /**
     * The password encoder to use for hashing passwords.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * The token service to use for storing information about admins who have no set password.
     */
    private final TokenService tokenService;

    /**
     * Constructs a new user initialization with the given dependencies.
     *
     * @param userRepository The user repository to use.
     * @param passwordEncoder The password encoder to use.
     * @param tokenService The token service to use.
     */
    @Autowired
    public UserInitialization(final UserRepository userRepository, final PasswordEncoder passwordEncoder,
                              final TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    /**
     * Searches for existing administrators in the database and adds a new administrator with the given values, if
     * none exist. This new admin will receive a randomly generated password that is printed to console for logging in.
     * In future app starts, admins that have yet to set their own password will continue to receive new random
     * passwords.
     */
    @PostConstruct
    public void init() {
        List<User> users = userRepository.findAllByRole(Role.ADMIN);
        if (users == null || users.isEmpty()) {
            User user = new User();
            user.setActive(true);
            user.setEmail("admin@admin.de");
            user.setUsername("admin");
            user.setRole(Role.ADMIN);
            user.setLanguage(Constants.DEFAULT_LANGUAGE);

            // Store the random password in the database, even though it will reset at the next startup, so that
            // existing login logic can stay mostly unchanged.
            String password = CustomPasswordGenerator.generatePassword(Constants.RANDOM_PASSWORD_LENGTH);
            user.setPassword(passwordEncoder.encode(password));

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            tokenService.createRandomPasswordToken(user.getId());
            logger.info("********************");
            logger.info("User \"admin\" was added to the database as a first administrator with password {}", password);
            logger.info("********************");
        } else {
            for (User user : users) {
                if (tokenService.checkRandomPasswordToken(user.getId())) {
                    String password = CustomPasswordGenerator.generatePassword(Constants.RANDOM_PASSWORD_LENGTH);
                    user.setPassword(passwordEncoder.encode(password));
                    userRepository.save(user);
                    logger.info("********************");
                    logger.info("Random password for admin with id {} this session is: {}", user.getId(), password);
                    logger.info("********************");
                }
            }
        }
    }

}
