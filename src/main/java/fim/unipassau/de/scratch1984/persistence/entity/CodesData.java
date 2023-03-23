package fim.unipassau.de.scratch1984.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
