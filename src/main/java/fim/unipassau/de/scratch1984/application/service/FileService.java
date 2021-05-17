package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.persistence.entity.File;
import fim.unipassau.de.scratch1984.persistence.repository.FileRepository;
import fim.unipassau.de.scratch1984.web.dto.FileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service providing methods related to file persistence and retrieval.
 */
@Service
public class FileService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    /**
     * The file repository to use for database queries related to files.
     */
    private final FileRepository fileRepository;

    /**
     * Constructs a file service with the given dependencies.
     *
     * @param fileRepository The file repository to use.
     */
    @Autowired
    public FileService(final FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    /**
     * Creates a new file with the given parameters in the database.
     *
     * @param fileDTO The dto containing the file information to set.
     */
    public void saveFile(final FileDTO fileDTO) {
    }

    /**
     * Returns the file with the specified ID. If no such file exists, returns {@code null}.
     *
     * @param id The file ID to search for.
     * @return The file, if it exists, or {@code null} if no file with that ID exists.
     */
    public byte[] getFileContent(final Integer id) {
        return null;
    }

    /**
     * Creates a {@link File} with the given information of the {@link FileDTO}.
     *
     * @param fileDTO The dto containing the information.
     * @return The new file containing the information passed in the DTO.
     */
    private File createFile(final FileDTO fileDTO) {
        return null;
    }

}
