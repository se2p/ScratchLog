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

package de.uni_passau.fim.se2.scratchlog.application;

import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.exception.StoreException;
import de.uni_passau.fim.se2.scratchlog.application.service.TokenService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Token;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.TokenRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.util.enums.TokenType;
import de.uni_passau.fim.se2.scratchlog.web.dto.TokenDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private final Token defaultPasswordToken = new Token(TokenType.DEFAULT_PASSWORD, date, "1", user);
    private final List<Token> registerTokens = new ArrayList<>();
    private final List<Token> defaultTokens = new ArrayList<>();

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
        defaultPasswordToken.setValue(VALUE);
        defaultPasswordToken.setMetadata("1");
        defaultTokens.add(defaultPasswordToken);
    }

    @Test
    public void testGenerateEmailToken() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(tokenRepository.save(any())).thenReturn(token);
        TokenDTO tokenDTO = tokenService.generateToken(TokenType.CHANGE_EMAIL, EMAIL, ID);
        assertAll(
                () -> assertEquals(VALUE, tokenDTO.getValue()),
                () -> assertEquals(TokenType.CHANGE_EMAIL, tokenDTO.getType()),
                () -> assertEquals(EMAIL, tokenDTO.getMetadata()),
                () -> assertEquals(ID, tokenDTO.getUser())
        );
        verify(userRepository).getReferenceById(ID);
        verify(tokenRepository).save(any());
    }

    @Test
    public void testGenerateRegisterToken() {
        token.setType(TokenType.REGISTER);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(tokenRepository.save(any())).thenReturn(token);
        TokenDTO tokenDTO = tokenService.generateToken(TokenType.REGISTER, null, ID);
        assertAll(
                () -> assertEquals(VALUE, tokenDTO.getValue()),
                () -> assertEquals(TokenType.REGISTER, tokenDTO.getType()),
                () -> assertEquals(EMAIL, tokenDTO.getMetadata()),
                () -> assertEquals(ID, tokenDTO.getUser())
        );
        verify(userRepository).getReferenceById(ID);
        verify(tokenRepository).save(any());
    }

    @Test
    public void testGenerateForgotToken() {
        token.setType(TokenType.FORGOT_PASSWORD);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(tokenRepository.save(any())).thenReturn(token);
        TokenDTO tokenDTO = tokenService.generateToken(TokenType.FORGOT_PASSWORD, null, ID);
        assertAll(
                () -> assertEquals(VALUE, tokenDTO.getValue()),
                () -> assertEquals(TokenType.FORGOT_PASSWORD, tokenDTO.getType()),
                () -> assertEquals(EMAIL, tokenDTO.getMetadata()),
                () -> assertEquals(ID, tokenDTO.getUser())
        );
        verify(userRepository).getReferenceById(ID);
        verify(tokenRepository).save(any());
    }

    @Test
    public void testGenerateReactivateToken() {
        token.setType(TokenType.DEACTIVATED);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(tokenRepository.save(any())).thenReturn(token);
        TokenDTO tokenDTO = tokenService.generateToken(TokenType.DEACTIVATED, null, ID);
        assertAll(
                () -> assertEquals(VALUE, tokenDTO.getValue()),
                () -> assertEquals(TokenType.DEACTIVATED, tokenDTO.getType()),
                () -> assertEquals(EMAIL, tokenDTO.getMetadata()),
                () -> assertEquals(ID, tokenDTO.getUser())
        );
        verify(userRepository).getReferenceById(ID);
        verify(tokenRepository).save(any());
    }

    @Test
    public void testGenerateTokenStore() {
        token.setValue(null);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(tokenRepository.save(any())).thenReturn(token);
        assertThrows(StoreException.class,
                () -> tokenService.generateToken(TokenType.CHANGE_EMAIL, EMAIL, ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(tokenRepository).save(any());
    }

    @Test
    public void testGenerateTokenNotFound() {
        when(tokenRepository.save(any())).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> tokenService.generateToken(TokenType.CHANGE_EMAIL, EMAIL, ID)
        );
        verify(userRepository).getReferenceById(ID);
        verify(tokenRepository).save(any());
    }

    @Test
    public void testGenerateTokenTypeNull() {
        assertThrows(IllegalArgumentException.class,
                () -> tokenService.generateToken(null, EMAIL, ID)
        );
        verify(userRepository, never()).getReferenceById(ID);
        verify(tokenRepository, never()).save(any());
    }

    @Test
    public void testGenerateTokenInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> tokenService.generateToken(TokenType.CHANGE_EMAIL, EMAIL, 0)
        );
        verify(userRepository, never()).getReferenceById(ID);
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
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        tokenService.reactivateUserAccounts(dateTime);
        assertAll(
                () -> assertTrue(user.isActive()),
                () -> assertEquals(0, user.getAttempts())
        );
        verify(tokenRepository).findAllByDateBeforeAndType(dateTime, TokenType.DEACTIVATED);
        verify(userRepository, times(2)).getReferenceById(ID);
        verify(userRepository, times(2)).save(user);
    }

    @Test
    public void testReactivateUserAccountsEntityNotFound() {
        LocalDateTime dateTime = LocalDateTime.now();
        when(tokenRepository.findAllByDateBeforeAndType(dateTime, TokenType.DEACTIVATED)).thenReturn(registerTokens);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(userRepository.save(user)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> tokenService.reactivateUserAccounts(dateTime)
        );
        verify(tokenRepository).findAllByDateBeforeAndType(dateTime, TokenType.DEACTIVATED);
        verify(userRepository).getReferenceById(ID);
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
        verify(userRepository, never()).getReferenceById(anyInt());
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
        verify(userRepository, never()).getReferenceById(anyInt());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testReactivateUserAccountsTimeNull() {
        assertThrows(IllegalArgumentException.class,
                () -> tokenService.reactivateUserAccounts(null)
        );
        verify(tokenRepository, never()).findAllByDateBeforeAndType(any(), any());
        verify(userRepository, never()).getReferenceById(anyInt());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testCheckDefaultPasswordToken() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(tokenRepository.findAllByTypeAndUser(TokenType.DEFAULT_PASSWORD, user)).thenReturn(defaultTokens);
        assertEquals(1, tokenService.checkDefaultPasswordToken(ID, false));
        verify(userRepository).getReferenceById(ID);
        verify(tokenRepository).findAllByTypeAndUser(TokenType.DEFAULT_PASSWORD, user);
        verify(tokenRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testCheckDefaultPasswordTokenNoToken() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(tokenRepository.findAllByTypeAndUser(TokenType.DEFAULT_PASSWORD, user)).thenReturn(new ArrayList<>());
        assertEquals(0, tokenService.checkDefaultPasswordToken(ID, false));
        verify(userRepository).getReferenceById(ID);
        verify(tokenRepository).findAllByTypeAndUser(TokenType.DEFAULT_PASSWORD, user);
        verify(tokenRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testCheckDefaultPasswordTokenLoginGenerateToken() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(tokenRepository.findAllByTypeAndUser(TokenType.DEFAULT_PASSWORD, user)).thenReturn(new ArrayList<>());
        assertEquals(0, tokenService.checkDefaultPasswordToken(ID, true));
        verify(userRepository).getReferenceById(ID);
        verify(tokenRepository).findAllByTypeAndUser(TokenType.DEFAULT_PASSWORD, user);
        verify(tokenRepository).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testCheckDefaultPasswordTokenLoginUpdateToken() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(tokenRepository.findAllByTypeAndUser(TokenType.DEFAULT_PASSWORD, user)).thenReturn(defaultTokens);
        assertAll(
                () -> assertEquals(1, tokenService.checkDefaultPasswordToken(ID, true)),
                () -> assertEquals("2", defaultPasswordToken.getMetadata())
        );
        verify(userRepository).getReferenceById(ID);
        verify(tokenRepository).findAllByTypeAndUser(TokenType.DEFAULT_PASSWORD, user);
        verify(tokenRepository).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testCheckDefaultPasswordTokenLoginDeactivateUser() {
        defaultPasswordToken.setMetadata("5");
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(tokenRepository.findAllByTypeAndUser(TokenType.DEFAULT_PASSWORD, user)).thenReturn(defaultTokens);
        assertEquals(Constants.MAX_DEFAULT_ATTEMPTS, tokenService.checkDefaultPasswordToken(ID, true));
        verify(userRepository).getReferenceById(ID);
        verify(tokenRepository).findAllByTypeAndUser(TokenType.DEFAULT_PASSWORD, user);
        verify(tokenRepository, never()).save(any());
        verify(tokenRepository).deleteById(defaultPasswordToken.getValue());
        verify(userRepository).save(any());
    }

    @Test
    public void testCheckDefaultPasswordTokenIllegalState() {
        List<Token> tokens = List.of(defaultPasswordToken, defaultPasswordToken);
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(tokenRepository.findAllByTypeAndUser(TokenType.DEFAULT_PASSWORD, user)).thenReturn(tokens);
        assertThrows(IllegalStateException.class,
                () -> tokenService.checkDefaultPasswordToken(ID, false)
        );
        verify(userRepository).getReferenceById(ID);
        verify(tokenRepository).findAllByTypeAndUser(TokenType.DEFAULT_PASSWORD, user);
        verify(tokenRepository, never()).save(any());
        verify(tokenRepository, never()).deleteById(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testCheckDefaultPasswordTokenNotFound() {
        when(userRepository.getReferenceById(ID)).thenReturn(user);
        when(tokenRepository.findAllByTypeAndUser(TokenType.DEFAULT_PASSWORD,
                user)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> tokenService.checkDefaultPasswordToken(ID, false)
        );
        verify(userRepository).getReferenceById(ID);
        verify(tokenRepository).findAllByTypeAndUser(TokenType.DEFAULT_PASSWORD, user);
        verify(tokenRepository, never()).save(any());
        verify(tokenRepository, never()).deleteById(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testCheckDefaultPasswordTokenInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> tokenService.checkDefaultPasswordToken(0, false)
        );
        verify(userRepository, never()).getReferenceById(anyInt());
        verify(tokenRepository, never()).findAllByTypeAndUser(any(), any());
        verify(tokenRepository, never()).save(any());
        verify(tokenRepository, never()).deleteById(anyString());
        verify(userRepository, never()).save(any());
    }
}
