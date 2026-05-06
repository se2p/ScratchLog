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

import de.uni_passau.fim.se2.scratchlog.persistence.entity.BlockEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.ClickEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.JsonEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.ResourceEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.BlockEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ClickEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.JsonEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ResourceEventRepository;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.BlockEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ClickEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.JsonEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.JsonEventType;
import de.uni_passau.fim.se2.scratchlog.util.enums.ResourceEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.ResourceEventType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Utility methods for event domain entity creations for tests.
 *
 * <p>Implementation note: The public methods should save the created entities to the database already and return the
 * persisted objects.
 */
@Service
public class EventUtilService {

    private final BlockEventRepository blockEventRepository;

    private final ClickEventRepository clickEventRepository;

    private final ResourceEventRepository resourceEventRepository;

    private final JsonEventRepository jsonEventRepository;

    public EventUtilService(
        final BlockEventRepository blockEventRepository,
        final ClickEventRepository clickEventRepository,
        final ResourceEventRepository resourceEventRepository,
        final JsonEventRepository JsonEventRepository
    ) {
        this.blockEventRepository = blockEventRepository;
        this.clickEventRepository = clickEventRepository;
        this.resourceEventRepository = resourceEventRepository;
        this.jsonEventRepository = JsonEventRepository;
    }

    /**
     * Generates a new block event.
     *
     * @param user         The user that initiated the event. Must already exist in the database.
     * @param experiment   The experiment the event was created in. Must already exist in the database.
     * @param type         The event type.
     * @param specificType The specific event type.
     * @return The event as stored to the database.
     */
    public BlockEvent generateBlockEvent(
        final User user, final Experiment experiment, final BlockEventType type, final BlockEventSpecific specificType
    ) {
        final BlockEvent event = new BlockEvent(
            user, experiment, LocalDateTime.now(), type, specificType, "figure 1", "meta", "xml", null
        );
        return blockEventRepository.save(event);
    }

    /**
     * Generates a new block event that contains a valid JSON.
     *
     * @param user The user that initiated the event. Must already exist in the database.
     * @param experiment The experiment the event was created in. Must already exist in the database.
     * @param type The event type.
     * @param specificType The specific event type.
     * @return The event as stored to the database.
     */
    public BlockEvent generateBlockEventWithCode(
        final User user, final Experiment experiment, final BlockEventType type, final BlockEventSpecific specificType
    ) {
        final BlockEvent event = new BlockEvent(
            user, experiment, LocalDateTime.now(), type, specificType, "figure 1", "meta", "xml", readJsonFixture()
        );
        return blockEventRepository.save(event);
    }

    /**
     * Generates a new click event.
     *
     * @param user         The user that initiated the event. Must already exist in the database.
     * @param experiment   The experiment the event was created in. Must already exist in the database.
     * @param type         The event type.
     * @param specificType The specific event type.
     * @return The event as stored to the database.
     */
    public ClickEvent generateClickEvent(
        final User user, final Experiment experiment, final ClickEventType type, final ClickEventSpecific specificType
    ) {
        final ClickEvent event = new ClickEvent(
            user, experiment, LocalDateTime.now(), type, specificType, "meta"
        );
        return clickEventRepository.save(event);
    }

    /**
     * Generates a new resource event.
     *
     * @param user         The user that initiated the event. Must already exist in the database.
     * @param experiment   The experiment the event was created in. Must already exist in the database.
     * @param type         The event type.
     * @param specificType The specific event type.
     * @return The event as stored to the database.
     */
    public ResourceEvent generateResourceEvent(
        final User user, final Experiment experiment, final ResourceEventType type, final ResourceEventSpecific specificType
    ) {
        final ResourceEvent event = new ResourceEvent(
            user, experiment, LocalDateTime.now(), type, specificType, "name", "hash", "type", 0
        );
        return resourceEventRepository.save(event);
    }

    /**
     * Generates a new JSON event.
     *
     * @param user         The user that initiated the event. Must already exist in the database.
     * @param experiment   The experiment the event was created in. Must already exist in the database.
     * @param type         The event type.
     * @param specificType The specific event type.
     * @return The event as stored to the database.
     */
    public JsonEvent generateJsonEvent(
        final User user, final Experiment experiment, final JsonEventType type, final JsonEventSpecific specificType
    ) {
        final JsonEvent event = new JsonEvent(
            user, experiment, LocalDateTime.now(), type, specificType, "name", ""
        );
        return jsonEventRepository.save(event);
    }

    private String readJsonFixture() {
        final URL url = getClass().getClassLoader().getResource("json.txt");
        assertNotNull(url);

        try (var is = url.openStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not read json.txt fixture.", e);
        }
    }
}
