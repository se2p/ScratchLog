import {addClickEventListenerCloseModal, addClickEventListenerOpenModal} from "./eventListeners.js";

let deleteExperimentModal = document.getElementById("deleteExperimentModal");
let stopExperimentModal = document.getElementById("stopExperimentModal");
let addModal = document.getElementById("addParticipantModal");
let addParticipantsCsvModal = document.getElementById("addParticipantCsvModal");
let deleteParticipantModal = document.getElementById("deleteParticipantModal");
let deleteSb3Modal = document.getElementById("deleteSb3Modal");

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
    addClickEventListenerOpenModal("openDeleteExperimentModal", deleteExperimentModal);
    addClickEventListenerCloseModal("abortDeleteExperiment", deleteExperimentModal);
    addClickEventListenerOpenModal("openStopExperimentModal", stopExperimentModal);
    addClickEventListenerCloseModal("abortStopExperiment", stopExperimentModal);
    addClickEventListenerOpenModal("openAddParticipantModal", addModal);
    addClickEventListenerCloseModal("abortAddParticipant", addModal);
    addClickEventListenerOpenModal("openAddParticipantCsvModal", addParticipantsCsvModal);
    addClickEventListenerCloseModal("abortAddParticipantsCsv", addParticipantsCsvModal);
    addClickEventListenerOpenModal("openDeleteParticipantModal", deleteParticipantModal);
    addClickEventListenerCloseModal("abortDeleteParticipant", deleteParticipantModal);
    addClickEventListenerOpenModal("openDeleteSb3Modal", deleteSb3Modal);
    addClickEventListenerCloseModal("abortSb3Delete", deleteSb3Modal);
}
