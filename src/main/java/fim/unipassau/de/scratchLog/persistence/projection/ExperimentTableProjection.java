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

package fim.unipassau.de.scratchLog.persistence.projection;

/**
 * Projection interface for the {@link fim.unipassau.de.scratchLog.persistence.entity.Experiment} class to return only
 * the id, title, description, and status.
 */
public interface ExperimentTableProjection {

    /**
     * Returns the unique id of the experiment.
     *
     * @return The experiment id.
     */
    Integer getId();

    /**
     * Returns the unique title of the experiment.
     *
     * @return The title.
     */
    String getTitle();

    /**
     * Returns the description of the experiment.
     *
     * @return The description.
     */
    String getDescription();

    /**
     * Returns whether the experiment is currently running.
     *
     * @return The experiment status.
     */
    boolean isActive();

}
