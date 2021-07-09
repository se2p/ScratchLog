package fim.unipassau.de.scratch1984.integration;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.EventService;
import fim.unipassau.de.scratch1984.application.service.FileService;
import fim.unipassau.de.scratch1984.application.service.UserService;
import fim.unipassau.de.scratch1984.persistence.projection.FileProjection;
import fim.unipassau.de.scratch1984.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratch1984.web.controller.ResultController;
import fim.unipassau.de.scratch1984.web.dto.EventCountDTO;
import fim.unipassau.de.scratch1984.web.dto.FileDTO;
import fim.unipassau.de.scratch1984.web.dto.Sb3ZipDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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
    private EventService eventService;

    @MockBean
    private FileService fileService;

    private static final String RESULT = "result";
    private static final String ERROR = "redirect:/error";
    private static final String ID_STRING = "1";
    private static final String EXPERIMENT_PARAM = "experiment";
    private static final String USER_PARAM = "user";
    private static final String ID_PARAM = "id";
    private static final int ID = 1;
    private final FileDTO fileDTO = new FileDTO(ID, ID, LocalDateTime.now(), "file", "type",
            new byte[]{1, 2, 3});
    private final Sb3ZipDTO sb3ZipDTO = new Sb3ZipDTO(ID, ID, LocalDateTime.now(), "file", new byte[]{1, 2, 3});
    private final List<EventCountDTO> blockEvents = getEventCounts(5, "CREATE");
    private final List<EventCountDTO> resourceEvents = getEventCounts(2, "RENAME");
    private final List<FileProjection> files = getFileProjections(7);
    private final List<Integer> zips = Arrays.asList(1, 4, 10, 18);
    private final List<Sb3ZipDTO> sb3ZipDTOs = getSb3ZipDTOs(6);
    private final String TOKEN_ATTR_NAME = "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN";
    private final HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository = new HttpSessionCsrfTokenRepository();
    private final CsrfToken csrfToken = httpSessionCsrfTokenRepository.generateToken(new MockHttpServletRequest());

    @Test
    public void testGetResult() throws Exception {
        when(userService.existsParticipant(ID, ID)).thenReturn(true);
        when(eventService.getBlockEventCounts(ID, ID)).thenReturn(blockEvents);
        when(eventService.getResourceEventCounts(ID, ID)).thenReturn(resourceEvents);
        when(fileService.getFiles(ID, ID)).thenReturn(files);
        when(fileService.getZipIds(ID, ID)).thenReturn(zips);
        mvc.perform(get("/result")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute("blockEvents", is(blockEvents)))
                .andExpect(model().attribute("resourceEvents", is(resourceEvents)))
                .andExpect(model().attribute("files", is(files)))
                .andExpect(model().attribute("zips", is(zips)))
                .andExpect(model().attribute("user", is(ID)))
                .andExpect(model().attribute("experiment", is(ID)))
                .andExpect(status().isOk())
                .andExpect(view().name(RESULT));
        verify(userService).existsParticipant(ID, ID);
        verify(eventService).getBlockEventCounts(ID, ID);
        verify(eventService).getResourceEventCounts(ID, ID);
        verify(fileService).getFiles(ID, ID);
        verify(fileService).getZipIds(ID, ID);
    }

    @Test
    public void testGetResultNotFound() throws Exception {
        when(userService.existsParticipant(ID, ID)).thenReturn(true);
        when(eventService.getBlockEventCounts(ID, ID)).thenReturn(blockEvents);
        when(eventService.getResourceEventCounts(ID, ID)).thenReturn(resourceEvents);
        when(fileService.getFiles(ID, ID)).thenReturn(files);
        when(fileService.getZipIds(ID, ID)).thenThrow(NotFoundException.class);
        mvc.perform(get("/result")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute("blockEvents", nullValue()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).existsParticipant(ID, ID);
        verify(eventService).getBlockEventCounts(ID, ID);
        verify(eventService).getResourceEventCounts(ID, ID);
        verify(fileService).getFiles(ID, ID);
        verify(fileService).getZipIds(ID, ID);
    }

    @Test
    public void testGetResultNoParticipant() throws Exception {
        mvc.perform(get("/result")
                .param(EXPERIMENT_PARAM, ID_STRING)
                .param(USER_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute("blockEvents", nullValue()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService).existsParticipant(ID, ID);
        verify(eventService, never()).getBlockEventCounts(anyInt(), anyInt());
        verify(eventService, never()).getResourceEventCounts(anyInt(), anyInt());
        verify(fileService, never()).getFiles(anyInt(), anyInt());
        verify(fileService, never()).getZipIds(anyInt(), anyInt());
    }

    @Test
    public void testGetResultInvalidId() throws Exception {
        mvc.perform(get("/result")
                .param(EXPERIMENT_PARAM, "0")
                .param(USER_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(model().attribute("blockEvents", nullValue()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(userService, never()).existsParticipant(anyInt(), anyInt());
        verify(eventService, never()).getBlockEventCounts(anyInt(), anyInt());
        verify(eventService, never()).getResourceEventCounts(anyInt(), anyInt());
        verify(fileService, never()).getFiles(anyInt(), anyInt());
        verify(fileService, never()).getZipIds(anyInt(), anyInt());
    }

    @Test
    public void testDownloadFile() throws Exception {
        when(fileService.findFile(ID)).thenReturn(fileDTO);
        mvc.perform(get("/result/file")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
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
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
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
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(ERROR));
        verify(fileService, never()).findFile(anyInt());
    }

    @Test
    public void testDownloadZip() throws Exception {
        when(fileService.findZip(ID)).thenReturn(sb3ZipDTO);
        mvc.perform(get("/result/zip")
                .param(ID_PARAM, ID_STRING)
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
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
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
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
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
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
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
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
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
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
                .sessionAttr(TOKEN_ATTR_NAME, csrfToken)
                .param(csrfToken.getParameterName(), csrfToken.getToken())
                .contentType(MediaType.ALL)
                .accept(MediaType.ALL))
                .andExpect(status().isBadRequest());
        verify(fileService, never()).getZipFiles(anyInt(), anyInt());
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
}
