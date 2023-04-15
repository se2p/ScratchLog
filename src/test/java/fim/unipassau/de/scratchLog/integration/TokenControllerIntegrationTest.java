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
package fim.unipassau.de.scratchLog.integration;

import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.application.service.TokenService;
import fim.unipassau.de.scratchLog.application.service.UserService;
import fim.unipassau.de.scratchLog.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratchLog.util.enums.Language;
import fim.unipassau.de.scratchLog.util.enums.Role;
import fim.unipassau.de.scratchLog.util.enums.TokenType;
import fim.unipassau.de.scratchLog.web.controller.TokenController;
import fim.unipassau.de.scratchLog.web.dto.TokenDTO;
import fim.unipassau.de.scratchLog.web.dto.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TokenController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class TokenControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @MockBean
    private TokenService tokenService;

    private static final String ERROR = "redirect:/error";
    private static final String REDIRECT_SUCCESS = "redirect:/?success=true";
    private static final String REDIRECT_ERROR = "redirect:/?error=true";
    private static final String PASSWORD_SET = "password-set";
    private static final String VALUE = "value";
    private static final String EMAIL = "admin@admin.com";
    private static final String VALID_PASSWORD = "V4l1d_P4ssw0rd!";
    private static final String USER_DTO = "userDTO";
    private static final String TOKEN = "token";
    private static final int ID = 1;
    private final TokenDTO tokenDTO = new TokenDTO(TokenType.CHANGE_EMAIL, LocalDateTime.now(), EMAIL, ID);
    private final TokenDTO registerToken = new TokenDTO(TokenType.REGISTER, LocalDateTime.now(), null, ID);
    private final TokenDTO forgotToken = new TokenDTO(TokenType.FORGOT_PASSWORD, LocalDateTime.now(), null, ID);
    private final UserDTO userDTO = new UserDTO("admin", "admin1@admin.de", Role.ADMIN,
            Language.ENGLISH, "admin", "secret1");

    @BeforeEach
    public void setup() {
        LocalDateTime expirationDate = LocalDateTime.now();
        expirationDate = expirationDate.plusHours(1);
        tokenDTO.setValue(VALUE);
        tokenDTO.setType(TokenType.CHANGE_EMAIL);
        tokenDTO.setExpirationDate(expirationDate);
        registerToken.setValue(VALUE);
        registerToken.setExpirationDate(expirationDate);
        forgotToken.setValue(VALUE);
        forgotToken.setExpirationDate(expirationDate);
        userDTO.setId(ID);
        userDTO.setPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
    }

    @AfterEach
    public void resetService() {reset(userService, tokenService);}

    @Test
    public void testValidateToken() throws Exception {
        when(tokenService.findToken(VALUE)).thenReturn(tokenDTO);
        mvc.perform(get("/token")
                .param("value", VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_SUCCESS));
        verify(tokenService).findToken(VALUE);
        verify(userService).updateEmail(ID, EMAIL);
        verify(tokenService).deleteToken(VALUE);
    }

    @Test
    public void testValidateTokenRegister() throws Exception {
        when(tokenService.findToken(VALUE)).thenReturn(registerToken);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        mvc.perform(get("/token")
                .param("value", VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(model().attribute(USER_DTO, is(userDTO)))
                .andExpect(model().attribute(TOKEN, is(VALUE)))
                .andExpect(status().isOk())
                .andExpect(view().name(PASSWORD_SET));
        verify(tokenService).findToken(VALUE);
        verify(userService).getUserById(ID);
    }

    @Test
    public void testValidateTokenForgot() throws Exception {
        when(tokenService.findToken(VALUE)).thenReturn(forgotToken);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        mvc.perform(get("/token")
                .param("value", VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(model().attribute(USER_DTO, is(userDTO)))
                .andExpect(model().attribute(TOKEN, is(VALUE)))
                .andExpect(status().isOk())
                .andExpect(view().name(PASSWORD_SET));
        verify(tokenService).findToken(VALUE);
        verify(userService).getUserById(ID);
    }

    @Test
    public void testValidateTokenUserNotFound() throws Exception {
        when(tokenService.findToken(VALUE)).thenReturn(tokenDTO);
        doThrow(NotFoundException.class).when(userService).updateEmail(ID, EMAIL);
        mvc.perform(get("/token")
                .param("value", VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(tokenService).findToken(VALUE);
        verify(userService).updateEmail(ID, EMAIL);
        verify(tokenService, never()).deleteToken(VALUE);
    }

    @Test
    public void testValidateTokenExpired() throws Exception {
        tokenDTO.setExpirationDate(LocalDateTime.now());
        when(tokenService.findToken(VALUE)).thenReturn(tokenDTO);
        mvc.perform(get("/token")
                .param("value", VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_ERROR));
        verify(tokenService).findToken(VALUE);
        verify(userService, never()).updateEmail(ID, EMAIL);
        verify(tokenService, never()).deleteToken(VALUE);
    }

    @Test
    public void testValidateTokenNotFound() throws Exception {
        when(tokenService.findToken(VALUE)).thenThrow(NotFoundException.class);
        mvc.perform(get("/token")
                .param("value", VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(tokenService).findToken(VALUE);
        verify(userService, never()).updateEmail(ID, EMAIL);
        verify(tokenService, never()).deleteToken(VALUE);
    }

    @Test
    public void testRegisterUser() throws Exception {
        when(tokenService.findToken(VALUE)).thenReturn(registerToken);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        mvc.perform(post("/token/password")
                .flashAttr(USER_DTO, userDTO)
                .param("value", VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_SUCCESS));
        verify(tokenService).findToken(VALUE);
        verify(userService).getUserById(ID);
        verify(userService).saveUser(userDTO);
        verify(tokenService).deleteToken(VALUE);
    }

    @Test
    public void testRegisterUserNotFound() throws Exception {
        when(tokenService.findToken(VALUE)).thenThrow(NotFoundException.class);
        mvc.perform(post("/token/password")
                .flashAttr(USER_DTO, userDTO)
                .param("value", VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(tokenService).findToken(VALUE);
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).deleteToken(anyString());
    }

    @Test
    public void testRegisterUserInvalidPassword() throws Exception {
        userDTO.setPassword(USER_DTO);
        userDTO.setConfirmPassword(USER_DTO);
        mvc.perform(post("/token/password")
                .flashAttr(USER_DTO, userDTO)
                .param("value", VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(PASSWORD_SET));
        verify(tokenService, never()).findToken(anyString());
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).deleteToken(anyString());
    }
}
