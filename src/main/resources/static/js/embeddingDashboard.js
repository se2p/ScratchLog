/**
 * Maps User ID to `{name, enabled}`.
 *
 * `enabled` marks the user as visible in the user-history trace.
 */
let participantsMap;
let latestChart;
let timelineChart;
let timelineChartInterval = 5;

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
});

function addUserSelectionEventListener(participant) {
    const checkbox = document.getElementById(`user-select-${participant.id}`)
    checkbox.addEventListener("change", () => toggleUserForTimeline(participant.id, checkbox.checked));
}

function toggleUserForTimeline(userId, isEnabled) {
    participantsMap.get(userId).enabled = isEnabled;
    setTimeout(() => updateTimelineChart(), 0);
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
                chartData.push(dataSeries.datapoints[0]);
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
                            }
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
                    data: dataSeries.datapoints,
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
