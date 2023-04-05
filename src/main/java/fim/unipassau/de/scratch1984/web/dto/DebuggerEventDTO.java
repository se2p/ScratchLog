package fim.unipassau.de.scratch1984.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A DTO representing a debugger event that resulted from user interaction with the Scratch debugger, not including
 * interactions with questions.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DebuggerEventDTO implements EventDTO {

    /**
     * All possible event types for a debugger event.
     */
    public enum DebuggerEventType {
        /**
         * The event was caused by a breakpoint interaction.
         */
        BREAKPOINT,

        /**
         * The event was caused by a block interaction in the debugger.
         */
        BLOCK,

        /**
         * The event was caused by selecting a sprite in the debugger.
         */
        SPRITE,

        /**
         * The event was caused by opening the debugger.
         */
        TARGET
    }

    /**
     * All possible specific events for a debugger event.
     */
    public enum DebuggerEvent {
        /**
         * The user opened the debugger on a sprite or stage.
         */
        OPEN_DEBUGGER,

        /**
         * The user selected a sprite instance in the dropdown list of the debugger.
         */
        SELECT_SPRITE,

        /**
         * The user opened the debugger for a specific block.
         */
        OPEN_BLOCK,

        /**
         * The user selected a block execution in the dropdown list of the debugger.
         */
        SELECT_BLOCK_EXECUTION,

        /**
         * The user clicked on a block and is routed to the block-debugger.
         */
        ROUTE_TO_BLOCK,

        /**
         * The user added a breakpoint.
         */
        ADD_BREAKPOINT,

        /**
         * The user deleted a breakpoint.
         */
        DELETE_BREAKPOINT
    }

    /**
     * The unique ID of the debugger event.
     */
    private Integer id;

    /**
     * The ID of the user who caused the event.
     */
    private Integer user;

    /**
     * The ID of the experiment during which the event occurred.
     */
    private Integer experiment;

    /**
     * The local date time at which the debugger interaction occurred in the Scratch GUI.
     */
    private LocalDateTime date;

    /**
     * The type of debugger event that occurred.
     */
    private DebuggerEventType eventType;

    /**
     * The specific event that occurred.
     */
    private DebuggerEvent event;

    /**
     * The block or target id of the debugger event.
     */
    private String blockOrTargetID;

    /**
     * The target name or block opcode of the debugger event.
     */
    private String nameOrOpcode;

    /**
     * Only applicable to select sprite event, null for everything else.
     */
    private Integer original;

    /**
     * The number of the block executions of the debugger event.
     */
    private Integer execution;

    /**
     * Constructs a new debugger event dto with the given attributes.
     *
     * @param user The id of the user who caused the event.
     * @param experiment The id of the experiment during which the event occurred.
     * @param date The time at which the event occurred.
     * @param eventType The type of event.
     * @param event The specific event.
     * @param blockOrTargetID The block or target ID of the event.
     * @param nameOrOpcode The target name or block opcode of the event.
     * @param original Only applicable to the select sprite event.
     * @param execution The number of the block executions of the event.
     */
    public DebuggerEventDTO(final Integer user, final Integer experiment, final LocalDateTime date,
                            final DebuggerEventType eventType, final DebuggerEvent event, final String blockOrTargetID,
                            final String nameOrOpcode, final Integer original, final Integer execution) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.eventType = eventType;
        this.event = event;
        this.blockOrTargetID = blockOrTargetID;
        this.nameOrOpcode = nameOrOpcode;
        this.original = original;
        this.execution = execution;
    }

    /**
     * Indicates whether some {@code other} debugger event DTO is semantically equal to this debugger event DTO.
     *
     * @param other The object to compare this debugger event DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent debugger event DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        DebuggerEventDTO that = (DebuggerEventDTO) other;
        return Objects.equals(id, that.id);
    }

    /**
     * Calculates a hash code for this debugger event DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the debugger event DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
