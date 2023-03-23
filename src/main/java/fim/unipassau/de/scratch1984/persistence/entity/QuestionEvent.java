package fim.unipassau.de.scratch1984.persistence.entity;

import fim.unipassau.de.scratch1984.util.enums.QuestionEventSpecific;
import fim.unipassau.de.scratch1984.util.enums.QuestionEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * An entity representing a question event that resulted from user interaction with the questions in the Scratch
 * debugger.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class QuestionEvent implements Event {

    /**
     * The unique ID of the question event.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The {@link User} who caused the question event.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The {@link Experiment} during which the question event occurred.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private Experiment experiment;

    /**
     * The datetime at which the question event occurred.
     */
    @Column(name = "date")
    private LocalDateTime date;

    /**
     * The type of question event that occurred.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private QuestionEventType eventType;

    /**
     * The specific event that occurred.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event")
    private QuestionEventSpecific event;

    /**
     * The feedback for the question, if any.
     */
    @Column(name = "feedback")
    private Integer feedback;

    /**
     * The type of question of the event.
     */
    @Column(name = "q_type")
    private String type;

    /**
     * Any values associated with the event.
     */
    @Column(name = "q_values")
    private String values;

    /**
     * The question category of the event.
     */
    @Column(name = "category")
    private String category;

    /**
     * The form of question event, negative or positive.
     */
    @Column(name = "form")
    private String form;

    /**
     * The block id of the question event.
     */
    @Column(name = "block_id")
    private String blockID;

    /**
     * The block opcode of the question event.
     */
    @Column(name = "opcode")
    private String opcode;

}
