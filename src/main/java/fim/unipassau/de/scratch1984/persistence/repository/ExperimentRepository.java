package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * A repository providing functionality for retrieving experiment data.
 */
public interface ExperimentRepository extends JpaRepository<Experiment, Integer> {

    /**
     * Checks, whether an experiment with the given title already exists in the database.
     *
     * @param title The title to search for.
     * @return {@code true} iff the name already exists.
     */
    boolean existsByTitle(String title);

    /**
     * Checks, whether an experiment with the given id already exists in the database.
     *
     * @param id The id to search for.
     * @return {@code true} iff the id already exists.
     */
    boolean existsById(int id);

    /**
     * Returns the experiment identified by the given title, if one exists.
     *
     * @param title The title to search for.
     * @return The experiment data or {@code null}, if no entry could be found.
     */
    Experiment findByTitle(String title);

    /**
     * Returns the experiment identified by the given id, if one exists.
     *
     * @param id The id to search for.
     * @return The experiment data or {@code null}, if no entry could be found.
     */
    Experiment findById(int id);

    /**
     * Returns a page of experiments corresponding to the parameters set in the pageable.
     *
     * @param pageable The pageable to use.
     * @return An new experiment page.
     */
    Page<Experiment> findAll(Pageable pageable);

    /**
     * Returns a page of experiments with the given active status corresponding to the parameters set in the pageable.
     *
     * @param active The active status of the experiment.
     * @param pageable The pageable to use.
     * @return An new experiment page.
     */
    Page<Experiment> findAllByActive(boolean active, Pageable pageable);

    /**
     * Deletes the experiment with the given id from the database, if existent.
     *
     * @param id The experiment id.
     */
    void deleteById(int id);

    /**
     * Updates the active status of the experiment with the given id to the given value.
     *
     * @param id The experiment id.
     * @param change The new status.
     */
    @Modifying
    @Query("UPDATE Experiment e SET e.active = :change WHERE e.id = :id")
    void updateStatusById(@Param("id") int id, @Param("change") boolean change);

}
