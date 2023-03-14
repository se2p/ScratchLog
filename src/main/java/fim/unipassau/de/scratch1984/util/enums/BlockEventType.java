package fim.unipassau.de.scratch1984.util.enums;

/**
 * All possible event types for a block event.
 */
public enum BlockEventType {

    /**
     * The event was caused by a mouse click.
     */
    CLICK,

    /**
     * The event was caused by the renaming of a variable or sprite.
     */
    RENAME,

    /**
     * The event was caused by the creation of a block, comment or variable.
     */
    CREATE,

    /**
     * The event was caused by the change of an existent block or comment.
     */
    CHANGE,

    /**
     * The event was caused by a block being moved.
     */
    MOVE,

    /**
     * The event was caused by the deletion of a block, variable or comment.
     */
    DELETE,

    /**
     * The event was caused by dragging a block.
     */
    DRAG

}
