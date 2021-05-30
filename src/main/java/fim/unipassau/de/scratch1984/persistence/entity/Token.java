package fim.unipassau.de.scratch1984.persistence.entity;

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
     * Default constructor for the token entity.
     */
    public Token() {
    }

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

    /**
     * Returns the value of the token.
     *
     * @return The respective value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the token.
     *
     * @param value The value to be set.
     */
    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Returns the type of the token.
     *
     * @return The respective type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the token.
     *
     * @param type The type to be set.
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Returns the expiration date of the token.
     *
     * @return The respective date.
     */
    public Timestamp getDate() {
        return date;
    }

    /**
     * Sets the expiration timestamp of the token.
     *
     * @param date The timestamp to be set.
     */
    public void setDate(final Timestamp date) {
        this.date = date;
    }

    /**
     * Returns the metadata of the token.
     *
     * @return The respective metadata.
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata of the token.
     *
     * @param metadata The metadata to be set.
     */
    public void setMetadata(final String metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns the user of the token.
     *
     * @return The respective user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user of the token.
     *
     * @param user The user to be set.
     */
    public void setUser(final User user) {
        this.user = user;
    }

}
