package fim.unipassau.de.scratch1984.web;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.service.ExperimentService;
import fim.unipassau.de.scratch1984.web.controller.ExperimentController;
import fim.unipassau.de.scratch1984.web.dto.ExperimentDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.LocaleResolver;

import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExperimentControllerTest {

    @InjectMocks
    private ExperimentController experimentController;

    @Mock
    private ExperimentService experimentService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private ResourceBundle resourceBundle;

    @Mock
    private LocaleResolver localeResolver;

    private static final String TITLE = "My Experiment";
    private static final String DESCRIPTION = "A description";
    private static final String INFO = "Some info text";
    private static final String ERROR = "redirect:/error";
    private static final String EXPERIMENT = "experiment";
    private static final String EXPERIMENT_EDIT = "experiment-edit";
    private static final String SAVED_EXPERIMENT = "redirect:/experiment?id=";
    private static final String SUCCESS = "redirect:/?success=true";
    private static final String BLANK = "    ";
    private static final String EXPERIMENT_DTO = "experimentDTO";
    private static final String ID_STRING = "1";
    private static final String INVALID_ID = "-1";
    private static final int ID = 1;
    private final ExperimentDTO experimentDTO = new ExperimentDTO(ID, TITLE, DESCRIPTION, INFO, false);

    @BeforeEach
    public void setup() {
        experimentDTO.setId(ID);
        experimentDTO.setTitle(TITLE);
        experimentDTO.setDescription(DESCRIPTION);
        experimentDTO.setInfo(INFO);
    }

    @Test
    public void testGetExperiment() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        String returnString = experimentController.getExperiment(ID_STRING, model);
        assertEquals(EXPERIMENT, returnString);
        verify(experimentService).getExperiment(ID);
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        String returnString = experimentController.getExperiment(ID_STRING, model);
        assertEquals(ERROR, returnString);
        verify(experimentService).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentIdNull() {
        String returnString = experimentController.getExperiment(null, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentIdInvalid() {
        String returnString = experimentController.getExperiment(INVALID_ID, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentIdNotANumber() {
        String returnString = experimentController.getExperiment(BLANK, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentForm() {
        String returnString = experimentController.getExperimentForm(experimentDTO);
        assertEquals(EXPERIMENT_EDIT, returnString);
    }

    @Test
    public void testGetExperimentEditForm() {
        when(experimentService.getExperiment(ID)).thenReturn(experimentDTO);
        String returnString = experimentController.getEditExperimentForm(ID_STRING, model);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(experimentService).getExperiment(ID);
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentEditFormNotFound() {
        when(experimentService.getExperiment(ID)).thenThrow(NotFoundException.class);
        String returnString = experimentController.getEditExperimentForm(ID_STRING, model);
        assertEquals(ERROR, returnString);
        verify(experimentService).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testGetExperimentEditFormIdInvalid() {
        String returnString = experimentController.getEditExperimentForm(INVALID_ID, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).getExperiment(ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testEditExperiment() {
        when(experimentService.saveExperiment(experimentDTO)).thenReturn(experimentDTO);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(SAVED_EXPERIMENT + ID, returnString);
        verify(bindingResult, never()).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService).saveExperiment(experimentDTO);
    }

    @Test
    public void testEditExperimentTitleExists() {
        when(experimentService.existsExperiment(experimentDTO.getTitle(), experimentDTO.getId())).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).saveExperiment(any());
    }

    @Test
    public void testEditExperimentCreate() {
        experimentDTO.setId(null);
        when(experimentService.saveExperiment(experimentDTO)).thenReturn(experimentDTO);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(SAVED_EXPERIMENT + experimentDTO.getId(), returnString);
        verify(bindingResult, never()).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle());
        verify(experimentService).saveExperiment(experimentDTO);
    }

    @Test
    public void testEditExperimentCreateTitleExists() {
        experimentDTO.setId(null);
        when(experimentService.existsExperiment(experimentDTO.getTitle())).thenReturn(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle());
        verify(experimentService, never()).saveExperiment(any());
    }

    @Test
    public void testEditExperimentTitleTooLong() {
        experimentDTO.setTitle(createLongString(200).toString());
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).saveExperiment(any());
    }

    @Test
    public void testEditExperimentTitleAndDescriptionNull() {
        experimentDTO.setTitle(null);
        experimentDTO.setDescription(null);
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult, times(2)).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).saveExperiment(any());
    }

    @Test
    public void testEditExperimentTitleAndDescriptionBlank() {
        experimentDTO.setTitle(BLANK);
        experimentDTO.setDescription(BLANK);
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult, times(2)).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).saveExperiment(any());
    }

    @Test
    public void testEditExperimentDescriptionTooLong() {
        experimentDTO.setDescription(createLongString(2000).toString());
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).saveExperiment(any());
    }

    @Test
    public void testEditExperimentInfoTooLong() {
        experimentDTO.setInfo(createLongString(60000).toString());
        when(bindingResult.hasErrors()).thenReturn(true);
        String returnString = experimentController.editExperiment(experimentDTO, bindingResult);
        assertEquals(EXPERIMENT_EDIT, returnString);
        verify(bindingResult).addError(any());
        verify(experimentService).existsExperiment(experimentDTO.getTitle(), experimentDTO.getId());
        verify(experimentService, never()).saveExperiment(any());
    }

    @Test
    public void testDeleteExperiment() {
        String returnString = experimentController.deleteExperiment(ID_STRING);
        assertEquals(SUCCESS, returnString);
        verify(experimentService).deleteExperiment(ID);
    }

    @Test
    public void testDeleteExperimentInvalidId() {
        String returnString = experimentController.deleteExperiment(INVALID_ID);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).deleteExperiment(ID);
    }

    @Test
    public void testChangeExperimentStatusOpen() {
        when(experimentService.changeExperimentStatus(true, ID)).thenReturn(experimentDTO);
        String returnString = experimentController.changeExperimentStatus("open", ID_STRING, model);
        assertEquals(EXPERIMENT, returnString);
        verify(experimentService).changeExperimentStatus(true, ID);
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testChangeExperimentStatusClose() {
        when(experimentService.changeExperimentStatus(false, ID)).thenReturn(experimentDTO);
        String returnString = experimentController.changeExperimentStatus("close", ID_STRING, model);
        assertEquals(EXPERIMENT, returnString);
        verify(experimentService).changeExperimentStatus(false, ID);
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testChangeExperimentStatusCloseInfoNull() {
        experimentDTO.setInfo(null);
        when(experimentService.changeExperimentStatus(false, ID)).thenReturn(experimentDTO);
        String returnString = experimentController.changeExperimentStatus("close", ID_STRING, model);
        assertEquals(EXPERIMENT, returnString);
        verify(experimentService).changeExperimentStatus(false, ID);
        verify(model).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testChangeExperimentStatusInvalid() {
        String returnString = experimentController.changeExperimentStatus("blabla", ID_STRING, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).changeExperimentStatus(anyBoolean(), anyInt());
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testChangeExperimentStatusOpenNotFound() {
        when(experimentService.changeExperimentStatus(true, ID)).thenThrow(NotFoundException.class);
        String returnString = experimentController.changeExperimentStatus("open", ID_STRING, model);
        assertEquals(ERROR, returnString);
        verify(experimentService).changeExperimentStatus(true, ID);
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testChangeExperimentStatusStatusNull() {
        String returnString = experimentController.changeExperimentStatus(null, ID_STRING, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).changeExperimentStatus(anyBoolean(), anyInt());
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    @Test
    public void testChangeExperimentStatusIdNull() {
        String returnString = experimentController.changeExperimentStatus("open", null, model);
        assertEquals(ERROR, returnString);
        verify(experimentService, never()).changeExperimentStatus(anyBoolean(), anyInt());
        verify(model, never()).addAttribute(EXPERIMENT_DTO, experimentDTO);
    }

    private StringBuilder createLongString(int length) {
        StringBuilder longString = new StringBuilder();
        longString.append("a".repeat(Math.max(0, length)));
        return longString;
    }
}
