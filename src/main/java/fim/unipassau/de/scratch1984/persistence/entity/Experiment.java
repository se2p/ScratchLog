package fim.unipassau.de.scratch1984.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * An entity representing an experiment.
 */
@Entity
public class Experiment {

    /**
     * The unique ID of the experiment.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The unique name of the experiment.
     */
    @Column(unique = true, name = "name")
    private String name;

    /**
     * The short description text of the experiment.
     */
    @Column(name = "description")
    private String description;

    /**
     * The information text of the experiment.
     */
    @Column(name = "infotext")
    private String info;

    /**
     * Returns the ID of the experiment.
     *
     * @return The experiment ID.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the ID of the experiment.
     *
     * @param id The experiment ID to be set.
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * Returns the name of the experiment.
     *
     * @return The experiment name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the experiment.
     *
     * @param name The experiment name to be set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the description of the experiment.
     *
     * @return The experiment description text.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the experiment.
     *
     * @param description The experiment description to be set.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Returns the information text of the experiment.
     *
     * @return The experiment information text.
     */
    public String getInfo() {
        return info;
    }

    /**
     * Sets the information text of the experiment.
     *
     * @param info The experiment information text to be set.
     */
    public void setInfo(final String info) {
        this.info = info;
    }

}
