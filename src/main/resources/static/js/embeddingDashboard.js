/**
 * Maps User ID to `{name, enabled}`.
 *
 * `enabled` marks the user as visible in the user-history trace.
 */
let participantsMap;
let latestChart;
let timelineChart;
let timelineChartInterval = 5;
let embeddingTestDistanceChart;
let testResults;

const chartCommonOptions = {
    scales: {
        x: {
            title: {
                display: true,
                text: "Progress"
            },
            type: "linear",
        },
        y: {
            title: {
                display: true,
                text: "Variance"
            }
        }
    },
    plugins: {
        zoom: {
            pan: {
                enabled: true,
            },
            zoom: {
                wheel: {
                    enabled: true,
                },
                pinch: {
                    enabled: true
                },
            }
        }
    }
};

$(document).ready(() => {
    participantsMap = new Map();
    for (const p of participants) {
        participantsMap.set(p.id, {name: p.username, enabled: false});
        addUserSelectionEventListener(p);
    }

    const timelineChartIntervalInput = document.getElementById("chart-timeline-interval");
    timelineChartIntervalInput.addEventListener("change", (event) => onTimelineIntervalChange(event.target.value));

    setTimeout(() => updateAllLatestChart(), 0);
    setTimeout(() => updateTimelineChart(), 0);
    setTimeout(() => updateTestDistanceChart(), 0);
    setTimeout(() => updateTestResultTable(), 0);
});

function addUserSelectionEventListener(participant) {
    const checkbox = document.getElementById(`user-select-${participant.id}`)
    checkbox.addEventListener("change", () => toggleUserForTimeline(participant.id, checkbox.checked));
}

function toggleUserForTimeline(userId, isEnabled) {
    participantsMap.get(userId).enabled = isEnabled;
    setTimeout(() => updateTimelineChart(), 0);
    setTimeout(() => buildTestResultTable(), 0);
}

function xyToBubble(xy) {
    return {x: xy[0], y: xy[1], r: 5}
}

function updateAllLatestChart() {
    $.ajax({
        type: "get",
        url: contextPath + "embeddings/progress-variance-projection/all/latest",
        data: {experimentId},
        accept: "application/json",
        success: function (data) {
            const element = document.getElementById("chart-all-latest");

            const labels = [];
            const chartData = [];
            for (const dataSeries of data.data) {
                labels.push(participantsMap.get(dataSeries.userId).name);
                const datapoint = dataSeries.datapoints[0];
                chartData.push(xyToBubble(datapoint));
            }

            if (latestChart) {
                latestChart.data.labels = labels;
                latestChart.data.datasets = [{
                    label: translations.latestChart,
                    data: chartData,
                }];
                latestChart.update();
            } else {
                latestChart = new Chart(element, {
                    type: "bubble",
                    data: {
                        labels: labels,
                        datasets: [
                            {
                                label: translations.latestChart,
                                data: chartData,
                            },
                            {
                                label: "Starting Project",
                                data: [{x: 0, y: 0, r: 7}]
                            },
                            {
                                label: "Example Solution",
                                data: [{x: 1, y: 0, r: 7}]
                            },
                        ],
                    },
                    options: chartCommonOptions,
                });
                addResetZoomEventHandler("chart-all-latest-reset-zoom", latestChart);
            }
        },
        error: function (err) {
            console.log(err.statusText);
        }
    });
}

function updateTimelineChart() {
    const userIds = [];
    participantsMap.forEach((v, k) => {
        if (v.enabled) {
            userIds.push(k);
        }
    });

    document.getElementById("chart-timeline").hidden = userIds.length === 0;
    document.getElementById("chart-timeline-reset-zoom").hidden = userIds.length === 0;

    if (userIds.length === 0) {
        if (timelineChart) {
            timelineChart.data.labels = [];
            timelineChart.data.datasets = [];
            timelineChart.update();
        }
        return;
    }

    $.ajax({
        type: "get",
        url: contextPath + "embeddings/progress-variance-projection/timeline",
        data: {experimentId, userIds, stepMinutes: timelineChartInterval},
        accept: "application/json",
        success: function (data) {
            const element = document.getElementById("chart-timeline");

            const labels = [];
            const datasets = [];
            for (const dataSeries of data.data) {
                datasets.push({
                    label: participantsMap.get(dataSeries.userId).name,
                    data: dataSeries.datapoints.map(xyToBubble),
                    tension: 0.1,
                });
                for (let idx = 1; idx <= dataSeries.datapoints.length; ++idx) {
                    labels.push(idx.toString());
                }
            }

            if (timelineChart) {
                timelineChart.data.labels = labels;
                timelineChart.data.datasets = datasets;
                timelineChart.update();
            } else {
                timelineChart = new Chart(element, {
                    type: "line",
                    data: {
                        labels: labels,
                        datasets: datasets,
                    },
                    options: chartCommonOptions,
                });
                addResetZoomEventHandler("chart-timeline-reset-zoom", timelineChart);
            }

        },
        error: function (err) {
            console.log(err.statusText);
        }
    });
}

function updateTestDistanceChart() {
    const element = document.getElementById("chart-embedding-test-distances");
    if (!element) {
        return;
    }

    $.ajax({
        type: "get",
        url: contextPath + "embeddings/embedding-test-distance/all/latest",
        data: {experimentId},
        accept: "application/json",
        success: function (data) {
            const labels = [];
            const chartData = [];
            for (const dataSeries of data.data) {
                labels.push(participantsMap.get(dataSeries.userId).name);
                const datapoint = dataSeries.datapoints[0];
                if (datapoint[0] === 0 && datapoint[1] === 0) {
                    continue;
                }
                chartData.push(xyToBubble(datapoint));
            }

            if (embeddingTestDistanceChart) {
                embeddingTestDistanceChart.data.labels = labels;
                embeddingTestDistanceChart.data.datasets = [{
                    label: translations.embeddingTestDistanceChart,
                    data: chartData,
                }];
                embeddingTestDistanceChart.update();
            } else {
                embeddingTestDistanceChart = new Chart(element, {
                    type: "bubble",
                    data: {
                        labels: labels,
                        datasets: [
                            {
                                label: translations.embeddingTestDistanceChart,
                                data: chartData,
                            }
                        ],
                    },
                    options: {
                        scales: {
                            x: {
                                title: {
                                    display: true,
                                        text: "Test Fitness"
                                },
                                type: "linear",
                            },
                            y: {
                                title: {
                                    display: true,
                                        text: "Embedding Fitness"
                                }
                            }
                        },
                    },
                });
            }
        },
        error: function (err) {
            console.log(err.statusText);
        }
    });
}

function addResetZoomEventHandler(elementId, targetChart) {
    const element = document.getElementById(elementId);
    element.addEventListener("click", () => {
        if (targetChart) {
            targetChart.resetZoom();
        }
    });
}

function onTimelineIntervalChange(newValue) {
    if (newValue !== timelineChartInterval) {
        timelineChartInterval = newValue;
        setTimeout(() => updateTimelineChart(), 0);
    }
}

function updateTestResultTable() {
    const table = document.getElementById("test-results-latest-table");
    if (!table) {
        return
    }

    $.ajax({
        type: "get",
        url: contextPath + "whisker/test/latest",
        data: {experimentId},
        accept: "application/json",
        success: (data) => {
            testResults = data;
            buildTestResultTable();
        },
    });
}

function buildTestResultTable() {
    const table = document.getElementById("test-results-latest-table");

    const {
        testCaseNames,
        userProgramTestResults
    } = testResults;

    let html = `<thead><tr><th scope="col">User</th>`;
    testCaseNames.forEach(name => html += `<th scope="col">${name}</th>`);
    html += "</tr></thead><tbody>";

    let atLeastOneEnabled = false

    for (const user of userProgramTestResults) {
        if (!participantsMap.get(user.userId).enabled) {
            continue;
        }

        atLeastOneEnabled = true;

        const username = user.username;
        const userTestResults = user.testResults;

        html += `<tr><th scope="row">${username}</th>`;
        testCaseNames.forEach(name => {
            const testResult = userTestResults[name];
            let label;
            if (testResult === undefined) {
                label = "?";
            } else if (testResult === "pass") {
                label = "✓";
            } else if (testResult === "fail") {
                label = "✗";
            } else {
                label = testResult;
            }
            html += `<td style="text-align: center;">${label}</td>`
        });
        html += "</tr>";
    }
    html += "</tbody>"

    if (atLeastOneEnabled) {
        table.innerHTML = html;
    } else {
        table.innerHTML = "<tr><td>No Users Selected</td></tr>";
    }
}
