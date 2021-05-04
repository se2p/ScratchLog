package fim.unipassau.de.scratch1984.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception indicating that a resource could not be found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    /**
     * Constructs a {@link NotFoundException} with no detail message.
     */
    public NotFoundException() {
        super();
    }

    /**
     * Constructs a {@link NotFoundException} with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated in this
     * exception's detail message.
     *
     * @param message The detail message describing this particular exception.
     * @param cause   The cause for this particular exception.
     */
    public NotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@link NotFoundException} with the specified detail message.
     *
     * @param message The detail message describing this particular exception.
     */
    public NotFoundException(final String message) {
        super(message);
    }

    /**
     * Constructs a {@link NotFoundException} with the specified cause and the detail message of {@code cause}.
     * This constructor is useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public NotFoundException(final Throwable cause) {
        super(cause);
    }

}
