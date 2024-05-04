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

package de.uni_passau.fim.se2.scratchlog.application.service;

import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.exception.StoreException;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Token;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.TokenRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenService.class);

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
     * The time in years until a default password token expires.
     */
    private static final int DEFAULT_PASSWORD_TOKEN_EXPIRES = 10;

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
     * @throws StoreException if the created token could not be persisted.
     */
    @Transactional
    public TokenDTO generateToken(final TokenType type, final String metadata, final int userId) {
        if (userId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot generate token with invalid user id " + userId + "!");
        } else if (type == null) {
            throw new IllegalArgumentException("Cannot generate token with type null " + "!");
        }

        TokenDTO tokenDTO = new TokenDTO(type, computeExpirationDate(type), metadata, userId);
        User user = userRepository.getReferenceById(tokenDTO.getUser());
        Token token = createToken(tokenDTO);

        try {
            token.setUser(user);
            token = tokenRepository.save(token);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find user with id " + tokenDTO.getUser() + "!", e);
            throw new NotFoundException("Could not find user with id " + tokenDTO.getUser() + "!", e);
        }

        if (token.getValue() == null) {
            throw new StoreException("Failed to store token for user with id " + tokenDTO.getUser() + "!");
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
    @Transactional
    public TokenDTO findToken(final String value) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException("Cannot search for token with null or empty value!");
        }

        Optional<Token> token = tokenRepository.findByValue(value);

        if (token.isEmpty()) {
            LOGGER.error("Could not find token with value " + value + " in the database!");
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
                LOGGER.error("Cannot reactivate user account for user with id " + user.getId() + "!", e);
                throw new NotFoundException("Cannot reactivate user account for user with id " + user.getId() + "!", e);
            }
        }
    }

    /**
     * Checks whether a default password token for the user with the given id exists. This token is meant to track how
     * often administrator accounts have logged in using the default admin password. The number of times the user has
     * logged in with the default password is returned. If the user tries to log in with the default password and a
     * corresponding token could be found, the number of login attempts saved in the token metadata is increased. If the
     * login attempts are larger than the allowed number, the user account is deactivated and the token deleted. If no
     * token could be found, a new token is generated for the user.
     *
     * @param userId The id of the user.
     * @param isLogin Boolean indicating whether this method is called during login, meaning that a token should be
     *                created or updated.
     * @return The number of times the user has logged in with the default password.
     * @throws IllegalArgumentException if the given user id is invalid.
     * @throws IllegalStateException if the number of default password tokens found for the user is larger than one.
     * @throws NotFoundException if no no user account with the given id could be found.
     */
    @Transactional
    public int checkDefaultPasswordToken(final int userId, final boolean isLogin) {
        if (userId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot search for default password token with invalid user id "
                    + userId + "!");
        }

        User user = userRepository.getReferenceById(userId);

        try {
            List<Token> tokens = tokenRepository.findAllByTypeAndUser(TokenType.DEFAULT_PASSWORD, user);

            if (tokens.size() > 1) {
                throw new IllegalStateException("More than one default password token exists for user with id " + userId
                        + "!");
            } else if (isLogin) {
                return generateOrUpdateToken(tokens, user);
            } else {
                return tokens.isEmpty() ? 0 : Integer.parseInt(tokens.get(0).getMetadata());
            }
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find user with id " + userId + " in the database!", e);
            throw new NotFoundException("Could not find user with id " + userId + " in the database!", e);
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

        if (type == TokenType.CHANGE_EMAIL) {
            return dateTime.plusHours(EMAIL_TOKEN_EXPIRES);
        } else if (type == TokenType.FORGOT_PASSWORD) {
            return dateTime.plusHours(PASSWORD_TOKEN_EXPIRES);
        } else if (type == TokenType.DEACTIVATED) {
            return dateTime.plusHours(DEACTIVATED_TOKEN_EXPIRES);
        } else if (type == TokenType.REGISTER) {
            return dateTime.plusDays(REGISTER_TOKEN_EXPIRES);
        } else {
            return dateTime.plusYears(DEFAULT_PASSWORD_TOKEN_EXPIRES);
        }
    }

    /**
     * Generates or updates the default password token for the given user, depending on whether such a token already
     * exists.
     *
     * @param tokens A list of default password tokens saved for the given user.
     * @param user The user for whom a token should be generated or updated.
     * @return The number of times the user has logged in with the default password.
     */
    private int generateOrUpdateToken(final List<Token> tokens, final User user) {
        if (tokens.isEmpty()) {
            generateDefaultPasswordToken(user);
            return 0;
        } else {
            return updateDefaultPasswordToken(tokens.get(0), user);
        }
    }

    /**
     * Generates a default password token for the given user to track how often this user has logged in using the
     * default admin password.
     *
     * @param user The {@link User} for whom the token should be generated.
     */
    private void generateDefaultPasswordToken(final User user) {
        TokenDTO tokenDTO = new TokenDTO(TokenType.DEFAULT_PASSWORD,
                computeExpirationDate(TokenType.DEFAULT_PASSWORD), "1", user.getId());
        Token token = createToken(tokenDTO);
        token.setUser(user);
        tokenRepository.save(token);
    }

    /**
     * Updates the default password token of the given user. If the user has logged in with the default admin password
     * more times than allowed, the account is deactivated and the token deleted. Otherwise, the login counter is
     * increased and the token updated.
     *
     * @param token The default password token to be updated.
     * @param user The user whose account is deactivated if the maximum number of login attempts is reached.
     * @return The number of times the user has logged in with the default password.
     */
    private int updateDefaultPasswordToken(final Token token, final User user) {
        int logins = Integer.parseInt(token.getMetadata());

        if (logins >= Constants.MAX_DEFAULT_ATTEMPTS) {
            user.setActive(false);
            tokenRepository.deleteById(token.getValue());
            userRepository.save(user);
            return Constants.MAX_DEFAULT_ATTEMPTS;
        } else {
            logins++;
            token.setMetadata(Integer.toString(logins));
            tokenRepository.save(token);
            return logins - 1;
        }
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
