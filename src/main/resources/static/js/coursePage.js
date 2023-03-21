import {redirectErrorPage} from "./errorRedirect.js";
import {
    addClickEventListener,
    addClickEventListenerCloseModal,
    addClickEventListenerOpenModal
} from "./eventListeners.js";

/**
 * Readies all necessary event listeners for buttons on the course page.
 */
$(document).ready(function () {
    addEventListeners();
});

/**
 * Loads the next course participant page and updates the course participant table with the new data.
 */
function loadNextCourseParticipantPage() {
    const PAGE = participantPage + 1;
    updateLastCourseParticipantPage();
    loadCourseParticipantPage(PAGE);
}

/**
 * Loads the previous course participant page and updates the course participant table with the new data.
 */
function loadPreviousCourseParticipantPage() {
    const PAGE = participantPage - 1;
    updateLastCourseParticipantPage();
    loadCourseParticipantPage(PAGE);
}

/**
 * Loads the first course participant page and updates the course participant table with the new data.
 */
function loadFirstCourseParticipantPage() {
    updateLastCourseParticipantPage();
    loadCourseParticipantPage(0);
}

/**
 * Loads the last course participant page and updates the course participant table with the new data.
 */
function loadLastCourseParticipantPage() {
    updateLastCourseParticipantPage();
    loadCourseParticipantPage(lastParticipantPage);
}

/**
 * Loads the next course experiment page and updates the course experiment table with the new data.
 */
function loadNextCourseExperimentPage() {
    const PAGE = experimentPage + 1;
    updateLastCourseExperimentPage();
    loadCourseExperimentPage(PAGE);
}

/**
 * Loads the previous course experiment page and updates the course experiment table with the new data.
 */
function loadPreviousCourseExperimentPage() {
    const PAGE = experimentPage - 1;
    updateLastCourseExperimentPage();
    loadCourseExperimentPage(PAGE);
}

/**
 * Loads the first course experiment page and updates the course experiment table with the new data.
 */
function loadFirstCourseExperimentPage() {
    updateLastCourseExperimentPage();
    loadCourseExperimentPage(0);
}

/**
 * Loads the last course experiment page and updates the course experiment table with the new data.
 */
function loadLastCourseExperimentPage() {
    updateLastCourseExperimentPage();
    loadCourseExperimentPage(lastExperimentPage);
}

/**
 * Loads the course participant page for the given page number from the database and updates the corresponding table.
 *
 * @param page The page to be retrieved.
 */
function loadCourseParticipantPage(page) {
    $.ajax({
        type: 'get',
        url: contextPath + "/course/page/participant",
        data: {id: courseId, page: page},
        success: function(data) {
            updateCourseParticipantTable(data);
            participantPage = page;
        },
        error: function() {
            redirectErrorPage();
        }
    });
}

/**
 * Retrieves the page number of the last course participant page from the database.
 */
function updateLastCourseParticipantPage() {
    $.ajax({
        type: 'get',
        url: contextPath + "/pages/course/participant",
        data: {id: courseId},
        success: function(data) {
            lastParticipantPage = data;
        },
        error: function() {
            redirectErrorPage();
        }
    });
}

/**
 * Updates the information shown in the course participant table with the given data and adds the required event
 * listeners.
 */
function updateCourseParticipantTable(data) {
    $('#course_participant_table').html(data);
    addEventListeners();
}

/**
 * Loads the course experiment page for the given page number from the database and updates the corresponding table.
 *
 * @param page The page to be retrieved.
 */
function loadCourseExperimentPage(page) {
    $.ajax({
        type: 'get',
        url: contextPath + "/course/page/experiment",
        data: {id: courseId, page: page},
        success: function(data) {
            updateCourseExperimentTable(data);
            experimentPage = page;
        },
        error: function() {
            redirectErrorPage();
        }
    });
}

/**
 * Retrieves the page number of the last course experiment page from the database.
 */
function updateLastCourseExperimentPage() {
    $.ajax({
        type: 'get',
        url: contextPath + "/pages/course/experiment",
        data: {id: courseId},
        success: function(data) {
            lastExperimentPage = data;
        },
        error: function() {
            redirectErrorPage();
        }
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
    let deleteModal = document.getElementById("openDelete");
    let addParticipantModal = document.getElementById("openAddParticipant");
    let deleteParticipantModal = document.getElementById("openDeleteParticipant");
    let deleteExperimentModal = document.getElementById("openDeleteExperiment");

    addClickEventListenerOpenModal("close", stopModal);
    addClickEventListenerCloseModal("abortStop", stopModal);
    addClickEventListenerOpenModal("delete", deleteModal);
    addClickEventListenerCloseModal("abortDelete", deleteModal);
    addClickEventListenerOpenModal("addParticipant", addParticipantModal);
    addClickEventListenerCloseModal("abortAddParticipant", addParticipantModal);
    addClickEventListenerOpenModal("deleteParticipant", deleteParticipantModal);
    addClickEventListenerCloseModal("abortDeleteParticipant", deleteParticipantModal);
    addClickEventListenerOpenModal("deleteExperiment", deleteExperimentModal);
    addClickEventListenerCloseModal("abortDeleteExperiment", deleteExperimentModal);
}

/**
 * Adds the event listeners to handle user clicks on the buttons of the course page.
 */
function addEventListeners() {
    addModalOnclickFunctions();
    addKeyupFunctions();
    addClickEventListener("participantsNext", loadNextCourseParticipantPage);
    addClickEventListener("participantsPrev", loadPreviousCourseParticipantPage);
    addClickEventListener("participantsFirst", loadFirstCourseParticipantPage);
    addClickEventListener("participantsLast", loadLastCourseParticipantPage);
    addClickEventListener("experimentsNext", loadNextCourseExperimentPage);
    addClickEventListener("experimentsPrev", loadPreviousCourseExperimentPage);
    addClickEventListener("experimentsFirst", loadFirstCourseExperimentPage);
    addClickEventListener("experimentsLast", loadLastCourseExperimentPage);
}
