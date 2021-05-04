package fim.unipassau.de.scratch1984.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception indicating that a property supposed to be unique already exists.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyExistsException extends RuntimeException {

    /**
     * Constructs a {@link AlreadyExistsException} with no detail message.
     */
    public AlreadyExistsException() {
        super();
    }

    /**
     * Constructs a {@link AlreadyExistsException} with the specified detail message and cause.
     *
     * Note that the detail message associated with {@code cause} is not automatically incorporated in this
     * exception's detail message.
     *
     * @param message The detail message describing this particular exception.
     * @param cause   The cause for this particular exception.
     */
    public AlreadyExistsException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@link AlreadyExistsException} with the specified detail message.
     *
     * @param message The detail message describing this particular exception.
     */
    public AlreadyExistsException(final String message) {
        super(message);
    }

    /**
     * Constructs a {@link AlreadyExistsException} with the specified cause and the detail message of {@code cause}.
     * This constructor is useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public AlreadyExistsException(final Throwable cause) {
        super(cause);
    }

}
