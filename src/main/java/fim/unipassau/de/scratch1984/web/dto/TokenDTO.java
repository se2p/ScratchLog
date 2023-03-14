package fim.unipassau.de.scratch1984.web.dto;

import fim.unipassau.de.scratch1984.util.enums.TokenType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A DTO representing a token.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TokenDTO {

    /**
     * The unique value of the token.
     */
    private String value;

    /**
     * The type of the token.
     */
    private TokenType type;

    /**
     * The expiration date of the token.
     */
    private LocalDateTime expirationDate;

    /**
     * The optional metadata for this token.
     */
    private String metadata;

    /**
     * The id of the user to whom this token belongs.
     */
    private Integer user;

    /**
     * Constructs a new token dto with the given attributes.
     *
     * @param type The type of the token.
     * @param expirationDate The expiration date of the token.
     * @param metadata Optional metadata for the token.
     * @param user The id of the user for whom this token is to be created.
     */
    public TokenDTO(final TokenType type, final LocalDateTime expirationDate, final String metadata,
                    final Integer user) {
        this.type = type;
        this.expirationDate = expirationDate;
        this.metadata = metadata;
        this.user = user;
    }

    /**
     * Indicates whether some {@code other} token DTO is semantically equal to this token DTO.
     *
     * @param other The object to compare this token DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent token DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        TokenDTO tokenDTO = (TokenDTO) other;
        return value.equals(tokenDTO.value);
    }

    /**
     * Calculates a hash code for this token DTO for hashing purposes, and to fulfill the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of the token DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

}
