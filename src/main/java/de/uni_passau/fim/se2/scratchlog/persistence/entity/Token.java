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

package de.uni_passau.fim.se2.scratchlog.persistence.entity;

import de.uni_passau.fim.se2.scratchlog.util.enums.TokenType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * An entity representing a token.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Token {

    /**
     * The unique value of the token.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "value")
    private String value;

    /**
     * The type of the token.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TokenType type;

    /**
     * The expiration date of the token.
     */
    @Column(name = "expiration")
    private LocalDateTime date;

    /**
     * Any additional information related to this token.
     */
    @Column(name = "metadata")
    private String metadata;

    /**
     * The {@link User} to whom this token belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Constructs a new token with the given attributes.
     *
     * @param type The type of the token.
     * @param date The expiration datetime of the token.
     * @param metadata Optional metadata for the token.
     * @param user The user to whom this token belongs.
     */
    public Token(final TokenType type, final LocalDateTime date, final String metadata, final User user) {
        this.type = type;
        this.date = date;
        this.metadata = metadata;
        this.user = user;
    }

}
