package de.uni_passau.fim.se2.scratchlog.persistence.repository;

import de.uni_passau.fim.se2.scratchlog.persistence.entity.ExampleSolution;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExampleSolutionRepository extends JpaRepository<ExampleSolution, Integer> {

    @Transactional
    void deleteExampleSolutionByExperiment_Id(int experimentId);

    List<ExampleSolution> findExampleSolutionsByExperiment_Id(int experimentId);
}
