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

package de.uni_passau.fim.se2.scratchlog.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A DTO representing a click event that resulted from user interaction with a button, icon, or similar event.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClickEventDTO implements EventDTO {

    /**
     * The unique ID of the click event.
     */
    private Integer id;

    /**
     * The ID of the user who caused the event.
     */
    private Integer user;

    /**
     * The ID of the experiment during which the event occurred.
     */
    private Integer experiment;

    /**
     * The local date time at which the click interaction occurred in the Scratch GUI.
     */
    @JsonProperty("time")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime date;

    /**
     * The type of click event that occurred.
     */
    @JsonProperty("type")
    private ClickEventType eventType;

    /**
     * The specific event that occurred.
     */
    private ClickEventSpecific event;

    /**
     * Additional information about the event.
     */
    private String metadata;

    /**
     * A secret that can be used to check the event is associated with the correct participant.
     */
    @JsonProperty("secret")
    private String secret;

    /**
     * Constructs a new click event dto with the given attributes.
     *
     * @param user       The id of the user who caused the event.
     * @param experiment The id of the experiment during which the event occurred.
     * @param secret     The secret used to identify the participant.
     * @param eventType  The type of event.
     * @param event      The specific event.
     * @param metadata   The metadata.
     * @param date       The time at which the event occurred.
     */
    public ClickEventDTO(final Integer user, final Integer experiment, final String secret,
                         final ClickEventType eventType, final ClickEventSpecific event, final String metadata,
                         final LocalDateTime date) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.eventType = eventType;
        this.event = event;
        this.metadata = metadata;
        this.secret = secret;
    }

    /**
     * Indicates whether some {@code other} click event DTO is semantically equal to this click event DTO.
     *
     * @param other The object to compare this click event DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent click event DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ClickEventDTO that = (ClickEventDTO) other;
        return Objects.equals(id, that.id);
    }

    /**
     * Calculates a hash code for this click event DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the click event DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
