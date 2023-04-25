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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import fim.unipassau.de.scratchLog.util.enums.DebuggerEventSpecific;
import fim.unipassau.de.scratchLog.util.enums.DebuggerEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A DTO representing a debugger event that resulted from user interaction with the Scratch debugger, not including
 * interactions with questions.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DebuggerEventDTO implements EventDTO {

    /**
     * The unique ID of the debugger event.
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
     * The local date time at which the debugger interaction occurred in the Scratch GUI.
     */
    @JsonProperty("time")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime date;

    /**
     * The type of debugger event that occurred.
     */
    @JsonProperty("type")
    private DebuggerEventType eventType;

    /**
     * The specific event that occurred.
     */
    private DebuggerEventSpecific event;

    /**
     * The block or target id of the debugger event.
     */
    @JsonProperty("scratchId")
    private String blockOrTargetID;

    /**
     * The target name or block opcode of the debugger event.
     */
    @JsonProperty("name")
    private String nameOrOpcode;

    /**
     * Only applicable to select sprite event, null for everything else.
     */
    private Integer original;

    /**
     * The number of the block executions of the debugger event.
     */
    private Integer execution;

    /**
     * Constructs a new debugger event dto with the given attributes.
     *
     * @param user The id of the user who caused the event.
     * @param experiment The id of the experiment during which the event occurred.
     * @param date The time at which the event occurred.
     * @param eventType The type of event.
     * @param event The specific event.
     * @param blockOrTargetID The block or target ID of the event.
     * @param nameOrOpcode The target name or block opcode of the event.
     * @param original Only applicable to the select sprite event.
     * @param execution The number of the block executions of the event.
     */
    public DebuggerEventDTO(final Integer user, final Integer experiment, final LocalDateTime date,
                            final DebuggerEventType eventType, final DebuggerEventSpecific event,
                            final String blockOrTargetID, final String nameOrOpcode, final Integer original,
                            final Integer execution) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.eventType = eventType;
        this.event = event;
        this.blockOrTargetID = blockOrTargetID;
        this.nameOrOpcode = nameOrOpcode;
        this.original = original;
        this.execution = execution;
    }

    /**
     * Indicates whether some {@code other} debugger event DTO is semantically equal to this debugger event DTO.
     *
     * @param other The object to compare this debugger event DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent debugger event DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        DebuggerEventDTO that = (DebuggerEventDTO) other;
        return Objects.equals(id, that.id);
    }

    /**
     * Calculates a hash code for this debugger event DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the debugger event DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
