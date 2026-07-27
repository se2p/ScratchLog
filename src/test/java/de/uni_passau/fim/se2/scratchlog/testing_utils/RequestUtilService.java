/*
 * This file is part of ScratchLog.
 * Licenced under the GPL v3.0 or later.
 *
 * SPDX-FileCopyrightText: 2021-2026 Scratchlog contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.uni_passau.fim.se2.scratchlog.testing_utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import java.util.List;
import java.util.Map;

@Service
public class RequestUtilService {

    private final RestTestClient testClient;

    @Autowired
    public RequestUtilService(final WebApplicationContext applicationContext) {
        testClient = RestTestClient.bindToApplicationContext(applicationContext).build();
    }

    /**
     * Sends a get request.
     *
     * @param path           The REST endpoint to send the data to.
     * @param responseType   Type of the response body.
     * @param expectedStatus The expected HTTP status of the request.
     * @return The response body for the request, already parsed.
     * @param <R> The type of the body that is received back from the endpoint.
     */
    public <R> R get(final String path, final Class<R> responseType, final HttpStatus expectedStatus) {
        return get(path, responseType, expectedStatus, null);
    }

    /**
     * Sends a get request.
     *
     * @param path           The REST endpoint to send the data to.
     * @param responseType   Type of the response body.
     * @param expectedStatus The expected HTTP status of the request.
     * @param params         Additional request URL parameters.
     * @return The response body for the request, already parsed.
     * @param <R> The type of the body that is received back from the endpoint.
     */
    public <R> R get(
        final String path, final Class<R> responseType, final HttpStatus expectedStatus, final Map<String, String> params
    ) {
        final var request = buildGetRequest(path, params);
        final var response = request.exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectBody(responseType)
            .returnResult();

        return response.getResponseBody();
    }

    private RestTestClient.RequestHeadersSpec<?> buildGetRequest(final String path, final Map<String, String> params) {
        return testClient.get()
            .uri(uriBuilder -> uriBuilder.path(path).queryParams(buildParams(params)).build());
    }

    private static LinkedMultiValueMap<String, String> buildParams(final Map<String, String> params) {
        final LinkedMultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        if (params == null) {
            return result;
        }

        for (final Map.Entry<String, String> entry : params.entrySet()) {
            result.put(entry.getKey(), List.of(entry.getValue()));
        }

        return result;
    }
}
