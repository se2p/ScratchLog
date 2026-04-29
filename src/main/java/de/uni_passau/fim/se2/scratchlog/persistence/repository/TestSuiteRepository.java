package de.uni_passau.fim.se2.scratchlog.persistence.repository;

import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestSuite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestSuiteRepository extends JpaRepository<TestSuite, Integer> {

    Optional<TestSuite> getByExperiment_Id(int experimentId);

    void deleteByExperiment_Id(int experimentId);

}
