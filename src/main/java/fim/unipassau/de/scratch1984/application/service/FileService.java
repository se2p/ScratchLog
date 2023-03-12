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
    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

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

            if (isInvalidParticipant(participant, user, experiment, "file")) {
                return;
            }

            File file = createFile(fileDTO, user, experiment);
            fileRepository.save(file);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find user with id " + fileDTO.getUser() + " or experiment with id "
                    + fileDTO.getExperiment() + " when trying to save a file!", e);
        } catch (ConstraintViolationException e) {
            LOGGER.error("Could not store the file for user with id " + fileDTO.getUser() + " for experiment with id "
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

            if (isInvalidParticipant(participant, user, experiment, "sb3 zip file")) {
                return;
            }

            Sb3Zip sb3Zip = createSb3Zip(sb3ZipDTO, user, experiment);
            sb3ZipRepository.save(sb3Zip);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find user with id " + sb3ZipDTO.getUser() + " or experiment with id "
                    + sb3ZipDTO.getExperiment() + " when trying to save an sb3 zip file!", e);
        } catch (ConstraintViolationException e) {
            LOGGER.error("Could not store the zip for user with id " + sb3ZipDTO.getUser() + " for experiment with id "
                    + sb3ZipDTO.getExperiment() + " since the sb3 zip violates the" + " file table constraints!", e);
        }
    }

    /**
     * Returns the file names and ids of all {@link File}s the user with the given id uploaded during the experiment
     * with the given id.
     *
     * @param userId The user id to search for.
     * @param experimentId The experiment id to search for.
     * @return A {@link List} containing the file ids and names.
     * @throws IllegalArgumentException if the passed user or experiment ids are invalid.
     * @throws NotFoundException if no corresponding user or experiment could be found.
     */
    @Transactional
    public List<FileProjection> getFiles(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot get file ids and names for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            return fileRepository.findFilesByUserAndExperiment(user, experiment);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not retrieve the file names and ids since the  user with id " + userId
                    + " or experiment with id " + experimentId + " could not be found!", e);
            throw new NotFoundException("Could not retrieve the file names and ids since the  user with id " + userId
                    + " or experiment with id " + experimentId + " could not be found!", e);
        }
    }

    /**
     * Returns all files the user with the given id uploaded during the experiment with the given id.
     *
     * @param userId The user id to search for.
     * @param experimentId The experiment id to search for.
     * @return A {@link List} containing the files.
     * @throws IllegalArgumentException if the passed user or experiment ids are invalid.
     * @throws NotFoundException if no corresponding user or experiment could be found.
     */
    public List<FileDTO> getFileDTOs(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot get files for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            List<File> files = fileRepository.findAllByUserAndExperiment(user, experiment);
            return createFileDTOList(files);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not retrieve the files since the user with id " + userId + " or experiment with id "
                    + experimentId + " could not be found!", e);
            throw new NotFoundException("Could not retrieve the files since the  user with id " + userId
                    + " or experiment with id " + experimentId + " could not be found!", e);
        }
    }

    /**
     * Returns the zip file ids of all {@link Sb3Zip}s that were created for the user with the given id during the
     * experiment with the given id.
     *
     * @param userId The user id to search for.
     * @param experimentId The experiment id to search for.
     * @return A {@link List} containing the file ids and names.
     * @throws IllegalArgumentException if the passed user or experiment ids are invalid.
     * @throws NotFoundException if no corresponding user or experiment could be found.
     */
    @Transactional
    public List<Integer> getZipIds(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot get zip file ids for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            return sb3ZipRepository.findAllIdsByUserAndExperiment(user, experiment);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not retrieve the zip file ids since the  user with id " + userId
                    + " or experiment with id " + experimentId + " could not be found!", e);
            throw new NotFoundException("Could not retrieve the zip file ids since the  user with id " + userId
                    + " or experiment with id " + experimentId + " could not be found!", e);
        }
    }

    /**
     * Returns a {@link FileDTO} with the specified ID.
     *
     * @param id The file ID to search for.
     * @return The file, if it exists.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding file could be found.
     */
    @Transactional
    public FileDTO findFile(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot search for file with invalid id " + id + "!");
        }

        Optional<File> file = fileRepository.findById(id);

        if (file.isEmpty()) {
            LOGGER.error("Could not find file with id " + id + "!");
            throw new NotFoundException("Could not find file with id " + id + "!");
        }

        return createFileDTO(file.get());
    }

    /**
     * Returns a {@link Sb3ZipDTO} with the specified ID.
     *
     * @param id The zip file ID to search for.
     * @return The zip file, if it exists.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding sb3 zip could be found.
     */
    @Transactional
    public Sb3ZipDTO findZip(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot search for zip file with invalid id " + id + "!");
        }

        Optional<Sb3Zip> zip = sb3ZipRepository.findById(id);

        if (zip.isEmpty()) {
            LOGGER.error("Could not find zip file with id " + id + "!");
            throw new NotFoundException("Could not find zip file with id " + id + "!");
        }

        return createSb3ZipDTO(zip.get());
    }

    /**
     * Returns the final project {@link Sb3ZipDTO} for the user with the given id during the experiment with the given
     * id. If no such file exists, an empty {@link Optional} dto is returned instead.
     *
     * @param userId The user ID to search for.
     * @param experimentId The experiment ID to search for.
     * @return The zip file, if it exists, or an empty optional.
     * @throws IllegalArgumentException if the passed user or experiment ids are invalid.
     * @throws NotFoundException if no corresponding user or experiment could be found.
     */
    @Transactional
    public Optional<Sb3ZipDTO> findFinalProject(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot search for final project for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            Optional<Sb3Zip> finalProject = sb3ZipRepository.findFirstByUserAndExperiment(user,
                    experiment);

            if (finalProject.isEmpty()) {
                LOGGER.info("Could not find final project file for user with id " + userId + " for experiment with id "
                        + experimentId + "!");
                return Optional.empty();
            }

            return Optional.of(createSb3ZipDTO(finalProject.get()));
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not retrieve the final project file since the  user with id " + userId
                    + " or experiment with id " + experimentId + " could not be found!", e);
            throw new NotFoundException("Could not retrieve the final project file since the  user with id " + userId
                    + " or experiment with id " + experimentId + " could not be found!", e);
        }
    }

    /**
     * Returns a list of all {@link Sb3ZipDTO}s that were created for the user with the given id during the experiment
     * with the given id.
     *
     * @param userId The user id to search for.
     * @param experimentId The experiment id to search for.
     * @return A list of zip files.
     * @throws IllegalArgumentException if the passed user or experiment ids are invalid.
     * @throws NotFoundException if no corresponding user or experiment could be found.
     */
    @Transactional
    public List<Sb3ZipDTO> getZipFiles(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot download zip files for user with invalid id " + userId
                    + " or experiment with invalid id " + experimentId + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            List<Sb3Zip> sb3Zips = sb3ZipRepository.findAllByUserAndExperiment(user, experiment);

            if (sb3Zips.isEmpty()) {
                LOGGER.error("Could not find any zip files for user with id " + userId + " for experiment with id "
                        + experimentId + "!");
                throw new NotFoundException("Could not find any zip files for user with id " + userId
                        + " for experiment with id " + experimentId + "!");
            }

            return createSb3ZipDTOList(sb3Zips);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Cannot download zip files as no user with id " + userId + " or no experiment with id "
                    + experimentId + " could be found in the database!", e);
            throw new NotFoundException("Cannot download zip files as no user with id " + userId
                    + " or no experiment with id " + experimentId + " could be found in the database!", e);
        }
    }

    /**
     * Checks, whether the given participant data is valid. This is the case if no corresponding participant exists, the
     * participant has already finished the experiment, or the user or experiment itself is inactive.
     *
     * @param participant The {@link Participant} to check.
     * @param user The {@link User} participating in the experiment.
     * @param experiment The {@link Experiment} in question.
     * @param fileType The type of file that is to be saved.
     * @return {@code true} if the participant data is invalid, or {@code false} otherwise.
     */
    private boolean isInvalidParticipant(final Participant participant, final User user, final Experiment experiment,
                                         final String fileType) {
        if (participant == null) {
            LOGGER.error("No corresponding participant entry could be found for user with id " + user.getId()
                    + " and experiment " + experiment.getId() + " when trying to save a " + fileType + "!");
            return true;
        } else if (participant.getEnd() != null) {
            LOGGER.error("Tried to save a " + fileType + " for participant " + user.getId() + " during experiment "
                    + experiment.getId() + " who has already finished!");
            return true;
        } else if (!user.isActive() || !experiment.isActive()) {
            LOGGER.error("Tried to save a " + fileType + " for participant " + user.getId() + " during experiment "
                    + experiment.getId() + " with user or experiment inactive!");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates a list of {@link FileDTO}s form the given {@link File} list.
     *
     * @param files A list of files.
     * @return A list of file dtos.
     */
    private List<FileDTO> createFileDTOList(final List<File> files) {
        List<FileDTO> fileDTOS = new ArrayList<>();

        for (File file : files) {
            fileDTOS.add(createFileDTO(file));
        }

        return fileDTOS;
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
        File file = File.builder()
                .user(user)
                .experiment(experiment)
                .date(Timestamp.valueOf(fileDTO.getDate()))
                .name(fileDTO.getName())
                .filetype(fileDTO.getFiletype())
                .content(fileDTO.getContent())
                .build();

        if (fileDTO.getId() != null) {
            file.setId(fileDTO.getId());
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
        FileDTO fileDTO = FileDTO.builder()
                .user(file.getUser().getId())
                .experiment(file.getExperiment().getId())
                .filetype(file.getFiletype())
                .date(file.getDate().toLocalDateTime())
                .name(file.getName())
                .content(file.getContent())
                .build();

        if (file.getId() != null) {
            fileDTO.setId(file.getId());
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
        Sb3Zip sb3Zip = Sb3Zip.builder()
                .user(user)
                .experiment(experiment)
                .date(Timestamp.valueOf(sb3ZipDTO.getDate()))
                .name(sb3ZipDTO.getName())
                .content(sb3ZipDTO.getContent())
                .build();

        if (sb3ZipDTO.getId() != null) {
            sb3Zip.setId(sb3ZipDTO.getId());
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
        Sb3ZipDTO sb3ZipDTO = Sb3ZipDTO.builder()
                .user(sb3Zip.getUser().getId())
                .experiment(sb3Zip.getExperiment().getId())
                .name(sb3Zip.getName())
                .date(sb3Zip.getDate().toLocalDateTime())
                .content(sb3Zip.getContent())
                .build();

        if (sb3Zip.getId() != null) {
            sb3ZipDTO.setId(sb3Zip.getId());
        }

        return sb3ZipDTO;
    }

}
