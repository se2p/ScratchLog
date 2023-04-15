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
 * Utility class for the composite key of {@link EventCount} as per JPA specification.
 */
public class EventCountId implements Serializable {

    /**
     * The user ID.
     */
    private Integer user;

    /**
     * The experiment ID.
     */
    private Integer experiment;

    /**
     * The event for which its occurrences have been counted.
     */
    private String event;

    /**
     * Default constructor for the ID.
     */
    public EventCountId() {
    }

    /**
     * Constructs a new event count ID with the given user and experiment IDs as well as the given event.
     *
     * @param user The user ID.
     * @param experiment The experiment ID.
     * @param event The event.
     */
    public EventCountId(final Integer user, final Integer experiment, final String event) {
        this.user = user;
        this.experiment = experiment;
        this.event = event;
    }

    /**
     * Indicates whether some {@code other} event count id is semantically equal to this id.
     *
     * @param other The object to compare this id to.
     * @return {@code true} iff {@code other} is a semantically equivalent event count id.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        EventCountId that = (EventCountId) other;
        return user.equals(that.user) && experiment.equals(that.experiment) && event.equals(that.event);
    }

    /**
     * Calculates a hash code for the event count id for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the event count id.
     */
    @Override
    public int hashCode() {
        return Objects.hash(user, experiment, event);
    }

}
