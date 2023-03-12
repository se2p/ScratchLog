package fim.unipassau.de.scratch1984.spring;

import fim.unipassau.de.scratch1984.application.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

/**
 * Class performing scheduled tasks to clean up expired tokens and any changes associated with them.
 */
@Configuration
public class TokenCleaner {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenCleaner.class);

    /**
     * The token service to use for generating tokens.
     */
    private final TokenService tokenService;

    /**
     * The interval in milliseconds until the next scheduled task invocation.
     */
    private static final int CLEANER_INTERVAL = 600000;

    /**
     * Constructs a new token cleaner with the given dependencies.
     *
     * @param tokenService The {@link TokenService} to use.
     */
    public TokenCleaner(final TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Task scheduled to run every 10 minutes to delete expired tokens from the database.
     */
    @Scheduled(fixedRate = CLEANER_INTERVAL)
    public void cleanOldTokens() {
        LOGGER.info("Starting scheduled task to delete expired tokens.");
        LocalDateTime time = LocalDateTime.now();
        tokenService.deleteExpiredAccounts(time);
        tokenService.reactivateUserAccounts(time);
        tokenService.deleteExpiredTokens(time);
    }

}
