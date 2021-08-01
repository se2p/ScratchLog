/**
 * Readies the functions to retrieve user or experiment suggestions on user input in the header search area from the
 * search rest controller.
 */
$(document).ready(function () {
    $("#search").keyup(function() {
        $('#searchResults').html("");
        getSuggestions();
    });
});

/**
 * Fires an ajax request to the search rest controller to retrieve suggestions based on the input query in the
 * participant input field on the experiment page for the experiment in question. On success, all retrieved suggestions
 * are added to the result div to be displayed as a list of usernames and emails that matched the query.
 */
function getSuggestions() {
    let request = $.ajax({
        dataType: "json",
        url: "/search/suggestions",
        delay: 250,
        data: {query: $('#search').val()}
    });

    request.done(function(result) {
        let html = "<ul class='list-group'>";
        result.forEach(function(element) {
            html += "<li class='list-group-item list-group-item-action'><a class='no_decoration'"
                + " onclick='setHref(this.firstChild.innerHTML)'><span class='me-2'>"
                + element[0] + "</span><span>" + element[1] + "</span></a></li>";
        });
        html += "</ul>";
        $("#searchResults").html(html);
    });
}

/**
 * Sets link location value to redirect the user to the corresponding profile or experiment page depending on whether
 * the value passed consists only of numbers and is thus an experiment id, or not.
 *
 * @param value The experiment id or user name.
 */
function setHref(value) {
    let regex=/^[0-9]+$/;

    if (value.match(regex)) {
        location.href = "/experiment?id=" + value;
    } else {
        location.href = "/users/profile?name=" + value;
    }
}
