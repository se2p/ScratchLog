package fim.unipassau.de.scratch1984.persistence.entity;

import fim.unipassau.de.scratch1984.util.enums.BlockEventSpecific;
import fim.unipassau.de.scratch1984.util.enums.BlockEventType;
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
     * The datetime at which the block event occurred.
     */
    @Column(name = "date")
    private LocalDateTime date;

    /**
     * The type of block event that occurred.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private BlockEventType eventType;

    /**
     * The specific event that occurred.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event")
    private BlockEventSpecific event;

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
    public BlockEvent(final User user, final Experiment experiment, final LocalDateTime date,
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

}
