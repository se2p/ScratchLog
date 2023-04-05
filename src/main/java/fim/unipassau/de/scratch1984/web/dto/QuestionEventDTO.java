package fim.unipassau.de.scratch1984.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import fim.unipassau.de.scratch1984.util.enums.QuestionEventSpecific;
import fim.unipassau.de.scratch1984.util.enums.QuestionEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A DTO representing a question event that resulted from user interaction with the Scratch debugger questions.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionEventDTO implements EventDTO {

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
    @JsonProperty("time")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime date;

    /**
     * The type of question event that occurred.
     */
    @JsonProperty("type")
    private QuestionEventType eventType;

    /**
     * The specific event that occurred.
     */
    private QuestionEventSpecific event;

    /**
     * The feedback for the question, if any.
     */
    private Integer feedback;

    /**
     * The type of question of the event.
     */
    @JsonProperty("q_type")
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
                            final QuestionEventType eventType, final QuestionEventSpecific event,
                            final Integer feedback, final String type, final String[] values, final String category,
                            final String form, final String blockID, final String opcode) {
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

}
