/**
 * Readies the functions to retrieve user suggestions on the experiment page from the search rest controller on user
 * input in the respective input fields.
 */
$(document).ready(function () {
    $("#participantInput").keyup(function() {
        $('#results').html("");
        getUserSuggestions();
    });

    $("#deleteParticipantInput").keyup(function() {
        $("#deleteResults").html("");
        getUserDeleteSuggestions();
    });
});

/**
 * Fires an ajax request to the search rest controller to retrieve suggestions based on the input query in the
 * participant input field on the experiment page for the experiment in question. On success, all retrieved suggestions
 * are added to the result div to be displayed as a list of usernames and emails that matched the query.
 */
function getUserSuggestions() {
    let request = $.ajax({
        dataType: "json",
        url: "/search/user",
        delay: 250,
        data: {query: $('#participantInput').val(), id: $('#experimentId').val()}
    });

    request.done(function(result) {
        let html = "<ul class='list-group'>";
        result.forEach(function(element) {
            html += "<li class='list-group-item list-group-item-action'><div class='ms-2 me-auto'>"
                + "<div class='fw-bold' onclick='setParticipantInput(this.innerHTML)'>" + element[0] + "</div>"
                + "<div onclick='setParticipantInput(this.innerHTML)'>" + element[1] + "</div>" + "</div></li>";
        });
        html += "</ul>";
        $("#results").html(html);
    });
}

/**
 * Fires an ajax request to the search rest controller to retrieve suggestions based on the input query in the delete
 * participant input field on the experiment page for the experiment in question. On success, all retrieved suggestions
 * are added to the delete result div to be displayed as a list of usernames and emails that matched the query.
 */
function getUserDeleteSuggestions() {
    let request = $.ajax({
        dataType: "json",
        url: "/search/delete",
        delay: 250,
        data: {query: $('#deleteParticipantInput').val(), id: $('#deleteId').val()}
    });

    request.done(function(result) {
        let html = "<ul class='list-group'>";
        result.forEach(function(element) {
            html += "<li class='list-group-item list-group-item-action'><div class='ms-2 me-auto'>"
                + "<div class='fw-bold' onclick='setDeleteInput(this.innerHTML)'>" + element[0] + "</div>"
                + "<div onclick='setDeleteInput(this.innerHTML)'>" + element[1] + "</div>" + "</div></li>";
        });
        html += "</ul>";
        $("#deleteResults").html(html);
    });
}

/**
 * Sets the value of the participant input field on the experiment page to the given value.
 *
 * @param element The value to be set.
 */
function setParticipantInput(element) {
    document.getElementById("participantInput").value = element;
}

/**
 * Sets the value of the delete participant input field on the experiment page to the given value.
 *
 * @param element The value to be set.
 */
function setDeleteInput(element) {
    document.getElementById("deleteParticipantInput").value = element;
}
