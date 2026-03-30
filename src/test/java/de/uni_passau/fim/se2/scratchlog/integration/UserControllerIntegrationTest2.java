package de.uni_passau.fim.se2.scratchlog.integration;

import de.uni_passau.fim.se2.scratchlog.AbstractScratchLogControllerTest;
import de.uni_passau.fim.se2.scratchlog.application.service.UserService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserBulkDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class UserControllerIntegrationTest2 extends AbstractScratchLogControllerTest {

    private static final String ATTR_USER_BULK_DTO = "userBulkDTO";
    private static final String ATTR_FILE_DTO = "fileDTO";
    private static final String ATTR_FILE = "file";

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
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserBulkDTO userBulkDTO;

    @BeforeEach
    public void setup() {
        userBulkDTO = new UserBulkDTO(5, Language.ENGLISH, uniquePrefix() + "bulk_", false);
    }

    /**
     * Delete all users that start with the common username prefix of the test CSV files.
     * This is necessary since otherwise a lot of tests would falsely fail due to adding duplicate users.
     */
    @AfterEach
    public void cleanup() {
        userRepository.findAll().stream()
            .filter(u -> u.getUsername().startsWith(USERS_CSV_USERNAME))
            .forEach(userRepository::delete);
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
        assertThat(users)
            .hasSize(userBulkDTO.getAmount())
            .allMatch(user ->
                user.getUsername().startsWith(userBulkDTO.getUsername())
                    && user.getLanguage().equals(userBulkDTO.getLanguage())
                    && !user.getPassword().isEmpty()
            );
    }

    @Test
    public void testAddUsersInBulkStartAtOne() throws Exception {
        userBulkDTO.setStartAtOne(true);
        mvc.perform(post("/users/bulk").flashAttr(ATTR_USER_BULK_DTO, userBulkDTO))
            .andExpect(status().isOk());
        List<User> users = userRepository.findAll().stream()
            .filter(u -> u.getUsername().startsWith(userBulkDTO.getUsername())).toList();
        assertThat(users).hasSize(userBulkDTO.getAmount()).allMatch(user -> {
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
        assertThat(users).hasSize(2 * amount).allMatch(user -> {
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
            assertEquals(userBulkDTO.getUsername() + i, parts[0]);
            assertFalse(parts[1].isBlank());
        }
    }

    @Test
    public void testAddUsersInBulkInvalidUsername() throws Exception {
        userBulkDTO.setUsername("aa");
        mvc.perform(post("/users/bulk").flashAttr(ATTR_USER_BULK_DTO, userBulkDTO))
            .andExpect(status().isOk())
            .andExpect(view().name(VIEW_ADD_BULK))
            .andExpect(model().attributeHasFieldErrors(ATTR_USER_BULK_DTO, "username"));
    }

    @Test
    public void testAddUsersInBulkInvalidAmount() throws Exception {
        userBulkDTO.setAmount(-1);
        mvc.perform(post("/users/bulk").flashAttr(ATTR_USER_BULK_DTO, userBulkDTO))
            .andExpect(status().isOk())
            .andExpect(view().name(VIEW_ADD_BULK))
            .andExpect(model().attributeHasFieldErrors(ATTR_USER_BULK_DTO, "amount"));
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

        assertThat(users)
            .hasSize(USERS_CSV_AMOUNT)
            .anyMatch(u -> u.getUsername().equals(USERS_CSV_USERNAME + "1")
                && u.getEmail().equals(USERS_CSV_USERNAME + "1@user.de") && u.getLanguage() == Language.GERMAN
            ).anyMatch(u -> u.getUsername().equals(USERS_CSV_USERNAME + "2")
                && u.getEmail().equals(USERS_CSV_USERNAME + "2@user.com") && u.getLanguage() == Language.ENGLISH
            )
            .anyMatch(u -> u.getUsername().equals(USERS_CSV_USERNAME + "3")
                && u.getEmail().equals(USERS_CSV_USERNAME + "3@example.com") && u.getLanguage() == Language.ENGLISH
            );
    }

    @Test
    public void testAddUsersViaCSVNoEmail() throws Exception {
        mvc.perform(multipart("/users/csv").file(getCSVFile("usersSimple.csv")))
            .andExpect(status().isOk());
        List<User> users = userRepository.findAll().stream()
            .filter(u -> u.getUsername().startsWith(USERS_CSV_USERNAME)).toList();

        assertThat(users)
            .hasSize(2)
            .anyMatch(u -> u.getUsername().equals(USERS_CSV_USERNAME + "1")
                && u.getLanguage() == Language.GERMAN
            )
            .anyMatch(u -> u.getUsername().equals(USERS_CSV_USERNAME + "2")
                && u.getLanguage() == Language.ENGLISH
            );
    }

    @Test
    public void testAddUsersViaCSVPasswords() throws Exception {
        mvc.perform(multipart("/users/csv").file(getCSVFile("usersPassword.csv")))
            .andExpect(status().isOk());
        List<User> users = userRepository.findAll().stream()
            .filter(u -> u.getUsername().startsWith(USERS_CSV_USERNAME)).toList();
        assertThat(users)
            .isNotEmpty()
            .allMatch(u -> passwordEncoder.matches("Unicorns1!", u.getPassword()));
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
        assertAddUsersViaCSVError(getCSVFile("usersInvalid.csv"), "invalid_attributes");
    }

    @Test
    public void testAddUsersViaCSVInvalidPassword() throws Exception {
        assertAddUsersViaCSVError(getCSVFile("usersInvalidPassword.csv"), "invalid_passwords");
    }

    @Test
    public void testAddUsersViaCSVEmailExists() throws Exception {
        UserDTO user = new UserDTO();
        user.setUsername(uniquePrefix() + USERS_CSV_USERNAME);
        user.setEmail(USERS_CSV_USERNAME + "1@user.de");
        userService.completeUserInformation(user);
        userService.saveUser(user);
        assertAddUsersViaCSVError(getCSVFile(USERS_CSV_FILENAME), "existing_attributes");
    }

    @Test
    public void testAddUsersViaCSVUsernameExists() throws Exception {
        UserDTO user = new UserDTO();
        user.setUsername(USERS_CSV_USERNAME + "1");
        userService.completeUserInformation(user);
        userService.saveUser(user);
        assertAddUsersViaCSVError(getCSVFile(USERS_CSV_FILENAME), "existing_attributes");
    }

    @Test
    public void testAddUsersViaCSVDuplicateUsernames() throws Exception {
        assertAddUsersViaCSVError(getCSVFile("usersDuplicateUsernames.csv"), "duplicate_usernames");
    }

    @Test
    public void testAddUsersViaCSVDuplicateEmails() throws Exception {
        assertAddUsersViaCSVError(getCSVFile("usersDuplicateEmails.csv"), "duplicate_emails");
    }

    @Test
    public void testAddCSVParticipantsIOException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(ATTR_FILE, USERS_CSV_FILENAME, USERS_CSV_FILETYPE,
            new ClassPathResource(USERS_CSV_FILENAME).getInputStream());
        MockMultipartFile mockFile = spy(file);
        when(mockFile.getInputStream()).thenThrow(IOException.class);
        assertAddUsersViaCSVError(mockFile, "csv_error");
    }

    @Test
    public void testAddUsersViaCSVInvalidFilename() throws Exception {
        assertAddUsersViaCSVFileError(USERS_CSV_FILENAME, "invaliddotcsv", USERS_CSV_FILETYPE);
    }

    @Test
    public void testAddUsersViaCSVFilenameNull() throws Exception {
        assertAddUsersViaCSVFileError(USERS_CSV_FILENAME, null, USERS_CSV_FILETYPE);
    }

    @Test
    public void testAddUsersViaCSVInvalidContentType() throws Exception {
        assertAddUsersViaCSVFileError(USERS_CSV_FILENAME, USERS_CSV_FILENAME, "image/png");
    }

    @Test
    public void testAddUsersViaCSVContentTypeNull() throws Exception {
        assertAddUsersViaCSVFileError(USERS_CSV_FILENAME, USERS_CSV_FILENAME, null);
    }

    @Test
    public void testAddUsersViaCSVFileEmpty() throws Exception {
        assertAddUsersViaCSVFileError("empty.csv", "empty.csv", USERS_CSV_FILETYPE);
    }

    private void assertAddUsersViaCSVError(MockMultipartFile file, String errorCode) throws Exception {
        int usersBefore = userRepository.findAll().size();
        mvc.perform(multipart("/users/csv").file(file))
            .andExpect(status().isOk())
            .andExpect(view().name(VIEW_ADD_CSV))
            .andExpect(model().attributeHasFieldErrorCode(ATTR_FILE_DTO, ATTR_FILE, errorCode));
        // Assert that no users were added to the DB.
        int usersNow = userRepository.findAll().size();
        assertEquals(usersBefore, usersNow);
    }

    private void assertAddUsersViaCSVFileError(String realFilename, String uploadFilename, String contentType)
        throws Exception {
        MockMultipartFile file = new MockMultipartFile(ATTR_FILE, uploadFilename, contentType,
            new ClassPathResource(realFilename).getInputStream());
        int usersBefore = userRepository.findAll().size();
        mvc.perform(multipart("/users/csv").file(file))
            .andExpect(status().isOk())
            .andExpect(view().name(VIEW_ADD_CSV))
            .andExpect(model().attributeHasFieldErrorCode(ATTR_FILE_DTO, ATTR_FILE, "ValidFile"));
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
