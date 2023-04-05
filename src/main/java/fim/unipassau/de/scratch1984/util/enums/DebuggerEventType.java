package fim.unipassau.de.scratch1984.util.enums;

/**
 * All possible event types for a debugger event.
 */
public enum DebuggerEventType {

    /**
     * The event was caused by a breakpoint interaction.
     */
    BREAKPOINT,

    /**
     * The event was caused by a block interaction in the debugger.
     */
    BLOCK,

    /**
     * The event was caused by selecting a sprite in the debugger.
     */
    SPRITE,

    /**
     * The event was caused by opening the debugger.
     */
    TARGET

}
