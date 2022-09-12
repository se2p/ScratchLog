/**
 * Readies the functions to retrieve a different experiment or course page upon clicking the respective buttons.
 */
$(document).ready(function () {
    addEventListeners();
});

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
 * Adds the event listeners to handle user clicks on the buttons under the experiment table.
 */
function addEventListeners() {
    document.getElementById("experimentsNext").addEventListener("click", loadNextExperimentPage);
    document.getElementById("experimentsPrev").addEventListener("click", loadPreviousExperimentPage);
    document.getElementById("experimentsFirst").addEventListener("click", loadFirstExperimentPage);
    document.getElementById("experimentsLast").addEventListener("click", loadLastExperimentPage);
}
