package fim.unipassau.de.scratch1984.web.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A DTO representing a block event that resulted from user interaction with a Scratch block, the green flag or stop
 * all icon, or a sprite rename.
 */
public class BlockEventDTO {

    //TODO just use String instead?
    /**
     * All possible event types for a block event.
     */
    public enum BlockEventType {
        /**
         * The event was caused by a direct interaction with a block.
         */
        BLOCK,

        /**
         * The event was caused by a mouse click.
         */
        CLICK,

        /**
         * The event was caused by a sprite rename.
         */
        RENAME

        //TODO alternativ:
        /*
        CLICK,
        RENAME,
        CREATE,
        CHANGE,
        MOVE,
        DELETE,
        DRAG
        */
    }

    /**
     * All possible specific events for a block event.
     */
    public enum BlockEvent {
        /**
         * The user clicked on the green flag icon.
         */
        GREENFLAG,

        /**
         * The user clicked on the stop all icon.
         */
        STOPALL,

        /**
         * The user renamed the sprite.
         */
        SPRITE,

        /**
         * The user clicked on a block.
         */
        STACKCLICK,

        /**
         * The user created a new block.
         */
        CREATE,

        /**
         * The user changed an existing block.
         */
        CHANGE,

        /**
         * The user moved a block.
         */
        MOVE,

        /**
         * The user dragged a block outside.
         */
        DRAGOUTSIDE,

        /**
         * The user dragged a block onto another.
         */
        ENDDRAGONTO,

        /**
         * The user finished dragging the block.
         */
        ENDDRAG,

        /**
         * The user deleted a block.
         */
        DELETE,

        /**
         * The user created a global variable.
         */
        VAR_CREATE_GLOBAL,

        /**
         * The user created a local variable.
         */
        VAR_CREATE_LOCAL,

        /**
         * The user renamed a global variable.
         */
        VAR_RENAME_GLOBAL,

        /**
         * The user renamed a local variable.
         */
        VAR_RENAME_LOCAL,

        /**
         * The user deleted a variable.
         */
        VAR_DELETE,

        /**
         * The user created a comment.
         */
        COMMENT_CREATE,

        /**
         * The user changed a comment.
         */
        COMMENT_CHANGE,

        /**
         * The user moved a comment.
         */
        COMMENT_MOVE,

        /**
         * The user deleted a comment.
         */
        COMMENT_DELETE

        //TODO alternativ:
        /*
        GREENFLAG,
        STOPALL,
        SPRITE,
        STACK,
        CREATE,
        CHANGE,
        MOVE,
        DRAGOUTSIDE,
        ENDDRAGONTO,
        ENDDRAG,
        DELETE,
        VAR_GLOBAL,
        VAR_LOCAL,
        VAR,
        COMMENT
        */
    }

    /**
     * The unique ID of the block event.
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
     * The local date time at which the block interaction occurred in the Scratch GUI.
     */
    private LocalDateTime date;

    /**
     * The type of block event that occurred.
     */
    private BlockEventType eventType;

    /**
     * The specific event that occurred.
     */
    private BlockEvent event;

    /**
     * The name of the sprite on which the event occurred.
     */
    private String sprite;

    /**
     * Additional information about the event.
     */
    private String metadata;

    /**
     * An xml representation of the blocks on the sprite after the event occurred.
     */
    private String xml;

    /**
     * The Scratch project state after the event saved in a json format.
     */
    private String code;

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
    public BlockEventType getEventType() {
        return eventType;
    }

    /**
     * Sets the type of the event.
     *
     * @param eventType The event type to be set.
     */
    public void setEventType(final BlockEventType eventType) {
        this.eventType = eventType;
    }

    /**
     * Returns the specific event that occurred.
     *
     * @return The event.
     */
    public BlockEvent getEvent() {
        return event;
    }

    /**
     * Sets the specific event that occurred.
     *
     * @param event The event to be set.
     */
    public void setEvent(final BlockEvent event) {
        this.event = event;
    }

    /**
     * Returns the spritename of the event.
     *
     * @return The spritename.
     */
    public String getSprite() {
        return sprite;
    }

    /**
     * Sets the spritename of the event.
     *
     * @param sprite The name to be set.
     */
    public void setSprite(final String sprite) {
        this.sprite = sprite;
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
     * Returns the xml saved for this event, if any.
     *
     * @return The xml.
     */
    public String getXml() {
        return xml;
    }

    /**
     * Sets the xml of the event.
     *
     * @param xml The xml to be set.
     */
    public void setXml(final String xml) {
        this.xml = xml;
    }

    /**
     * Returns the code saved for this event, if any.
     *
     * @return The code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the code of the event.
     *
     * @param code The code to be set.
     */
    public void setCode(final String code) {
        this.code = code;
    }

    /**
     * Indicates whether some {@code other} block event DTO is semantically equal to this block event DTO.
     *
     * @param other The object to compare this block event DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent block event DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        BlockEventDTO that = (BlockEventDTO) other;
        return id.equals(that.id);
    }

    /**
     * Calculates a hash code for this block event DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the block event DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Converts the block event DTO into a human-readable string representation.
     *
     * @return A human-readable string representation of the block event DTO.
     */
    @Override
    public String toString() {
        return "BlockEventDTO{"
                + "id=" + id
                + ", user=" + user
                + ", experiment=" + experiment
                + ", date=" + date
                + ", eventType=" + eventType
                + ", event=" + event
                + ", sprite='" + sprite + '\''
                + ", metadata='" + metadata + '\''
                + ", xml='" + xml + '\''
                + ", code='" + code + '\''
                + '}';
    }

}
