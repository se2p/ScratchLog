package de.uni_passau.fim.se2.scratchlog.application.service;

import com.fasterxml.jackson.annotation.JsonValue;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestResultState;
import de.uni_passau.fim.se2.scratchlog.spring.configuration.WhiskerConfiguration;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Profile(Constants.PROFILE_WHISKER)
public class WhiskerService {

    private static final Logger log = LoggerFactory.getLogger(WhiskerService.class);

    private final RestClient restClient;

    @Autowired
    public WhiskerService(final WhiskerConfiguration whiskerConfiguration) {
        this.restClient = RestClient.create(whiskerConfiguration.getBaseUrl());
    }

    /**
     * Calls the external Whisker API to run the test suite for the given project.
     *
     * @param projectSb3 A Scratch project in SB3 format.
     * @param testSuite A Whisker test suite.
     * @return The test case execution results.
     */
    @Nullable
    public WhiskerApiResponse runTests(final byte[] projectSb3, final String testSuite) {
        final MultiValueMap<String, Resource> body = new LinkedMultiValueMap<>();
        body.add("project", new ByteArrayResource(projectSb3));
        body.add("testsuite", new ByteArrayResource(testSuite.getBytes(StandardCharsets.UTF_8)));

        try {
            return restClient
                .post()
                .uri("/test")
                .body(body)
                .retrieve()
                .body(WhiskerApiResponse.class);
        } catch (RestClientResponseException e) {
            log.warn("The Whisker API could not process our request.", e);
            return null;
        }
    }

    public record WhiskerApiResponse(@JsonValue List<WhiskerTestResult> testResult) {
    }

    public record WhiskerTestResult(String name, int index, TestResultState result) {
    }
}
