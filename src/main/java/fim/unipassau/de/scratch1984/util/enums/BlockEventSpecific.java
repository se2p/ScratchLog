package fim.unipassau.de.scratch1984.util.enums;

/**
 * All possible specific events for a block event.
 */
public enum BlockEventSpecific {

    /**
     * The user clicked on the green flag icon.
     */
    GREENFLAG,

    /**
     * The user clicked on the stop all icon.
     */
    STOPALL,

    /**
     * The user renamed the sprite.
     */
    SPRITE,

    /**
     * The user clicked on a block.
     */
    STACKCLICK,

    /**
     * The user created a new block.
     */
    CREATE,

    /**
     * The user changed an existing block.
     */
    CHANGE,

    /**
     * The user moved a block.
     */
    MOVE,

    /**
     * The user dragged a block outside.
     */
    DRAGOUTSIDE,

    /**
     * The user dragged a block onto another.
     */
    ENDDRAGONTO,

    /**
     * The user finished dragging the block.
     */
    ENDDRAG,

    /**
     * The user deleted a block.
     */
    DELETE,

    /**
     * The user created a global variable.
     */
    VAR_CREATE_GLOBAL,

    /**
     * The user created a local variable.
     */
    VAR_CREATE_LOCAL,

    /**
     * The user renamed a global variable.
     */
    VAR_RENAME_GLOBAL,

    /**
     * The user renamed a local variable.
     */
    VAR_RENAME_LOCAL,

    /**
     * The user deleted a variable.
     */
    VAR_DELETE,

    /**
     * The user created a comment.
     */
    COMMENT_CREATE,

    /**
     * The user changed a comment.
     */
    COMMENT_CHANGE,

    /**
     * The user moved a comment.
     */
    COMMENT_MOVE,

    /**
     * The user deleted a comment.
     */
    COMMENT_DELETE

}
