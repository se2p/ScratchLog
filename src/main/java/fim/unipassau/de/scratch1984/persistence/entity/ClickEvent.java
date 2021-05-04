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
 * An entity representing a click event that resulted from user mouse or key interactions in the Scratch GUI.
 */
@Entity
public class ClickEvent {

    /**
     * The unique ID of the click event.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The {@link User} who caused the click event.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private User user;

    /**
     * The {@link Experiment} during which the click event occurred.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private Experiment experiment;

    /**
     * The timestamp at which the click event occurred.
     */
    @Column(name = "date")
    private Timestamp date;

    /**
     * A String representing the {@link fim.unipassau.de.scratch1984.web.dto.ClickEventDTO.ClickEventType}.
     */
    @Column(name = "event_type")
    private String eventType;

    /**
     * A String representing the {@link fim.unipassau.de.scratch1984.web.dto.ClickEventDTO.ClickEvent}.
     */
    @Column(name = "event")
    private String event;

    /**
     * The x-coordinate on the screen of a mouse event or the name of the key pressed.
     */
    @Column(name = "x_or_key")
    private String xKey;

    /**
     * The y-coordinate on the screen of a mouse event or the value entered with the key press.
     */
    @Column(name = "y_or_code")
    private String yCode;

    /**
     * The class of the element being the target of this click event.
     */
    @Column(name = "class")
    private String targetClass;

    /**
     * The node name of the target element.
     */
    @Column(name = "node_name")
    private String nodeName;

    //TODO sinnvoll?
    /**
     * If the target element was a button, this information is stored here.
     */
    @Column(name = "button")
    private String button;

    //TODO sinnvoll?
    /**
     * The id of the target element.
     */
    @Column(name = "data_id")
    private String dataID;

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
     * Returns the x-coordinate or the key name.
     *
     * @return The respective coordinate or key name.
     */
    public String getxKey() {
        return xKey;
    }

    /**
     * Sets the x-coordinate or key name.
     *
     * @param xKey The coordinate or name to be set.
     */
    public void setxKey(final String xKey) {
        this.xKey = xKey;
    }

    /**
     * Returns the y-coordinate or the key value.
     *
     * @return The respective coordinate or key value.
     */
    public String getyCode() {
        return yCode;
    }

    /**
     * Sets the y-coordinate or the key value.
     *
     * @param yCode The coordinate or value to be set.
     */
    public void setyCode(final String yCode) {
        this.yCode = yCode;
    }

    /**
     * Returns the target class of the event.
     *
     * @return The target class.
     */
    public String getTargetClass() {
        return targetClass;
    }

    /**
     * Sets the target class of the event.
     *
     * @param targetClass The class to be set.
     */
    public void setTargetClass(final String targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * Returns the node name of the event.
     *
     * @return The respective node name.
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Sets the node name of the event.
     *
     * @param nodeName The name to be set.
     */
    public void setNodeName(final String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * Returns the button that was clicked during the event.
     *
     * @return The respective button.
     */
    public String getButton() {
        return button;
    }

    /**
     * Sets the button of the event.
     *
     * @param button The button to be set.
     */
    public void setButton(final String button) {
        this.button = button;
    }

    /**
     * Returns the ID of the target element.
     *
     * @return The target element ID.
     */
    public String getDataID() {
        return dataID;
    }

    /**
     * Sets the ID of the target element.
     *
     * @param dataID The ID to be set.
     */
    public void setDataID(final String dataID) {
        this.dataID = dataID;
    }

}
