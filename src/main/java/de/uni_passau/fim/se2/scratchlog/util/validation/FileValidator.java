/*
 * This file is part of ScratchLog.
 * Licenced under the GPL v3.0 or later.
 *
 * SPDX-FileCopyrightText: 2021-2026 Scratchlog contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.uni_passau.fim.se2.scratchlog.util.validation;

import de.uni_passau.fim.se2.scratchlog.util.validation.annotation.ValidFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Map;

/**
 * A Jakarta constraint validator for {@link MultipartFile}s.
 *
 * @see ValidFile
 */
public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    private String[] allowedContentTypes;

    private String[] allowedFileEndings;

    /**
     * Initializes the validator by reading the annotation parameters.
     *
     * @param annotation The applied annotation.
     */
    @Override
    public void initialize(final ValidFile annotation) {
        ConstraintValidator.super.initialize(annotation);
        allowedContentTypes = annotation.contentTypes();
        allowedFileEndings = annotation.fileEndings();
    }

    /**
     * Validates if the given file is valid. A valid file is non-empty, has one of the specified content types,
     * and has a filename ending in one of the allowed endings.
     *
     * @param file The {@link MultipartFile} to validate.
     * @param context The current {@link ConstraintValidatorContext} of the validator.
     * @return Whether the given file is valid.
     */
    @Override
    public boolean isValid(final MultipartFile file, final ConstraintValidatorContext context) {
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
        if (filename == null
            || Arrays.stream(allowedFileEndings).noneMatch(ending -> filename.endsWith("." + ending))) {
            return ValidationUtils.reject(
                context, "error_file_ending", Map.of("expected", Arrays.toString(allowedFileEndings)));
        }

        return true;
    }

}
