package fim.unipassau.de.scratch1984.web.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A DTO representing a resource event that resulted from a user adding, renaming or deleting a resource in the Scratch
 * GUI.
 */
public class ResourceEventDTO {

    /**
     * All possible event types for a resource event.
     */
    public enum ResourceEventType {
        /**
         * The event was caused by a user adding a resource.
         */
        ADD,

        /**
         * The event was caused by a user renaming a resource.
         */
        RENAME,

        /**
         * The event was caused by a user deleting a resource.
         */
        DELETE
    }

    /**
     * All possible specific events for a resource event.
     */
    public enum ResourceEvent {
        /**
         * The user deleted a costume or backdrop.
         */
        DELETE_COSTUME,

        /**
         * The user deleted a sound.
         */
        DELETE_SOUND,

        /**
         * The user added a costume or backdrop.
         */
        ADD_COSTUME,

        /**
         * The user added a sound.
         */
        ADD_SOUND,

        /**
         * The user renamed a costume.
         */
        RENAME_COSTUME,

        /**
         * The user renamed a backdrop.
         */
        RENAME_BACKDROP,

        /**
         * The user renamed a sound.
         */
        RENAME_SOUND
    }

    /**
     * All possible library status values for a resource event.
     */
    public enum LibraryResource {
        /**
         * The resource is from the Scratch library.
         */
        TRUE,

        /**
         * The resource is a user-produced resource.
         */
        FALSE,

        /**
         * It is unknown, whether the resource is from the library or user-produced.
         */
        UNKNOWN
    }

    /**
     * The unique ID of the resource event.
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
     * The local date time at which the block interaction occurred in the Scratch GUI.
     */
    private LocalDateTime date;

    /**
     * The type of resource event that occurred.
     */
    private ResourceEventType eventType;

    /**
     * The specific event that occurred.
     */
    private ResourceEvent event;

    /**
     * The name of the resource.
     */
    private String name;

    /**
     * The md5 hash value of the resource.
     */
    private String md5;

    /**
     * The filetype of the resource.
     */
    private String filetype;

    /**
     * Whether the file is from the Scratch library or unknown.
     */
    private LibraryResource libraryResource;

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
     * Returns the ID of the user who caused the event.
     *
     * @return The user's ID.
     */
    public Integer getUser() {
        return user;
    }

    /**
     * Sets the user ID of the event.
     *
     * @param user The user ID to be set.
     */
    public void setUser(final Integer user) {
        this.user = user;
    }

    /**
     * Returns the ID of the experiment where the event occurred.
     *
     * @return The experiment ID.
     */
    public Integer getExperiment() {
        return experiment;
    }

    /**
     * Sets the experiment ID of the event.
     *
     * @param experiment The experiment ID to be set.
     */
    public void setExperiment(final Integer experiment) {
        this.experiment = experiment;
    }

    /**
     * Returns the time at which the event occurred.
     *
     * @return The event time.
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * Sets the local date time of the event.
     *
     * @param date The time to be set.
     */
    public void setDate(final LocalDateTime date) {
        this.date = date;
    }

    /**
     * Returns the type of the event.
     *
     * @return The event type.
     */
    public ResourceEventType getEventType() {
        return eventType;
    }

    /**
     * Sets the type of the event.
     *
     * @param eventType The event type to be set.
     */
    public void setEventType(final ResourceEventType eventType) {
        this.eventType = eventType;
    }

    /**
     * Returns the specific event that occurred.
     *
     * @return The event.
     */
    public ResourceEvent getEvent() {
        return event;
    }

    /**
     * Sets the specific event that occurred.
     *
     * @param event The event to be set.
     */
    public void setEvent(final ResourceEvent event) {
        this.event = event;
    }

    /**
     * Returns the name of the resource.
     *
     * @return The resource name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the resource.
     *
     * @param name The name to be set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the md5 hash value of the resource.
     *
     * @return The hash value.
     */
    public String getMd5() {
        return md5;
    }

    /**
     * Sets the md5 hash value of the resource.
     *
     * @param md5 The value to be set.
     */
    public void setMd5(final String md5) {
        this.md5 = md5;
    }

    /**
     * Returns the filetype of the resource.
     *
     * @return The filetype.
     */
    public String getFiletype() {
        return filetype;
    }

    /**
     * Sets the filetype of the resource.
     *
     * @param filetype The filetype to be set.
     */
    public void setFiletype(final String filetype) {
        this.filetype = filetype;
    }

    /**
     * Returns the library status of the resource.
     *
     * @return The library status.
     */
    public LibraryResource getLibraryResource() {
        return libraryResource;
    }

    /**
     * Sets the library status of the resource.
     *
     * @param libraryResource The status to be set.
     */
    public void setLibraryResource(final LibraryResource libraryResource) {
        this.libraryResource = libraryResource;
    }

    /**
     * Indicates whether some {@code other} resource event DTO is semantically equal to this resource event DTO.
     *
     * @param other The object to compare this resource event DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent resource event DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ResourceEventDTO that = (ResourceEventDTO) other;
        return id.equals(that.id);
    }

    /**
     * Calculates a hash code for this resource event DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the resource event DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Converts the resource event DTO into a human-readable string representation.
     *
     * @return A human-readable string representation of the resource event DTO.
     */
    @Override
    public String toString() {
        return "ResourceEventDTO{"
                + "id=" + id
                + ", user=" + user
                + ", experiment=" + experiment
                + ", date=" + date
                + ", eventType=" + eventType
                + ", event=" + event
                + ", name='" + name + '\''
                + ", md5='" + md5 + '\''
                + ", filetype='" + filetype + '\''
                + ", libraryResource=" + libraryResource
                + '}';
    }

}