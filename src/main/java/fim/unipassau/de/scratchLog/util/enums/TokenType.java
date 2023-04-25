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
 * The available token types.
 */
public enum TokenType {

    /**
     * A token for registration.
     */
    REGISTER,

    /**
     * A token for resetting the password.
     */
    FORGOT_PASSWORD,

    /**
     * A token for changing the e-mail address.
     */
    CHANGE_EMAIL,

    /**
     * A token for a temporarily deactivated user account.
     */
    DEACTIVATED

}
