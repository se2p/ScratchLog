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
     * The unique title of the experiment.
     */
    @Column(unique = true, name = "title")
    private String title;

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
     * Boolean indicating whether the experiment is running or not.
     */
    @Column(name = "active")
    private boolean active;

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
     * Returns the title of the experiment.
     *
     * @return The experiment title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the experiment.
     *
     * @param name The experiment title to be set.
     */
    public void setTitle(final String name) {
        this.title = name;
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

    /**
     * Returns whether the experiment is currently running.
     *
     * @return The experiment status.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the running status the experiment.
     *
     * @param active The status.
     */
    public void setActive(final boolean active) {
        this.active = active;
    }

}
