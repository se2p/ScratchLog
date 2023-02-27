package fim.unipassau.de.scratch1984.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;

/**
 * An entity representing an experiment conducted as part of a course.
 */
@Entity
@IdClass(CourseExperimentId.class)
public class CourseExperiment {

    /**
     * The {@link Course} to which the experiment belongs.
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    /**
     * The {@link Experiment} conducted as part of the course.
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private Experiment experiment;

    /**
     * The timestamp at which the experiment was added to the course.
     */
    @Column(name = "added")
    private Timestamp added;

    /**
     * Default constructor for the course experiment entity.
     */
    public CourseExperiment() {
    }

    /**
     * Constructs a new course participant with the given attributes.
     *
     * @param course The course in which the experiment is conducted.
     * @param experiment The experiment conducted as part of the course.
     * @param added The timestamp at which the experiment was added to the course.
     */
    public CourseExperiment(final Course course, final Experiment experiment, final Timestamp added) {
        this.course = course;
        this.experiment = experiment;
        this.added = added;
    }

    /**
     * Returns the course in which the experiment is conducted.
     *
     * @return The respective course.
     */
    public Course getCourse() {
        return course;
    }

    /**
     * Sets the course in which the experiment is conducted.
     *
     * @param course The course to be set.
     */
    public void setCourse(final Course course) {
        this.course = course;
    }

    /**
     * Returns the experiment that is part of the course.
     *
     * @return The respective experiment.
     */
    public Experiment getExperiment() {
        return experiment;
    }

    /**
     * Sets the experiment as part of the course.
     *
     * @param experiment The experiment to be set.
     */
    public void setExperiment(final Experiment experiment) {
        this.experiment = experiment;
    }

    /**
     * Returns the timestamp at which the experiment was added to the course.
     *
     * @return The timestamp.
     */
    public Timestamp getAdded() {
        return added;
    }

    /**
     * Sets the timestamp at which the experiment was added to the course.
     *
     * @param added The timestamp to be set.
     */
    public void setAdded(final Timestamp added) {
        this.added = added;
    }

}
