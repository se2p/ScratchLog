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

package fim.unipassau.de.scratchLog.util.enums;

/**
 * All possible specific events for a click event.
 */
public enum ClickEventSpecific {

    /**
     * The user clicked on the green flag icon.
     */
    GREENFLAG,

    /**
     * The user clicked on the stop all icon.
     */
    STOPALL,

    /**
     * The user clicked on a block.
     */
    STACKCLICK,

    /**
     * The user rewound the execution slider.
     */
    REWIND_EXECUTION_SLIDER_CHANGE,

    /**
     * The user revisited the previous step in the block execution.
     */
    STEP_BACK,

    /**
     * The user jumped over an execution step.
     */
    STEP_OVER,

    /**
     * The user paused the execution of a code block.
     */
    PAUSE_EXECUTION,

    /**
     * The user resumed the execution of a code block.
     */
    RESUME_EXECUTION,

    /**
     * The user deactivated the observation.
     */
    DEACTIVATE_OBSERVATION,

    /**
     * The user activated the observation.
     */
    ACTIVATE_OBSERVATION,

    /**
     * The user closed the debugger.
     */
    CLOSE_DEBUGGER

}
