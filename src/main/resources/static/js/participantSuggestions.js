$(document).ready(function () {
    $("#participantInput").keyup(function () {
        $('#results').html("");
        getUserSuggestions();
    });
});

function getUserSuggestions() {
    $.getJSON("/search/user", {query: $('#participantInput').val(), id: $('#experimentId').val()},
        function(result) {
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

function setParticipantInput(element) {
    document.getElementById("participantInput").value = element;
}
