package fim.unipassau.de.scratch1984.util.enums;

/**
 * All possible specific events for a debugger event.
 */
public enum DebuggerEventSpecific {

    /**
     * The user opened the debugger on a sprite or stage.
     */
    OPEN_DEBUGGER,

    /**
     * The user selected a sprite instance in the dropdown list of the debugger.
     */
    SELECT_SPRITE,

    /**
     * The user opened the debugger for a specific block.
     */
    OPEN_BLOCK,

    /**
     * The user selected a block execution in the dropdown list of the debugger.
     */
    SELECT_BLOCK_EXECUTION,

    /**
     * The user clicked on a block and is routed to the block-debugger.
     */
    ROUTE_TO_BLOCK,

    /**
     * The user added a breakpoint.
     */
    ADD_BREAKPOINT,

    /**
     * The user deleted a breakpoint.
     */
    DELETE_BREAKPOINT

}
