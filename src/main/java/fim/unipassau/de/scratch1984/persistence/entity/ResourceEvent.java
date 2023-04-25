package fim.unipassau.de.scratch1984.persistence.entity;

import fim.unipassau.de.scratch1984.util.enums.ResourceEventSpecific;
import fim.unipassau.de.scratch1984.util.enums.ResourceEventType;
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
     * The datetime at which the resource event occurred.
     */
    @Column(name = "date")
    private LocalDateTime date;

    /**
     * The type of resource event that occurred.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private ResourceEventType eventType;

    /**
     * The specific event that occurred.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event")
    private ResourceEventSpecific event;

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
     * A String representing the {@link fim.unipassau.de.scratch1984.util.enums.LibraryResource}.
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
    public ResourceEvent(final User user, final Experiment experiment, final LocalDateTime date,
                         final ResourceEventType eventType, final ResourceEventSpecific event,
                         final String resourceName, final String hash, final String resourceType,
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
