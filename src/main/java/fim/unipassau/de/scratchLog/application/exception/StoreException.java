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

package fim.unipassau.de.scratchLog.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception indicating that data could not be stored.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StoreException extends RuntimeException {

    /**
     * Constructs a {@link StoreException} with no detail message.
     */
    public StoreException() {
        super();
    }

    /**
     * Constructs a {@link StoreException} with the specified detail message and cause.
     *
     * Note that the detail message associated with {@code cause} is not automatically incorporated in this
     * exception's detail message.
     *
     * @param message The detail message describing this particular exception.
     * @param cause   The cause for this particular exception.
     */
    public StoreException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@link StoreException} with the specified detail message.
     *
     * @param message The detail message describing this particular exception.
     */
    public StoreException(final String message) {
        super(message);
    }

    /**
     * Constructs a {@link StoreException} with the specified cause and the detail message of {@code cause}.
     * This constructor is useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public StoreException(final Throwable cause) {
        super(cause);
    }

}
