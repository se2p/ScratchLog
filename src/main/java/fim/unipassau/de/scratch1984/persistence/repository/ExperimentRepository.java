package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentProjection;
import fim.unipassau.de.scratch1984.persistence.projection.ExperimentTableProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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
     * Checks, whether an experiment with the given id and project not null exists in the database.
     *
     * @param id The id to search for.
     * @return {@code true} iff such an experiment exists.
     */
    boolean existsByIdAndProjectIsNotNull(int id);

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
     * Returns the experiment projection identified by the given id, if one exists.
     *
     * @param id The id to search for.
     * @return An {@link Optional} containing the data, if it exists.
     */
    Optional<ExperimentProjection> findExperimentById(int id);

    /**
     * Returns a page of experiments corresponding to the parameters set in the pageable.
     *
     * @param pageable The pageable to use.
     * @return A new experiment page.
     */
    Page<ExperimentTableProjection> findAllProjectedBy(Pageable pageable);

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

    /**
     * Returns a list of the first five experiments whose title contains the given query value.
     *
     * @param query The title to search for.
     * @return A list of {@link ExperimentTableProjection}s.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM experiment AS e WHERE e.title LIKE CONCAT('%', :query, '%')"
            + " LIMIT 5")
    List<ExperimentTableProjection> findExperimentSuggestions(@Param("query") String query);

    /**
     * Returns a list of at most as many experiments as the given limit with the given offset whose title contains the
     * given query value.
     *
     * @param query The title to search for.
     * @param limit The maximum amount of results to be returned.
     * @param offset The offset used to return new results.
     * @return A list of {@link ExperimentTableProjection}s.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM experiment AS e WHERE e.title LIKE CONCAT('%', :query, '%')"
            + " LIMIT :limit OFFSET :offset")
    List<ExperimentTableProjection> findExperimentResults(@Param("query") String query, @Param("limit") int limit,
                                                          @Param("offset") int offset);

    /**
     * Returns the number of experiments whose title contains the given query value.
     *
     * @param query The title to search for.
     * @return The number of experiment results.
     */
    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM experiment AS e WHERE e.title LIKE"
            + " CONCAT('%', :query, '%')")
    int getExperimentResultsCount(@Param("query") String query);

    /**
     * Returns a page of experiments in which the user with the given id is participating in.
     *
     * @param userId The user id to search for.
     * @param pageable The pageable to use.
     * @return A page of {@link ExperimentTableProjection}s.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM experiment AS e INNER JOIN participant AS p ON p.experiment_id"
            + " = e.id WHERE p.user_id = :participant")
    Page<ExperimentTableProjection> findExperimentsByParticipant(@Param("participant") int userId, Pageable pageable);

    /**
     * Returns the number of experiment pages for the given participant.
     *
     * @param userId The user id to search for.
     * @return The page count.
     */
    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM experiment AS e INNER JOIN participant AS p ON"
            + " p.experiment_id = e.id WHERE p.user_id = :participant")
    int getParticipantPageCount(@Param("participant") int userId);

}
