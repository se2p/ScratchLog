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
 * An entity representing a debugger event that resulted from user interaction with the Scratch debugger,
 * not including interaction with questions.
 */
@Entity
public class DebuggerEvent {

    /**
     * The unique ID of the debugger event.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The {@link User} who caused the debugger event.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The {@link Experiment} during which the debugger event occurred.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private Experiment experiment;

    /**
     * The timestamp at which the debugger event occurred.
     */
    @Column(name = "date")
    private Timestamp date;

    /**
     * A String representing the {@link fim.unipassau.de.scratch1984.web.dto.DebuggerEventDTO.DebuggerEventType}.
     */
    @Column(name = "event_type")
    private String eventType;

    /**
     * A String representing the {@link fim.unipassau.de.scratch1984.web.dto.DebuggerEventDTO.DebuggerEvent}.
     */
    @Column(name = "event")
    private String event;

    /**
     * The block or target id of the debugger event.
     */
    @Column(name = "block_target_id")
    private String blockOrTargetID;

    /**
     * The target name or block opcode of the debugger event.
     */
    @Column(name = "name_opcode")
    private String nameOrOpcode;

    /**
     * Only applicable to select sprite event, null for everything else.
     */
    @Column(name = "original")
    private Integer original;

    /**
     * Default constructor for the debugger event entity.
     */
    public DebuggerEvent() {
    }

    /**
     * Constructs a new debugger event with the given attributes.
     *
     * @param user The user who caused the event.
     * @param experiment The experiment during which the event occurred.
     * @param date The time at which the event occurred.
     * @param eventType The event type.
     * @param event The specific event.
     * @param blockOrTargetID The block or target ID of the event.
     * @param nameOrOpcode The target name or block opcode of the event.
     * @param original Only applicable to the select sprite event.
     */
    public DebuggerEvent(final User user, final Experiment experiment, final Timestamp date, final String eventType,
                         final String event, final String blockOrTargetID, final String nameOrOpcode,
                         final Integer original) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.eventType = eventType;
        this.event = event;
        this.blockOrTargetID = blockOrTargetID;
        this.nameOrOpcode = nameOrOpcode;
        this.original = original;
    }

    /**
     * Returns the ID of the event.
     *
     * @return The event ID.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the ID of the event.
     *
     * @param id The event ID to be set.
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * Returns the user of the event.
     *
     * @return The respective user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user of the event.
     *
     * @param user The event user to be set.
     */
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * Returns the experiment of the event.
     *
     * @return The respective experiment.
     */
    public Experiment getExperiment() {
        return experiment;
    }

    /**
     * Sets the experiment of the event.
     *
     * @param experiment The event experiment to be set.
     */
    public void setExperiment(final Experiment experiment) {
        this.experiment = experiment;
    }

    /**
     * Returns the timestamp of the event.
     *
     * @return The respective timestamp.
     */
    public Timestamp getDate() {
        return date;
    }

    /**
     * Sets the timestamp of the event.
     *
     * @param date The event timestamp to be set.
     */
    public void setDate(final Timestamp date) {
        this.date = date;
    }

    /**
     * Returns the type of the event.
     *
     * @return The event type.
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Sets the type of the event.
     *
     * @param eventType The event type to be set.
     */
    public void setEventType(final String eventType) {
        this.eventType = eventType;
    }

    /**
     * Returns the concrete event that occurred.
     *
     * @return The respective event.
     */
    public String getEvent() {
        return event;
    }

    /**
     * Sets the concrete event that occurred.
     *
     * @param event The event to be set.
     */
    public void setEvent(final String event) {
        this.event = event;
    }

    /**
     * Returns the block or target ID.
     *
     * @return The ID.
     */
    public String getBlockOrTargetID() {
        return blockOrTargetID;
    }

    /**
     * Sets the block or target ID.
     *
     * @param blockOrTargetID The ID to be set.
     */
    public void setBlockOrTargetID(final String blockOrTargetID) {
        this.blockOrTargetID = blockOrTargetID;
    }

    /**
     * Returns the target name or block opcode.
     *
     * @return The name.
     */
    public String getNameOrOpcode() {
        return nameOrOpcode;
    }

    /**
     * Sets the target name or block opcode.
     *
     * @param nameOrOpcode The name or opcode to be set.
     */
    public void setNameOrOpcode(final String nameOrOpcode) {
        this.nameOrOpcode = nameOrOpcode;
    }

    /**
     * Returns the original value.
     *
     * @return The value.
     */
    public Integer getOriginal() {
        return original;
    }

    /**
     * Sets the original value.
     *
     * @param original The value to be set.
     */
    public void setOriginal(final Integer original) {
        this.original = original;
    }

}
