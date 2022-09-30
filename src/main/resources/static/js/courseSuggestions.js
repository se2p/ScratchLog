/**
 * Adds the event listeners for providing suggestions on experiments or participants on the course page.
 */
function addKeyupFunctions() {
    document.getElementById("participantInput").addEventListener("keyup", function () {
        getCourseParticipantSuggestions();
    });
    document.getElementById("deleteParticipantInput").addEventListener("keyup", function () {
        getCourseParticipantDeleteSuggestions();
    });
    document.getElementById("deleteExperimentInput").addEventListener("keyup", function () {
        getCourseExperimentDeleteSuggestions();
    });
}

/**
 * Fires an ajax request to the search REST controller to retrieve suggestions based on the input query in the
 * participant input field on the course page for the course in question. On success, all retrieved suggestions are
 * added to the participant result div to be displayed as a list of participant usernames and emails matching the query.
 */
function getCourseParticipantSuggestions() {
    let request = $.ajax({
        dataType: "json",
        url: contextPath + "/search/course/participant",
        delay: 250,
        data: {query: $('#participantInput').val(), id: $('#addParticipantId').val()}
    });

    request.done(function(result) {
        let html = "<ul class='list-group'>";
        result.forEach(function(element) {
            html += `
            <li class="list-group-item list-group-item-action">
                <div class='ms-2 me-auto no_decoration'
                onclick="setParticipantInput(this.getElementsByClassName('fw-bold')[0])">
                    <div class="fw-bold">${sanitize(element[0])}</div>
                    <div>${sanitize(element[1])}</div>
                </div>
            </li>
            `
        });
        html += "</ul>";
        $("#participantResults").html(html);
    });
}

/**
 * Fires an ajax request to the search REST controller to retrieve suggestions based on the input query in the delete
 * participant input field on the course page for the course in question. On success, all retrieved suggestions are
 * added to the delete participant result div to be displayed as a list of participant usernames and emails matching
 * the query.
 */
function getCourseParticipantDeleteSuggestions() {
    let request = $.ajax({
        dataType: "json",
        url: contextPath + "/search/course/delete/participant",
        delay: 250,
        data: {query: $('#deleteParticipantInput').val(), id: $('#deleteParticipantId').val()}
    });

    request.done(function(result) {
        let html = "<ul class='list-group'>";
        result.forEach(function(element) {
            html += `
            <li class="list-group-item list-group-item-action">
                <div class='ms-2 me-auto no_decoration'
                onclick="setDeleteParticipantInput(this.getElementsByClassName('fw-bold')[0])">
                    <div class="fw-bold">${sanitize(element[0])}</div>
                    <div>${sanitize(element[1])}</div>
                </div>
            </li>
            `
        });
        html += "</ul>";
        $("#deleteParticipantResults").html(html);
    });
}

/**
 * Fires an ajax request to the search REST controller to retrieve suggestions based on the input query in the delete
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
 * Sets the value of the participant input field on the course page to the given value.
 *
 * @param element The value to be set.
 */
function setParticipantInput(element) {
    document.getElementById("participantInput").value = element.innerText;
}

/**
 * Sets the value of the delete participant input field on the course page to the given value.
 *
 * @param element The value to be set.
 */
function setDeleteParticipantInput(element) {
    document.getElementById("deleteParticipantInput").value = element.innerText;
}

/**
 * Sets the value of the delete experiment input field on the course page to the given value.
 *
 * @param element The value to be set.
 */
function setDeleteExperimentInput(element) {
    document.getElementById("deleteExperimentInput").value = element.innerText;
}
