package de.uni_passau.fim.se2.scratchlog.util.validation;

import de.uni_passau.fim.se2.scratchlog.util.validation.annotation.ValidFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Map;

public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    private String[] allowedContentTypes;
    private String[] allowedFileEndings;

    @Override
    public void initialize(ValidFile annotation) {
        ConstraintValidator.super.initialize(annotation);
        allowedContentTypes = annotation.contentTypes();
        allowedFileEndings = annotation.fileEndings();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // Allow null files to allow for optional arguments. Non-nullable fields should be covered by a separate
        // @NotNull annotation.
        if (file == null) {
            return true;
        }

        if (file.isEmpty()) {
            return ValidationUtils.reject(context, "file_empty");
        }

        if (!Arrays.asList(allowedContentTypes).contains(file.getContentType())) {
            return ValidationUtils.reject(
                context, "error_file_type", Map.of("expected", Arrays.toString(allowedContentTypes)));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || Arrays.stream(allowedFileEndings).noneMatch(filename::endsWith)) {
            return ValidationUtils.reject(
                context, "error_file_ending", Map.of("expected", Arrays.toString(allowedFileEndings)));
        }

        return true;
    }

}
