package fim.unipassau.de.scratch1984.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * An entity representing the number of xml codes generated for a user during an experiment.
 */
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

    /**
     * Default constructor for the codes data entity.
     */
    public CodesData() {
    }

    /**
     * Constructs a new codes data entity with the given attributes.
     *
     * @param user The id of the user for whom the code was saved.
     * @param experiment The id of the experiment during which the code was saved.
     * @param count The number of times an xml code was saved.
     */
    public CodesData(final Integer user, final Integer experiment, final int count) {
        this.user = user;
        this.experiment = experiment;
        this.count = count;
    }

    /**
     * Returns the ID of the user to whom this data belongs.
     *
     * @return The user ID.
     */
    public Integer getUser() {
        return user;
    }

    /**
     * Sets the user ID.
     *
     * @param user The user ID to be set.
     */
    public void setUser(final Integer user) {
        this.user = user;
    }

    /**
     * Returns the ID of the experiment to which this data belongs.
     *
     * @return The experiment ID.
     */
    public Integer getExperiment() {
        return experiment;
    }

    /**
     * Sets the experiment ID.
     *
     * @param experiment The experiment ID to be set.
     */
    public void setExperiment(final Integer experiment) {
        this.experiment = experiment;
    }

    /**
     * Returns the calculated count value.
     *
     * @return The counted occurrences.
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets the count value.
     *
     * @param count The value to be set.
     */
    public void setCount(final int count) {
        this.count = count;
    }

}
