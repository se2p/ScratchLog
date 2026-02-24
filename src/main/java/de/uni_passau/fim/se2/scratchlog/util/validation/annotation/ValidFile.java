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
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileValidator.class)
@Documented
public @interface ValidFile {

    // Required annotation parameters for Jakarta Validation.
    String message() default "{file_empty}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    /**
     * The content types that should be allowed for this file.
     */
    String[] contentTypes() default {};

    /**
     * The file endings that should be allowed for this file.
     */
    String[] fileEndings() default {};

}
