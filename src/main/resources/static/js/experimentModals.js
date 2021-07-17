let deleteModal = document.getElementById("openDelete");
let deleteButton = document.getElementById("delete");
let abortDelete = document.getElementById("abortDelete");

let stopModal = document.getElementById("openStop");
let stopButton = document.getElementById("close");
let abortStop = document.getElementById("abortStop");

let addModal = document.getElementById("openAdd");
let addButton = document.getElementById("add");
let abortAdd = document.getElementById("abortAdd");

let deleteParticipantModal = document.getElementById("openDeleteParticipant");
let deleteParticipantButton = document.getElementById("deleteParticipant");
let abortDeleteParticipant = document.getElementById("abortDeleteParticipant");

let deleteSb3Modal = document.getElementById("openDeleteSb3");
let deleteSb3Button = document.getElementById("deleteSb3");
let abortDeleteSb3 = document.getElementById("abortSb3Delete");

/**
 * Configures the delete experiment button on the experiment page to open the delete experiment modal when the button is
 * clicked.
 */
deleteButton.onclick = function() {
    openModal(deleteModal);
}

/**
 * Configures the abort delete button in the delete experiment modal to close the modal when the button is clicked.
 */
abortDelete.onclick = function() {
    closeModal(deleteModal);
}

/**
 * Configures the stop experiment button on the experiment page to open the stop experiment modal when the button is
 * clicked.
 */
stopButton.onclick = function() {
    openModal(stopModal);
}

/**
 * Configures the abort stop button in the stop experiment modal to close the modal when the button is clicked.
 */
abortStop.onclick = function() {
    closeModal(stopModal);
}

/**
 * Configures the add existing participant button on the experiment page to open the add participant modal when the
 * button is clicked.
 */
addButton.onclick = function() {
    openModal(addModal);
}

/**
 * Configures the abort add button in the add participant modal to close the modal when the button is clicked.
 */
abortAdd.onclick = function() {
    closeModal(addModal);
}

/**
 * Configures the delete participant button on the experiment page to open the delete participant modal when the button
 * is clicked.
 */
deleteParticipantButton.onclick = function() {
    openModal(deleteParticipantModal);
}

/**
 * Configures the abort button in the delete participant modal to close the modal when the button is clicked.
 */
abortDeleteParticipant.onclick = function() {
    closeModal(deleteParticipantModal);
}

/**
 * Configures the delete sb3 file button on the experiment page to open the delete sb3 modal when the button is clicked.
 */
deleteSb3Button.onclick = function() {
    openModal(deleteSb3Modal);
}

/**
 * Configures the abort button in the delete sb3 modal to close the modal when the button is clicked.
 */

abortDeleteSb3.onclick = function() {
    closeModal(deleteSb3Modal);
}

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
