package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.QuestionEvent;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * A repository providing functionality for retrieving the question event data.
 */
public interface QuestionEventRepository extends JpaRepository<QuestionEvent, Integer> {
}
