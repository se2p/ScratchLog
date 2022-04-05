package fim.unipassau.de.scratch1984;

import fim.unipassau.de.scratch1984.util.Constants;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

/**
 * Utility class to set the mail server constants in the {@link fim.unipassau.de.scratch1984.util.Constants} class for
 * testing.
 */
public final class MailServerSetter {

    public static void setMailServer(boolean isMailServer) {
        Mockito.mock(Constants.class);
        Whitebox.setInternalState(Constants.class, "MAIL_SERVER", isMailServer);
    }
}
