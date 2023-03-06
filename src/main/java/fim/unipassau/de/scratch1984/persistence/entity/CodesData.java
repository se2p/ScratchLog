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
import javax.persistence.Table;

/**
 * An entity representing the number of xml codes generated for a user during an experiment.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "codes_data")
@IdClass(CodesDataId.class)
public class CodesData {

    /**
     * The ID of the user to whom the data belongs.
     */
    @Id
    @Column(name = "user")
    private Integer user;

    /**
     * The ID of the experiment for which the xml code was saved.
     */
    @Id
    @Column(name = "experiment")
    private Integer experiment;

    /**
     * The number of times the an xml code was generated.
     */
    @Column(name = "count")
    private int count;

}
