package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.TokenService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.web.controller.TokenController;
import fim.unipassau.de.scratch1984.web.dto.TokenDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenControllerTest {

    @InjectMocks
    private TokenController tokenController;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserService userService;

    private static final String ERROR = "redirect:/error";
    private static final String REDIRECT_SUCCESS = "redirect:/?success=true";
    private static final String REDIRECT_ERROR = "redirect:/?error=true";
    private static final String VALUE = "value";
    private static final String EMAIL = "admin@admin.com";
    private static final int ID = 1;
    private final TokenDTO tokenDTO = new TokenDTO(TokenDTO.Type.CHANGE_EMAIL, LocalDateTime.now(), EMAIL, ID);

    @BeforeEach
    public void setup() {
        LocalDateTime expirationDate = LocalDateTime.now();
        expirationDate = expirationDate.plusHours(1);
        tokenDTO.setValue(VALUE);
        tokenDTO.setType(TokenDTO.Type.CHANGE_EMAIL);
        tokenDTO.setExpirationDate(expirationDate);
    }

    @Test
    public void testValidateToken() {
        when(tokenService.findToken(VALUE)).thenReturn(tokenDTO);
        assertEquals(REDIRECT_SUCCESS, tokenController.validateToken(VALUE));
        verify(tokenService).findToken(VALUE);
        verify(userService).updateEmail(ID, EMAIL);
        verify(tokenService).deleteToken(VALUE);
    }

    @Test
    public void testValidateTokenUserNotFound() {
        when(tokenService.findToken(VALUE)).thenReturn(tokenDTO);
        doThrow(NotFoundException.class).when(userService).updateEmail(ID, EMAIL);
        assertEquals(ERROR, tokenController.validateToken(VALUE));
        verify(tokenService).findToken(VALUE);
        verify(userService).updateEmail(ID, EMAIL);
        verify(tokenService, never()).deleteToken(VALUE);
    }

    @Test
    public void testValidateTokenExpired() {
        tokenDTO.setExpirationDate(LocalDateTime.now());
        when(tokenService.findToken(VALUE)).thenReturn(tokenDTO);
        assertEquals(REDIRECT_ERROR, tokenController.validateToken(VALUE));
        verify(tokenService).findToken(VALUE);
        verify(userService, never()).updateEmail(ID, EMAIL);
        verify(tokenService, never()).deleteToken(VALUE);
    }

    @Test
    public void testValidateTokenNotFound() {
        when(tokenService.findToken(VALUE)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, tokenController.validateToken(VALUE));
        verify(tokenService).findToken(VALUE);
        verify(userService, never()).updateEmail(ID, EMAIL);
        verify(tokenService, never()).deleteToken(VALUE);
    }
}
