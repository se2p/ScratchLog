package fim.unipassau.de.scratch1984.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;

/**
 * An entity representing a token.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Token {

    /**
     * The unique value of the token.
     */
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "value")
    private String value;

    /**
     * A string representing the {@link fim.unipassau.de.scratch1984.web.dto.TokenDTO.Type}.
     */
    @Column(name = "type")
    private String type;

    /**
     * The expiration date of the token.
     */
    @Column(name = "expiration")
    private Timestamp date;

    /**
     * Any additional information related to this token.
     */
    @Column(name = "metadata")
    private String metadata;

    /**
     * The {@link User} to whom this token belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Constructs a new token with the given attributes.
     *
     * @param type The type of the token.
     * @param date The expiration timestamp of the token.
     * @param metadata Optional metadata for the token.
     * @param user The user to whom this token belongs.
     */
    public Token(final String type, final Timestamp date, final String metadata, final User user) {
        this.type = type;
        this.date = date;
        this.metadata = metadata;
        this.user = user;
    }

}
