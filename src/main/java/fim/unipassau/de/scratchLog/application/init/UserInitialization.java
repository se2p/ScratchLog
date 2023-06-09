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

package fim.unipassau.de.scratchLog.application.init;

import fim.unipassau.de.scratchLog.persistence.entity.User;
import fim.unipassau.de.scratchLog.persistence.repository.UserRepository;
import fim.unipassau.de.scratchLog.util.enums.Language;
import fim.unipassau.de.scratchLog.util.enums.Role;
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
     * Constructs a new user initialization with the given dependencies.
     *
     * @param userRepository The user repository to use.
     * @param passwordEncoder The password encoder to use.
     */
    @Autowired
    public UserInitialization(final UserRepository userRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Searches for existing administrators in the database and adds a new administrator with the given values, if
     * none exist.
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
            user.setLanguage(Language.ENGLISH);
            user.setPassword(passwordEncoder.encode("!ISeeYou!"));
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            logger.info("User admin:admin was added to the database as a first administrator.");
        }
    }

}
