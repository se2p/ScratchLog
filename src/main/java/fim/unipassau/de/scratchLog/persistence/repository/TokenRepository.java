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

package fim.unipassau.de.scratchLog.persistence.repository;

import fim.unipassau.de.scratchLog.persistence.entity.Token;
import fim.unipassau.de.scratchLog.util.enums.TokenType;
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
