/**
 * Maps User ID to `{name, enabled}`.
 *
 * `enabled` marks the user as visible in the user-history trace.
 */
let participantsMap;

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
    }
};

$(document).ready(() => {
    participantsMap = new Map();
    for (const p of participants) {
        participantsMap.set(p.id, {name: p.username, enabled: false});
    }

    setTimeout(() => updateAllLatestChart(), 0);
    setTimeout(() => updateTimelineChart(), 0);
});

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

            new Chart(element, {
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

    $.ajax({
        type: "get",
        url: contextPath + "embeddings/progress-variance-projection/timeline",
        data: {experimentId, userIds},
        accept: "application/json",
        success: function (data) {
            const element = document.getElementById("chart-timeline");

            const labels = [];
            const datasets = [];
            for (const dataSeries of data.data) {
                datasets.push({
                    label: participantsMap.get(dataSeries.userId).name,
                    data: dataSeries.datapoints,
                    tension: 0.2,
                });
                for (let idx = 1; idx <= dataSeries.datapoints.length; ++idx) {
                    labels.push(idx.toString());
                }
            }

            new Chart(element, {
                type: "line",
                data: {
                    labels: labels,
                    datasets: datasets,
                },
                options: chartCommonOptions,
            });
        },
        error: function (err) {
            console.log(err.statusText);
        }
    });
}
