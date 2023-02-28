package fim.unipassau.de.scratch1984.integration;

import fim.unipassau.de.scratch1984.application.service.PageService;
import fim.unipassau.de.scratch1984.spring.configuration.SecurityTestConfig;
import fim.unipassau.de.scratch1984.web.controller.PageRestController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PageRestController.class)
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class PageRestControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PageService pageService;

    private final String ID_PARAM = "id";
    private final String ID_STRING = "1";
    private final String PAGE_STRING = "2";
    private final int ID = 1;
    private final int LAST_PAGE = 3;

    @Test
    public void testGetLastCourseParticipantPage() throws Exception {
        when(pageService.getLastParticipantCoursePage(ID)).thenReturn(LAST_PAGE);
        mvc.perform(get("/page/course/participant")
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(content().string(PAGE_STRING));
        verify(pageService).getLastParticipantCoursePage(ID);
    }

    @Test
    public void testGetLastCourseExperimentPage() throws Exception {
        when(pageService.getLastCourseExperimentPage(ID)).thenReturn(LAST_PAGE);
        mvc.perform(get("/page/course/experiment")
                        .param(ID_PARAM, ID_STRING)
                        .contentType(MediaType.ALL)
                        .accept(MediaType.ALL))
                .andExpect(status().isOk())
                .andExpect(content().string(PAGE_STRING));
        verify(pageService).getLastCourseExperimentPage(ID);
    }

}
