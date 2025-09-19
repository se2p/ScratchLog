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

package de.uni_passau.fim.se2.scratchlog.integration;

import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.service.CodeService;
import de.uni_passau.fim.se2.scratchlog.application.service.EventService;
import de.uni_passau.fim.se2.scratchlog.application.service.ExperimentDataService;
import de.uni_passau.fim.se2.scratchlog.application.service.ExperimentService;
import de.uni_passau.fim.se2.scratchlog.application.service.FileService;
import de.uni_passau.fim.se2.scratchlog.application.service.ParticipantService;
import de.uni_passau.fim.se2.scratchlog.application.service.UserService;
import de.uni_passau.fim.se2.scratchlog.application.service.ZipExportService;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.BlockEventJSONProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.BlockEventProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.BlockEventXMLProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.ExperimentProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.FileProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.spring.configuration.SecurityTestConfig;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.web.AbstractControllerTest;
import de.uni_passau.fim.se2.scratchlog.web.controller.ResultController;
import de.uni_passau.fim.se2.scratchlog.web.dto.CodesDataDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.EventCountDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.FileDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.ParticipantDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.Sb3ZipDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ResultController.class)
@Import(SecurityTestConfig.class)
public class ResultControllerIntegrationTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ExperimentService experimentService;

    @MockitoBean
    private ExperimentDataService experimentDataService;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private CodeService codeService;

    @MockitoBean
    private FileService fileService;

    @MockitoBean
    private ParticipantService participantService;

    @SpyBean
    private ZipExportService zipExportService;

    @MockitoBean
    private UserRepository userRepository;

    private static final String RESULT = "result";
    private static final String ERROR = "redirect:/error";
    private static final String ID_STRING = "1";
    private static final String EXPERIMENT_PARAM = "experiment";
    private static final String USER_PARAM = "user";
    private static final String ID_PARAM = "id";
    private static final String PAGE_PARAM = "page";
    private static final String STEP_PARAM = "step";
    private static final String START_PARAM = "start";
    private static final String END_PARAM = "end";
    private static final String INCLUDE_PARAM = "include";
    private static final String PAGE = "0";
    private static final String JSON = "json";
    private static final int ID = 1;
    private final FileDTO fileDTO = new FileDTO(ID, ID, "secret", "file", "type", new byte[]{1, 2, 3},
        LocalDateTime.now());
    private final FileDTO zip = new FileDTO(ID, ID, "secret", "file.zip", "wav", new byte[]{1, 2, 3, 4},
        LocalDateTime.now());
    private final ParticipantDTO participantDTO1 = new ParticipantDTO(ID, ID);
    private final ParticipantDTO participantDTO2 = new ParticipantDTO(2, ID);
    private final UserDTO userDTOWithUsername1 = new UserDTO("user1", null, null, null, null, null);
    private final UserDTO userDTOWithUsername2 = new UserDTO("user2", null, null, null, null, null);
    private final Sb3ZipDTO sb3ZipDTO = new Sb3ZipDTO(ID, ID, "secret", "file", new byte[]{1, 2, 3},
        LocalDateTime.now());
    private final CodesDataDTO codesDataDTO = new CodesDataDTO(ID, ID, 9);
    private final List<EventCountDTO> blockEvents = getEventCounts(5, "CREATE");
    private final List<EventCountDTO> clickEvents = getEventCounts(3, "GREENFLAG");
    private final List<EventCountDTO> resourceEvents = getEventCounts(2, "RENAME");
    private final List<FileProjection> files = getFileProjections(7);
    private final List<Integer> zips = Arrays.asList(1, 4, 10, 18);
    private final List<Sb3ZipDTO> sb3ZipDTOs = getSb3ZipDTOs(6);
    private final List<BlockEventXMLProjection> xmlProjections = new ArrayList<>();
    private final List<BlockEventJSONProjection> jsonProjections = getJsonProjections(3);
    private final Page<BlockEventProjection> blockEventProjections = new PageImpl<>(getBlockEventProjections(2));
    private final List<ParticipantDTO> participants = List.of(participantDTO1, participantDTO2);
    private final List<Integer> bugPatterns = List.of(0, 0, 1);
    private final List<Integer> smells = List.of(0, 0, 0);
    private final List<Integer> perfumes = List.of(0, 1, 1);
    private final List<List<Integer>> analysisResults = List.of(bugPatterns, smells, perfumes);
    ExperimentProjection experimentProjection = new ExperimentProjection() {
        @Override
        public Integer getId() {
            return ID;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public byte[] getProject() {
            return null;
        }
    };

    @Test
    public void testGetResult() throws Exception {
        when(userService.existsParticipant(ID, ID)).thenReturn(true);
        when(eventService.getBlockEventCounts(ID, ID)).thenReturn(blockEvents);
        when(eventService.getClickEventCounts(ID, ID)).thenReturn(clickEvents);
        when(eventService.getResourceEventCounts(ID, ID)).thenReturn(resourceEvents);
        when(fileService.getFiles(ID, ID)).thenReturn(files);
        when(fileService.getZipIds(ID, ID)).thenReturn(zips);
        when(codeService.getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any())).thenReturn(
                jsonProjections);
        when(experimentDataService.getAnalyzedProgramDataCount(jsonProjections)).thenReturn(analysisResults);
        when(eventService.getCodesData(ID, ID)).thenReturn(codesDataDTO);
        mvc.perform(get("/result")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute("blockEvents", is(blockEvents)))
                .andExpect(model().attribute("clickEvents", is(clickEvents)))
                .andExpect(model().attribute("resourceEvents", is(resourceEvents)))
                .andExpect(model().attribute("files", is(files)))
                .andExpect(model().attribute("zips", is(zips)))
                .andExpect(model().attribute("user", is(ID)))
                .andExpect(model().attribute("experiment", is(ID)))
                .andExpect(model().attribute("bugs", is(bugPatterns)))
                .andExpect(model().attribute("smells", is(smells)))
                .andExpect(model().attribute("perfumes", is(perfumes)))
                .andExpect(model().attribute("codeCount", is(codesDataDTO.getCount())))
                .andExpect(status().isOk())
                .andExpect(view().name(RESULT));
        verify(userService).existsParticipant(ID, ID);
        verify(eventService).getBlockEventCounts(ID, ID);
        verify(eventService).getClickEventCounts(ID, ID);
        verify(eventService).getResourceEventCounts(ID, ID);
        verify(fileService).getFiles(ID, ID);
        verify(fileService).getZipIds(ID, ID);
        verify(codeService).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(experimentDataService).getAnalyzedProgramDataCount(jsonProjections);
        verify(eventService).getCodesData(ID, ID);
    }

    @Test
    public void testGetResultCodesDataZero() throws Exception {
        when(userService.existsParticipant(ID, ID)).thenReturn(true);
        when(eventService.getBlockEventCounts(ID, ID)).thenReturn(blockEvents);
        when(eventService.getClickEventCounts(ID, ID)).thenReturn(clickEvents);
        when(eventService.getResourceEventCounts(ID, ID)).thenReturn(resourceEvents);
        when(fileService.getFiles(ID, ID)).thenReturn(files);
        when(fileService.getZipIds(ID, ID)).thenReturn(zips);
        when(codeService.getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any())).thenReturn(
                jsonProjections);
        when(experimentDataService.getAnalyzedProgramDataCount(jsonProjections)).thenReturn(analysisResults);
        when(eventService.getCodesData(ID, ID)).thenReturn(new CodesDataDTO());
        mvc.perform(get("/result")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute("blockEvents", is(blockEvents)))
                .andExpect(model().attribute("clickEvents", is(clickEvents)))
                .andExpect(model().attribute("resourceEvents", is(resourceEvents)))
                .andExpect(model().attribute("files", is(files)))
                .andExpect(model().attribute("zips", is(zips)))
                .andExpect(model().attribute("user", is(ID)))
                .andExpect(model().attribute("experiment", is(ID)))
                .andExpect(model().attribute("bugs", is(bugPatterns)))
                .andExpect(model().attribute("smells", is(smells)))
                .andExpect(model().attribute("perfumes", is(perfumes)))
                .andExpect(model().attribute("codeCount", is(0)))
                .andExpect(status().isOk())
                .andExpect(view().name(RESULT));
        verify(userService).existsParticipant(ID, ID);
        verify(eventService).getBlockEventCounts(ID, ID);
        verify(eventService).getClickEventCounts(ID, ID);
        verify(eventService).getResourceEventCounts(ID, ID);
        verify(fileService).getFiles(ID, ID);
        verify(fileService).getZipIds(ID, ID);
        verify(codeService).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(experimentDataService).getAnalyzedProgramDataCount(jsonProjections);
        verify(eventService).getCodesData(ID, ID);
    }

    @Test
    public void testGetResultNotFound() throws Exception {
        when(userService.existsParticipant(ID, ID)).thenReturn(true);
        when(eventService.getBlockEventCounts(ID, ID)).thenReturn(blockEvents);
        when(eventService.getClickEventCounts(ID, ID)).thenReturn(clickEvents);
        when(eventService.getResourceEventCounts(ID, ID)).thenReturn(resourceEvents);
        when(fileService.getFiles(ID, ID)).thenReturn(files);
        when(fileService.getZipIds(ID, ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/result")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute("blockEvents", nullValue()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).existsParticipant(ID, ID);
        verify(eventService).getBlockEventCounts(ID, ID);
        verify(eventService).getClickEventCounts(ID, ID);
        verify(eventService).getResourceEventCounts(ID, ID);
        verify(fileService).getFiles(ID, ID);
        verify(fileService).getZipIds(ID, ID);
        verify(codeService, never()).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(experimentDataService, never()).getAnalyzedProgramDataCount(any());
        verify(eventService, never()).getCodesData(anyInt(), anyInt());
    }

    @Test
    public void testGetResultNoParticipant() throws Exception {
        mvc.perform(get("/result")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute("blockEvents", nullValue()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).existsParticipant(ID, ID);
        verify(eventService, never()).getBlockEventCounts(anyInt(), anyInt());
        verify(eventService, never()).getClickEventCounts(anyInt(), anyInt());
        verify(eventService, never()).getResourceEventCounts(anyInt(), anyInt());
        verify(fileService, never()).getFiles(anyInt(), anyInt());
        verify(fileService, never()).getZipIds(anyInt(), anyInt());
        verify(codeService, never()).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(experimentDataService, never()).getAnalyzedProgramDataCount(any());
        verify(eventService, never()).getCodesData(anyInt(), anyInt());
    }

    @Test
    public void testDownloadFile() throws Exception {
        when(fileService.findFile(ID)).thenReturn(fileDTO);
        mvc.perform(get("/result/file")
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", is("attachment; filename=\""
                        + fileDTO.getName() + "\"")))
                .andExpect(content().bytes(fileDTO.getContent()));
        verify(fileService).findFile(ID);
    }

    @Test
    public void testDownloadFileNotFound() throws Exception {
        when(fileService.findFile(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/result/file")
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(fileService).findFile(ID);
    }

    @Test
    public void testDownloadFileInvalidId() throws Exception {
        mvc.perform(get("/result/file")
                .param(ID_PARAM, "  ")
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is4xxClientError())
                .andExpect(view().name(Constants.ERROR));
        verify(fileService, never()).findFile(anyInt());
    }

    @Test
    public void testGenerateZipFile() throws Exception {
        URL zipUrl = getClass().getClassLoader().getResource("Taylor-b.zip");
        URL sb3 = getClass().getClassLoader().getResource("Scratch-Projekt.sb3");
        File sb3File = new File(sb3.getFile());
        File zipFile = new File(zipUrl.getFile());
        byte[] sb3Bytes = new byte[(int) sb3File.length()];
        byte[] zipBytes = new byte[(int) zipFile.length()];
        FileInputStream sb3InputStream = new FileInputStream(sb3File);
        FileInputStream zipInputStream = new FileInputStream(zipFile);
        sb3InputStream.read(sb3Bytes);
        sb3InputStream.close();
        zipInputStream.read(zipBytes);
        zipInputStream.close();
        zip.setContent(zipBytes);
        List<FileDTO> fileDTOS = new ArrayList<>();
        fileDTOS.add(fileDTO);
        fileDTOS.add(zip);
        ExperimentProjection projection = new ExperimentProjection() {
            @Override
            public Integer getId() {
                return ID;
            }

            @Override
            public boolean isActive() {
                return true;
            }

            @Override
            public byte[] getProject() {
                return sb3Bytes;
            }
        };
        when(experimentService.getSb3File(ID, true)).thenReturn(projection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(codeService.findJsonById(ID)).thenReturn(JSON);
        mvc.perform(get("/result/generate")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .param(JSON, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk());
        verify(experimentService).getSb3File(ID, true);
        verify(fileService).getFileDTOs(ID, ID);
        verify(codeService).findJsonById(ID);
    }

    @Test
    public void testGenerateZipFileProjectNull() throws Exception {
        URL zipUrl = getClass().getClassLoader().getResource("Taylor-b.zip");
        File zipFile = new File(zipUrl.getFile());
        byte[] zipBytes = new byte[(int) zipFile.length()];
        FileInputStream zipInputStream = new FileInputStream(zipFile);
        zipInputStream.read(zipBytes);
        zipInputStream.close();
        zip.setContent(zipBytes);
        List<FileDTO> fileDTOS = new ArrayList<>();
        fileDTOS.add(fileDTO);
        fileDTOS.add(zip);
        when(experimentService.getSb3File(ID, true)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(codeService.findJsonById(ID)).thenReturn(JSON);
        mvc.perform(get("/result/generate")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .param(JSON, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk());
        verify(experimentService).getSb3File(ID, true);
        verify(fileService).getFileDTOs(ID, ID);
        verify(codeService).findJsonById(ID);
    }

    @Test
    public void testGenerateZipFileNotFound() throws Exception {
        when(experimentService.getSb3File(ID, true)).thenThrow(NotFoundException.class);
        mvc.perform(get("/result/generate")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .param(JSON, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isNotFound());
        verify(experimentService).getSb3File(ID, true);
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(codeService, never()).findJsonById(anyInt());
    }

    @Test
    public void testGenerateZipFileInvalidJsonId() throws Exception {
        mvc.perform(get("/result/generate")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .param(JSON, "  ")
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(experimentService, never()).getSb3File(anyInt(), anyBoolean());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(codeService, never()).findJsonById(anyInt());
    }

    @Test
    public void testDownloadZip() throws Exception {
        when(fileService.findZip(ID)).thenReturn(sb3ZipDTO);
        mvc.perform(get("/result/zip")
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", is("attachment; filename=\""
                        + sb3ZipDTO.getName() + "\"")))
                .andExpect(content().bytes(sb3ZipDTO.getContent()));
        verify(fileService).findZip(ID);
    }

    @Test
    public void testDownloadZipNotFound() throws Exception {
        when(fileService.findZip(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/result/zip")
                .param(ID_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(fileService).findZip(ID);
    }

    @Test
    public void testDownloadAllZips() throws Exception {
        when(fileService.getZipFiles(ID, ID)).thenReturn(sb3ZipDTOs);
        mvc.perform(get("/result/zips")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk());
        verify(fileService).getZipFiles(ID, ID);
    }

    @Test
    public void testDownloadAllZipsNotFound() throws Exception {
        when(fileService.getZipFiles(ID, ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/result/zips")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isNotFound());
        verify(fileService).getZipFiles(ID, ID);
    }

    @Test
    public void testDownloadAllXmlFiles() throws Exception {
        when(codeService.getXMLForUser(ID, ID)).thenReturn(xmlProjections);
        mvc.perform(get("/result/xmls")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk());
        verify(codeService).getXMLForUser(ID, ID);
    }

    @Test
    public void testDownloadAllXmlFilesNotFound() throws Exception {
        when(codeService.getXMLForUser(ID, ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/result/xmls")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isNotFound());
        verify(codeService).getXMLForUser(ID, ID);
    }

    @Test
    public void testDownloadAllJsonFilesForUser() throws Exception {
        when(codeService.getJsonForUser(ID, ID)).thenReturn(jsonProjections);
        mvc.perform(get("/result/jsons")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk());
        verify(codeService).getJsonForUser(ID, ID);
    }

    @Test
    public void testDownloadAllJsonFilesForUserNotFound() throws Exception {
        when(codeService.getJsonForUser(ID, ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/result/jsons")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isNotFound());
        verify(codeService).getJsonForUser(ID, ID);
    }

    @Test
    public void testDownloadAllJsonFilesForUserInvalidId() throws Exception {
        mvc.perform(get("/result/jsons")
                .param(EXPERIMENT_PARAM, "id")
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(codeService, never()).getJsonForUser(anyInt(), anyInt());
    }

    @Test
    public void testGetCodes() throws Exception {
        when(codeService.getCodesForUser(anyInt(), anyInt(), any(PageRequest.class))).thenReturn(blockEventProjections);
        mvc.perform(get("/result/codes")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .param(PAGE_PARAM, PAGE)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk());
        verify(codeService).getCodesForUser(anyInt(), anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetCodesInvalidPage() throws Exception {
        mvc.perform(get("/result/codes")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .param(PAGE_PARAM, "-3")
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(codeService, never()).getCodesForUser(anyInt(), anyInt(), any(PageRequest.class));
    }

    @Test
    public void testDownloadSb3Files() throws Exception {
        URL zipUrl = getClass().getClassLoader().getResource("Taylor-b.zip");
        URL sb3 = getClass().getClassLoader().getResource("Scratch-Projekt.sb3");
        File sb3File = new File(sb3.getFile());
        File zipFile = new File(zipUrl.getFile());
        byte[] sb3Bytes = new byte[(int) sb3File.length()];
        byte[] zipBytes = new byte[(int) zipFile.length()];
        FileInputStream sb3InputStream = new FileInputStream(sb3File);
        FileInputStream zipInputStream = new FileInputStream(zipFile);
        sb3InputStream.read(sb3Bytes);
        sb3InputStream.close();
        zipInputStream.read(zipBytes);
        zipInputStream.close();
        zip.setContent(zipBytes);
        List<FileDTO> fileDTOS = new ArrayList<>();
        fileDTOS.add(fileDTO);
        fileDTOS.add(zip);
        Optional<Sb3ZipDTO> project = Optional.of(sb3ZipDTO);
        ExperimentProjection projection = new ExperimentProjection() {
            @Override
            public Integer getId() {
                return ID;
            }

            @Override
            public boolean isActive() {
                return true;
            }

            @Override
            public byte[] getProject() {
                return sb3Bytes;
            }
        };
        when(experimentService.getSb3File(ID, true)).thenReturn(projection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(codeService.getFilteredJsons(ID, ID, 0, 0, 0, project)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(project);
        when(userService.getUserById(ID)).thenReturn(userDTOWithUsername1);
        mvc.perform(get("/result/sb3s")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        is("attachment;filename=zip_user1_experiment1.zip")));
        verify(experimentService).getSb3File(ID, true);
        verify(fileService).getFileDTOs(ID, ID);
        verify(codeService).getFilteredJsons(ID, ID, 0, 0, 0, project);
        verify(fileService).findFinalProject(ID, ID);
    }

    @Test
    public void testDownloadSb3FilesNoInitialAndFinalProjects() throws Exception {
        Optional<Sb3ZipDTO> noSavedProject = Optional.empty();
        when(experimentService.getSb3File(ID, true)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(new ArrayList<>());
        when(codeService.getFilteredJsons(ID, ID, 0, 0, 0, noSavedProject)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(noSavedProject);
        when(userService.getUserById(ID)).thenReturn(userDTOWithUsername1);
        mvc.perform(get("/result/sb3s")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        is("attachment;filename=zip_user1_experiment1.zip")));
        verify(experimentService).getSb3File(ID, true);
        verify(fileService).getFileDTOs(ID, ID);
        verify(codeService).getFilteredJsons(ID, ID, 0, 0, 0, noSavedProject);
        verify(fileService).findFinalProject(ID, ID);
    }

    @Test
    public void testDownloadSb3FilesStep() throws Exception {
        Optional<Sb3ZipDTO> project = Optional.of(sb3ZipDTO);
        when(experimentService.getSb3File(ID, true)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(new ArrayList<>());
        when(codeService.getFilteredJsons(ID, ID, ID, 0, 0, project)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(project);
        when(userService.getUserById(ID)).thenReturn(userDTOWithUsername1);
        mvc.perform(get("/result/sb3s")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .param(STEP_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        is("attachment;filename=zip_user1_experiment1.zip")));
        verify(experimentService).getSb3File(ID, true);
        verify(fileService).getFileDTOs(ID, ID);
        verify(codeService).getFilteredJsons(ID, ID, ID, 0, 0, project);
        verify(fileService).findFinalProject(ID, ID);
    }

    @Test
    public void testDownloadSb3FilesStartStop() throws Exception {
        Optional<Sb3ZipDTO> project = Optional.of(sb3ZipDTO);
        when(experimentService.getSb3File(ID, true)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(new ArrayList<>());
        when(codeService.getFilteredJsons(ID, ID, 0, ID, 2, project)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(project);
        when(userService.getUserById(ID)).thenReturn(userDTOWithUsername1);
        mvc.perform(get("/result/sb3s")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .param(START_PARAM, ID_STRING)
                .param(END_PARAM, "2")
                .param(INCLUDE_PARAM, "false")
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        is("attachment;filename=zip_user1_experiment1.zip")));
        verify(experimentService).getSb3File(ID, true);
        verify(fileService).getFileDTOs(ID, ID);
        verify(codeService).getFilteredJsons(ID, ID, 0, ID, 2, project);
        verify(fileService).findFinalProject(ID, ID);
    }

    @Test
    public void testDownloadSb3FilesInvalidUserId() throws Exception {
        mvc.perform(get("/result/sb3s")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, "ID_STRING")
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(experimentService, never()).getSb3File(anyInt(), anyBoolean());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(codeService, never()).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
    }

    @Test
    public void testDownloadSb3FilesInvalidStep() throws Exception {
        mvc.perform(get("/result/sb3s")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .param(STEP_PARAM, "bla")
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(experimentService, never()).getSb3File(anyInt(), anyBoolean());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(codeService, never()).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
    }

    @Test
    public void testDownloadSb3FilesInvalidStartPosition() throws Exception {
        mvc.perform(get("/result/sb3s")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .param(START_PARAM, "3")
                .param(END_PARAM, "2")
                .param(INCLUDE_PARAM, "false")
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(experimentService, never()).getSb3File(anyInt(), anyBoolean());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(codeService, never()).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
    }

    @Test
    public void testDownloadExperimentSb3Files() throws Exception {
        URL zipUrl = getClass().getClassLoader().getResource("Taylor-b.zip");
        URL sb3 = getClass().getClassLoader().getResource("Scratch-Projekt.sb3");
        File sb3File = new File(sb3.getFile());
        File zipFile = new File(zipUrl.getFile());
        byte[] sb3Bytes = new byte[(int) sb3File.length()];
        byte[] zipBytes = new byte[(int) zipFile.length()];
        FileInputStream sb3InputStream = new FileInputStream(sb3File);
        FileInputStream zipInputStream = new FileInputStream(zipFile);
        sb3InputStream.read(sb3Bytes);
        sb3InputStream.close();
        zipInputStream.read(zipBytes);
        zipInputStream.close();
        zip.setContent(zipBytes);
        List<FileDTO> fileDTOS = new ArrayList<>();
        fileDTOS.add(fileDTO);
        fileDTOS.add(zip);
        ExperimentProjection projection = new ExperimentProjection() {
            @Override
            public Integer getId() {
                return ID;
            }

            @Override
            public boolean isActive() {
                return true;
            }

            @Override
            public byte[] getProject() {
                return sb3Bytes;
            }
        };
        when(participantService.getParticipants(ID)).thenReturn(participants);
        when(experimentService.getSb3File(ID, true)).thenReturn(projection);
        when(fileService.getFileDTOs(anyInt(), anyInt())).thenReturn(fileDTOS);
        when(codeService.getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any())).thenReturn(
                jsonProjections);
        when(fileService.findFinalProject(anyInt(), anyInt())).thenReturn(Optional.of(sb3ZipDTO));
        when(userService.getUserById(participants.getFirst().getUser())).thenReturn(userDTOWithUsername1);
        when(userService.getUserById(participants.get(1).getUser())).thenReturn(userDTOWithUsername2);
        mvc.perform(get("/result/sb3s/all")
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        is("attachment;filename=experiment1_all_sb3s.zip")));
        verify(experimentService).getSb3File(ID, true);
        verify(fileService, times(2)).getFileDTOs(anyInt(), anyInt());
        verify(codeService, times(2)).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, times(2)).findFinalProject(anyInt(), anyInt());
    }

    @Test
    public void testDownloadExperimentSb3FilesStep() throws Exception {
        URL zipUrl = getClass().getClassLoader().getResource("Taylor-b.zip");
        URL sb3 = getClass().getClassLoader().getResource("Scratch-Projekt.sb3");
        File sb3File = new File(sb3.getFile());
        File zipFile = new File(zipUrl.getFile());
        byte[] sb3Bytes = new byte[(int) sb3File.length()];
        byte[] zipBytes = new byte[(int) zipFile.length()];
        FileInputStream sb3InputStream = new FileInputStream(sb3File);
        FileInputStream zipInputStream = new FileInputStream(zipFile);
        sb3InputStream.read(sb3Bytes);
        sb3InputStream.close();
        zipInputStream.read(zipBytes);
        zipInputStream.close();
        zip.setContent(zipBytes);
        List<FileDTO> fileDTOS = new ArrayList<>();
        fileDTOS.add(fileDTO);
        fileDTOS.add(zip);
        ExperimentProjection projection = new ExperimentProjection() {
            @Override
            public Integer getId() {
                return ID;
            }

            @Override
            public boolean isActive() {
                return true;
            }

            @Override
            public byte[] getProject() {
                return sb3Bytes;
            }
        };
        when(participantService.getParticipants(ID)).thenReturn(participants);
        when(experimentService.getSb3File(ID, true)).thenReturn(projection);
        when(fileService.getFileDTOs(anyInt(), anyInt())).thenReturn(fileDTOS);
        when(codeService.getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any())).thenReturn(jsonProjections);
        when(fileService.findFinalProject(anyInt(), anyInt())).thenReturn(Optional.of(sb3ZipDTO));
        when(userService.getUserById(participants.getFirst().getUser())).thenReturn(userDTOWithUsername1);
        when(userService.getUserById(participants.get(1).getUser())).thenReturn(userDTOWithUsername2);
        mvc.perform(get("/result/sb3s/all")
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .param(STEP_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        is("attachment;filename=experiment1_all_sb3s_step1.zip")));
        verify(experimentService).getSb3File(ID, true);
        verify(fileService, times(2)).getFileDTOs(anyInt(), anyInt());
        verify(codeService, times(2)).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, times(2)).findFinalProject(anyInt(), anyInt());
    }

    @Test
    public void testDownloadExperimentSb3FilesNoParticipants() throws Exception {
        when(participantService.getParticipants(ID)).thenReturn(new ArrayList<>());
        mvc.perform(get("/result/sb3s/all")
                        .param(EXPERIMENT_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(experimentService, never()).getSb3File(ID, true);
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(codeService, never()).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
    }

    @Test
    public void testDownloadLastExperimentSb3Files() throws Exception {
        URL zipUrl = getClass().getClassLoader().getResource("Taylor-b.zip");
        URL sb3 = getClass().getClassLoader().getResource("Scratch-Projekt.sb3");
        File sb3File = new File(sb3.getFile());
        File zipFile = new File(zipUrl.getFile());
        byte[] sb3Bytes = new byte[(int) sb3File.length()];
        byte[] zipBytes = new byte[(int) zipFile.length()];
        FileInputStream sb3InputStream = new FileInputStream(sb3File);
        FileInputStream zipInputStream = new FileInputStream(zipFile);
        sb3InputStream.read(sb3Bytes);
        sb3InputStream.close();
        zipInputStream.read(zipBytes);
        zipInputStream.close();
        zip.setContent(zipBytes);
        List<FileDTO> fileDTOS = new ArrayList<>();
        fileDTOS.add(fileDTO);
        fileDTOS.add(zip);
        ExperimentProjection projection = new ExperimentProjection() {
            @Override
            public Integer getId() {
                return ID;
            }

            @Override
            public boolean isActive() {
                return true;
            }

            @Override
            public byte[] getProject() {
                return sb3Bytes;
            }
        };
        when(participantService.getParticipants(ID)).thenReturn(participants);
        when(experimentService.getSb3File(ID, true)).thenReturn(projection);
        when(fileService.getFileDTOs(anyInt(), anyInt())).thenReturn(fileDTOS);
        when(userService.getUserById(participants.getFirst().getUser())).thenReturn(userDTOWithUsername1);
        when(userService.getUserById(participants.get(1).getUser())).thenReturn(userDTOWithUsername2);
        mvc.perform(get("/result/sb3s/last")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition",
                is("attachment;filename=experiment1_last_sb3s.zip")));
    }

    @Test
    public void testDownloadLastExperimentSb3FilesNoParticipants() throws Exception {
        when(participantService.getParticipants(ID)).thenReturn(new ArrayList<>());
        mvc.perform(get("/result/sb3s/last")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testDownloadAllJsonFiles() throws Exception {
        when(participantService.getParticipants(ID)).thenReturn(participants);
        when(userService.getUserById(participants.getFirst().getUser())).
            thenReturn(new UserDTO("user1", null, null, null, null, null));
        when(userService.getUserById(participants.get(1).getUser())).
            thenReturn(new UserDTO("user2", null, null, null, null, null));
        mvc.perform(get("/result/jsons/all")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition",
                is("attachment;filename=experiment1_all_jsons.zip")));
    }

    @Test
    public void testDownloadAllJsonFilesNoParticipants() throws Exception {
        when(participantService.getParticipants(ID)).thenReturn(new ArrayList<>());
        mvc.perform(get("/result/jsons/all")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
            .andExpect(status().isBadRequest());
    }

    private List<EventCountDTO> getEventCounts(int number, String event) {
        List<EventCountDTO> eventCountDTOS = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            eventCountDTOS.add(new EventCountDTO(1, 1, i, event + i));
        }
        return eventCountDTOS;
    }

    private List<FileProjection> getFileProjections(int number) {
        List<FileProjection> fileProjections = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            final int id = i;
            fileProjections.add(new FileProjection() {
                @Override
                public Integer getId() {
                    return id;
                }

                @Override
                public String getName() {
                    return "some name" + id;
                }
            });
        }
        return fileProjections;
    }

    private List<Sb3ZipDTO> getSb3ZipDTOs(int number) {
        List<Sb3ZipDTO> sb3ZipDTOs = new ArrayList<>();

        for (int i = 0; i < number; i++) {
            sb3ZipDTOs.add(new Sb3ZipDTO(ID, ID, "secret", "zip" + i, new byte[]{1, 2, 3}, LocalDateTime.now()));
        }

        return sb3ZipDTOs;
    }

    private List<BlockEventJSONProjection> getJsonProjections(int number) {
        List<BlockEventJSONProjection> projections = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            final int id = i;
            projections.add(new BlockEventJSONProjection() {
                @Override
                public Integer getId() {
                    return id;
                }

                @Override
                public String getCode() {
                    return "json" + id;
                }

                @Override
                public LocalDateTime getDate() {
                    return LocalDateTime.now().plusMinutes(id);
                }

                @Override
                public String getEvent() {
                    return "event";
                }
            });
        }
        return projections;
    }

    private List<BlockEventProjection> getBlockEventProjections(int number) {
        List<BlockEventProjection> projections = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            final int id = i;
            projections.add(new BlockEventProjection() {
                @Override
                public Integer getId() {
                    return id;
                }

                @Override
                public String getXml() {
                    return "xml" + id;
                }

                @Override
                public String getCode() {
                    return "code" + id;
                }

                @Override
                public LocalDateTime getDate() {
                    return null;
                }

                @Override
                public String getSprite() {
                    return "sprite";
                }
            });
        }
        return projections;
    }
}
