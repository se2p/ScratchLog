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
 * All possible specific events for a resource event.
 */
public enum ResourceEventSpecific {

    /**
     * The user deleted a costume or backdrop.
     */
    DELETE_COSTUME,

    /**
     * The user deleted a sound.
     */
    DELETE_SOUND,

    /**
     * The user added a costume or backdrop.
     */
    ADD_COSTUME,

    /**
     * The user added a sound.
     */
    ADD_SOUND,

    /**
     * The user renamed a costume.
     */
    RENAME_COSTUME,

    /**
     * The user renamed a backdrop.
     */
    RENAME_BACKDROP,

    /**
     * The user renamed a sound.
     */
    RENAME_SOUND

}
