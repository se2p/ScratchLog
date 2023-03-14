package fim.unipassau.de.scratch1984.application;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.application.service.TokenService;
import fim.unipassau.de.scratch1984.persistence.entity.Token;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.TokenRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.util.enums.TokenType;
import fim.unipassau.de.scratch1984.web.dto.TokenDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    private static final String VALUE = "value";
    private static final String EMAIL = "admin@admin.com";
    private static final String BLANK = "   ";
    private static final int ID = 1;
    private final User user = new User();
    private final LocalDateTime date = LocalDateTime.now();
    private final Token token = new Token(TokenType.CHANGE_EMAIL, LocalDateTime.now(), EMAIL, user);
    private final Token registerToken1 = new Token(TokenType.REGISTER, date, null, user);
    private final Token registerToken2 = new Token(TokenType.REGISTER, date, null, user);
    private final List<Token> registerTokens = new ArrayList<>();

    @BeforeEach
    public void setup() {
        user.setId(ID);
        user.setAttempts(3);
        user.setActive(false);
        token.setValue(VALUE);
        token.setType(TokenType.CHANGE_EMAIL);
        registerToken1.setUser(user);
        registerTokens.add(registerToken1);
        registerTokens.add(registerToken2);
    }

    @Test
    public void testGenerateEmailToken() {
        when(userRepository.getOne(ID)).thenReturn(user);
        when(tokenRepository.save(any())).thenReturn(token);
        TokenDTO tokenDTO = tokenService.generateToken(TokenType.CHANGE_EMAIL, EMAIL, ID);
        assertAll(
                () -> assertEquals(VALUE, tokenDTO.getValue()),
                () -> assertEquals(TokenType.CHANGE_EMAIL, tokenDTO.getType()),
                () -> assertEquals(EMAIL, tokenDTO.getMetadata()),
                () -> assertEquals(ID, tokenDTO.getUser())
        );
        verify(userRepository).getOne(ID);
        verify(tokenRepository).save(any());
    }

    @Test
    public void testGenerateRegisterToken() {
        token.setType(TokenType.REGISTER);
        when(userRepository.getOne(ID)).thenReturn(user);
        when(tokenRepository.save(any())).thenReturn(token);
        TokenDTO tokenDTO = tokenService.generateToken(TokenType.REGISTER, null, ID);
        assertAll(
                () -> assertEquals(VALUE, tokenDTO.getValue()),
                () -> assertEquals(TokenType.REGISTER, tokenDTO.getType()),
                () -> assertEquals(EMAIL, tokenDTO.getMetadata()),
                () -> assertEquals(ID, tokenDTO.getUser())
        );
        verify(userRepository).getOne(ID);
        verify(tokenRepository).save(any());
    }

    @Test
    public void testGenerateForgotToken() {
        token.setType(TokenType.FORGOT_PASSWORD);
        when(userRepository.getOne(ID)).thenReturn(user);
        when(tokenRepository.save(any())).thenReturn(token);
        TokenDTO tokenDTO = tokenService.generateToken(TokenType.FORGOT_PASSWORD, null, ID);
        assertAll(
                () -> assertEquals(VALUE, tokenDTO.getValue()),
                () -> assertEquals(TokenType.FORGOT_PASSWORD, tokenDTO.getType()),
                () -> assertEquals(EMAIL, tokenDTO.getMetadata()),
                () -> assertEquals(ID, tokenDTO.getUser())
        );
        verify(userRepository).getOne(ID);
        verify(tokenRepository).save(any());
    }

    @Test
    public void testGenerateReactivateToken() {
        token.setType(TokenType.DEACTIVATED);
        when(userRepository.getOne(ID)).thenReturn(user);
        when(tokenRepository.save(any())).thenReturn(token);
        TokenDTO tokenDTO = tokenService.generateToken(TokenType.DEACTIVATED, null, ID);
        assertAll(
                () -> assertEquals(VALUE, tokenDTO.getValue()),
                () -> assertEquals(TokenType.DEACTIVATED, tokenDTO.getType()),
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
                () -> tokenService.generateToken(TokenType.CHANGE_EMAIL, EMAIL, ID)
        );
        verify(userRepository).getOne(ID);
        verify(tokenRepository).save(any());
    }

    @Test
    public void testGenerateTokenNotFound() {
        when(tokenRepository.save(any())).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> tokenService.generateToken(TokenType.CHANGE_EMAIL, EMAIL, ID)
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
                () -> tokenService.generateToken(TokenType.CHANGE_EMAIL, EMAIL, 0)
        );
        verify(userRepository, never()).getOne(ID);
        verify(tokenRepository, never()).save(any());
    }

    @Test
    public void testFindToken() {
        when(tokenRepository.findByValue(VALUE)).thenReturn(Optional.of(token));
        TokenDTO tokenDTO = tokenService.findToken(VALUE);
        assertAll(
                () -> assertEquals(VALUE, tokenDTO.getValue()),
                () -> assertEquals(TokenType.CHANGE_EMAIL, tokenDTO.getType()),
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

    @Test
    public void testDeleteExpiredAccounts() {
        LocalDateTime dateTime = LocalDateTime.now();
        when(tokenRepository.findAllByDateBeforeAndType(dateTime, TokenType.REGISTER)).thenReturn(registerTokens);
        assertDoesNotThrow(
                () -> tokenService.deleteExpiredAccounts(dateTime)
        );
        verify(tokenRepository).findAllByDateBeforeAndType(dateTime, TokenType.REGISTER);
        verify(userRepository, times(2)).deleteById(ID);
    }

    @Test
    public void testDeleteExpiredAccountsUserNull() {
        registerToken1.setUser(null);
        LocalDateTime dateTime = LocalDateTime.now();
        when(tokenRepository.findAllByDateBeforeAndType(dateTime, TokenType.REGISTER)).thenReturn(registerTokens);
        assertThrows(IllegalStateException.class,
                () -> tokenService.deleteExpiredAccounts(dateTime)
        );
        verify(tokenRepository).findAllByDateBeforeAndType(dateTime, TokenType.REGISTER);
        verify(userRepository, never()).deleteById(anyInt());
    }

    @Test
    public void testDeleteExpiredAccountsDateTimeNull() {
        assertThrows(IllegalArgumentException.class,
                () -> tokenService.deleteExpiredAccounts(null)
        );
        verify(tokenRepository, never()).findAllByDateBeforeAndType(any(), any());
        verify(userRepository, never()).deleteById(anyInt());
    }

    @Test
    public void testReactivateUserAccounts() {
        LocalDateTime dateTime = LocalDateTime.now();
        when(tokenRepository.findAllByDateBeforeAndType(dateTime, TokenType.DEACTIVATED)).thenReturn(registerTokens);
        when(userRepository.getOne(ID)).thenReturn(user);
        tokenService.reactivateUserAccounts(dateTime);
        assertAll(
                () -> assertTrue(user.isActive()),
                () -> assertEquals(0, user.getAttempts())
        );
        verify(tokenRepository).findAllByDateBeforeAndType(dateTime, TokenType.DEACTIVATED);
        verify(userRepository, times(2)).getOne(ID);
        verify(userRepository, times(2)).save(user);
    }

    @Test
    public void testReactivateUserAccountsEntityNotFound() {
        LocalDateTime dateTime = LocalDateTime.now();
        when(tokenRepository.findAllByDateBeforeAndType(dateTime, TokenType.DEACTIVATED)).thenReturn(registerTokens);
        when(userRepository.getOne(ID)).thenReturn(user);
        when(userRepository.save(user)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> tokenService.reactivateUserAccounts(dateTime)
        );
        verify(tokenRepository).findAllByDateBeforeAndType(dateTime, TokenType.DEACTIVATED);
        verify(userRepository).getOne(ID);
        verify(userRepository).save(user);
    }

    @Test
    public void testReactivateUserAccountsIdNull() {
        user.setId(null);
        LocalDateTime dateTime = LocalDateTime.now();
        when(tokenRepository.findAllByDateBeforeAndType(dateTime, TokenType.DEACTIVATED)).thenReturn(registerTokens);
        assertThrows(IllegalStateException.class,
                () -> tokenService.reactivateUserAccounts(dateTime)
        );
        verify(tokenRepository).findAllByDateBeforeAndType(dateTime, TokenType.DEACTIVATED);
        verify(userRepository, never()).getOne(anyInt());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testReactivateUserAccountsUserNull() {
        registerToken1.setUser(null);
        LocalDateTime dateTime = LocalDateTime.now();
        when(tokenRepository.findAllByDateBeforeAndType(dateTime, TokenType.DEACTIVATED)).thenReturn(registerTokens);
        assertThrows(IllegalStateException.class,
                () -> tokenService.reactivateUserAccounts(dateTime)
        );
        verify(tokenRepository).findAllByDateBeforeAndType(dateTime, TokenType.DEACTIVATED);
        verify(userRepository, never()).getOne(anyInt());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testReactivateUserAccountsTimeNull() {
        assertThrows(IllegalArgumentException.class,
                () -> tokenService.reactivateUserAccounts(null)
        );
        verify(tokenRepository, never()).findAllByDateBeforeAndType(any(), any());
        verify(userRepository, never()).getOne(anyInt());
        verify(userRepository, never()).save(any());
    }
}
