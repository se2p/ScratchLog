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

package de.uni_passau.fim.se2.scratchlog.util;

import com.opencsv.bean.AbstractBeanField;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;

import java.util.Locale;

public final class CsvParserUtils {

    private CsvParserUtils() {
        throw new IllegalCallerException("utility class");
    }

    public static class TrimValueConverter extends AbstractBeanField<String, String> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected String convert(final String value) {
            if (value == null) {
                return null;
            } else {
                return value.trim();
            }
        }

    }

    public static class LanguageConverter extends AbstractBeanField<String, Language> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected Language convert(final String value) {
            if (value == null) {
                return null;
            } else {
                return Language.valueOf(value.trim().toUpperCase(Locale.ROOT));
            }
        }

    }

}
