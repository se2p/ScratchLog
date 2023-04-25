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

import java.io.Serializable;
import java.util.Objects;

/**
 * Utility class for the composite key of {@link CourseParticipant} as per JPA specification.
 */
public class CourseParticipantId implements Serializable {

    /**
     * The id of the participating user.
     */
    private int user;

    /**
     * The id of the course in which the user participates.
     */
    private int course;

    /**
     * Default constructor for the ID.
     */
    public CourseParticipantId() {
    }

    /**
     * Constructs a new course participant ID with the given user and course IDs.
     *
     * @param user The participating user's ID.
     * @param course The respective course ID.
     */
    public CourseParticipantId(final int user, final int course) {
        this.user = user;
        this.course = course;
    }

    /**
     * Indicates whether some {@code other} course participant id is semantically equal to this id.
     *
     * @param other The object to compare this id to.
     * @return {@code true} iff {@code other} is a semantically equivalent course participant id.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        CourseParticipantId that = (CourseParticipantId) other;
        return user == that.user && course == that.course;
    }

    /**
     * Calculates a hash code for the course participant id for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the course participant id.
     */
    @Override
    public int hashCode() {
        return Objects.hash(user, course);
    }

}
