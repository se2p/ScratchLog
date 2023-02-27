/**
 * Opens the given modal and adds a filter to the page so that the page content behind the popup modal is blurred.
 *
 * @param element The modal to be opened.
 */
function openModal(element) {
    let content = document.getElementById("content");
    element.style.display = "block";
    content.style.filter = "blur(4px)";
}

/**
 * Closes the given modal and removes the filter on the page content.
 *
 * @param element The modal to be closed.
 */
function closeModal(element) {
    let content = document.getElementById("content");
    element.style.display = "none";
    content.style.filter = "none";
}

export {openModal, closeModal}
