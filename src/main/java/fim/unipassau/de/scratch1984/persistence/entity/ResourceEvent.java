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
public class ResourceEvent {

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
    @JoinColumn(name = "id")
    private User user;

    /**
     * The {@link Experiment} during which the resource event occurred.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
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
     * Returns the user of the event.
     *
     * @return The respective user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user of the event.
     *
     * @param user The event user to be set.
     */
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * Returns the experiment of the event.
     *
     * @return The respective experiment.
     */
    public Experiment getExperiment() {
        return experiment;
    }

    /**
     * Sets the experiment of the event.
     *
     * @param experiment The event experiment to be set.
     */
    public void setExperiment(final Experiment experiment) {
        this.experiment = experiment;
    }

    /**
     * Returns the timestamp of the event.
     *
     * @return The respective timestamp.
     */
    public Timestamp getDate() {
        return date;
    }

    /**
     * Sets the timestamp of the event.
     *
     * @param date The event timestamp to be set.
     */
    public void setDate(final Timestamp date) {
        this.date = date;
    }

    /**
     * Returns the type of the event.
     *
     * @return The event type.
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Sets the type of the event.
     *
     * @param eventType The event type to be set.
     */
    public void setEventType(final String eventType) {
        this.eventType = eventType;
    }

    /**
     * Returns the concrete event that occurred.
     *
     * @return The respective event.
     */
    public String getEvent() {
        return event;
    }

    /**
     * Sets the concrete event that occurred.
     *
     * @param event The event to be set.
     */
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
