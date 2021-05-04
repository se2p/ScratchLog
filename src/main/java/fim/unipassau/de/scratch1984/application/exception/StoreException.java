package fim.unipassau.de.scratch1984.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception indicating that data could not be stored.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StoreException extends RuntimeException {

    /**
     * Constructs a {@link StoreException} with no detail message.
     */
    public StoreException() {
        super();
    }

    /**
     * Constructs a {@link StoreException} with the specified detail message and cause.
     *
     * Note that the detail message associated with {@code cause} is not automatically incorporated in this
     * exception's detail message.
     *
     * @param message The detail message describing this particular exception.
     * @param cause   The cause for this particular exception.
     */
    public StoreException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@link StoreException} with the specified detail message.
     *
     * @param message The detail message describing this particular exception.
     */
    public StoreException(final String message) {
        super(message);
    }

    /**
     * Constructs a {@link StoreException} with the specified cause and the detail message of {@code cause}.
     * This constructor is useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public StoreException(final Throwable cause) {
        super(cause);
    }

}
