package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.exception.IncompleteDataException;
import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.EventService;
import fim.unipassau.de.scratch1984.application.service.FileService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventJSONProjection;
import fim.unipassau.de.scratch1984.persistence.projection.BlockEventXMLProjection;
import fim.unipassau.de.scratch1984.persistence.projection.FileProjection;
import fim.unipassau.de.scratch1984.web.controller.ResultController;
import fim.unipassau.de.scratch1984.web.dto.EventCountDTO;
import fim.unipassau.de.scratch1984.web.dto.FileDTO;
import fim.unipassau.de.scratch1984.web.dto.Sb3ZipDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private static final int ID = 1;
    private final FileDTO fileDTO = new FileDTO(ID, ID, LocalDateTime.now(), "file", "type",
            new byte[]{1, 2, 3});
    private final Sb3ZipDTO sb3ZipDTO = new Sb3ZipDTO(ID, ID, LocalDateTime.now(), "file", new byte[]{1, 2, 3});
    private final List<EventCountDTO> blockEvents = getEventCounts(5, "CREATE");
    private final List<EventCountDTO> resourceEvents = getEventCounts(2, "RENAME");
    private final List<FileProjection> files = getFileProjections(7);
    private final List<Integer> zips = Arrays.asList(1, 4, 10, 18);
    private final List<Sb3ZipDTO> sb3ZipDTOs = getSb3ZipDTOs(6);
    private final List<BlockEventXMLProjection> xmlProjections = getXmlProjections(3);
    private final List<BlockEventJSONProjection> jsonProjections = getJsonProjections(4);

    @Test
    public void testGetResult() {
        when(userService.existsParticipant(ID, ID)).thenReturn(true);
        when(eventService.getBlockEventCounts(ID, ID)).thenReturn(blockEvents);
        when(eventService.getResourceEventCounts(ID, ID)).thenReturn(resourceEvents);
        when(fileService.getFiles(ID, ID)).thenReturn(files);
        when(fileService.getZipIds(ID, ID)).thenReturn(zips);
        assertEquals(RESULT, resultController.getResult(ID_STRING, ID_STRING, model));
        verify(userService).existsParticipant(ID, ID);
        verify(eventService).getBlockEventCounts(ID, ID);
        verify(eventService).getResourceEventCounts(ID, ID);
        verify(fileService).getFiles(ID, ID);
        verify(fileService).getZipIds(ID, ID);
        verify(model, times(7)).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResultNotFound() {
        when(userService.existsParticipant(ID, ID)).thenReturn(true);
        when(eventService.getBlockEventCounts(ID, ID)).thenReturn(blockEvents);
        when(eventService.getResourceEventCounts(ID, ID)).thenReturn(resourceEvents);
        when(fileService.getFiles(ID, ID)).thenThrow(NotFoundException.class);
        assertEquals(ERROR, resultController.getResult(ID_STRING, ID_STRING, model));
        verify(userService).existsParticipant(ID, ID);
        verify(eventService).getBlockEventCounts(ID, ID);
        verify(eventService).getResourceEventCounts(ID, ID);
        verify(fileService).getFiles(ID, ID);
        verify(fileService, never()).getZipIds(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResultNoParticipant() {
        assertEquals(ERROR, resultController.getResult(ID_STRING, ID_STRING, model));
        verify(userService).existsParticipant(ID, ID);
        verify(eventService, never()).getBlockEventCounts(anyInt(), anyInt());
        verify(eventService, never()).getResourceEventCounts(anyInt(), anyInt());
        verify(fileService, never()).getFiles(anyInt(), anyInt());
        verify(fileService, never()).getZipIds(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResultInvalidExperimentId() {
        assertEquals(ERROR, resultController.getResult("0", ID_STRING, model));
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(eventService, never()).getBlockEventCounts(anyInt(), anyInt());
        verify(eventService, never()).getResourceEventCounts(anyInt(), anyInt());
        verify(fileService, never()).getFiles(anyInt(), anyInt());
        verify(fileService, never()).getZipIds(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResultInvalidUserId() {
        assertEquals(ERROR, resultController.getResult(ID_STRING, "  ", model));
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(eventService, never()).getBlockEventCounts(anyInt(), anyInt());
        verify(eventService, never()).getResourceEventCounts(anyInt(), anyInt());
        verify(fileService, never()).getFiles(anyInt(), anyInt());
        verify(fileService, never()).getZipIds(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResultExperimentIdNull() {
        assertEquals(ERROR, resultController.getResult(null, ID_STRING, model));
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(eventService, never()).getBlockEventCounts(anyInt(), anyInt());
        verify(eventService, never()).getResourceEventCounts(anyInt(), anyInt());
        verify(fileService, never()).getFiles(anyInt(), anyInt());
        verify(fileService, never()).getZipIds(anyInt(), anyInt());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    public void testGetResultUserIdNull() {
        assertEquals(ERROR, resultController.getResult(ID_STRING, null, model));
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
            });
        }
        return projections;
    }
}
