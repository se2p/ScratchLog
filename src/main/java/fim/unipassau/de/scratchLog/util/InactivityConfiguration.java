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

package fim.unipassau.de.scratchLog.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

@Component
@ApplicationScope
@Getter
public class InactivityConfiguration {

    /**
     * The maximum number of days since last login time for participants before their account is deactivated.
     */
    private final int disableInactiveAccountAfterDays;

    /**
     * The maximum number of days during which no user starts or finishes an experiment before it is deactivated.
     */
    private final int disableInactiveExperimentAfterDays;

    /**
     * The maximum number of days a course can be inactive before it is deactivated.
     */
    private final int disableInactiveCourseAfterDays;

    /**
     * Builds a nwe inactivity configuration.
     *
     * @param accountInactivity The number of days after which inactive accounts will be disabled.
     * @param experimentInactivity The number of days after which inactive experiments will be disabled.
     * @param courseInactivity The number of days after which inactive courses will be disabled.
     */
    @Autowired
    public InactivityConfiguration(
        @Value("${scratchlog.inactivity.disable-after-days.account:30}") final int accountInactivity,
        @Value("${scratchlog.inactivity.disable-after-days.experiment:90}") final int experimentInactivity,
        @Value("${scratchlog.inactivity.disable-after-days.course:180}") final int courseInactivity
    ) {
        this.disableInactiveAccountAfterDays = accountInactivity;
        this.disableInactiveExperimentAfterDays = experimentInactivity;
        this.disableInactiveCourseAfterDays = courseInactivity;
    }

    /**
     * Builds the String representation of this class.
     *
     * @return The class in string representation.
     */
    @Override
    public String toString() {
        return "InactivityConfiguration{"
            + "disableInactiveAccountAfterDays=" + disableInactiveAccountAfterDays
            + ", disableInactiveExperimentAfterDays=" + disableInactiveExperimentAfterDays
            + ", disableInactiveCourseAfterDays=" + disableInactiveCourseAfterDays
            + '}';
    }

}
