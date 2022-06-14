package fim.unipassau.de.scratch1984.web.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A DTO representing a debugger event that resulted from user interaction with the Scratch debugger, not including
 * interactions with questions.
 */
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
     * Default constructor for the debugger event dto.
     */
    public DebuggerEventDTO() {
    }

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
     */
    public DebuggerEventDTO(final Integer user, final Integer experiment, final LocalDateTime date,
                            final DebuggerEventType eventType, final DebuggerEvent event, final String blockOrTargetID,
                            final String nameOrOpcode, final Integer original) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.eventType = eventType;
        this.event = event;
        this.blockOrTargetID = blockOrTargetID;
        this.nameOrOpcode = nameOrOpcode;
        this.original = original;
    }

    /**
     * {@inheritDoc}
     *
     * @return The event ID.
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     *
     * @param id The event ID to be set.
     */
    @Override
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     *
     * @return The user's ID.
     */
    @Override
    public Integer getUser() {
        return user;
    }

    /**
     * {@inheritDoc}
     *
     * @param user The user ID to be set.
     */
    @Override
    public void setUser(final Integer user) {
        this.user = user;
    }

    /**
     * {@inheritDoc}
     *
     * @return The experiment ID.
     */
    @Override
    public Integer getExperiment() {
        return experiment;
    }

    /**
     * {@inheritDoc}
     *
     * @param experiment The experiment ID to be set.
     */
    @Override
    public void setExperiment(final Integer experiment) {
        this.experiment = experiment;
    }

    /**
     * {@inheritDoc}
     *
     * @return The event time.
     */
    @Override
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * {@inheritDoc}
     *
     * @param date The time to be set.
     */
    @Override
    public void setDate(final LocalDateTime date) {
        this.date = date;
    }

    /**
     * Returns the type of the event.
     *
     * @return The event type.
     */
    public DebuggerEventType getEventType() {
        return eventType;
    }

    /**
     * Sets the type of the event.
     *
     * @param eventType The event type to be set.
     */
    public void setEventType(final DebuggerEventType eventType) {
        this.eventType = eventType;
    }

    /**
     * Returns the specific event that occurred.
     *
     * @return The event.
     */
    public DebuggerEvent getEvent() {
        return event;
    }

    /**
     * Sets the specific event that occurred.
     *
     * @param event The event to be set.
     */
    public void setEvent(final DebuggerEvent event) {
        this.event = event;
    }

    /**
     * Returns the block or target ID.
     *
     * @return The ID.
     */
    public String getBlockOrTargetID() {
        return blockOrTargetID;
    }

    /**
     * Sets the block or target ID.
     *
     * @param blockOrTargetID The ID to be set.
     */
    public void setBlockOrTargetID(final String blockOrTargetID) {
        this.blockOrTargetID = blockOrTargetID;
    }

    /**
     * Returns the target name or block opcode.
     *
     * @return The name.
     */
    public String getNameOrOpcode() {
        return nameOrOpcode;
    }

    /**
     * Sets the target name or block opcode.
     *
     * @param nameOrOpcode The name or opcode to be set.
     */
    public void setNameOrOpcode(final String nameOrOpcode) {
        this.nameOrOpcode = nameOrOpcode;
    }

    /**
     * Returns the original value.
     *
     * @return The value.
     */
    public Integer getOriginal() {
        return original;
    }

    /**
     * Sets the original value.
     *
     * @param original The value to be set.
     */
    public void setOriginal(final Integer original) {
        this.original = original;
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

    /**
     * Converts the debugger event DTO into a human-readable string representation.
     *
     * @return A human-readable string representation of the debugger event DTO.
     */
    @Override
    public String toString() {
        return "DebuggerEventDTO{"
                + "id=" + id
                + ", user=" + user
                + ", experiment=" + experiment
                + ", date=" + date
                + ", eventType=" + eventType
                + ", event=" + event
                + ", blockOrTargetID='" + blockOrTargetID + '\''
                + ", nameOrOpcode='" + nameOrOpcode + '\''
                + ", original=" + original
                + '}';
    }

}
