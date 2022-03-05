package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.controller.SecretController;
import fim.unipassau.de.scratch1984.web.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SecretControllerTest {

    @InjectMocks
    SecretController secretController;

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    private static final int ID = 1;
    private static final String STRING_ID = "1";
    private static final String SECRET = "secret";
    private static final String BLANK = " ";
    private final UserDTO user1 = new UserDTO("participant1", "part1@part.de", UserDTO.Role.PARTICIPANT,
            UserDTO.Language.ENGLISH, "password1", SECRET);
    private final UserDTO user2 = new UserDTO("participant2", "part2@part.de", UserDTO.Role.PARTICIPANT,
            UserDTO.Language.ENGLISH, "password2", SECRET);
    private List<UserDTO> users;

    @BeforeEach
    public void setUp() {
        user1.setId(ID);
        user1.setSecret(SECRET);
        user2.setId(ID + 1);
        users = Arrays.asList(user1, user2);
    }

    @Test
    public void testDisplaySecret() {
        when(userService.getUserById(ID)).thenReturn(user1);
        assertEquals(SECRET, secretController.displaySecret(STRING_ID, STRING_ID, model));
        verify(userService).getUserById(ID);
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretNull() {
        user1.setSecret(null);
        when(userService.getUserById(ID)).thenReturn(user1);
        assertEquals(Constants.ERROR, secretController.displaySecret(STRING_ID, STRING_ID, model));
        verify(userService).getUserById(ID);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretNotFound() {
        when(userService.getUserById(ID)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, secretController.displaySecret(STRING_ID, STRING_ID, model));
        verify(userService).getUserById(ID);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretInvalidUserId() {
        assertEquals(Constants.ERROR, secretController.displaySecret("0", STRING_ID, model));
        verify(userService, never()).getUserById(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretInvalidExperimentId() {
        assertEquals(Constants.ERROR, secretController.displaySecret(STRING_ID, SECRET, model));
        verify(userService, never()).getUserById(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretUserIdBlank() {
        assertEquals(Constants.ERROR, secretController.displaySecret(BLANK, STRING_ID, model));
        verify(userService, never()).getUserById(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretExperimentIdBlank() {
        assertEquals(Constants.ERROR, secretController.displaySecret(STRING_ID, BLANK, model));
        verify(userService, never()).getUserById(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretUserIdNull() {
        assertEquals(Constants.ERROR, secretController.displaySecret(null, STRING_ID, model));
        verify(userService, never()).getUserById(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretExperimentIdNull() {
        assertEquals(Constants.ERROR, secretController.displaySecret(STRING_ID, null, model));
        verify(userService, never()).getUserById(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecrets() {
        when(userService.findUnfinishedUsers(ID)).thenReturn(users);
        assertEquals(SECRET, secretController.displaySecrets(STRING_ID, model));
        verify(userService).findUnfinishedUsers(ID);
        verify(model, times(3)).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretsNotFound() {
        when(userService.findUnfinishedUsers(ID)).thenThrow(NotFoundException.class);
        assertEquals(Constants.ERROR, secretController.displaySecrets(STRING_ID, model));
        verify(userService).findUnfinishedUsers(ID);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretsInvalidId() {
        assertEquals(Constants.ERROR, secretController.displaySecrets("0", model));
        verify(userService, never()).findUnfinishedUsers(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretsExperimentBlank() {
        assertEquals(Constants.ERROR, secretController.displaySecrets(BLANK, model));
        verify(userService, never()).findUnfinishedUsers(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDisplaySecretsExperimentNull() {
        assertEquals(Constants.ERROR, secretController.displaySecrets(null, model));
        verify(userService, never()).findUnfinishedUsers(anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }
}
