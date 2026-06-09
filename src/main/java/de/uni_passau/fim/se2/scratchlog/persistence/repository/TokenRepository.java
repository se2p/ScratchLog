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

package de.uni_passau.fim.se2.scratchlog.persistence.repository;

import de.uni_passau.fim.se2.scratchlog.persistence.entity.Token;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.util.enums.TokenType;
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

    /**
     * Returns a list of all tokens of a specific type saved for the given user.
     *
     * @param type The token type to search for.
     * @param user The user to search for.
     * @return A list of tokens matching the specified criteria.
     */
    List<Token> findAllByTypeAndUser(TokenType type, User user);

}
