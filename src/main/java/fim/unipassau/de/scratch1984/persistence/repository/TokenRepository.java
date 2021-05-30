package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, String> {

    /**
     * Returns the token identified by the given value, if one exists.
     *
     * @param value The value to search for.
     * @return The token data or {@code null}, if no token could be found.
     */
    Token findByValue(String value);

}
