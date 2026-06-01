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

package de.uni_passau.fim.se2.scratchlog.integration;

import de.uni_passau.fim.se2.scratchlog.AbstractScratchLogControllerTest;
import de.uni_passau.fim.se2.scratchlog.application.service.ExperimentService;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.ExperimentProjection;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class ExperimentControllerIntegrationTest2 extends AbstractScratchLogControllerTest {

    private static final String VIEW_EXPERIMENT = "experiment";
    private static final String REDIRECT_EXPERIMENT = "redirect:/experiment?id=";

    private static final String PARAM_ID = "id";
    private static final String ATTR_FILE_DTO = "fileDTO";
    private static final String ATTR_FILE = "file";

    private static final String INVALID_ID = "-1";
    private static final String SB3_FILENAME = "Scratch-Projekt.sb3";
    private static final String SB3_FILETYPE = "application/octet-stream";

    @Autowired
    private ExperimentService experimentService;

    private Experiment experiment;
    private String experimentIdString;
    private MockMultipartFile sb3File;

    @BeforeEach
    public void setup() throws IOException {
        experiment = entityUtilService.generateExperiment("Test Experiment");
        experimentIdString = experiment.getId().toString();
        sb3File = new MockMultipartFile(ATTR_FILE, SB3_FILENAME, SB3_FILETYPE,
            new ClassPathResource(SB3_FILENAME).getInputStream());
    }

    @Test
    public void testUploadProjectFile() throws Exception {
        mvc.perform(multipart("/experiment/project")
                .file(sb3File)
                .param(PARAM_ID, experimentIdString))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name(REDIRECT_EXPERIMENT + experimentIdString));

        ExperimentProjection project = experimentService.getSb3File(experiment.getId(), true);
        assertArrayEquals(sb3File.getBytes(), project.getProject());
    }

    @Test
    public void testUploadProjectFileExperimentNotFound() throws Exception {
        mvc.perform(multipart("/experiment/project")
                .file(sb3File)
                .param(PARAM_ID, INVALID_ID))
            .andExpect(status().isNotFound());
    }

    @Test
    public void testUploadProjectInvalidFileName() throws Exception {
        sb3File = new MockMultipartFile(sb3File.getName(), "invalid", sb3File.getContentType(), sb3File.getBytes());
        mvc.perform(multipart("/experiment/project")
                .file(sb3File)
                .param(PARAM_ID, experimentIdString))
            .andExpect(status().isOk())
            .andExpect(view().name(VIEW_EXPERIMENT))
            .andExpect(model().attributeHasFieldErrors(ATTR_FILE_DTO, ATTR_FILE));
    }

    @Test
    public void testUploadProjectInvalidFileType() throws Exception {
        sb3File = new MockMultipartFile(sb3File.getName(),sb3File.getOriginalFilename(), "image/png",
            sb3File.getBytes());
        mvc.perform(multipart("/experiment/project")
                .file(sb3File)
                .param(PARAM_ID, experimentIdString))
            .andExpect(status().isOk())
            .andExpect(view().name(VIEW_EXPERIMENT))
            .andExpect(model().attributeHasFieldErrors(ATTR_FILE_DTO, ATTR_FILE));
    }

    @Test
    public void testUploadProjectFileEmpty() throws Exception {
        sb3File = new MockMultipartFile(sb3File.getName(),sb3File.getOriginalFilename(), sb3File.getContentType(),
            new byte[] {});
        mvc.perform(multipart("/experiment/project")
                .file(sb3File)
                .param(PARAM_ID, experimentIdString))
            .andExpect(status().isOk())
            .andExpect(view().name(VIEW_EXPERIMENT))
            .andExpect(model().attributeHasFieldErrors(ATTR_FILE_DTO, ATTR_FILE));
    }

    @Test
    public void testUploadProjectIOException() throws Exception {
        mvc.perform(multipart("/experiment/project")
                .file(new ThrowingMockMultiPartFile(sb3File))
                .param(PARAM_ID, experimentIdString))
            .andExpect(status().isOk())
            .andExpect(view().name(VIEW_EXPERIMENT))
            .andExpect(model().attributeHasFieldErrorCode(ATTR_FILE_DTO, ATTR_FILE, "error_io"));
    }

    @Test
    public void testDeleteProject() throws Exception {
        mvc.perform(multipart("/experiment/project").file(sb3File).param(PARAM_ID, experimentIdString));
        mvc.perform(get("/experiment/project/delete")
                .param(PARAM_ID, experimentIdString))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name(REDIRECT_EXPERIMENT + experimentIdString));
        assertFalse(experimentService.hasProjectFile(experiment.getId()));
    }

    @Test
    public void testDeleteProjectInvalidId() throws Exception {
        mvc.perform(get("/experiment/project/delete")
                .param(PARAM_ID, INVALID_ID))
            .andExpect(status().isNotFound());
    }

    /**
     * Needed for mocking the IOException path for uploading projects, since we cannot mock the service itself
     * and {@link MockMultipartFile#getBytes()} does not declare throwing IOExceptions (hence a Mockito mock cannot make
     * it throw those exceptions).
     */
    private static class ThrowingMockMultiPartFile extends MockMultipartFile {
        public ThrowingMockMultiPartFile(MockMultipartFile original) throws IOException {
            super(original.getName(), original.getOriginalFilename(), original.getContentType(), original.getBytes());
        }

        @Override
        public byte @NonNull [] getBytes() throws IOException {
            throw new IOException();
        }
    }
}
