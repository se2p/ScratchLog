package fim.unipassau.de.scratch1984.web.dto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * A DTO representing a question event that resulted from user interaction with the Scratch debugger questions.
 */
public class QuestionEventDTO implements EventDTO {

    /**
     * All possible event types for a question event.
     */
    public enum QuestionEventType {
        /**
         * The event was caused by selecting or rating a question.
         */
        QUESTION,

        /**
         * The event was caused by opening a question category.
         */
        QUESTION_CATEGORY
    }

    /**
     * All possible specific events for a question event.
     */
    public enum QuestionEvent {
        /**
         * The user opened a question category.
         */
        OPEN_CATEGORY,

        /**
         * The user selected a question.
         */
        SELECT,

        /**
         * The user rated a question.
         */
        RATE
    }

    /**
     * The unique ID of the question event.
     */
    private Integer id;

    /**
     * The ID of the user who caused the event.
     */
    private Integer user;

    /**
     * The ID of the experiment during which the event occurred.
     */
    private Integer experiment;

    /**
     * The local date time at which the question interaction occurred in the Scratch GUI.
     */
    private LocalDateTime date;

    /**
     * The type of question event that occurred.
     */
    private QuestionEventType eventType;

    /**
     * The specific event that occurred.
     */
    private QuestionEvent event;

    /**
     * The feedback for the question, if any.
     */
    private Integer feedback;

    /**
     * The type of question of the event.
     */
    private String type;

    /**
     * Any values associated with the event.
     */
    private String[] values;

    /**
     * The question category of the event.
     */
    private String category;

    /**
     * The form of question event, negative or positive.
     */
    private String form;

    /**
     * The block id of the question event.
     */
    private String blockID;

    /**
     * The block opcode of the question event.
     */
    private String opcode;

    /**
     * Default constructor for the question event dto.
     */
    public QuestionEventDTO() {
    }

    /**
     * Constructs a new question event dto with the given attributes.
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
    public QuestionEventDTO(final Integer user, final Integer experiment, final LocalDateTime date,
                            final QuestionEventType eventType, final QuestionEvent event, final Integer feedback,
                            final String type, final String[] values, final String category, final String form,
                            final String blockID, final String opcode) {
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
     * @return The user's ID.
     */
    @Override
    public Integer getUser() {
        return user;
    }

    /**
     * {@inheritDoc}
     *
     * @param user The user ID to be set.
     */
    @Override
    public void setUser(final Integer user) {
        this.user = user;
    }

    /**
     * {@inheritDoc}
     *
     * @return The experiment ID.
     */
    @Override
    public Integer getExperiment() {
        return experiment;
    }

    /**
     * {@inheritDoc}
     *
     * @param experiment The experiment ID to be set.
     */
    @Override
    public void setExperiment(final Integer experiment) {
        this.experiment = experiment;
    }

    /**
     * {@inheritDoc}
     *
     * @return The event time.
     */
    @Override
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * {@inheritDoc}
     *
     * @param date The time to be set.
     */
    @Override
    public void setDate(final LocalDateTime date) {
        this.date = date;
    }

    /**
     * Returns the type of the event.
     *
     * @return The event type.
     */
    public QuestionEventType getEventType() {
        return eventType;
    }

    /**
     * Sets the type of the event.
     *
     * @param eventType The event type to be set.
     */
    public void setEventType(final QuestionEventType eventType) {
        this.eventType = eventType;
    }

    /**
     * Returns the specific event that occurred.
     *
     * @return The event.
     */
    public QuestionEvent getEvent() {
        return event;
    }

    /**
     * Sets the specific event that occurred.
     *
     * @param event The event to be set.
     */
    public void setEvent(final QuestionEvent event) {
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
    public String[] getValues() {
        return values;
    }

    /**
     * Sets the question values.
     *
     * @param values The values to be set.
     */
    public void setValues(final String[] values) {
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

    /**
     * Indicates whether some {@code other} question event DTO is semantically equal to this question event DTO.
     *
     * @param other The object to compare this question event DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent question event DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        QuestionEventDTO that = (QuestionEventDTO) other;
        return Objects.equals(id, that.id);
    }

    /**
     * Calculates a hash code for this question event DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the question event DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Converts the question event DTO into a human-readable string representation.
     *
     * @return A human-readable string representation of the question event DTO.
     */
    @Override
    public String toString() {
        return "QuestionEventDTO{"
                + "id=" + id
                + ", user=" + user
                + ", experiment=" + experiment
                + ", date=" + date
                + ", eventType=" + eventType
                + ", event=" + event
                + ", feedback=" + feedback
                + ", type='" + type + '\''
                + ", values=" + Arrays.toString(values)
                + ", category='" + category + '\''
                + ", form='" + form + '\''
                + ", blockID='" + blockID + '\''
                + ", opcode='" + opcode + '\''
                + '}';
    }

}
