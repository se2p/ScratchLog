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

package fim.unipassau.de.scratchLog.spring;

import fim.unipassau.de.scratchLog.application.service.CourseService;
import fim.unipassau.de.scratchLog.application.service.ParticipantService;
import fim.unipassau.de.scratchLog.application.service.UserService;
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
     * The participant service to use for participant management.
     */
    private final ParticipantService participantService;

    /**
     * The course service to use for course management.
     */
    private final CourseService courseService;

    /**
     * The interval in milliseconds until the next scheduled task invocation.
     */
    private static final int INTERVAL = 86400000;

    /**
     * Constructs a new deactivation handler with the given dependencies.
     *
     * @param userService The {@link UserService} to use.
     * @param participantService The {@link ParticipantService} to use.
     * @param courseService The {@link CourseService} to use.
     */
    public DeactivationHandler(final UserService userService, final ParticipantService participantService,
                               final CourseService courseService) {
        this.userService = userService;
        this.participantService = participantService;
        this.courseService = courseService;
    }

    /**
     * Task scheduled to run once a day to deactivate old participant accounts, experiments and courses.
     */
    @Scheduled(fixedRate = INTERVAL)
    public void deactivateInactiveEntities() {
        LOGGER.info("Starting scheduled task to deactivate old participant accounts, experiments and courses.");
        userService.deactivateOldParticipantAccounts();
        participantService.deactivateInactiveExperiments();
        courseService.deactivateInactiveCourses();
    }

}
