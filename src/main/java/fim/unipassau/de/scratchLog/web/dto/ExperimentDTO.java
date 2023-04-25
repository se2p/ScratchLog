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

package fim.unipassau.de.scratchLog.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * A DTO representing an experiment.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentDTO {

    /**
     * The unique ID of the experiment.
     */
    private Integer id;

    /**
     * The ID of the course to which this experiment is to be added.
     */
    private Integer course;

    /**
     * The unique title of the experiment.
     */
    private String title;

    /**
     * The short description text of the experiment.
     */
    private String description;

    /**
     * The information text of the experiment.
     */
    private String info;

    /**
     * The text shown after the user has finished the experiment.
     */
    private String postscript;

    /**
     * Boolean indicating whether the experiment is running or not.
     */
    private boolean active;

    /**
     * Boolean indicating whether the experiment is part of a course.
     */
    private boolean courseExperiment;

    /**
     * The URL of the instrumented Scratch-GUI instance to be used for this experiment.
     */
    private String guiURL;

    /**
     * Constructs a new experiment dto with the given attributes.
     *
     * @param id The experiment id.
     * @param title The experiment title.
     * @param description The experiment description.
     * @param info The experiment information text.
     * @param postscript The postscript text.
     * @param active Whether the experiment is currently running or not.
     * @param courseExperiment Whether the experiment is part of a course or not.
     * @param guiURL The URL of the instrumented Scratch-GUI this experiment uses.
     */
    public ExperimentDTO(final Integer id, final String title, final String description, final String info,
                         final String postscript, final boolean active, final boolean courseExperiment,
                         final String guiURL) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.info = info;
        this.postscript = postscript;
        this.active = active;
        this.courseExperiment = courseExperiment;
        this.guiURL = guiURL;
    }

    /**
     * Indicates whether some {@code other} experiment DTO is semantically equal to this experiment DTO.
     *
     * @param other The object to compare this experiment DTO to.
     * @return {@code true} iff {@code other} is a semantically equivalent experiment DTO.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ExperimentDTO that = (ExperimentDTO) other;
        return Objects.equals(id, that.id);
    }

    /**
     * Calculates a hash code for this experiment DTO for hashing purposes, and to fulfill the
     * {@link Object#equals(Object)} contract.
     *
     * @return The hash code value of the experiment DTO.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
