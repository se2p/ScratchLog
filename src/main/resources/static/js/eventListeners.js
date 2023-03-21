import {closeModal, openModal} from "./modals.js";

/**
 * Adds a click event listener calling the given function if the element with the given id can be found.
 *
 * @param elementId The id of the element.
 * @param callFunction The function to be called on click.
 */
function addClickEventListener(elementId, callFunction) {
    if (document.getElementById(elementId)) {
        document.getElementById(elementId).addEventListener("click", callFunction);
    }
}

/**
 * Adds a click event listener opening the given modal if the element with the given id can be found.
 *
 * @param elementId The id of the element.
 * @param modal The modal to be opened.
 */
function addClickEventListenerOpenModal(elementId, modal) {
    if (document.getElementById(elementId)) {
        document.getElementById(elementId).addEventListener("click", function () {
            openModal(modal);
        });
    }
}

/**
 * Adds a click event listener closing the given modal if the element with the given id can be found.
 *
 * @param elementId The id of the element.
 * @param modal The modal to be closed.
 */
function addClickEventListenerCloseModal(elementId, modal) {
    if (document.getElementById(elementId)) {
        document.getElementById(elementId).addEventListener("click", function () {
            closeModal(modal);
        });
    }
}

export {addClickEventListener, addClickEventListenerOpenModal, addClickEventListenerCloseModal}
