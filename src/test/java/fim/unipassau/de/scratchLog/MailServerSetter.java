/*
 * Copyright (C) 2023 ScratchLog contributors
 *
 * This file is part of ScratchLog.
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ScratchLog. If not, see <http://www.gnu.org/licenses/>.
 */
package fim.unipassau.de.scratchLog;

import fim.unipassau.de.scratchLog.util.ApplicationProperties;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

/**
 * Utility class to set the mail server constants in the {@link fim.unipassau.de.scratchLog.util.ApplicationProperties}
 * class for testing.
 */
public final class MailServerSetter {

    public static void setMailServer(boolean isMailServer) {
        Mockito.mock(ApplicationProperties.class);
        Whitebox.setInternalState(ApplicationProperties.class, "MAIL_SERVER", isMailServer);
    }
}
