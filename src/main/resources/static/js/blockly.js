var blocklyArea = document.getElementById('blocklyArea');
var blocklyDiv = document.getElementById('blocklyDiv');
var toolbox = document.getElementById('toolbox');
var workspace = Blockly.inject('blocklyDiv', {toolbox: toolbox});
var blocklyCode = '';

let xmlButton = document.getElementById("xml");
let jsonButton = document.getElementById("json");
let sb3Button = document.getElementById("sb3");
let downloadRangeButton = document.getElementById("downloadRange");

let current = document.getElementById("current");
let time = document.getElementById("time");
let sprite = document.getElementById("sprite");
let first = document.getElementById("first");
let prev = document.getElementById("prev");
let next = document.getElementById("next");
let last = document.getElementById("last");

let slider = document.getElementById("slider");
let sliderValues = [
    document.getElementById("from"),
    document.getElementById("to")
];
let rangeLastFile = document.getElementById("rangeLastFileCheck");

let count = 0;
let pos = 0;
let page = 0;
let xml = [];

/**
 * Sets the coordinates and dimensions of the blockly area on the result page.
 * @param e
 */
var onresize = function (e) {
    // Compute the absolute coordinates and dimensions of blocklyArea.
    var element = blocklyArea;
    var x = 0;
    var y = 0;
    do {
        x += element.offsetLeft;
        y += element.offsetTop;
        element = element.offsetParent;
    } while (element);
    // Position blocklyDiv over blocklyArea.
    blocklyDiv.style.left = 0 + 'px';
    blocklyDiv.style.top = 0 + 'px';
    blocklyDiv.style.right = 20 + "px";
    blocklyDiv.style.width = blocklyArea.offsetWidth + 'px';
    blocklyDiv.style.height = blocklyArea.offsetHeight + 'px';
};

/**
 * Renders the blockly div on the result page.
 */
var renderBlockly = function () {
    this.blocklyDiv.innerHTML = '';
    this.workspace = Blockly.inject('blocklyDiv',
        {toolbox: null,
            readOnly: true,
            scrollbars:  true
        });
    this.workspace.options.pathToMedia = window.location.origin + contextPath
        + "/static/node_modules/scratch-blocks/media/";
    if(blocklyCode) {
        var xml = Blockly.Xml.textToDom(blocklyCode);
        Blockly.Xml.domToWorkspace(xml, this.workspace);
        this.workspace.scrollCenter();
    }
};

renderBlockly();
window.addEventListener('resize', onresize, false);
onresize();
Blockly.svgResize(workspace);
window.dispatchEvent(new CustomEvent("_blocklyReady", {detail: {}, bubbles: true, composed: true}));

/**
 * Sets the code displayed in the blockly div to the xml code at the current position.
 * @param position The current position.
 */
var setCode = function(position){
    blocklyCode = xml[position].xml;
    renderBlockly();
};

/**
 * Sets the display values for the navigation buttons below the blockly area and retrieves the first page of block event
 * projections from the result controller.
 */
document.addEventListener("DOMContentLoaded", function () {
    if (total > 0) {
        first.style.display = "none";
        prev.style.display = "none";

        if (total < 2) {
            next.style.display = "none";
            last.style.display = "none";
        }

        getXML();
    }
});

/**
 * Makes the xml code at the current array position available for download in a file.
 */
xmlButton.addEventListener("click", function () {
    let xmlElem = document.createElement("a");
    xmlElem.download = "xml_" + xml[pos].id + "_uid_" + user + "_eid_" + experiment + ".xml";
    let file = new Blob([xml[pos].xml], {
        type: "text/xml"
    });
    xmlElem.href = window.URL.createObjectURL(file);
    xmlElem.click();
});

/**
 * Makes the json code at the current array position available for download in a file.
 */
jsonButton.addEventListener("click", function () {
    let json = document.createElement("a");
    json.download = "json_" + xml[pos].id + "_uid_" + user + "_eid_" + experiment + ".json";
    let file = new Blob([xml[pos].code], {
        type: "text/plain"
    });
    json.href = window.URL.createObjectURL(file);
    json.click();
});

/**
 * Sets the href attribute of the sb3 button in preparation for the sb3 file download and clicks the button.
 */
sb3Button.addEventListener("click", function () {
    sb3Button.href = contextPath + "/result/generate?user=" + user + "&experiment=" + experiment + "&json="
        + xml[pos].id;
    sb3Button.click();
});

/**
 * Sets the href attribute of the download button to download sb3 files for json codes in the selected range and clicks
 * the button.
 */
downloadRangeButton.addEventListener("click", function () {
    let include = rangeLastFile.checked;
    let start = sliderValues[0].innerHTML.substring(0, sliderValues[0].innerHTML.indexOf("."));
    let end = sliderValues[1].innerHTML.substring(0, sliderValues[1].innerHTML.indexOf("."));
    downloadRangeButton.href = contextPath + "/result/sb3s?user=" + user + "&experiment=" + experiment + "&start="
        + start + "&end=" + end + "&include=" + include;
    downloadRangeButton.click();
});

/**
 * Sets the code displayed in the blockly area to the first xml code and sets the button display values accordingly. If
 * the current page is not the first page, it is loaded from the result controller.
 */
first.addEventListener("click", function () {
    count = 0;
    pos = 0;

    if (page > 0) {
        page = 0;
        getXML();
    } else {
        setCode(pos);
        setInformation(pos);
    }

    first.style.display = "none";
    prev.style.display = "none";
    next.style.display = "";
    last.style.display = "";
});

/**
 * Sets the code displayed in the blockly area to the previous xml code in the array and sets the button display values
 * accordingly.
 */
prev.addEventListener("click", function () {
    if (count > 0) {
        if (count % pageSize === 0) {
            page--;
            count--;
            pos = pageSize - 1;
            getXML();
        } else {
            count--;
            pos--;
            setCode(pos);
            setInformation(pos);
        }

        if (count === 0) {
            first.style.display = "none";
            prev.style.display = "none";
        }

        next.style.display = "";
        last.style.display = "";
    }
});

/**
 * Sets the code displayed in the blockly area to the next xml code in the array and sets the button display values
 * accordingly.
 */
next.addEventListener("click", function () {
    if (count < total - 1) {
        count++;
        if (count % pageSize === 0) {
            page++;
            pos = 0;
            getXML();
        } else {
            pos++;
            setCode(pos);
            setInformation(pos);
        }

        if (count === total - 1) {
            last.style.display = "none";
            next.style.display = "none";
        }

        prev.style.display = "";
        first.style.display = "";
    }
});

/**
 * Sets the code displayed in the blockly area to the last xml code and sets the button display values accordingly. If
 * the current page is not the last page, it is loaded from the result controller.
 */
last.addEventListener("click", function () {
    count = total - 1;
    pos = total % pageSize === 0 ? pageSize - 1 : total % pageSize - 1;

    if ((page + 1) * pageSize < total) {
        let quotient = Math.floor(total/pageSize);
        page = total % pageSize === 0 ? quotient - 1 : quotient;
        getXML();
    } else {
        setCode(pos);
        setInformation(pos);
    }

    prev.style.display = "";
    first.style.display = "";
    last.style.display = "none";
    next.style.display = "none";
});

/**
 * Retrieves the given page of block event projections from the result controller for the given user and experiment.
 */
function getXML() {
    $.ajax({
        type: "GET",
        url: contextPath + "/result/codes",
        data: {
            experiment: experiment,
            user: user,
            page: page
        },
        success: function (data) {
            if (data) {
                xml = data;
                setCode(pos);
                setInformation(pos);
            }
        }
    });
}

/**
 * Updates the number of the current step displayed in the blockly area.
 */
function setInformation(position) {
    let currentDate = xml[position].date;
    let day = currentDate.substring(0, currentDate.indexOf('T'));
    let hours = currentDate.substring(currentDate.indexOf('T') + 1, currentDate.indexOf('.'))
    current.innerText = count + 1;
    time.innerText = day + " " + hours;
    sprite.innerText = xml[position].sprite;
}

/**
 * Creates the slider used to specify the interval of sb3 files to be downloaded. The maximum is set to amount of json
 * strings that could be found in the database.
 */
noUiSlider.create(slider, {
    start: [1, total],
    connect: true,
    step: 1,
    range: {
        'min': 1,
        'max': total
    }
});

/**
 * Updates the values displayed below the slider on user interaction.
 */
slider.noUiSlider.on('update', function (values, handle) {
    sliderValues[handle].innerHTML = values[handle];
});
