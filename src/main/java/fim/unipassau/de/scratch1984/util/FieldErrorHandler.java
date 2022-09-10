package fim.unipassau.de.scratch1984.util;

import fim.unipassau.de.scratch1984.util.validation.EmailValidator;
import fim.unipassau.de.scratch1984.util.validation.StringValidator;
import fim.unipassau.de.scratch1984.util.validation.UsernameValidator;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ResourceBundle;

/**
 * Utility class for creating {@link org.springframework.validation.FieldError}s and verifying form inputs.
 */
public final class FieldErrorHandler {

    /**
     * Creates a new field error with the given parameters.
     *
     * @param bindingResult The {@link BindingResult} for returning information on invalid user input.
     * @param objectName The name of the object.
     * @param field The field to which the error applies.
     * @param error The error message string.
     * @param resourceBundle The resource bundle to retrieve the error message in the current language.
     */
    public static void addFieldError(final BindingResult bindingResult, final String objectName, final String field,
                               final String error, final ResourceBundle resourceBundle) {
        bindingResult.addError(new FieldError(objectName, field, resourceBundle.getString(error)));
    }

    /**
     * Creates a new field error with the title exists error message.
     *
     * @param bindingResult The {@link BindingResult} for returning information on invalid user input.
     * @param objectName The name of the object.
     * @param resourceBundle The {@link ResourceBundle} for error fetching error messages in the correct language.
     */
    public static void addTitleExistsError(final BindingResult bindingResult, final String objectName,
                                           final ResourceBundle resourceBundle) {
        bindingResult.addError(new FieldError(objectName, "title", resourceBundle.getString("title_exists")));
    }

    /**
     * Validates that the passed email String is a valid email according to the {@link EmailValidator}. If the passed
     * email is invalid, a corresponding error message is appended to the {@link BindingResult}.
     *
     * @param email The email string to validate.
     * @param bindingResult The {@link BindingResult} for returning information on invalid user input.
     * @param resourceBundle The {@link ResourceBundle} for error fetching error messages in the correct language.
     * @return The validation string as returned by the validator.
     */
    public static String validateEmail(final String email, final BindingResult bindingResult,
                                       final ResourceBundle resourceBundle) {
        String emailValidation = EmailValidator.validate(email);

        if (emailValidation != null) {
            bindingResult.addError(new FieldError("userDTO", "email", resourceBundle.getString(emailValidation)));
        }

        return emailValidation;
    }

    /**
     * Validates that the passed username is a valid one according to the {@link UsernameValidator}. If the passed
     * username is invalid, a corresponding error message is appended to the {@link BindingResult}.
     *
     * @param username The username to validate.
     * @param bindingResult The {@link BindingResult} for returning information on invalid user input.
     * @param resourceBundle The {@link ResourceBundle} for error fetching error messages in the correct language.
     * @return The validation string as returned by the validator.
     */
    public static String validateUsername(final String username, final BindingResult bindingResult,
                                          final ResourceBundle resourceBundle) {
        String usernameValidation = UsernameValidator.validate(username);

        if (usernameValidation != null) {
            bindingResult.addError(new FieldError("userDTO", "username", resourceBundle.getString(usernameValidation)));
        }

        return usernameValidation;
    }

    /**
     * Validates that the title, description and info strings passed for an experiment are valid.
     *
     * @param title The title to validate.
     * @param description The description to validate.
     * @param info The information text to validate.
     * @param bindingResult The {@link BindingResult} for returning information on invalid user input.
     * @param resourceBundle The {@link ResourceBundle} for error fetching error messages in the correct language.
     */
    public static void validateExperimentInput(final String title, final String description, final String info,
                                               final BindingResult bindingResult, final ResourceBundle resourceBundle) {
        validateTitleAndDescription(title, description, "experimentDTO", bindingResult, resourceBundle);

        if (info.length() > Constants.LARGE_AREA) {
            bindingResult.addError(new FieldError("experimentDTO", "info", resourceBundle.getString("long_string")));
        }
    }

    /**
     * Validates that the title, description and content strings passed for a course are valid.
     *
     * @param title The title to validate.
     * @param description The description to validate.
     * @param content The content text to validate.
     * @param bindingResult The {@link BindingResult} for returning information on invalid user input.
     * @param resourceBundle The {@link ResourceBundle} for error fetching error messages in the correct language.
     */
    public static void validateCourseInput(final String title, final String description, final String content,
                                           final BindingResult bindingResult, final ResourceBundle resourceBundle) {
        validateTitleAndDescription(title, description, "courseDTO", bindingResult, resourceBundle);

        if (content.length() > Constants.LARGE_AREA) {
            bindingResult.addError(new FieldError("courseDTO", "content", resourceBundle.getString("long_string")));
        }
    }

    /**
     * Validates that the given title and description of an experiment or a course are valid.
     *
     * @param title The title to validate.
     * @param description The description to validate.
     * @param objectName The name of the object.
     * @param bindingResult The {@link BindingResult} for returning information on invalid user input.
     * @param resourceBundle The {@link ResourceBundle} for error fetching error messages in the correct language.
     */
    private static void validateTitleAndDescription(final String title, final String description,
                                                    final String objectName, final BindingResult bindingResult,
                                                    final ResourceBundle resourceBundle) {
        String titleValidation = StringValidator.validate(title, Constants.LARGE_FIELD);
        String descriptionValidation = StringValidator.validate(description, Constants.SMALL_AREA);

        if (titleValidation != null) {
            bindingResult.addError(new FieldError(objectName, "title", resourceBundle.getString(titleValidation)));
        }
        if (descriptionValidation != null) {
            bindingResult.addError(new FieldError(objectName, "description",
                    resourceBundle.getString(descriptionValidation)));
        }
    }

}
