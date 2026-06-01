/*
 * This file is part of ScratchLog.
 * Licenced under the GPL v3.0 or later.
 *
 * SPDX-FileCopyrightText: 2021-2026 Scratchlog contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.uni_passau.fim.se2.scratchlog.application.service;

import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Token;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.TokenRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import de.uni_passau.fim.se2.scratchlog.util.enums.TokenType;
import de.uni_passau.fim.se2.scratchlog.web.dto.TokenDTO;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * A service providing methods related to tokens.
 */
@Service
public class TokenService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    /**
     * The user repository to use for database queries related to user data.
     */
    private final UserRepository userRepository;

    /**
     * The token repository to use for database queries related to token data.
     */
    private final TokenRepository tokenRepository;

    /**
     * The time in hours until an email token expires.
     */
    private static final int EMAIL_TOKEN_EXPIRES = 1;

    /**
     * The time in hours until a forgot password token expires.
     */
    private static final int PASSWORD_TOKEN_EXPIRES = 1;

    /**
     * The time in hours until a deactivated token expires.
     */
    private static final int DEACTIVATED_TOKEN_EXPIRES = 1;

    /**
     * The time in days until a registration token expires.
     */
    private static final int REGISTER_TOKEN_EXPIRES = 1;

    /**
     * Constructs a token service with the given dependencies.
     *
     * @param userRepository The user repository to use.
     * @param tokenRepository The token repository to use.
     */
    @Autowired
    public TokenService(final UserRepository userRepository, final TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    /**
     * Creates a new token with the given parameters.
     *
     * @param type The {@link TokenType} of the token.
     * @param metadata Optional metadata for the token.
     * @param userId The user for whom this token is to be created.
     * @return The newly created token, if the information was persisted.
     * @throws IllegalArgumentException if the passed token type is null or the user id is invalid.
     * @throws NotFoundException if no corresponding user could be found.
     */
    @Transactional
    public TokenDTO generateToken(final TokenType type, final String metadata, final int userId) {
        if (type == null) {
            throw new IllegalArgumentException("Cannot generate token with type null " + "!");
        }

        TokenDTO tokenDTO = new TokenDTO(type, computeExpirationDate(type), metadata, userId);
        User user = userRepository.getReferenceById(tokenDTO.getUser());
        Token token = createToken(tokenDTO);

        try {
            token.setUser(user);
            token = tokenRepository.save(token);
        } catch (EntityNotFoundException e) {
            log.error("Could not find user with id {}!", tokenDTO.getUser(), e);
            throw new NotFoundException("Could not find user with id " + tokenDTO.getUser() + "!", e);
        }

        return createTokenDTO(token);
    }

    /**
     * Returns the {@link TokenDTO} with the given value.
     *
     * @param value The token value to search for.
     * @return The token dto.
     * @throws IllegalArgumentException if the passed value is null or blank.
     * @throws NotFoundException if no corresponding token could be found.
     */
    public TokenDTO findToken(final String value) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException("Cannot search for token with null or empty value!");
        }

        Optional<Token> token = tokenRepository.findByValue(value);

        if (token.isEmpty()) {
            log.error("Could not find token with value {} in the database!", value);
            throw new NotFoundException("Could not find token with value " + value + " in the database!");
        }

        return createTokenDTO(token.get());
    }

    /**
     * Deletes the token with the given value from the database.
     *
     * @param value The token value to search for.
     * @throws IllegalArgumentException if the passed value is null or blank.
     */
    @Transactional
    public void deleteToken(final String value) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException("Cannot search for token with null or empty value!");
        }

        tokenRepository.deleteById(value);
    }

    /**
     * Deletes all expired tokens from the database.
     *
     * @param localDateTime The current {@link LocalDateTime}.
     * @throws IllegalArgumentException if the passed time is null.
     */
    @Transactional
    public void deleteExpiredTokens(final LocalDateTime localDateTime) {
        if (localDateTime == null) {
            throw new IllegalArgumentException("Cannot delete expired tokens with timestamp null!");
        }

        tokenRepository.deleteAllByDateBefore(localDateTime);
    }

    /**
     * Deletes the user accounts whose registration tokens have expired.
     *
     * @param localDateTime The current {@link LocalDateTime}.
     * @throws IllegalArgumentException if the passed time is null.
     * @throws IllegalStateException if the user referenced by the token does not exist.
     */
    @Transactional
    public void deleteExpiredAccounts(final LocalDateTime localDateTime) {
        if (localDateTime == null) {
            throw new IllegalArgumentException("Cannot delete expired accounts with timestamp null!");
        }

        List<Token> expiredRegistrations = tokenRepository.findAllByDateBeforeAndType(localDateTime,
                TokenType.REGISTER);

        for (Token token : expiredRegistrations) {
            if (token.getUser() == null) {
                throw new IllegalStateException("Cannot delete expired user account from token " + token.getValue()
                        + " with user null!");
            }

            userRepository.deleteById(token.getUser().getId());
        }
    }

    /**
     * Reactivates the user accounts whose deactivated tokens have expired.
     *
     * @param localDateTime The current {@link LocalDateTime}.
     * @throws IllegalArgumentException if the passed time is null.
     * @throws IllegalStateException if the user referenced by the token does not exist.
     * @throws NotFoundException if no corresponding user entry could be found.
     */
    @Transactional
    public void reactivateUserAccounts(final LocalDateTime localDateTime) {
        if (localDateTime == null) {
            throw new IllegalArgumentException("Cannot reactivate user accounts with timestamp null!");
        }

        List<Token> deactivatedAccounts = tokenRepository.findAllByDateBeforeAndType(localDateTime,
                TokenType.DEACTIVATED);

        for (Token token : deactivatedAccounts) {
            if (token.getUser() == null || token.getUser().getId() == null) {
                throw new IllegalStateException("Cannot reactivate user from token " + token.getValue()
                        + " with user null!");
            }

            User user = userRepository.getReferenceById(token.getUser().getId());

            try {
                user.setAttempts(0);
                user.setActive(true);
                userRepository.save(user);
            } catch (EntityNotFoundException e) {
                log.error("Cannot reactivate user account for user with id {}!", user.getId(), e);
                throw new NotFoundException("Cannot reactivate user account for user with id " + user.getId() + "!", e);
            }
        }
    }

    /**
     * Create a token for the given admin that indicates that they have no traditional stored password, but should
     * instead log in with a random password that is generated and printed upon app startup.
     *
     * @param userId The id of the admin account to create the token for.
     * @throws IllegalArgumentException If the given user is not an admin.
     * @throws IllegalStateException If the given user already has such a token associated with them.
     * @throws NotFoundException If the user can not be found in the database.
     */
    @Transactional
    public void createRandomPasswordToken(final int userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException("Cannot create random password for non-admin user " + userId + ".");
            }

            generateToken(TokenType.ADMIN_WITH_RANDOM_PASSWORD, "", userId);
        } else {
            log.error("Could not find user with id {} in the database.", userId);
            throw new NotFoundException("Could not find user with id " + userId + " in the database.");
        }
    }

    /**
     * Return whether the given admin uses a random password generated at app startup to log in, by checking whether
     * they have that token associated with them.
     *
     * @param userId The id of the admin account to check the token for.
     * @return Whether the given admin uses the random password login method.
     * @throws IllegalArgumentException If the given user is not an admin.
     * @throws IllegalStateException If the given user has more than one token of this kind.
     * @throws NotFoundException If the user can not be found in the database.
     */
    @Transactional
    public boolean checkRandomPasswordToken(final int userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException("Cannot check for random password token for non-admin user with id "
                    + userId + ".");
            }

            List<Token> tokens = tokenRepository.findAllByTypeAndUser(TokenType.ADMIN_WITH_RANDOM_PASSWORD, user);
            if (tokens.size() > 1) {
                throw new IllegalStateException("More than one random password token exist for user " + userId + ".");
            } else {
                return !tokens.isEmpty();
            }
        } else {
            log.error("Could not find user with id {} in the database.", userId);
            throw new NotFoundException("Could not find user with id " + userId + " in the database.");
        }
    }

    /**
     * Delete the random password tokens for the given admin, if they exist.
     *
     * @param userId The id of the admin account to delete the tokens for.
     * @throws IllegalArgumentException If the given user is not an admin.
     * @throws NotFoundException If the user can not be found in the database.
     */
    @Transactional
    public void deleteRandomPasswordToken(final int userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException("Cannot delete random password token for non-admin user with id "
                    + userId + ".");
            }
            List<Token> tokens = tokenRepository.findAllByTypeAndUser(TokenType.ADMIN_WITH_RANDOM_PASSWORD, user);
            tokenRepository.deleteAllInBatch(tokens);
        } else {
            log.error("Could not find user with id {} in the database.", userId);
            throw new NotFoundException("Could not find user with id " + userId + " in the database.");
        }
    }

    /**
     * Returns the {@link LocalDateTime} expiration date for a token with the given type.
     *
     * @param type The {@link TokenType}.
     * @return The computed expiration date.
     */
    private LocalDateTime computeExpirationDate(final TokenType type) {
        LocalDateTime dateTime = LocalDateTime.now();

        return switch (type) {
            case CHANGE_EMAIL -> dateTime.plusHours(EMAIL_TOKEN_EXPIRES);
            case FORGOT_PASSWORD -> dateTime.plusHours(PASSWORD_TOKEN_EXPIRES);
            case DEACTIVATED -> dateTime.plusHours(DEACTIVATED_TOKEN_EXPIRES);
            case REGISTER -> dateTime.plusDays(REGISTER_TOKEN_EXPIRES);
            case ADMIN_WITH_RANDOM_PASSWORD -> Constants.MAX_DATETIME;                  // Make this token never expire.
        };
    }

    /**
     * Creates a {@link Token} with the given information of the {@link TokenDTO}.
     *
     * @param tokenDTO The DTO containing the information.
     * @return The new token containing the information passed in the DTO.
     */
    private Token createToken(final TokenDTO tokenDTO) {
        Token token = Token.builder()
                .type(tokenDTO.getType())
                .date(tokenDTO.getExpirationDate())
                .build();

        if (tokenDTO.getValue() != null) {
            token.setValue(tokenDTO.getValue());
        }
        if (tokenDTO.getMetadata() != null) {
            token.setMetadata(tokenDTO.getMetadata());
        }

        return token;
    }

    /**
     * Creates a {@link TokenDTO} with the given information from the {@link Token}.
     *
     * @param token The token object containing the information.
     * @return The new token DTO containing the information passed in the token object.
     */
    private TokenDTO createTokenDTO(final Token token) {
        TokenDTO tokenDTO = TokenDTO.builder()
                .type(token.getType())
                .expirationDate(token.getDate())
                .user(token.getUser().getId())
                .build();

        if (token.getValue() != null) {
            tokenDTO.setValue(token.getValue());
        }
        if (token.getMetadata() != null) {
            tokenDTO.setMetadata(token.getMetadata());
        }

        return tokenDTO;
    }

}
