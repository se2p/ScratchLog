package fim.unipassau.de.scratch1984.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * An entity representing an sb3 zip file.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "sb3_zip")
public class Sb3Zip {

    /**
     * The unique ID of the zip file.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The {@link User} for whom the zip file was created.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The {@link Experiment} in which the zip file was created.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_id")
    private Experiment experiment;

    /**
     * The datetime at which the zip file was created by the Scratch VM.
     */
    @Column(name = "date")
    private LocalDateTime date;

    /**
     * The name of the zip file.
     */
    @Column(name = "name")
    private String name;

    /**
     * The zip file content.
     */
    @Column(name = "content")
    private byte[] content;

    /**
     * Constructs a new zip file with the given attributes.
     *
     * @param user The user for whom the zip file was created.
     * @param experiment The experiment during which the zip file was created.
     * @param date The datetime at which the file was created.
     * @param name The name of the zip.
     * @param content The content.
     */
    public Sb3Zip(final User user, final Experiment experiment, final LocalDateTime date, final String name,
                  final byte[] content) {
        this.user = user;
        this.experiment = experiment;
        this.date = date;
        this.name = name;
        this.content = content;
    }

}
