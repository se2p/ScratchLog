package de.uni_passau.fim.se2.scratchlog.persistence.repository;

/**
 * Represents a Scratch project JSON.
 *
 * @param id The context-specific ID. Might be the user ID of the user that
 *           created the project, or the ID of the event that resulted int this
 *           project.
 * @param projectJson The project JSON.
 */
public record Project(int id, String projectJson) {
}
