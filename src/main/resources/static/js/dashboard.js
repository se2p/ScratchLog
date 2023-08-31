import {redirectErrorPage} from "./errorRedirect.js";

let started = document.getElementById("started");
let totalStarted = document.getElementById("totalStarted");
let finished = document.getElementById("finished");
let totalFinished = document.getElementById("totalFinished");
let participants = [];
let selectedParticipants = [];
let blockEvent = "CREATE";
let blockEventData = [];
let blockEventChart;

/**
 * Readies the function to retrieve the information to be displayed on the dashboard.
 */
$(document).ready(function () {
    getDashboardInfo();
});

/**
 * Retrieves the data to be displayed on the dashboard from the database. If an error occurred during data fetching, the
 * user is redirected to the error page. This function is executed periodically to refresh the data.
 */
function getDashboardInfo() {
    fetchExperimentData();
    fetchParticipantData();
    setTimeout(getDashboardInfo, 60000);
}

/**
 * Fetches the experiment data containing information about the number of participants from the database. If an error
 * occurred during data fetching, the user is redirected to the error page.
 */
function fetchExperimentData() {
    $.ajax({
        type: "get",
        url: contextPath + "/dashboard/data",
        data: {id: experimentId},
        success: function(data) {
            totalStarted.innerText = data[0];
            totalFinished.innerText = data[0];
            started.innerText = data[1];
            finished.innerText = data[2];
        },
        error: function() {
            redirectErrorPage();
        }
    });
}

/**
 * Fetches the ids and usernames of all participants from the database. If an error occurred during data fetching, the
 * user is redirected to the error page.
 */
function fetchParticipantData() {
    $.ajax({
        type: "get",
        url: contextPath + "/dashboard/data/participants",
        data: {id: experimentId},
        success: function(data) {
            participants = data;
            _updateSelectedParticipants();
            _addSelectedParticipants();
            _fillParticipantsDropdown();
            _addEventListeners();
            fetchBlockEventData();
        },
        error: function() {
            redirectErrorPage();
        }
    });
}

/**
 * Fetches the number of executions per minute for a given block event for the currently selected users.
 */
function fetchBlockEventData() {
    let selectedIds = selectedParticipants.map(function(item) {
        return item[0];
    });
    $.ajax({
        type: "get",
        url: contextPath + "/dashboard/data/event/block",
        data: {id: experimentId, users: JSON.stringify(selectedIds), event: blockEvent},
        success: function(data) {
            document.getElementById("blockEventChartDescription").innerText = " " + blockEvent;
            let maxLength = _prepareBlockEventData(data);

            if (blockEventChart) {
                blockEventChart.destroy();
            }

            if (maxLength > 0) {
                document.getElementById("blockEventChartNoData").style.display = "none";
                document.getElementById("blockEventChart").style.display = "block";
                let xValues = Array.from(Array(maxLength).keys());
                _drawLineChart("blockEventChart", xValues, blockEventData);
            } else {
                document.getElementById("blockEventChartNoData").style.display = "block";
                document.getElementById("blockEventChart").style.display = "none";
            }
        },
        error: function() {
            redirectErrorPage();
        }
    });
}

/**
 * Updates the selected participants list based on the fetched participant data.
 */
function _updateSelectedParticipants() {
    if (selectedParticipants.length === 0) {
        let numSelected = participants.length > 5 ? 5 : participants.length;

        for (let i = 0; i < numSelected; i++) {
            selectedParticipants.push(participants[i]);
        }
    } else {
        selectedParticipants = selectedParticipants.filter(
            item => !participants.includes(part => part === item));

        if (selectedParticipants.length === 0) {
            selectedParticipants.push(participants[0]);
        }
    }
}

/**
 * Adds the selected participants as a list to the dashboard page to be displayed.
 */
function _addSelectedParticipants() {
    let html = `<ul class="list-group list-group-horizontal flex-wrap mx-3">`;
    selectedParticipants.forEach(function (participant) {
        html += `
        <li class="list-group-item">
            <span>${participant[1]}</span>
            <i aria-hidden="true" class="fas fa-trash-alt"></i>
        </li>
        `
    });
    html += `</ul>`
    $("#selectedParticipants").html(html);
}

/**
 * Fills the participant dropdown menu with usernames of participants who are not currently selected.
 */
function _fillParticipantsDropdown() {
    let html;
    participants.forEach(function (participant) {
        if (typeof selectedParticipants.find(_containsParticipant, participant) === "undefined") {
            html += `
            <option class="text-dark">${participant[1]}</option>
            `
        }
    });
    $("#participants").html(html);
}

/**
 * Adds event listeners to all elements of the selected participants list to allow removal of selected participants and
 * to all elements of the participants dropdown menu to allow adding participants as selected.
 */
function _addEventListeners() {
    let listElements = document.getElementsByClassName("fas fa-trash-alt");
    Array.from(listElements).forEach(element => element.addEventListener("click", function() {
        _removeSelectedParticipant(element.previousElementSibling.innerHTML);
    }));
    let dropdownElements = document.getElementsByClassName("text-dark");
    Array.from(dropdownElements).forEach(element => element.addEventListener("click", function() {
        _addSelectedParticipant(element.innerHTML);
    }));
}

/**
 * Adds a new participant to the selected participants lists and updates the displayed list and dropdown menu
 * accordingly.
 *
 * @param participant The username of the participant to be added.
 */
function _addSelectedParticipant(participant) {
    if (selectedParticipants.length < 10) {
        const index = participants.findIndex(item => item[1] === participant);
        selectedParticipants.push(participants[index]);
        participants.splice(index, 1);
        _addSelectedParticipants();
        _fillParticipantsDropdown();
        _addEventListeners();
        _changeDropdownVisibility();
        fetchBlockEventData();
    }
}

/**
 * Removes the given participant from the selected participants list and updates the displayed list and dropdown menu
 * accordingly.
 *
 * @param participant The username of the participant to be removed.
 */
function _removeSelectedParticipant(participant) {
    if (selectedParticipants.length > 1) {
        selectedParticipants = selectedParticipants.filter(item => item[1] !== participant);
        _addSelectedParticipants();
        _fillParticipantsDropdown();
        _addEventListeners();
        _changeDropdownVisibility();
        fetchBlockEventData();
    }
}

/**
 * Changes the visibility of the participants dropdown menu to allow only a limited number of participant data to be
 * displayed at once.
 */
function _changeDropdownVisibility() {
    if (selectedParticipants.length >= 10) {
        document.getElementById("participants").style.display = "none";
        document.getElementById("participantsLabel").style.display = "none";
    } else {
        document.getElementById("participants").style.display = "block";
        document.getElementById("participantsLabel").style.display = "block";
    }
}

/**
 * Prepares the event data retrieved from the database by filling missing values for users with zeros.
 *
 * @param data The event data.
 * @return {number} The maximum number of data points for a single participant.
 */
function _prepareBlockEventData(data) {
    blockEventData = data;
    let maxLength = 0;
    blockEventData.forEach(item => {
        maxLength = item.length > maxLength ? item.length : maxLength;
    });
    blockEventData.forEach(item => {
        if (item.length < maxLength) {
            while (item.length < maxLength) {
                item.push(0);
            }
        }
    });
    return maxLength;
}

/**
 * Draws a line chart with one line for each currently selected participant for the given event count data.
 *
 * @param item The id of the html element where the chart should be drawn.
 * @param xValues The values to be displayed on the x axis.
 * @param data The event count data of all selected participants.
 */
function _drawLineChart(item, xValues, data) {
    let datasets = [];
    for (let i = 0; i < data.length; i++) {
        let nextDataset = {
            data: data[i],
            fill: false,
            label: selectedParticipants[i][1]
        }
        datasets.push(nextDataset);
    }

    blockEventChart = new Chart(item, {
        type: "line",
        data: {
            labels: xValues,
            datasets: datasets
        },
        options: {
            legend: {display: true}
        }
    });
}

/**
 * Checks if the given participant is equal to the current one.
 *
 * @param participant1 The participant to compare to.
 * @return {boolean} true, if the participants are equal, or false otherwise.
 */
function _containsParticipant(participant1) {
    return participant1[0] === this[0] && participant1[1] === this[1];
}
