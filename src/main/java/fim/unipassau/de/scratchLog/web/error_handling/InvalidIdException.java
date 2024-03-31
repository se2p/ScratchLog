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

package fim.unipassau.de.scratchLog.web.error_handling;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Exception to be thrown in case invalid IDs are passed in by the client.
 */
public class InvalidIdException extends HttpStatusCodeException {

    /**
     * Creates a new exception instance.
     *
     * @param entity The name/type of the entity for which an invalid ID was received. E.g. "user", "page", …
     * @param id The invalid ID.
     */
    public InvalidIdException(final String entity, final int id) {
        super(HttpStatus.BAD_REQUEST, "Received invalid ID " + id + " for a " + entity);
    }

}
