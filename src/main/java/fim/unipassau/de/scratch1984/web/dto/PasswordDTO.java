package fim.unipassau.de.scratch1984.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A DTO representing a user password.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
public class PasswordDTO {

    /**
     * The user's password.
     */
    private String password;

    /**
     * Constructs a new password dto with the given attributes.
     *
     * @param password The password.
     */
    public PasswordDTO(final String password) {
        this.password = password;
    }

}
