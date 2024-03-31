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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles common cases of invalid request parameters.
 */
@ControllerAdvice(annotations = {Controller.class})
public class InvalidParamBindingControllerAdvice {

    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(InvalidParamBindingControllerAdvice.class);

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
     * Handles cases where an invalid ID was received.
     * @param exception The original error.
     * @return A redirect to the error page.
     */
    @ExceptionHandler(InvalidIdException.class)
    public ModelAndView handleInvalidParameterTypeException(final InvalidIdException exception) {
        log.error("Received invalid ID!", exception);
        return redirectToErrorPage();
    }

    private ModelAndView redirectToErrorPage() {
        ModelAndView mv = new ModelAndView("error");
        mv.setStatus(HttpStatus.BAD_REQUEST);
        return mv;
    }

}
