import {addClickEventListenerCloseModal, addClickEventListenerOpenModal} from "./eventListeners.js";

let deleteExperimentModal = document.getElementById("openDeleteExperiment");
let stopExperimentModal = document.getElementById("openStopExperiment");
let addModal = document.getElementById("openAddParticipant");
let addParticipantsCsvModal = document.getElementById("openAddParticipantCsv");
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
    addClickEventListenerOpenModal("deleteExperiment", deleteExperimentModal);
    addClickEventListenerCloseModal("abortDeleteExperiment", deleteExperimentModal);
    addClickEventListenerOpenModal("stopExperiment", stopExperimentModal);
    addClickEventListenerCloseModal("abortStopExperiment", stopExperimentModal);
    addClickEventListenerOpenModal("addParticipant", addModal);
    addClickEventListenerCloseModal("abortAddParticipant", addModal);
    addClickEventListenerOpenModal("addParticipantsCsv", addParticipantsCsvModal);
    addClickEventListenerCloseModal("abortAddParticipantsCsv", addParticipantsCsvModal);
    addClickEventListenerOpenModal("deleteParticipant", deleteParticipantModal);
    addClickEventListenerCloseModal("abortDeleteParticipant", deleteParticipantModal);
    addClickEventListenerOpenModal("deleteSb3", deleteSb3Modal);
    addClickEventListenerCloseModal("abortSb3Delete", deleteSb3Modal);
}
