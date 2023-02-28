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
    $.ajax({
        type: 'get',
        url: contextPath + "/next/course",
        data: {page: coursePage},
        success: function(data) {
            updateCourseTable(data);
            coursePage++;
        },
    });
}

/**
 * Loads the previous course page and updates the course table with the new value.
 */
function loadPreviousCoursePage() {
    $.ajax({
        type: 'get',
        url: contextPath + "/previous/course",
        data: {page: coursePage},
        success: function(data) {
            updateCourseTable(data);
            coursePage--;
        },
    });
}

/**
 * Loads the first course page and updates the course table with the new value.
 */
function loadFirstCoursePage() {
    $.ajax({
        type: 'get',
        url: contextPath + "/first/course",
        success: function(data) {
            updateCourseTable(data);
            coursePage = 1;
        },
    });
}

/**
 * Loads the last course page and updates the course table with the new value.
 */
function loadLastCoursePage() {
    $.ajax({
        type: 'get',
        url: contextPath + "/last/course",
        success: function(data) {
            updateCourseTable(data);
            coursePage = lastCoursePage;
        },
    });
}

/**
 * Loads the next experiment page and updates the experiment table with the new value.
 */
function loadNextExperimentPage() {
    $.ajax({
        type: 'get',
        url: contextPath + "/next/experiment",
        data: {page: experimentPage},
        success: function(data) {
            updateExperimentTable(data);
            experimentPage++;
        },
    });
}

/**
 * Loads the previous experiment page and updates the experiment table with the new value.
 */
function loadPreviousExperimentPage() {
    $.ajax({
        type: 'get',
        url: contextPath + "/previous/experiment",
        data: {page: experimentPage},
        success: function(data) {
            updateExperimentTable(data);
            experimentPage--;
        },
    });
}

/**
 * Loads the first experiment page and updates the experiment table with the new value.
 */
function loadFirstExperimentPage() {
    $.ajax({
        type: 'get',
        url: contextPath + "/first/experiment",
        success: function(data) {
            updateExperimentTable(data);
            experimentPage = 1;
        },
    });
}

/**
 * Loads the last experiment page and updates the experiment table with the new value.
 */
function loadLastExperimentPage() {
    $.ajax({
        type: 'get',
        url: contextPath + "/last/experiment",
        success: function(data) {
            updateExperimentTable(data);
            experimentPage = lastExperimentPage;
        },
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