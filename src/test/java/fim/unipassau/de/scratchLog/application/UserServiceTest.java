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
package fim.unipassau.de.scratchLog.application;

import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.application.exception.StoreException;
import fim.unipassau.de.scratchLog.application.service.UserService;
import fim.unipassau.de.scratchLog.persistence.entity.Experiment;
import fim.unipassau.de.scratchLog.persistence.entity.Participant;
import fim.unipassau.de.scratchLog.persistence.entity.User;
import fim.unipassau.de.scratchLog.persistence.projection.UserProjection;
import fim.unipassau.de.scratchLog.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratchLog.persistence.repository.UserRepository;
import fim.unipassau.de.scratchLog.util.enums.Language;
import fim.unipassau.de.scratchLog.util.enums.Role;
import fim.unipassau.de.scratchLog.web.dto.UserDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private static final String USERNAME = "admin";
    private static final String BLANK = "   ";
    private static final String PASSWORD = "admin1";
    private static final String EMAIL = "admin1@admin.de";
    private static final String SECRET = "secret";
    private static final String GUI_URL = "scratch";
    private static final int ID = 1;
    private static final long INACTIVE_DAYS = 90;
    private final Experiment experiment = new Experiment(ID, "title", "description", "info", "postscript", true,
            false, GUI_URL);
    private final User user1 = new User(USERNAME, EMAIL, Role.ADMIN, Language.ENGLISH, PASSWORD, SECRET);
    private final User user2 = new User("participant1", "part1@part.de", Role.PARTICIPANT, Language.ENGLISH,
            PASSWORD, SECRET);
    private final User user3 = new User("participant2", "part2@part.de", Role.PARTICIPANT, Language.ENGLISH,
            PASSWORD, null);
    private final User user4 = new User("participant3", "part3@part.de", Role.PARTICIPANT, Language.ENGLISH,
            PASSWORD, null);
    private final UserDTO userDTO = new UserDTO(USERNAME, EMAIL, Role.ADMIN, Language.ENGLISH, PASSWORD, SECRET);
    private final Participant participant1 = new Participant(user2, experiment, null, null);
    private final Participant participant2 = new Participant(user3, experiment, null, null);
    private final Participant participant3 = new Participant(user4, experiment, null, null);
    private List<User> admins;
    private List<Participant> participants;

    @BeforeEach
    public void setup() {
        admins = new ArrayList<>();
        participants = new ArrayList<>();
        user2.setId(2);
        user3.setId(3);
        user4.setId(4);
        admins.add(user1);
        participants.add(participant1);
        participants.add(participant2);
        participants.add(participant3);
        user1.setId(ID);
        user1.setAttempts(0);
        user1.setLastLogin(LocalDateTime.now());
        user2.setActive(false);
        user2.setSecret(SECRET);
        user2.setLastLogin(null);
        userDTO.setId(ID);
        userDTO.setUsername(USERNAME);
        userDTO.setPassword(PASSWORD);
    }

    @Test
    public void testExistsUser() {
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);
        assertTrue(userService.existsUser(USERNAME));
        verify(userRepository).existsByUsername(USERNAME);
    }

    @Test
    public void testExistsUserFalse() {
        assertFalse(userService.existsUser(USERNAME));
        verify(userRepository).existsByUsername(USERNAME);
    }

    @Test
    public void testExistsUserUsernameNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.existsUser(null)
        );
        verify(userRepository, never()).existsByUsername(USERNAME);
    }

    @Test
    public void testExistsUserUsernameBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.existsUser(BLANK)
        );
        verify(userRepository, never()).existsByUsername(USERNAME);
    }

    @Test
    public void testExistsEmail() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);
        assertTrue(userService.existsEmail(EMAIL));
        verify(userRepository).existsByEmail(EMAIL);
    }

    @Test
    public void testExistsEmailFalse() {
        assertFalse(userService.existsEmail(EMAIL));
        verify(userRepository).existsByEmail(EMAIL);
    }

    @Test
    public void testExistsEmailNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.existsEmail(null)
        );
        verify(userRepository, never()).existsByEmail(EMAIL);
    }

    @Test
    public void testExistsEmailBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.existsEmail(BLANK)
        );
        verify(userRepository, never()).existsByEmail(EMAIL);
    }

    @Test
    public void testExistsParticipant() {
        when(participantRepository.existsByUserAndExperiment(any(), any())).thenReturn(true);
        assertTrue(userService.existsParticipant(ID, ID));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).existsByUserAndExperiment(any(), any());
    }

    @Test
    public void testExistsParticipantEntityNotFound() {
        when(participantRepository.existsByUserAndExperiment(any(), any())).thenThrow(EntityNotFoundException.class);
        assertFalse(userService.existsParticipant(ID, ID));
        verify(userRepository).getReferenceById(ID);
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).existsByUserAndExperiment(any(), any());
    }

    @Test
    public void testExistsParticipantUserIdInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.existsParticipant(-1, ID)
        );
        verify(userRepository, never()).getReferenceById(ID);
        verify(experimentRepository, never()).getReferenceById(ID);
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
    }

    @Test
    public void testExistsParticipantExperimentIdInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.existsParticipant(ID, 0)
        );
        verify(userRepository, never()).getReferenceById(ID);
        verify(experimentRepository, never()).getReferenceById(ID);
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
    }

    @Test
    public void testSaveUsers() {
        when(userRepository.save(any())).thenReturn(user1, user2);
        List<UserDTO> saved = userService.saveUsers(List.of(userDTO, userDTO));
        assertAll(
                () -> assertEquals(2, saved.size()),
                () -> assertEquals(user1.getId(), saved.get(0).getId()),
                () -> assertEquals(user2.getId(), saved.get(1).getId())
        );
        verify(userRepository, times(2)).save(any());
    }

    @Test
    public void testSaveUser() {
        when(userRepository.save(any())).thenReturn(user1);
        UserDTO saved = userService.saveUser(userDTO);
        assertAll(
                () -> assertEquals(user1.getId(), saved.getId()),
                () -> assertEquals(user1.getUsername(), saved.getUsername()),
                () -> assertEquals(user1.getEmail(), saved.getEmail()),
                () -> assertEquals(user1.getPassword(), saved.getPassword()),
                () -> assertEquals(user1.getSecret(), saved.getSecret()),
                () -> assertEquals(user1.getRole(), saved.getRole()),
                () -> assertEquals(user1.getLanguage(), saved.getLanguage())
        );
        verify(userRepository).save(any());
    }

    @Test
    public void testSaveUserIdNull() {
        user1.setId(null);
        when(userRepository.save(any())).thenReturn(user1);
        assertThrows(StoreException.class,
                () -> userService.saveUser(userDTO)
        );
        verify(userRepository).save(any());
    }

    @Test
    public void testSaveUserUsernameNull() {
        userDTO.setUsername(null);
        assertThrows(IllegalArgumentException.class,
                () -> userService.saveUser(userDTO)
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testSaveUserUsernameBlank() {
        userDTO.setUsername(BLANK);
        assertThrows(IllegalArgumentException.class,
                () -> userService.saveUser(userDTO)
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testGetUser() {
        when(userRepository.findUserByUsername(USERNAME)).thenReturn(Optional.of(user1));
        UserDTO userDTO = userService.getUser(USERNAME);
        assertAll(
                () -> assertEquals(userDTO.getUsername(), user1.getUsername()),
                () -> assertEquals(userDTO.getEmail(), user1.getEmail()),
                () -> assertEquals(userDTO.getRole(), user1.getRole()),
                () -> assertEquals(userDTO.getLanguage(), user1.getLanguage()),
                () -> assertEquals(userDTO.getPassword(), user1.getPassword()),
                () -> assertEquals(userDTO.getSecret(), user1.getSecret())
        );
        verify(userRepository).findUserByUsername(USERNAME);
    }

    @Test
    public void testGetUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> userService.getUser(USERNAME)
        );
        verify(userRepository).findUserByUsername(USERNAME);
    }

    @Test
    public void testGetUserUsernameNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.getUser(null)
        );
        verify(userRepository, never()).findUserByUsername(USERNAME);
    }

    @Test
    public void testGetUserUsernameBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.getUser(BLANK)
        );
        verify(userRepository, never()).findUserByUsername(USERNAME);
    }

    @Test
    public void testGetUserById() {
        when(userRepository.findById(ID)).thenReturn(java.util.Optional.of(user1));
        UserDTO found = userService.getUserById(ID);
        assertAll(
                () -> assertEquals(USERNAME, found.getUsername()),
                () -> assertEquals(EMAIL, found.getEmail()),
                () -> assertEquals(PASSWORD, found.getPassword()),
                () -> assertEquals(Role.ADMIN, found.getRole()),
                () -> assertEquals(Language.ENGLISH, found.getLanguage())
        );
        verify(userRepository).findById(ID);
    }

    @Test
    public void testGetUserByIdEmpty() {
        assertThrows(NotFoundException.class,
                () -> userService.getUserById(ID)
        );
        verify(userRepository).findById(ID);
    }

    @Test
    public void testGetUserByIdInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(0)
        );
        verify(userRepository, never()).findById(anyInt());
    }

    @Test
    public void testGetUserByEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(java.util.Optional.of(user1));
        UserDTO found = userService.getUserByEmail(EMAIL);
        assertAll(
                () -> assertEquals(USERNAME, found.getUsername()),
                () -> assertEquals(EMAIL, found.getEmail()),
                () -> assertEquals(PASSWORD, found.getPassword()),
                () -> assertEquals(Role.ADMIN, found.getRole()),
                () -> assertEquals(Language.ENGLISH, found.getLanguage())
        );
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    public void testGetUserByEmailEmpty() {
        assertThrows(NotFoundException.class,
                () -> userService.getUserByEmail(EMAIL)
        );
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    public void testGetUserByEmailBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserByEmail(BLANK)
        );
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    public void testGetUserByEmailNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserByEmail(null)
        );
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    public void testGetUserByUsernameOrEmail() {
        when(userRepository.findUserByUsernameOrEmail(USERNAME, USERNAME)).thenReturn(Optional.of(user1));
        UserDTO found = userService.getUserByUsernameOrEmail(USERNAME);
        assertAll(
                () -> assertEquals(user1.getId(), found.getId()),
                () -> assertEquals(user1.getUsername(), found.getUsername()),
                () -> assertEquals(user1.getEmail(), found.getEmail())
        );
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
    }

    @Test
    public void testGetUserByUsernameOrEmailUserNull() {
        assertNull(userService.getUserByUsernameOrEmail(USERNAME));
        verify(userRepository).findUserByUsernameOrEmail(USERNAME, USERNAME);
    }

    @Test
    public void testGetUserByUsernameOrEmailBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserByUsernameOrEmail(BLANK)
        );
        verify(userRepository, never()).findUserByUsernameOrEmail(anyString(), anyString());
    }

    @Test
    public void testGetUserByUsernameOrEmailNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserByUsernameOrEmail(null)
        );
        verify(userRepository, never()).findUserByUsernameOrEmail(anyString(), anyString());
    }

    @Test
    public void testLoginUser() {
        user1.setAttempts(2);
        LocalDateTime lastLogin = user1.getLastLogin();
        when(userRepository.findUserByUsername(USERNAME)).thenReturn(Optional.of(user1));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        assertAll(
                () -> assertTrue(userService.loginUser(userDTO)),
                () -> assertEquals(0, user1.getAttempts()),
                () -> assertTrue(user1.getLastLogin().isAfter(lastLogin))
        );
        verify(userRepository).findUserByUsername(USERNAME);
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(userRepository).save(user1);
    }

    @Test
    public void testLoginUserPasswordNotMatching() {
        when(userRepository.findUserByUsername(USERNAME)).thenReturn(Optional.of(user1));
        assertAll(
                () -> assertFalse(userService.loginUser(userDTO)),
                () -> assertEquals(1, user1.getAttempts())
        );
        verify(userRepository).findUserByUsername(USERNAME);
        verify(userRepository).save(user1);
        verify(passwordEncoder).matches(anyString(), anyString());
    }

    @Test
    public void testLoginUserPasswordNull() {
        userDTO.setPassword(null);
        when(userRepository.findUserByUsername(USERNAME)).thenReturn(Optional.of(user1));
        assertAll(
                () -> assertFalse(userService.loginUser(userDTO)),
                () -> assertEquals(1, user1.getAttempts())
        );
        verify(userRepository).findUserByUsername(USERNAME);
        verify(userRepository).save(user1);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    public void testLoginUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> userService.loginUser(userDTO)
        );
        verify(userRepository).findUserByUsername(USERNAME);
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    public void testAuthenticateUser() {
        LocalDateTime lastLogin = user1.getLastLogin();
        when(userRepository.findUserBySecret(SECRET)).thenReturn(Optional.of(user1));
        when(userRepository.save(user1)).thenReturn(user1);
        UserDTO authenticated = userService.authenticateUser(SECRET);
        assertAll(
                () -> assertEquals(user1.getId(), authenticated.getId()),
                () -> assertEquals(user1.getUsername(), authenticated.getUsername()),
                () -> assertEquals(user1.getEmail(), authenticated.getEmail()),
                () -> assertEquals(user1.getPassword(), authenticated.getPassword()),
                () -> assertEquals(user1.getSecret(), authenticated.getSecret()),
                () -> assertEquals(user1.getRole(), authenticated.getRole()),
                () -> assertEquals(user1.getLanguage(), authenticated.getLanguage()),
                () -> assertTrue(user1.getLastLogin().isAfter(lastLogin)),
                () -> assertTrue(authenticated.isActive())
        );
        verify(userRepository).findUserBySecret(SECRET);
        verify(userRepository).save(user1);
    }

    @Test
    public void testAuthenticateUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> userService.authenticateUser(SECRET)
        );
        verify(userRepository).findUserBySecret(SECRET);
        verify(userRepository, never()).save(user1);
    }

    @Test
    public void testAuthenticateUserSecretNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.authenticateUser(null)
        );
        verify(userRepository, never()).findUserBySecret(anyString());
        verify(userRepository, never()).save(user1);
    }

    @Test
    public void testAuthenticateUserSecretBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.authenticateUser(BLANK)
        );
        verify(userRepository, never()).findUserBySecret(anyString());
        verify(userRepository, never()).save(user1);
    }

    @Test
    public void testUpdateUser() {
        when(userRepository.save(any())).thenReturn(user1);
        UserDTO saved = userService.updateUser(userDTO);
        assertAll(
                () -> assertEquals(userDTO.getId(), saved.getId()),
                () -> assertEquals(userDTO.getUsername(), saved.getUsername()),
                () -> assertEquals(userDTO.getEmail(), saved.getEmail()),
                () -> assertEquals(userDTO.getRole(), saved.getRole()),
                () -> assertEquals(userDTO.getLanguage(), saved.getLanguage()),
                () -> assertEquals(userDTO.getPassword(), saved.getPassword()),
                () -> assertEquals(userDTO.getSecret(), saved.getSecret()),
                () -> assertFalse(saved.isActive()),
                () -> assertEquals(0, saved.getAttempts())
        );
        verify(userRepository).save(any());
    }

    @Test
    public void testUpdateUserIdNull() {
        userDTO.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(userDTO)
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testUpdateUserIdInvalid() {
        userDTO.setId(0);
        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(userDTO)
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testUpdateEmail() {
        when(userRepository.findById(ID)).thenReturn(java.util.Optional.of(user1));
        assertDoesNotThrow(() -> userService.updateEmail(ID, EMAIL));
        verify(userRepository).save(any());
    }

    @Test
    public void testUpdateEmailNotFound() {
        when(userRepository.findById(ID)).thenReturn(java.util.Optional.empty());
        assertThrows(NotFoundException.class,
                () -> userService.updateEmail(ID, EMAIL)
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testUpdateEmailNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.updateEmail(ID, null)
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testUpdateEmailBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.updateEmail(ID, BLANK)
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testUpdateEmailIdInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.updateEmail(0, EMAIL)
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testDeactivateOldParticipantAccounts() {
        user2.setActive(true);
        user2.setLastLogin(LocalDateTime.now().minusDays(INACTIVE_DAYS));
        when(userRepository.findAllByRoleAndLastLoginBefore(any(), any())).thenReturn(List.of(user2));
        userService.deactivateOldParticipantAccounts();
        assertAll(
                () -> assertFalse(user2.isActive()),
                () -> assertNull(user2.getSecret())
        );
        verify(userRepository).findAllByRoleAndLastLoginBefore(any(), any());
        verify(userRepository).save(user2);
    }

    @Test
    public void testReactivateUserAccount() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findAllByExperimentAndEnd(experiment, null)).thenReturn(participants);
        List<UserDTO> userDTOS = userService.reactivateUserAccounts(ID);
        assertAll(
                () -> assertEquals(3, userDTOS.size()),
                () -> assertTrue(userDTOS.stream().anyMatch(u -> u.getId() == 2)),
                () -> assertTrue(userDTOS.stream().anyMatch(u -> u.getId() == 3)),
                () -> assertTrue(userDTOS.stream().anyMatch(u -> u.getId() == 4)),
                () -> assertTrue(userDTOS.stream().allMatch(UserDTO::isActive)),
                () -> assertTrue(userDTOS.stream().allMatch(u -> u.getSecret() != null))
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findAllByExperimentAndEnd(experiment, null);
        verify(userRepository, times(3)).save(any());
    }

    @Test
    public void testReactivateUserAccountsUserIdNull() {
        user2.setId(null);
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findAllByExperimentAndEnd(experiment, null)).thenReturn(participants);
        assertThrows(IllegalStateException.class,
                () -> userService.reactivateUserAccounts(ID)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findAllByExperimentAndEnd(experiment, null);
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testReactivateUserAccountsEmptyList() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        List<UserDTO> userDTOS = userService.reactivateUserAccounts(ID);
        assertTrue(userDTOS.isEmpty());
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findAllByExperimentAndEnd(experiment, null);
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testReactivateUserAccountsNotFound() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findAllByExperimentAndEnd(experiment,
                null)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> userService.reactivateUserAccounts(ID)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findAllByExperimentAndEnd(experiment, null);
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testReactivateUserAccountsInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.reactivateUserAccounts(0)
        );
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(participantRepository, never()).findAllByExperimentAndEnd(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testFindUnfinishedUsers() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findAllByExperimentAndEnd(experiment, null)).thenReturn(participants);
        List<UserDTO> userDTOS = userService.findUnfinishedUsers(ID);
        assertAll(
                () -> assertEquals(3, userDTOS.size()),
                () -> assertTrue(userDTOS.stream().anyMatch(u -> u.getId() == 2)),
                () -> assertTrue(userDTOS.stream().anyMatch(u -> u.getId() == 3)),
                () -> assertTrue(userDTOS.stream().anyMatch(u -> u.getId() == 4))
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findAllByExperimentAndEnd(experiment, null);
    }

    @Test
    public void testFindUnfinishedUsersNotFound() {
        when(experimentRepository.getReferenceById(ID)).thenReturn(experiment);
        when(participantRepository.findAllByExperimentAndEnd(experiment,
                null)).thenThrow(EntityNotFoundException.class);
        assertThrows(NotFoundException.class,
                () -> userService.findUnfinishedUsers(ID)
        );
        verify(experimentRepository).getReferenceById(ID);
        verify(participantRepository).findAllByExperimentAndEnd(experiment, null);
    }

    @Test
    public void testFindUnfinishedUsersInvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.findUnfinishedUsers(0)
        );
        verify(experimentRepository, never()).getReferenceById(anyInt());
        verify(participantRepository, never()).findAllByExperimentAndEnd(any(), any());
    }

    @Test
    public void testIsLastAdmin() {
        admins.add(new User());
        when(userRepository.findAllByRole(Role.ADMIN)).thenReturn(admins);
        assertFalse(userService.isLastAdmin());
        verify(userRepository).findAllByRole(Role.ADMIN);
    }

    @Test
    public void testIsLastAdminTrue() {
        when(userRepository.findAllByRole(Role.ADMIN)).thenReturn(admins);
        assertTrue(userService.isLastAdmin());
        verify(userRepository).findAllByRole(Role.ADMIN);
    }

    @Test
    public void testIsLastAdminNoAdmins() {
        when(userRepository.findAllByRole(Role.ADMIN)).thenReturn(new ArrayList<>());
        assertThrows(IllegalStateException.class,
                () -> userService.isLastAdmin()
        );
        verify(userRepository).findAllByRole(Role.ADMIN);
    }

    @Test
    public void testDeleteUser() {
        userService.deleteUser(ID);
        verify(userRepository).deleteById(ID);
    }

    @Test
    public void testDeleteUserIdInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(0)
        );
        verify(userRepository, never()).deleteById(anyInt());
    }

    @Test
    public void testFindLastId() {
        when(userRepository.findFirstByOrderByIdDesc()).thenReturn(Optional.of(user1));
        assertEquals(user1.getId(), userService.findLastId());
        verify(userRepository).findFirstByOrderByIdDesc();
    }

    @Test
    public void testFindLastIdNotFound() {
        assertThrows(IllegalStateException.class,
                () -> userService.findLastId()
        );
        verify(userRepository).findFirstByOrderByIdDesc();
    }

    @ParameterizedTest
    @ValueSource(ints = {9, 25, 100})
    public void testFindLastUsernameNumber(int number) {
        UserProjection projection = getProjection(number);
        when(userRepository.findLastUsername(USERNAME)).thenReturn(Optional.of(projection));
        assertEquals(number + 1, userService.findValidNumberForUsername(USERNAME));
        verify(userRepository).findLastUsername(USERNAME);
    }

    @Test
    public void testFindLastUsernameOnlyDigits() {
        UserProjection projection = new UserProjection() {
            @Override
            public Integer getId() {
                return 1;
            }

            @Override
            public String getUsername() {
                return "9999";
            }

            @Override
            public String getEmail() {
                return EMAIL;
            }

            @Override
            public Role getRole() {
                return Role.ADMIN;
            }
        };
        when(userRepository.findLastUsername(USERNAME)).thenReturn(java.util.Optional.of(projection));
        assertEquals(10000, userService.findValidNumberForUsername(USERNAME));
        verify(userRepository).findLastUsername(USERNAME);
    }

    @Test
    public void testFindLastUsernameNumberNoDigit() {
        UserProjection projection = getProjection(null);
        when(userRepository.findLastUsername(USERNAME)).thenReturn(java.util.Optional.of(projection));
        assertEquals(1, userService.findValidNumberForUsername(USERNAME));
        verify(userRepository).findLastUsername(USERNAME);
    }

    @Test
    public void testFindLastUsernameEmpty() {
        when(userRepository.findLastUsername(USERNAME)).thenReturn(Optional.empty());
        assertEquals(1, userService.findValidNumberForUsername(USERNAME));
        verify(userRepository).findLastUsername(USERNAME);
    }

    @Test
    public void testFindLastUsernameBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.findValidNumberForUsername(BLANK)
        );
        verify(userRepository, never()).findLastUsername(anyString());
    }

    @Test
    public void testFindLastUsernameNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.findValidNumberForUsername(null)
        );
        verify(userRepository, never()).findLastUsername(anyString());
    }

    private UserProjection getProjection(Integer number) {
        return new UserProjection() {
            @Override
            public Integer getId() {
                return number;
            }

            @Override
            public String getUsername() {
                return number == null ? USERNAME : USERNAME + number;
            }

            @Override
            public String getEmail() {
                return EMAIL;
            }

            @Override
            public Role getRole() {
                return Role.ADMIN;
            }
        };
    }
}
