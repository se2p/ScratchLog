package fim.unipassau.de.scratch1984.persistence;

import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentTableProjection;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class ExperimentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExperimentRepository repository;

    private static final String SHORT_QUERY = "Exp";
    private static final String TITLE_QUERY = "Experiment";
    private static final String NO_RESULTS = "description";
    private static final String GUI_URL = "scratch";
    private static final int LIMIT = 5;
    private PageRequest pageRequest;
    private User user = new User("user", "email", "PARTICIPANT", "ENGLISH", "password", "secret");
    private Experiment experiment1 = new Experiment(null, "Experiment 1", "Description for experiment 1", "Some info",
            "Some postscript", false, GUI_URL);
    private Experiment experiment2 = new Experiment(null, "Experiment 2", "Description for experiment 2", "Some info",
            "Some postscript", true, GUI_URL);
    private Experiment experiment3 = new Experiment(null, "Experiment 3", "Description for experiment 3", "Some info",
            "Some postscript", false, GUI_URL);
    private Experiment experiment4 = new Experiment(null, "Experiment 4", "Description for experiment 1", "Some info",
            "Some postscript", false, GUI_URL);
    private Experiment experiment5 = new Experiment(null, "Exp 5", "Description for experiment 2", "Some info",
            "Some postscript", false, GUI_URL);
    private Experiment experiment6 = new Experiment(null, "Exp 6", "Description for experiment 3", "Some info",
            "Some postscript", false, GUI_URL);
    private Participant participant1 = new Participant(user, experiment1, null, null);
    private Participant participant2 = new Participant(user, experiment2, null, null);
    private Participant participant3 = new Participant(user, experiment3, null, null);
    private Participant participant4 = new Participant(user, experiment4, null, null);

    @BeforeEach
    public void setup() {
        pageRequest = PageRequest.of(0, Constants.PAGE_SIZE);
        user = entityManager.persist(user);
        experiment1 = entityManager.persist(experiment1);
        experiment2 = entityManager.persist(experiment2);
        experiment3 = entityManager.persist(experiment3);
        experiment4 = entityManager.persist(experiment4);
        experiment5 = entityManager.persist(experiment5);
        experiment6 = entityManager.persist(experiment6);
        participant1 = entityManager.persist(participant1);
        participant2 = entityManager.persist(participant2);
        participant3 = entityManager.persist(participant3);
        participant4 = entityManager.persist(participant4);
    }

    @Test
    public void testUpdateStatusById() {
        repository.updateStatusById(experiment1.getId(), true);
        entityManager.refresh(experiment1);
        assertTrue(experiment1.isActive());
    }

    @Test
    public void testUpdateStatusByIdFalse() {
        repository.updateStatusById(experiment2.getId(), false);
        entityManager.refresh(experiment2);
        assertFalse(experiment2.isActive());
    }

    @Test
    public void testFindExperimentSuggestions() {
        List<ExperimentTableProjection> experiments = repository.findExperimentSuggestions(SHORT_QUERY);
        assertAll(
                () -> assertEquals(5, experiments.size()),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment1.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment2.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment3.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment4.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment5.getTitle())))
        );
    }

    @Test
    public void testFindExperimentSuggestionsLessThan5() {
        List<ExperimentTableProjection> experiments = repository.findExperimentSuggestions(TITLE_QUERY);
        assertAll(
                () -> assertEquals(4, experiments.size()),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment1.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment2.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment3.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment4.getTitle()))),
                () -> assertFalse(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment5.getTitle())))
        );
    }

    @Test
    public void testFindExperimentSuggestionsNoResults() {
        List<ExperimentTableProjection> experiments = repository.findExperimentSuggestions(NO_RESULTS);
        assertTrue(experiments.isEmpty());
    }

    @Test
    public void testFindExperimentResults() {
        List<ExperimentTableProjection> experiments = repository.findExperimentResults(SHORT_QUERY, LIMIT, 0);
        assertAll(
                () -> assertEquals(5, experiments.size()),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment1.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment2.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment3.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment4.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment5.getTitle())))
        );
    }

    @Test
    public void testFindExperimentResultsOffset() {
        List<ExperimentTableProjection> experiments = repository.findExperimentResults(SHORT_QUERY, LIMIT, 2);
        assertAll(
                () -> assertEquals(4, experiments.size()),
                () -> assertFalse(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment1.getTitle()))),
                () -> assertFalse(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment2.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment3.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment4.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment5.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment6.getTitle())))
        );
    }

    @Test
    public void testFindExperimentResultsAll() {
        List<ExperimentTableProjection> experiments = repository.findExperimentResults(SHORT_QUERY,
                Constants.PAGE_SIZE, 0);
        assertAll(
                () -> assertEquals(6, experiments.size()),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment1.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment2.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment3.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment4.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment5.getTitle()))),
                () -> assertTrue(experiments.stream().anyMatch(experiment
                        -> experiment.getTitle().equals(experiment6.getTitle())))
        );
    }

    @Test
    public void testFindExperimentResultsNoResults() {
        assertTrue(repository.findExperimentSuggestions(NO_RESULTS).isEmpty());
    }

    @Test
    public void testGetExperimentResultCount() {
        assertEquals(6, repository.getExperimentResultsCount(SHORT_QUERY));
    }

    @Test
    public void testGetExperimentResultCount4() {
        assertEquals(4, repository.getExperimentResultsCount(TITLE_QUERY));
    }

    @Test
    public void testGetExperimentResultCountZero() {
        assertEquals(0, repository.getExperimentResultsCount(NO_RESULTS));
    }

    @Test
    public void testFindExperimentsByParticipant() {
        Page<ExperimentTableProjection> projections = repository.findExperimentsByParticipant(user.getId(),
                pageRequest);
        assertAll(
                () -> assertEquals(4, projections.getNumberOfElements()),
                () -> assertTrue(projections.stream().anyMatch(e -> Objects.equals(e.getId(), experiment1.getId()))),
                () -> assertTrue(projections.stream().anyMatch(e -> Objects.equals(e.getId(), experiment2.getId()))),
                () -> assertTrue(projections.stream().anyMatch(e -> Objects.equals(e.getId(), experiment3.getId()))),
                () -> assertTrue(projections.stream().anyMatch(e -> Objects.equals(e.getId(), experiment4.getId())))
        );
    }

    @Test
    public void testFindExperimentsByParticipantPageSizeTooSmall() {
        pageRequest = PageRequest.of(0, 3);
        Page<ExperimentTableProjection> projections = repository.findExperimentsByParticipant(user.getId(),
                pageRequest);
        assertAll(
                () -> assertEquals(3, projections.getNumberOfElements()),
                () -> assertTrue(projections.stream().anyMatch(e -> Objects.equals(e.getId(), experiment1.getId()))),
                () -> assertTrue(projections.stream().anyMatch(e -> Objects.equals(e.getId(), experiment2.getId()))),
                () -> assertTrue(projections.stream().anyMatch(e -> Objects.equals(e.getId(), experiment3.getId()))),
                () -> assertFalse(projections.stream().anyMatch(e -> Objects.equals(e.getId(), experiment4.getId())))
        );
    }

    @Test
    public void testFindExperimentsByParticipantNoUser() {
        Page<ExperimentTableProjection> projections = repository.findExperimentsByParticipant(5, pageRequest);
        assertEquals(0, projections.getNumberOfElements());
    }

    @Test
    public void testGetParticipantPageCount() {
        assertEquals(4, repository.getParticipantPageCount(user.getId()));
    }

    @Test
    public void testGetParticipantPageCountZero() {
        assertEquals(0, repository.getParticipantPageCount(5));
    }
}
