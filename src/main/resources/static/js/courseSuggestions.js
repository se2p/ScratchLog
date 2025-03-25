/**
 * Adds the event listeners for providing suggestions on experiments or participants on the course page.
 */
function addKeyupFunctions() {
    document.getElementById("participantInput").addEventListener("keyup", function () {
        getParticipantSuggestions(
            "/search/course/participant",
            { query: $("#participantInput").val(), id: $('#addParticipantId').val() },
            "addParticipantsSelect",
        );
    });
    document.getElementById("deleteParticipantInput").addEventListener("keyup", function () {
        getParticipantSuggestions(
            "/search/course/delete/participant",
            { query: $('#deleteParticipantInput').val(), id: $('#deleteParticipantId').val() },
            "deleteParticipantsSelect"
        )
    });
    document.getElementById("deleteExperimentInput").addEventListener("keyup", function () {
        getCourseExperimentDeleteSuggestions();
    });
}


/**
 * Fires an AJAX request to the given search URL with the given search data to retrieve participant suggestions.
 * On success, all retrieved suggestions are added as options to the select element with the given id, displaying
 * username and email of the suggested user.
 * Assumes the requested URL returns an array of 2-element arrays of username and email.
 *
 * @param searchUrl The URL of the search controller to make the request to.
 * @param searchData The data passed to the controller, as an object.
 * @param selectElementId The ID of the <select> element to fill with the suggested options.
 */
function getParticipantSuggestions(searchUrl, searchData, selectElementId) {
    const request = $.ajax({
        dataType: "json",
        url: contextPath + searchUrl,
        delay: 250,
        data: searchData,
    });

    request.done(function(result) {
        const participantsSelect = document.getElementById(selectElementId);
        let html = "";

        participantsSelect.size = participantsSelect.length;

        // Use display-none to hide the select element when there are currently no suggestions.
        if (result.length === 0) {
            participantsSelect.classList.add("d-none");
        } else {
            // Shrink the select if the returned options do not fill its entire height.
            participantsSelect.size = participantsSelect.length;
            participantsSelect.classList.remove("d-none");

            result.forEach(([username, email]) =>  {
                html += `
                <option value="${sanitize(username)}" class="list-group-item list-group-item-action p-3">
                    ${sanitize(username)} (${sanitize(email)})
                </option>
                `
            });
        }

        participantsSelect.innerHTML = html;
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
 * Sets the value of the delete experiment input field on the course page to the given value.
 *
 * @param element The value to be set.
 */
function setDeleteExperimentInput(element) {
    document.getElementById("deleteExperimentInput").value = element.innerText;
}
