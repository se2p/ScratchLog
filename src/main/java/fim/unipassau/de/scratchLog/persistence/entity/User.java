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

package fim.unipassau.de.scratchLog.persistence.entity;

import fim.unipassau.de.scratchLog.util.enums.Language;
import fim.unipassau.de.scratchLog.util.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * An entity representing a user.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "`user`")
public class User {

    /**
     * The user's unique ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The user's unique username.
     */
    @Column(unique = true, name = "username")
    private String username;

    /**
     * The user's email.
     */
    @Column(name = "email")
    private String email;

    /**
     * The user's role.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    /**
     * The user's preferred language.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "language")
    private Language language;

    /**
     * The user's hashed password.
     */
    @Column(name = "password")
    private String password;

    /**
     * The secret used to identify the user.
     */
    @Column(name = "secret")
    private String secret;

    /**
     * The number of current login attempts for the user.
     */
    @Column(name = "attempts")
    private int attempts;

    /**
     * Boolean indicating whether the user is active and thus able to use certain functions of the application.
     */
    @Column(name = "active")
    private boolean active;

    /**
     * The last time the user logged in to use the application.
     */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    /**
     * Constructs a new user with the given attributes.
     *
     * @param username The user's username.
     * @param email The user's email.
     * @param role The user's role.
     * @param language The user's preferred language.
     * @param password The user's hashed password.
     * @param secret The user's secret.
     */
    public User(final String username, final String email, final Role role, final Language language,
                final String password, final String secret) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.language = language;
        this.password = password;
        this.secret = secret;
    }

}
