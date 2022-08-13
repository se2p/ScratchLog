package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.File;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.projection.FileProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * A repository providing functionality for retrieving file data.
 */
public interface FileRepository extends JpaRepository<File, Integer> {

    /**
     * Returns the ids and names of all files uploaded by the given user during the given experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return A list of files that is empty if no entry could be found.
     */
    List<FileProjection> findFilesByUserAndExperiment(User user, Experiment experiment);

    /**
     * Returns all files uploaded by the given user during the given experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return A list of files that is empty if no entry could be found.
     */
    List<File> findAllByUserAndExperiment(User user, Experiment experiment);

    /**
     * Returns all files with the given name uploaded by the given user during the given experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @param name The file name to search for.
     * @return A list of files, or an empty list, if no matching entries could be found.
     */
    List<File> findAllByUserAndExperimentAndNameOrderByDateDesc(User user, Experiment experiment, String name);

    /**
     * Returns the file with the given id, if any such file exists.
     *
     * @param id The file id to search for.
     * @return An {@link Optional} file.
     */
    Optional<File> findById(int id);

}
