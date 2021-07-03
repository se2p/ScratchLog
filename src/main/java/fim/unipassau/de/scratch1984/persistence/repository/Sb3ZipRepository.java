package fim.unipassau.de.scratch1984.persistence.repository;

import fim.unipassau.de.scratch1984.persistence.entity.Sb3Zip;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * A repository providing functionality for sb3 zip files.
 */
public interface Sb3ZipRepository extends JpaRepository<Sb3Zip, Integer> {

    /**
     * Returns the zip file with the given id, if any such file exists.
     *
     * @param id The file id to search for.
     * @return A content of the file.
     */
    Sb3Zip findSb3ZipById(int id);

}
