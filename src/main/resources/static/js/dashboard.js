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
let clickEvent = "GREENFLAG";
let clickEventData = [];
let clickEventChart;
let radarEventChart;
let resizeId;

/**
 * Triggers the function to hide the chart legends on small screens after a short timeout.
 */
$(window).resize(function() {
    clearTimeout(resizeId);
    resizeId = setTimeout(_checkHideLegends, 100);
});

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
            fetchClickEventData();
            fetchRadarChartData();
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
            blockEventData = _prepareEventData(data, blockEventData);
            let maxLength = blockEventData.length > 0 ? blockEventData[0].length : 0;

            if (blockEventChart) {
                blockEventChart.destroy();
            }

            if (maxLength > 0) {
                document.getElementById("blockEventChartNoData").style.display = "none";
                document.getElementById("blockEventChart").style.display = "block";
                let xValues = Array.from(Array(maxLength).keys());
                blockEventChart = _drawChart("blockEventChart", xValues, blockEventData, "line");
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
 * Fetches the number of executions per minute for a given click event for the currently selected users.
 */
function fetchClickEventData() {
    let selectedIds = selectedParticipants.map(function(item) {
        return item[0];
    });
    $.ajax({
        type: "get",
        url: contextPath + "/dashboard/data/event/click",
        data: {id: experimentId, users: JSON.stringify(selectedIds), event: clickEvent},
        success: function(data) {
            document.getElementById("clickEventChartDescription").innerText = " " + clickEvent;
            clickEventData = _prepareEventData(data, clickEventData);
            let maxLength = clickEventData.length > 0 ? clickEventData[0].length : 0;

            if (clickEventChart) {
                clickEventChart.destroy();
            }

            if (maxLength > 0) {
                document.getElementById("clickEventChartNoData").style.display = "none";
                document.getElementById("clickEventChart").style.display = "block";
                let xValues = Array.from(Array(maxLength).keys());
                clickEventChart = _drawChart("clickEventChart", xValues, clickEventData, "line");
            } else {
                document.getElementById("clickEventChartNoData").style.display = "block";
                document.getElementById("clickEventChart").style.display = "none";
            }
        },
        error: function() {
            redirectErrorPage();
        }
    });
}

/**
 * Fetches the number of total executions of specific events for the currently selected users.
 */
function fetchRadarChartData() {
    let selectedIds = selectedParticipants.map(function(item) {
        return item[0];
    });

    $.ajax({
        type: "get",
        url: contextPath + "/dashboard/data/event/counts",
        data: {id: experimentId, users: JSON.stringify(selectedIds)},
        success: function(data) {
            let counts = data.flat().reduce((sum, num) => {return sum + num}, 0);

            if (radarEventChart) {
                radarEventChart.destroy();
            }

            if (counts > 0) {
                document.getElementById("radarEventChartNoData").style.display = "none";
                document.getElementById("radarEventChart").style.display = "block";
                let xValues = ["CREATE", "MOVE", "DELETE", "GREENFLAG", "STOPALL", "STACKCLICK"];
                radarEventChart = _drawChart("radarEventChart", xValues, data, "radar");
            } else {
                document.getElementById("radarEventChartNoData").style.display = "block";
                document.getElementById("radarEventChart").style.display = "none";
            }
        },
        error: function() {
            redirectErrorPage();
        }
    });
}

/**
 * Hides the chart legends on small screens.
 */
function _checkHideLegends() {
    let width = document.getElementsByClassName("inside")[0].clientWidth;
    blockEventChart.legend.options.display = width > 600;
    blockEventChart.options.legend.display = width > 600;
    blockEventChart.update();
    clickEventChart.legend.options.display = width > 600;
    clickEventChart.options.legend.display = width > 600;
    clickEventChart.update();
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
    let blockEventElements = document.getElementById("blockEvents").children;
    Array.from(blockEventElements).forEach(element => element.addEventListener("click", function() {
        _updateBlockEventData(element.value);
    }));
    let clickEventElements = document.getElementById("clickEvents").children;
    Array.from(clickEventElements).forEach(element => element.addEventListener("click", function() {
       _updateClickEventData(element.value);
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
        fetchClickEventData();
        fetchRadarChartData();
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
        fetchClickEventData();
        fetchRadarChartData();
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
 * Fetches the data for the new block event when the event changes.
 *
 * @param event The new event information to be fetched.
 */
function _updateBlockEventData(event) {
    blockEvent = event;
    fetchBlockEventData();
}

/**
 * Fetches the data for the new click event when the event changes.
 *
 * @param event The new event information to be fetched.
 */
function _updateClickEventData(event) {
    clickEvent = event;
    fetchClickEventData();
}

/**
 * Prepares the event data retrieved from the database by filling missing values for users with zeros.
 *
 * @param data The event data retrieved from the database.
 * @param eventData The variable to which the data should be saved.
 * @return {number} The maximum number of data points for a single participant.
 */
function _prepareEventData(data, eventData) {
    eventData = data;
    let maxLength = 0;
    eventData.forEach(item => {
        maxLength = item.length > maxLength ? item.length : maxLength;
    });
    eventData.forEach(item => {
        if (item.length < maxLength) {
            while (item.length < maxLength) {
                item.push(0);
            }
        }
    });
    return eventData;
}

/**
 * Draws a chart of the specified type with entries for each currently selected participant for the given event count
 * data.
 *
 * @param item The id of the html element where the chart should be drawn.
 * @param xValues The values to be displayed on the x axis.
 * @param data The event count data of all selected participants.
 * @param type The type of chart to be drawn.
 * @return {Chart} The created chart.
 */
function _drawChart(item, xValues, data, type) {
    let datasets = [];
    for (let i = 0; i < data.length; i++) {
        let nextDataset = {
            data: data[i],
            fill: false,
            label: selectedParticipants[i][1]
        }
        datasets.push(nextDataset);
    }

    return new Chart(item, {
        type: type,
        data: {
            labels: xValues,
            datasets: datasets
        },
        options: {
            legend: {
                display: true
            }
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
