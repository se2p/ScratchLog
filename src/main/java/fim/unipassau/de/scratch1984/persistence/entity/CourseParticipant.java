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
 * An entity representing a participation in a course.
 */
@Entity
@IdClass(CourseParticipantId.class)
public class CourseParticipant {

    /**
     * The {@link User} representing the course participant.
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The {@link Course} in which the user participates.
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    /**
     * The timestamp at which the user was added as participant to the course.
     */
    @Column(name = "added")
    private Timestamp added;

    /**
     * Default constructor for the course participant entity.
     */
    public CourseParticipant() {
    }

    /**
     * Constructs a new course participant with the given attributes.
     *
     * @param user The participating user.
     * @param course The course in which the user is participating.
     * @param added The timestamp at which the user was added to the course.
     */
    public CourseParticipant(final User user, final Course course, final Timestamp added) {
        this.user = user;
        this.course = course;
        this.added = added;
    }

    /**
     * Returns the participating user.
     *
     * @return The respective user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the participating user.
     *
     * @param user The user to be set.
     */
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * Returns the course in which the user is participating.
     *
     * @return The respective course.
     */
    public Course getCourse() {
        return course;
    }

    /**
     * Sets the course in which the user is participating.
     *
     * @param course The course to be set.
     */
    public void setCourse(final Course course) {
        this.course = course;
    }

    /**
     * Returns the timestamp at which the user was added to the course.
     *
     * @return The timestamp.
     */
    public Timestamp getAdded() {
        return added;
    }

    /**
     * Sets the timestamp at which the user was added to the course.
     *
     * @param added The timestamp to be set.
     */
    public void setAdded(final Timestamp added) {
        this.added = added;
    }

}
