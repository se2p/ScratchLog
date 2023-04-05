package fim.unipassau.de.scratch1984.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
     * The timestamp at which the question event occurred.
     */
    @Column(name = "date")
    private Timestamp date;

    /**
     * A String representing the {@link fim.unipassau.de.scratch1984.web.dto.QuestionEventDTO.QuestionEventType}.
     */
    @Column(name = "event_type")
    private String eventType;

    /**
     * A String representing the {@link fim.unipassau.de.scratch1984.web.dto.QuestionEventDTO.QuestionEvent}.
     */
    @Column(name = "event")
    private String event;

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
