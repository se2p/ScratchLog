package de.uni_passau.fim.se2.scratchlog.persistence.repository;

/**
 * Represents a Scratch project JSON.
 *
 * @param id The ID of the block event that resulted in this project.
 * @param userId The ID of the user that created this project.
 * @param username The name of the user that created this project.
 * @param projectJson The project JSON.
 */
public record Project(int id, int userId, String username, String projectJson) {
}
