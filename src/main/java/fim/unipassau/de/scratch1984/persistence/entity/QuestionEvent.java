package fim.unipassau.de.scratch1984.persistence.entity;

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

    /**
     * Default constructor for the question event entity.
     */
    public QuestionEvent() {
    }

    /**
     * Constructs a new question event with the given attributes.
     *
     * @param user The id of the user who caused the event.
     * @param experiment The id of the experiment during which the event occurred.
     * @param date The time at which the event occurred.
     * @param eventType The type of event.
     * @param event The specific event.
     * @param feedback The feedback for the question.
     * @param type The type of question.
     * @param values The values of the question.
     * @param category The question category.
     * @param form The question form.
     * @param blockID The block ID of the event.
     * @param opcode The block opcode of the event.
     */
    public QuestionEvent(final User user, final Experiment experiment, final Timestamp date, final String eventType,
                         final String event, final Integer feedback, final String type, final String values,
                         final String category, final String form, final String blockID, final String opcode) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.eventType = eventType;
        this.event = event;
        this.feedback = feedback;
        this.type = type;
        this.values = values;
        this.category = category;
        this.form = form;
        this.blockID = blockID;
        this.opcode = opcode;
    }

    /**
     * {@inheritDoc}
     *
     * @return The event ID.
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     *
     * @param id The event ID to be set.
     */
    @Override
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     *
     * @return The respective user.
     */
    @Override
    public User getUser() {
        return user;
    }

    /**
     * {@inheritDoc}
     *
     * @param user The event user to be set.
     */
    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * {@inheritDoc}
     *
     * @return The respective experiment.
     */
    @Override
    public Experiment getExperiment() {
        return experiment;
    }

    /**
     * {@inheritDoc}
     *
     * @param experiment The event experiment to be set.
     */
    @Override
    public void setExperiment(final Experiment experiment) {
        this.experiment = experiment;
    }

    /**
     * {@inheritDoc}
     *
     * @return The respective timestamp.
     */
    @Override
    public Timestamp getDate() {
        return date;
    }

    /**
     * {@inheritDoc}
     *
     * @param date The event timestamp to be set.
     */
    @Override
    public void setDate(final Timestamp date) {
        this.date = date;
    }

    /**
     * {@inheritDoc}
     *
     * @return The event type.
     */
    @Override
    public String getEventType() {
        return eventType;
    }

    /**
     * {@inheritDoc}
     *
     * @param eventType The event type to be set.
     */
    @Override
    public void setEventType(final String eventType) {
        this.eventType = eventType;
    }

    /**
     * {@inheritDoc}
     *
     * @return The respective event.
     */
    @Override
    public String getEvent() {
        return event;
    }

    /**
     * {@inheritDoc}
     *
     * @param event The event to be set.
     */
    @Override
    public void setEvent(final String event) {
        this.event = event;
    }

    /**
     * Returns the feedback for the question.
     *
     * @return The feedback.
     */
    public Integer getFeedback() {
        return feedback;
    }

    /**
     * Sets the question feedback.
     *
     * @param feedback The feedback to be set.
     */
    public void setFeedback(final Integer feedback) {
        this.feedback = feedback;
    }

    /**
     * Returns the type of question.
     *
     * @return The type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the question type.
     *
     * @param type The type to be set.
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Returns the values of the question.
     *
     * @return The values.
     */
    public String getValues() {
        return values;
    }

    /**
     * Sets the question values.
     *
     * @param values The values to be set.
     */
    public void setValues(final String values) {
        this.values = values;
    }

    /**
     * Returns the question category.
     *
     * @return The category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the question category.
     *
     * @param category The category to be set.
     */
    public void setCategory(final String category) {
        this.category = category;
    }

    /**
     * Returns the question form.
     *
     * @return The form.
     */
    public String getForm() {
        return form;
    }

    /**
     * Sets the question form.
     *
     * @param form The form to be set.
     */
    public void setForm(final String form) {
        this.form = form;
    }

    /**
     * Returns the block ID.
     *
     * @return The ID.
     */
    public String getBlockID() {
        return blockID;
    }

    /**
     * Sets the block ID.
     *
     * @param blockID The ID to be set.
     */
    public void setBlockID(final String blockID) {
        this.blockID = blockID;
    }

    /**
     * Returns the block opcode.
     *
     * @return The opcode.
     */
    public String getOpcode() {
        return opcode;
    }

    /**
     * Sets the block opcode.
     *
     * @param opcode The opcode to be set.
     */
    public void setOpcode(final String opcode) {
        this.opcode = opcode;
    }

}
