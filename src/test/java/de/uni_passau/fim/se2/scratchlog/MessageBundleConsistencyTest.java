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

package de.uni_passau.fim.se2.scratchlog;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class MessageBundleConsistencyTest {

    private final Set<String> languages = Set.of("en", "de");

    @Test
    void checkSameTranslationKeys() {
        final Map<String, Set<String>> bundles = new HashMap<>();

        for (String locale : languages) {
            ResourceBundle bundle = ResourceBundle.getBundle("i18n/messages", Locale.forLanguageTag(locale));
            bundles.put(locale, bundle.keySet());
        }

        final Set<String> base = bundles.get("en");
        assertAll(
            languages.stream()
                .map(locale -> () -> assertThat(bundles.get(locale))
                    .as("Bundle '%s' does not have same keys as English.", locale)
                    .containsExactlyElementsOf(base)
                )
        );
    }
}
