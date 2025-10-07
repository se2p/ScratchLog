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

package de.uni_passau.fim.se2.scratchlog.util.enums;

import de.uni_passau.fim.se2.scratchlog.util.Constants;

import java.util.Locale;

/**
 * Enumeration of all supported application languages in ScratchLog.
 */
public enum Language {

    GERMAN(Locale.GERMAN),
    ENGLISH(Locale.ENGLISH);

    private final Locale locale;

    Language(Locale locale) {
        this.locale = locale;
    }

    public Locale toLocale() {
        return locale;
    }

    public static Language fromString(String value) {
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
