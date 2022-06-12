package fim.unipassau.de.scratch1984.web.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A DTO representing a click event that resulted from user interaction with a button, icon, or similar event.
 */
public class ClickEventDTO {

    /**
     * All possible event types for a click event.
     */
    public enum ClickEventType {
        /**
         * The event was caused by clicking on a button.
         */
        BUTTON,

        /**
         * The event was caused by clicking on an icon.
         */
        ICON,

        /**
         * The event was caused by clicking on code segments.
         */
        CODE
    }

    /**
     * All possible specific events for a block event.
     */
    public enum ClickEvent {
        /**
         * The user clicked on the green flag icon.
         */
        GREENFLAG,

        /**
         * The user clicked on the stop all icon.
         */
        STOPALL,

        /**
         * The user clicked on a block.
         */
        STACKCLICK
    }

    /**
     * The unique ID of the click event.
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
     * The local date time at which the click interaction occurred in the Scratch GUI.
     */
    private LocalDateTime date;

    /**
     * The type of click event that occurred.
     */
    private ClickEventType eventType;

    /**
     * The specific event that occurred.
     */
    private ClickEvent event;

    /**
     * Additional information about the event.
     */
    private String metadata;

    /**
     * Default constructor for the click event dto.
     */
    public ClickEventDTO() {
    }

    /**
     * Constructs a new click event dto with the given attributes.
     *
     * @param user The id of the user who caused the event.
     * @param experiment The id of the experiment during which the event occurred.
     * @param date The time at which the event occurred.
     * @param eventType The type of event.
     * @param event The specific event.
     * @param metadata The metadata.
     */
    public ClickEventDTO(final Integer user, final Integer experiment, final LocalDateTime date,
                         final ClickEventType eventType, final ClickEvent event, final String metadata) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.eventType = eventType;
        this.event = event;
        this.metadata = metadata;
    }

    /**
     * Returns the ID of the event.
     *
     * @return The event ID.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the ID of the event.
     *
     * @param id The event ID to be set.
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * Returns the ID of the user who caused the event.
     *
     * @return The user's ID.
     */
    public Integer getUser() {
        return user;
    }

    /**
     * Sets the user ID of the event.
     *
     * @param user The user ID to be set.
     */
    public void setUser(final Integer user) {
        this.user = user;
    }

    /**
     * Returns the ID of the experiment where the event occurred.
     *
     * @return The experiment ID.
     */
    public Integer getExperiment() {
        return experiment;
    }

    /**
     * Sets the experiment ID of the event.
     *
     * @param experiment The experiment ID to be set.
     */
    public void setExperiment(final Integer experiment) {
        this.experiment = experiment;
    }

    /**
     * Returns the time at which the event occurred.
     *
     * @return The event time.
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * Sets the local date time of the event.
     *
     * @param date The time to be set.
     */
    public void setDate(final LocalDateTime date) {
        this.date = date;
    }

    /**
     * Returns the type of the event.
     *
     * @return The event type.
     */
    public ClickEventType getEventType() {
        return eventType;
    }

    /**
     * Sets the type of the event.
     *
     * @param eventType The event type to be set.
     */
    public void setEventType(final ClickEventType eventType) {
        this.eventType = eventType;
    }

    /**
     * Returns the specific event that occurred.
     *
     * @return The event.
     */
    public ClickEvent getEvent() {
        return event;
    }

    /**
     * Sets the specific event that occurred.
     *
     * @param event The event to be set.
     */
    public void setEvent(final ClickEvent event) {
        this.event = event;
    }

    /**
     * Returns the metadata of this event, if any.
     *
     * @return The metadata.
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata of the event.
     *
     * @param metadata The metadata to be set.
     */
    public void setMetadata(final String metadata) {
        this.metadata = metadata;
    }

    /**
     * Indicates whether some {@code other} click event DTO is semantically equal to this click event DTO.
     *
     * @param other The object to compare this click event DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent click event DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ClickEventDTO that = (ClickEventDTO) other;
        return id.equals(that.id);
    }

    /**
     * Calculates a hash code for this click event DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the click event DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Converts the click event DTO into a human-readable string representation.
     *
     * @return A human-readable string representation of the click event DTO.
     */
    @Override
    public String toString() {
        return "ClickEventDTO{"
                + "id=" + id
                + ", user=" + user
                + ", experiment=" + experiment
                + ", date=" + date
                + ", eventType=" + eventType
                + ", event=" + event
                + ", metadata='" + metadata + '\''
                + '}';
    }

}
