package fim.unipassau.de.scratch1984.persistence.entity;

import fim.unipassau.de.scratch1984.util.enums.TokenType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

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
     * The type of the token.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TokenType type;

    /**
     * The expiration date of the token.
     */
    @Column(name = "expiration")
    private LocalDateTime date;

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
     * @param date The expiration datetime of the token.
     * @param metadata Optional metadata for the token.
     * @param user The user to whom this token belongs.
     */
    public Token(final TokenType type, final LocalDateTime date, final String metadata, final User user) {
        this.type = type;
        this.date = date;
        this.metadata = metadata;
        this.user = user;
    }

}
