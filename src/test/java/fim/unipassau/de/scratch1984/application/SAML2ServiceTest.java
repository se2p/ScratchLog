package fim.unipassau.de.scratch1984.application;

import fim.unipassau.de.scratch1984.application.service.SAML2Service;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.spring.configuration.SAML2Properties;
import fim.unipassau.de.scratch1984.util.enums.Language;
import fim.unipassau.de.scratch1984.util.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SAML2ServiceTest {

    private SAML2Service saml2Service;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SAML2Properties properties;

    @Mock
    private Saml2AuthenticatedPrincipal principal;

    @Mock
    private Map<String, List<Object>> attributes;

    private static final String VALUE = "value";
    private Set<String> keySet = Set.of(VALUE);
    private final User user = new User("user", "email", Role.PARTICIPANT, Language.ENGLISH, "password", "secret");

    @BeforeEach
    public void setup() {
        when(properties.getKey()).thenReturn(VALUE);
        when(properties.getValuePattern()).thenReturn("(?<value>\\d+)");
        saml2Service = new SAML2Service(userRepository, properties);
        keySet = Set.of(VALUE);
        user.setId(1);
    }

    @Test
    public void testHandleAuthentication() {
        when(properties.getUsernamePattern()).thenReturn(VALUE);
        when(properties.getEmailPattern()).thenReturn(VALUE);
        when(principal.getAttributes()).thenReturn(attributes);
        when(attributes.keySet()).thenReturn(keySet);
        when(principal.getFirstAttribute(VALUE)).thenReturn(VALUE);
        when(userRepository.save(any())).thenReturn(user);
        Authentication authentication = saml2Service.handleAuthentication(principal);
        assertEquals(authentication.getName(), user.getUsername());
        verify(properties).getUsernamePattern();
        verify(properties).getEmailPattern();
        verify(principal, times(2)).getAttributes();
        verify(attributes, times(2)).keySet();
        verify(principal, times(2)).getFirstAttribute(VALUE);
        verify(userRepository).save(any());
    }

    @Test
    public void testHandleAuthenticationUserPresent() {
        when(properties.getUsernamePattern()).thenReturn(VALUE);
        when(principal.getAttributes()).thenReturn(attributes);
        when(attributes.keySet()).thenReturn(keySet);
        when(principal.getFirstAttribute(VALUE)).thenReturn(VALUE);
        when(userRepository.findUserByUsername(VALUE)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        Authentication authentication = saml2Service.handleAuthentication(principal);
        assertAll(
                () -> assertEquals(authentication.getName(), user.getUsername()),
                () -> assertNotNull(user.getLastLogin())
        );
        verify(properties).getUsernamePattern();
        verify(principal).getAttributes();
        verify(attributes).keySet();
        verify(principal).getFirstAttribute(VALUE);
        verify(userRepository).save(any());
    }

    @Test
    public void testHandleAuthenticationPatternNull() {
        keySet = Set.of("bla");
        when(properties.getUsernamePattern()).thenReturn(VALUE);
        when(properties.getEmailPattern()).thenReturn(VALUE);
        when(principal.getAttributes()).thenReturn(attributes);
        when(attributes.keySet()).thenReturn(keySet);
        when(principal.getFirstAttribute("bla")).thenReturn("bla");
        when(userRepository.save(any())).thenReturn(user);
        Authentication authentication = saml2Service.handleAuthentication(principal);
        assertEquals(authentication.getName(), user.getUsername());
        verify(properties).getUsernamePattern();
        verify(properties).getEmailPattern();
        verify(principal, times(2)).getAttributes();
        verify(attributes, times(2)).keySet();
        verify(principal, times(2)).getFirstAttribute("bla");
        verify(userRepository).save(any());
    }

    @Test
    public void testHandleAuthenticationAttributeNull() {
        when(properties.getUsernamePattern()).thenReturn(VALUE);
        when(properties.getEmailPattern()).thenReturn(VALUE);
        when(principal.getAttributes()).thenReturn(attributes);
        when(attributes.keySet()).thenReturn(keySet);
        when(userRepository.save(any())).thenReturn(user);
        Authentication authentication = saml2Service.handleAuthentication(principal);
        assertEquals(authentication.getName(), user.getUsername());
        verify(properties).getUsernamePattern();
        verify(properties).getEmailPattern();
        verify(principal, times(2)).getAttributes();
        verify(attributes, times(2)).keySet();
        verify(principal, times(2)).getFirstAttribute(VALUE);
        verify(userRepository).save(any());
    }

    @Test
    public void testHandleAuthenticationUserIdNull() {
        user.setId(null);
        when(properties.getUsernamePattern()).thenReturn(VALUE);
        when(properties.getEmailPattern()).thenReturn(VALUE);
        when(principal.getAttributes()).thenReturn(attributes);
        when(attributes.keySet()).thenReturn(keySet);
        when(principal.getFirstAttribute(VALUE)).thenReturn(VALUE);
        when(userRepository.save(any())).thenReturn(user);
        assertThrows(IllegalStateException.class,
                () -> saml2Service.handleAuthentication(principal)
        );
        verify(properties).getUsernamePattern();
        verify(properties).getEmailPattern();
        verify(principal, times(2)).getAttributes();
        verify(attributes, times(2)).keySet();
        verify(principal, times(2)).getFirstAttribute(VALUE);
        verify(userRepository).save(any());
    }

}
