package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.File;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.Sb3Zip;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.projection.FileProjection;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.FileRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.Sb3ZipRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.dto.FileDTO;
import fim.unipassau.de.scratch1984.web.dto.Sb3ZipDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
     * The sb3 zip repository to use for sb3 zip queries.
     */
    private final Sb3ZipRepository sb3ZipRepository;

    /**
     * Constructs a file service with the given dependencies.
     *
     * @param fileRepository The file repository to use.
     * @param participantRepository The participant repository to use.
     * @param userRepository The user repository to use.
     * @param experimentRepository The experiment repository to use.
     * @param sb3ZipRepository The sb3 zip repository to use.
     */
    @Autowired
    public FileService(final FileRepository fileRepository, final ParticipantRepository participantRepository,
                       final UserRepository userRepository, final ExperimentRepository experimentRepository,
                       final Sb3ZipRepository sb3ZipRepository) {
        this.fileRepository = fileRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.experimentRepository = experimentRepository;
        this.sb3ZipRepository = sb3ZipRepository;
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
     * Creates a new sb3 zip file with the given parameters in the database.
     *
     * @param sb3ZipDTO The dto containing the file information to set.
     */
    @Transactional
    public void saveSb3Zip(final Sb3ZipDTO sb3ZipDTO) {
        User user = userRepository.getOne(sb3ZipDTO.getUser());
        Experiment experiment = experimentRepository.getOne(sb3ZipDTO.getExperiment());

        try {
            Participant participant = participantRepository.findByUserAndExperiment(user, experiment);

            if (participant == null) {
                logger.error("No corresponding participant entry could be found for user with id "
                        + sb3ZipDTO.getUser() + " and experiment " + sb3ZipDTO.getExperiment()
                        + " when trying to save an sb3 zip file!");
                return;
            }

            Sb3Zip sb3Zip = createSb3Zip(sb3ZipDTO, user, experiment);
            sb3ZipRepository.save(sb3Zip);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find user with id " + sb3ZipDTO.getUser() + " or experiment with id "
                    + sb3ZipDTO.getExperiment() + " when trying to save an sb3 zip file!", e);
        } catch (ConstraintViolationException e) {
            logger.error("Could not store the zip for user with id " + sb3ZipDTO.getUser() + " for experiment with id "
                    + sb3ZipDTO.getExperiment() + " since the sb3 zip violates the" + " file table constraints!", e);
        }
    }

    /**
     * Returns the file names and ids of all {@link File}s the user with the given id uploaded during the experiment
     * with the given id. If no corresponding user or experiment can be found, a {@link NotFoundException} is thrown
     * instead.
     *
     * @param userId The user id to search for.
     * @param experimentId The experiment id to search for.
     * @return A {@link List} containing the file ids and names.
     */
    @Transactional
    public List<FileProjection> getFiles(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot get file ids and names for user with invalid id " + userId + " or experiment with "
                    + "invalid id " + experimentId + "!");
            throw new IllegalArgumentException("Cannot get file ids and names for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            return fileRepository.findFilesByUserAndExperiment(user, experiment);
        } catch (EntityNotFoundException e) {
            logger.error("Could not retrieve the file names and ids since the  user with id " + userId
                    + " or experiment with id " + experimentId + " could not be found!", e);
            throw new NotFoundException("Could not retrieve the file names and ids since the  user with id " + userId
                    + " or experiment with id " + experimentId + " could not be found!", e);
        }
    }

    /**
     * Returns the zip file ids of all {@link Sb3Zip}s that were created for the user with the given id during the
     * experiment with the given id. If no corresponding user or experiment can be found, a {@link NotFoundException} is
     * thrown instead.
     *
     * @param userId The user id to search for.
     * @param experimentId The experiment id to search for.
     * @return A {@link List} containing the file ids and names.
     */
    @Transactional
    public List<Integer> getZipIds(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot get zip file ids for user with invalid id " + userId + " or experiment with invalid "
                    + "id " + experimentId + "!");
            throw new IllegalArgumentException("Cannot get zip file ids for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            return sb3ZipRepository.findAllIdsByUserAndExperiment(user, experiment);
        } catch (EntityNotFoundException e) {
            logger.error("Could not retrieve the zip file ids since the  user with id " + userId
                    + " or experiment with id " + experimentId + " could not be found!", e);
            throw new NotFoundException("Could not retrieve the zip file ids since the  user with id " + userId
                    + " or experiment with id " + experimentId + " could not be found!", e);
        }
    }

    /**
     * Returns a {@link FileDTO} with the specified ID. If no such file exists, a {@link NotFoundException} is thrown
     * instead.
     *
     * @param id The file ID to search for.
     * @return The file, if it exists.
     */
    @Transactional
    public FileDTO findFile(final int id) {
        if (id < Constants.MIN_ID) {
            logger.error("Cannot search for file with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot search for file with invalid id " + id + "!");
        }

        Optional<File> file = fileRepository.findById(id);

        if (file.isEmpty()) {
            logger.error("Could not find file with id " + id + "!");
            throw new NotFoundException("Could not find file with id " + id + "!");
        }

        return createFileDTO(file.get());
    }

    /**
     * Returns a {@link Sb3ZipDTO} with the specified ID. If no such file exists, a {@link NotFoundException} is thrown
     * instead.
     *
     * @param id The zip file ID to search for.
     * @return The zip file, if it exists.
     */
    @Transactional
    public Sb3ZipDTO findZip(final int id) {
        if (id < Constants.MIN_ID) {
            logger.error("Cannot search for zip file with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot search for zip file with invalid id " + id + "!");
        }

        Optional<Sb3Zip> zip = sb3ZipRepository.findById(id);

        if (zip.isEmpty()) {
            logger.error("Could not find zip file with id " + id + "!");
            throw new NotFoundException("Could not find zip file with id " + id + "!");
        }

        return createSb3ZipDTO(zip.get());
    }

    /**
     * Returns a list of all {@link Sb3ZipDTO}s that were created for the user with the given id during the experiment
     * with the given id. If no corresponding user, experiment or zip files could be found, a {@link NotFoundException}
     * is thrown instead.
     *
     * @param userId The user id to search for.
     * @param experimentId The experiment id to search for.
     * @return A list of zip files.
     */
    @Transactional
    public List<Sb3ZipDTO> getZipFiles(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot download zip files for user with invalid id " + userId + " or experiment with invalid "
                    + "id " + experimentId + "!");
            throw new IllegalArgumentException("Cannot download zip files for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            List<Sb3Zip> sb3Zips = sb3ZipRepository.findAllByUserAndExperiment(user, experiment);

            if (sb3Zips.isEmpty()) {
                logger.error("Could not find any zip files for user with id " + userId + " for experiment with id "
                        + experimentId + "!");
                throw new NotFoundException("Could not find any zip files for user with id " + userId
                        + " for experiment with id " + experimentId + "!");
            }

            return createSb3ZipDTOList(sb3Zips);
        } catch (EntityNotFoundException e) {
            logger.error("Cannot download zip files as no user with id " + userId + " or no experiment with id "
                    + experimentId + " could be found in the database!", e);
            throw new NotFoundException("Cannot download zip files as no user with id " + userId
                    + " or no experiment with id " + experimentId + " could be found in the database!", e);
        }
    }

    /**
     * Creates a {@link File} with the given information of the {@link FileDTO}, the {@link User}, and the
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

    /**
     * Creates a {@link FileDTO} with the given information of the {@link File}.
     *
     * @param file The file containing the information.
     * @return The new file dto containing the information passed in the file.
     */
    private FileDTO createFileDTO(final File file) {
        FileDTO fileDTO = new FileDTO();

        if (file.getUser() != null) {
            fileDTO.setUser(file.getUser().getId());
        }
        if (file.getExperiment() != null) {
            fileDTO.setExperiment(file.getExperiment().getId());
        }
        if (file.getId() != null) {
            fileDTO.setId(file.getId());
        }
        if (file.getFiletype() != null) {
            fileDTO.setFiletype(file.getFiletype());
        }
        if (file.getDate() != null) {
            fileDTO.setDate(file.getDate().toLocalDateTime());
        }
        if (file.getName() != null) {
            fileDTO.setName(file.getName());
        }
        if (file.getContent() != null) {
            fileDTO.setContent(file.getContent());
        }

        return fileDTO;
    }

    /**
     * Creates a list of {@link Sb3ZipDTO}s form the given {@link Sb3Zip} list.
     *
     * @param sb3Zips A list of zip files.
     * @return A list of zip file dtos.
     */
    private List<Sb3ZipDTO> createSb3ZipDTOList(final List<Sb3Zip> sb3Zips) {
        List<Sb3ZipDTO> sb3ZipDTOS = new ArrayList<>();

        for (Sb3Zip sb3Zip : sb3Zips) {
            sb3ZipDTOS.add(createSb3ZipDTO(sb3Zip));
        }

        return sb3ZipDTOS;
    }

    /**
     * Creates a {@link Sb3Zip} with the given information of the {@link Sb3ZipDTO}, the {@link User}, and the
     * {@link Experiment}.
     *
     * @param sb3ZipDTO The dto containing the information.
     * @param user The user for whom the zip file was created.
     * @param experiment The experiment during which the zip file was created.
     * @return The new zip file containing the information passed in the DTO.
     */
    private Sb3Zip createSb3Zip(final Sb3ZipDTO sb3ZipDTO, final User user, final Experiment experiment) {
        Sb3Zip sb3Zip = new Sb3Zip();

        if (sb3ZipDTO.getId() != null) {
            sb3Zip.setId(sb3ZipDTO.getId());
        }
        if (user != null) {
            sb3Zip.setUser(user);
        }
        if (experiment != null) {
            sb3Zip.setExperiment(experiment);
        }
        if (sb3ZipDTO.getDate() != null) {
            sb3Zip.setDate(Timestamp.valueOf(sb3ZipDTO.getDate()));
        }
        if (sb3ZipDTO.getName() != null) {
            sb3Zip.setName(sb3ZipDTO.getName());
        }
        if (sb3ZipDTO.getContent() != null) {
            sb3Zip.setContent(sb3ZipDTO.getContent());
        }

        return sb3Zip;
    }

    /**
     * Creates a {@link Sb3ZipDTO} with the given information of the {@link Sb3Zip}.
     *
     * @param sb3Zip The zip file containing the information.
     * @return The new zip dto file.
     */
    private Sb3ZipDTO createSb3ZipDTO(final Sb3Zip sb3Zip) {
        Sb3ZipDTO sb3ZipDTO = new Sb3ZipDTO();

        if (sb3Zip.getUser() != null) {
            sb3ZipDTO.setUser(sb3Zip.getUser().getId());
        }
        if (sb3Zip.getExperiment() != null) {
            sb3ZipDTO.setExperiment(sb3Zip.getExperiment().getId());
        }
        if (sb3Zip.getId() != null) {
            sb3ZipDTO.setId(sb3Zip.getId());
        }
        if (sb3Zip.getName() != null) {
            sb3ZipDTO.setName(sb3Zip.getName());
        }
        if (sb3Zip.getDate() != null) {
            sb3ZipDTO.setDate(sb3Zip.getDate().toLocalDateTime());
        }
        if (sb3Zip.getContent() != null) {
            sb3ZipDTO.setContent(sb3Zip.getContent());
        }

        return sb3ZipDTO;
    }

}
