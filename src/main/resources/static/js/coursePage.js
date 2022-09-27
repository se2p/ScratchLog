import {closeModal, openModal} from "./modals.js";

/**
 * Readies all necessary event listeners for buttons on the course page.
 */
$(document).ready(function () {
    addEventListeners();
});

/**
 * Loads the next course experiment page and updates the course experiment table with the new data.
 */
function loadNextCourseExperimentPage() {
    $.ajax({
        type: 'get',
        url: contextPath + "/course/next/experiment",
        data: {id: courseId, lastPage: lastExperimentPage, page: experimentPage},
        success: function(data) {
            updateCourseExperimentTable(data);
            experimentPage++;
        },
    });
}

/**
 * Loads the previous course experiment page and updates the course experiment table with the new data.
 */
function loadPreviousCourseExperimentPage() {
    $.ajax({
        type: 'get',
        url: contextPath + "/course/previous/experiment",
        data: {id: courseId, lastPage: lastExperimentPage, page: experimentPage},
        success: function(data) {
            updateCourseExperimentTable(data);
            experimentPage--;
        },
    });
}

/**
 * Loads the first course experiment page and updates the course experiment table with the new data.
 */
function loadFirstCourseExperimentPage() {
    $.ajax({
        type: 'get',
        url: contextPath + "/course/first/experiment",
        data: {id: courseId, lastPage: lastExperimentPage},
        success: function(data) {
            updateCourseExperimentTable(data);
            experimentPage = 1;
        },
    });
}

/**
 * Loads the last course experiment page and updates the course experiment table with the new data.
 */
function loadLastCourseExperimentPage() {
    $.ajax({
        type: 'get',
        url: contextPath + "/course/last/experiment",
        data: {id: courseId, lastPage: lastExperimentPage},
        success: function(data) {
            updateCourseExperimentTable(data);
            experimentPage = lastExperimentPage;
        },
    });
}

/**
 * Updates the information shown in the course experiment table with the given data and adds the required event
 * listeners.
 */
function updateCourseExperimentTable(data) {
    $('#course_experiment_table').html(data);
    addEventListeners();
}

/**
 * Adds the event listeners for opening and closing all modals on the course page.
 */
function addModalOnclickFunctions() {
    let stopModal = document.getElementById("openStop");
    let addParticipantModal = document.getElementById("openAddParticipant");
    let deleteParticipantModal = document.getElementById("openDeleteParticipant");
    let addExperimentModal = document.getElementById("openAddExperiment");
    let deleteExperimentModal = document.getElementById("openDeleteExperiment");

    document.getElementById("close").addEventListener("click", function () {
        openModal(stopModal);
    });
    document.getElementById("abortStop").addEventListener("click", function () {
        closeModal(stopModal);
    })
    document.getElementById("addParticipant").addEventListener("click", function () {
        openModal(addParticipantModal);
    });
    document.getElementById("abortAddParticipant").addEventListener("click", function () {
        closeModal(addParticipantModal);
    });
    document.getElementById("deleteParticipant").addEventListener("click", function () {
        openModal(deleteParticipantModal);
    });
    document.getElementById("abortDeleteParticipant").addEventListener("click", function () {
        closeModal(deleteParticipantModal);
    });
    document.getElementById("addExperiment").addEventListener("click", function () {
        openModal(addExperimentModal);
    });
    document.getElementById("abortAddExperiment").addEventListener("click", function () {
        closeModal(addExperimentModal);
    });
    document.getElementById("deleteExperiment").addEventListener("click", function () {
       openModal(deleteExperimentModal);
    });
    document.getElementById("abortDeleteExperiment").addEventListener("click", function () {
        closeModal(deleteExperimentModal);
    });
}

/**
 * Adds the event listeners to handle user clicks on the buttons of the course page.
 */
function addEventListeners() {
    document.getElementById("experimentsNext").addEventListener("click", loadNextCourseExperimentPage);
    document.getElementById("experimentsPrev").addEventListener("click", loadPreviousCourseExperimentPage);
    document.getElementById("experimentsFirst").addEventListener("click", loadFirstCourseExperimentPage);
    document.getElementById("experimentsLast").addEventListener("click", loadLastCourseExperimentPage);
    addModalOnclickFunctions();
    addKeyupFunctions();
}
