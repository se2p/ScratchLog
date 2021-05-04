package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.ResourceEvent;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * A repository providing functionality for retrieving the resource event data.
 */
public interface ResourceEventRepository extends JpaRepository<ResourceEvent, Integer> {
}
