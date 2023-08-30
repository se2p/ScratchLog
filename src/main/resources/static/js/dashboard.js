import {redirectErrorPage} from "./errorRedirect.js";

let started = document.getElementById("started");
let totalStarted = document.getElementById("totalStarted");
let finished = document.getElementById("finished");
let totalFinished = document.getElementById("totalFinished");
let participants = [];
let selectedParticipants = [];

/**
 * Readies the function to retrieve the information to be displayed on the dashboard.
 */
$(document).ready(function () {
    getDashboardInfo();
});

/**
 * Retrieves the data to be displayed on the dashboard from the database. If an error occurred during data fetching, the
 * user is redirected to the error page. This function is executed periodically to refresh the data.
 */
function getDashboardInfo() {
    fetchExperimentData();
    fetchParticipantData();
    setTimeout(getDashboardInfo, 60000);
}

/**
 * Fetches the experiment data containing information about the number of participants from the database. If an error
 * occurred during data fetching, the user is redirected to the error page.
 */
function fetchExperimentData() {
    $.ajax({
        type: "get",
        url: contextPath + "/dashboard/data",
        data: {id: experimentId},
        success: function(data) {
            totalStarted.innerText = data[0];
            totalFinished.innerText = data[0];
            started.innerText = data[1];
            finished.innerText = data[2];
        },
        error: function() {
            redirectErrorPage();
        }
    });
}

/**
 * Fetches the ids and usernames of all participants from the database. If an error occurred during data fetching, the
 * user is redirected to the error page.
 */
function fetchParticipantData() {
    $.ajax({
        type: "get",
        url: contextPath + "/dashboard/data/participants",
        data: {id: experimentId},
        success: function(data) {
            participants = data;
            updateSelectedParticipants();
            addSelectedParticipants();
            fillParticipantsDropdown();
            addEventListeners();
        },
        error: function() {
            redirectErrorPage();
        }
    });
}

/**
 * Updates the selected participants list based on the fetched participant data.
 */
function updateSelectedParticipants() {
    if (selectedParticipants.length === 0) {
        let numSelected = participants.length > 5 ? 5 : participants.length;

        for (let i = 0; i < numSelected; i++) {
            selectedParticipants.push(participants[i][1]);
        }
    } else {
        selectedParticipants = selectedParticipants.filter(
            item => !participants.includes(part => part[1] === item));

        if (selectedParticipants.length === 0) {
            selectedParticipants.push(participants[0][1])
        }
    }
}

/**
 * Adds the selected participants as a list to the dashboard page to be displayed.
 */
function addSelectedParticipants() {
    let html = `<ul class="list-group list-group-horizontal flex-wrap mx-3">`;
    selectedParticipants.forEach(function (participant) {
        html += `
        <li class="list-group-item">
            <span>${participant}</span>
            <i aria-hidden="true" class="fas fa-trash-alt"></i>
        </li>
        `
    });
    html += `</ul>`
    $("#selectedParticipants").html(html);
}

/**
 * Fills the participant dropdown menu with usernames of participants who are not currently selected.
 */
function fillParticipantsDropdown() {
    let html;
    participants.forEach(function (participant) {
        if (!selectedParticipants.includes(participant[1])) {
            html += `
            <option class="text-dark">${participant[1]}</option>
            `
        }
    });
    $("#participants").html(html);
}

/**
 * Adds event listeners to all elements of the selected participants list to allow removal of selected participants and
 * to all elements of the participants dropdown menu to allow adding participants as selected.
 */
function addEventListeners() {
    let listElements = document.getElementsByClassName("fas fa-trash-alt");
    Array.from(listElements).forEach(element => element.addEventListener("click", function() {
        removeSelectedParticipant(element.previousElementSibling.innerHTML)
    }));
    let dropdownElements = document.getElementsByClassName("text-dark");
    Array.from(dropdownElements).forEach(element => element.addEventListener("click", function() {
        addSelectedParticipant(element.firstChild.nodeValue);
    }));
}

/**
 * Adds a new participant to the selected participants lists and updates the displayed list and dropdown menu
 * accordingly.
 *
 * @param participant The username of the participant to be added.
 */
function addSelectedParticipant(participant) {
    if (selectedParticipants.length < 10) {
        const index = participants.indexOf(item => item[1] === participant);
        selectedParticipants.push(participant);
        participants.splice(index, 1);
        addSelectedParticipants();
        fillParticipantsDropdown();
        addEventListeners();
        changeDropdownVisibility();
    }
}

/**
 * Removes the given participant from the selected participants list and updates the displayed list and dropdown menu
 * accordingly.
 *
 * @param participant The username of the participant to be removed.
 */
function removeSelectedParticipant(participant) {
    if (selectedParticipants.length > 1) {
        selectedParticipants = selectedParticipants.filter(item => item !== participant);
        addSelectedParticipants();
        fillParticipantsDropdown();
        addEventListeners();
        changeDropdownVisibility();
    }
}

/**
 * Changes the visibility of the participants dropdown menu to allow only a limited number of participant data to be
 * displayed at once.
 */
function changeDropdownVisibility() {
    if (selectedParticipants.length >= 10) {
        document.getElementById("participants").style.display = "none";
        document.getElementById("participantsLabel").style.display = "none";
    } else {
        document.getElementById("participants").style.display = "block";
        document.getElementById("participantsLabel").style.display = "block";
    }
}
