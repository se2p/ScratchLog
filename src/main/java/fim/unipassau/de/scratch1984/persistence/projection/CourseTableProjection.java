package fim.unipassau.de.scratch1984.persistence.projection;

/**
 * Projection interface for the {@link fim.unipassau.de.scratch1984.persistence.entity.Course} class to return only
 * the id, title, description, and status.
 */
public interface CourseTableProjection {

    /**
     * Returns the unique id of the course.
     *
     * @return The course id.
     */
    Integer getId();

    /**
     * Returns the unique title of the course.
     *
     * @return The title.
     */
    String getTitle();

    /**
     * Returns the description of the course.
     *
     * @return The description.
     */
    String getDescription();

    /**
     * Returns whether the course is currently being conducted.
     *
     * @return The course status.
     */
    boolean isActive();

}
