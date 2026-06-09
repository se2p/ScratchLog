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

import de.uni_passau.fim.se2.scratchlog.util.Constants;

import java.util.Locale;

/**
 * Enumeration of all supported application languages in ScratchLog.
 */
public enum Language {

    /**
     * German.
     */
    GERMAN(Locale.GERMAN),

    /**
     * English.
     */
    ENGLISH(Locale.ENGLISH);

    private final Locale locale;

    Language(final Locale locale) {
        this.locale = locale;
    }

    public Locale toLocale() {
        return locale;
    }

    /**
     * Tries to convert the given string into a language.
     *
     * @param value Some string.
     * @return The language, or {@link Constants#DEFAULT_LANGUAGE} if the given value cannot be
     *         interpreted as any of the defined languages.
     */
    public static Language fromString(final String value) {
        if (value == null) {
            return Constants.DEFAULT_LANGUAGE;
        }

        return switch (value.toLowerCase()) {
            case "de" -> GERMAN;
            case "en" -> ENGLISH;
            default -> Constants.DEFAULT_LANGUAGE;
        };
    }
}
