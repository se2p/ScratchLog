package fim.unipassau.de.scratch1984.persistence.entity;

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
     * Default constructor for the block event entity.
     */
    public BlockEvent() {
    }

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
     * @return The respective user.
     */
    @Override
    public User getUser() {
        return user;
    }

    /**
     * {@inheritDoc}
     *
     * @param user The event user to be set.
     */
    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * {@inheritDoc}
     *
     * @return The respective experiment.
     */
    @Override
    public Experiment getExperiment() {
        return experiment;
    }

    /**
     * {@inheritDoc}
     *
     * @param experiment The event experiment to be set.
     */
    @Override
    public void setExperiment(final Experiment experiment) {
        this.experiment = experiment;
    }

    /**
     * {@inheritDoc}
     *
     * @return The respective timestamp.
     */
    @Override
    public Timestamp getDate() {
        return date;
    }

    /**
     * {@inheritDoc}
     *
     * @param date The event timestamp to be set.
     */
    @Override
    public void setDate(final Timestamp date) {
        this.date = date;
    }

    /**
     * {@inheritDoc}
     *
     * @return The event type.
     */
    @Override
    public String getEventType() {
        return eventType;
    }

    /**
     * {@inheritDoc}
     *
     * @param eventType The event type to be set.
     */
    @Override
    public void setEventType(final String eventType) {
        this.eventType = eventType;
    }

    /**
     * {@inheritDoc}
     *
     * @return The respective event.
     */
    @Override
    public String getEvent() {
        return event;
    }

    /**
     * {@inheritDoc}
     *
     * @param event The event to be set.
     */
    @Override
    public void setEvent(final String event) {
        this.event = event;
    }

    /**
     * Returns the spritename of the event.
     *
     * @return The respective spritename.
     */
    public String getSprite() {
        return sprite;
    }

    /**
     * Sets the spritename of the event.
     *
     * @param sprite The name of the sprite to be set.
     */
    public void setSprite(final String sprite) {
        this.sprite = sprite;
    }

    /**
     * Returns the metadata of the event.
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
     * Returns the xml of the event.
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
     * Returns the code of the event.
     *
     * @return The respective code.
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

}
