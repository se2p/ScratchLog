package fim.unipassau.de.scratch1984.application.service;

import fim.unipassau.de.scratch1984.application.exception.NotFoundException;
import fim.unipassau.de.scratch1984.application.exception.StoreException;
import fim.unipassau.de.scratch1984.persistence.entity.Experiment;
import fim.unipassau.de.scratch1984.persistence.entity.Participant;
import fim.unipassau.de.scratch1984.persistence.entity.ParticipantId;
import fim.unipassau.de.scratch1984.persistence.entity.User;
import fim.unipassau.de.scratch1984.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratch1984.persistence.repository.ParticipantRepository;
import fim.unipassau.de.scratch1984.persistence.repository.UserRepository;
import fim.unipassau.de.scratch1984.util.Constants;
import fim.unipassau.de.scratch1984.web.dto.ParticipantDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A service providing methods related experiment participation.
 */
@Service
public class ParticipantService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger logger = LoggerFactory.getLogger(ParticipantService.class);

    /**
     * The user repository to use for database queries related to user data.
     */
    private final UserRepository userRepository;

    /**
     * The experiment repository to use for database queries related to experiment data.
     */
    private final ExperimentRepository experimentRepository;

    /**
     * The participant repository to use for database queries participation data.
     */
    private final ParticipantRepository participantRepository;

    /**
     * Constructs a participant service with the given dependencies.
     *
     * @param userRepository The user repository to use.
     * @param participantRepository The participant repository to use.
     * @param experimentRepository The experiment repository to use.
     */
    @Autowired
    public ParticipantService(final UserRepository userRepository, final ParticipantRepository participantRepository,
                              final ExperimentRepository experimentRepository) {
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
        this.experimentRepository = experimentRepository;
    }

    /**
     * Retrieves the participant information for the given experiment id and the given user id from the database. If no
     * corresponding user, experiment or participant can be found, a {@link NotFoundException} is thrown instead.
     *
     * @param experimentId The experiment id.
     * @param userId The user id.
     * @return The {@link ParticipantDTO} containing the participant information.
     */
    @Transactional
    public ParticipantDTO getParticipant(final int experimentId, final int userId) {
        if (experimentId < Constants.MIN_ID || userId < Constants.MIN_ID) {
            logger.error("Cannot search for participant with invalid experiment id " + experimentId
                    + " or invalid user id " + userId + "!");
            throw new IllegalArgumentException("Cannot search for participant with invalid experiment id "
                    + experimentId + " or invalid user id " + userId + "!");
        }

        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            Participant participant = participantRepository.findByUserAndExperiment(user, experiment);

            if (participant == null) {
                logger.error("Could not find any participant entry for user with id " + userId + " for experiment with "
                        + "id " + experimentId + "!");
                throw new NotFoundException("Could not find any participant entry for user with id " + userId
                        + " for experiment with id " + experimentId + "!");
            }

            return createParticipantDTO(participant);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find user with id " + userId + " or experiment with id " + experimentId
                    + " in the database!", e);
            throw new NotFoundException("Could not find user with id " + userId + " or experiment with id "
                    + experimentId + " in the database!", e);
        }
    }

    /**
     * Creates a new participation for the given user and experiment in the database.
     *
     * @param userId The user id.
     * @param experimentId The experiment id.
     */
    @Transactional
    public void saveParticipant(final int userId, final int experimentId) {
        User user = userRepository.getOne(userId);
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            Participant participant = new Participant(user, experiment, null, null);
            participantRepository.save(participant);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find the user or experiment when saving the participant data!", e);
            throw new NotFoundException("Could not find the user or experiment when saving the participant data!", e);
        } catch (ConstraintViolationException e) {
            logger.error("The given participant data does not meet the foreign key constraints!", e);
            throw new StoreException("The given participant data does not meet the foreign key constraints!", e);
        }
    }

    /**
     * Updates the participation information with the given values.
     *
     * @param participantDTO The dto containing the updated participation information.
     * @return {@code true} if the information was persisted, or {@code false} if not.
     */
    @Transactional
    public boolean updateParticipant(final ParticipantDTO participantDTO) {
        if (participantDTO.getExperiment() < Constants.MIN_ID || participantDTO.getUser() < Constants.MIN_ID) {
            logger.error("Cannot search for participant with invalid experiment id " + participantDTO.getExperiment()
                    + " or invalid user id " + participantDTO.getUser() + "!");
            throw new IllegalArgumentException("Cannot search for participant with invalid experiment id "
                    + participantDTO.getExperiment() + " or invalid user id " + participantDTO.getUser() + "!");
        }

        User user = userRepository.getOne(participantDTO.getUser());
        Experiment experiment = experimentRepository.getOne(participantDTO.getExperiment());

        try {
            participantRepository.save(createParticipant(participantDTO, user, experiment));
            return true;
        } catch (EntityNotFoundException e) {
            logger.error("Could not find the user with id " + participantDTO.getUser() + " or experiment with id "
                    + participantDTO.getExperiment() + " when trying to update a participant!", e);
        } catch (ConstraintViolationException e) {
            logger.error("No participant entry could be found for user with id " + participantDTO.getUser()
                    + " for experiment with id " + participantDTO.getExperiment() + "!", e);
        }

        return false;
    }

    /**
     * Retrieves a page of participants for the experiment with the given id. If no corresponding experiment exists in
     * the database a {@link NotFoundException} is thrown instead.
     *
     * @param id The experiment id.
     * @param pageable The pageable containing the page size and page number.
     * @return The participant page.
     */
    @Transactional
    public Page<Participant> getParticipantPage(final int id, final Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();

        if (id < Constants.MIN_ID) {
            logger.error("Cannot find participant data for experiment with invalid id " + id + "!");
            throw new IllegalArgumentException("Cannot find participant data for experiment with invalid id " + id
                    + "!");
        } else if (pageSize != Constants.PAGE_SIZE) {
            logger.error("Cannot return participant page with invalid page size of " + pageSize + "!");
            throw new IllegalArgumentException("Cannot return experiment page with invalid page size of " + pageSize
                    + "!");
        }

        Experiment experiment = experimentRepository.getOne(id);
        Page<Participant> participants;

        try {
            participants = participantRepository.findAllByExperiment(experiment, PageRequest.of(currentPage, pageSize,
                    Sort.by("user").descending()));
        } catch (EntityNotFoundException e) {
            logger.error("Could not find experiment with id " + id + " in the database!", e);
            throw new NotFoundException("Could not find experiment with id " + id + " in the database!", e);
        }

        return participants;
    }

    /**
     * Retrieves a list of participant ids for the experiment with the given id. If no corresponding experiment exists
     * in the database a {@link NotFoundException} is thrown instead.
     *
     * @param experimentId The experiment id.
     */
    @Transactional
    public void deactivateParticipantAccounts(final int experimentId) {
        if (experimentId < Constants.MIN_ID) {
            logger.error("Cannot find participant data for experiment with invalid id " + experimentId + "!");
            throw new IllegalArgumentException("Cannot find participant data for experiment with invalid id "
                    + experimentId + "!");
        }

        List<Participant> participants;
        Experiment experiment = experimentRepository.getOne(experimentId);

        try {
            participants = participantRepository.findAllByExperiment(experiment);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find experiment with id " + experimentId + "!");
            throw new NotFoundException("Could not find experiment with id " + experimentId + "!");
        }

        for (Participant participant : participants) {
            Optional<User> user = userRepository.findById(participant.getUser().getId());

            if (user.isEmpty()) {
                logger.error("No user entry could be found for user with id " + participant.getUser().getId()
                        + " corresponding to the participant entry for experiment with id " + experimentId + "!");
                throw new IllegalStateException("No user entry could be found for user with id "
                        + participant.getUser().getId() + " corresponding to the participant entry for experiment with "
                        + "id " + experimentId + "!");
            }

            User found = user.get();
            found.setActive(false);
            found.setSecret(null);
            userRepository.save(found);
        }
    }

    /**
     * Retrieves the experiment ids of the experiments in which the user with the given user id is participating. If no
     * corresponding user exists in the database a {@link NotFoundException} is thrown instead.
     *
     * @param userId The user id.
     * @return The list of experiment ids.
     */
    @Transactional
    public List<Integer> getExperimentIdsForParticipant(final int userId) {
        if (userId < Constants.MIN_ID) {
            logger.error("Cannot find participant data for user with invalid id " + userId + "!");
            throw new IllegalArgumentException("Cannot find participant data for user with invalid id "
                    + userId + "!");
        }

        List<Participant> participants;
        List<Integer> ids = new ArrayList<>();
        User user = userRepository.getOne(userId);

        try {
            participants = participantRepository.findAllByUser(user);
        } catch (EntityNotFoundException e) {
            logger.error("Could not find user with id " + userId + "!");
            throw new NotFoundException("Could not find user with id " + userId + "!");
        }

        for (Participant participant : participants) {
            ids.add(participant.getExperiment().getId());
        }

        return ids;
    }

    /**
     * Deletes the participant with the given user and experiment id from the database, if any such participant exists.
     *
     * @param userId The user id to search for.
     * @param experimentId The experiment id to search for.
     */
    @Transactional
    public void deleteParticipant(final int userId, final int experimentId) {
        if (userId < Constants.MIN_ID || experimentId < Constants.MIN_ID) {
            logger.error("Cannot delete participant with invalid user id " + userId + " or invalid experiment id "
                    + experimentId + "!");
            throw new IllegalArgumentException("Cannot delete participant with invalid user id " + userId
                    + " or invalid experiment id " + experimentId + "!");
        }

        ParticipantId participantId = new ParticipantId(userId, experimentId);
        participantRepository.deleteById(participantId);
    }

    /**
     * Creates a {@link Participant} with the given information of the {@link ParticipantDTO}, the {@link User}, and the
     * {@link Experiment}.
     *
     * @param participantDTO The dto containing the information.
     * @param user The corresponding user.
     * @param experiment The corresponding experiment.
     * @return The new participant containing the information passed in the DTO.
     */
    private Participant createParticipant(final ParticipantDTO participantDTO, final User user,
                                          final Experiment experiment) {
        Participant participant = new Participant();

        if (user != null) {
            participant.setUser(user);
        }
        if (experiment != null) {
            participant.setExperiment(experiment);
        }
        if (participantDTO.getStart() != null) {
            participant.setStart(Timestamp.valueOf(participantDTO.getStart()));
        }
        if (participantDTO.getEnd() != null) {
            participant.setEnd(Timestamp.valueOf(participantDTO.getEnd()));
        }

        return participant;
    }

    /**
     * Creates a {@link ParticipantDTO} with the given information from the {@link Participant}.
     *
     * @param participant The participant object containing the information.
     * @return The new participant DTO containing the information passed in the participant object.
     */
    private ParticipantDTO createParticipantDTO(final Participant participant) {
        ParticipantDTO participantDTO = new ParticipantDTO();

        if (participant.getUser().getId() != null) {
            participantDTO.setUser(participant.getUser().getId());
        }
        if (participant.getExperiment().getId() != null) {
            participantDTO.setExperiment(participant.getExperiment().getId());
        }
        if (participant.getStart() != null) {
            participantDTO.setStart(participant.getStart().toLocalDateTime());
        }
        if (participant.getEnd() != null) {
            participantDTO.setEnd(participant.getEnd().toLocalDateTime());
        }

        return participantDTO;
    }

}