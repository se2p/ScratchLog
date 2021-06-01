package fim.unipassau.de.scratch1984.application;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.application.service.TokenService;
import fim.unipassau.de.scratch1984.persistence.entity.Token;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.TokenRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.web.dto.TokenDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    private static final String CHANGE_EMAIL = "CHANGE_EMAIL";
    private static final String VALUE = "value";
    private static final String EMAIL = "admin@admin.com";
    private static final String BLANK = "   ";
    private static final int ID = 1;
    private final User user = new User();
    private final Token token = new Token(CHANGE_EMAIL, Timestamp.valueOf(LocalDateTime.now()), EMAIL, user);

    @BeforeEach
    public void setup() {
        user.setId(1);
        token.setValue(VALUE);
        token.setType(CHANGE_EMAIL);
    }

    @Test
    public void testGenerateEmailToken() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(tokenRepository.save(any())).thenReturn(token);
        TokenDTO tokenDTO = tokenService.generateToken(TokenDTO.Type.CHANGE_EMAIL, EMAIL, ID);
        assertAll(
                () -> assertEquals(VALUE, tokenDTO.getValue()),
                () -> assertEquals(TokenDTO.Type.CHANGE_EMAIL, tokenDTO.getType()),
                () -> assertEquals(EMAIL, tokenDTO.getMetadata()),
                () -> assertEquals(ID, tokenDTO.getUser())
        );
        verify(userRepository).getOne(ID);
        verify(tokenRepository).save(any());
    }

    @Test
    public void testGenerateRegisterToken() {
        token.setType(TokenDTO.Type.REGISTER.toString());
        when(userRepository.getOne(ID)).thenReturn(user);
        when(tokenRepository.save(any())).thenReturn(token);
        TokenDTO tokenDTO = tokenService.generateToken(TokenDTO.Type.REGISTER, null, ID);
        assertAll(
                () -> assertEquals(VALUE, tokenDTO.getValue()),
                () -> assertEquals(TokenDTO.Type.REGISTER, tokenDTO.getType()),
                () -> assertEquals(EMAIL, tokenDTO.getMetadata()),
                () -> assertEquals(ID, tokenDTO.getUser())
        );
        verify(userRepository).getOne(ID);
        verify(tokenRepository).save(any());
    }

    @Test
    public void testGenerateForgotToken() {
        token.setType(TokenDTO.Type.FORGOT_PASSWORD.toString());
        when(userRepository.getOne(ID)).thenReturn(user);
        when(tokenRepository.save(any())).thenReturn(token);
        TokenDTO tokenDTO = tokenService.generateToken(TokenDTO.Type.FORGOT_PASSWORD, null, ID);
        assertAll(
                () -> assertEquals(VALUE, tokenDTO.getValue()),
                () -> assertEquals(TokenDTO.Type.FORGOT_PASSWORD, tokenDTO.getType()),
                () -> assertEquals(EMAIL, tokenDTO.getMetadata()),
                () -> assertEquals(ID, tokenDTO.getUser())
        );
        verify(userRepository).getOne(ID);
        verify(tokenRepository).save(any());
    }

    @Test
    public void testGenerateTokenStore() {
        token.setValue(null);
        when(userRepository.getOne(ID)).thenReturn(user);
        when(tokenRepository.save(any())).thenReturn(token);
        assertThrows(StoreException.class,
                () -> tokenService.generateToken(TokenDTO.Type.CHANGE_EMAIL, EMAIL, ID)
        );
        verify(userRepository).getOne(ID);
        verify(tokenRepository).save(any());
    }

    @Test
    public void testGenerateTokenNotFound() {
        when(tokenRepository.save(any())).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> tokenService.generateToken(TokenDTO.Type.CHANGE_EMAIL, EMAIL, ID)
        );
        verify(userRepository).getOne(ID);
        verify(tokenRepository).save(any());
    }

    @Test
    public void testGenerateTokenTypeNull() {
        assertThrows(IllegalArgumentException.class,
                () -> tokenService.generateToken(null, EMAIL, ID)
        );
        verify(userRepository, never()).getOne(ID);
        verify(tokenRepository, never()).save(any());
    }

    @Test
    public void testGenerateTokenInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> tokenService.generateToken(TokenDTO.Type.CHANGE_EMAIL, EMAIL, 0)
        );
        verify(userRepository, never()).getOne(ID);
        verify(tokenRepository, never()).save(any());
    }

    @Test
    public void testFindToken() {
        when(tokenRepository.findByValue(VALUE)).thenReturn(token);
        TokenDTO tokenDTO = tokenService.findToken(VALUE);
        assertAll(
                () -> assertEquals(VALUE, tokenDTO.getValue()),
                () -> assertEquals(TokenDTO.Type.CHANGE_EMAIL, tokenDTO.getType()),
                () -> assertEquals(EMAIL, tokenDTO.getMetadata()),
                () -> assertEquals(ID, tokenDTO.getUser())
        );
        verify(tokenRepository).findByValue(VALUE);
    }

    @Test
    public void testFindTokenNotFound() {
        assertThrows(NotFoundException.class,
                () -> tokenService.findToken(VALUE)
        );
        verify(tokenRepository).findByValue(VALUE);
    }

    @Test
    public void testFindTokenValueBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> tokenService.findToken(BLANK)
        );
        verify(tokenRepository, never()).findByValue(anyString());
    }

    @Test
    public void testFindTokenValueNull() {
        assertThrows(IllegalArgumentException.class,
                () -> tokenService.findToken(null)
        );
        verify(tokenRepository, never()).findByValue(anyString());
    }

    @Test
    public void testDeleteToken() {
        tokenService.deleteToken(VALUE);
        verify(tokenRepository).deleteById(VALUE);
    }

    @Test
    public void testDeleteTokenValueBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> tokenService.deleteToken(BLANK)
        );
        verify(tokenRepository, never()).deleteById(anyString());
    }

    @Test
    public void testDeleteTokenValueNull() {
        assertThrows(IllegalArgumentException.class,
                () -> tokenService.deleteToken(null)
        );
        verify(tokenRepository, never()).deleteById(anyString());
    }

    @Test
    public void testDeleteExpiredTokens() {
        tokenService.deleteExpiredTokens(LocalDateTime.now());
        verify(tokenRepository).deleteAllByDateBefore(any());
    }

    @Test
    public void testDeleteExpiredTokensNull() {
        assertThrows(IllegalArgumentException.class,
                () -> tokenService.deleteExpiredTokens(null)
        );
        verify(tokenRepository, never()).deleteAllByDateBefore(any());
    }
}
