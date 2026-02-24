package de.uni_passau.fim.se2.scratchlog.util.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.Map;

/**
 * A collection of general utility methods for validation purposes.
 */
public final class ValidationUtils {

    /**
     * Rejects a value in validation by building a constraint validation for the given {@code context}, as well as
     * attaching an error message.
     *
     * @param context The {@link ConstraintValidatorContext} of the current validation.
     * @param messageKey The resource bundle key that contains the error message.
     * @return {@code false}, always.
     */
    public static boolean reject(ConstraintValidatorContext context, String messageKey) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("{" + messageKey + "}").addConstraintViolation();
        return false;
    }

    /**
     * Same as {@link #reject(ConstraintValidatorContext, String)}, but allows replacing message template parameters
     * via the given map.
     *
     * @param context The {@link ConstraintValidatorContext} of the current validation.
     * @param messageKey The resource bundle key that contains the error message.
     * @param messageParameters A string map in which the keys represent the resource bundle message placeholders,
     *                          and values represent the corresponding text to insert.
     * @return {@code false}, always.
     */
    public static boolean reject(ConstraintValidatorContext context, String messageKey,
                           Map<String, String> messageParameters) {
        HibernateConstraintValidatorContext hibernateContext
            = context.unwrap(HibernateConstraintValidatorContext.class);
        for (Map.Entry<String, String> entry : messageParameters.entrySet()) {
            hibernateContext.addMessageParameter(entry.getKey(), entry.getValue());
        }
        return reject(hibernateContext, messageKey);
    }
}
