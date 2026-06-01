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

package de.uni_passau.fim.se2.scratchlog.persistence.entity;

import de.uni_passau.fim.se2.scratchlog.util.enums.JsonEventSpecific;
import de.uni_passau.fim.se2.scratchlog.util.enums.JsonEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * An entity representing a JSON event being the result of a user using an added feature in the Scratch GUI.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class JsonEvent implements Event {

    /**
     * The unique ID of the JSON event.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The {@link User} who caused the JSON event.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The {@link Experiment} during which the JSON event occurred.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private Experiment experiment;

    /**
     * The datetime at which the JSON event occurred.
     */
    @Column(name = "date")
    private LocalDateTime date;

    /**
     * The type of JSON event that occurred.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private JsonEventType eventType;

    /**
     * The specific event that occurred.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event")
    private JsonEventSpecific event;

    /**
     * The name of the file.
     */
    @Column(name = "name")
    private String fileName;

    /**
     * The content of the file.
     */
    @Column(name = "content")
    private String content;

    /**
     * Constructs a new JSON event with the given attributes.
     *
     * @param user       The user who caused the event.
     * @param experiment The experiment during which the event occurred.
     * @param date       The time at which the event occurred.
     * @param eventType  The event type.
     * @param event      The specific event.
     * @param fileName   The name of the file.
     * @param content    The content of the file.
     */
    public JsonEvent(final User user, final Experiment experiment, final LocalDateTime date,
                     final JsonEventType eventType, final JsonEventSpecific event,
                     final String fileName, final String content) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.eventType = eventType;
        this.event = event;
        this.fileName = fileName;
        this.content = content;
    }

}
