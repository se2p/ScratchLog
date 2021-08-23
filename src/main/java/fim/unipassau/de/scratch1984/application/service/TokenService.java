package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.persistence.entity.Token;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.TokenRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.dto.TokenDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A service providing methods related to tokens.
 */
@Service
public class TokenService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

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
     * Creates a new token with the given parameters. If crucial information needed to store the token is missing, an
     * {@link IllegalArgumentException} is thrown instead. If no user with the given id could be found in the database,
     * a {@link EntityNotFoundException} is thrown. If the information could not be persisted correctly, a
     * {@link StoreException} is thrown instead.
     *
     * @param type The {@link fim.unipassau.de.scratch1984.web.dto.TokenDTO.Type} of the token.
     * @param metadata Optional metadata for the token.
     * @param userId The user for whom this token is to be created.
     * @return The newly created token, if the information was persisted.
     */
    @Transactional
    public TokenDTO generateToken(final TokenDTO.Type type, final String metadata, final int userId) {
        if (userId < Constants.MIN_ID) {
            logger.error("Cannot generate token with invalid user id " + userId + "!");
            throw new IllegalArgumentException("Cannot generate token with invalid user id " + userId + "!");
        } else if (type == null) {
            logger.error("Cannot generate token with type null " + "!");
            throw new IllegalArgumentException("Cannot generate token with type null " + "!");
        }

        TokenDTO tokenDTO = new TokenDTO(type, computeExpirationDate(type), metadata, userId);
        User user = userRepository.getOne(tokenDTO.getUser());
        Token token = createToken(tokenDTO);

        try {
            token.setUser(user);
            token = tokenRepository.save(token);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find user with id " + tokenDTO.getUser() + "!", e);
            throw new NotFoundException("Could not find user with id " + tokenDTO.getUser() + "!", e);
        }

        if (token.getValue() == null) {
            logger.error("Failed to store token for user with id " + tokenDTO.getUser() + "!");
            throw new StoreException("Failed to store token for user with id " + tokenDTO.getUser() + "!");
        }

        return createTokenDTO(token);
    }

    /**
     * Returns the {@link TokenDTO} with the given value. If no corresponding token exists in the database, a
     * {@link NotFoundException} is thrown instead.
     *
     * @param value The token value to search for.
     * @return The token dto.
     */
    @Transactional
    public TokenDTO findToken(final String value) {
        if (value == null || value.trim().isBlank()) {
            logger.error("Cannot search for token with null or empty value!");
            throw new IllegalArgumentException("Cannot search for token with null or empty value!");
        }

        Token token = tokenRepository.findByValue(value);

        if (token == null) {
            logger.error("Could not find token with value " + value + " in the database!");
            throw new NotFoundException("Could not find token with value " + value + " in the database!");
        }

        return createTokenDTO(token);
    }

    /**
     * Deletes the token with the given value from the database.
     *
     * @param value The token value to search for.
     */
    @Transactional
    public void deleteToken(final String value) {
        if (value == null || value.trim().isBlank()) {
            logger.error("Cannot search for token with null or empty value!");
            throw new IllegalArgumentException("Cannot search for token with null or empty value!");
        }

        tokenRepository.deleteById(value);
    }

    /**
     * Deletes all expired tokens from the database.
     *
     * @param localDateTime The current {@link LocalDateTime}.
     */
    @Transactional
    public void deleteExpiredTokens(final LocalDateTime localDateTime) {
        if (localDateTime == null) {
            logger.error("Cannot delete expired tokens with timestamp null!");
            throw new IllegalArgumentException("Cannot delete expired tokens with timestamp null!");
        }

        tokenRepository.deleteAllByDateBefore(Timestamp.valueOf(localDateTime));
    }

    /**
     * Deletes the user accounts whose registration tokens have expired.
     *
     * @param localDateTime The current {@link LocalDateTime}.
     */
    @Transactional
    public void deleteExpiredAccounts(final LocalDateTime localDateTime) {
        if (localDateTime == null) {
            logger.error("Cannot delete expired accounts with timestamp null!");
            throw new IllegalArgumentException("Cannot delete expired accounts with timestamp null!");
        }

        List<Token> expiredRegistrations = tokenRepository.findAllByDateBeforeAndType(Timestamp.valueOf(localDateTime),
                TokenDTO.Type.REGISTER.toString());

        for (Token token : expiredRegistrations) {
            if (token.getUser() == null) {
                logger.error("Cannot delete expired user account from token " + token.getValue() + " with user null!");
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
     */
    @Transactional
    public void reactivateUserAccounts(final LocalDateTime localDateTime) {
        if (localDateTime == null) {
            logger.error("Cannot reactivate user accounts with timestamp null!");
            throw new IllegalArgumentException("Cannot reactivate user accounts with timestamp null!");
        }

        List<Token> deactivatedAccounts = tokenRepository.findAllByDateBeforeAndType(Timestamp.valueOf(localDateTime),
                TokenDTO.Type.DEACTIVATED.toString());

        for (Token token : deactivatedAccounts) {
            if (token.getUser() == null || token.getUser().getId() == null) {
                logger.error("Cannot reactivate user account from token " + token.getValue() + " with user null!");
                throw new IllegalStateException("Cannot reactivate user from token " + token.getValue()
                        + " with user null!");
            }

            User user = userRepository.getOne(token.getUser().getId());

            try {
                user.setAttempts(0);
                user.setActive(true);
                userRepository.save(user);
            } catch (EntityNotFoundException e) {
                logger.error("Cannot reactivate user account for user with id " + user.getId() + "!", e);
                throw new NotFoundException("Cannot reactivate user account for user with id " + user.getId() + "!", e);
            }
        }
    }

    /**
     * Returns the {@link LocalDateTime} expiration date for a token with the given type.
     *
     * @param type The {@link fim.unipassau.de.scratch1984.web.dto.TokenDTO.Type}.
     * @return The computed expiration date.
     */
    private LocalDateTime computeExpirationDate(final TokenDTO.Type type) {
        LocalDateTime dateTime = LocalDateTime.now();

        if (type == TokenDTO.Type.CHANGE_EMAIL) {
            return dateTime.plusHours(EMAIL_TOKEN_EXPIRES);
        } else if (type == TokenDTO.Type.FORGOT_PASSWORD) {
            return dateTime.plusHours(PASSWORD_TOKEN_EXPIRES);
        } else if (type == TokenDTO.Type.DEACTIVATED) {
            return dateTime.plusHours(DEACTIVATED_TOKEN_EXPIRES);
        } else {
            return dateTime.plusDays(REGISTER_TOKEN_EXPIRES);
        }
    }

    /**
     * Creates a {@link Token} with the given information of the {@link TokenDTO}.
     *
     * @param tokenDTO The DTO containing the information.
     * @return The new token containing the information passed in the DTO.
     */
    private Token createToken(final TokenDTO tokenDTO) {
        Token token = new Token();

        if (tokenDTO.getValue() != null) {
            token.setValue(tokenDTO.getValue());
        }
        if (tokenDTO.getType() != null) {
            token.setType(tokenDTO.getType().toString());
        }
        if (tokenDTO.getExpirationDate() != null) {
            token.setDate(Timestamp.valueOf(tokenDTO.getExpirationDate()));
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
        TokenDTO tokenDTO = new TokenDTO();

        if (token.getValue() != null) {
            tokenDTO.setValue(token.getValue());
        }
        if (token.getType() != null) {
            tokenDTO.setType(TokenDTO.Type.valueOf(token.getType()));
        }
        if (token.getDate() != null) {
            tokenDTO.setExpirationDate(token.getDate().toLocalDateTime());
        }
        if (token.getMetadata() != null) {
            tokenDTO.setMetadata(token.getMetadata());
        }
        if ((token.getUser() != null) && (token.getUser().getId() != null)) {
            tokenDTO.setUser(token.getUser().getId());
        }

        return tokenDTO;
    }

}