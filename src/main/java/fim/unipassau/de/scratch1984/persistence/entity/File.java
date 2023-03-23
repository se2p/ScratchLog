package fim.unipassau.de.scratch1984.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * An entity representing a file.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class File {

    /**
     * The unique ID of the file.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The datetime at which the file was added in the Scratch GUI.
     */
    @Column(name = "date")
    private LocalDateTime date;

    /**
     * The name of the file.
     */
    @Column(name = "name")
    private String name;

    /**
     * The type of the file.
     */
    @Column(name = "type")
    private String filetype;

    /**
     * The file content.
     */
    @Column(name = "content")
    private byte[] content;

    /**
     * The {@link User} who added the file.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The {@link Experiment} in which the file was added.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private Experiment experiment;

    /**
     * Constructs a new file with the given attributes.
     *
     * @param user The user who uploaded the file.
     * @param experiment The experiment during which the file was uploaded.
     * @param date The datetime at which the file was uploaded.
     * @param name The name of the file.
     * @param filetype The filetype.
     * @param content The content.
     */
    public File(final User user, final Experiment experiment, final LocalDateTime date, final String name,
                final String filetype, final byte[] content) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.name = name;
        this.filetype = filetype;
        this.content = content;
    }

}
