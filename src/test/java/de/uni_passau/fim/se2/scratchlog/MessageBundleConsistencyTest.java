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
