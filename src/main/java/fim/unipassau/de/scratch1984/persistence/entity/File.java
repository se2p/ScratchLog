package fim.unipassau.de.scratch1984.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;

/**
 * An entity representing a file.
 */
@Entity
public class File {

    /**
     * The unique ID of the file.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The timestamp at which the file was added in the Scratch GUI.
     */
    @Column(name = "date")
    private Timestamp date;

    /**
     * The name of the file.
     */
    @Column(name = "name")
    private String name;

    /**
     * The type of the file.
     */
    @Column(name = "type")
    private String filetype;

    /**
     * The file content.
     */
    @Column(name = "content")
    private byte[] content;

    /**
     * The {@link User} who added the file.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The {@link Experiment} in which the file was added.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private Experiment experiment;

    /**
     * Returns the ID of the file.
     *
     * @return The file ID.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the ID of the file.
     *
     * @param id The file ID to be set.
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * Returns the timestamp at which the file was added.
     *
     * @return The respective timestamp.
     */
    public Timestamp getDate() {
        return date;
    }

    /**
     * Sets the timestamp of the file.
     *
     * @param date The timestamp to be set.
     */
    public void setDate(final Timestamp date) {
        this.date = date;
    }

    /**
     * Returns the name of the file.
     *
     * @return The file name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the file.
     *
     * @param name The file name to be set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the type of the file.
     *
     * @return The filetype.
     */
    public String getFiletype() {
        return filetype;
    }

    /**
     * Sets the type of the file.
     *
     * @param filetype The filetyepe to be set.
     */
    public void setFiletype(final String filetype) {
        this.filetype = filetype;
    }

    /**
     * Returns the file content.
     *
     * @return The file content.
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Sets the content of the file.
     *
     * @param content The file content to be set.
     */
    public void setContent(final byte[] content) {
        this.content = content;
    }

    /**
     * Returns the user who added the file.
     *
     * @return The respective user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user who added the file.
     *
     * @param user The respective user.
     */
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * Returns the experiment during which the file was added.
     *
     * @return The respective experiment.
     */
    public Experiment getExperiment() {
        return experiment;
    }

    /**
     * Sets the experiment during which the file was added.
     *
     * @param experiment The respective experiment.
     */
    public void setExperiment(final Experiment experiment) {
        this.experiment = experiment;
    }

}
