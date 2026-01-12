package de.uni_passau.fim.se2.scratchlog.application;

import de.uni_passau.fim.se2.scratchlog.AbstractScratchLogTest;
import de.uni_passau.fim.se2.scratchlog.application.service.UserService;
import de.uni_passau.fim.se2.scratchlog.testing_utils.DtoUtil;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserBulkDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserServiceTest2 extends AbstractScratchLogTest {

    @Autowired
    private UserService userService;

    private UserBulkDTO userBulkDTO;
    private List<UserDTO> csvUserList;

    private UserDTO user1DTO;
    private UserDTO user2DTO;

    @BeforeEach
    public void setup() {
        user1DTO = DtoUtil.generateUserDTO("User1");
        user2DTO = DtoUtil.generateUserDTO("User2");

        userBulkDTO = new UserBulkDTO(5, Language.ENGLISH, uniquePrefix() + "userbulktest_", false);
        csvUserList = List.of(
            UserDTO.builder().username("csvuser1").confirmPassword("password1!").build(),
            UserDTO.builder().username("csvuser2").confirmPassword("password2!").build());
    }

    @Test
    public void testSaveUsers() {
        List<UserDTO> saved = userService.saveUsers(List.of(user1DTO, user2DTO));
        assertAll(
                () -> assertTrue(userService.existsUser(user1DTO.getUsername())),
                () -> assertTrue(userService.existsUser(user2DTO.getUsername())),
                () -> assertThat(saved).extracting(UserDTO::getUsername)
                        .containsExactlyInAnyOrder(user1DTO.getUsername(), user2DTO.getUsername())
        );
    }

    @Test
    public void testAddUsersInBulk() {
        List<UserDTO> users = userService.addUsersInBulk(userBulkDTO);
        assertThat(users).hasSize(users.size());
        assertThat(users).allMatch(user -> {
            // Refetch user to get the id.
            user = userService.getUser(user.getUsername());
            return user.getUsername().startsWith(userBulkDTO.getUsername())
                && user.getUsername().endsWith(user.getId().toString())
                && user.getLanguage().equals(userBulkDTO.getLanguage())
                && !user.getPassword().isEmpty();
        });
    }

    @Test
    public void testAddUsersInBulkStartAtOne() {
        userBulkDTO.setStartAtOne(true);
        List<UserDTO> users = userService.addUsersInBulk(userBulkDTO);
        assertThat(users).hasSize(users.size());
        assertThat(users).allMatch(user -> {
            int suffix = Integer.parseInt(user.getUsername().split("_")[2]);
            return user.getUsername().startsWith(userBulkDTO.getUsername())
                && suffix >= 1 && suffix <= userBulkDTO.getAmount()
                && user.getLanguage().equals(userBulkDTO.getLanguage())
                && !user.getPassword().isEmpty();
        });
    }

    @Test
    public void testAddUsersInBulkStartAtOneAlreadyPresent() {
        userBulkDTO.setStartAtOne(true);
        userService.addUsersInBulk(userBulkDTO);
        List<UserDTO> users = userService.addUsersInBulk(userBulkDTO);
        assertThat(users).hasSize(users.size());
        assertThat(users).allMatch(user -> {
            int suffix = Integer.parseInt(user.getUsername().split("_")[2]);
            int amount = userBulkDTO.getAmount();
            return user.getUsername().startsWith(userBulkDTO.getUsername())
                && suffix >= amount + 1 && suffix <= 2 * amount
                && user.getLanguage().equals(userBulkDTO.getLanguage())
                && !user.getPassword().isEmpty();
        });
    }

    @Test
    public void testAddUsersInBulkNull() {
        assertThrows(IllegalArgumentException.class, () -> userService.addUsersInBulk(null));
    }

    @Test
    public void testGenerateUsernamePasswordCsv() {
        String csv = userService.generateUsernamePasswordCsv(csvUserList);
        assertEquals("""
         username,password
         csvuser1,password1!
         csvuser2,password2!
         """, csv);
    }

    @Test
    public void testGenerateUsernamePasswordCsvNull() {
        assertThrows(IllegalArgumentException.class, () -> userService.generateUsernamePasswordCsv(null));
    }

    @Test
    public void testGenerateUsernamePasswordCsvUsernameNull() {
        csvUserList.getFirst().setUsername(null);
        assertThrows(IllegalArgumentException.class, () -> userService.generateUsernamePasswordCsv(null));
    }

    @Test
    public void testGenerateUsernamePasswordCsvPasswordNull() {
        csvUserList.getFirst().setConfirmPassword(null);
        assertThrows(IllegalArgumentException.class, () -> userService.generateUsernamePasswordCsv(null));
    }

    private String uniquePrefix() {
        return UUID.randomUUID() + "_";
    }
}
