package fim.unipassau.de.scratch1984.persistence;

import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentSearchProjection;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

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
    private static final int LIMIT = 5;
    private Experiment experiment1 = new Experiment(null, "Experiment 1", "Description for experiment 1", "Some info",
            "Some postscript", false);
    private Experiment experiment2 = new Experiment(null, "Experiment 2", "Description for experiment 2", "Some info",
            "Some postscript", true);
    private Experiment experiment3 = new Experiment(null, "Experiment 3", "Description for experiment 3", "Some info",
            "Some postscript", false);
    private Experiment experiment4 = new Experiment(null, "Experiment 4", "Description for experiment 1", "Some info",
            "Some postscript", false);
    private Experiment experiment5 = new Experiment(null, "Exp 5", "Description for experiment 2", "Some info",
            "Some postscript", false);
    private Experiment experiment6 = new Experiment(null, "Exp 6", "Description for experiment 3", "Some info",
            "Some postscript", false);

    @BeforeEach
    public void setup() {
        experiment1 = entityManager.persist(experiment1);
        experiment2 = entityManager.persist(experiment2);
        experiment3 = entityManager.persist(experiment3);
        experiment4 = entityManager.persist(experiment4);
        experiment5 = entityManager.persist(experiment5);
        experiment6 = entityManager.persist(experiment6);
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
    public void testFindAllByActive() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Experiment> active = repository.findAllByActive(true, pageable);
        Page<Experiment> inactive = repository.findAllByActive(false, pageable);
        assertAll(
                () -> assertEquals(1, active.getNumberOfElements()),
                () -> assertEquals(5, inactive.getNumberOfElements())
        );
    }

    @Test
    public void testFindExperimentSuggestions() {
        List<ExperimentSearchProjection> experiments = repository.findExperimentSuggestions(SHORT_QUERY);
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
        List<ExperimentSearchProjection> experiments = repository.findExperimentSuggestions(TITLE_QUERY);
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
        List<ExperimentSearchProjection> experiments = repository.findExperimentSuggestions(NO_RESULTS);
        assertTrue(experiments.isEmpty());
    }

    @Test
    public void testFindExperimentResults() {
        List<ExperimentSearchProjection> experiments = repository.findExperimentResults(SHORT_QUERY, LIMIT);
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
    public void testFindExperimentResultsAll() {
        List<ExperimentSearchProjection> experiments = repository.findExperimentResults(SHORT_QUERY,
                Constants.PAGE_SIZE);
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
}
