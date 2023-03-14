package fim.unipassau.de.scratch1984.web.dto;

import fim.unipassau.de.scratch1984.util.enums.BlockEventSpecific;
import fim.unipassau.de.scratch1984.util.enums.BlockEventType;
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
    private BlockEventSpecific event;

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
                         final BlockEventType eventType, final BlockEventSpecific event, final String sprite,
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
