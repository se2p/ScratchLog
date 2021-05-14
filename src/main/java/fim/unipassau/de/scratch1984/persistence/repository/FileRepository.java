package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.File;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * A repository providing functionality for retrieving file data.
 */
public interface FileRepository extends JpaRepository<File, Integer> {

    /**
     * Returns all files uploaded by the given user during the given experiment, if any exist.
     *
     * @param user The user to search for.
     * @param experiment The experiment to search for.
     * @return A list of files that is empty if no entry could be found.
     */
    List<File> findFileByUserAndExperiment(User user, Experiment experiment);

    /**
     * Returns the content of the file with the given id, if any such file exists.
     *
     * @param id The file id to search for.
     * @return A content of the file.
     */
    @Query(nativeQuery = true, value = "SELECT f.content FROM file AS f WHERE f.id = :fileId;")
    byte[] getFileContent(@Param("fileId") Integer id);

}