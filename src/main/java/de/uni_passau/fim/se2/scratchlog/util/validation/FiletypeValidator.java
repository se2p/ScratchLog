/*
 * Copyright (C) 2023 ScratchLog contributors
 *
 * This file is part of ScratchLog.
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ScratchLog. If not, see <http://www.gnu.org/licenses/>.
 */

package de.uni_passau.fim.se2.scratchlog.util.validation;

import de.uni_passau.fim.se2.scratchlog.util.Constants;
import org.springframework.web.multipart.MultipartFile;

/**
 * Validator for uploaded files.
 */
public final class FiletypeValidator {

    /**
     * Checks, whether the given file is a valid given the desired content type and ending. If the file is empty or does
     * not have the desired content type or file ending, a string corresponding to a key for retrieving an error message
     * is returned.
     *
     * @param file The file to be checked.
     * @param contentType The desired filetype.
     * @param fileEnding The desired ending of the file, e.g. '.csv'.
     * @return The key of the error message to be retrieved and displayed to the user.
     */
    public static String validate(final MultipartFile file, final String contentType, final String fileEnding) {
        if (file.isEmpty()) {
            return "file_empty";
        } else if (file.getContentType() == null || !file.getContentType().equals(contentType)) {
            return "file_type";
        } else if (file.getOriginalFilename() == null || !file.getOriginalFilename().endsWith(fileEnding)) {
            return Constants.SB3.equals(fileEnding) ? "file_name" : "csv_file_name";
        }

        return null;
    }

}
