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
 * An entity representing a resource event being the result of a user adding costumes or sounds in the Scratch GUI.
 */
@Entity
public class ResourceEvent implements Event {

    /**
     * The unique ID of the resource event.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The {@link User} who caused the resource event.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The {@link Experiment} during which the resource event occurred.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private Experiment experiment;

    /**
     * The timestamp at which the resource event occurred.
     */
    @Column(name = "date")
    private Timestamp date;

    /**
     * A String representing the {@link fim.unipassau.de.scratch1984.web.dto.ResourceEventDTO.ResourceEventType}.
     */
    @Column(name = "event_type")
    private String eventType;

    /**
     * A String representing the {@link fim.unipassau.de.scratch1984.web.dto.ResourceEventDTO.ResourceEvent}.
     */
    @Column(name = "event")
    private String event;

    /**
     * The name of the resource.
     */
    @Column(name = "name")
    private String resourceName;

    /**
     * The md5 hash value of the resource.
     */
    @Column(name = "md5")
    private String hash;

    /**
     * The filetype of the resource.
     */
    @Column(name = "type")
    private String resourceType;

    /**
     * A String representing the {@link fim.unipassau.de.scratch1984.web.dto.ResourceEventDTO.LibraryResource}.
     */
    @Column(name = "library")
    private Integer libraryResource;

    /**
     * Default constructor for the resource event entity.
     */
    public ResourceEvent() {
    }

    /**
     * Constructs a new resource event with the given attributes.
     *
     * @param user The user who caused the event.
     * @param experiment The experiment during which the event occurred.
     * @param date The time at which the event occurred.
     * @param eventType The event type.
     * @param event The specific event.
     * @param resourceName The name of the resource.
     * @param hash The md5 hash value of the resource.
     * @param resourceType The filetype of the resource.
     * @param libraryResource Whether the resource is from the Scratch library or not.
     */
    public ResourceEvent(final User user, final Experiment experiment, final Timestamp date, final String eventType,
                         final String event, final String resourceName, final String hash, final String resourceType,
                         final Integer libraryResource) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.eventType = eventType;
        this.event = event;
        this.resourceName = resourceName;
        this.hash = hash;
        this.resourceType = resourceType;
        this.libraryResource = libraryResource;
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
     * Returns the name of the resource.
     *
     * @return The resource name.
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Sets the name of the resource.
     *
     * @param resourceName The name to be set.
     */
    public void setResourceName(final String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * Returns the md5 hash of the resource.
     *
     * @return The hash value.
     */
    public String getHash() {
        return hash;
    }

    /**
     * Sets the md5 hash of the resource.
     *
     * @param hash The hash value to be set.
     */
    public void setHash(final String hash) {
        this.hash = hash;
    }

    /**
     * Returns the file type of the resource.
     *
     * @return The file type.
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * Sets the type of the resource.
     *
     * @param resourceType The file type to be set.
     */
    public void setResourceType(final String resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * Returns the library status of the resource.
     *
     * @return The library status.
     */
    public Integer getLibraryResource() {
        return libraryResource;
    }

    /**
     * Sets the library status of the resource.
     *
     * @param libraryResource The status to be set.
     */
    public void setLibraryResource(final Integer libraryResource) {
        this.libraryResource = libraryResource;
    }

}
