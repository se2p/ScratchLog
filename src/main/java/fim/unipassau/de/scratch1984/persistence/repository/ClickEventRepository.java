package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * A repository providing functionality for retrieving the click event data.
 */
public interface ClickEventRepository extends JpaRepository<ClickEvent, Integer> {
}
