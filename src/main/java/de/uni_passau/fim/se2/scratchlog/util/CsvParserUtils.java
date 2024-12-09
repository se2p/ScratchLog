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
