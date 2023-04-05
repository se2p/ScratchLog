package fim.unipassau.de.scratch1984.util.enums;

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
