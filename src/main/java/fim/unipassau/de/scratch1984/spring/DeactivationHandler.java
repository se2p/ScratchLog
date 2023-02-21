package fim.unipassau.de.scratch1984.spring;

import fim.unipassau.de.scratch1984.application.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Class performing scheduled tasks to deactivate old participant accounts, experiments and courses.
 */
@Configuration
public class DeactivationHandler {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DeactivationHandler.class);

    /**
     * The user service to use for user management.
     */
    private final UserService userService;

    /**
     * The interval in milliseconds until the next scheduled task invocation.
     */
    private static final int INTERVAL = 86400000;

    /**
     * Constructs a new deactivation handler with the given dependencies.
     *
     * @param userService The {@link UserService} to use.
     */
    public DeactivationHandler(final UserService userService) {
        this.userService = userService;
    }

    /**
     * Task scheduled to run once a day to deactivate old participant accounts.
     */
    @Scheduled(fixedRate = INTERVAL)
    public void deactivateOldAccounts() {
        LOGGER.info("Starting scheduled task to deactivate old participant accounts.");
        userService.deactivateOldParticipantAccounts();
    }

}
