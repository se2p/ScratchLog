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

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A DTO representing an experiment.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
public class CourseDTO {

    /**
     * The unique ID of the course.
     */
    private Integer id;

    /**
     * The unique title of the course.
     */
    private String title;

    /**
     * The short description text of the course.
     */
    private String description;

    /**
     * The content text containing further information about the course.
     */
    private String content;

    /**
     * Boolean indicating whether the course is currently being conducted or not.
     */
    private boolean active;

    /**
     * The time at which the course information was last updated or an experiment or participant added.
     */
    private LocalDateTime lastChanged;

    /**
     * Constructs a new course dto with the given attributes.
     *
     * @param id The course id.
     * @param title The course title.
     * @param description The course description.
     * @param content The course content text.
     * @param active Whether the course is currently being conducted or not.
     * @param lastChanged The last time at which the course was updated.
     */
    public CourseDTO(final Integer id, final String title, final String description, final String content,
                     final boolean active, final LocalDateTime lastChanged) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.active = active;
        this.lastChanged = lastChanged;
    }

    /**
     * Indicates whether some {@code other} course DTO is semantically equal to this course DTO.
     *
     * @param other The object to compare this course DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent course DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        CourseDTO that = (CourseDTO) other;
        return Objects.equals(id, that.id);
    }

    /**
     * Calculates a hash code for this course DTO for hashing purposes, and to fulfill the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of the course DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
