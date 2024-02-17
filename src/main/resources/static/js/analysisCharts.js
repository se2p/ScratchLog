/**
 * Renders the charts for displaying information on bug patterns, code smells and perfumes once DOM is ready.
 */
$(document).ready(function() {
    if (bugPatterns.length > 0) {
        let bugsX = [...Array(bugPatterns.length).keys()];
        _renderChart("bugsChart", bugsX, "Bug Patterns", bugPatterns, 'red');
    }
    if (smells.length > 0) {
        let smellsX = [...Array(smells.length).keys()];
        _renderChart("smellsChart", smellsX, "Smells", smells, 'orange');
    }
    if (perfumes.length > 0) {
        let perfumesX = [...Array(perfumes.length).keys()];
        _renderChart("perfumesChart", perfumesX, "Perfumes", perfumes, 'green');
    }
});

/**
 * Draws a line chart for the given data representing bug patterns, code smells or perfumes per minute.
 *
 * @param chartId The id of the html element where the chart should be drawn.
 * @param xValues The values to be displayed on the x axis.
 * @param label The label to be displayed for the drawn line.
 * @param data An array containing the data points per minute.
 * @param borderColor The color of the line.
 * @private
 */
function _renderChart(chartId, xValues, label, data, borderColor) {
    new Chart(chartId, {
        type: "line",
        data: {
            labels: xValues,
            datasets: [{
                label: label,
                data: data,
                borderColor: borderColor
            }]
        }
    });
}
