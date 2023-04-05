package fim.unipassau.de.scratch1984.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

/**
 * An entity representing an experiment.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

}
