import {redirectErrorPage} from "./errorRedirect.js";

let started = document.getElementById("started");
let totalStarted = document.getElementById("totalStarted");
let finished = document.getElementById("finished");
let totalFinished = document.getElementById("totalFinished");
let participants = [];
let selectedParticipants = [];
let blockEventData = [];
let blockEventChart;
let clickEventData = [];
let clickEventChart;
let resourceEventData = [];
let resourceEventChart;
let radarEventChart;
let radarEventData;
let chart1;
let chart2;
let chart3;
let resizeId;
let maxHeight;

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
    maxHeight = document.getElementById("chartsCarousel").clientHeight;
});

/**
 * Retrieves the data to be displayed on the dashboard from the database. If an error occurred during data fetching, the
 * user is redirected to the error page. This function is executed periodically to refresh the data.
 */
function getDashboardInfo() {
    fetchExperimentData();
    fetchParticipantData();
    _changeDropdownVisibility();
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
            blockEventData = _prepareEventData(data, blockEventData);
            fetchClickEventData();
            let maxLength = blockEventData.length > 0 ? blockEventData[0].length : 0;

            if (blockEventChart) {
                blockEventChart.destroy();
            }

            let xValues = Array.from(Array(maxLength).keys());
            blockEventChart = _displayChart(xValues, blockEventData, "blockEventChart", "blockEventChartNoData",
                maxLength, "line", true);
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
            fetchResourceEventData();
            let maxLength = clickEventData.length > 0 ? clickEventData[0].length : 0;

            if (clickEventChart) {
                clickEventChart.destroy();
            }

            let xValues = Array.from(Array(maxLength).keys());
            clickEventChart = _displayChart(xValues, clickEventData, "clickEventChart", "clickEventChartNoData",
                maxLength, "line", true);
        },
        error: function() {
            redirectErrorPage();
        }
    });
}

/**
 * Fetches the number of executions per minute for a given resource event for the currently selected users.
 */
function fetchResourceEventData() {
    let selectedIds = selectedParticipants.map(function(item) {
        return item[0];
    });
    $.ajax({
        type: "get",
        url: contextPath + "/dashboard/data/event/resource",
        data: {id: experimentId, users: JSON.stringify(selectedIds), event: resourceEvent},
        success: function(data) {
            document.getElementById("resourceEventChartDescription").innerText = " " + resourceEvent;
            resourceEventData = _prepareEventData(data, resourceEventData);
            fetchRadarChartData();
            let maxLength = resourceEventData.length > 0 ? resourceEventData[0].length : 0;

            if (resourceEventChart) {
                resourceEventChart.destroy();
            }

            let xValues = Array.from(Array(maxLength).keys());
            resourceEventChart = _displayChart(xValues, resourceEventData, "resourceEventChart",
                "resourceEventChartNoData", maxLength, "line", true);
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
            radarEventData = data;

            if (radarEventChart) {
                radarEventChart.destroy();
            }

            radarEventChart = _displayChart(radarValues, data, "radarEventChart", "radarEventChartNoData", counts,
                "radar", true);
            document.getElementById("radarEventChart").style.maxHeight = maxHeight + "px";
            _displayUnseenCharts();
            _checkHideLegends();
        },
        error: function() {
            redirectErrorPage();
        }
    });
}

/**
 * Displays the charts currently not displayed in the carousel as smaller charts below.
 * @private
 */
function _displayUnseenCharts() {
    let active = document.getElementsByClassName("carousel-item active")[0];
    let clickMaxLength = clickEventData.length > 0 ? clickEventData[0].length : 0;
    let blockMaxLength = blockEventData.length > 0 ? blockEventData[0].length : 0;
    let resourceMaxLength = resourceEventData.length > 0 ? resourceEventData[0].length : 0;
    let counts = radarEventData.flat().reduce((sum, num) => {return sum + num}, 0);
    let clickValues = Array.from(Array(clickMaxLength).keys());
    let blockValues = Array.from(Array(blockMaxLength).keys());
    let resourceValues = Array.from(Array(resourceMaxLength).keys());

    if (chart1) {
        chart1.destroy();
    }
    if (chart2) {
        chart2.destroy();
    }
    if (chart3) {
        chart3.destroy();
    }

    if (active.querySelector("#blockEvents")) {
        chart1 = _displayChart(clickValues, clickEventData, "chart1", "chart1NoData", clickMaxLength, "line", false);
        chart2 = _displayChart(resourceValues, resourceEventData, "chart2", "chart2NoData", counts, "line", false);
        chart3 = _displayChart(radarValues, radarEventData, "chart3", "chart3NoData", counts, "radar", false);
    } else if (active.querySelector("#clickEvents")) {
        chart1 = _displayChart(blockValues, blockEventData, "chart1", "chart1NoData", blockMaxLength, "line", false);
        chart2 = _displayChart(resourceValues, resourceEventData, "chart2", "chart2NoData", counts, "line", false);
        chart3 = _displayChart(radarValues, radarEventData, "chart3", "chart3NoData", counts, "radar", false);
    } else if (active.querySelector("#resourceEvents")) {
        chart1 = _displayChart(blockValues, blockEventData, "chart1", "chart1NoData", blockMaxLength, "line", false);
        chart2 = _displayChart(clickValues, clickEventData, "chart2", "chart2NoData", clickMaxLength, "line", false);
        chart3 = _displayChart(radarValues, radarEventData, "chart3", "chart3NoData", counts, "radar", false);
    } else {
        chart1 = _displayChart(blockValues, blockEventData, "chart1", "chart1NoData", blockMaxLength, "line", false);
        chart2 = _displayChart(clickValues, clickEventData, "chart2", "chart2NoData", clickMaxLength, "line", false);
        chart3 = _displayChart(resourceValues, resourceEventData, "chart3", "chart3NoData", counts, "line", false);
    }

    _checkHideLegends();
}

/**
 * Displays a chart of the given type for the passed data if it contains any values.
 *
 * @param xValues The values to be displayed on the x-axis.
 * @param chartData The data to be visualized in the chart.
 * @param canvasId The id of the canvas element where the chart should be drawn.
 * @param noDataId The id of the element stating that no data is available for the chart.
 * @param counts Number indicating whether the passed data contains values that should be displayed.
 * @param type The type of chart to be drawn.
 * @param displayLegend Boolean indicating whether a legend should be displayed or not.
 * @return {Chart} The drawn chart or nothing, if no chart was rendered.
 * @private
 */
function _displayChart(xValues, chartData, canvasId, noDataId, counts, type, displayLegend) {
    if (counts > 0) {
        document.getElementById(noDataId).style.display = "none";
        document.getElementById(canvasId).style.display = "block";
        return _drawChart(canvasId, xValues, chartData, type, displayLegend);
    } else {
        document.getElementById(noDataId).style.display = "flex";
        document.getElementById(canvasId).style.display = "none";
    }
}

/**
 * Hides the chart legends on small screens.
 * @private
 */
function _checkHideLegends() {
    let width = document.getElementsByClassName("inside")[0].clientWidth;
    if (blockEventChart) {
        blockEventChart.legend.options.display = width > 600;
        blockEventChart.update();
    }
    if (clickEventChart) {
        clickEventChart.legend.options.display = width > 600;
        clickEventChart.update();
    }
    if (resourceEventChart) {
        resourceEventChart.legend.options.display = width > 600;
        resourceEventChart.update();
    }
    if(radarEventChart) {
        radarEventChart.legend.options.display = width > 600;
        radarEventChart.update();
    }
    if (chart1) {
        chart1.legend.options.display = false;
        chart1.update();
    }
    if (chart2) {
        chart2.legend.options.display = false;
        chart2.update();
    }
    if (chart3) {
        chart3.legend.options.display = false;
        chart3.update();
    }
}

/**
 * Updates the selected participants list based on the fetched participant data.
 * @private
 */
function _updateSelectedParticipants() {
    if (selectedParticipants.length === 0) {
        for (let i = 0; i < participants.length; i++) {
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
 * @private
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
 * @private
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
 * @private
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
    let resourceEventElements = document.getElementById("resourceEvents").children;
    Array.from(resourceEventElements).forEach(element => element.addEventListener("click", function() {
        _updateResourceEventData(element.value);
    }));
    $('#chartsCarousel').on('slid.bs.carousel', function () {
        _displayUnseenCharts();
    });
}

/**
 * Adds a new participant to the selected participants lists and updates the displayed list and dropdown menu
 * accordingly.
 *
 * @param participant The username of the participant to be added.
 * @private
 */
function _addSelectedParticipant(participant) {
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
    _checkHideLegends();
}

/**
 * Removes the given participant from the selected participants list and updates the displayed list and dropdown menu
 * accordingly.
 *
 * @param participant The username of the participant to be removed.
 * @private
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
        _checkHideLegends();
    }
}

/**
 * Changes the visibility of the participants dropdown menu if data of all participants is displayed.
 * @private
 */
function _changeDropdownVisibility() {
    if (participants.length === 0) {
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
 * @private
 */
function _updateBlockEventData(event) {
    blockEvent = event;
    fetchBlockEventData();
    _checkHideLegends();
}

/**
 * Fetches the data for the new click event when the event changes.
 *
 * @param event The new event information to be fetched.
 * @private
 */
function _updateClickEventData(event) {
    clickEvent = event;
    fetchClickEventData();
    _checkHideLegends();
}

/**
 * Fetches the data for the new resource event when the event changes.
 *
 * @param event The new event information to be fetched.
 * @private
 */
function _updateResourceEventData(event) {
    resourceEvent = event;
    fetchResourceEventData();
    _checkHideLegends();
}

/**
 * Prepares the event data retrieved from the database by filling missing values for users with zeros.
 *
 * @param data The event data retrieved from the database.
 * @param eventData The variable to which the data should be saved.
 * @return {number} The maximum number of data points for a single participant.
 * @private
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
 * @param displayLegend Boolean indicating whether a legend should be displayed or not.
 * @return {Chart} The created chart.
 * @private
 */
function _drawChart(item, xValues, data, type, displayLegend) {
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
                display: displayLegend
            }
        }
    });
}

/**
 * Checks if the given participant is equal to the current one.
 *
 * @param participant1 The participant to compare to.
 * @return {boolean} true, if the participants are equal, or false otherwise.
 * @private
 */
function _containsParticipant(participant1) {
    return participant1[0] === this[0] && participant1[1] === this[1];
}
