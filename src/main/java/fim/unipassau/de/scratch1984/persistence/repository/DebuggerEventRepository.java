package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.DebuggerEvent;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * A repository providing functionality for retrieving the debugger event data.
 */
public interface DebuggerEventRepository extends JpaRepository<DebuggerEvent, Integer> {
}
