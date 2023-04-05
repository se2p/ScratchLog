package fim.unipassau.de.scratch1984.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 * An entity representing the number of times a user executed a specific event during an experiment.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@IdClass(EventCountId.class)
public class EventCount {

    /**
     * The ID of the user to whom the data belongs.
     */
    @Id
    @Column(name = "user")
    private Integer user;

    /**
     * The ID of the experiment in which the counted events occurred.
     */
    @Id
    @Column(name = "experiment")
    private Integer experiment;

    /**
     * The number of times the event occurred.
     */
    @Column(name = "count")
    private int count;

    /**
     * The event for which its occurrences have been counted.
     */
    @Id
    @Column(name = "event")
    private String event;

}
