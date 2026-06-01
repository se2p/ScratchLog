/*
 * This file is part of ScratchLog.
 * Licenced under the GPL v3.0 or later.
 *
 * SPDX-FileCopyrightText: 2021-2026 Scratchlog contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.uni_passau.fim.se2.scratchlog.util.enums;

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
