/**
 * Fires an AJAX request to the given search URL with the given search data to retrieve search suggestions.
 * On success, all retrieved suggestions are added as options (constructed by `makeOption`) to the select element with
 * the given id.
 *
 * @param searchUrl The URL of the search controller to make the request to.
 * @param searchData The data passed to the controller, as an object.
 * @param selectElementId The ID of the <select> element to fill with the suggested options.
 * @param makeOption A function that transforms one item of the result returned by the search controller to an
 *                   {@link Option}. By default, constructs an option displaying username and email for use
 *                   with participant suggestions.
 */
export function getSearchSuggestions(searchUrl, searchData, selectElementId, makeOption = makeParticipantOption) {
    const request = $.ajax({
        dataType: "json",
        url: contextPath + searchUrl,
        delay: 250,
        data: searchData,
    });

    request.fail(function (_result) {
        console.error("Retrieving search suggestions failed.");
    });

    request.done(function(result) {
        const selectElement = document.getElementById(selectElementId);

        // Use display-none to hide the select element when there are currently no suggestions.
        if (result.length === 0) {
            selectElement.classList.add("d-none");
        } else {
            const options = result.map(resultElement => {
                const option = makeOption(resultElement)
                option.classList.add("list-group-item", "list-group-item-action", "p-3");
                return option;
            });
            selectElement.replaceChildren(...options);

            // Shrink the select if the returned options do not fill its entire height.
            selectElement.size = selectElement.length;
            selectElement.classList.remove("d-none");
        }
    });
}

/**
 * Constructs an Option from a pair of username and email.
 * @returns {HTMLOptionElement} The constructed option.
 */
const makeParticipantOption = ([username, email]) => new Option(
    `${sanitize(username)} (${sanitize(email)})`,
    sanitize(username)
);
