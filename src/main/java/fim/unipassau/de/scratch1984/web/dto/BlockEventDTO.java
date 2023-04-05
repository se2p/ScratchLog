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
 * A DTO representing a block event that resulted from user interaction with a Scratch block, the green flag or stop
 * all icon, or a sprite rename.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BlockEventDTO implements EventDTO {

    /**
     * All possible event types for a block event.
     */
    public enum BlockEventType {
        /**
         * The event was caused by a mouse click.
         */
        CLICK,

        /**
         * The event was caused by the renaming of a variable or sprite.
         */
        RENAME,

        /**
         * The event was caused by the creation of a block, comment or variable.
         */
        CREATE,

        /**
         * The event was caused by the change of an existent block or comment.
         */
        CHANGE,

        /**
         * The event was caused by a block being moved.
         */
        MOVE,

        /**
         * The event was caused by the deletion of a block, variable or comment.
         */
        DELETE,

        /**
         * The event was caused by dragging a block.
         */
        DRAG
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
     * Constructs a new block event dto with the given attributes.
     *
     * @param user The id of the user who caused the event.
     * @param experiment The id of the experiment during which the event occurred.
     * @param date The time at which the event occurred.
     * @param eventType The type of event.
     * @param event The specific event.
     * @param sprite The name of the sprite.
     * @param metadata The metadata.
     * @param xml The current xml.
     * @param code The current json code.
     */
    public BlockEventDTO(final Integer user, final Integer experiment, final LocalDateTime date,
                         final BlockEventType eventType, final BlockEvent event, final String sprite,
                         final String metadata, final String xml, final String code) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.eventType = eventType;
        this.event = event;
        this.sprite = sprite;
        this.metadata = metadata;
        this.xml = xml;
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
        return Objects.equals(id, that.id);
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

}
