package de.uni_passau.fim.se2.scratchlog;

import de.uni_passau.fim.se2.scratchlog.spring.configuration.CodeEmbeddingConfiguration;
import de.uni_passau.fim.se2.scratchlog.spring.configuration.WhiskerConfiguration;
import de.uni_passau.fim.se2.scratchlog.testing_utils.EntityUtilService;
import de.uni_passau.fim.se2.scratchlog.testing_utils.EventUtilService;
import de.uni_passau.fim.se2.scratchlog.util.ApplicationProperties;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.core.Ordered;

import java.io.IOException;

import static org.mockito.Mockito.doReturn;

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
@ActiveProfiles({"test", Constants.PROFILE_WHISKER, Constants.PROFILE_CODE_EMBEDDINGS})
@Testcontainers
public abstract class AbstractScratchLogTest {

    @Autowired
    protected EntityUtilService entityUtilService;

    @Autowired
    protected EventUtilService eventUtilService;

    @MockitoSpyBean
    protected ApplicationProperties applicationProperties;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private WhiskerConfiguration whiskerConfiguration;

    @Autowired
    private CodeEmbeddingConfiguration codeEmbeddingConfiguration;

    protected static MockWebServer mockWebServer;

    @BeforeAll
    static void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void afterAll() {
        mockWebServer.close();
    }

    @BeforeEach
    void setUp() {
        whiskerConfiguration.setBaseUrl(mockWebServer.url("/").uri());
        codeEmbeddingConfiguration.setEmbeddingConnectorUrl(mockWebServer.url("/").uri());
    }

    @AfterEach
    void resetMocks() {
        Mockito.reset(applicationProperties);
    }

    protected void setMailServer(final boolean useMail) {
        doReturn(useMail).when(applicationProperties).useMail();
    }

    protected <T> void enqueueMockWebServerJsonResponse(final T response) {
        enqueueMockWebServerJsonResponse(response, HttpStatus.OK);
    }

    protected <T> void enqueueMockWebServerJsonResponse(final T response, final HttpStatusCode statusCode) {
        final String responseJson = jsonMapper.writeValueAsString(response);
        mockWebServer.enqueue(
            new MockResponse.Builder()
                .body(responseJson)
                .code(statusCode.value())
                .addHeader("Content-Type", "application/json")
                .build()
        );
    }

    @Configuration
    public static class ScratchLogTestConfiguration {
        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        public CodeEmbeddingConfiguration codeEmbeddingConfiguration () {
            final CodeEmbeddingConfiguration configuration = new CodeEmbeddingConfiguration();
            configuration.setModel("llm");
            configuration.setEmbeddingConnectorUrl(mockWebServer.url("/").uri());

            return configuration;
        }

        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        public WhiskerConfiguration whiskerConfiguration () {
            final WhiskerConfiguration configuration = new WhiskerConfiguration();
            configuration.setBaseUrl(mockWebServer.url("/").uri());

            return configuration;
        }
    }
}
