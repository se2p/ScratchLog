/*
 * Copyright (C) 2023 ScratchLog contributors
 *
 * This file is part of ScratchLog.
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ScratchLog. If not, see <http://www.gnu.org/licenses/>.
 */

package fim.unipassau.de.scratchLog.persistence.entity;

import fim.unipassau.de.scratchLog.util.enums.DebuggerEventSpecific;
import fim.unipassau.de.scratchLog.util.enums.DebuggerEventType;
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
 * An entity representing a debugger event that resulted from user interaction with the Scratch debugger, not including
 * interaction with questions.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class DebuggerEvent implements Event {

    /**
     * The unique ID of the debugger event.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The {@link User} who caused the debugger event.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The {@link Experiment} during which the debugger event occurred.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private Experiment experiment;

    /**
     * The datetime at which the debugger event occurred.
     */
    @Column(name = "date")
    private LocalDateTime date;

    /**
     * The type of debugger event that occurred.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private DebuggerEventType eventType;

    /**
     * The specific event that occurred.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event")
    private DebuggerEventSpecific event;

    /**
     * The block or target id of the debugger event.
     */
    @Column(name = "block_target_id")
    private String blockOrTargetID;

    /**
     * The target name or block opcode of the debugger event.
     */
    @Column(name = "name_opcode")
    private String nameOrOpcode;

    /**
     * Only applicable to select sprite event, null for everything else.
     */
    @Column(name = "original")
    private Integer original;

    /**
     * The number of the block executions of the debugger event.
     */
    @Column(name = "execution")
    private Integer execution;

}
