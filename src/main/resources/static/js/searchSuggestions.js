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
        url: contextPath + "/search/suggestions",
        delay: 250,
        data: {query: $('#search').val()}
    });

    request.done(function(result) {
        let html = "<ul class='list-group'>";
        result.forEach(function(element) {
            html += `
            <li class="list-group-item list-group-item-action">
                <a class="no_decoration"
                onclick="setHref(this.firstChild.innerText)"><span style="display: none">${sanitize(element[0])}</span>
                    <span class="me-2">${sanitize(element[1])}</span>
                    <span>${sanitize(element[2])}</span>
                </a>
            </li>
            `
        });
        html += "</ul>";
        $("#searchResults").html(html);
    });
}

/**
 * Sets link location value to redirect the user to the corresponding profile, experiment or course.
 *
 * @param value The course or experiment id or the username.
 */
function setHref(value) {
   location.href = contextPath + value;
}

/**
 * Sanitizes the passed data by replacing dangerous characters to avoid code injections.
 *
 * @param data The data to be sanitized.
 * @returns {any} The sanitized data.
 */
function sanitize(data) {
    const tagsToReplace = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '(': '%28',
        ')': '%29',
    }

    let str = JSON.stringify(data)

    const replaceTag = function(tag) {
        return tagsToReplace[tag] || tag
    }

    const safe_tags_replace = function(str) {
        return str.replace(/[&<>()]/g, replaceTag)
    }

    str = safe_tags_replace(str)
    return JSON.parse(str)
}
