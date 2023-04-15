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

package fim.unipassau.de.scratchLog.web.dto;

import fim.unipassau.de.scratchLog.util.enums.Language;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * A DTO that represents adding multiple participants at once.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserBulkDTO {

    /**
     * The number of participants to add.
     */
    private int amount;

    /**
     * One of the available languages.
     */
    private Language language;

    /**
     * The username pattern.
     */
    private String username;

    /**
     * The boolean indicating whether the usernames should be numbered starting with one. Alternatively, the currently
     * highest user ID found in the database will be used.
     */
    private boolean startAtOne;

    /**
     * Constructs a new user bulk DTO with the given attributes.
     *
     * @param amount The number of participants to add.
     * @param language The preferred language.
     * @param username The username pattern.
     * @param startAtOne Whether the username numbering should start at one.
     */
    public UserBulkDTO(final int amount, final Language language, final String username, final boolean startAtOne) {
        this.amount = amount;
        this.language = language;
        this.username = username;
        this.startAtOne = startAtOne;
    }

    /**
     * Indicates whether some {@code other} user bulk DTO is semantically equal to this user bulk DTO.
     *
     * @param other The object to compare this user bulk DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent user bulk DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        UserBulkDTO that = (UserBulkDTO) other;
        return amount == that.amount
                && startAtOne == that.startAtOne
                && language == that.language
                && username.equals(that.username);
    }

    /**
     * Calculates a hash code for this user bulk DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the user bulk DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(amount, language, username, startAtOne);
    }

}
