package fim.unipassau.de.scratch1984.web.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A DTO representing a click event that resulted from user mouse or key interactions in the Scratch GUI.
 */
public class ClickEventDTO {

    //TODO just use String instead?
    /**
     * All possible event types for a click event.
     */
    public enum ClickEventType {
        /**
         * The event was caused by user interaction with the mouse.
         */
        MOUSE,

        /**
         * The event was caused by user interatcion with the keyboard.
         */
        KEYBOARD
    }

    /**
     * All possible specific events for a click event.
     */
    public enum ClickEvent {

        /**
         * The user released their mouse.
         */
        MOUSEUP,

        /**
         * The user pressed their mouse.
         */
        MOUSEDOWN,

        /**
         * The user pressed a key on their keyboard.
         */
        KEYDOWN
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
     * The local date time at which the mouse or keyboard interaction occurred in the Scratch GUI.
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
     * The x-coordinate on the screen of a mouse event or the name of the key pressed.
     */
    private String xKey;

    /**
     * The y-coordinate on the screen of a mouse event or the value entered with the key press.
     */
    private String yCode;

    /**
     * The class of the element being the target of this click event.
     */
    private String targetClass;

    /**
     * The node name of the target element.
     */
    private String nodeName;

    //TODO sinnvoll?
    /**
     * If the target element was a button, this information is stored here.
     */
    private String button;

    //TODO sinnvoll?
    /**
     * The id of the target element.
     */
    private String dataID;

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
     * Returns the x-coordinate or the key name of the event.
     *
     * @return The respective value.
     */
    public String getxKey() {
        return xKey;
    }

    /**
     * Sets the x-coordinate or key name of the event.
     *
     * @param xKey The value to be set.
     */
    public void setxKey(final String xKey) {
        this.xKey = xKey;
    }

    /**
     * Returns the y-coordinate or the key value of the event.
     *
     * @return The respective value.
     */
    public String getyCode() {
        return yCode;
    }

    /**
     * Sets the y-coordinate or key value of the event.
     *
     * @param yCode The value to be set.
     */
    public void setyCode(final String yCode) {
        this.yCode = yCode;
    }

    /**
     * Returns the target class of the event.
     *
     * @return The target class.
     */
    public String getTargetClass() {
        return targetClass;
    }

    /**
     * Sets the target class of the event.
     *
     * @param targetClass The target class to be set.
     */
    public void setTargetClass(final String targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * Returns the node name of the event.
     *
     * @return The node name.
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Sets the node name of the event.
     *
     * @param nodeName The node name to be set.
     */
    public void setNodeName(final String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * Returns the button of the event.
     *
     * @return The button.
     */
    public String getButton() {
        return button;
    }

    /**
     * Sets the button of the event.
     *
     * @param button The button to be set.
     */
    public void setButton(final String button) {
        this.button = button;
    }

    /**
     * Returns the data ID of the event.
     *
     * @return The data ID.
     */
    public String getDataID() {
        return dataID;
    }

    /**
     * Sets the data ID of the event.
     *
     * @param dataID The ID to be set.
     */
    public void setDataID(final String dataID) {
        this.dataID = dataID;
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
                + ", xKey='" + xKey + '\''
                + ", yCode='" + yCode + '\''
                + ", targetClass='" + targetClass + '\''
                + ", nodeName='" + nodeName + '\''
                + ", button='" + button + '\''
                + ", dataID='" + dataID + '\''
                + '}';
    }

}
