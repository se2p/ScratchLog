package de.uni_passau.fim.se2.scratchlog.persistence.repository;

import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestResult;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.TestCaseCountSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface TestResultRepository extends JpaRepository<TestResult, Integer> {

    @Modifying
    @Transactional
    void deleteAllByProject_Id(int projectId);

    /**
     * Counts how many test case results exist for the given block events.
     *
     * @param blockEventIds Some block events.
     * @return The number of test case results grouped by block event and kind of result.
     */
    @Query("""
            select new de.uni_passau.fim.se2.scratchlog.persistence.projection.TestCaseCountSummary(
                t.project.id,
                t.result,
                count(*)
            )
            from TestResult t
            where t.project.id in :blockEventIds
            group by t.project.id, t.result
            """)
    List<TestCaseCountSummary> getTestResultSummaries(Set<Integer> blockEventIds);

}
