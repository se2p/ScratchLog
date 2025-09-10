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

package de.uni_passau.fim.se2.scratchlog.application.service;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.ProgramBugAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.ProgramMetricAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.metric.MetricResult;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.ImageMetadata;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ParentVisitor;
import de.uni_passau.fim.se2.scratchlog.application.exception.NotFoundException;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.BlockEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.ClickEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Experiment;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.Participant;
import de.uni_passau.fim.se2.scratchlog.persistence.entity.ResourceEvent;
import de.uni_passau.fim.se2.scratchlog.persistence.projection.BlockEventJSONProjection;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.BlockEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ClickEventRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ExperimentRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ParticipantRepository;
import de.uni_passau.fim.se2.scratchlog.persistence.repository.ResourceEventRepository;
import de.uni_passau.fim.se2.scratchlog.util.Constants;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A service providing methods for retrieving data saved during experiments to be made available for download.
 */
@Service
public class ExperimentDataService {

    /**
     * The log instance associated with this class for logging purposes.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentDataService.class);

    /**
     * A parser for Scratch programs.
     */
    private final Scratch3Parser scratch3Parser = new Scratch3Parser();

    /**
     * The block event repository to use for block event queries.
     */
    private final BlockEventRepository blockEventRepository;

    /**
     * The click event repository to use for click event queries.
     */
    private final ClickEventRepository clickEventRepository;

    /**
     * The resource event repository to use for resource event queries.
     */
    private final ResourceEventRepository resourceEventRepository;

    /**
     * The experiment repository to use for experiment queries.
     */
    private final ExperimentRepository experimentRepository;

    /**
     * The participant repository to use for participation queries.
     */
    private final ParticipantRepository participantRepository;

    /**
     * String used to search for bug patterns in JSON code using LitterBox.
     */
    private static final String BUGS = "bugs";

    /**
     * String used to search for code smells in JSON code using LitterBox.
     */
    private static final String SMELLS = "smells";

    /**
     * String used to search for code perfumes in JSON code using LitterBox.
     */
    private static final String PERFUMES = "perfumes";

    /**
     * Constructs an event service with the given dependencies.
     *
     * @param blockEventRepository The {@link BlockEventRepository} to use.
     * @param clickEventRepository The {@link ClickEventRepository} to use.
     * @param resourceEventRepository The {@link ResourceEventRepository} to use.
     * @param experimentRepository The {@link ExperimentRepository} to use.
     * @param participantRepository The {@link ParticipantRepository} to use.
     */
    @Autowired
    public ExperimentDataService(final BlockEventRepository blockEventRepository,
                                 final ClickEventRepository clickEventRepository,
                                 final ResourceEventRepository resourceEventRepository,
                                 final ExperimentRepository experimentRepository,
                                 final ParticipantRepository participantRepository) {
        this.blockEventRepository = blockEventRepository;
        this.clickEventRepository = clickEventRepository;
        this.resourceEventRepository = resourceEventRepository;
        this.experimentRepository = experimentRepository;
        this.participantRepository = participantRepository;
    }

    /**
     * Retrieves all block, click and resource events that occurred during the experiment with the given id. The
     * retrieved events are converted to a list of string arrays containing all information about the events in a fixed
     * format.
     *
     * @param id The experiment ID.
     * @return A list of string arrays containing information about all events.
     */
    public List<String[]> getEventData(final int id) {
        Experiment experiment = experimentRepository.getReferenceById(id);

        try {
            List<BlockEvent> blockEvents = blockEventRepository.findAllByExperiment(experiment);
            List<ClickEvent> clickEvents = clickEventRepository.findAllByExperiment(experiment);
            List<ResourceEvent> resourceEvents = resourceEventRepository.findAllByExperiment(experiment);
            return createEventList(blockEvents, clickEvents, resourceEvents);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find experiment with id {} in the database!", id, e);
            throw new NotFoundException("Could not find experiment with id " + id + " in the database!", e);
        }
    }

    /**
     * Computes the number of bug patterns, code smells and perfumes for every JSON code present in the list.
     *
     * @param jsons The JSON codes to analyze.
     * @return The number of bug patterns, code smells and perfumes for each JSON.
     */
    public List<List<Integer>> getAnalyzedProgramDataCount(final List<BlockEventJSONProjection> jsons) {
        if (jsons.isEmpty()) {
            throw new IllegalArgumentException("Cannot analyze empty list of Scratch programs!");
        }

        List<Integer> bugs = new ArrayList<>();
        List<Integer> smells = new ArrayList<>();
        List<Integer> perfumes = new ArrayList<>();
        List<List<Integer>> results = new ArrayList<>();
        jsons.forEach(json -> addAnalyzedBugDataCount(bugs, smells, perfumes, json.getCode()));
        results.add(bugs);
        results.add(smells);
        results.add(perfumes);
        return results;
    }

    /**
     * Analyzes all Scratch codes saved for participants during the experiment with the given id using LitterBox. The
     * codes are checked for bug patterns, code smells and perfumes. Additionally, code metrics are calculated.
     *
     * @param id The experiment ID.
     * @return The analysis results.
     * @throws IllegalArgumentException if the passed id is invalid.
     * @throws NotFoundException if no corresponding experiment could be found.
     */
    public List<String[]> getLitterBoxAnalysisResults(final int id) {
        Experiment experiment = experimentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Could not find experiment with id " + id + " in the database!"));

        List<Participant> participants = participantRepository.findAllByExperiment(experiment);
        List<String[]> issues = new ArrayList<>();
        List<String[]> metrics = new ArrayList<>();
        issues.add(new String[]{"user", "issue id", "finder name", "translated finder name", "issue type",
                "severity", "actor name", "location", "hint", "costumes", "current costumes", "json", "timestamp"});
        participants.forEach(participant -> analyzeCodesForUser(participant, issues, metrics));
        issues.addAll(metrics);
        return issues;
    }

    /**
     * Takes the given block, click and resource events and adds the contained information in a fixed format as string
     * arrays to a list. An additional string entry is added to indicate from which table, i.e. block_event, click_event
     * or resource_event, the specific string array originated.
     *
     * @param blockEvents The block events whose information should be extracted.
     * @param clickEvents The click events whose information should be extracted.
     * @param resourceEvents The resource events whose information should be extracted.
     * @return A list of string arrays containing all the information of the given events.
     */
    private List<String[]> createEventList(final List<BlockEvent> blockEvents, final List<ClickEvent> clickEvents,
                                           final List<ResourceEvent> resourceEvents) {
        List<String[]> events = new ArrayList<>();
        String[] header = {"id", "user", "username", "experiment", "date", "eventType", "event", "spritename",
                "metadata", "xml", "json", "name", "md5", "filetype", "library", "table"};
        events.add(header);
        addBlockEventsToList(events, blockEvents);
        addClickEventsToList(events, clickEvents);
        addResourceEventsToList(events, resourceEvents);
        return events;
    }


    /**
     * Adds the information contained in the given block events to the passed list.
     *
     * @param events The list to which the information should be added.
     * @param blockEvents The block events.
     */
    private void addBlockEventsToList(final List<String[]> events, final List<BlockEvent> blockEvents) {
        for (BlockEvent blockEvent : blockEvents) {
            String[] data = {blockEvent.getId().toString(), blockEvent.getUser().getId().toString(),
                    blockEvent.getUser().getUsername(), blockEvent.getExperiment().getId().toString(),
                    blockEvent.getDate().toString(), blockEvent.getEventType().toString(),
                    blockEvent.getEvent().toString(), blockEvent.getSprite(), blockEvent.getMetadata(),
                    blockEvent.getXml(), blockEvent.getCode(), null, null, null, null, "block_event"};
            events.add(data);
        }
    }

    /**
     * Adds the information contained in the given click events to the passed list.
     *
     * @param events The list to which the information should be added.
     * @param clickEvents The click events.
     */
    private void addClickEventsToList(final List<String[]> events, final List<ClickEvent> clickEvents) {
        for (ClickEvent clickEvent : clickEvents) {
            String[] data = {clickEvent.getId().toString(), clickEvent.getUser().getId().toString(),
                    clickEvent.getUser().getUsername(), clickEvent.getExperiment().getId().toString(),
                    clickEvent.getDate().toString(), clickEvent.getEventType().toString(),
                    clickEvent.getEvent().toString(), null, clickEvent.getMetadata(), null, null, null, null, null,
                    null, "click_event"};
            events.add(data);
        }
    }

    /**
     * Adds the information contained in the given resource events to the passed list.
     *
     * @param events The list to which the information should be added.
     * @param resourceEvents The resource events.
     */
    private void addResourceEventsToList(final List<String[]> events, final List<ResourceEvent> resourceEvents) {
        for (ResourceEvent resourceEvent : resourceEvents) {
            String[] data = {resourceEvent.getId().toString(), resourceEvent.getUser().getId().toString(),
                    resourceEvent.getUser().getUsername(), resourceEvent.getExperiment().getId().toString(),
                    resourceEvent.getDate().toString(), resourceEvent.getEventType().toString(),
                    resourceEvent.getEvent().toString(), null, null, null, null, resourceEvent.getResourceName(),
                    resourceEvent.getHash(), resourceEvent.getResourceType(), resourceEvent.getLibraryResource() == null
                    ? null : resourceEvent.getLibraryResource().toString(), "resource_event"};
            events.add(data);
        }
    }

    /**
     * Analyzes the given JSON code using LitterBox and adds the number of bug patterns, code smells and perfumes found
     * to the respective list.
     *
     * @param bugs The list used for storing the number of bug patterns.
     * @param smells The list used for storing the number of code smells.
     * @param perfumes The list used for storing the number of perfumes.
     * @param json The JSON code to analyze.
     * @throws RuntimeException if the JSON could not be parsed correctly.
     */
    private void addAnalyzedBugDataCount(final List<Integer> bugs, final List<Integer> smells,
                                         final List<Integer> perfumes, final String json) {
        try {
            Program program = getProgram(json);
            Map<String, Set<Issue>> results = getProgramIssues(program);
            bugs.add(results.get(BUGS).size());
            smells.add(results.get(SMELLS).size());
            perfumes.add(results.get(PERFUMES).size());
        } catch (ParsingException e) {
            throw new RuntimeException("Failed to parse JSON code when trying to compute bug counts!", e);
        }
    }

    /**
     * Analyzes all JSON codes saved for the given participant with LitterBox and adds the found issues and metric
     * results to the respective list. If more than a certain number of codes has been saved for the participant, only
     * the first codes are analyzed to limit computation time.
     *
     * @param participant The participant whose codes should be analyzed.
     * @param issues The list used to store found issues, i.e. bug patterns, code smells and perfumes.
     * @param metrics The list used to store code metric results.
     */
    private void analyzeCodesForUser(final Participant participant, final List<String[]> issues,
                                     final List<String[]> metrics) {
        List<BlockEventJSONProjection> projections =
                blockEventRepository.findAllByCodeIsNotNullAndUserAndExperimentOrderByDateAsc(participant.getUser(),
                        participant.getExperiment());

        if (projections.size() > Constants.MAX_DATA_POINTS) {
            projections = projections.subList(0, Constants.MAX_DATA_POINTS);
        }

        projections.forEach(projection -> addAnalysisResults(issues, metrics, participant.getUser().getUsername(),
                projection.getCode(), projection.getDate()));
    }

    /**
     * Adds information on bug patterns, code smells and perfumes discovered in the given Scratch code as well as
     * computed code metrics to the respective list.
     *
     * @param issues The list used to store issue information, i.e. bug patterns, code smells and perfumes.
     * @param metrics The list used to store code metric information.
     * @param username The name of the user whose program should be analyzed.
     * @param json The Scratch code to be analyzed.
     * @param time The date and time at which the Scratch code was created.
     * @throws RuntimeException if the given code could not be parsed correctly.
     */
    private void addAnalysisResults(final List<String[]> issues, final List<String[]> metrics, final String username,
                                    final String json, final LocalDateTime time) {
        try {
            Program program = getProgram(json);
            Map<String, Set<Issue>> results = getProgramIssues(program);
            results.get(BUGS).forEach(issue -> addIssueData(issues, issue, username, json, time));
            results.get(SMELLS).forEach(issue -> addIssueData(issues, issue, username, json, time));
            results.get(PERFUMES).forEach(issue -> addIssueData(issues, issue, username, json, time));
            addMetricData(program, metrics, username, json, time);
        } catch (ParsingException e) {
            throw new RuntimeException("Failed to parse JSON code for analysis!", e);
        }
    }

    /**
     * Creates a LitterBox program from the given Scratch JSON code for further analysis.
     *
     * @param json The string be parsed.
     * @return The parsed program.
     * @throws ParsingException if LitterBox failed to parse the JSON.
     */
    private Program getProgram(final String json) throws ParsingException {
        Program program = scratch3Parser.parseString("json", json);
        program.accept(new ParentVisitor());
        return program;
    }

    /**
     * Analyzes the given LitterBox program for bug patterns, code smells and perfumes and returns the results in a map.
     *
     * @param program The program to analyze.
     * @return The analysis results.
     */
    private Map<String, Set<Issue>> getProgramIssues(final Program program) {
        Map<String, Set<Issue>> results = new HashMap<>();
        ProgramBugAnalyzer bugAnalyzer = new ProgramBugAnalyzer(BUGS, false);
        ProgramBugAnalyzer smellsAnalyzer = new ProgramBugAnalyzer(SMELLS, false);
        ProgramBugAnalyzer perfumesAnalyzer = new ProgramBugAnalyzer(PERFUMES, false);
        results.put(BUGS, bugAnalyzer.analyze(program));
        results.put(SMELLS, smellsAnalyzer.analyze(program));
        results.put(PERFUMES, perfumesAnalyzer.analyze(program));
        return results;
    }

    /**
     * Adds relevant information from the given LitterBox issue, i.e. a bug pattern, code smell or perfume, to the
     * passed list.
     *
     * @param issues The list used to store the information.
     * @param issue The LitterBox issue from which information is extracted.
     * @param username The name of the user for whom this issue was created.
     * @param json The string of the Scratch code in which the issue was found.
     * @param time The date and time at which the Scratch code was created.
     */
    private void addIssueData(final List<String[]> issues, final Issue issue, final String username,
                              final String json, final LocalDateTime time) {
        String issueLocation = issue.getCodeLocation() == null ? null : AstNodeUtil.getBlockId(issue.getCodeLocation());
        List<String> costumes = issue.getActor().getActorMetadata().getCostumes().getList().stream()
                .map(ImageMetadata::getAssetId).toList();
        issues.add(new String[]{username, String.valueOf(issue.getId()), issue.getFinderName(),
                issue.getTranslatedFinderName(), issue.getIssueType().name(), String.valueOf(
                issue.getSeverity().getSeverityLevel()), issue.getActorName(), issueLocation, issue.getHint(),
                String.valueOf(costumes), String.valueOf(issue.getActor().getActorMetadata().getCurrentCostume()),
                json, String.valueOf(time)});
    }

    /**
     * Computes all code metrics supported by LitterBox for the given program and adds the results to the given list.
     *
     * @param program The program to analyze.
     * @param metrics The list used for storing results.
     * @param username The name of the user whose Scratch code is being analyzed.
     * @param json The corresponding Scratch code as a string.
     * @param time The date and time at which the Scratch code was created.
     */
    private void addMetricData(final Program program, final List<String[]> metrics, final String username,
                               final String json, final LocalDateTime time) {
        List<MetricResult> metricResults = getProgramMetrics(program);

        if (metrics.isEmpty()) {
            List<String> header = new ArrayList<>();
            header.add("user");
            header.add("json");
            header.add("timestamp");
            metricResults.forEach(metric -> header.add(metric.name()));
            metrics.add(header.toArray(String[]::new));
        }

        List<String> results = new ArrayList<>();
        results.add(username);
        results.add(json);
        results.add(String.valueOf(time));
        metricResults.forEach(metric -> results.add(String.valueOf(metric.value())));
        metrics.add(results.toArray(String[]::new));
    }

    /**
     * Analyzes the given LitterBox program using different code metrics.
     *
     * @param program The program to analyze.
     * @return A list containing results for each computed metric.
     */
    private List<MetricResult> getProgramMetrics(final Program program) {
        ProgramMetricAnalyzer metricAnalyzer = new ProgramMetricAnalyzer();
        return metricAnalyzer.analyze(program);
    }

}
