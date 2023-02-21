package fim.unipassau.de.scratch1984.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Formula;

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
     * The text shown after the user has finished the experiment.
     */
    @Column(name = "postscript")
    private String postscript;

    /**
     * Boolean indicating whether the experiment is running or not.
     */
    @Column(name = "active")
    private boolean active;

    /**
     * Boolean indicating whether the experiment is part of a course.
     */
    @Formula("(SELECT EXISTS(SELECT c.experiment_id FROM course_experiment AS c WHERE c.experiment_id = id))")
    private boolean courseExperiment;

    /**
     * The URL of the instrumented Scratch-GUI instance to be used for this experiment.
     */
    @Column(name = "gui_url")
    private String guiURL;

    /**
     * The sb3 project to load on experiment start.
     */
    private byte[] project;

    /**
     * Default constructor for the experiment entity.
     */
    public Experiment() {
    }

    /**
     * Constructs a new experiment with the given attributes.
     *
     * @param id The experiment id.
     * @param title The experiment title.
     * @param description The experiment description.
     * @param info The experiment information text.
     * @param postscript The postscript text.
     * @param active Whether the experiment is currently running or not.
     * @param courseExperiment Whether the experiment is part of a course or not.
     * @param guiURL The URL of the instrumented Scratch-GUI this experiment uses.
     */
    public Experiment(final Integer id, final String title, final String description, final String info,
                      final String postscript, final boolean active, final boolean courseExperiment,
                      final String guiURL) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.info = info;
        this.postscript = postscript;
        this.active = active;
        this.courseExperiment = courseExperiment;
        this.guiURL = guiURL;
    }

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
     * Returns the postscript of the experiment.
     *
     * @return The postscript text.
     */
    public String getPostscript() {
        return postscript;
    }

    /**
     * Sets the text shown after the user has finished the experiment.
     *
     * @param postscript The postscript to be set.
     */
    public void setPostscript(final String postscript) {
        this.postscript = postscript;
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

    /**
     * Returns whether the experiment is part of a course.
     *
     * @return The experiment course status.
     */
    public boolean isCourseExperiment() {
        return courseExperiment;
    }

    /**
     * Returns the current sb3 project.
     *
     * @return The sb3 project.
     */
    public byte[] getProject() {
        return project;
    }

    /**
     * Sets the current sb3 project.
     *
     * @param project The project.
     */
    public void setProject(final byte[] project) {
        this.project = project;
    }

    /**
     * Returns whether the GUI-URL of the experiment.
     *
     * @return The GUI-URL.
     */
    public String getGuiURL() {
        return guiURL;
    }

    /**
     * Sets the GUI_URL of the experiment.
     *
     * @param guiURL The GUI_URL.
     */
    public void setGuiURL(final String guiURL) {
        this.guiURL = guiURL;
    }

}
