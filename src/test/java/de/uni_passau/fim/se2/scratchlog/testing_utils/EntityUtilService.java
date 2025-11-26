package de.uni_passau.fim.se2.scratchlog.testing_utils;

import de.uni_passau.fim.se2.scratchlog.persistence.entity.Course;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.CourseExperiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.CourseParticipant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Participant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Sb3Zip;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.User;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.CourseExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.CourseParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.CourseRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.Sb3ZipRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.UserRepository;
import de.uni_passau.fim.se2.scratchlog.util.enums.Language;
import de.uni_passau.fim.se2.scratchlog.util.enums.Role;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Utility methods for common domain entity creations for tests.
 *
 * <p>Implementation note: The public methods should save the created entities to the database already and return the
 * persisted objects.
 */
@Service
public class EntityUtilService {

    private final CourseRepository courseRepository;

    private final ExperimentRepository experimentRepository;

    private final UserRepository userRepository;

    private final CourseParticipantRepository courseParticipantRepository;

    private final CourseExperimentRepository courseExperimentRepository;

    private final ParticipantRepository participantRepository;

    private final Sb3ZipRepository sb3ZipRepository;

    public EntityUtilService(
        CourseRepository courseRepository,
        ExperimentRepository experimentRepository,
        UserRepository userRepository,
        CourseParticipantRepository courseParticipantRepository,
        CourseExperimentRepository courseExperimentRepository,
        ParticipantRepository participantRepository,
        Sb3ZipRepository sb3ZipRepository
    ) {
        this.courseRepository = courseRepository;
        this.experimentRepository = experimentRepository;
        this.userRepository = userRepository;
        this.courseParticipantRepository = courseParticipantRepository;
        this.courseExperimentRepository = courseExperimentRepository;
        this.participantRepository = participantRepository;
        this.sb3ZipRepository = sb3ZipRepository;
    }

    /**
     * Generates a new user with a unique name ending in the given name.
     *
     * @param name The name suffix for the user.
     * @return The user as it was saved to the database.
     */
    public User generateUser(final String name) {
        final String login = namePrefix() + name;
        final User user = new User(login, login + "@example.com", Role.PARTICIPANT, Language.ENGLISH, "12345678", null);
        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Generates multiple users with unique names.
     *
     * <p>Naming schema: {@code <UNIQUE_PREFIX>_<namePrefix>_<number in 1..count>}.
     *
     * @param namePrefix The non-unique part of the username before the number.
     * @param count The number of users to be created.
     * @return The newly created users after saving them to the database.
     */
    public List<User> generateUsers(final String namePrefix, final int count) {
        final List<User> users = new ArrayList<>();
        final String fullName = namePrefix() + namePrefix;

        for (int i = 1; i <= count; ++i) {
            final String login = fullName + "_" + i;
            final User user = new User(login, login + "@example.com", Role.PARTICIPANT, Language.ENGLISH, "12345678", null);
            user.setLastLogin(LocalDateTime.now());
            users.add(user);
        }

        return userRepository.saveAll(users);
    }

    /**
     * Adds the given users to the course as participants.
     *
     * @param course Some course already existing in the database.
     * @param users Some users already existing in the database.
     * @return The participation information of the users as it was saved in the database.
     */
    public List<CourseParticipant> addUsersToCourse(final Course course, final Iterable<User> users) {
        final List<CourseParticipant> participants = new ArrayList<>();

        for (final User user : users) {
            final CourseParticipant participant = new CourseParticipant(user, course, LocalDateTime.now());
            participants.add(participant);
        }

        return courseParticipantRepository.saveAll(participants);
    }

    /**
     * Adds the given users to the experiment as participants.
     *
     * @param experiment Some experiment already existing in the database.
     * @param users Some users already existing in the database.
     * @return The participation information of the users as it was saved in the database.
     */
    public List<Participant> addUsersToExperiment(final Experiment experiment, final Iterable<User> users) {
        final List<Participant> participants = new ArrayList<>();

        for (final User user : users) {
            final Participant participant = new Participant(user, experiment, null, null);
            participants.add(participant);
        }

        return participantRepository.saveAll(participants);
    }

    /**
     * Adds the given users to the experiment as participants.
     *
     * @param experiment Some experiment already existing in the database.
     * @param users Some users already existing in the database.
     * @return The participation information of the users as it was saved in the database.
     */
    public List<Participant> addUsersToExperiment(final Experiment experiment, final User... users) {
        return addUsersToExperiment(experiment, Arrays.asList(users));
    }

    /**
     * Generates a new course with the given name as suffix.
     *
     * <p>Automatically adds a unique prefix to the name.
     *
     * @param name The name for the new course.
     * @return The course as it was stored in the database.
     */
    public Course generateCourse(final String name) {
        final Course course = new Course(null, namePrefix() + name, "Description", "", false, LocalDateTime.now());
        return courseRepository.save(course);
    }

    /**
     * Generates a new experiment with the given name as suffix.
     *
     * <p>Automatically adds a unique prefix to the name.
     *
     * @param name The name for the new experiment.
     * @return The experiment as it was stored in the database.
     */
    public Experiment generateExperiment(final String name) {
        final String fullName = namePrefix() + name;
        final Experiment experiment = new Experiment(null, fullName, "description for experiment " + fullName, "info", "postscript", false, false, "");
        return experimentRepository.save(experiment);
    }

    /**
     * Creates a new experiment and adds it to a course.
     *
     * <p>Automatically adds a unique prefix to the name.
     *
     * @param course A course already stored in the database.
     * @param name The name of the experiment.
     * @return The experiment as it was stored in the database.
     */
    public CourseExperiment addExperimentToCourse(final Course course, final String name) {
        final Experiment experiment = generateExperiment(name);
        final CourseExperiment courseExperiment = new CourseExperiment(course, experiment, LocalDateTime.now());
        return courseExperimentRepository.save(courseExperiment);
    }

    /**
     * Creates a new dummy SB3 file.
     *
     * <p>Does <em>not</em> contain a valid ZIP byte sequence.
     *
     * @param user The user the SB3 is related to. Must already exist in the database.
     * @param experiment The experiment the SB3 is related to. Must already exist in the database.
     * @return The SB3Zip as it was stored in the database.
     */
    public Sb3Zip generateSb3Zip(final User user, final Experiment experiment) {
        final Sb3Zip zip = new Sb3Zip(user, experiment, LocalDateTime.now(), UUID.randomUUID().toString(), new byte[] {1, 2, 3});
        return sb3ZipRepository.save(zip);
    }

    private String namePrefix() {
        return UUID.randomUUID() + "_";
    }
}
