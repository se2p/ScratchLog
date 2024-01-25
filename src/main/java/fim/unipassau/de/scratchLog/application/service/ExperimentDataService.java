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

package fim.unipassau.de.scratchLog.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import de.uni_passau.fim.se2.litterbox.analytics.BugAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.MetricAnalyzer;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ParentVisitor;
import fim.unipassau.de.scratchLog.application.exception.NotFoundException;
import fim.unipassau.de.scratchLog.persistence.entity.BlockEvent;
import fim.unipassau.de.scratchLog.persistence.entity.ClickEvent;
import fim.unipassau.de.scratchLog.persistence.entity.Experiment;
import fim.unipassau.de.scratchLog.persistence.entity.ResourceEvent;
import fim.unipassau.de.scratchLog.persistence.projection.BlockEventJSONProjection;
import fim.unipassau.de.scratchLog.persistence.repository.BlockEventRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ClickEventRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ExperimentRepository;
import fim.unipassau.de.scratchLog.persistence.repository.ResourceEventRepository;
import fim.unipassau.de.scratchLog.util.Constants;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * String used to analyze JSON code with different metrics using LitterBox.
     */
    private static final String METRICS = "metrics";

    /**
     * Constructs an event service with the given dependencies.
     *
     * @param blockEventRepository The {@link BlockEventRepository} to use.
     * @param clickEventRepository The {@link ClickEventRepository} to use.
     * @param resourceEventRepository The {@link ResourceEventRepository} to use.
     * @param experimentRepository The {@link ExperimentRepository} to use.
     */
    @Autowired
    public ExperimentDataService(final BlockEventRepository blockEventRepository,
                                 final ClickEventRepository clickEventRepository,
                                 final ResourceEventRepository resourceEventRepository,
                                 final ExperimentRepository experimentRepository) {
        this.blockEventRepository = blockEventRepository;
        this.clickEventRepository = clickEventRepository;
        this.resourceEventRepository = resourceEventRepository;
        this.experimentRepository = experimentRepository;
    }

    /**
     * Retrieves all block, click and resource events that occurred during the experiment with the given id. The
     * retrieved events are converted to a list of string arrays containing all information about the events in a fixed
     * format.
     *
     * @param id The experiment ID.
     * @return A list of string arrays containing information about all events.
     */
    @Transactional
    public List<String[]> getEventData(final int id) {
        if (id < Constants.MIN_ID) {
            throw new IllegalArgumentException("Cannot retrieve event data for experiment with invalid id " + id + "!");
        }

        Experiment experiment = experimentRepository.getReferenceById(id);

        try {
            List<BlockEvent> blockEvents = blockEventRepository.findAllByExperiment(experiment);
            List<ClickEvent> clickEvents = clickEventRepository.findAllByExperiment(experiment);
            List<ResourceEvent> resourceEvents = resourceEventRepository.findAllByExperiment(experiment);
            return createEventList(blockEvents, clickEvents, resourceEvents);
        } catch (EntityNotFoundException e) {
            LOGGER.error("Could not find experiment with id " + id + " in the database!", e);
            throw new NotFoundException("Could not find experiment with id " + id + " in the database!", e);
        }
    }

    /**
     * Computes the number of bug patterns, code smells and perfumes for every JSON code present in the list.
     *
     * @param jsons The JSON codes to analyze.
     * @return The number of bug patterns, code smells and perfumes for each JSON.
     */
    @Transactional
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
            Map<String, Set<?>> results = analyzeJSON(json, false);
            bugs.add(results.get(BUGS).size());
            smells.add(results.get(SMELLS).size());
            perfumes.add(results.get(PERFUMES).size());
        } catch (ParsingException | JsonProcessingException e) {
            throw new RuntimeException("Failed to analyze JSON code when trying to compute bug counts!", e);
        }
    }

    /**
     * Analyzes the given JSON code for bug patterns, code smells, perfumes and metrics and returns the results in a
     * map.
     *
     * @param json The JSON code to analyze.
     * @param analyzeMetrics Boolean indicating whether the code metrics should be computed.
     * @return The analysis results.
     * @throws JsonProcessingException if the JSON code could not be parsed correctly.
     * @throws ParsingException if LitterBox failed to parse the JSON.
     */
    private Map<String, Set<?>> analyzeJSON(final String json, final boolean analyzeMetrics)
            throws JsonProcessingException, ParsingException {
        Map<String, Set<?>> results = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        BugAnalyzer bugAnalyzer = new BugAnalyzer(null, null, BUGS, false, false, false);
        BugAnalyzer smellsAnalyzer = new BugAnalyzer(null, null, SMELLS, false, false, false);
        BugAnalyzer perfumesAnalyzer = new BugAnalyzer(null, null, PERFUMES, false, false, false);
        Program program = ProgramParser.parseProgram("json", rootNode);
        program.accept(new ParentVisitor());
        results.put(BUGS, bugAnalyzer.check(program));
        results.put(SMELLS, smellsAnalyzer.check(program));
        results.put(PERFUMES, perfumesAnalyzer.check(program));

        if (analyzeMetrics) {
            MetricAnalyzer metricAnalyzer = new MetricAnalyzer(null, null, false);
            results.put(METRICS, Sets.newHashSet(metricAnalyzer.check(program)));
        }

        return results;
    }

}
