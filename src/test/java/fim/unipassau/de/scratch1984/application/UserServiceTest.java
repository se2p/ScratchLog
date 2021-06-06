package fim.unipassau.de.scratch1984.application;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
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
    private static final String ADMIN = "ADMIN";
    private static final String SECRET = "secret";
    private static final int ID = 1;
    private final User user = new User(USERNAME, EMAIL, "ADMIN", "ENGLISH", PASSWORD, SECRET);
    private final UserDTO userDTO = new UserDTO(USERNAME, EMAIL, UserDTO.Role.ADMIN, UserDTO.Language.ENGLISH,
            PASSWORD, SECRET);
    private List<User> admins;

    @BeforeEach
    public void setup() {
        admins = new ArrayList<>();
        admins.add(user);
        user.setId(ID);
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
        assertFalse(userService.existsUser(null));
        verify(userRepository, never()).existsByUsername(USERNAME);
    }

    @Test
    public void testExistsUserUsernameBlank() {
        assertFalse(userService.existsUser(BLANK));
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
        assertFalse(userService.existsEmail(null));
        verify(userRepository, never()).existsByEmail(EMAIL);
    }

    @Test
    public void testExistsEmailBlank() {
        assertFalse(userService.existsEmail(BLANK));
        verify(userRepository, never()).existsByEmail(EMAIL);
    }

    @Test
    public void testExistsParticipant() {
        when(participantRepository.existsByUserAndExperiment(any(), any())).thenReturn(true);
        assertTrue(userService.existsParticipant(ID, ID));
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).existsByUserAndExperiment(any(), any());
    }

    @Test
    public void testExistsParticipantEntityNotFound() {
        when(participantRepository.existsByUserAndExperiment(any(), any())).thenThrow(EntityNotFoundException.class);
        assertFalse(userService.existsParticipant(ID, ID));
        verify(userRepository).getOne(ID);
        verify(experimentRepository).getOne(ID);
        verify(participantRepository).existsByUserAndExperiment(any(), any());
    }

    @Test
    public void testExistsParticipantUserIdInvalid() {
        assertFalse(userService.existsParticipant(-1, ID));
        verify(userRepository, never()).getOne(ID);
        verify(experimentRepository, never()).getOne(ID);
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
    }

    @Test
    public void testExistsParticipantExperimentIdInvalid() {
        assertFalse(userService.existsParticipant(ID, 0));
        verify(userRepository, never()).getOne(ID);
        verify(experimentRepository, never()).getOne(ID);
        verify(participantRepository, never()).existsByUserAndExperiment(any(), any());
    }

    @Test
    public void testSaveUser() {
        when(userRepository.save(any())).thenReturn(user);
        UserDTO saved = userService.saveUser(userDTO);
        assertAll(
                () -> assertEquals(user.getId(), saved.getId()),
                () -> assertEquals(user.getUsername(), saved.getUsername()),
                () -> assertEquals(user.getEmail(), saved.getEmail()),
                () -> assertEquals(user.getPassword(), saved.getPassword()),
                () -> assertEquals(user.getSecret(), saved.getSecret()),
                () -> assertEquals(user.getRole(), saved.getRole().toString()),
                () -> assertEquals(user.getLanguage(), saved.getLanguage().toString())
        );
        verify(userRepository).save(any());
    }

    @Test
    public void testSaveUserIdNull() {
        user.setId(null);
        when(userRepository.save(any())).thenReturn(user);
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
        when(userRepository.findUserByUsername(USERNAME)).thenReturn(user);
        UserDTO userDTO = userService.getUser(USERNAME);
        assertAll(
                () -> assertEquals(userDTO.getUsername(), user.getUsername()),
                () -> assertEquals(userDTO.getEmail(), user.getEmail()),
                () -> assertEquals(userDTO.getRole(), UserDTO.Role.valueOf(user.getRole())),
                () -> assertEquals(userDTO.getLanguage(), UserDTO.Language.valueOf(user.getLanguage())),
                () -> assertEquals(userDTO.getPassword(), user.getPassword()),
                () -> assertEquals(userDTO.getSecret(), user.getSecret())
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
        when(userRepository.findById(ID)).thenReturn(java.util.Optional.of(user));
        UserDTO found = userService.getUserById(ID);
        assertAll(
                () -> assertEquals(USERNAME, found.getUsername()),
                () -> assertEquals(EMAIL, found.getEmail()),
                () -> assertEquals(PASSWORD, found.getPassword()),
                () -> assertEquals(UserDTO.Role.ADMIN, found.getRole()),
                () -> assertEquals(UserDTO.Language.ENGLISH, found.getLanguage())
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
    public void testLoginUser() {
        when(userRepository.findUserByUsername(USERNAME)).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        UserDTO loginUser = userService.loginUser(userDTO);
        assertAll(
                () -> assertEquals(userDTO.getUsername(), loginUser.getUsername()),
                () -> assertEquals(userDTO.getPassword(), loginUser.getPassword())
        );
        verify(userRepository).findUserByUsername(USERNAME);
        verify(passwordEncoder).matches(anyString(), anyString());
    }

    @Test
    public void testLoginUserPasswordNotMatching() {
        when(userRepository.findUserByUsername(USERNAME)).thenReturn(user);
        assertThrows(NotFoundException.class,
                () -> userService.loginUser(userDTO)
        );
        verify(userRepository).findUserByUsername(USERNAME);
        verify(passwordEncoder).matches(anyString(), anyString());
    }

    @Test
    public void testLoginUserPasswordNull() {
        userDTO.setPassword(null);
        when(userRepository.findUserByUsername(USERNAME)).thenReturn(user);
        assertThrows(NotFoundException.class,
                () -> userService.loginUser(userDTO)
        );
        verify(userRepository).findUserByUsername(USERNAME);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    public void testLoginUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> userService.loginUser(userDTO)
        );
        verify(userRepository).findUserByUsername(USERNAME);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    public void testAuthenticateUser() {
        when(userRepository.findUserBySecret(SECRET)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        UserDTO authenticated = userService.authenticateUser(SECRET);
        assertAll(
                () -> assertEquals(user.getId(), authenticated.getId()),
                () -> assertEquals(user.getUsername(), authenticated.getUsername()),
                () -> assertEquals(user.getEmail(), authenticated.getEmail()),
                () -> assertEquals(user.getPassword(), authenticated.getPassword()),
                () -> assertEquals(user.getSecret(), authenticated.getSecret()),
                () -> assertEquals(user.getRole(), authenticated.getRole().toString()),
                () -> assertEquals(user.getLanguage(), authenticated.getLanguage().toString()),
                () -> assertTrue(authenticated.isActive())
        );
        verify(userRepository).findUserBySecret(SECRET);
        verify(userRepository).save(user);
    }

    @Test
    public void testAuthenticateUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> userService.authenticateUser(SECRET)
        );
        verify(userRepository).findUserBySecret(SECRET);
        verify(userRepository, never()).save(user);
    }

    @Test
    public void testAuthenticateUserSecretNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.authenticateUser(null)
        );
        verify(userRepository, never()).findUserBySecret(anyString());
        verify(userRepository, never()).save(user);
    }

    @Test
    public void testAuthenticateUserSecretBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.authenticateUser(BLANK)
        );
        verify(userRepository, never()).findUserBySecret(anyString());
        verify(userRepository, never()).save(user);
    }

    @Test
    public void testUpdateUser() {
        when(userRepository.save(any())).thenReturn(user);
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
                () -> assertFalse(saved.isReset())
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
        when(userRepository.findById(ID)).thenReturn(java.util.Optional.of(user));
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
    public void testIsLastAdmin() {
        admins.add(new User());
        when(userRepository.findAllByRole(ADMIN)).thenReturn(admins);
        assertFalse(userService.isLastAdmin());
        verify(userRepository).findAllByRole(ADMIN);
    }

    @Test
    public void testIsLastAdminTrue() {
        when(userRepository.findAllByRole(ADMIN)).thenReturn(admins);
        assertTrue(userService.isLastAdmin());
        verify(userRepository).findAllByRole(ADMIN);
    }

    @Test
    public void testIsLastAdminNoAdmins() {
        when(userRepository.findAllByRole(ADMIN)).thenReturn(new ArrayList<>());
        assertThrows(IllegalStateException.class,
                () -> userService.isLastAdmin()
        );
        verify(userRepository).findAllByRole(ADMIN);
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
        when(userRepository.findFirstByOrderByIdDesc()).thenReturn(user);
        assertEquals(user.getId(), userService.findLastId());
        verify(userRepository).findFirstByOrderByIdDesc();
    }

    @Test
    public void testFindLastIdNotFound() {
        assertThrows(IllegalStateException.class,
                () -> userService.findLastId()
        );
        verify(userRepository).findFirstByOrderByIdDesc();
    }
}
