package fim.unipassau.de.scratch1984;

import fim.unipassau.de.scratch1984.util.ApplicationProperties;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

/**
 * Utility class to set the mail server constants in the {@link fim.unipassau.de.scratch1984.util.ApplicationProperties}
 * class for testing.
 */
public final class MailServerSetter {

    public static void setMailServer(boolean isMailServer) {
        Mockito.mock(ApplicationProperties.class);
        Whitebox.setInternalState(ApplicationProperties.class, "MAIL_SERVER", isMailServer);
    }
}
