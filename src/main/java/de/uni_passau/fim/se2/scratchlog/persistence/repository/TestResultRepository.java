package de.uni_passau.fim.se2.scratchlog.persistence.repository;

import de.uni_passau.fim.se2.scratchlog.persistence.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface TestResultRepository extends JpaRepository<TestResult, Integer> {

    @Modifying
    @Transactional
    void deleteAllByProject_Id(int projectId);

}
