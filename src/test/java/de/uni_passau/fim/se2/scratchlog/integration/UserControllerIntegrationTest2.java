package de.uni_passau.fim.se2.scratchlog.integration;

import de.uni_passau.fim.se2.scratchlog.AbstractScratchLogControllerTest;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserBulkDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.List;
import java.util.UUID;

public class UserControllerIntegrationTest2 extends AbstractScratchLogControllerTest {

    private static final String ATTR_USER_BULK_DTO = "userBulkDTO";
    private static final String ATTR_FILE_DTO = "fileDTO";
    private static final String ATTR_FILE = "file";
    private static final String ATTR_ERROR = "error";

    private static final String VIEW_REDIRECT = "redirect:/";
    private static final String VIEW_ADD_BULK = "users-add";
    private static final String VIEW_ADD_CSV = "users-csv";

    private static final String USERS_CSV_FILENAME = "users.csv";
    private static final String USERS_CSV_FILETYPE = "text/csv";
    private static final String USERS_CSV_USERNAME = "newUser";
    private static final int USERS_CSV_AMOUNT = 3;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserBulkDTO userBulkDTO;

    @BeforeEach
    public void setup() {
        userBulkDTO = new UserBulkDTO(5, Language.ENGLISH, uniquePrefix() + "bulk_", false);
    }

    @Test
    void testGetAddUsersInBulk() throws Exception {
        setMailServer(false);

        mvc.perform(get("/users/bulk"))
            .andExpect(status().isOk())
            .andExpect(view().name(VIEW_ADD_BULK));
    }

    @Test
    void testGetAddUsersInBulkMailServer() throws Exception {
        setMailServer(true);

        mvc.perform(get("/users/bulk"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name(VIEW_REDIRECT));
    }

    @Test
    public void testAddUsersInBulk() throws Exception {
        mvc.perform(post("/users/bulk").flashAttr(ATTR_USER_BULK_DTO, userBulkDTO))
            .andExpect(status().isOk());
        List<User> users = userRepository.findAll().stream()
            .filter(u -> u.getUsername().startsWith(userBulkDTO.getUsername())).toList();
        assertThat(users).hasSize(userBulkDTO.getAmount());
        assertThat(users).allMatch(user ->
            user.getUsername().equals(userBulkDTO.getUsername() + user.getId())
                && user.getLanguage().equals(userBulkDTO.getLanguage())
                && !user.getPassword().isEmpty());
    }

    @Test
    public void testAddUsersInBulkStartAtOne() throws Exception {
        userBulkDTO.setStartAtOne(true);
        mvc.perform(post("/users/bulk").flashAttr(ATTR_USER_BULK_DTO, userBulkDTO))
            .andExpect(status().isOk());
        List<User> users = userRepository.findAll().stream()
            .filter(u -> u.getUsername().startsWith(userBulkDTO.getUsername())).toList();
        assertThat(users).hasSize(userBulkDTO.getAmount());
        assertThat(users).allMatch(user -> {
            int suffix = Integer.parseInt(user.getUsername().split("_")[2]);
            return user.getUsername().startsWith(userBulkDTO.getUsername())
                && suffix >= 1 && suffix <= userBulkDTO.getAmount();
        });
    }

    @Test
    public void testAddUsersInBulkStartAtOneAlreadyPresent() throws Exception {
        int amount = userBulkDTO.getAmount();
        userBulkDTO.setStartAtOne(true);
        mvc.perform(post("/users/bulk").flashAttr(ATTR_USER_BULK_DTO, userBulkDTO));
        mvc.perform(post("/users/bulk").flashAttr(ATTR_USER_BULK_DTO, userBulkDTO))
            .andExpect(status().isOk());
        List<User> users = userRepository.findAll().stream()
            .filter(u -> u.getUsername().startsWith(userBulkDTO.getUsername())).toList();
        assertThat(users).hasSize(2 * amount);
        assertThat(users).allMatch(user -> {
            int suffix = Integer.parseInt(user.getUsername().split("_")[2]);
            return user.getUsername().startsWith(userBulkDTO.getUsername())
                && suffix >= 1 && suffix <= 2 * amount;
        });
    }

    @Test
    public void testAddUsersInBulkDownloadCSV() throws Exception {
        // Start at one to make validating the CSV easier.
        userBulkDTO.setStartAtOne(true);
        String content = mvc.perform(post("/users/bulk").flashAttr(ATTR_USER_BULK_DTO, userBulkDTO))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String[] lines = content.split(System.lineSeparator());

        assertEquals("username,password", lines[0]);
        assertThat(lines).hasSize(userBulkDTO.getAmount() + 1);
        for (int i = 1; i < lines.length; ++i) {
            String[] parts =  lines[i].split(",");
            assertEquals(userBulkDTO.getUsername() + 1, parts[0]);
            assertFalse(parts[1].isBlank());
        }
    }

    @Test
    public void testAddUsersInBulkInvalidUsername() throws Exception {
        userBulkDTO.setUsername("aa");
        mvc.perform(post("/users/bulk").flashAttr(ATTR_USER_BULK_DTO, userBulkDTO))
            .andExpect(status().isOk())
            .andExpect(view().name(VIEW_ADD_BULK))
            .andExpect(model().attribute(ATTR_ERROR, nullValue()));
    }

    @Test
    public void testAddUsersInBulkInvalidAmount() throws Exception {
        userBulkDTO.setAmount(-1);
        mvc.perform(post("/users/bulk").flashAttr(ATTR_USER_BULK_DTO, userBulkDTO))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name(Constants.ERROR))
            .andExpect(model().attribute(ATTR_ERROR, nullValue()));
    }

    @Test
    public void testGetAddUsersViaCSV() throws Exception {
        mvc.perform(get("/users/csv"))
            .andExpect(status().isOk())
            .andExpect(view().name(VIEW_ADD_CSV));
    }

    @Test
    public void testAddUsersViaCSV() throws Exception {
        mvc.perform(multipart("/users/csv").file(getCSVFile(USERS_CSV_FILENAME)))
            .andExpect(status().isOk());
        List<User> users = userRepository.findAll().stream()
            .filter(u -> u.getUsername().startsWith(USERS_CSV_USERNAME)).toList();

        assertThat(users).hasSize(USERS_CSV_AMOUNT);
        assertThat(users).anyMatch(u -> u.getUsername().equals(USERS_CSV_USERNAME + "1")
            && u.getEmail().equals(USERS_CSV_USERNAME + "1@user.de") && u.getLanguage() == Language.GERMAN);
        assertThat(users).anyMatch(u -> u.getUsername().equals(USERS_CSV_USERNAME + "2")
            && u.getEmail().equals(USERS_CSV_USERNAME + "2@user.com") && u.getLanguage() == Language.ENGLISH);
        assertThat(users).anyMatch(u -> u.getUsername().equals(USERS_CSV_USERNAME + "3")
            && u.getEmail().equals(USERS_CSV_USERNAME + "3@example.com") && u.getLanguage() == Language.ENGLISH);
    }

    @Test
    public void testAddUsersViaCSVNoEmail() throws Exception {
        mvc.perform(multipart("/users/csv").file(getCSVFile("usersSimple.csv")))
            .andExpect(status().isOk());
        List<User> users = userRepository.findAll().stream()
            .filter(u -> u.getUsername().startsWith(USERS_CSV_USERNAME)).toList();

        assertThat(users).hasSize(2);
        assertThat(users).anyMatch(u -> u.getUsername().equals(USERS_CSV_USERNAME + "1")
            && u.getLanguage() == Language.GERMAN);
        assertThat(users).anyMatch(u -> u.getUsername().equals(USERS_CSV_USERNAME + "2")
            && u.getLanguage() == Language.ENGLISH);
    }

    @Test
    public void testAddUsersViaCSVPasswords() throws Exception {
        mvc.perform(multipart("/users/csv").file(getCSVFile("usersPassword.csv")))
            .andExpect(status().isOk());
        List<User> users = userRepository.findAll().stream()
            .filter(u -> u.getUsername().startsWith(USERS_CSV_USERNAME)).toList();
        System.out.println(passwordEncoder.encode("Unicorns1!"));
        System.out.println(users.getFirst().getPassword());
        assertThat(users).allMatch(u -> u.getPassword().equals(passwordEncoder.encode("Unicorns1!")));
    }

    @Test
    public void testAddUsersViaCSVDownloadCSV() throws Exception {
        String content = mvc.perform(multipart("/users/csv").file(getCSVFile(USERS_CSV_FILENAME)))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String[] lines = content.split(System.lineSeparator());

        assertEquals("username,password", lines[0]);
        assertThat(lines).hasSize(USERS_CSV_AMOUNT + 1);
        for (int i = 1; i < lines.length; ++i) {
            String[] parts =  lines[i].split(",");
            assertEquals(USERS_CSV_USERNAME + i, parts[0]);
            assertFalse(parts[1].isBlank());
        }
    }

    @Test
    public void testAddUsersViaCSVInvalidAttributes() throws Exception {
        int usersBefore = userRepository.findAll().size();
        mvc.perform(multipart("/users/csv").file(getCSVFile("usersInvalid.csv")))
            .andExpect(status().isOk())
            .andExpect(view().name(VIEW_ADD_CSV))
            .andExpect(model().attributeHasFieldErrorCode(ATTR_FILE_DTO, ATTR_FILE, "invalid_attributes"));

        int usersNow = userRepository.findAll().size();
        assertEquals(usersBefore, usersNow);
    }

    private MockMultipartFile getCSVFile(String filename) throws IOException {
        return new MockMultipartFile(ATTR_FILE, filename, USERS_CSV_FILETYPE,
            new ClassPathResource(filename).getInputStream());
    }

    private String uniquePrefix() {
        // Remove the hyphens since those are not allowed by the username validator.
        return (UUID.randomUUID().toString().replaceAll("-", "")) + "_";
    }
}
