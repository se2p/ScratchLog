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
 * An entity representing a click event that resulted from user interaction with a button, icon, or similar event.
 */
@Entity
public class ClickEvent implements Event {

    /**
     * The unique ID of the click event.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The {@link User} who caused the click event.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The {@link Experiment} during which the click event occurred.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private Experiment experiment;

    /**
     * The timestamp at which the click event occurred.
     */
    @Column(name = "date")
    private Timestamp date;

    /**
     * A String representing the {@link fim.unipassau.de.scratch1984.web.dto.ClickEventDTO.ClickEventType}.
     */
    @Column(name = "event_type")
    private String eventType;

    /**
     * A String representing the {@link fim.unipassau.de.scratch1984.web.dto.ClickEventDTO.ClickEvent}.
     */
    @Column(name = "event")
    private String event;

    /**
     * Additional information about the event.
     */
    @Column(name = "metadata")
    private String metadata;

    /**
     * Default constructor for the click event entity.
     */
    public ClickEvent() {
    }

    /**
     * Constructs a new click event with the given attributes.
     *
     * @param user The user who caused the event.
     * @param experiment The experiment during which the event occurred.
     * @param date The time at which the event occurred.
     * @param eventType The event type.
     * @param event The specific event.
     * @param metadata The metadata.
     */
    public ClickEvent(final User user, final Experiment experiment, final Timestamp date, final String eventType,
                      final String event, final String metadata) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.eventType = eventType;
        this.event = event;
        this.metadata = metadata;
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

}
