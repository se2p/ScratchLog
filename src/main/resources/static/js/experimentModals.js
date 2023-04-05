import {addClickEventListenerCloseModal, addClickEventListenerOpenModal} from "./eventListeners.js";

let deleteModal = document.getElementById("openDelete");
let stopModal = document.getElementById("openStop");
let addModal = document.getElementById("openAdd");
let deleteParticipantModal = document.getElementById("openDeleteParticipant");
let deleteSb3Modal = document.getElementById("openDeleteSb3");

/**
 * Readies all necessary event listeners for buttons on the experiment page.
 */
$(document).ready(function () {
    addEventListeners();
});

/**
 * Adds the required event listeners to open and close all modals on the experiment page.
 */
function addEventListeners() {
    addClickEventListenerOpenModal("delete", deleteModal);
    addClickEventListenerCloseModal("abortDelete", deleteModal);
    addClickEventListenerOpenModal("close", stopModal);
    addClickEventListenerCloseModal("abortStop", stopModal);
    addClickEventListenerOpenModal("add", addModal);
    addClickEventListenerCloseModal("abortAdd", addModal);
    addClickEventListenerOpenModal("deleteParticipant", deleteParticipantModal);
    addClickEventListenerCloseModal("abortDeleteParticipant", deleteParticipantModal);
    addClickEventListenerOpenModal("deleteSb3", deleteSb3Modal);
    addClickEventListenerCloseModal("abortSb3Delete", deleteSb3Modal);
}
