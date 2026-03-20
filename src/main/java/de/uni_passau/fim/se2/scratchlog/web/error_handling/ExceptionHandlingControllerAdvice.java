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

package de.uni_passau.fim.se2.scratchlog.web.error_handling;

import de.uni_passau.fim.se2.scratchlog.util.Constants;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Global controller advice that handles common exceptions thrown in controllers.
 */
@ControllerAdvice(annotations = {Controller.class})
public class ExceptionHandlingControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlingControllerAdvice.class);

    /**
     * Handles cases where the type of the parameter could not be converted automatically.
     *
     * @param exception The original error.
     * @return A redirect to the error page.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView handleInvalidParameterTypeException(final MethodArgumentTypeMismatchException exception) {
        log.error("Was unable to convert parameter type!", exception);
        return redirectToErrorPage();
    }

    /**
     * Global exception handler that handles {@link IllegalArgumentException}s by redirecting to the error page with a
     * "bad request" response.
     *
     * @return A redirect to the error page with a HTTP 400 status code.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgumentException() {
        return redirectToErrorPage();
    }

    /**
     * Global exception handle that handles {@link ConstraintViolationException}s by logging the exception and
     * redirecting to the error page with a "bad request" response.
     *
     * @param exception The exception that occurred.
     * @return A redirect to the error page with a HTTP 400 status code.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ModelAndView handleConstraintViolationException(final ConstraintViolationException exception) {
        log.error("Constraint violation occurred!", exception);
        return redirectToErrorPage();
    }

    /**
     * Converts the exception into the {@code NOT_FOUND} HTTP code.
     *
     * @return A NOT_FOUND response.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> handleNotFoundException() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    private ModelAndView redirectToErrorPage() {
        ModelAndView mv = new ModelAndView(Constants.ERROR);
        mv.setStatus(HttpStatus.BAD_REQUEST);
        return mv;
    }

}
