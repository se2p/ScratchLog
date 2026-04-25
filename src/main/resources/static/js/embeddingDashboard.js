/**
 * Maps User ID to username
 */
let participantsMap;

$(document).ready(() => {
    participantsMap = new Map();
    for (const p of participants) {
        // enabled for the user-history trace?
        participantsMap.set(p.id, {name: p.username, enabled: false});
    }

    updateAllLatestChart();
});

function updateAllLatestChart() {
    $.ajax({
        type: "get",
        url: contextPath + "embeddings/progress-variance-projection/all/latest",
        data: {experimentId},
        success: function (data) {
            const element = document.getElementById("chart-all-latest");

            const labels = Object.keys(data.projections)
                .map(id => participantsMap.get(Number(id)).name);
            const chartData = Object.values(data.projections);

            new Chart(element, {
                type: "bubble",
                data: {
                    labels: labels,
                    datasets: [
                        {
                            // todo: translate
                            label: "Latest Code State All Participants",
                            data: chartData,
                        }
                    ]
                }
            });
        }
    })
}
