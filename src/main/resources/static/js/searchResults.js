let userButton = document.getElementById("userButton");
let userBody = document.getElementById("userBody");
let userPage = 1;
let experimentButton = document.getElementById("experimentButton");
let experimentBody = document.getElementById("experimentBody");
let experimentPage = 1;
let courseButton = document.getElementById("courseButton");
let courseBody = document.getElementById("courseBody");
let coursePage = 1;
let query = new URL(window.location.href).searchParams.get("query");

/**
 * Readies the functions to retrieve more user or experiment results from the search rest controller upon clicking the
 * respective button .
 */
$(document).ready(function () {
    $("#userResults").click(function() {
        loadUserResults();
    });

    $("#experimentResults").click(function() {
        loadExperimentResults();
    });

    $("#courseResults").click(function() {
        loadCourseResults();
    });
});

/**
 * Fires an ajax request to the search rest controller to retrieve more user results to display based on the input query
 * in the search header bar. On success, all retrieved user results are added to the user result table to be displayed
 * on the search page. If, as a result of this request, all user results matching the query have been retrieved, the
 * button to load more user results is hidden.
 */
function loadUserResults() {
    let request = $.ajax({
        dataType: "json",
        url: contextPath + "/search/users",
        data: {query: query, page: userPage}
    });

    request.done(function(result) {
        userPage++;
        let html = userBody.innerHTML;
        result.forEach(function(element) {
            html += `
            <tr>
                <th scope="row">
                    <a class="no_decoration" href="${contextPath}/users/profile?name=${element[1]}">${element[0]}</a>
                </th>
                <td>
                    <a class="no_decoration" href="${contextPath}/users/profile?name=${element[1]}">${element[1]}</a>
                </td>
                <td>
                    <a class="no_decoration" href="${contextPath}/users/profile?name=${element[1]}">
                        ${sanitize(element[2])}
                    </a>
                </td>
                <td>
                    <a class="no_decoration" href="${contextPath}/users/profile?name=${element[1]}">${element[3]}</a>
                </td>
            </tr>
            `
        });
        userBody.innerHTML = html;
        hideUserResultButton();
    });
}

/**
 * Fires an ajax request to the search rest controller to retrieve more experiment results to display based on the input
 * query in the search header bar. On success, all retrieved experiment results are added to the experiment result table
 * to be displayed on the search page. If, as a result of this request, all experiment results matching the query have
 * been retrieved, the button to load more experiment results is hidden.
 */
function loadExperimentResults() {
    let request = $.ajax({
        dataType: "json",
        url: contextPath + "/search/experiments",
        data: {query: query, page: experimentPage}
    });

    request.done(function(result) {
        experimentPage++;
        let html = experimentBody.innerHTML;
        result.forEach(function(element) {
            html += `
            <tr>
                <th scope="row">
                    <a class="no_decoration" href="${contextPath}/experiment?id=${element[0]}">${element[0]}</a>
                </th>
                <td>
                    <a class="no_decoration" href="${contextPath}/experiment?id=${element[0]}">
                        ${sanitize(element[1])}
                    </a>
                </td>
                <td>
                    <a class="no_decoration" href="${contextPath}/experiment?id=${element[0]}">
                        ${sanitize(element[2])}
                    </a>
                </td>
            </tr>
            `
        });
        experimentBody.innerHTML = html;
        hideExperimentResultButton();
    });
}

/**
 * Fires an ajax request to the search rest controller to retrieve more course results to display based on the input
 * query in the search header bar. On success, all retrieved course results are added to the course result table
 * to be displayed on the search page. If, as a result of this request, all course results matching the query have
 * been retrieved, the button to load more course results is hidden.
 */
function loadCourseResults() {
    let request = $.ajax({
        dataType: "json",
        url: contextPath + "/search/courses",
        data: {query: query, page: coursePage}
    });

    request.done(function(result) {
        coursePage++;
        let html = courseBody.innerHTML;
        result.forEach(function(element) {
            html += `
            <tr>
                <th scope="row">
                    <a class="no_decoration" href="${contextPath}/course?id=${element[0]}">${element[0]}</a>
                </th>
                <td>
                    <a class="no_decoration" href="${contextPath}/course?id=${element[0]}">
                        ${sanitize(element[1])}
                    </a>
                </td>
                <td>
                    <a class="no_decoration" href="${contextPath}/course?id=${element[0]}">
                        ${sanitize(element[2])}
                    </a>
                </td>
            </tr>
            `
        });
        courseBody.innerHTML = html;
        hideCourseResultButton();
    });
}

/**
 * Checks, whether the total amount of user results matching the current query string is smaller than or equal to the
 * current amount of user results being displayed. If so, the button to load additional user results is hidden.
 */
function hideUserResultButton() {
    if (userCount <= userPage * pageSize) {
        userButton.style.visibility = "hidden";
    }
}

/**
 * Checks, whether the total amount of experiment results matching the current query string is smaller than or equal to
 * the current amount of experiment results being displayed. If so, the button to load additional experiment results is
 * hidden.
 */
function hideExperimentResultButton() {
    if (experimentCount <= experimentPage * pageSize) {
        experimentButton.style.visibility = "hidden";
    }
}

/**
 * Checks, whether total amount of course results matching the current query string is smaller than or equal to the
 * current amount of course results being displayed. If so, the button to load additional course results is hidden.
 */
function hideCourseResultButton() {
    if (courseCount <= coursePage * pageSize) {
        courseButton.style.visibility = "hidden";
    }
}
