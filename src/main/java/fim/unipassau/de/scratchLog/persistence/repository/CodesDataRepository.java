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

package fim.unipassau.de.scratchLog.persistence.repository;

import fim.unipassau.de.scratchLog.persistence.entity.CodesData;
import fim.unipassau.de.scratchLog.persistence.entity.CodesDataId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * A repository providing functionality for retrieving the codes count data.
 */
public interface CodesDataRepository extends JpaRepository<CodesData, CodesDataId> {

    /**
     * Returns the {@link CodesData} for the given user during the given experiment, if any exist.
     *
     * @param user The user id to search for.
     * @param experiment The experiment id to search for.
     * @return The corresponding codes data, or {@code null}.
     */
    Optional<CodesData> findByUserAndExperiment(Integer user, Integer experiment);

    /**
     * Returns a list of all {@link CodesData} for the given experiment, if any exist.
     *
     * @param experiment The experiment id to search for.
     * @return A list containing the data.
     */
    List<CodesData> findAllByExperiment(Integer experiment);

}
