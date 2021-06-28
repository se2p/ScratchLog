package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.File;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.FileRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.web.dto.FileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.sql.Timestamp;

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
     * The participant repository to use for participation queries.
     */
    private final ParticipantRepository participantRepository;

    /**
     * The user repository to use for user queries.
     */
    private final UserRepository userRepository;

    /**
     * The experiment repository to use for experiment queries.
     */
    private final ExperimentRepository experimentRepository;

    /**
     * Constructs a file service with the given dependencies.
     *
     * @param fileRepository The file repository to use.
     * @param participantRepository The participant repository to use.
     * @param userRepository The user repository to use.
     * @param experimentRepository THe experiment repository to use.
     */
    @Autowired
    public FileService(final FileRepository fileRepository, final ParticipantRepository participantRepository,
                       final UserRepository userRepository, final ExperimentRepository experimentRepository) {
        this.fileRepository = fileRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.experimentRepository = experimentRepository;
    }

    /**
     * Creates a new file with the given parameters in the database.
     *
     * @param fileDTO The dto containing the file information to set.
     */
    @Transactional
    public void saveFile(final FileDTO fileDTO) {
        User user = userRepository.getOne(fileDTO.getUser());
        Experiment experiment = experimentRepository.getOne(fileDTO.getExperiment());

        try {
            Participant participant = participantRepository.findByUserAndExperiment(user, experiment);

            if (participant == null) {
                logger.error("No corresponding participant entry could be found for user with id "
                        + fileDTO.getUser() + " and experiment " + fileDTO.getExperiment()
                        + " when trying to save a file!");
                return;
            }

            File file = createFile(fileDTO, user, experiment);
            fileRepository.save(file);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find user with id " + fileDTO.getUser() + " or experiment with id "
                    + fileDTO.getExperiment() + " when trying to save a file!", e);
        } catch (ConstraintViolationException e) {
            logger.error("Could not store the file for user with id " + fileDTO.getUser() + " for experiment with id "
                    + fileDTO.getExperiment() + " since the file violates the" + " file table constraints!", e);
        }
    }

    /**
     * Returns the file with the specified ID. If no such file exists, returns {@code null}.
     *
     * @param id The file ID to search for.
     * @return The file, if it exists, or {@code null} if no file with that ID exists.
     */
    @Transactional
    public byte[] getFileContent(final Integer id) {
        return null;
    }

    /**
     * Creates a {@link File} with the given information of the {@link FileDTO}, , the {@link User}, and the
     * {@link Experiment}.
     *
     * @param fileDTO The dto containing the information.
     * @param user The user who uploaded the file.
     * @param experiment The experiment during which the file was uploaded.
     * @return The new file containing the information passed in the DTO.
     */
    private File createFile(final FileDTO fileDTO, final User user, final Experiment experiment) {
        File file = new File();

        if (user != null) {
            file.setUser(user);
        }
        if (experiment != null) {
            file.setExperiment(experiment);
        }
        if (fileDTO.getId() != null) {
            file.setId(fileDTO.getId());
        }
        if (fileDTO.getDate() != null) {
            file.setDate(Timestamp.valueOf(fileDTO.getDate()));
        }
        if (fileDTO.getName() != null) {
            file.setName(fileDTO.getName());
        }
        if (fileDTO.getFiletype() != null) {
            file.setFiletype(fileDTO.getFiletype());
        }
        if (fileDTO.getContent() != null) {
            file.setContent(fileDTO.getContent());
        }

        return file;
    }

}
