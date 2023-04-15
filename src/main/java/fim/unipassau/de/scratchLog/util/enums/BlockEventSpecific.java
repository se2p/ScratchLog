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
