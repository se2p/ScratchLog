package fim.unipassau.de.scratch1984.persistence.entity;

import fim.unipassau.de.scratch1984.util.enums.ClickEventSpecific;
import fim.unipassau.de.scratch1984.util.enums.ClickEventType;
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
 * An entity representing a click event that resulted from user interaction with a button, icon, or similar event.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
     * The datetime at which the click event occurred.
     */
    @Column(name = "date")
    private LocalDateTime date;

    /**
     * The type of click event that occurred.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private ClickEventType eventType;

    /**
     * The specific event that occurred.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event")
    private ClickEventSpecific event;

    /**
     * Additional information about the event.
     */
    @Column(name = "metadata")
    private String metadata;

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
    public ClickEvent(final User user, final Experiment experiment, final LocalDateTime date,
                      final ClickEventType eventType, final ClickEventSpecific event, final String metadata) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.eventType = eventType;
        this.event = event;
        this.metadata = metadata;
    }

}
