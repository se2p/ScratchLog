import {redirectErrorPage} from "./errorRedirect.js";

let started = document.getElementById("started");
let totalStarted = document.getElementById("totalStarted");
let finished = document.getElementById("finished");
let totalFinished = document.getElementById("totalFinished");

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

    setTimeout(getDashboardInfo, 60000);
}
