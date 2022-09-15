$(document).ready(function() {
    $("#experimentInput").keyup(function() {
        $('#experimentResults').html("");
        getCourseExperimentSuggestions();
    });

    $("#deleteExperimentInput").keyup(function() {
        $('#deleteExperimentResults').html("");
        getCourseExperimentDeleteSuggestions();
    });
});

/**
 * Fires an ajax request to the search rest controller to retrieve suggestions based on the input query in the
 * experiment input field on the course page for the course in question. On success, all retrieved suggestions are added
 * to the experiment result div to be displayed as a list of experiment ids and titles that matched the query.
 */
function getCourseExperimentSuggestions() {
    let request = $.ajax({
        dataType: "json",
        url: contextPath + "/search/course/experiment",
        delay: 250,
        data: {query: $('#experimentInput').val(), id: $('#addExperimentId').val()}
    });

    request.done(function(result) {
        let html = "<ul class='list-group'>";
        result.forEach(function(element) {
            html += `
            <li class="list-group-item list-group-item-action">
                <div class='ms-2 me-auto no_decoration'
                onclick="setExperimentInput(this.getElementsByClassName('fw-bold')[0])">
                    <span>${sanitize(element[1])}</span>
                    <span class="fw-bold">${sanitize(element[2])}</span>
                </div>
            </li>
            `
        });
        html += "</ul>";
        $("#experimentResults").html(html);
    });
}

/**
 * Fires an ajax request to the search rest controller to retrieve suggestions based on the input query in the delete
 * experiment input field on the course page for the course in question. On success, all retrieved suggestions are added
 * to the delete experiment result div to be displayed as a list of experiment ids and titles that matched the query.
 */
function getCourseExperimentDeleteSuggestions() {
    let request = $.ajax({
        dataType: "json",
        url: contextPath + "/search/course/delete/experiment",
        delay: 250,
        data: {query: $('#deleteExperimentInput').val(), id: $('#deleteExperimentId').val()}
    });

    request.done(function(result) {
        let html = "<ul class='list-group'>";
        result.forEach(function(element) {
            html += `
            <li class="list-group-item list-group-item-action">
                <div class='ms-2 me-auto no_decoration'
                onclick="setDeleteExperimentInput(this.getElementsByClassName('fw-bold')[0])">
                    <span>${sanitize(element[1])}</span>
                    <span class="fw-bold">${sanitize(element[2])}</span>
                </div>
            </li>
            `
        });
        html += "</ul>";
        $("#deleteExperimentResults").html(html);
    });
}

/**
 * Sets the value of the experiment input field on the course page to the given value.
 *
 * @param element The value to be set.
 */
function setExperimentInput(element) {
    document.getElementById("experimentInput").value = element.innerText;
}

/**
 * Sets the value of the delete experiment input field on the course page to the given value.
 *
 * @param element The value to be set.
 */
function setDeleteExperimentInput(element) {
    document.getElementById("deleteExperimentInput").value = element.innerText;
}
