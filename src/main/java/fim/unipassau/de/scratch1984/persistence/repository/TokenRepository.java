package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;

public interface TokenRepository extends JpaRepository<Token, String> {

    /**
     * Returns the token identified by the given value, if one exists.
     *
     * @param value The value to search for.
     * @return The token data or {@code null}, if no token could be found.
     */
    Token findByValue(String value);

    /**
     * Deletes all expired tokens from the database.
     *
     * @param date The current date timestamp.
     */
    void deleteAllByDateBefore(Timestamp date);

}
