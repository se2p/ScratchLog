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
    updateLastCoursePage();
    loadCoursePage(PAGE);
}

/**
 * Loads the previous course page and updates the course table with the new value.
 */
function loadPreviousCoursePage() {
    const PAGE = coursePage - 1;
    updateLastCoursePage();
    loadCoursePage(PAGE);
}

/**
 * Loads the first course page and updates the course table with the new value.
 */
function loadFirstCoursePage() {
    updateLastCoursePage();
    loadCoursePage(0);
}

/**
 * Loads the last course page and updates the course table with the new value.
 */
function loadLastCoursePage() {
    updateLastCoursePage();
    loadCoursePage(lastCoursePage);
}

/**
 * Loads the next experiment page and updates the experiment table with the new value.
 */
function loadNextExperimentPage() {
    const PAGE = experimentPage + 1;
    updateLastExperimentPage();
    loadExperimentPage(PAGE);
}

/**
 * Loads the previous experiment page and updates the experiment table with the new value.
 */
function loadPreviousExperimentPage() {
    const PAGE = experimentPage - 1;
    updateLastExperimentPage();
    loadExperimentPage(PAGE);
}

/**
 * Loads the first experiment page and updates the experiment table with the new value.
 */
function loadFirstExperimentPage() {
    updateLastExperimentPage();
    loadExperimentPage(0);
}

/**
 * Loads the last experiment page and updates the experiment table with the new value.
 */
function loadLastExperimentPage() {
    updateLastExperimentPage();
    loadExperimentPage(lastExperimentPage);
}

/**
 * Retrieves the page number of the last experiment page from the database.
 */
function updateLastExperimentPage() {
    $.ajax({
        type: 'get',
        url: contextPath + "/pages/home/experiment",
        success: function(data) {
            lastExperimentPage = data;
        },
        error: function() {
            redirectErrorPage();
        }
    });
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
            experimentPage = page;
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
 * Retrieves the page number of the last course page from the database.
 */
function updateLastCoursePage() {
    $.ajax({
        type: 'get',
        url: contextPath + "/pages/home/course",
        success: function(data) {
            lastCoursePage = data;
        },
        error: function() {
            redirectErrorPage();
        }
    });
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
            coursePage = page;
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
    document.getElementById("coursesNext").addEventListener("click", loadNextCoursePage);
    document.getElementById("coursesPrev").addEventListener("click", loadPreviousCoursePage);
    document.getElementById("coursesFirst").addEventListener("click", loadFirstCoursePage);
    document.getElementById("coursesLast").addEventListener("click", loadLastCoursePage);
    document.getElementById("experimentsNext").addEventListener("click", loadNextExperimentPage);
    document.getElementById("experimentsPrev").addEventListener("click", loadPreviousExperimentPage);
    document.getElementById("experimentsFirst").addEventListener("click", loadFirstExperimentPage);
    document.getElementById("experimentsLast").addEventListener("click", loadLastExperimentPage);
}
