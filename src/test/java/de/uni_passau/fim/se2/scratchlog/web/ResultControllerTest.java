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

package de.uni_passau.fim.se2.scratchlog.web;

import de.uni_passau.fim.se2.scratchlog.application.exception.IncompleteDataException;
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
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.web.controller.ResultController;
import de.uni_passau.fim.se2.scratchlog.web.dto.CodesDataDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.EventCountDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.FileDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.ParticipantDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.Sb3ZipDTO;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static de.uni_passau.fim.se2.scratchlog.util.CommonAssertions.assertInvalidIdException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResultControllerTest {

    private ResultController resultController;

    @Mock
    private UserService userService;

    @Mock
    private ExperimentService experimentService;

    @Mock
    private EventService eventService;

    @Mock
    private ExperimentDataService experimentDataService;

    @Mock
    private CodeService codeService;

    @Mock
    private FileService fileService;

    @Mock
    private ParticipantService participantService;

    @Mock
    private Model model;

    @Mock
    private HttpServletResponse httpServletResponse;

    private static final String RESULT = "result";
    private static final String ERROR = "redirect:/error";
    private static final int PAGE = 0;
    private static final String JSON = "json";
    private static final int ID = 1;
    private final FileDTO fileDTO = new FileDTO(ID, ID, "secret", "file", "type", new byte[]{1, 2, 3},
        LocalDateTime.now());
    private final FileDTO zip = new FileDTO(ID, ID, "secret", "file.zip", "wav", new byte[]{1, 2, 3, 4},
        LocalDateTime.now());
    private final Sb3ZipDTO sb3ZipDTO = new Sb3ZipDTO(ID, ID, "secret", "file", new byte[]{1, 2, 3},
        LocalDateTime.now().plusMinutes(15));
    private final ParticipantDTO participantDTO1 = new ParticipantDTO(ID, ID);
    private final ParticipantDTO participantDTO2 = new ParticipantDTO(2, ID);
    private final CodesDataDTO codesDataDTO = new CodesDataDTO(ID, ID, 9);
    private final List<EventCountDTO> blockEvents = getEventCounts(5, "CREATE");
    private final List<EventCountDTO> clickEvents = getEventCounts(3, "GREENFLAG");
    private final List<EventCountDTO> resourceEvents = getEventCounts(2, "RENAME");
    private final List<FileProjection> files = getFileProjections(7);
    private final List<Integer> zips = Arrays.asList(1, 4, 10, 18);
    private final List<Sb3ZipDTO> sb3ZipDTOs = getSb3ZipDTOs(6);
    private final List<BlockEventXMLProjection> xmlProjections = getXmlProjections(3);
    private final List<BlockEventJSONProjection> jsonProjections = getJsonProjections(4);
    private final Page<BlockEventProjection> blockEventProjections = new PageImpl<>(getBlockEventProjections(2));
    private List<FileDTO> fileDTOS = new ArrayList<>();
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

    @BeforeEach
    public void setUp() {
        fileDTOS = new ArrayList<>();
        zip.setContent(new byte[]{1, 2, 3, 4});

        ZipExportService zipExportService = new ZipExportService(
            codeService, experimentService, fileService, participantService
        );
        resultController = new ResultController(
            userService, eventService, experimentDataService, codeService, fileService, zipExportService
        );
    }

    @Test
    public void testGetResult() {
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
        assertEquals(RESULT, resultController.getResult(ID, ID, model).getViewName());
        verify(userService).existsParticipant(ID, ID);
        verify(eventService).getBlockEventCounts(ID, ID);
        verify(eventService).getClickEventCounts(ID, ID);
        verify(eventService).getResourceEventCounts(ID, ID);
        verify(fileService).getFiles(ID, ID);
        verify(fileService).getZipIds(ID, ID);
        verify(codeService).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(experimentDataService).getAnalyzedProgramDataCount(jsonProjections);
        verify(eventService).getCodesData(ID, ID);
        verify(model, times(12)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResultNoJsons() {
        when(userService.existsParticipant(ID, ID)).thenReturn(true);
        when(eventService.getBlockEventCounts(ID, ID)).thenReturn(blockEvents);
        when(eventService.getClickEventCounts(ID, ID)).thenReturn(clickEvents);
        when(eventService.getResourceEventCounts(ID, ID)).thenReturn(resourceEvents);
        when(fileService.getFiles(ID, ID)).thenReturn(files);
        when(fileService.getZipIds(ID, ID)).thenReturn(zips);
        when(eventService.getCodesData(ID, ID)).thenReturn(codesDataDTO);
        assertEquals(RESULT, resultController.getResult(ID, ID, model).getViewName());
        verify(userService).existsParticipant(ID, ID);
        verify(eventService).getBlockEventCounts(ID, ID);
        verify(eventService).getClickEventCounts(ID, ID);
        verify(eventService).getResourceEventCounts(ID, ID);
        verify(fileService).getFiles(ID, ID);
        verify(fileService).getZipIds(ID, ID);
        verify(codeService).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(experimentDataService, never()).getAnalyzedProgramDataCount(any());
        verify(eventService).getCodesData(ID, ID);
        verify(model, times(12)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResultCodesDataZero() {
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
        assertEquals(RESULT, resultController.getResult(ID, ID, model).getViewName());
        verify(userService).existsParticipant(ID, ID);
        verify(eventService).getBlockEventCounts(ID, ID);
        verify(eventService).getClickEventCounts(ID, ID);
        verify(eventService).getResourceEventCounts(ID, ID);
        verify(fileService).getFiles(ID, ID);
        verify(fileService).getZipIds(ID, ID);
        verify(codeService).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(experimentDataService).getAnalyzedProgramDataCount(jsonProjections);
        verify(eventService).getCodesData(ID, ID);
        verify(model, times(12)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResultNotFound() {
        when(userService.existsParticipant(ID, ID)).thenReturn(true);
        when(eventService.getBlockEventCounts(ID, ID)).thenReturn(blockEvents);
        when(eventService.getClickEventCounts(ID, ID)).thenReturn(clickEvents);
        when(eventService.getResourceEventCounts(ID, ID)).thenReturn(resourceEvents);
        when(fileService.getFiles(ID, ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, resultController.getResult(ID, ID, model).getViewName());
        verify(userService).existsParticipant(ID, ID);
        verify(eventService).getBlockEventCounts(ID, ID);
        verify(eventService).getClickEventCounts(ID, ID);
        verify(eventService).getResourceEventCounts(ID, ID);
        verify(fileService).getFiles(ID, ID);
        verify(fileService, never()).getZipIds(anyInt(), anyInt());
        verify(codeService, never()).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(experimentDataService, never()).getAnalyzedProgramDataCount(any());
        verify(eventService, never()).getCodesData(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResultNoParticipant() {
        assertEquals(ERROR, resultController.getResult(ID, ID, model).getViewName());
        verify(userService).existsParticipant(ID, ID);
        verify(eventService, never()).getBlockEventCounts(anyInt(), anyInt());
        verify(eventService, never()).getClickEventCounts(anyInt(), anyInt());
        verify(eventService, never()).getResourceEventCounts(anyInt(), anyInt());
        verify(fileService, never()).getFiles(anyInt(), anyInt());
        verify(fileService, never()).getZipIds(anyInt(), anyInt());
        verify(codeService, never()).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(experimentDataService, never()).getAnalyzedProgramDataCount(any());
        verify(eventService, never()).getCodesData(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDownloadFile() {
        when(fileService.findFile(ID)).thenReturn(fileDTO);
        Object responseEntity = resultController.downloadFile(ID);
        assertAll(
                () -> assertEquals(ResponseEntity.class, responseEntity.getClass()),
                () -> assertEquals(HttpStatus.OK, ((ResponseEntity<?>) responseEntity).getStatusCode()),
                () -> assertEquals(fileDTO.getContent(), ((ResponseEntity<?>) responseEntity).getBody())
        );
        verify(fileService).findFile(ID);
    }

    @Test
    public void testDownloadFileNotFound() {
        when(fileService.findFile(ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, resultController.downloadFile(ID));
        verify(fileService).findFile(ID);
    }

    @Test
    public void testGenerateZipFile() throws IOException {
        URL sb3 = getClass().getClassLoader().getResource("Scratch-Projekt.sb3");
        File file = new File(sb3.getFile());
        byte[] b = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(b);
        fileInputStream.close();
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
                return b;
            }
        };
        when(httpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {

            }
        });
        when(experimentService.getSb3File(ID, true)).thenReturn(projection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(codeService.findJsonById(ID)).thenReturn(JSON);
        assertDoesNotThrow(
                () -> resultController.generateZipFile(ID, ID, ID, httpServletResponse)
        );
        verify(experimentService).getSb3File(ID, true);
        verify(fileService).getFileDTOs(ID, ID);
        verify(codeService).findJsonById(ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testGenerateZipFileZipFileContent() throws IOException {
        URL zipFile = getClass().getClassLoader().getResource("Taylor-b.zip");
        File file = new File(zipFile.getFile());
        byte[] b = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(b);
        fileInputStream.close();
        zip.setContent(b);
        fileDTOS.add(fileDTO);
        fileDTOS.add(zip);
        when(httpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {

            }
        });
        when(experimentService.getSb3File(ID, true)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(codeService.findJsonById(ID)).thenReturn(JSON);
        assertDoesNotThrow(
                () -> resultController.generateZipFile(ID, ID, ID, httpServletResponse)
        );
        verify(experimentService).getSb3File(ID, true);
        verify(fileService).getFileDTOs(ID, ID);
        verify(codeService).findJsonById(ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testGenerateZipFileSameName() throws IOException {
        URL zipFile = getClass().getClassLoader().getResource("Taylor-b.zip");
        File file = new File(zipFile.getFile());
        byte[] b = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(b);
        fileInputStream.close();
        zip.setContent(b);
        fileDTOS.add(fileDTO);
        fileDTOS.add(new FileDTO(ID, ID, "secret", "file", "type", new byte[]{1, 2, 3}, LocalDateTime.now()));
        fileDTOS.add(zip);
        fileDTOS.add(new FileDTO(ID, ID, "secret", "file.zip", "wav", zip.getContent(), LocalDateTime.now()));
        when(httpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {

            }
        });
        when(experimentService.getSb3File(ID, true)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(codeService.findJsonById(ID)).thenReturn(JSON);
        assertDoesNotThrow(
                () -> resultController.generateZipFile(ID, ID, ID, httpServletResponse)
        );
        verify(experimentService).getSb3File(ID, true);
        verify(fileService).getFileDTOs(ID, ID);
        verify(codeService).findJsonById(ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testGenerateZipFileProjectNull() throws IOException {
        fileDTOS.add(fileDTO);
        fileDTOS.add(zip);
        when(httpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {

            }
        });
        when(experimentService.getSb3File(ID, true)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(codeService.findJsonById(ID)).thenReturn(JSON);
        assertDoesNotThrow(
                () -> resultController.generateZipFile(ID, ID, ID, httpServletResponse)
        );
        verify(experimentService).getSb3File(ID, true);
        verify(fileService).getFileDTOs(ID, ID);
        verify(codeService).findJsonById(ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testGenerateZipFilesIO() throws IOException {
        when(httpServletResponse.getOutputStream()).thenThrow(IOException.class);
        assertThrows(RuntimeException.class,
                () -> resultController.generateZipFile(ID, ID, ID, httpServletResponse)
        );
        verify(httpServletResponse).getOutputStream();
    }

    @Test
    public void testDownloadZip() {
        when(fileService.findZip(ID)).thenReturn(sb3ZipDTO);
        Object responseEntity = resultController.downloadZip(ID);
        assertAll(
                () -> assertEquals(ResponseEntity.class, responseEntity.getClass()),
                () -> assertEquals(HttpStatus.OK, ((ResponseEntity<?>) responseEntity).getStatusCode()),
                () -> assertEquals(sb3ZipDTO.getContent(), ((ResponseEntity<?>) responseEntity).getBody())
        );
        verify(fileService).findZip(ID);
    }

    @Test
    public void testDownloadZipNotFound() {
        when(fileService.findZip(ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, resultController.downloadZip(ID));
        verify(fileService).findZip(ID);
    }

    @Test
    public void testDownloadAllZips() throws IOException {
        when(fileService.getZipFiles(ID, ID)).thenReturn(sb3ZipDTOs);
        when(httpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {

            }
        });
        assertDoesNotThrow(
                () -> resultController.downloadAllZips(ID, ID, httpServletResponse)
        );
        verify(fileService).getZipFiles(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadAllZipsIO() throws IOException {
        when(httpServletResponse.getOutputStream()).thenThrow(IOException.class);
        assertThrows(RuntimeException.class,
                () -> resultController.downloadAllZips(ID, ID, httpServletResponse)
        );
        verify(fileService, never()).getZipFiles(anyInt(), anyInt());
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadAllXmlFiles() throws IOException {
        when(codeService.getXMLForUser(ID, ID)).thenReturn(xmlProjections);
        when(httpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {

            }
        });
        assertDoesNotThrow(
                () -> resultController.downloadAllXmlFiles(ID, ID, httpServletResponse)
        );
        verify(codeService).getXMLForUser(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadAllXmlFilesIO() throws IOException {
        when(httpServletResponse.getOutputStream()).thenThrow(IOException.class);
        assertThrows(RuntimeException.class,
                () -> resultController.downloadAllXmlFiles(ID, ID, httpServletResponse)
        );
        verify(codeService, never()).getXMLForUser(anyInt(), anyInt());
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadAllJsonFiles() throws IOException {
        when(codeService.getJsonForUser(ID, ID)).thenReturn(jsonProjections);
        when(httpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {

            }
        });
        assertDoesNotThrow(
                () -> resultController.downloadAllJsonFiles(ID, ID, httpServletResponse)
        );
        verify(codeService).getJsonForUser(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadAllJsonFilesIO() throws IOException {
        when(httpServletResponse.getOutputStream()).thenThrow(IOException.class);
        assertThrows(RuntimeException.class,
                () -> resultController.downloadAllJsonFiles(ID, ID, httpServletResponse)
        );
        verify(codeService, never()).getJsonForUser(anyInt(), anyInt());
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testGetCodes() {
        when(codeService.getCodesForUser(anyInt(), anyInt(),
                any(PageRequest.class))).thenReturn(blockEventProjections);
        List<BlockEventProjection> projections = resultController.getCodes(ID, ID, PAGE);
        assertAll(
                () -> assertEquals(2, projections.size()),
                () -> assertEquals(0, projections.getFirst().getId()),
                () -> assertEquals("xml0", projections.getFirst().getXml()),
                () -> assertEquals("code0", projections.getFirst().getCode()),
                () -> assertEquals(1, projections.get(1).getId()),
                () -> assertEquals("xml1", projections.get(1).getXml()),
                () -> assertEquals("code1", projections.get(1).getCode())
        );
        verify(codeService).getCodesForUser(anyInt(), anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetCodesInvalidPage() {
        assertInvalidIdException(() -> resultController.getCodes(ID, ID, -1));
        verify(codeService, never()).getCodesForUser(anyInt(), anyInt(), any(PageRequest.class));
    }

    @Test
    public void testDownloadSb3Files() throws IOException {
        URL sb3 = getClass().getClassLoader().getResource("Scratch-Projekt.sb3");
        File file = new File(sb3.getFile());
        byte[] b = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(b);
        fileInputStream.close();
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
                return b;
            }
        };
        when(httpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {

            }
        });
        when(experimentService.getSb3File(ID, true)).thenReturn(projection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(codeService.getFilteredJsons(ID, ID, 0, 0, 0, project)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(project);
        assertDoesNotThrow(
                () -> resultController.downloadSb3Files(ID, ID, null, null, null, null,
                        httpServletResponse)
        );
        verify(experimentService).getSb3File(ID, true);
        verify(fileService).getFileDTOs(ID, ID);
        verify(codeService).getFilteredJsons(ID, ID, 0, 0, 0, project);
        verify(fileService).findFinalProject(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadSb3FilesProjectionNull() throws IOException {
        Optional<Sb3ZipDTO> project = Optional.of(sb3ZipDTO);
        when(httpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {

            }
        });
        when(experimentService.getSb3File(ID, true)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(codeService.getFilteredJsons(ID, ID, 0, 0, 0, project)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(project);
        assertDoesNotThrow(
                () -> resultController.downloadSb3Files(ID, ID, null, null, null, null,
                        httpServletResponse)
        );
        verify(experimentService).getSb3File(ID, true);
        verify(fileService).getFileDTOs(ID, ID);
        verify(codeService).getFilteredJsons(ID, ID, 0, 0, 0, project);
        verify(fileService).findFinalProject(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadSb3FilesFinalProjectEmpty() throws IOException {
        Optional<Sb3ZipDTO> noSavedProject = Optional.empty();
        when(httpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {

            }
        });
        when(experimentService.getSb3File(ID, true)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(codeService.getFilteredJsons(ID, ID, 0, 0, 0, noSavedProject)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(noSavedProject);
        assertDoesNotThrow(
                () -> resultController.downloadSb3Files(ID, ID, null, null, null, null,
                        httpServletResponse)
        );
        verify(experimentService).getSb3File(ID, true);
        verify(fileService).getFileDTOs(ID, ID);
        verify(codeService).getFilteredJsons(ID, ID, 0, 0, 0, noSavedProject);
        verify(fileService).findFinalProject(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadSb3FilesStep() throws IOException {
        URL sb3 = getClass().getClassLoader().getResource("Scratch-Projekt.sb3");
        File file = new File(sb3.getFile());
        byte[] b = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(b);
        fileInputStream.close();
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
                return b;
            }
        };
        when(httpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {

            }
        });
        when(experimentService.getSb3File(ID, true)).thenReturn(projection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(fileService.findFinalProject(ID, ID)).thenReturn(project);
        when(codeService.getFilteredJsons(ID, ID, ID, 0, 0, project)).thenReturn(jsonProjections);
        assertDoesNotThrow(
                () -> resultController.downloadSb3Files(ID, ID, ID, null, null, null,
                        httpServletResponse)
        );
        verify(experimentService).getSb3File(ID, true);
        verify(fileService).getFileDTOs(ID, ID);
        verify(codeService).getFilteredJsons(ID, ID, ID, 0, 0, project);
        verify(fileService).findFinalProject(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadSb3FilesStartStopStartBiggerEnd() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadSb3Files(ID, ID, null, 2, ID, true,
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt(), anyBoolean());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(codeService, never()).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesStartStopStartInvalidStart() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadSb3Files(ID, ID, null, -1, 3, true,
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt(), anyBoolean());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(codeService, never()).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesStartStopStartInvalidEnd() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadSb3Files(ID, ID, null, ID, 0, true,
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt(), anyBoolean());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(codeService, never()).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesIOException() throws IOException {
        when(httpServletResponse.getOutputStream()).thenThrow(IOException.class);
        assertThrows(RuntimeException.class,
                () -> resultController.downloadSb3Files(ID, ID, null, null, null, null,
                        httpServletResponse)
        );
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadSb3FilesStepInvalid() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadSb3Files(ID, ID, 0, null, null, null,
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt(), anyBoolean());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(codeService, never()).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesStartAndStepNotNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadSb3Files(ID, ID, ID, ID, 3, false,
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt(), anyBoolean());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(codeService, never()).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesIncludeNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadSb3Files(ID, ID, null, ID, 3, null,
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt(), anyBoolean());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(codeService, never()).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesEndNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadSb3Files(ID, ID, null, ID, null, false,
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt(), anyBoolean());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(codeService, never()).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesStartNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadSb3Files(ID, ID, null, null, 3, false,
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt(), anyBoolean());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(codeService, never()).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesStartAndEndNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadSb3Files(ID, ID, null, null, null, false,
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt(), anyBoolean());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(codeService, never()).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadExperimentSb3Files() throws IOException {
        when(httpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {

            }
        });
        when(participantService.getParticipants(ID)).thenReturn(participants);
        when(experimentService.getSb3File(ID, true)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(anyInt(), anyInt())).thenReturn(fileDTOS);
        when(codeService.getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any())).thenReturn(
                jsonProjections);
        when(fileService.findFinalProject(anyInt(), anyInt())).thenReturn(Optional.of(sb3ZipDTO));
        assertDoesNotThrow(
                () -> resultController.downloadExperimentSb3Files(ID, 0, httpServletResponse)
        );
        verify(participantService).getParticipants(ID);
        verify(experimentService).getSb3File(ID, true);
        verify(fileService, times(2)).getFileDTOs(anyInt(), anyInt());
        verify(codeService, times(2)).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, times(2)).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadExperimentSb3FilesSteps() throws IOException {
        when(httpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {

            }
        });
        when(participantService.getParticipants(ID)).thenReturn(participants);
        when(experimentService.getSb3File(ID, true)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(anyInt(), anyInt())).thenReturn(fileDTOS);
        when(codeService.getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any())).thenReturn(
                jsonProjections);
        when(fileService.findFinalProject(anyInt(), anyInt())).thenReturn(Optional.of(sb3ZipDTO));
        assertDoesNotThrow(
                () -> resultController.downloadExperimentSb3Files(ID, ID, httpServletResponse)
        );
        verify(participantService).getParticipants(ID);
        verify(experimentService).getSb3File(ID, true);
        verify(fileService, times(2)).getFileDTOs(anyInt(), anyInt());
        verify(codeService, times(2)).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, times(2)).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadExperimentSb3FilesNoEntries() throws IOException {
        when(httpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {

            }
        });
        when(participantService.getParticipants(ID)).thenReturn(participants);
        when(experimentService.getSb3File(ID, true)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(anyInt(), anyInt())).thenReturn(fileDTOS);
        when(codeService.getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any())).thenReturn(
                new ArrayList<>());
        when(fileService.findFinalProject(anyInt(), anyInt())).thenReturn(Optional.of(sb3ZipDTO));
        assertDoesNotThrow(
                () -> resultController.downloadExperimentSb3Files(ID, 0, httpServletResponse)
        );
        verify(participantService).getParticipants(ID);
        verify(experimentService).getSb3File(ID, true);
        verify(fileService, times(2)).getFileDTOs(anyInt(), anyInt());
        verify(codeService, times(2)).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, times(2)).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadExperimentSb3FilesIOException() throws IOException {
        when(httpServletResponse.getOutputStream()).thenThrow(IOException.class);
        assertThrows(RuntimeException.class,
                () -> resultController.downloadExperimentSb3Files(ID, 0, httpServletResponse)
        );
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(codeService, never()).getFilteredJsons(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
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

    private List<BlockEventXMLProjection> getXmlProjections(int number) {
        List<BlockEventXMLProjection> projections = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            final int id = i;
            projections.add(new BlockEventXMLProjection() {
                @Override
                public Integer getId() {
                    return id;
                }

                @Override
                public String getXml() {
                    return "xml" + id;
                }
            });
        }
        return projections;
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
                    return LocalDateTime.now().plusMinutes(id).minusSeconds(id);
                }

                @Override
                public String getEvent() {
                    return "event";
                }
            });
        }
        return projections;
    }

    private List<BlockEventJSONProjection> getJsonProjectionsWithCustomTimeDifference() {
        List<BlockEventJSONProjection> projections = new ArrayList<>();

        projections.add(new BlockEventJSONProjection() {
            @Override
            public Integer getId() {
                return 1;
            }

            @Override
            public String getCode() {
                return "json" + 1;
            }

            @Override
            public LocalDateTime getDate() {
                return LocalDateTime.now();
            }

            @Override
            public String getEvent() {
                return "event";
            }
        });

        projections.add(new BlockEventJSONProjection() {
            @Override
            public Integer getId() {
                return 2;
            }

            @Override
            public String getCode() {
                return "json" + 2;
            }

            @Override
            public LocalDateTime getDate() {
                return LocalDateTime.now().plusMinutes(Constants.MAX_ALLOWED_BREAK_FACTOR + 1);
            }

            @Override
            public String getEvent() {
                return "event";
            }
        });

        projections.add(new BlockEventJSONProjection() {
            @Override
            public Integer getId() {
                return 3;
            }

            @Override
            public String getCode() {
                return "json" + 3;
            }

            @Override
            public LocalDateTime getDate() {
                return LocalDateTime.now().plusMinutes(Constants.MAX_ALLOWED_BREAK_FACTOR + 3);
            }

            @Override
            public String getEvent() {
                return "event";
            }
        });

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
