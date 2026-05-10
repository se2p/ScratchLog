import {addClickEventListenerCloseModal, addClickEventListenerOpenModal} from "./eventListeners.js";
import {getSearchSuggestions} from "./getSearchSuggestions.js";

let deleteExperimentModal = document.getElementById("deleteExperimentModal");
let stopExperimentModal = document.getElementById("stopExperimentModal");
let addModal = document.getElementById("addParticipantModal");
let addParticipantsCsvModal = document.getElementById("addParticipantCsvModal");
let deleteParticipantModal = document.getElementById("deleteParticipantModal");
let deleteSb3Modal = document.getElementById("deleteSb3Modal");

/**
 * Readies all necessary event listeners for buttons on the experiment page and enables suggestion/multi-select
 * functionality within the modals.
 */
$(document).ready(function () {
    addEventListeners();

    $("#addParticipantsInput").on("keyup", function() {
        getSearchSuggestions(
            "/search/user",
            { query: $("#addParticipantsInput").val(), id: $("#experimentId").val() },
            "addParticipantsSelect",
        )
    });

    $("#deleteParticipantsInput").on("keyup", function() {
        getSearchSuggestions(
            "/search/delete",
            { query: $("#deleteParticipantsInput").val(), id: $("#deleteParticipantsExperimentId").val() },
            "deleteParticipantsSelect"
        )
    });
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
