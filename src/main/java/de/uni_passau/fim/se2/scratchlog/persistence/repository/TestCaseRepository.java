package de.uni_passau.fim.se2.scratchlog.persistence.repository;

import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TestCaseRepository extends JpaRepository<TestCase, Integer> {

    @Query("""
            select distinct tr.testCase
            from TestResult tr
            where tr.testCase.testSuite.experiment.id = :experimentId
            order by tr.testCase.name
            """)
    List<TestCase> findTestCasesUsedInExperiment(int experimentId);

}
