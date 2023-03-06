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
 * An entity representing a resource event being the result of a user adding costumes or sounds in the Scratch GUI.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

}
