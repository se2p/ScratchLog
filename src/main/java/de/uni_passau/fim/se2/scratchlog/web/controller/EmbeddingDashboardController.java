package de.uni_passau.fim.se2.scratchlog.web.controller;

import de.uni_passau.fim.se2.scratchlog.application.service.ParticipantService;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/embedding-dashboard")
public class EmbeddingDashboardController {

    private final ParticipantService participantService;

    @Autowired
    public EmbeddingDashboardController(final ParticipantService participantService) {
        this.participantService = participantService;
    }

    /**
     * Model init for the embedding dashboard page.
     *
     * @param experimentId The experiment id.
     * @param model The view model.
     * @return Redirect to the embedding dashboard page.
     */
    @GetMapping("")
    @Secured(Constants.ROLE_ADMIN)
    public String getDashboard(@RequestParam("id") final int experimentId, final Model model) {
        model.addAttribute("experiment", experimentId);
        model.addAttribute("participants", participantService.getParticipantNames(experimentId));

        return "embedding-dashboard";
    }
}
