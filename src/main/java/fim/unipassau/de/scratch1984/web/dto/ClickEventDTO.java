package fim.unipassau.de.scratch1984.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A DTO representing a click event that resulted from user interaction with a button, icon, or similar event.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ClickEventDTO implements EventDTO {

    /**
     * All possible event types for a click event.
     */
    public enum ClickEventType {
        /**
         * The event was caused by clicking on a button.
         */
        BUTTON,

        /**
         * The event was caused by clicking on an icon.
         */
        ICON,

        /**
         * The event was caused by clicking on code segments.
         */
        CODE
    }

    /**
     * All possible specific events for a click event.
     */
    public enum ClickEvent {
        /**
         * The user clicked on the green flag icon.
         */
        GREENFLAG,

        /**
         * The user clicked on the stop all icon.
         */
        STOPALL,

        /**
         * The user clicked on a block.
         */
        STACKCLICK,

        /**
         * The user rewound the execution slider.
         */
        REWIND_EXECUTION_SLIDER_CHANGE,

        /**
         * The user revisited the previous step in the block execution.
         */
        STEP_BACK,

        /**
         * The user jumped over an execution step.
         */
        STEP_OVER,

        /**
         * The user paused the execution of a code block.
         */
        PAUSE_EXECUTION,

        /**
         * The user resumed the execution of a code block.
         */
        RESUME_EXECUTION,

        /**
         * The user deactivated the observation.
         */
        DEACTIVATE_OBSERVATION,

        /**
         * The user activated the observation.
         */
        ACTIVATE_OBSERVATION,

        /**
         * The user closed the debugger.
         */
        CLOSE_DEBUGGER
    }

    /**
     * The unique ID of the click event.
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
     * The local date time at which the click interaction occurred in the Scratch GUI.
     */
    private LocalDateTime date;

    /**
     * The type of click event that occurred.
     */
    private ClickEventType eventType;

    /**
     * The specific event that occurred.
     */
    private ClickEvent event;

    /**
     * Additional information about the event.
     */
    private String metadata;

    /**
     * Constructs a new click event dto with the given attributes.
     *
     * @param user The id of the user who caused the event.
     * @param experiment The id of the experiment during which the event occurred.
     * @param date The time at which the event occurred.
     * @param eventType The type of event.
     * @param event The specific event.
     * @param metadata The metadata.
     */
    public ClickEventDTO(final Integer user, final Integer experiment, final LocalDateTime date,
                         final ClickEventType eventType, final ClickEvent event, final String metadata) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.eventType = eventType;
        this.event = event;
        this.metadata = metadata;
    }

    /**
     * Indicates whether some {@code other} click event DTO is semantically equal to this click event DTO.
     *
     * @param other The object to compare this click event DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent click event DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ClickEventDTO that = (ClickEventDTO) other;
        return Objects.equals(id, that.id);
    }

    /**
     * Calculates a hash code for this click event DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the click event DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
