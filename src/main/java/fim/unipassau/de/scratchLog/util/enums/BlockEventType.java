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
