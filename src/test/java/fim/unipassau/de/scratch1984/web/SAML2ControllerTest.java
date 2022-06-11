package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.service.SAML2Service;
import fim.unipassau.de.scratch1984.spring.authentication.CustomAuthenticationProvider;
import fim.unipassau.de.scratch1984.util.ApplicationProperties;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.controller.SAML2Controller;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.reflect.Whitebox;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SAML2ControllerTest {

    @InjectMocks
    private SAML2Controller saml2Controller;

    @Mock
    private Optional<SAML2Service> service;

    @Mock
    private SAML2Service saml2Service;

    @Mock
    private CustomAuthenticationProvider authenticationProvider;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpSession session;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private Saml2AuthenticatedPrincipal principal;

    private MockedStatic<SecurityContextHolder> securityContextHolder;

    @BeforeEach
    public void setup() {
        setSAMLAuthentication(true);
        securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    public void cleanup() {
        securityContextHolder.close();
    }

    @Test
    public void testAuthorizeSAML2() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(service.get()).thenReturn(saml2Service);
        when(saml2Service.handleAuthentication(principal)).thenReturn(authentication);
        when(httpServletRequest.getSession(anyBoolean())).thenReturn(session);
        when(authenticationProvider.authenticate(authentication)).thenReturn(authentication);
        assertEquals("redirect:/", saml2Controller.authorizeSAML2(httpServletRequest));
        verify(securityContext).getAuthentication();
        verify(authentication).isAuthenticated();
        verify(authentication, times(2)).getPrincipal();
        verify(service).get();
        verify(saml2Service).handleAuthentication(principal);
        verify(httpServletRequest, times(2)).getSession(anyBoolean());
        verify(authenticationProvider).authenticate(authentication);
    }

    @Test
    public void testAuthorizeSAML2Exception() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(service.get()).thenReturn(saml2Service);
        when(saml2Service.handleAuthentication(principal)).thenThrow(IllegalStateException.class);
        assertEquals(Constants.ERROR, saml2Controller.authorizeSAML2(httpServletRequest));
        verify(securityContext).getAuthentication();
        verify(authentication).isAuthenticated();
        verify(authentication, times(2)).getPrincipal();
        verify(service).get();
        verify(saml2Service).handleAuthentication(principal);
        verify(httpServletRequest).getSession(false);
        verify(authenticationProvider, never()).authenticate(any());
    }

    @Test
    public void testAuthorizeSAML2WrongPrincipal() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        assertEquals(Constants.ERROR, saml2Controller.authorizeSAML2(httpServletRequest));
        verify(securityContext).getAuthentication();
        verify(authentication).isAuthenticated();
        verify(authentication).getPrincipal();
        verify(service, never()).get();
        verify(saml2Service, never()).handleAuthentication(any());
        verify(httpServletRequest).getSession(false);
        verify(authenticationProvider, never()).authenticate(any());
    }

    @Test
    public void testAuthorizeSAML2NotAuthenticated() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Constants.ERROR, saml2Controller.authorizeSAML2(httpServletRequest));
        verify(securityContext).getAuthentication();
        verify(authentication).isAuthenticated();
        verify(authentication, never()).getPrincipal();
        verify(service, never()).get();
        verify(saml2Service, never()).handleAuthentication(any());
        verify(httpServletRequest).getSession(false);
        verify(authenticationProvider, never()).authenticate(any());
    }

    @Test
    public void testAuthorizeSAML2NoAuthentication() {
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        assertEquals(Constants.ERROR, saml2Controller.authorizeSAML2(httpServletRequest));
        verify(securityContext).getAuthentication();
        verify(authentication, never()).isAuthenticated();
        verify(authentication, never()).getPrincipal();
        verify(service, never()).get();
        verify(saml2Service, never()).handleAuthentication(any());
        verify(httpServletRequest).getSession(false);
        verify(authenticationProvider, never()).authenticate(any());
    }

    @Test
    public void testAuthorizeSAML2NotEnabled() {
        setSAMLAuthentication(false);
        assertEquals(Constants.ERROR, saml2Controller.authorizeSAML2(httpServletRequest));
        verify(securityContext, never()).getAuthentication();
        verify(authentication, never()).isAuthenticated();
        verify(authentication, never()).getPrincipal();
        verify(service, never()).get();
        verify(saml2Service, never()).handleAuthentication(any());
        verify(httpServletRequest).getSession(false);
        verify(authenticationProvider, never()).authenticate(any());
    }

    @Test
    public void testAuthorizeSAML2ServiceEmpty() {
        when(service.isEmpty()).thenReturn(true);
        setSAMLAuthentication(false);
        assertEquals(Constants.ERROR, saml2Controller.authorizeSAML2(httpServletRequest));
        verify(securityContext, never()).getAuthentication();
        verify(authentication, never()).isAuthenticated();
        verify(authentication, never()).getPrincipal();
        verify(service, never()).get();
        verify(saml2Service, never()).handleAuthentication(any());
        verify(httpServletRequest).getSession(false);
        verify(authenticationProvider, never()).authenticate(any());
    }

    public static void setSAMLAuthentication(boolean isSAMLAuth) {
        Mockito.mock(ApplicationProperties.class);
        Whitebox.setInternalState(ApplicationProperties.class, "SAML_AUTHENTICATION", isSAMLAuth);
    }

}
