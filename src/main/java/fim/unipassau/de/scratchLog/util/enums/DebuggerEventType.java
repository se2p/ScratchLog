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
