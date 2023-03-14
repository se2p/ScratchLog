package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Token;
import fim.unipassau.de.scratch1984.util.enums.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, String> {

    /**
     * Returns the token identified by the given value, if one exists.
     *
     * @param value The value to search for.
     * @return The token data or {@code null}, if no token could be found.
     */
    Optional<Token> findByValue(String value);

    /**
     * Deletes all expired tokens from the database.
     *
     * @param date The current date datetime.
     */
    void deleteAllByDateBefore(LocalDateTime date);

    /**
     * Returns a list of all tokens with an expiration date prior to the given value and with the given type.
     *
     * @param date The expiration date to match.
     * @param type The token type to search for.
     * @return A list of tokens matching the specified criteria.
     */
    List<Token> findAllByDateBeforeAndType(LocalDateTime date, TokenType type);

}
