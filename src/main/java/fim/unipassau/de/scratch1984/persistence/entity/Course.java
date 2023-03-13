package fim.unipassau.de.scratch1984.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

/**
 * An entity representing a course.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
     * The datetime at which the course information was last updated or an experiment or participant added.
     */
    @Column(name = "last_changed")
    private LocalDateTime lastChanged;

}
