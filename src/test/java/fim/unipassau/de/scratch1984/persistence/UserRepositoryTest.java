package fim.unipassau.de.scratch1984.persistence;

import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository repository;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String LANGUAGE = "ENGLISH";
    private static final String ADMIN1 = "admin1";
    private User user1 = new User(ADMIN1, "admin1@admin.de", ROLE_ADMIN, LANGUAGE, "admin1", "secret1");
    private User user2 = new User("admin2", "admin2@admin.com", ROLE_ADMIN, LANGUAGE, "admin2", "secret2");
    private User user3 = new User("user", "user@user.de", "PARTICIPANT", LANGUAGE, "user", "secret3");

    @BeforeEach
    public void setup() {
        user1 = entityManager.persist(user1);
        user2 = entityManager.persist(user2);
        user3 = entityManager.persist(user3);
    }

    @Test
    public void testExistsByUsername() {
        assertAll(
                () -> assertTrue(repository.existsByUsername(ADMIN1)),
                () -> assertFalse(repository.existsByUsername(ADMIN1 + 1))
        );
    }

    @Test
    public void testFindUserByUsername() {
        assertAll(
                () -> assertEquals(user1.getId(), repository.findUserByUsername(ADMIN1).getId()),
                () -> assertNull(repository.findUserByUsername(ADMIN1 + 1))
        );
    }

    @Test
    public void testFindUserByRole() {
        List<User> admins = repository.findAllByRole(ROLE_ADMIN);
        assertEquals(2, admins.size());
    }

}
