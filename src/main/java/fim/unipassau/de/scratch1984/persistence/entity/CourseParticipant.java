package fim.unipassau.de.scratch1984.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

/**
 * An entity representing a participation in a course.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
     * The datetime at which the user was added as participant to the course.
     */
    @Column(name = "added")
    private LocalDateTime added;

}
