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
import fim.unipassau.de.scratchLog.util.enums.LibraryResource;
import fim.unipassau.de.scratchLog.util.enums.ResourceEventSpecific;
import fim.unipassau.de.scratchLog.util.enums.ResourceEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A DTO representing a resource event that resulted from a user adding, renaming or deleting a resource in the Scratch
 * GUI.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceEventDTO implements EventDTO {

    /**
     * The unique ID of the resource event.
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
     * The local date time at which the block interaction occurred in the Scratch GUI.
     */
    @JsonProperty("time")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime date;

    /**
     * The type of resource event that occurred.
     */
    @JsonProperty("type")
    private ResourceEventType eventType;

    /**
     * The specific event that occurred.
     */
    private ResourceEventSpecific event;

    /**
     * The name of the resource.
     */
    private String name;

    /**
     * The md5 hash value of the resource.
     */
    private String md5;

    /**
     * The filetype of the resource.
     */
    @JsonProperty("dataFormat")
    private String filetype;

    /**
     * Whether the file is from the Scratch library or unknown.
     */
    private LibraryResource libraryResource;

    /**
     * Constructs a new resource event dto with the given attributes.
     *
     * @param user The id of the user who caused the event.
     * @param experiment The id of the experiment during which the event occurred.
     * @param date The time at which the event occurred.
     * @param eventType The type of event.
     * @param event The specific event.
     * @param name The name of the resource.
     * @param md5 The md5 hash of the resource.
     * @param filetype The filetype of the resource.
     * @param libraryResource Whether or not the resource is external.
     */
    public ResourceEventDTO(final Integer user, final Integer experiment, final LocalDateTime date,
                            final ResourceEventType eventType, final ResourceEventSpecific event, final String name,
                            final String md5, final String filetype, final LibraryResource libraryResource) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.eventType = eventType;
        this.event = event;
        this.name = name;
        this.md5 = md5;
        this.filetype = filetype;
        this.libraryResource = libraryResource;
    }

    /**
     * Indicates whether some {@code other} resource event DTO is semantically equal to this resource event DTO.
     *
     * @param other The object to compare this resource event DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent resource event DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ResourceEventDTO that = (ResourceEventDTO) other;
        return Objects.equals(id, that.id);
    }

    /**
     * Calculates a hash code for this resource event DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the resource event DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
