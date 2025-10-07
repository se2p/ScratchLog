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

package de.uni_passau.fim.se2.scratchlog.spring.configuration;

import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.Locale;

/**
 * Locale configuration for ScratchLog.
 * Resolves the locale in the following order:
 * 1. URL parameter "?lang="
 * 2. Authenticated user's preferred language
 * 3. Accept-Language header
 * 4. Default language (fallback)
 */
@Configuration
public class LanguageConfiguration {

    /**
     * Provides the message source for Thymeleaf and other Spring components.
     * Ensures UTF-8 encoding, fallback to default locale, and disables system locale fallback.
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultLocale(Constants.DEFAULT_LANGUAGE.toLocale());
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {

        AcceptHeaderLocaleResolver acceptHeaderLocaleResolver = new AcceptHeaderLocaleResolver();
        acceptHeaderLocaleResolver.setSupportedLocales(
            Arrays.stream(Language.values()).map(Language::toLocale).toList());
        acceptHeaderLocaleResolver.setDefaultLocale(Constants.DEFAULT_LANGUAGE.toLocale());

        return new LocaleResolver() {

            @Override
            @NonNull
            public Locale resolveLocale(@NonNull HttpServletRequest request) {

                // 1. URL parameter
                String langParam = request.getParameter("lang");
                if (langParam != null) {
                    return Language.fromString(langParam).toLocale();
                }

                // 2. Authenticated user's language
                Object sessionLocale = request.getSession().getAttribute("USER_LOCALE");
                if (sessionLocale instanceof Locale loc) {
                    return loc;
                }

                // 3. Delegate to AcceptHeaderLocaleResolver (4. fallback to default Locale)
                return acceptHeaderLocaleResolver.resolveLocale(request);
            }

            @Override
            public void setLocale(@NonNull HttpServletRequest request, HttpServletResponse response, Locale locale) {
                if (locale != null) {
                    request.getSession(true).setAttribute("USER_LOCALE", locale);
                }
            }
        };
    }
}
