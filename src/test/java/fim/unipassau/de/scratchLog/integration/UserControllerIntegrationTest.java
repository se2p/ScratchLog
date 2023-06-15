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

import fim.unipassau.de.scratchLog.MailServerSetter;
import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.application.service.MailService;
import fim.unipassau.de.scratchLog.application.service.ParticipantService;
import fim.unipassau.de.scratchLog.application.service.TokenService;
import fim.unipassau.de.scratchLog.application.service.UserService;
import fim.unipassau.de.scratchLog.spring.authentication.CustomAuthenticationProvider;
import fim.unipassau.de.scratchLog.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratchLog.util.Constants;
import fim.unipassau.de.scratchLog.util.enums.Language;
import fim.unipassau.de.scratchLog.util.enums.Role;
import fim.unipassau.de.scratchLog.util.enums.TokenType;
import fim.unipassau.de.scratchLog.web.controller.UserController;
import fim.unipassau.de.scratchLog.web.dto.PasswordDTO;
import fim.unipassau.de.scratchLog.web.dto.TokenDTO;
import fim.unipassau.de.scratchLog.web.dto.UserBulkDTO;
import fim.unipassau.de.scratchLog.web.dto.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private UserService userService;

    @MockBean
    private ParticipantService participantService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private MailService mailService;

    @MockBean
    private CustomAuthenticationProvider authenticationProvider;

    private static final String USERNAME = "admin";
    private static final String NEW_USERNAME = "admin1";
    private static final String PASSWORD = "adminPassword";
    private static final String VALID_PASSWORD = "V4l1d_P4ssw0rd!";
    private static final String EMAIL = "admin1@admin.de";
    private static final String NEW_EMAIL = "admin@admin.com";
    private static final String REDIRECT = "redirect:/";
    private static final String LOGIN = "login";
    private static final String PROFILE = "profile";
    private static final String PROFILE_EDIT = "profile-edit";
    private static final String PARTICIPANTS_CSV = "participants-csv";
    private static final String PROFILE_REDIRECT = "redirect:/users/profile?name=";
    private static final String EMAIL_REDIRECT = "redirect:/users/profile?update=true&name=";
    private static final String REDIRECT_SUCCESS = "redirect:/?success=true";
    private static final String REDIRECT_INFO = "redirect:/?info=true";
    private static final String REDIRECT_EXPERIMENT = "redirect:/experiment?id=";
    private static final String LAST_ADMIN = "redirect:/users/profile?lastAdmin=true";
    private static final String INVALID = "redirect:/users/profile?invalid=true&name=";
    private static final String USER = "user";
    private static final String PASSWORD_PAGE = "password";
    private static final String PARTICIPANTS_ADD = "participants-add";
    private static final String USER_DTO = "userDTO";
    private static final String PASSWORD_DTO = "passwordDTO";
    private static final String USER_BULK_DTO = "userBulkDTO";
    private static final String ERROR_ATTRIBUTE = "error";
    private static final String NAME = "name";
    private static final String ID_STRING = "1";
    private static final String ID_PARAM = "id";
    private static final String SECRET = "secret";
    private static final String FILETYPE = "text/csv";
    private static final String FILENAME = "users.csv";
    private static final int ID = 1;
    private static final int AMOUNT = 5;
    private final UserDTO userDTO = new UserDTO(USERNAME, EMAIL, Role.ADMIN, Language.ENGLISH, PASSWORD, SECRET);
    private final UserDTO oldDTO = new UserDTO(USERNAME, EMAIL, Role.ADMIN, Language.ENGLISH, PASSWORD, SECRET);
    private final TokenDTO tokenDTO = new TokenDTO(TokenType.CHANGE_EMAIL, LocalDateTime.now(), NEW_EMAIL, ID);
    private final PasswordDTO passwordDTO = new PasswordDTO(PASSWORD);
    private final UserBulkDTO userBulkDTO = new UserBulkDTO(AMOUNT, Language.ENGLISH, USERNAME, true);

    @BeforeEach
    public void setup() {
        oldDTO.setId(ID);
        oldDTO.setActive(true);
        oldDTO.setPassword(PASSWORD);
        oldDTO.setUsername(USERNAME);
        oldDTO.setEmail(EMAIL);
        oldDTO.setRole(Role.ADMIN);
        userDTO.setId(ID);
        userDTO.setActive(true);
        userDTO.setPassword(PASSWORD);
        userDTO.setUsername(USERNAME);
        userDTO.setEmail(EMAIL);
        userDTO.setSecret(SECRET);
        userDTO.setRole(Role.ADMIN);
        userDTO.setNewPassword("");
        userDTO.setConfirmPassword("");
        userDTO.setAttempts(0);
        passwordDTO.setPassword(PASSWORD);
        userBulkDTO.setUsername(USERNAME);
        userBulkDTO.setAmount(AMOUNT);
    }

    @AfterEach
    public void resetService() {reset(userService, tokenService, mailService);}

    @Test
    public void testAuthenticateUser() throws Exception {
        when(userService.authenticateUser(SECRET)).thenReturn(userDTO);
        when(userService.existsParticipant(userDTO.getId(), ID)).thenReturn(true);
        mvc.perform(get("/users/authenticate")
                .param("id", ID_STRING)
                .param("secret", SECRET)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_EXPERIMENT + ID));
        verify(userService).authenticateUser(SECRET);
        verify(userService).existsParticipant(userDTO.getId(), ID);
    }

    @Test
    public void testAuthenticateUserNoParticipant() throws Exception {
        when(userService.authenticateUser(SECRET)).thenReturn(userDTO);
        mvc.perform(get("/users/authenticate")
                .param("id", ID_STRING)
                .param("secret", SECRET)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).authenticateUser(SECRET);
        verify(userService).existsParticipant(userDTO.getId(), ID);
    }

    @Test
    public void testAuthenticateUserNotFound() throws Exception {
        when(userService.authenticateUser(SECRET)).thenThrow(NotFoundException.class);
        mvc.perform(get("/users/authenticate")
                .param("id", ID_STRING)
                .param("secret", SECRET)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).authenticateUser(SECRET);
        verify(userService, never()).existsParticipant(userDTO.getId(), ID);
    }

    @Test
    public void testAuthenticateUserInvalidId() throws Exception {
        mvc.perform(get("/users/authenticate")
                .param("id", "0")
                .param("secret", SECRET)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService, never()).authenticateUser(SECRET);
        verify(userService, never()).existsParticipant(userDTO.getId(), ID);
    }

    @Test
    public void testLoginUser() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.loginUser(userDTO)).thenReturn(true);
        mvc.perform(post("/users/login")
                .flashAttr(USER_DTO, userDTO)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT));
        verify(userService).getUser(USERNAME);
        verify(userService, never()).updateUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(userService).loginUser(userDTO);
    }

    @Test
    public void testLoginUserFalse() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(post("/users/login")
                .flashAttr(USER_DTO, userDTO)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, notNullValue()))
                .andExpect(status().isOk())
                .andExpect(view().name(LOGIN));
        verify(userService).getUser(USERNAME);
        verify(userService, never()).updateUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(userService).loginUser(userDTO);
    }

    @Test
    public void testLoginUserNotFound() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.loginUser(any())).thenThrow(NotFoundException.class);
        mvc.perform(post("/users/login")
                .flashAttr(USER_DTO, userDTO)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, notNullValue()))
                .andExpect(status().isOk())
                .andExpect(view().name(LOGIN));
        verify(userService).getUser(USERNAME);
        verify(userService, never()).updateUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(userService).loginUser(userDTO);
    }

    @Test
    public void testLoginUserMaxAttempts() throws Exception {
        userDTO.setAttempts(4);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(post("/users/login")
                .flashAttr(USER_DTO, userDTO)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, notNullValue()))
                .andExpect(status().isOk())
                .andExpect(view().name(LOGIN));
        verify(userService).getUser(USERNAME);
        verify(userService).updateUser(userDTO);
        verify(tokenService).generateToken(TokenType.DEACTIVATED, "", userDTO.getId());
        verify(userService, never()).loginUser(any());
    }

    @Test
    public void testLoginUserInactive() throws Exception {
        userDTO.setActive(false);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(post("/users/login")
                .flashAttr(USER_DTO, userDTO)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(LOGIN));
        verify(userService).getUser(USERNAME);
        verify(userService, never()).updateUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(userService, never()).loginUser(any());
    }

    @Test
    public void testLoginUserInvalidInput() throws Exception {
        userDTO.setUsername(null);
        userDTO.setPassword(null);
        mvc.perform(post("/users/login")
                .flashAttr(USER_DTO, userDTO)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(LOGIN));
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).updateUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(userService, never()).loginUser(any());
    }

    @Test
    public void testLogoutUser() throws Exception {
        when(userService.existsUser(anyString())).thenReturn(true);
        mvc.perform(get("/users/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT));
        verify(userService).existsUser(anyString());
    }

    @Test
    public void testLogoutUserNonExistent() throws Exception {
        mvc.perform(get("/users/logout")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).existsUser(anyString());
    }

    @Test
    public void testGetAddUser() throws Exception {
        mvc.perform(get("/users/add")
                .flashAttr(USER_DTO, new UserDTO())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(USER));
    }

    @Test
    public void testAddUser() throws Exception {
        MailServerSetter.setMailServer(true);
        userDTO.setId(null);
        when(userService.saveUser(userDTO)).thenReturn(oldDTO);
        when(tokenService.generateToken(TokenType.REGISTER, null, oldDTO.getId())).thenReturn(tokenDTO);
        when(mailService.sendEmail(anyString(), anyString(), any(), anyString())).thenReturn(true);
        mvc.perform(post("/users/add")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_SUCCESS));
        verify(userService).existsUser(userDTO.getUsername());
        verify(userService).existsEmail(userDTO.getEmail());
        verify(userService).saveUser(userDTO);
        verify(tokenService).generateToken(TokenType.REGISTER, null, oldDTO.getId());
        verify(mailService).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddUserEmailNotSent() throws Exception {
        MailServerSetter.setMailServer(true);
        userDTO.setId(null);
        when(userService.saveUser(userDTO)).thenReturn(oldDTO);
        when(tokenService.generateToken(TokenType.REGISTER, null, oldDTO.getId())).thenReturn(tokenDTO);
        mvc.perform(post("/users/add")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).existsUser(userDTO.getUsername());
        verify(userService).existsEmail(userDTO.getEmail());
        verify(userService).saveUser(userDTO);
        verify(tokenService).generateToken(TokenType.REGISTER, null, oldDTO.getId());
        verify(mailService).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddUserNoMailServer() throws Exception {
        MailServerSetter.setMailServer(false);
        userDTO.setId(null);
        when(userService.saveUser(userDTO)).thenReturn(oldDTO);
        when(tokenService.generateToken(TokenType.REGISTER, null, oldDTO.getId())).thenReturn(tokenDTO);
        when(mailService.sendEmail(anyString(), anyString(), any(), anyString())).thenReturn(true);
        mvc.perform(post("/users/add")
                        .flashAttr(USER_DTO, userDTO)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(PROFILE_REDIRECT + oldDTO.getUsername()));
        verify(userService).existsUser(userDTO.getUsername());
        verify(userService).existsEmail(userDTO.getEmail());
        verify(userService).saveUser(userDTO);
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddUserEmailInvalidUsernameExists() throws Exception {
        userDTO.setId(null);
        userDTO.setEmail(USERNAME);
        when(userService.existsUser(userDTO.getUsername())).thenReturn(true);
        mvc.perform(post("/users/add")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(USER));
        verify(userService).existsUser(userDTO.getUsername());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testAddUserUsernameInvalidEmailExists() throws Exception {
        userDTO.setId(null);
        userDTO.setUsername("ad");
        when(userService.existsEmail(userDTO.getEmail())).thenReturn(true);
        mvc.perform(post("/users/add")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(USER));
        verify(userService, never()).existsUser(anyString());
        verify(userService).existsEmail(userDTO.getEmail());
        verify(userService, never()).saveUser(any());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testGetAddParticipants() throws Exception {
        MailServerSetter.setMailServer(false);
        mvc.perform(get("/users/bulk")
                        .flashAttr(USER_BULK_DTO, userBulkDTO)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(PARTICIPANTS_ADD));
    }

    @Test
    public void testGetAddParticipantsMailServer() throws Exception {
        MailServerSetter.setMailServer(true);
        mvc.perform(get("/users/bulk")
                        .flashAttr(USER_BULK_DTO, userBulkDTO)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT));
    }

    @Test
    public void testAddParticipants() throws Exception {
        mvc.perform(post("/users/bulk")
                        .flashAttr(USER_BULK_DTO, userBulkDTO)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_SUCCESS))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, nullValue()));
        verify(userService).findValidNumberForUsername(userBulkDTO.getUsername());
        verify(userService, never()).findLastId();
        verify(userService, times(AMOUNT)).existsUser(anyString());
        verify(userService, times(AMOUNT)).saveUser(any());
    }

    @Test
    public void testAddParticipantsUsernameExists() throws Exception {
        List<String> existingNames = List.of("admin5");
        when(userService.findValidNumberForUsername(userBulkDTO.getUsername())).thenReturn(1);
        when(userService.existsUser(existingNames.get(0))).thenReturn(true);
        mvc.perform(post("/users/bulk")
                        .flashAttr(USER_BULK_DTO, userBulkDTO)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(PARTICIPANTS_ADD))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, is(existingNames)));
        verify(userService).findValidNumberForUsername(userBulkDTO.getUsername());
        verify(userService, never()).findLastId();
        verify(userService, times(AMOUNT)).existsUser(anyString());
        verify(userService, times(AMOUNT - existingNames.size())).saveUser(any());
    }

    @Test
    public void testAddParticipantsInvalidUsername() throws Exception {
        userBulkDTO.setUsername("ad");
        mvc.perform(post("/users/bulk")
                        .flashAttr(USER_BULK_DTO, userBulkDTO)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(PARTICIPANTS_ADD))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, nullValue()));
        verify(userService, never()).findValidNumberForUsername(anyString());
        verify(userService, never()).findLastId();
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).saveUser(any());
    }

    @Test
    public void testAddParticipantsInvalidAmount() throws Exception {
        userBulkDTO.setAmount(-1);
        mvc.perform(post("/users/bulk")
                        .flashAttr(USER_BULK_DTO, userBulkDTO)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR))
                .andExpect(model().attribute(ERROR_ATTRIBUTE, nullValue()));
        verify(userService, never()).findValidNumberForUsername(anyString());
        verify(userService, never()).findLastId();
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).saveUser(any());
    }

    @Test
    public void testGetCSVParticipants() throws Exception {
        mvc.perform(get("/users/csv"))
                .andExpect(status().isOk())
                .andExpect(view().name(PARTICIPANTS_CSV));
    }

    @Test
    public void testAddCSVParticipants() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", FILENAME, FILETYPE,
                new ClassPathResource("users.csv").getInputStream());
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(multipart("/users/csv")
                        .file(file)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk());
        verify(userService, times(2)).existsUser(anyString());
        verify(userService, times(2)).existsEmail(anyString());
        verify(userService, times(2)).encodePassword(anyString());
        verify(userService).saveUsers(any());
    }

    @Test
    public void testAddCSVParticipantsInvalidAttributes() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", FILENAME, FILETYPE,
                new ClassPathResource("usersInvalid.csv").getInputStream());
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(multipart("/users/csv")
                        .file(file)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(PARTICIPANTS_CSV));
        verify(userService).existsUser(anyString());
        verify(userService).existsEmail(anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(userService, never()).saveUsers(any());
    }

    @Test
    public void testAddCSVParticipantsInvalidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", FILENAME, FILENAME,
                new ClassPathResource("usersInvalid.csv").getInputStream());
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(multipart("/users/csv")
                        .file(file)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(view().name(PARTICIPANTS_CSV));
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(userService, never()).saveUsers(any());
    }

    @Test
    public void testPasswordReset() throws Exception {
        MailServerSetter.setMailServer(true);
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(userService.getUserByEmail(userDTO.getEmail())).thenReturn(userDTO);
        when(tokenService.generateToken(TokenType.FORGOT_PASSWORD, null, userDTO.getId())).thenReturn(tokenDTO);
        when(mailService.sendEmail(anyString(), anyString(), any(), anyString())).thenReturn(true);
        mvc.perform(post("/users/reset")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_INFO));
        verify(userService).getUser(userDTO.getUsername());
        verify(userService).getUserByEmail(userDTO.getEmail());
        verify(tokenService).generateToken(TokenType.FORGOT_PASSWORD, null, userDTO.getId());
        verify(mailService).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testPasswordResetMailNotSent() throws Exception {
        MailServerSetter.setMailServer(true);
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(userService.getUserByEmail(userDTO.getEmail())).thenReturn(userDTO);
        when(tokenService.generateToken(TokenType.FORGOT_PASSWORD, null, userDTO.getId())).thenReturn(tokenDTO);
        mvc.perform(post("/users/reset")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_INFO));
        verify(userService).getUser(userDTO.getUsername());
        verify(userService).getUserByEmail(userDTO.getEmail());
        verify(tokenService).generateToken(TokenType.FORGOT_PASSWORD, null, userDTO.getId());
        verify(mailService).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testPasswordResetNoMailServer() throws Exception {
        MailServerSetter.setMailServer(false);
        mvc.perform(post("/users/reset")
                        .flashAttr(USER_DTO, userDTO)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).getUserByEmail(anyString());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testPasswordResetUsersNotEqual() throws Exception {
        MailServerSetter.setMailServer(true);
        oldDTO.setId(ID + 1);
        when(userService.getUser(userDTO.getUsername())).thenReturn(userDTO);
        when(userService.getUserByEmail(userDTO.getEmail())).thenReturn(oldDTO);
        mvc.perform(post("/users/reset")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_INFO));
        verify(userService).getUser(userDTO.getUsername());
        verify(userService).getUserByEmail(userDTO.getEmail());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    public void testPasswordResetUsernameNotFound() throws Exception {
        MailServerSetter.setMailServer(true);
        when(userService.getUser(userDTO.getUsername())).thenThrow(NotFoundException.class);
        mvc.perform(post("/users/reset")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_INFO));
        verify(userService).getUser(userDTO.getUsername());
        verify(userService, never()).getUserByEmail(anyString());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(mailService, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testGetProfile() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(get("/users/profile")
                .param(NAME, USERNAME)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute(PASSWORD_DTO, notNullValue()))
                .andExpect(model().attribute(USER_DTO, allOf(
                        hasProperty("id", is(userDTO.getId())),
                        hasProperty("username", is(USERNAME))
                )))
                .andExpect(view().name(PROFILE));
        verify(userService).getUser(USERNAME);
        verify(participantService, never()).getExperimentInfoForParticipant(userDTO.getId());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testGetProfileParticipant() throws Exception {
        HashMap<Integer, String> experiments = new HashMap<>();
        experiments.put(ID, "Title");
        userDTO.setRole(Role.PARTICIPANT);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(participantService.getExperimentInfoForParticipant(userDTO.getId())).thenReturn(experiments);
        mvc.perform(get("/users/profile")
                .param(NAME, USERNAME)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute(PASSWORD_DTO, notNullValue()))
                .andExpect(model().attribute("experiments", is(experiments)))
                .andExpect(model().attribute(USER_DTO, allOf(
                        hasProperty("id", is(userDTO.getId())),
                        hasProperty("username", is(USERNAME))
                )))
                .andExpect(view().name(PROFILE));
        verify(userService).getUser(USERNAME);
        verify(participantService).getExperimentInfoForParticipant(userDTO.getId());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testGetProfileNotFound() throws Exception {
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        mvc.perform(get("/users/profile")
                .param(NAME, USERNAME)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).getUser(USERNAME);
        verify(participantService, never()).getExperimentInfoForParticipant(userDTO.getId());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testGetProfileOwnProfile() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(get("/users/profile")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute(PASSWORD_DTO, notNullValue()))
                .andExpect(model().attribute(USER_DTO, allOf(
                        hasProperty("id", is(userDTO.getId())),
                        hasProperty("username", is(USERNAME))
                )))
                .andExpect(view().name(PROFILE));
        verify(userService).getUser(USERNAME);
        verify(participantService, never()).getExperimentInfoForParticipant(userDTO.getId());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"PARTICIPANT"})
    public void testGetProfileUserParticipant() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(get("/users/profile")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute(PASSWORD_DTO, notNullValue()))
                .andExpect(model().attribute(USER_DTO, allOf(
                        hasProperty("id", is(userDTO.getId())),
                        hasProperty("username", is(USERNAME))
                )))
                .andExpect(view().name(PROFILE));
        verify(userService).getUser(USERNAME);
        verify(participantService, never()).getExperimentInfoForParticipant(anyInt());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"PARTICIPANT"})
    public void testGetProfileUserParticipantUsernameNotNull() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(get("/users/profile")
                .param(NAME, USERNAME + 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute(PASSWORD_DTO, notNullValue()))
                .andExpect(model().attribute(USER_DTO, allOf(
                        hasProperty("id", is(userDTO.getId())),
                        hasProperty("username", is(USERNAME))
                )))
                .andExpect(view().name(PROFILE));
        verify(userService).getUser(userDTO.getUsername());
        verify(participantService, never()).getExperimentInfoForParticipant(anyInt());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testGetEditProfileForm() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(get("/users/edit")
                .param(NAME, USERNAME)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute(USER_DTO, allOf(
                        hasProperty("id", is(userDTO.getId())),
                        hasProperty("username", is(USERNAME)),
                        hasProperty("email", is(EMAIL))
                )))
                .andExpect(view().name(PROFILE_EDIT));
        verify(userService).getUser(USERNAME);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testGetEditProfileFormNotFound() throws Exception {
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        mvc.perform(get("/users/edit")
                .param(NAME, USERNAME)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).getUser(USERNAME);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testGetEditProfileFormUsernameNull() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(get("/users/edit")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute(USER_DTO, allOf(
                        hasProperty("id", is(userDTO.getId())),
                        hasProperty("username", is(USERNAME)),
                        hasProperty("email", is(EMAIL))
                )))
                .andExpect(view().name(PROFILE_EDIT));
        verify(userService).getUser(USERNAME);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testGetEditProfileFormUsernameBlank() throws Exception {
        userDTO.setEmail(null);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(get("/users/edit")
                .param(NAME, "  ")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute(USER_DTO, allOf(
                        hasProperty("id", is(userDTO.getId())),
                        hasProperty("username", is(USERNAME)),
                        hasProperty("email", nullValue())
                )))
                .andExpect(view().name(PROFILE_EDIT));
        verify(userService).getUser(USERNAME);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testGetEditProfileFormUsernameNotFound() throws Exception {
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        mvc.perform(get("/users/edit")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).getUser(USERNAME);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"PARTICIPANT"})
    public void testGetEditProfileFormUserParticipant() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(get("/users/edit")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute(USER_DTO, allOf(
                        hasProperty("id", is(userDTO.getId())),
                        hasProperty("username", is(USERNAME)),
                        hasProperty("email", is(EMAIL))
                )))
                .andExpect(view().name(PROFILE_EDIT));
        verify(userService).getUser(USERNAME);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"PARTICIPANT"})
    public void testGetEditProfileFormUserParticipantUsernameNotNull() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(get("/users/edit")
                .param(NAME, USERNAME + 1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(model().attribute(USER_DTO, allOf(
                        hasProperty("id", is(userDTO.getId())),
                        hasProperty("username", is(USERNAME)),
                        hasProperty("email", is(EMAIL))
                )))
                .andExpect(view().name(PROFILE_EDIT));
        verify(userService).getUser(USERNAME);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testUpdateUser() throws Exception {
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        when(userService.getUser(USERNAME)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(PROFILE_REDIRECT + USERNAME));
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(userService).updateUser(oldDTO);
        verify(userService).getUser(USERNAME);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testUpdateUserChangeUsernameOwnProfile() throws Exception {
        userDTO.setUsername(NEW_USERNAME);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        when(userService.getUser(USERNAME)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(PROFILE_REDIRECT + NEW_USERNAME));
        verify(userService).existsUser(NEW_USERNAME);
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider).authenticate(any());
        verify(userService).updateUser(userDTO);
        verify(userService).getUser(USERNAME);
    }

    @Test
    @WithMockUser(username = PROFILE, roles = {"ADMIN"})
    public void testUpdateUserChangePassword() throws Exception {
        userDTO.setNewPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        when(userService.encodePassword(VALID_PASSWORD)).thenReturn(VALID_PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        when(userService.getUser(PROFILE)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(PROFILE_REDIRECT + USERNAME));
        verify(userService, never()).existsUser(NEW_USERNAME);
        verify(userService, never()).existsEmail(anyString());
        verify(userService).matchesPassword(anyString(), anyString());
        verify(userService).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService).updateUser(oldDTO);
        verify(userService).getUser(PROFILE);
    }

    @Test
    @WithMockUser(username = PROFILE, roles = {"ADMIN"})
    public void testUpdateUserChangeEmail() throws Exception {
        MailServerSetter.setMailServer(true);
        userDTO.setEmail(NEW_EMAIL);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        when(tokenService.generateToken(TokenType.CHANGE_EMAIL, NEW_EMAIL, ID)).thenReturn(tokenDTO);
        when(mailService.sendEmail(anyString(), any(), any(), anyString())).thenReturn(true);
        when(userService.getUser(PROFILE)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(EMAIL_REDIRECT + USERNAME));
        verify(userService, never()).existsUser(anyString());
        verify(userService).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService).updateUser(oldDTO);
        verify(mailService).sendEmail(anyString(), any(), any(), anyString());
        verify(tokenService).generateToken(TokenType.CHANGE_EMAIL, NEW_EMAIL, ID);
        verify(userService).getUser(PROFILE);
    }

    @Test
    @WithMockUser(username = PROFILE, roles = {"ADMIN"})
    public void testUpdateUserChangeEmailNoMailServer() throws Exception {
        MailServerSetter.setMailServer(false);
        userDTO.setEmail(NEW_EMAIL);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        when(tokenService.generateToken(TokenType.CHANGE_EMAIL, NEW_EMAIL, ID)).thenReturn(tokenDTO);
        when(mailService.sendEmail(anyString(), any(), any(), anyString())).thenReturn(true);
        when(userService.getUser(PROFILE)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                        .flashAttr(USER_DTO, userDTO)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(PROFILE_REDIRECT + USERNAME));
        verify(userService, never()).existsUser(anyString());
        verify(userService).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService).updateUser(oldDTO);
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(tokenService, never()).generateToken(any(), anyString(), anyInt());
        verify(userService).getUser(PROFILE);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"PARTICIPANT"})
    public void testUpdateUserParticipantChangeEmailUpdatePassword() throws Exception {
        MailServerSetter.setMailServer(true);
        userDTO.setUsername(null);
        userDTO.setPassword(PASSWORD);
        userDTO.setNewPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        userDTO.setEmail(NEW_EMAIL);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        when(tokenService.generateToken(TokenType.CHANGE_EMAIL, NEW_EMAIL, ID)).thenReturn(tokenDTO);
        when(mailService.sendEmail(anyString(), any(), any(), anyString())).thenReturn(true);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        when(userService.encodePassword(VALID_PASSWORD)).thenReturn(VALID_PASSWORD);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(EMAIL_REDIRECT + USERNAME));
        verify(userService, never()).existsUser(anyString());
        verify(userService).existsEmail(anyString());
        verify(userService).matchesPassword(anyString(), anyString());
        verify(userService).encodePassword(anyString());
        verify(authenticationProvider).authenticate(any());
        verify(userService).updateUser(oldDTO);
        verify(mailService).sendEmail(anyString(), any(), any(), anyString());
        verify(tokenService).generateToken(TokenType.CHANGE_EMAIL, NEW_EMAIL, ID);
        verify(userService, never()).getUser(anyString());
    }

    @Test
    @WithMockUser(username = PROFILE, roles = {"ADMIN"})
    public void testUpdateUserChangeEmailNotSent() throws Exception {
        MailServerSetter.setMailServer(true);
        userDTO.setEmail(NEW_EMAIL);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.updateUser(oldDTO)).thenReturn(oldDTO);
        when(tokenService.generateToken(TokenType.CHANGE_EMAIL, NEW_EMAIL, ID)).thenReturn(tokenDTO);
        when(userService.getUser(PROFILE)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(PROFILE_REDIRECT + USERNAME));
        verify(userService, never()).existsUser(anyString());
        verify(userService).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService).updateUser(oldDTO);
        verify(mailService).sendEmail(anyString(), any(), any(), anyString());
        verify(tokenService).generateToken(TokenType.CHANGE_EMAIL, NEW_EMAIL, ID);
        verify(userService).getUser(PROFILE);
    }

    @Test
    @WithMockUser(username = PROFILE, roles = {"ADMIN"})
    public void testUpdateUserNewEmailExistsUsernameExistsPasswordsNotMatching() throws Exception {
        userDTO.setNewPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        userDTO.setUsername(NEW_USERNAME);
        userDTO.setEmail(NEW_EMAIL);
        when(userService.existsUser(NEW_USERNAME)).thenReturn(true);
        when(userService.existsEmail(NEW_EMAIL)).thenReturn(true);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.getUser(PROFILE)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(PROFILE_EDIT));
        verify(userService).existsUser(NEW_USERNAME);
        verify(userService).existsEmail(NEW_EMAIL);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).updateUser(any());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(userService).getUser(PROFILE);
    }

    @Test
    @WithMockUser(username = PROFILE, roles = {"ADMIN"})
    public void testUpdateUserChangePasswordInvalidNoInputPassword() throws Exception {
        userDTO.setPassword("");
        userDTO.setNewPassword(PASSWORD);
        userDTO.setConfirmPassword(PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.getUser(PROFILE)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(PROFILE_EDIT));
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).updateUser(any());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(userService).getUser(PROFILE);
    }

    @Test
    @WithMockUser(username = PROFILE, roles = {"ADMIN"})
    public void testUpdateUserChangeEmailInvalid() throws Exception {
        userDTO.setEmail(PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.getUser(PROFILE)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(PROFILE_EDIT));
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).updateUser(any());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(userService).getUser(PROFILE);
    }

    @Test
    @WithMockUser(username = PROFILE, roles = {"ADMIN"})
    public void testUpdateUserChangeUsernameInvalid() throws Exception {
        userDTO.setUsername("");
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.getUser(PROFILE)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(PROFILE_EDIT));
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).updateUser(any());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
        verify(userService).getUser(PROFILE);
    }

    @Test
    @WithMockUser(username = PROFILE, roles = {"ADMIN"})
    public void testUpdateUserNewEmailBlank() throws Exception {
        userDTO.setEmail("");
        userDTO.setNewPassword(null);
        userDTO.setConfirmPassword(null);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(PROFILE_EDIT));
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).updateUser(any());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
    }

    @Test
    public void testUpdateUserNotFound() throws Exception {
        when(userService.getUserById(ID)).thenThrow(NotFoundException.class);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).updateUser(any());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"PARTICIPANT"})
    public void testUpdateUserParticipantUsernameNotNull() throws Exception {
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).updateUser(any());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"PARTICIPANT"})
    public void testUpdateUserParticipantUsersNotEqual() throws Exception {
        userDTO.setUsername(null);
        oldDTO.setId(ID + 1);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        mvc.perform(post("/users/update")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService, never()).existsUser(anyString());
        verify(userService, never()).existsEmail(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(authenticationProvider, never()).authenticate(any());
        verify(userService, never()).updateUser(any());
        verify(mailService, never()).sendEmail(anyString(), any(), any(), anyString());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testDeleteUser() throws Exception {
        oldDTO.setId(ID + 1);
        oldDTO.setRole(Role.PARTICIPANT);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        mvc.perform(post("/users/delete")
                .param(ID_PARAM, ID_STRING)
                .flashAttr(PASSWORD_DTO, passwordDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_SUCCESS));
        verify(userService).getUser(USERNAME);
        verify(userService).getUserById(ID);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(userService, never()).isLastAdmin();
        verify(userService).deleteUser(ID + 1);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testDeleteUserAdmin() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        mvc.perform(post("/users/delete")
                .param(ID_PARAM, ID_STRING)
                .flashAttr(PASSWORD_DTO, passwordDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT_SUCCESS));
        verify(userService).getUser(USERNAME);
        verify(userService).getUserById(ID);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(userService).isLastAdmin();
        verify(userService).deleteUser(ID);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testDeleteUserLastAdmin() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        when(userService.isLastAdmin()).thenReturn(true);
        mvc.perform(post("/users/delete")
                .param(ID_PARAM, ID_STRING)
                .flashAttr(PASSWORD_DTO, passwordDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(LAST_ADMIN));
        verify(userService).getUser(USERNAME);
        verify(userService).getUserById(ID);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(userService).isLastAdmin();
        verify(userService, never()).deleteUser(anyInt());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testDeleteUserInvalidPassword() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        mvc.perform(post("/users/delete")
                .param(ID_PARAM, ID_STRING)
                .flashAttr(PASSWORD_DTO, passwordDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(INVALID + USERNAME));
        verify(userService).getUser(USERNAME);
        verify(userService).getUserById(ID);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(userService, never()).isLastAdmin();
        verify(userService, never()).deleteUser(anyInt());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testDeleteUserNotFound() throws Exception {
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.getUserById(ID)).thenThrow(NotFoundException.class);
        mvc.perform(post("/users/delete")
                .param(ID_PARAM, ID_STRING)
                .flashAttr(PASSWORD_DTO, passwordDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).getUser(USERNAME);
        verify(userService).getUserById(ID);
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).isLastAdmin();
        verify(userService, never()).deleteUser(anyInt());
    }

    @Test
    public void testDeleteUserInvalidId() throws Exception {
        mvc.perform(post("/users/delete")
                .param(ID_PARAM, USERNAME)
                .flashAttr(PASSWORD_DTO, passwordDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).getUserById(anyInt());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).isLastAdmin();
        verify(userService, never()).deleteUser(anyInt());
    }

    @Test
    public void testChangeActiveStatusDeactivate() throws Exception {
        userDTO.setRole(Role.PARTICIPANT);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        mvc.perform(get("/users/active")
                .param("id", ID_STRING)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(PROFILE_REDIRECT + userDTO.getUsername()));
        verify(userService).getUserById(ID);
        verify(userService).updateUser(userDTO);
    }

    @Test
    public void testChangeActiveStatusActivate() throws Exception {
        userDTO.setRole(Role.PARTICIPANT);
        userDTO.setActive(false);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        mvc.perform(get("/users/active")
                .param("id", ID_STRING)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(PROFILE_REDIRECT + userDTO.getUsername()));
        verify(userService).getUserById(ID);
        verify(userService).updateUser(userDTO);
    }

    @Test
    public void testChangeActiveStatusAdmin() throws Exception {
        when(userService.getUserById(ID)).thenReturn(userDTO);
        mvc.perform(get("/users/active")
                .param("id", ID_STRING)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).getUserById(ID);
        verify(userService, never()).updateUser(userDTO);
    }

    @Test
    public void testChangeActiveStatusNotFound() throws Exception {
        when(userService.getUserById(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/users/active")
                .param("id", ID_STRING)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).getUserById(ID);
        verify(userService, never()).updateUser(userDTO);
    }

    @Test
    public void testChangeActiveStatusInvalidId() throws Exception {
        mvc.perform(get("/users/active")
                .param("id", USERNAME)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService, never()).getUserById(ID);
        verify(userService, never()).updateUser(userDTO);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testGetPasswordResetForm() throws Exception {
        userDTO.setRole(Role.PARTICIPANT);
        when(userService.getUserById(ID)).thenReturn(userDTO);
        mvc.perform(get("/users/forgot")
                .param("id", ID_STRING)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(model().attribute(USER_DTO, is(userDTO)))
                .andExpect(status().isOk())
                .andExpect(view().name(PASSWORD_PAGE));
        verify(userService).getUserById(ID);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testGetPasswordResetFormAdmin() throws Exception {
        when(userService.getUserById(ID)).thenReturn(userDTO);
        mvc.perform(get("/users/forgot")
                        .param("id", ID_STRING)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).getUserById(ID);
    }

    @Test
    public void testGetPasswordResetFormNoAdmin() throws Exception {
        when(userService.getUserById(ID)).thenReturn(userDTO);
        mvc.perform(get("/users/forgot")
                .param("id", ID_STRING)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).getUserById(ID);
    }

    @Test
    public void testGetPasswordResetFormNotFound() throws Exception {
        when(userService.getUserById(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/users/forgot")
                .param("id", ID_STRING)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).getUserById(ID);
    }

    @Test
    public void testGetPasswordResetFormInvalidId() throws Exception {
        mvc.perform(get("/users/forgot")
                .param("id", "0")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService, never()).getUserById(anyInt());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testResetPassword() throws Exception {
        oldDTO.setRole(Role.PARTICIPANT);
        userDTO.setNewPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        when(userService.matchesPassword(PASSWORD, PASSWORD)).thenReturn(true);
        when(userService.encodePassword(VALID_PASSWORD)).thenReturn(VALID_PASSWORD);
        mvc.perform(post("/users/forgot")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(PROFILE_REDIRECT + USERNAME));
        verify(userService).getUserById(ID);
        verify(userService).getUser(USERNAME);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(userService).encodePassword(VALID_PASSWORD);
        verify(userService).saveUser(oldDTO);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testResetPasswordPasswordsNotMatching() throws Exception {
        oldDTO.setRole(Role.PARTICIPANT);
        userDTO.setNewPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.getUser(USERNAME)).thenReturn(userDTO);
        mvc.perform(post("/users/forgot")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(view().name(PASSWORD_PAGE));
        verify(userService).getUserById(ID);
        verify(userService).getUser(USERNAME);
        verify(userService).matchesPassword(PASSWORD, PASSWORD);
        verify(userService, never()).encodePassword(anyString());
        verify(userService, never()).saveUser(any());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testResetPasswordAdminNotFound() throws Exception {
        oldDTO.setRole(Role.PARTICIPANT);
        userDTO.setNewPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        when(userService.getUser(USERNAME)).thenThrow(NotFoundException.class);
        mvc.perform(post("/users/forgot")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).getUserById(ID);
        verify(userService).getUser(USERNAME);
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(userService, never()).saveUser(any());
    }

    @Test
    public void testResetPasswordUserNotAdmin() throws Exception {
        userDTO.setNewPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        mvc.perform(post("/users/forgot")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).getUserById(ID);
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(userService, never()).saveUser(any());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = {"ADMIN"})
    public void testResetPasswordUserAdmin() throws Exception {
        userDTO.setNewPassword(VALID_PASSWORD);
        userDTO.setConfirmPassword(VALID_PASSWORD);
        when(userService.getUserById(ID)).thenReturn(oldDTO);
        mvc.perform(post("/users/forgot")
                        .flashAttr(USER_DTO, userDTO)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).getUserById(ID);
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(userService, never()).saveUser(any());
    }

    @Test
    public void testResetPasswordUserNotFound() throws Exception {
        when(userService.getUserById(ID)).thenThrow(NotFoundException.class);
        mvc.perform(post("/users/forgot")
                .flashAttr(USER_DTO, userDTO)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(Constants.ERROR));
        verify(userService).getUserById(ID);
        verify(userService, never()).getUser(anyString());
        verify(userService, never()).matchesPassword(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(userService, never()).saveUser(any());
    }

}
