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
 * A DTO representing a resource event that resulted from a user adding, renaming or deleting a resource in the Scratch
 * GUI.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ResourceEventDTO implements EventDTO {

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
     * Constructs a new resource event dto with the given attributes.
     *
     * @param user The id of the user who caused the event.
     * @param experiment The id of the experiment during which the event occurred.
     * @param date The time at which the event occurred.
     * @param eventType The type of event.
     * @param event The specific event.
     * @param name The name of the resource.
     * @param md5 The md5 hash of the resource.
     * @param filetype The filetype of the resource.
     * @param libraryResource Whether or not the resource is external.
     */
    public ResourceEventDTO(final Integer user, final Integer experiment, final LocalDateTime date,
                            final ResourceEventType eventType, final ResourceEvent event, final String name,
                            final String md5, final String filetype, final LibraryResource libraryResource) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.eventType = eventType;
        this.event = event;
        this.name = name;
        this.md5 = md5;
        this.filetype = filetype;
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
        return Objects.equals(id, that.id);
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

}
