package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.EventService;
import fim.unipassau.de.scratch1984.application.service.FileService;
import fim.unipassau.de.scratch1984.application.service.UserService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        verify(model, times(4)).addAttribute(anyString(), any());
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
}
