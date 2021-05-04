package fim.unipassau.de.scratch1984.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception indicating that some received data is incomplete.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IncompleteDataException extends RuntimeException {

    /**
     * Constructs a {@link IncompleteDataException} with no detail message.
     */
    public IncompleteDataException() {
        super();
    }

    /**
     * Constructs a {@link IncompleteDataException} with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated in this
     * exception's detail message.
     *
     * @param message The detail message describing this particular exception.
     * @param cause   The cause for this particular exception.
     */
    public IncompleteDataException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@link IncompleteDataException} with the specified detail message.
     *
     * @param message The detail message describing this particular exception.
     */
    public IncompleteDataException(final String message) {
        super(message);
    }

    /**
     * Constructs a {@link IncompleteDataException} with the specified cause and the detail message of {@code cause}.
     * This constructor is useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public IncompleteDataException(final Throwable cause) {
        super(cause);
    }

}
