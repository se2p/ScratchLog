let deleteModal = document.getElementById("openDelete");
let deleteButton = document.getElementById("delete");
let abortDelete = document.getElementById("abortDelete");

let addModal = document.getElementById("openAdd");
let addButton = document.getElementById("add");
let abortAdd = document.getElementById("abortAdd");

deleteButton.onclick = function() {
    openModal(deleteModal);
}

abortDelete.onclick = function () {
    closeModal(deleteModal);
}

addButton.onclick = function() {
    openModal(addModal);
}

abortAdd.onclick = function() {
    closeModal(addModal);
}

function openModal(element) {
    let content = document.getElementById("content");
    element.style.display = "block";
    content.style.filter = "blur(4px)";
}

function closeModal(element) {
    let content = document.getElementById("content");
    element.style.display = "none";
    content.style.filter = "none";
}
