package de.uni_passau.fim.se2.scratchlog.util.validation.annotation;

import de.uni_passau.fim.se2.scratchlog.util.validation.FileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Jakarta Validation annotation for files.
 *
 * @see FileValidator
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileValidator.class)
@Documented
public @interface ValidFile {

    /**
     * The error message in case validation fails and the validator specifies no other error message.
     * Defaults to the `file_empty` resource bundle message.
     *
     * @return The error message in case validation fails.
     */
    String message() default "{file_empty}";

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

    /**
     * The content types that should be allowed for this file.
     *
     * @return The allowed content types.
     */
    String[] contentTypes() default {};

    /**
     * The file endings that should be allowed for this file.
     *
     * @return The allowed file endings.
     */
    String[] fileEndings() default {};

}
