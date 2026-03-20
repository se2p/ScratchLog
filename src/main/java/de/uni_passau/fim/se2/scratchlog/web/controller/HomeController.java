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

package de.uni_passau.fim.se2.scratchlog.web.controller;

import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.application.service.ExperimentService;
import de.uni_passau.fim.se2.scratchlog.application.service.PageService;
import de.uni_passau.fim.se2.scratchlog.application.service.ParticipantService;
import de.uni_passau.fim.se2.scratchlog.application.service.UserService;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.CourseTableProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.ExperimentTableProjection;
import de.uni_passau.fim.se2.scratchlog.util.ApplicationProperties;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import de.uni_passau.fim.se2.scratchlog.web.dto.ExperimentDTO;
import de.uni_passau.fim.se2.scratchlog.web.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.ResourceBundle;

/**
 * The controller for the homepage of the project.
 */
@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    private final ApplicationProperties applicationProperties;

    private final ExperimentService experimentService;

    private final PageService pageService;

    private final UserService userService;

    private final ParticipantService participantService;

    /**
     * String corresponding to the name of the model attribute containing the current page number or the corresponding
     * request parameter.
     */
    private static final String PAGE = "page";

    /**
     * Constructs a new home controller with the given dependencies.
     *
     * @param applicationProperties The {@link ApplicationProperties} to use.
     * @param experimentService The {@link ExperimentService} to use.
     * @param pageService The {@link PageService} to use.
     * @param userService The {@link UserService} to use.
     * @param participantService The {@link ParticipantService} to use.
     */
    @Autowired
    public HomeController(final ApplicationProperties applicationProperties,
                          final ExperimentService experimentService, final PageService pageService,
                          final UserService userService, final ParticipantService participantService) {
        this.applicationProperties = applicationProperties;
        this.experimentService = experimentService;
        this.pageService = pageService;
        this.userService = userService;
        this.participantService = participantService;
    }

    /**
     * Loads the index page containing basic information about the project. If the user is an administrator, a page
     * containing the latest experiments and courses is loaded instead. If the user is a participant, a page containing
     * the experiments and courses they are participating in is displayed instead.
     *
     * @param httpServletRequest The servlet request.
     * @param model The model to store the loaded information in.
     * @return The index page.
     */
    @GetMapping("/")
    public String getIndexPage(final HttpServletRequest httpServletRequest, final Model model) {
        if (httpServletRequest.isUserInRole(Constants.ROLE_PARTICIPANT)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || authentication.getName() == null) {
                log.error("Can't show the experiment and course information for an unauthenticated user!");
                return Constants.ERROR;
            }

            try {
                UserDTO userDTO = userService.getUser(authentication.getName());
                getInitialIndexPageInfo(userDTO.getId(), httpServletRequest.isUserInRole(Constants.ROLE_ADMIN), model);
            } catch (NotFoundException e) {
                return Constants.ERROR;
            }
        }

        return "index";
    }

    /**
     * Retrieves the course page corresponding to the given page number for the current user, if the provided
     * information is valid. An administrator will get an overview over all courses while participants will only see
     * courses in which they are participating.
     *
     * @param page The number of the page to be retrieved.
     * @param httpServletRequest The {@link HttpServletRequest} containing providing information on the user's role.
     * @return The retrieved course page information.
     */
    @GetMapping("/page/course")
    @Secured(Constants.ROLE_PARTICIPANT)
    public ModelAndView getCoursePage(@RequestParam(PAGE) final int page,
                                      final HttpServletRequest httpServletRequest) {
        UserDTO userDTO = fetchUserInformation();
        if (userDTO == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cannot get page for unauthenticated user.");
        }

        int lastPage;
        Page<CourseTableProjection> projections;
        if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            lastPage = pageService.getLastCoursePage();
            projections = pageService.getCoursePage(page);
        } else {
            lastPage = pageService.getLastCoursePageForUser(userDTO.getId());
            projections = pageService.getCourseParticipantPage(userDTO.getId(), page);
        }

        return getCourseModelView(projections, page, lastPage - 1);
    }

    /**
     * Retrieves the experiment page corresponding to the given page number for the current user if the provided
     * information is valid. An administrator will get an overview over all experiments while participants will only see
     * experiments in which they are participating.
     *
     * @param page The number of the page to be retrieved.
     * @param httpServletRequest The {@link HttpServletRequest} containing providing information on the user's role.
     * @return The retrieved experiment page information.
     */
    @GetMapping("/page/experiment")
    @Secured(Constants.ROLE_PARTICIPANT)
    public ModelAndView getExperimentPage(@RequestParam(PAGE) final int page,
                                          final HttpServletRequest httpServletRequest) {
        UserDTO userDTO = fetchUserInformation();
        if (userDTO == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cannot get page for unauthenticated user.");
        }

        int lastPage;
        Page<ExperimentTableProjection> projections;
        if (httpServletRequest.isUserInRole(Constants.ROLE_ADMIN)) {
            lastPage = pageService.getLastExperimentPage();
            projections = pageService.getExperimentPage(page);
        } else {
            lastPage = pageService.getLastExperimentPageForUser(userDTO.getId());
            projections = pageService.getExperimentParticipantPage(userDTO.getId(), page);
        }

        return getExperimentModelView(projections, page, lastPage - 1);
    }

    /**
     * Loads the login page for user authentication.
     *
     * @param userDTO The {@link UserDTO} user for authentication.
     * @return The login page.
     */
    @GetMapping("/login")
    public String getLoginPage(final UserDTO userDTO) {
        return "login";
    }

    /**
     * Loads the experiment finish page for the experiment with the current id.
     *
     * @param userId The user id of the participant.
     * @param experimentId The experiment id.
     * @param secret The user's secret.
     * @param model The model used to store the message to be displayed on the page.
     * @return The experiment finish page.
     */
    @GetMapping("/finish")
    public String getExperimentFinishPage(@RequestParam("user") final int userId,
                                          @RequestParam("experiment") final int experimentId,
                                          @RequestParam("secret") final String secret,
                                          final Model model) {
        if (isInvalidFinishParams(experimentId, userId, secret)) {
            return Constants.ERROR;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("i18n/messages",
                LocaleContextHolder.getLocale());

        try {
            ExperimentDTO experimentDTO = experimentService.getExperiment(experimentId);
            model.addAttribute("thanks", experimentDTO.getPostscript() != null ? experimentDTO.getPostscript()
                    : resourceBundle.getString("thanks"));
            model.addAttribute("secret", secret);
            model.addAttribute("user", userId);
            model.addAttribute("experiment", experimentId);
        } catch (NotFoundException e) {
            return Constants.ERROR;
        }

        return "experiment-finish";
    }

    /**
     * Loads the password reset page to reset a user password.
     *
     * @param userDTO The {@link UserDTO} user for resetting the password.
     * @return The password reset page.
     */
    @GetMapping("/reset")
    public String getResetPage(final UserDTO userDTO) {
        return applicationProperties.useMail() ? "password-reset" : Constants.ERROR;
    }


    /**
     * Retrieves the experiment and course pages to be displayed for the given user on initial page load. If the user is
     * administrator, information about all courses and experiments is retrieved. If the user is a participant, only
     * information about courses and experiments the user is participating in is retrieved.
     *
     * @param userId The id of the user.
     * @param isAdmin Whether the user is an administrator or not.
     * @param model The model used to store the information.
     */
    private void getInitialIndexPageInfo(final int userId, final boolean isAdmin, final Model model) {
        Page<ExperimentTableProjection> experimentPage;
        Page<CourseTableProjection> coursePage;

        if (isAdmin) {
            experimentPage = pageService.getExperimentPage(0);
            coursePage = pageService.getCoursePage(0);
        } else {
            experimentPage = pageService.getExperimentParticipantPage(userId, 0);
            coursePage = pageService.getCourseParticipantPage(userId, 0);
        }

        int lastExperimentPage = experimentPage.getTotalPages();
        int lastCoursePage = coursePage.getTotalPages();
        addModelInfo(experimentPage, coursePage, 0, 0, lastExperimentPage - 1, lastCoursePage - 1, model);
    }

    /**
     * Adds the required page numbers, experiment and course information to the {@link Model} to display the experiment
     * and course tables on the index page.
     *
     * @param experiments The current experiment page.
     * @param courses The current course page.
     * @param currentExperimentPage The number of the current experiment page.
     * @param currentCoursePage The number of the current course page.
     * @param lastExperimentPage The number of the last experiment page.
     * @param lastCoursePage The number of the last course page.
     * @param model The model used to save the info.
     */
    private void addModelInfo(final Page<ExperimentTableProjection> experiments,
                              final Page<CourseTableProjection> courses, final int currentExperimentPage,
                              final int currentCoursePage, final int lastExperimentPage, final int lastCoursePage,
                              final Model model) {
        model.addAttribute("experiments", experiments);
        model.addAttribute("courses", courses);
        model.addAttribute("experimentPage", currentExperimentPage);
        model.addAttribute("coursePage", currentCoursePage);
        model.addAttribute("lastExperimentPage", lastExperimentPage);
        model.addAttribute("lastCoursePage", lastCoursePage);
    }

    /**
     * Retrieves the user information of the current user. If the {@link Authentication} does not provide sufficient
     * information or no user with a corresponding username could be found, {@code null} is returned instead.
     *
     * @return The {@link UserDTO} containing the user information, or {@code null}.
     */
    private UserDTO fetchUserInformation() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            log.error("Can't show the participant experiment page for an unauthenticated user!");
            return null;
        }

        try {
            return userService.getUser(authentication.getName());
        } catch (NotFoundException e) {
            return null;
        }
    }

    /**
     * Checks if the passed ids and secret are valid parameters and match a participant and user in the database.
     *
     * @param experimentId The id of the experiment.
     * @param userId The id of the user.
     * @param secret The user's secret.
     * @return {@code true} if the passed parameters are invalid or {@code false} otherwise.
     */
    private boolean isInvalidFinishParams(final int experimentId, final int userId, final String secret) {
        if (secret == null || secret.isBlank()) {
            log.error("Cannot finish experiment with secret null or blank!");
            return true;
        } else {
            return participantService.isInvalidParticipant(userId, experimentId, secret, false);
        }
    }

    /**
     * Adds the required information for updating the experiment table on the index page.
     *
     * @param experiments The current experiment page.
     * @param currentPage The current experiment page number.
     * @param lastPage The last experiment page.
     * @return The {@link ModelAndView} used to store the information.
     */
    private ModelAndView getExperimentModelView(final Page<ExperimentTableProjection> experiments,
                                                final int currentPage, final int lastPage) {
        ModelAndView mv = new ModelAndView("index::experiment_table");
        mv.addObject("experiments", experiments);
        mv.addObject("experimentPage", currentPage);
        mv.addObject("lastExperimentPage", lastPage);
        return mv;
    }

    /**
     * Adds the required information for updating the course table on the index page.
     *
     * @param courses The current course page.
     * @param currentPage The current course page number.
     * @param lastPage The last course page.
     * @return The {@link ModelAndView} used to store the information.
     */
    private ModelAndView getCourseModelView(final Page<CourseTableProjection> courses, final int currentPage,
                                            final int lastPage) {
        ModelAndView mv = new ModelAndView("index::course_table");
        mv.addObject("courses", courses);
        mv.addObject("coursePage", currentPage);
        mv.addObject("lastCoursePage", lastPage);
        return mv;
    }

}
