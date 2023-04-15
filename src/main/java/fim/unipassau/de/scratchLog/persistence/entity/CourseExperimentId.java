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
 * Utility class for the composite key of {@link CourseExperiment} as per JPA specification.
 */
public class CourseExperimentId implements Serializable {

    /**
     * The id of the course.
     */
    private int course;

    /**
     * The id of the experiment belonging to this course.
     */
    private int experiment;

    /**
     * Default constructor for the ID.
     */
    public CourseExperimentId() {
    }

    /**
     * Constructs a new course experiment ID with the given course and experiment IDs.
     *
     * @param course The respective course ID.
     * @param experiment The respective experiment ID.
     */
    public CourseExperimentId(final int course, final int experiment) {
        this.course = course;
        this.experiment = experiment;
    }

    /**
     * Indicates whether some {@code other} course experiment id is semantically equal to this id.
     *
     * @param other The object to compare this id to.
     * @return {@code true} iff {@code other} is a semantically equivalent course experiment id.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        CourseExperimentId that = (CourseExperimentId) other;
        return course == that.course && experiment == that.experiment;
    }

    /**
     * Calculates a hash code for the course experiment id for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the course experiment id.
     */
    @Override
    public int hashCode() {
        return Objects.hash(course, experiment);
    }

}
