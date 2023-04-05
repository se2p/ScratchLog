package fim.unipassau.de.scratch1984.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;

/**
 * An entity representing a block event that resulted from user interaction with a Scratch block or a sprite rename.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class BlockEvent implements Event {

    /**
     * The unique ID of the block event.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The {@link User} who caused the block event.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The {@link Experiment} during which the block event occurred.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private Experiment experiment;

    /**
     * The timestamp at which the block event occurred.
     */
    @Column(name = "date")
    private Timestamp date;

    /**
     * A String representing the {@link fim.unipassau.de.scratch1984.web.dto.BlockEventDTO.BlockEventType}.
     */
    @Column(name = "event_type")
    private String eventType;

    /**
     * A String representing the {@link fim.unipassau.de.scratch1984.web.dto.BlockEventDTO.BlockEvent}.
     */
    @Column(name = "event")
    private String event;

    /**
     * The name of the sprite on which the event occurred.
     */
    @Column(name = "spritename")
    private String sprite;

    /**
     * Additional information about the event.
     */
    @Column(name = "metadata")
    private String metadata;

    /**
     * An xml representation of the blocks on the sprite after the event occurred.
     */
    @Column(name = "xml")
    private String xml;

    /**
     * The Scratch project state after the event saved in a json format.
     */
    @Column(name = "json")
    private String code;

    /**
     * Constructs a new block event with the given attributes.
     *
     * @param user The user who caused the event.
     * @param experiment The experiment during which the event occurred.
     * @param date The time at which the event occurred.
     * @param eventType The event type.
     * @param event The specific event.
     * @param sprite The name of the sprite.
     * @param metadata The metadata.
     * @param xml The current xml.
     * @param code The current json.
     */
    public BlockEvent(final User user, final Experiment experiment, final Timestamp date, final String eventType,
                      final String event, final String sprite, final String metadata, final String xml,
                      final String code) {
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

}
