import {redirectErrorPage} from "./errorRedirect.js";

/**
 * Readies the functions to retrieve a different experiment or course page upon clicking the respective buttons.
 */
$(document).ready(function () {
    if (document.getElementById("experiment_table") && document.getElementById("course_table")) {
        addEventListeners();
    }
});

/**
 * Loads the next course page and updates the course table with the new value.
 */
function loadNextCoursePage() {
    const PAGE = coursePage + 1;
    loadCoursePage(PAGE);
}

/**
 * Loads the previous course page and updates the course table with the new value.
 */
function loadPreviousCoursePage() {
    const PAGE = coursePage - 1;
    loadCoursePage(PAGE);
}

/**
 * Loads the first course page and updates the course table with the new value.
 */
function loadFirstCoursePage() {
    loadCoursePage(0);
}

/**
 * Loads the last course page and updates the course table with the new value.
 */
function loadLastCoursePage() {
    loadCoursePage(lastCoursePage);
}

/**
 * Loads the next experiment page and updates the experiment table with the new value.
 */
function loadNextExperimentPage() {
    const PAGE = experimentPage + 1;
    loadExperimentPage(PAGE);
}

/**
 * Loads the previous experiment page and updates the experiment table with the new value.
 */
function loadPreviousExperimentPage() {
    const PAGE = experimentPage - 1;
    loadExperimentPage(PAGE);
}

/**
 * Loads the first experiment page and updates the experiment table with the new value.
 */
function loadFirstExperimentPage() {
    loadExperimentPage(0);
}

/**
 * Loads the last experiment page and updates the experiment table with the new value.
 */
function loadLastExperimentPage() {
    loadExperimentPage(lastExperimentPage);
}

/**
 * Loads the experiment page for the given page number from the database and updates the corresponding table.
 *
 * @param page The page to be retrieved.
 */
function loadExperimentPage(page) {
    $.ajax({
        type: 'get',
        url: contextPath + "/page/experiment",
        data: {page: page},
        success: function(data) {
            updateExperimentTable(data);
        },
        error: function() {
            redirectErrorPage();
        }
    });
}

/**
 * Updates the information shown in the experiment table with the given data and adds the required event listeners.
 */
function updateExperimentTable(data) {
    $('#experiment_table').html(data);
    addEventListeners();
}

/**
 * Loads the course page for the given page number from the database and updates the corresponding table.
 *
 * @param page The page to be retrieved.
 */
function loadCoursePage(page) {
    $.ajax({
        type: 'get',
        url: contextPath + "/page/course",
        data: {page: page},
        success: function(data) {
            updateCourseTable(data);
        },
        error: function() {
            redirectErrorPage();
        }
    });
}

/**
 * Updates the information shown in the course table with the given data and adds the required event listeners.
 */
function updateCourseTable(data) {
    $('#course_table').html(data);
    addEventListeners();
}

/**
 * Adds the event listeners to handle user clicks on the buttons under the experiment and course tables.
 */
function addEventListeners() {
    addEventListener("coursesNext", loadNextCoursePage);
    addEventListener("coursesPrev", loadPreviousCoursePage);
    addEventListener("coursesFirst", loadFirstCoursePage);
    addEventListener("coursesLast", loadLastCoursePage);
    addEventListener("experimentsNext", loadNextExperimentPage);
    addEventListener("experimentsPrev", loadPreviousExperimentPage);
    addEventListener("experimentsFirst", loadFirstExperimentPage);
    addEventListener("experimentsLast", loadLastExperimentPage);
}

/**
 * Adds a click event listener calling the given function if the element with the given id can be found.
 *
 * @param elementId The id of the element.
 * @param callFunction The function to be called on click.
 */
function addEventListener(elementId, callFunction) {
    if (document.getElementById(elementId)) {
        document.getElementById(elementId).addEventListener("click", callFunction);
    }
}
