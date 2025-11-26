package de.uni_passau.fim.se2.scratchlog;

import de.uni_passau.fim.se2.scratchlog.testing_utils.EntityUtilService;
import de.uni_passau.fim.se2.scratchlog.testing_utils.EventUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for all integration tests in ScratchLog that need to use
 * Spring-instantiated and injected classes.
 *
 * <p>Add frequently needed helper classes and methods here.
 *
 * <p>Also add mocked beans here once instead of in the subclasses. This avoids
 * having to restart the Spring application multiple times during tests (due to
 * the mock bean in the concrete test class, the application context is
 * different and therefore a restart is required).
 * <em>Note</em>: Please only mock beans when really necessary. Usually that
 * should only be necessary when interacting with other external systems
 * (e.g. mail). For most tests, please set up the required entities in the
 * database first and then test against this ‘actual’ data rather than mocking
 * the database and/or ScratchLog service/repository/controller beans.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractScratchLogTest {

    @Autowired
    protected EntityUtilService entityUtilService;

    @Autowired
    protected EventUtilService eventUtilService;

}
