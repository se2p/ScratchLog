package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.exception.IncompleteDataException;
import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.EventService;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.application.service.FileService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventJSONProjection;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventProjection;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventXMLProjection;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentProjection;
import fim.unipassau.de.scratch1984.persistence.projection.FileProjection;
import fim.unipassau.de.scratch1984.web.controller.ResultController;
import fim.unipassau.de.scratch1984.web.dto.CodesDataDTO;
import fim.unipassau.de.scratch1984.web.dto.EventCountDTO;
import fim.unipassau.de.scratch1984.web.dto.FileDTO;
import fim.unipassau.de.scratch1984.web.dto.Sb3ZipDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResultControllerTest {

    @InjectMocks
    private ResultController resultController;

    @Mock
    private UserService userService;

    @Mock
    private ExperimentService experimentService;

    @Mock
    private EventService eventService;

    @Mock
    private FileService fileService;

    @Mock
    private Model model;

    @Mock
    private HttpServletResponse httpServletResponse;

    private static final String RESULT = "result";
    private static final String ERROR = "redirect:/error";
    private static final String ID_STRING = "1";
    private static final String PAGE = "0";
    private static final String JSON = "json";
    private static final int ID = 1;
    private final FileDTO fileDTO = new FileDTO(ID, ID, LocalDateTime.now(), "file", "type",
            new byte[]{1, 2, 3});
    private final FileDTO zip = new FileDTO(ID, ID, LocalDateTime.now(), "file.zip", "wav",
            new byte[]{1, 2, 3, 4});
    private final Sb3ZipDTO sb3ZipDTO = new Sb3ZipDTO(ID, ID, LocalDateTime.now().plusMinutes(15), "file",
            new byte[]{1, 2, 3});
    private final CodesDataDTO codesDataDTO = new CodesDataDTO(ID, ID, 9);
    private final List<EventCountDTO> blockEvents = getEventCounts(5, "CREATE");
    private final List<EventCountDTO> resourceEvents = getEventCounts(2, "RENAME");
    private final List<FileProjection> files = getFileProjections(7);
    private final List<Integer> zips = Arrays.asList(1, 4, 10, 18);
    private final List<Sb3ZipDTO> sb3ZipDTOs = getSb3ZipDTOs(6);
    private final List<BlockEventXMLProjection> xmlProjections = getXmlProjections(3);
    private final List<BlockEventJSONProjection> jsonProjections = getJsonProjections(4);
    private final Page<BlockEventProjection> blockEventProjections = new PageImpl<>(getBlockEventProjections(2));
    private final List<FileDTO> fileDTOS = new ArrayList<>();
    ExperimentProjection experimentProjection = new ExperimentProjection() {
        @Override
        public Integer getId() {
            return ID;
        }

        @Override
        public byte[] getProject() {
            return null;
        }
    };

    @Test
    public void testGetResult() {
        when(userService.existsParticipant(ID, ID)).thenReturn(true);
        when(eventService.getBlockEventCounts(ID, ID)).thenReturn(blockEvents);
        when(eventService.getResourceEventCounts(ID, ID)).thenReturn(resourceEvents);
        when(fileService.getFiles(ID, ID)).thenReturn(files);
        when(fileService.getZipIds(ID, ID)).thenReturn(zips);
        when(eventService.getCodesData(ID, ID)).thenReturn(codesDataDTO);
        assertEquals(RESULT, resultController.getResult(ID_STRING, ID_STRING, model).getViewName());
        verify(userService).existsParticipant(ID, ID);
        verify(eventService).getBlockEventCounts(ID, ID);
        verify(eventService).getResourceEventCounts(ID, ID);
        verify(fileService).getFiles(ID, ID);
        verify(fileService).getZipIds(ID, ID);
        verify(eventService).getCodesData(ID, ID);
        verify(model, times(8)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResultCodesDataZero() {
        when(userService.existsParticipant(ID, ID)).thenReturn(true);
        when(eventService.getBlockEventCounts(ID, ID)).thenReturn(blockEvents);
        when(eventService.getResourceEventCounts(ID, ID)).thenReturn(resourceEvents);
        when(fileService.getFiles(ID, ID)).thenReturn(files);
        when(fileService.getZipIds(ID, ID)).thenReturn(zips);
        when(eventService.getCodesData(ID, ID)).thenReturn(new CodesDataDTO());
        assertEquals(RESULT, resultController.getResult(ID_STRING, ID_STRING, model).getViewName());
        verify(userService).existsParticipant(ID, ID);
        verify(eventService).getBlockEventCounts(ID, ID);
        verify(eventService).getResourceEventCounts(ID, ID);
        verify(fileService).getFiles(ID, ID);
        verify(fileService).getZipIds(ID, ID);
        verify(eventService).getCodesData(ID, ID);
        verify(model, times(8)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResultNotFound() {
        when(userService.existsParticipant(ID, ID)).thenReturn(true);
        when(eventService.getBlockEventCounts(ID, ID)).thenReturn(blockEvents);
        when(eventService.getResourceEventCounts(ID, ID)).thenReturn(resourceEvents);
        when(fileService.getFiles(ID, ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, resultController.getResult(ID_STRING, ID_STRING, model).getViewName());
        verify(userService).existsParticipant(ID, ID);
        verify(eventService).getBlockEventCounts(ID, ID);
        verify(eventService).getResourceEventCounts(ID, ID);
        verify(fileService).getFiles(ID, ID);
        verify(fileService, never()).getZipIds(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResultNoParticipant() {
        assertEquals(ERROR, resultController.getResult(ID_STRING, ID_STRING, model).getViewName());
        verify(userService).existsParticipant(ID, ID);
        verify(eventService, never()).getBlockEventCounts(anyInt(), anyInt());
        verify(eventService, never()).getResourceEventCounts(anyInt(), anyInt());
        verify(fileService, never()).getFiles(anyInt(), anyInt());
        verify(fileService, never()).getZipIds(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResultInvalidExperimentId() {
        assertEquals(ERROR, resultController.getResult("0", ID_STRING, model).getViewName());
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(eventService, never()).getBlockEventCounts(anyInt(), anyInt());
        verify(eventService, never()).getResourceEventCounts(anyInt(), anyInt());
        verify(fileService, never()).getFiles(anyInt(), anyInt());
        verify(fileService, never()).getZipIds(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResultInvalidUserId() {
        assertEquals(ERROR, resultController.getResult(ID_STRING, "  ", model).getViewName());
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(eventService, never()).getBlockEventCounts(anyInt(), anyInt());
        verify(eventService, never()).getResourceEventCounts(anyInt(), anyInt());
        verify(fileService, never()).getFiles(anyInt(), anyInt());
        verify(fileService, never()).getZipIds(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResultExperimentIdNull() {
        assertEquals(ERROR, resultController.getResult(null, ID_STRING, model).getViewName());
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(eventService, never()).getBlockEventCounts(anyInt(), anyInt());
        verify(eventService, never()).getResourceEventCounts(anyInt(), anyInt());
        verify(fileService, never()).getFiles(anyInt(), anyInt());
        verify(fileService, never()).getZipIds(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResultUserIdNull() {
        assertEquals(ERROR, resultController.getResult(ID_STRING, null, model).getViewName());
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(eventService, never()).getBlockEventCounts(anyInt(), anyInt());
        verify(eventService, never()).getResourceEventCounts(anyInt(), anyInt());
        verify(fileService, never()).getFiles(anyInt(), anyInt());
        verify(fileService, never()).getZipIds(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testDownloadFile() {
        when(fileService.findFile(ID)).thenReturn(fileDTO);
        Object responseEntity = resultController.downloadFile(ID_STRING);
        assertAll(
                () -> assertEquals(responseEntity.getClass(), ResponseEntity.class),
                () -> assertEquals(HttpStatus.OK, ((ResponseEntity<?>) responseEntity).getStatusCode()),
                () -> assertEquals(fileDTO.getContent(), ((ResponseEntity<?>) responseEntity).getBody())
        );
        verify(fileService).findFile(ID);
    }

    @Test
    public void testDownloadFileNotFound() {
        when(fileService.findFile(ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, resultController.downloadFile(ID_STRING));
        verify(fileService).findFile(ID);
    }

    @Test
    public void testDownloadFileInvalidId() {
        assertEquals(ERROR, resultController.downloadFile("-5"));
        verify(fileService, never()).findFile(anyInt());
    }

    @Test
    public void testDownloadFileIdNull() {
        assertEquals(ERROR, resultController.downloadFile(null));
        verify(fileService, never()).findFile(anyInt());
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
        when(experimentService.getSb3File(ID)).thenReturn(projection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(eventService.findJsonById(ID)).thenReturn(JSON);
        assertDoesNotThrow(
                () -> resultController.generateZipFile(ID_STRING, ID_STRING, ID_STRING, httpServletResponse)
        );
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).findJsonById(ID);
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
        when(experimentService.getSb3File(ID)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(eventService.findJsonById(ID)).thenReturn(JSON);
        assertDoesNotThrow(
                () -> resultController.generateZipFile(ID_STRING, ID_STRING, ID_STRING, httpServletResponse)
        );
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).findJsonById(ID);
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
        when(experimentService.getSb3File(ID)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(eventService.findJsonById(ID)).thenReturn(JSON);
        assertDoesNotThrow(
                () -> resultController.generateZipFile(ID_STRING, ID_STRING, ID_STRING, httpServletResponse)
        );
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).findJsonById(ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testGenerateZipFilesIO() throws IOException {
        when(experimentService.getSb3File(ID)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(eventService.findJsonById(ID)).thenReturn(JSON);
        when(httpServletResponse.getOutputStream()).thenThrow(IOException.class);
        assertThrows(RuntimeException.class,
                () -> resultController.generateZipFile(ID_STRING, ID_STRING, ID_STRING, httpServletResponse)
        );
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).findJsonById(ID);
        verify(httpServletResponse).getOutputStream();
    }

    @Test
    public void testGenerateZipFilesInvalidJsonId() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.generateZipFile(ID_STRING, ID_STRING, "0", httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).findJsonById(anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testGenerateZipFilesInvalidUserId() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.generateZipFile(ID_STRING, "-1", ID_STRING, httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).findJsonById(anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testGenerateZipFilesInvalidExperimentId() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.generateZipFile("ID_STRING", ID_STRING, ID_STRING, httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).findJsonById(anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testGenerateZipFilesJsonNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.generateZipFile(ID_STRING, ID_STRING, null, httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).findJsonById(anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testGenerateZipFilesUserNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.generateZipFile(ID_STRING, null, ID_STRING, httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).findJsonById(anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testGenerateZipFilesExperimentNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.generateZipFile(null, ID_STRING, ID_STRING, httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).findJsonById(anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadZip() {
        when(fileService.findZip(ID)).thenReturn(sb3ZipDTO);
        Object responseEntity = resultController.downloadZip(ID_STRING);
        assertAll(
                () -> assertEquals(responseEntity.getClass(), ResponseEntity.class),
                () -> assertEquals(HttpStatus.OK, ((ResponseEntity<?>) responseEntity).getStatusCode()),
                () -> assertEquals(sb3ZipDTO.getContent(), ((ResponseEntity<?>) responseEntity).getBody())
        );
        verify(fileService).findZip(ID);
    }

    @Test
    public void testDownloadZipNotFound() {
        when(fileService.findZip(ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, resultController.downloadZip(ID_STRING));
        verify(fileService).findZip(ID);
    }

    @Test
    public void testDownloadZipInvalidId() {
        assertEquals(ERROR, resultController.downloadZip("ab"));
        verify(fileService, never()).findZip(anyInt());
    }

    @Test
    public void testDownloadZipIdNull() {
        assertEquals(ERROR, resultController.downloadZip(null));
        verify(fileService, never()).findFile(anyInt());
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
                () -> resultController.downloadAllZips(ID_STRING, ID_STRING, httpServletResponse)
        );
        verify(fileService).getZipFiles(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadAllZipsIO() throws IOException {
        when(fileService.getZipFiles(ID, ID)).thenReturn(sb3ZipDTOs);
        when(httpServletResponse.getOutputStream()).thenThrow(IOException.class);
        assertThrows(RuntimeException.class,
                () -> resultController.downloadAllZips(ID_STRING, ID_STRING, httpServletResponse)
        );
        verify(fileService).getZipFiles(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadAllZipsInvalidExperimentId() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadAllZips("0", ID_STRING, httpServletResponse)
        );
        verify(fileService, never()).getZipFiles(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setStatus(anyInt());
    }

    @Test
    public void testDownloadAllZipsInvalidUserId() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadAllZips(ID_STRING, "-1", httpServletResponse)
        );
        verify(fileService, never()).getZipFiles(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setStatus(anyInt());
    }

    @Test
    public void testDownloadAllZipsUserIdNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadAllZips(ID_STRING, null, httpServletResponse)
        );
        verify(fileService, never()).getZipFiles(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setStatus(anyInt());
    }

    @Test
    public void testDownloadAllZipsExperimentIdNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadAllZips(null, ID_STRING, httpServletResponse)
        );
        verify(fileService, never()).getZipFiles(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setStatus(anyInt());
    }

    @Test
    public void testDownloadAllXmlFiles() throws IOException {
        when(eventService.getXMLForUser(ID, ID)).thenReturn(xmlProjections);
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
                () -> resultController.downloadAllXmlFiles(ID_STRING, ID_STRING, httpServletResponse)
        );
        verify(eventService).getXMLForUser(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadAllXmlFilesIO() throws IOException {
        when(httpServletResponse.getOutputStream()).thenThrow(IOException.class);
        assertThrows(RuntimeException.class,
                () -> resultController.downloadAllXmlFiles(ID_STRING, ID_STRING, httpServletResponse)
        );
        verify(eventService).getXMLForUser(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadAllXmlFilesInvalidExperimentId() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadAllXmlFiles(ID_STRING, "0", httpServletResponse)
        );
        verify(eventService, never()).getXMLForUser(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setHeader(anyString(), anyString());
        verify(httpServletResponse, never()).setStatus(anyInt());
    }

    @Test
    public void testDownloadAllXmlFilesInvalidUserId() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadAllXmlFiles("-1", ID_STRING, httpServletResponse)
        );
        verify(eventService, never()).getXMLForUser(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setHeader(anyString(), anyString());
        verify(httpServletResponse, never()).setStatus(anyInt());
    }

    @Test
    public void testDownloadAllXmlFilesExperimentIdNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadAllXmlFiles(ID_STRING, null, httpServletResponse)
        );
        verify(eventService, never()).getXMLForUser(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setHeader(anyString(), anyString());
        verify(httpServletResponse, never()).setStatus(anyInt());
    }

    @Test
    public void testDownloadAllXmlFilesUserIdNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadAllXmlFiles(null, ID_STRING, httpServletResponse)
        );
        verify(eventService, never()).getXMLForUser(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setHeader(anyString(), anyString());
        verify(httpServletResponse, never()).setStatus(anyInt());
    }

    @Test
    public void testDownloadAllJsonFiles() throws IOException {
        when(eventService.getJsonForUser(ID, ID)).thenReturn(jsonProjections);
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
                () -> resultController.downloadAllJsonFiles(ID_STRING, ID_STRING, httpServletResponse)
        );
        verify(eventService).getJsonForUser(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadAllJsonFilesIO() throws IOException {
        when(httpServletResponse.getOutputStream()).thenThrow(IOException.class);
        assertThrows(RuntimeException.class,
                () -> resultController.downloadAllJsonFiles(ID_STRING, ID_STRING, httpServletResponse)
        );
        verify(eventService).getJsonForUser(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadAllJsonFilesInvalidExperimentId() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadAllJsonFiles(ID_STRING, "0", httpServletResponse)
        );
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setHeader(anyString(), anyString());
        verify(httpServletResponse, never()).setStatus(anyInt());
    }

    @Test
    public void testDownloadAllJsonFilesInvalidUserId() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadAllJsonFiles("-1", ID_STRING, httpServletResponse)
        );
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setHeader(anyString(), anyString());
        verify(httpServletResponse, never()).setStatus(anyInt());
    }

    @Test
    public void testDownloadAllJsonFilesExperimentIdNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadAllJsonFiles(ID_STRING, null, httpServletResponse)
        );
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setHeader(anyString(), anyString());
        verify(httpServletResponse, never()).setStatus(anyInt());
    }

    @Test
    public void testDownloadAllJsonFilesUserIdNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadAllJsonFiles(null, ID_STRING, httpServletResponse)
        );
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
        verify(httpServletResponse, never()).setContentType(anyString());
        verify(httpServletResponse, never()).setHeader(anyString(), anyString());
        verify(httpServletResponse, never()).setStatus(anyInt());
    }

    @Test
    public void testGetCodes() {
        when(eventService.getCodesForUser(anyInt(), anyInt(),
                any(PageRequest.class))).thenReturn(blockEventProjections);
        List<BlockEventProjection> projections = resultController.getCodes(ID_STRING, ID_STRING, PAGE);
        assertAll(
                () -> assertEquals(2, projections.size()),
                () -> assertEquals(0, projections.get(0).getId()),
                () -> assertEquals("xml0", projections.get(0).getXml()),
                () -> assertEquals("code0", projections.get(0).getCode()),
                () -> assertEquals(1, projections.get(1).getId()),
                () -> assertEquals("xml1", projections.get(1).getXml()),
                () -> assertEquals("code1", projections.get(1).getCode())
        );
        verify(eventService).getCodesForUser(anyInt(), anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetCodesInvalidPage() {
        assertThrows(IncompleteDataException.class,
                () -> resultController.getCodes(ID_STRING, ID_STRING, "-1")
        );
        verify(eventService, never()).getCodesForUser(anyInt(), anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetCodesInvalidExperimentId() {
        assertThrows(IncompleteDataException.class,
                () -> resultController.getCodes(ID_STRING, PAGE, PAGE)
        );
        verify(eventService, never()).getCodesForUser(anyInt(), anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetCodesInvalidUserId() {
        assertThrows(IncompleteDataException.class,
                () -> resultController.getCodes("-1", ID_STRING, PAGE)
        );
        verify(eventService, never()).getCodesForUser(anyInt(), anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetCodesPageNull() {
        assertThrows(IncompleteDataException.class,
                () -> resultController.getCodes(ID_STRING, ID_STRING, null)
        );
        verify(eventService, never()).getCodesForUser(anyInt(), anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetCodesExperimentNull() {
        assertThrows(IncompleteDataException.class,
                () -> resultController.getCodes(ID_STRING, null, PAGE)
        );
        verify(eventService, never()).getCodesForUser(anyInt(), anyInt(), any(PageRequest.class));
    }

    @Test
    public void testGetCodesUserNull() {
        assertThrows(IncompleteDataException.class,
                () -> resultController.getCodes(null, ID_STRING, PAGE)
        );
        verify(eventService, never()).getCodesForUser(anyInt(), anyInt(), any(PageRequest.class));
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
        ExperimentProjection projection = new ExperimentProjection() {
            @Override
            public Integer getId() {
                return ID;
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
        when(experimentService.getSb3File(ID)).thenReturn(projection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(eventService.getJsonForUser(ID, ID)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(Optional.of(sb3ZipDTO));
        assertDoesNotThrow(
                () -> resultController.downloadSb3Files(ID_STRING, ID_STRING, null, null, null, null,
                        httpServletResponse)
        );
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).getJsonForUser(ID, ID);
        verify(fileService).findFinalProject(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadSb3FilesProjectionNull() throws IOException {
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
        when(experimentService.getSb3File(ID)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(eventService.getJsonForUser(ID, ID)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(Optional.of(sb3ZipDTO));
        assertDoesNotThrow(
                () -> resultController.downloadSb3Files(ID_STRING, ID_STRING, null, null, null, null,
                        httpServletResponse)
        );
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).getJsonForUser(ID, ID);
        verify(fileService).findFinalProject(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadSb3FilesFinalProjectEmpty() throws IOException {
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
        when(experimentService.getSb3File(ID)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(eventService.getJsonForUser(ID, ID)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(Optional.empty());
        assertDoesNotThrow(
                () -> resultController.downloadSb3Files(ID_STRING, ID_STRING, null, null, null, null,
                        httpServletResponse)
        );
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).getJsonForUser(ID, ID);
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
        ExperimentProjection projection = new ExperimentProjection() {
            @Override
            public Integer getId() {
                return ID;
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
        when(experimentService.getSb3File(ID)).thenReturn(projection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(eventService.getJsonForUser(ID, ID)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(Optional.of(sb3ZipDTO));
        assertDoesNotThrow(
                () -> resultController.downloadSb3Files(ID_STRING, ID_STRING, ID_STRING, null, null, null,
                        httpServletResponse)
        );
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).getJsonForUser(ID, ID);
        verify(fileService).findFinalProject(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadSb3FilesStepNoFinalProject() throws IOException {
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
        when(experimentService.getSb3File(ID)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(eventService.getJsonForUser(ID, ID)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(Optional.empty());
        assertDoesNotThrow(
                () -> resultController.downloadSb3Files(ID_STRING, ID_STRING, ID_STRING, null, null, null,
                        httpServletResponse)
        );
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).getJsonForUser(ID, ID);
        verify(fileService).findFinalProject(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadSb3FilesStepSingleJson() throws IOException {
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
        when(experimentService.getSb3File(ID)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(eventService.getJsonForUser(ID, ID)).thenReturn(getJsonProjections(1));
        when(fileService.findFinalProject(ID, ID)).thenReturn(Optional.of(sb3ZipDTO));
        assertDoesNotThrow(
                () -> resultController.downloadSb3Files(ID_STRING, ID_STRING, ID_STRING, null, null, null,
                        httpServletResponse)
        );
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).getJsonForUser(ID, ID);
        verify(fileService).findFinalProject(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadSb3FilesStartStop() throws IOException {
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
        when(experimentService.getSb3File(ID)).thenReturn(projection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(eventService.getJsonForUser(ID, ID)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(Optional.of(sb3ZipDTO));
        assertDoesNotThrow(
                () -> resultController.downloadSb3Files(ID_STRING, ID_STRING, null, ID_STRING, "3", "false",
                        httpServletResponse)
        );
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).getJsonForUser(ID, ID);
        verify(fileService).findFinalProject(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadSb3FilesStartStopEndPositionTooBig() throws IOException {
        when(experimentService.getSb3File(ID)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(eventService.getJsonForUser(ID, ID)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> resultController.downloadSb3Files(ID_STRING, ID_STRING, null, ID_STRING, "5", "true",
                        httpServletResponse)
        );
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).getJsonForUser(ID, ID);
        verify(fileService).findFinalProject(ID, ID);
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesStartStopStartBiggerEnd() throws IOException {
        assertThrows(IllegalArgumentException.class,
                () -> resultController.downloadSb3Files(ID_STRING, ID_STRING, null, "2", ID_STRING, "true",
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesStartStopStartInvalidStart() throws IOException {
        assertThrows(IllegalArgumentException.class,
                () -> resultController.downloadSb3Files(ID_STRING, ID_STRING, null, "abc", "3", "true",
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesStartStopStartInvalidEnd() throws IOException {
        assertThrows(IllegalArgumentException.class,
                () -> resultController.downloadSb3Files(ID_STRING, ID_STRING, null, ID_STRING, "0", "true",
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesIOException() throws IOException {
        when(httpServletResponse.getOutputStream()).thenThrow(IOException.class);
        when(experimentService.getSb3File(ID)).thenReturn(experimentProjection);
        when(fileService.getFileDTOs(ID, ID)).thenReturn(fileDTOS);
        when(eventService.getJsonForUser(ID, ID)).thenReturn(jsonProjections);
        when(fileService.findFinalProject(ID, ID)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> resultController.downloadSb3Files(ID_STRING, ID_STRING, null, null, null, null,
                        httpServletResponse)
        );
        verify(experimentService).getSb3File(ID);
        verify(fileService).getFileDTOs(ID, ID);
        verify(eventService).getJsonForUser(ID, ID);
        verify(fileService).findFinalProject(ID, ID);
        verify(httpServletResponse).getOutputStream();
        verify(httpServletResponse).setContentType("application/zip");
        verify(httpServletResponse).setHeader(anyString(), anyString());
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDownloadSb3FilesInvalidUserId() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadSb3Files(ID_STRING, "0", null, null, null, null,
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesInvalidExperimentId() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadSb3Files("ID_STRING", ID_STRING, null, null, null, null,
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesStepInvalid() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadSb3Files(ID_STRING, ID_STRING, "0", null, null, null,
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesUserNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadSb3Files(ID_STRING, null, null, null, null, null,
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesExperimentNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadSb3Files(null, ID_STRING, null, null, null, null,
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesStartAndStepNotNull() throws IOException {
        assertThrows(IllegalArgumentException.class,
                () -> resultController.downloadSb3Files(ID_STRING, ID_STRING, ID_STRING, ID_STRING, "3", "false",
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesIncludeNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadSb3Files(ID_STRING, ID_STRING, null, ID_STRING, "3", null,
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesEndNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadSb3Files(ID_STRING, ID_STRING, null, ID_STRING, null, "false",
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
    }

    @Test
    public void testDownloadSb3FilesStartNull() throws IOException {
        assertThrows(IncompleteDataException.class,
                () -> resultController.downloadSb3Files(ID_STRING, ID_STRING, null, null, "3", "false",
                        httpServletResponse)
        );
        verify(experimentService, never()).getSb3File(anyInt());
        verify(fileService, never()).getFileDTOs(anyInt(), anyInt());
        verify(eventService, never()).getJsonForUser(anyInt(), anyInt());
        verify(fileService, never()).findFinalProject(anyInt(), anyInt());
        verify(httpServletResponse, never()).getOutputStream();
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
                public Timestamp getDate() {
                    return Timestamp.valueOf(LocalDateTime.now().plusMinutes(id).minusSeconds(id));
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
            });
        }
        return projections;
    }
}
