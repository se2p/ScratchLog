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

import java.time.LocalDateTime;

/**
 * An interface for event entities logged by the instrumented Scratch IDE.
 */
public interface Event {

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
     * Returns the user of the event.
     *
     * @return The respective user.
     */
    User getUser();

    /**
     * Sets the user of the event.
     *
     * @param user The event user to be set.
     */
    void setUser(User user);

    /**
     * Returns the experiment of the event.
     *
     * @return The respective experiment.
     */
    Experiment getExperiment();

    /**
     * Sets the experiment of the event.
     *
     * @param experiment The event experiment to be set.
     */
    void setExperiment(Experiment experiment);

    /**
     * Returns the datetime of the event.
     *
     * @return The respective timestamp.
     */
    LocalDateTime getDate();

    /**
     * Sets the datetime of the event.
     *
     * @param date The event timestamp to be set.
     */
    void setDate(LocalDateTime date);

}
