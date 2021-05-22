package fim.unipassau.de.scratch1984.persistence;

import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class ExperimentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExperimentRepository repository;

    private Experiment experiment1 = new Experiment(null, "Experiment 1", "Description for experiment 1", "Some info", false);
    private Experiment experiment2 = new Experiment(null, "Experiment 2", "Description for experiment 2", "Some info", true);
    private Experiment experiment3 = new Experiment(null, "Experiment 3", "Description for experiment 3", "Some info", false);

    @BeforeEach
    public void setup() {
        experiment1 = entityManager.persist(experiment1);
        experiment2 = entityManager.persist(experiment2);
        experiment3 = entityManager.persist(experiment3);
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
}
