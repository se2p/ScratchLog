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

import java.time.LocalDateTime;

/**
 * An interface for events logged by the instrumented Scratch IDE.
 */
public interface EventDTO {

    /**
     * Returns the ID of the event.
     *
     * @return The event ID.
     */
    Integer getId();

    /**
     * Sets the ID of the event.
     *
     * @param id The event ID to be set.
     */
    void setId(Integer id);

    /**
     * Returns the ID of the user who caused the event.
     *
     * @return The user's ID.
     */
    Integer getUser();

    /**
     * Sets the user ID of the event.
     *
     * @param user The user ID to be set.
     */
    void setUser(Integer user);

    /**
     * Returns the ID of the experiment where the event occurred.
     *
     * @return The experiment ID.
     */
    Integer getExperiment();

    /**
     * Sets the experiment ID of the event.
     *
     * @param experiment The experiment ID to be set.
     */
    void setExperiment(Integer experiment);

    /**
     * Returns the time at which the event occurred.
     *
     * @return The event time.
     */
    LocalDateTime getDate();

    /**
     * Sets the local date time of the event.
     *
     * @param date The time to be set.
     */
    void setDate(LocalDateTime date);

    /**
     * Converts the event DTO into a human-readable string representation.
     *
     * @return A human-readable string representation of the event DTO.
     */
    String toString();

}
