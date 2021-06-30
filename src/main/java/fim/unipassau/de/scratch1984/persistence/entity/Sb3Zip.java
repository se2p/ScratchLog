package fim.unipassau.de.scratch1984.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * An entity representing an sb3 zip file.
 */
@Entity
@Table(name = "sb3_zip")
public class Sb3Zip {

    /**
     * The unique ID of the zip file.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The {@link User} for whom the zip file was created.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The {@link Experiment} in which the zip file was created.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private Experiment experiment;

    /**
     * The timestamp at which the zip file was created by the Scratch VM.
     */
    @Column(name = "date")
    private Timestamp date;

    /**
     * The name of the zip file.
     */
    @Column(name = "name")
    private String name;

    /**
     * The zip file content.
     */
    @Column(name = "content")
    private byte[] content;

    /**
     * Returns the ID of the zip file.
     *
     * @return The file ID.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the ID of the zip file.
     *
     * @param id The file ID to be set.
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * Returns the user who added the zip file.
     *
     * @return The respective user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user who added the zip file.
     *
     * @param user The respective user.
     */
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * Returns the experiment during which the zip file was created.
     *
     * @return The respective experiment.
     */
    public Experiment getExperiment() {
        return experiment;
    }

    /**
     * Sets the experiment during which the zip file was created.
     *
     * @param experiment The respective experiment.
     */
    public void setExperiment(final Experiment experiment) {
        this.experiment = experiment;
    }

    /**
     * Returns the timestamp at which the zip file was created.
     *
     * @return The respective timestamp.
     */
    public Timestamp getDate() {
        return date;
    }

    /**
     * Sets the timestamp of the zip file.
     *
     * @param date The timestamp to be set.
     */
    public void setDate(final Timestamp date) {
        this.date = date;
    }

    /**
     * Returns the name of the zip file.
     *
     * @return The file name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the zip file.
     *
     * @param name The file name to be set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the zip file content.
     *
     * @return The file content.
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Sets the content of the zip file.
     *
     * @param content The file content to be set.
     */
    public void setContent(final byte[] content) {
        this.content = content;
    }

}
