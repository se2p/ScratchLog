import {openModal, closeModal} from "./modals.js";

let stopModal = document.getElementById("openStop");
let stopButton = document.getElementById("close");
let abortStop = document.getElementById("abortStop");

let addExperimentModal = document.getElementById("openAddExperiment");
let addExperimentButton = document.getElementById("addExperiment");
let abortAddExperiment = document.getElementById("abortAddExperiment");

let deleteExperimentModal = document.getElementById("openDeleteExperiment");
let deleteExperimentButton = document.getElementById("deleteExperiment");
let abortDeleteExperiment = document.getElementById("abortDeleteExperiment");

/**
 * Configures the stop course button on the course page to open the stop course modal when the button is clicked.
 */
stopButton.onclick = function() {
    openModal(stopModal);
}

/**
 * Configures the abort stop button in the stop course modal to close the modal when the button is clicked.
 */
abortStop.onclick = function() {
    closeModal(stopModal);
}

/**
 * Configures the add experiment button on the course page to open the add experiment modal when the button is clicked.
 */
addExperimentButton.onclick = function() {
    openModal(addExperimentModal);
}

/**
 * Configures the abort add experiment button in the add experiment modal to close the modal when the button is clicked.
 */
abortAddExperiment.onclick = function() {
    closeModal(addExperimentModal);
}

/**
 * Configures the delete experiment button on the course page to open the delete experiment modal when it is clicked.
 */
deleteExperimentButton.onclick = function() {
    openModal(deleteExperimentModal);
}

/**
 * Configures the abort delete experiment button in the delete experiment modal to close the modal when it is clicked.
 */
abortDeleteExperiment.onclick = function() {
    closeModal(deleteExperimentModal);
}
