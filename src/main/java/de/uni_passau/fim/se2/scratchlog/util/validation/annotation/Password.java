package de.uni_passau.fim.se2.scratchlog.util.validation.annotation;

import de.uni_passau.fim.se2.scratchlog.util.Constants;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Jakarta Validation constraint annotation for passwords. Requirements are given by the other validation annotations.
 */
@Constraint(validatedBy = {})
@Size(min = Constants.PASSWORD_MIN, max = Constants.SMALL_FIELD)
@Pattern(
    regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])([!?+\\-*.:,;@#$%_äöüÄÖÜßẞ]*).*",
    message = "{error_password_pattern}"
)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Password {

    /**
     * The error message in case validation fails and the validator specifies no other error message.
     * Defaults to the `error_password` resource bundle message.
     *
     * @return The error message in case validation fails.
     */
    String message() default "{error_password}";

    /**
     * The validation groups parameter required by Jakarta Validation.
     *
     * @return The validation groups.
     */
    Class<?>[] groups() default {};

    /**
     * The payload parameter required by Jakarta Validation.
     *
     * @return The payload metadata.
     */
    Class<? extends Payload>[] payload() default {};

}
