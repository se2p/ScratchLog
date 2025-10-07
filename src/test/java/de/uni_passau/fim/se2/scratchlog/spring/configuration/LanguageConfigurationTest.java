package de.uni_passau.fim.se2.scratchlog.spring.configuration;

import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageConfigurationTest {

    private LanguageConfiguration languageConfiguration;

    @BeforeEach
    void setUp() {
        languageConfiguration = new LanguageConfiguration();
    }

    @Test
    void testDefaultIsGerman() {
        assertEquals(Language.GERMAN.toLocale(), Constants.DEFAULT_LANGUAGE.toLocale());
    }

    @Test
    void testGetSomeMessageInEnglishAndGerman() {
        MessageSource messageSource = languageConfiguration.messageSource();
        String key = "project_info";

        String englishMessage = messageSource.getMessage(key, null, Locale.ENGLISH);
        assertNotNull(englishMessage);
        assertTrue(englishMessage.startsWith("This project"));

        String germanMessage = messageSource.getMessage(key, null, Locale.GERMAN);
        assertNotNull(germanMessage);
        assertTrue(germanMessage.startsWith("Das Projekt"));
    }

    @Test
    void testUrlParameter() {
        LocaleResolver resolver = languageConfiguration.localeResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("lang", "en");

        Locale locale = resolver.resolveLocale(request);
        assertEquals(Language.ENGLISH.toLocale(), locale);
    }

    @Test
    void testUrlParameterInvalid() {
        LocaleResolver resolver = languageConfiguration.localeResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("lang", "xx");

        Locale locale = resolver.resolveLocale(request);
        assertEquals(Constants.DEFAULT_LANGUAGE.toLocale(), locale);
    }

    @Test
    void testAcceptLanguageHeader() {
        LocaleResolver resolver = languageConfiguration.localeResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "de,de-DE;q=0.9,en;q=0.8");

        Locale locale = resolver.resolveLocale(request);
        assertEquals(Language.GERMAN.toLocale(), locale);
    }

    @Test
    void testAcceptLanguageHeaderRegion() {
        LocaleResolver resolver = languageConfiguration.localeResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "en-UK");

        Locale locale = resolver.resolveLocale(request);
        assertEquals(Language.ENGLISH.toLocale(), locale);
    }

    @Test
    void testFallback() {
        LocaleResolver resolver = languageConfiguration.localeResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();

        Locale locale = resolver.resolveLocale(request);
        assertEquals(Constants.DEFAULT_LANGUAGE.toLocale(), locale);
    }

    @Test
    void testPriorityUrlParameterOverAcceptLanguageHeader() {
        LocaleResolver resolver = languageConfiguration.localeResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("lang", "de");
        request.addHeader("Accept-Language", "en,en-US;q=0.8");

        Locale locale = resolver.resolveLocale(request);
        assertEquals(Language.GERMAN.toLocale(), locale);
    }

    @Test
    void testPriorityUserSetting() {
        LocaleResolver resolver = languageConfiguration.localeResolver();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);

        // user setting is stored in the session during authentication
        session.setAttribute("USER_LOCALE", Language.ENGLISH.toLocale());
        request.addHeader("Accept-Language", "de,de-DE;q=0.9,en;q=0.8");

        Locale locale = resolver.resolveLocale(request);
        assertEquals(Language.ENGLISH.toLocale(), locale);
    }

}
