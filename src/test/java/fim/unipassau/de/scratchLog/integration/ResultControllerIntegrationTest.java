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
package fim.unipassau.de.scratchLog.integration;

import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.application.service.EventService;
import fim.unipassau.de.scratchLog.application.service.ExperimentService;
import fim.unipassau.de.scratchLog.application.service.FileService;
import fim.unipassau.de.scratchLog.application.service.UserService;
import fim.unipassau.de.scratchLog.persistence.projection.BlockEventJSONProjection;
import fim.unipassau.de.scratchLog.persistence.projection.BlockEventProjection;
import fim.unipassau.de.scratchLog.persistence.projection.BlockEventXMLProjection;
import fim.unipassau.de.scratchLog.persistence.projection.ExperimentProjection;
import fim.unipassau.de.scratchLog.persistence.projection.FileProjection;
import fim.unipassau.de.scratchLog.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratchLog.web.controller.ResultController;
import fim.unipassau.de.scratchLog.web.dto.CodesDataDTO;
import fim.unipassau.de.scratchLog.web.dto.EventCountDTO;
import fim.unipassau.de.scratchLog.web.dto.FileDTO;
import fim.unipassau.de.scratchLog.web.dto.Sb3ZipDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ResultController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class ResultControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ExperimentService experimentService;

    @MockBean
    private EventService eventService;

    @MockBean
    private FileService fileService;

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
    private final FileDTO fileDTO = new FileDTO(ID, ID, LocalDateTime.now(), "file", "type",
            new byte[]{1, 2, 3});
    private final FileDTO zip = new FileDTO(ID, ID, LocalDateTime.now(), "file.zip", "wav",
            new byte[]{1, 2, 3, 4});
    private final Sb3ZipDTO sb3ZipDTO = new Sb3ZipDTO(ID, ID, LocalDateTime.now(), "file", new byte[]{1, 2, 3});
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
                .andExpect(model().attribute("codeCount", is(codesDataDTO.getCount())))
                .andExpect(status().isOk())
                .andExpect(view().name(RESULT));
        verify(userService).existsParticipant(ID, ID);
        verify(eventService).getBlockEventCounts(ID, ID);
        verify(eventService).getClickEventCounts(ID, ID);
        verify(eventService).getResourceEventCounts(ID, ID);
        verify(fileService).getFiles(ID, ID);
        verify(fileService).getZipIds(ID, ID);
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
                .andExpect(model().attribute("codeCount", is(0)))
                .andExpect(status().isOk())
                .andExpect(view().name(RESULT));
        verify(userService).existsParticipant(ID, ID);
        verify(eventService).getBlockEventCounts(ID, ID);
        verify(eventService).getClickEventCounts(ID, ID);
        verify(eventService).getResourceEventCounts(ID, ID);
        verify(fileService).getFiles(ID, ID);
        verify(fileService).getZipIds(ID, ID);
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
    }

    @Test
    public void testGetResultInvalidId() throws Exception {
        mvc.perform(get("/result")
                .param(EXPERIMENT_PARAM, "0")
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute("blockEvents", nullValue()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(eventService, never()).getBlockEventCounts(anyInt(), anyInt());
        verify(eventService, never()).getClickEventCounts(anyInt(), anyInt());
        verify(eventService, never()).getResourceEventCounts(anyInt(), anyInt());
        verify(fileService, never()).getFiles(anyInt(), anyInt());
        verify(fileService, never()).getZipIds(anyInt(), anyInt());
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
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
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
        when(experimentService.getSb3File(ID)).thenReturn(projection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(eventService.findJsonById(ID)).thenReturn(JSON);
        mvc.perform(get("/result/generate")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .param(JSON, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk());
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).findJsonById(ID);
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
        when(experimentService.getSb3File(ID)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(eventService.findJsonById(ID)).thenReturn(JSON);
        mvc.perform(get("/result/generate")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .param(JSON, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk());
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).findJsonById(ID);
    }

    @Test
    public void testGenerateZipFileNotFound() throws Exception {
        when(experimentService.getSb3File(ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/result/generate")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .param(JSON, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isNotFound());
        verify(experimentService).getSb3File(ID);
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).findJsonById(anyInt());
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
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).findJsonById(anyInt());
    }

    @Test
    public void testGenerateZipFileInvalidExperimentId() throws Exception {
        mvc.perform(get("/result/generate")
                .param(EXPERIMENT_PARAM, "0")
                .param(USER_PARAM, ID_STRING)
                .param(JSON, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).findJsonById(anyInt());
    }

    @Test
    public void testGenerateZipFileInvalidUserId() throws Exception {
        mvc.perform(get("/result/generate")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, "-1")
                .param(JSON, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).findJsonById(anyInt());
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
    public void testDownloadZipInvalidId() throws Exception {
        mvc.perform(get("/result/zip")
                .param(ID_PARAM, "0")
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(fileService, never()).findZip(anyInt());
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
    public void testDownloadAllZipsInvalidId() throws Exception {
        mvc.perform(get("/result/zips")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, "0")
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(fileService, never()).getZipFiles(anyInt(), anyInt());
    }

    @Test
    public void testDownloadAllXmlFiles() throws Exception {
        when(eventService.getXMLForUser(ID, ID)).thenReturn(xmlProjections);
        mvc.perform(get("/result/xmls")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk());
        verify(eventService).getXMLForUser(ID, ID);
    }

    @Test
    public void testDownloadAllXmlFilesNotFound() throws Exception {
        when(eventService.getXMLForUser(ID, ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/result/xmls")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isNotFound());
        verify(eventService).getXMLForUser(ID, ID);
    }

    @Test
    public void testDownloadAllXmlFilesInvalidId() throws Exception {
        mvc.perform(get("/result/xmls")
                .param(EXPERIMENT_PARAM, "-1")
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(eventService, never()).getXMLForUser(anyInt(), anyInt());
    }

    @Test
    public void testDownloadAllJsonFiles() throws Exception {
        when(eventService.getJsonForUser(ID, ID)).thenReturn(jsonProjections);
        mvc.perform(get("/result/jsons")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk());
        verify(eventService).getJsonForUser(ID, ID);
    }

    @Test
    public void testDownloadAllJsonFilesNotFound() throws Exception {
        when(eventService.getJsonForUser(ID, ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/result/jsons")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isNotFound());
        verify(eventService).getJsonForUser(ID, ID);
    }

    @Test
    public void testDownloadAllJsonFilesInvalidId() throws Exception {
        mvc.perform(get("/result/jsons")
                .param(EXPERIMENT_PARAM, "id")
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
    }

    @Test
    public void testGetCodes() throws Exception {
        when(eventService.getCodesForUser(anyInt(), anyInt(), any(PageRequest.class))).thenReturn(blockEventProjections);
        mvc.perform(get("/result/codes")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .param(PAGE_PARAM, PAGE)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk());
        verify(eventService).getCodesForUser(anyInt(), anyInt(), any(PageRequest.class));
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
        verify(eventService, never()).getCodesForUser(anyInt(), anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetCodesInvalidExperimentId() throws Exception {
        mvc.perform(get("/result/codes")
                .param(EXPERIMENT_PARAM, "-1")
                .param(USER_PARAM, ID_STRING)
                .param(PAGE_PARAM, PAGE)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(eventService, never()).getCodesForUser(anyInt(), anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetCodesInvalidUserId() throws Exception {
        mvc.perform(get("/result/codes")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, PAGE)
                .param(PAGE_PARAM, PAGE)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(eventService, never()).getCodesForUser(anyInt(), anyInt(), any(PageRequest.class));
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
        when(experimentService.getSb3File(ID)).thenReturn(projection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(eventService.getJsonForUser(ID, ID)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(Optional.of(sb3ZipDTO));
        mvc.perform(get("/result/sb3s")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        is("attachment;filename=zip_user1_experiment1.zip")));
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).getJsonForUser(ID, ID);
        verify(fileService).findFinalProject(ID, ID);
    }

    @Test
    public void testDownloadSb3FilesNoInitialAndFinalProjects() throws Exception {
        when(experimentService.getSb3File(ID)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(new ArrayList<>());
        when(eventService.getJsonForUser(ID, ID)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(Optional.empty());
        mvc.perform(get("/result/sb3s")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        is("attachment;filename=zip_user1_experiment1.zip")));
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).getJsonForUser(ID, ID);
        verify(fileService).findFinalProject(ID, ID);
    }

    @Test
    public void testDownloadSb3FilesStep() throws Exception {
        when(experimentService.getSb3File(ID)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(new ArrayList<>());
        when(eventService.getJsonForUser(ID, ID)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(Optional.of(sb3ZipDTO));
        mvc.perform(get("/result/sb3s")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .param(STEP_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        is("attachment;filename=zip_user1_experiment1.zip")));
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).getJsonForUser(ID, ID);
        verify(fileService).findFinalProject(ID, ID);
    }

    @Test
    public void testDownloadSb3FilesStartStop() throws Exception {
        when(experimentService.getSb3File(ID)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(new ArrayList<>());
        when(eventService.getJsonForUser(ID, ID)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(Optional.of(sb3ZipDTO));
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
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).getJsonForUser(ID, ID);
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
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
    }

    @Test
    public void testDownloadSb3FilesInvalidExperimentId() throws Exception {
        mvc.perform(get("/result/sb3s")
                .param(EXPERIMENT_PARAM, "0")
                .param(USER_PARAM, ID_STRING)
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
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
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
    }

    @Test
    public void testDownloadSb3FilesInvalidEndPosition() throws Exception {
        when(experimentService.getSb3File(ID)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(new ArrayList<>());
        when(eventService.getJsonForUser(ID, ID)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(Optional.of(sb3ZipDTO));
        mvc.perform(get("/result/sb3s")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .param(START_PARAM, ID_STRING)
                .param(END_PARAM, "5")
                .param(INCLUDE_PARAM, "false")
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).getJsonForUser(ID, ID);
        verify(fileService).findFinalProject(ID, ID);
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
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
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
            sb3ZipDTOs.add(new Sb3ZipDTO(ID, ID, LocalDateTime.now(), "zip" + i, new byte[]{1, 2, 3}));
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
