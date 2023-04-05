package fim.unipassau.de.scratch1984.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * An entity representing the number users participating in an experiment, those who started and those who finished the
 * experiment.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "experiment_data")
public class ExperimentData {

    /**
     * The ID of the experiment.
     */
    @Id
    @Column(name = "experiment")
    private Integer experiment;

    /**
     * The number of participants for the experiment.
     */
    @Column(name = "participants")
    private int participants;

    /**
     * The number of participants who started the experiment.
     */
    @Column(name = "started")
    private int started;

    /**
     * The number of participants who finished the experiment.
     */
    @Column(name = "finished")
    private int finished;

}
