package fim.unipassau.de.scratch1984.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity
public class Course {

    /**
     * The unique ID of the course.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The unique title of the course.
     */
    @Column(unique = true, name = "title")
    private String title;

    /**
     * The short description text of the course.
     */
    @Column(name = "description")
    private String description;

    /**
     * The content text containing further information about the course.
     */
    @Column(name = "content")
    private String content;

    /**
     * Boolean indicating whether the course is currently being conducted or not.
     */
    @Column(name = "active")
    private boolean active;

    /**
     * The timestamp at which the course information was last updated or an experiment or participant added.
     */
    @Column(name = "last_changed")
    private Timestamp lastChanged;

    /**
     * Default constructor for the course entity.
     */
    public Course() {
    }

    /**
     * Constructs a new course with the given attributes.
     *
     * @param id The course id.
     * @param title The course title.
     * @param description The course description.
     * @param content The course content text.
     * @param active Whether the course is currently being conducted or not.
     * @param lastChanged The last time at which the course was updated.
     */
    public Course(final Integer id, final String title, final String description, final String content,
                  final boolean active, final Timestamp lastChanged) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.active = active;
        this.lastChanged = lastChanged;
    }

    /**
     * Returns the ID of the course.
     *
     * @return The course ID.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the ID of the course.
     *
     * @param id The course ID to be set.
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * Returns the title of the course.
     *
     * @return The course title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the course.
     *
     * @param name The course title to be set.
     */
    public void setTitle(final String name) {
        this.title = name;
    }

    /**
     * Returns the description of the course.
     *
     * @return The course description text.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the course.
     *
     * @param description The course description to be set.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Returns the content of the course.
     *
     * @return The course content text.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of the course.
     *
     * @param content The course content to be set.
     */
    public void setContent(final String content) {
        this.content = content;
    }

    /**
     * Returns whether the course is currently being conducted.
     *
     * @return The course status.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the status of the course.
     *
     * @param active The status.
     */
    public void setActive(final boolean active) {
        this.active = active;
    }

    /**
     * Returns the timestamp at which the course has last been updated.
     *
     * @return The timestamp.
     */
    public Timestamp getLastChanged() {
        return lastChanged;
    }

    /**
     * Sets the timestamp of the last change to the course.
     *
     * @param lastChanged The timestamp to be set.
     */
    public void setLastChanged(final Timestamp lastChanged) {
        this.lastChanged = lastChanged;
    }

}
