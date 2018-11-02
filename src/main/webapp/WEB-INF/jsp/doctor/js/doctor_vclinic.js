const DOC_VCLINIC_STATE_OPEN = "OPEN";
const DOC_VCLINIC_STATE_CLOSED = "CLOSED";

const doc_id = auth0UserId;

const BEACON_INTERVAL = 30000;


const BEATCLASS_N = 1; // normal
const BEATCLASS_S = 2; // supra-ventricular
const BEATCLASS_V = 3; // ventricular
const BEATCLASS_O = 4; // other: eg: AFib
const BEATCLASS_U = 5; // unknown

var BEATCLASS_STR = {};
BEATCLASS_STR[BEATCLASS_N] = "N";
BEATCLASS_STR[BEATCLASS_S] = "S";
BEATCLASS_STR[BEATCLASS_V] = "V";
BEATCLASS_STR[BEATCLASS_O] = "O";
BEATCLASS_STR[BEATCLASS_U] = "U";


const RHYTHM_VDot = 1;       // V
const RHYTHM_V2g = 2;        // bigeminy
const RHYTHM_V3g = 3;        // trigeminy
const RHYTHM_VRun = 4;       // trigeminy
const RHYTHM_VCpl = 10;       // V Couplet
const RHYTHM_VTrp = 11;       // V Triplet

const RHYTHM_NDot = 5;       // N
const RHYTHM_NP = 6;         // PAUSE
const RHYTHM_NBr = 7;        // Brady
const RHYTHM_NTa = 8;        // Tachy

const RHYTHM_SDot = 9;       // S
const RHYTHM_STac = 12;      // S Tac
const RHYTHM_SCpl = 16;       // S Couplet
const RHYTHM_STrp = 17;       // S Triplet

const RHYTHM_AF = 13;        // afib
const RHYTHM_U = 14;        // UNKNOWN
const RHYTHM_DOT = 15;        // UNKNOWN

var RHYTHMCLASS_STR = {};
RHYTHMCLASS_STR[RHYTHM_VDot] = "V";       // V
RHYTHMCLASS_STR[RHYTHM_V2g] = "B";        // bigeminy
RHYTHMCLASS_STR[RHYTHM_V3g] = "T";        // trigeminy
RHYTHMCLASS_STR[RHYTHM_VRun] = "VRun";       // v Run
RHYTHMCLASS_STR[RHYTHM_VCpl] = "VCpl";       // V Couplet
RHYTHMCLASS_STR[RHYTHM_VTrp] = "VTrp";       // V Triplet

RHYTHMCLASS_STR[RHYTHM_NDot] = "N";       // N
RHYTHMCLASS_STR[RHYTHM_NP] = "P";         // PAUSE
RHYTHMCLASS_STR[RHYTHM_NBr] = "SBR";        // Brady
RHYTHMCLASS_STR[RHYTHM_NTa] = "VT";        // Tachy

RHYTHMCLASS_STR[RHYTHM_SDot] = "S";       // S
RHYTHMCLASS_STR[RHYTHM_STac] = "SVTA";      // S Tac
RHYTHMCLASS_STR[RHYTHM_SCpl] = "SCpl";       // S Couplet
RHYTHMCLASS_STR[RHYTHM_STrp] = "STrp";       // S Triplet

RHYTHMCLASS_STR[RHYTHM_AF] = "AFIB";        // afib
RHYTHMCLASS_STR[RHYTHM_U] = "U";        // UNKNOWN
RHYTHMCLASS_STR[RHYTHM_DOT] = ".";        // UNKNOWN



var userDetailsArr = [];


// VClinicState Class
var VClinicState = ()=>{};
VClinicState.init = () => {
    VClinicState.vClinicOpenedAt = -1;
    VClinicState.currentPatientId = "";
    VClinicState.lastBeaconSentAt = -1;
    VClinicState.vClinicState = DOC_VCLINIC_STATE_CLOSED;
    VClinicState.patientQueue = [];
    VClinicState.patientProfileArr = [];
    VClinicState.currOrFinishedQueue = [];
    VClinicState.clinicBeaconInterval = -1;
    VClinicState.patientListFetchInterval = -1;
};

VClinicState.init();

/*var VitalType = ()=>{};
    VitalType.NONE = -5000;
    VitalType.SPO2 = 1;
    VitalType.WEIGHT = 2;
    VitalType.BP = 4;
    VitalType.ECG = 5;
    VitalType.PKFLOW = 6;
    VitalType.SUGAR = 7;
    VitalType.TEMP = 8;*/


var EVisitState = () =>{ };
EVisitState.init = () => {
    EVisitState.IN_AN_EVISIT = false;
    EVisitState.currPatientInfo = null;
};




/**
 * Update the UI to respond to Clinic State Change
 */
function setClinicStateUi() {
    if(VClinicState.vClinicState == DOC_VCLINIC_STATE_OPEN) {
        $("#vclinic-btn-open").addClass("active");
        $("#vclinic-btn-closed").removeClass("active");
    }
    else {
        $("#vclinic-btn-open").removeClass("active");
        $("#vclinic-btn-closed").addClass("active");
    }
}


/**
 * Get Clinic State from server and call UI Update
 */
function getAndShowCurrentVClinicState() {

    getVClinicStateAjax((response) => {
        if(response.body.includes(DOC_VCLINIC_STATE_OPEN)) {
            VClinicState.vClinicState = DOC_VCLINIC_STATE_OPEN;

            var vClinicOpenedAt = parseInt(response.body.split("^^")[1]);
            console.log("VClinic Opened at: " + vClinicOpenedAt);
            VClinicState.vClinicOpenedAt = vClinicOpenedAt;

            openClinicSuccessCallback();
        }
        else
            VClinicState.vClinicState = DOC_VCLINIC_STATE_CLOSED;

        setClinicStateUi();
    });

}


/**
 * Open the clinic
 */
function openClinic() {

    if(VClinicState.vClinicState == DOC_VCLINIC_STATE_OPEN) {
        return;
    }

    $("#clinicStateModalBody").html('Opening ... <i class="fa fa-sign-in" aria-hidden="true"></i>');
    showClinicStateUpdateModal();

    console.log("Opening VClinic At Server");
    openClinicAjax((response)=>{
        if(response.body.includes("SUCCESS")) {
            console.log("Hide Modal");
            var vClinicOpenedAt = parseInt(response.body.split("^^")[1]);
            console.log("VClinic Opened at: " + vClinicOpenedAt);
            VClinicState.vClinicOpenedAt = vClinicOpenedAt;
            openClinicSuccessCallback();
        }
    });

}



/**
 * Callback for Opening Clinic from Server
 */
function openClinicSuccessCallback() {
    hideClinicStateUpdateModal();
    VClinicState.vClinicState = DOC_VCLINIC_STATE_OPEN;
    setClinicStateUi();

    startClinicBeacon();

    startPatientQueueCycle();
    getPatientQueueAjax((response) => {
                if(!response.exception) {
                    console.log(response.body);
                    updatePatientQueue(JSON.parse(response.body));
                }
                else {
                    console.log("Error ! " + response.body);
                }
            });

    showPatientQueueTemplate();
}


/**
 * Start the beacon for updating Clinic State on Server
 * Beacon timestamp on server must always <= clinic closed timestamp
 */
function startClinicBeacon() {
    console.log("Starting Clinic Beacon ... ");
    if(VClinicState.clinicBeaconInterval == -1) {
        console.log("Setting Beacon Interval Function .. ");
        VClinicState.clinicBeaconInterval = setInterval(()=>{
            console.log("Sending Beacon ... ");
            sendClinicBeaconAjax((response) => {
                if(response.body.includes("SUCCESS")) {
                    var beaconTimestampOnServer = parseInt(response.body.split("^^")[1]);
                    console.log("VClinic Beacon at: " + beaconTimestampOnServer + " on server");
                }
            });
        }, BEACON_INTERVAL);
    }
    else {
        console.log("Beacon Interval Already Running ... ");
    }
}


function startPatientQueueCycle() {

    console.log("Starting Patient Queue Cycle ... ");

    if(VClinicState.patientListFetchInterval == -1) {
        console.log("Setting Patient Queue Interval Function .. ");
        VClinicState.patientListFetchInterval = setInterval(()=>{
            console.log("Getting Patient Queue ... ");
            getPatientQueueAjax((response) => {
                if(!response.exception) {
                    console.log(response.body);
                    updatePatientQueue(JSON.parse(response.body));
                }
                else {
                    console.log("Error ! " + response.body);
                }
            });
        }, BEACON_INTERVAL/3);
    }
    else {
        console.log("Patient Queue Interval Already Running ... ");
    }

}


/**
 * Close the Clinic
 */
function closeClinic() {

    if(VClinicState.vClinicState == DOC_VCLINIC_STATE_CLOSED) {
        return;
    }

    $("#clinicStateModalBody").html('Closing ... <i class="fa fa-sign-out" aria-hidden="true"></i>');
    showClinicStateUpdateModal();

    console.log("Closing VClinic At Server");
    closeClinicAjax((response)=>{
        if(response.body.includes("SUCCESS")) {
            closeClinicSuccessCallback();
        }
    });

}

/**
 * Callback for Closing Clinic from Server
 */
function closeClinicSuccessCallback() {
    hideClinicStateUpdateModal();
    endClinicBeacon();
    endPatientQueueCycle();
    
    VClinicState.init();
    setClinicStateUi();
    hidePatientQueueTemplate();
}




function endClinicBeacon() {
    console.log("Ending Clinic Beacon");
    clearInterval(VClinicState.clinicBeaconInterval);
    VClinicState.clinicBeaconInterval = -1;
}


function endPatientQueueCycle() {
    console.log("Ending Patient Queue Cycle");
    clearInterval(VClinicState.patientListFetchInterval);
    VClinicState.patientListFetchInterval = -1;
}



/**
 * Show Modal showing Clinic State updates
 */
function showClinicStateUpdateModal() {
    $('#clinicStateModal').modal('show');
}

/**
 * Show Modal showing Clinic State updates
 */
function hideClinicStateUpdateModal() {
    setTimeout(()=>{
        $('#clinicStateModal').modal('hide');
    }, 1000);

}



function showPatientQueueTemplate() {
    console.log("Showing patient queuee template");
    var patientQueueTemplateNode = document.importNode(document.querySelector('#patientQueueTemplate').content, true);
    document.querySelector("#mw-main-content").appendChild(patientQueueTemplateNode);
}

function hidePatientQueueTemplate() {
    var par = document.querySelector("#mw-main-content"); 
    while (par.firstChild) {
        par.removeChild(par.firstChild);
    }
}


function updatePatientQueue(patientQueue) {
    VClinicState.patientQueue = [];
    if(patientQueue.length <= 0) {
        document.querySelector("#patientQueueTable tbody").innerHTML = "<tr><td colspan='5'><i>No Patients Waiting .. </i></td></tr>";
    }
    else {
        patientQueue.forEach(item => {
            var patId = item.pat_id;
            VClinicState.patientQueue.push(item);
        });

        if(VClinicState.patientQueue[0].evisit_state == "IN_SESSION") {
            seeNextPatient();
            return;
        }

        document.querySelector("#patientQueueTable tbody").innerHTML = "";
        VClinicState.patientQueue.forEach((item)=>{
            pidsha1 = sha1(item.pat_id);
            document.querySelector("#patientQueueTable tbody").innerHTML += "<tr><td id='img_"+pidsha1+"'></td><td style='text-align:left' id='fname_"+pidsha1+"'></td><td></td></tr>";
        });

        VClinicState.patientQueue.forEach((item)=>{
            getUserProfileForTable(item.pat_id);
        });
    }
}

function getUserProfileForTable(patId) {
    getAuth0UserProfileAjax(patId, (response) => {
        
        var patientProfileJson = JSON.parse(response.body);
        var pidsha1 = sha1(patId);
        VClinicState.patientProfileArr[patId] = patientProfileJson;

        $("#fname_"+pidsha1).html("<big><big>" + patientProfileJson.name + "</big></big>");
        $("#img_"+pidsha1).html("<img src='"+patientProfileJson.picture+"' width='50' />");
    });
}


function seeNextPatient() {
    var nextPatientInfo = VClinicState.patientQueue[0];
    if(!nextPatientInfo) {
        return;
    }
    console.log("Next Patient: " + nextPatientInfo.pat_id + ", evisitId: " + nextPatientInfo.evisit_id);
    inviteNextPatientInAjax(nextPatientInfo, (response)=>{
        EVisitState.IN_AN_EVISIT = true;
        // "SUCCESS"+"^^"+openTokSessionId+"^^"+docToken + "^^" + TokboxProxyGateway.API_KEY
        var responseItems = response.body.split("^^");
        var openTokSessionId = responseItems[1];
        var myOpenTokToken = responseItems[2];
        var openTokKey = responseItems[3];

        hidePatientQueueTemplate();
        openEVisit(nextPatientInfo, openTokSessionId, myOpenTokToken, openTokKey);
    });
}

function openEVisit(patientInfo, openTokSessionId, myOpenTokToken, openTokKey) {
    EVisitState.currPatientInfo = patientInfo;

    showEVisitTemplate();

    setTimeout(()=>{

        var w = parseInt($("#opentok_iframe").width());
        var h = parseInt($("#opentok_iframe").height());
        console.log("w: " + w + ", h = " + h);
        //var opentoksrc = "https://tokbx.glitch.me/video.html?sessionId="+openTokSessionId+"&apiKey="+openTokKey+"&token="+escape(myOpenTokToken);
        //var opentoksrc = "https://tokbx.glitch.me/pat.html?openTokSessionId="+escape(openTokSessionId)+"&apiKey="+escape(openTokKey)+"&openTokToken="+escape(myOpenTokToken)+"&w="+w+"&h="+h;
        var opentoksrc = "/vclinic/videochat?openTokSessionId="+escape(openTokSessionId)+"&apiKey="+escape(openTokKey)+"&openTokToken="+escape(myOpenTokToken)+"&w="+w+"&h="+h;

        showEVisitVideo(opentoksrc);
        showPatientRecords(patientInfo);

    }, 2000);


}

function showEVisitTemplate() {
    console.log("Showing patient queuee template");
    var evisitTemplateNode = document.importNode(document.querySelector('#eVisitTemplate').content, true);
    document.querySelector("#mw-main-content").appendChild(evisitTemplateNode);
}

function hideEVisitTemplate() {
    var par = document.querySelector("#mw-main-content"); 
    while (par.firstChild) {
        par.removeChild(par.firstChild);
    }
}


function showEVisitVideo(opentoksrc) {
    $("#opentok_iframe").attr("src", opentoksrc);
}

var vitalUpdateLoopInterval = -1;

function showPatientRecords(patientEvisitInfo) {

    console.log("");
    console.log("showPatientRecords() ... ");
    console.log(patientEvisitInfo.pat_id);

    var patId = patientEvisitInfo.pat_id;
    
    var patientProfile = VClinicState.patientProfileArr[patId];
    console.log(patientProfile);
    if(patientProfile == null) {
        getAuth0UserProfileAjax(patId, (response)=>{
            patientProfile = JSON.parse(response.body);
            showPatientRecordPage(patientProfile);
        });
    }
    else {
        showPatientRecordPage(patientProfile);
    }
    
}

function showPatientRecordPage(patientProfile) {

    setTimeout(()=>{

        showPatientProfile(patientProfile);
        getLocationDetails(patientProfile);

        getAllVitals(patientProfile, function(vitalsArr){
            if(drChronoVersion) {
                createPatientInDrChrono(patientProfile, jsonToken, userInfo, vitalsArr);
            }
        });


        getEcgStripList(patientProfile);
        getPhotoList(patientProfile);



    }, 100);

};


function updatePatientVitalsToDrChrono(drChronoPatientId, docJsonToken, docserInfo, vitalsArr) {

}


function showPatientProfile(patientProfile) {
    $("#evisit_pat_name").html(patientProfile.name);  
    $("#evisit_pat_img").attr("src", patientProfile.picture);  
}


// LOCATION

function getLocationDetails(patientProfile) {
    var userId = patientProfile.user_id;
    var url = "/location?userId=" + userId;
    ajaxGetCall(url, auth0_headers, (response)=>{
        var locationInfo = JSON.parse(response.body);

        if(response.body == "{}") {
            return;
        }

        console.log("Location Info: ", locationInfo);
        var gmapsApiKey = locationInfo.gmapsKey;
        var openWeatherMapKey = locationInfo.openWeatherMapKey
        var lat = locationInfo.lat;
        var lng = locationInfo.lng;

        showPatientMap(lat, lng, gmapsApiKey);
        showPatientAddress(lat, lng, gmapsApiKey);
        showWeatherDetails(lat, lng, openWeatherMapKey);
    });
}

function showPatientMap(lat, lng, gmapsApiKey) {
    var loccsv = lat+","+lng;
    $("#patient_map_img").attr("src", "https://maps.googleapis.com/maps/api/staticmap?center="+loccsv+"&zoom=10&size=50x50&maptype=roadmap&key=" + gmapsApiKey);
    $("#modal_patient_map_img").attr("src", "https://maps.googleapis.com/maps/api/staticmap?center="+loccsv+"&zoom=10&size=300x300&maptype=roadmap&key=" + gmapsApiKey);
    $("#patient_map_img").attr("style", "float:left;cursor:pointer");
    $("#patient_map_img").click(function(){
        showMapsDialog(lat, lng, gmapsApiKey);
    });
}

function showPatientAddress(lat, lng, gmapsApiKey) {
    var loccsv = lat + "," + lng;
    var url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+loccsv+"&key="+gmapsApiKey;
    var data = {key: gmapsApiKey};

    ajaxGetCall(url, {}, (response)=>{
        console.log(response.results[0].formatted_address);
        var modalLinkHtml = "<small>(<a href='javascript:showMapModal()'>show map</a>)</small>";
        var formattedAddress = "<small>" + response.results[2].formatted_address + "</small>";
        $("#patient_address_span").html(modalLinkHtml + "&nbsp;&nbsp;" + formattedAddress);
        $("#modal_patient_address_span").html(formattedAddress);
    });
}

function showMapModal() {
    $('#patientLocationModal').modal({});
}

function showMapsDialog(lat, lng, gmapsApiKey) {
    console.log("Showing Maps Dialog");
}

function showWeatherDetails(lat, lng, weatherApiKey) {

    var url = "https://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lng+"&appid=" + weatherApiKey;

    ajaxGetCall(url, {}, (response)=>{

        var desc = response.weather[0].description;
        var iconId = response.weather[0].icon;
        var temp = response.main.temp * (9/5) - 459.67;
        var humidity = response.main.humidity;
        var pressure = response.main.pressure;

        console.log(response.weather[0].description);

        var iconsrc = "https://openweathermap.org/img/w/"+iconId+".png";
        $("#patient_weather_img").attr("src", iconsrc);

        var weatherHtml = desc + ".  Temperature: " + temp.toFixed(1) + "&deg;F | ";
        weatherHtml += "Humidity: " + humidity + "% | ";
        weatherHtml += "Pressure: " + pressure + " hPa";

        $("#patient_weather_span").html(weatherHtml);
    });
}


// PATIENT PHOTOS
var photoList = [];

function getPhotoList(patientProfile) {
    var userId = patientProfile.user_id;
    var url = "/vitals/photos/list?patientId="+userId;
        ajaxGetCall(url, auth0_headers, (response)=>{

            console.log(response);
            photoList = JSON.parse(response.body);

            if(photoList.length == 0) {
                $(".ecgcard").css("background-color", "#ddd");
                $(".ecgcard").attr("title", "No Data Yet");
            }

        });
}

function showPhotoList() {
    var htmlStr = "<h5>Photo List</h5>";

    htmlStr += "<table width='70%' id='photoListTable'><thead><tr><th>Photo Timestamp</th><th></th></thead><tbody></tr>";
    photoList.forEach((item)=>{
        htmlStr += "<tr>";
        htmlStr += "<td>" + new Date(item.timestampMs) + "</td>";
        htmlStr += "<td><a href='javascript: displayPhoto("+item.timestampMs+", \""+item.photoid+"\")' class='btn-outline-primary btn-sm'>View Photo</a></td>";
        htmlStr += "</tr>";
    });
    /*if(ecgStripList.length == 0) {
        htmlStr += "<tr><td colspan='2'><i>No ECG Strips Yet.</i></td></tr>";
    }*/
    htmlStr += "</tbody></table>";
    $("#trends_highcharts_div").html(htmlStr);
    setTimeout(()=>{

        $.extend( $.fn.dataTable.defaults, {
            searching: false,
            ordering:  false,
            lengthChange: false,
            pageLength: 5
        } );

        $("#photoListTable").DataTable();

    }, 10);
}


function displayPhoto(timestampMs, photoid) {

    console.log(photoid);

    var url = "/vitals/photos?photoid="+photoid;
    ajaxGetCall(url, auth0_headers, (response)=>{
        console.log(response);
        $("#modal_patient_photo_img").attr("src", response.body);
        $("#modal_patient_photo_timestamp").html(new Date(timestampMs));

        $("#patientPhotoModal").modal({});
    });
}





// ECG STRIPS
var ch1DataPoints = [];
var ecgStripList = [];
function getEcgStripList(patientProfile) {
    var userId = patientProfile.user_id;
    var url = "/vitals/ecgstrip/list?patientId="+userId;
        ajaxGetCall(url, auth0_headers, (response)=>{

            console.log(response);
            ecgStripList = JSON.parse(response.body);

            if(ecgStripList.length == 0) {
                $(".ecgcard").css("background-color", "#ddd");
                $(".ecgcard").attr("title", "No Data Yet");
            }

        });
}

function showEcgStripList() {
    var htmlStr = "<h5>ECG Strip List</h5>";

    htmlStr += "<table width='70%' id='ecgStripListTable'><thead><tr><th>Record Timestamp</th><th></th></thead><tbody></tr>";
    ecgStripList.forEach((item)=>{
        htmlStr += "<tr>";
        htmlStr += "<td>" + new Date(item.timestampMs) + "</td>";
        htmlStr += "<td><a href='javascript: displayEcgStrip("+item.timestampMs+", \""+item.stripid+"\")' class='btn-outline-primary btn-sm'>View Strip</a></td>";
        htmlStr += "</tr>";
    });
    /*if(ecgStripList.length == 0) {
        htmlStr += "<tr><td colspan='2'><i>No ECG Strips Yet.</i></td></tr>";
    }*/
    htmlStr += "</tbody></table>";
    $("#trends_highcharts_div").html(htmlStr);
    setTimeout(()=>{

        $.extend( $.fn.dataTable.defaults, {
            searching: false,
            ordering:  false,
            lengthChange: false,
            pageLength: 5
        } );

        $("#ecgStripListTable").DataTable();

    }, 10);
}




var sampleRate = 250;
var windowSizeMs = 5000;
var windowSizeSamples = sampleRate * windowSizeMs / 1000;

var windowSlidingSizeMs = 2500;
var windowSlidingSizeSamples = sampleRate * windowSlidingSizeMs / 1000;


var currSlideNumber = 0;
var totalPossibleSlides = 0;

var stripStartTimestampMs = -1;
var ecgCanvasContext;
var currEcgStrip = [];
var currEcgStripResponseJson = "";
var currEcgBeatMapJson = {};



function displayEcgStrip(startTimestampMs, stripid) {

    stripStartTimestampMs = startTimestampMs;

    console.log(stripid);

    var url = "/vitals/ecgstrip?stripid="+stripid;

        ajaxGetCall(url, auth0_headers, (response)=>{

            var responseBody = JSON.parse(response.body);
            var ch1DataPoints = responseBody.ecgSamples;
            if(responseBody.analyticsJson)
                var analyticsJson = JSON.parse(responseBody.analyticsJson);

            var currBeatMapJson = {};


            console.log("analyticsJson:", analyticsJson);
            if(analyticsJson != undefined) {
                Object.keys(analyticsJson.beatMap).sort().forEach(function(key) {
                  currBeatMapJson[key] = analyticsJson.beatMap[key];
                });
            }


            currEcgStripResponseJson = responseBody;
            currEcgBeatMapJson = currBeatMapJson;

            var htmlStr = "";
            htmlStr += "<a href='javascript: showEcgStripList()' class='btn-outline-primary btn-sm'>< ECG List</a>";
            htmlStr += "&nbsp;&nbsp;";
            htmlStr += "<big><b>Recorded: " + new Date(startTimestampMs) + "</b></big>";

            if(analyticsJson != undefined) {
                if(analyticsJson.aFibBool) {
                    htmlStr += "&nbsp;&nbsp;";
                    htmlStr += "<span style='color:red'><u><b>AFib exists</b></u></span>";
                }
            }

            htmlStr += "<span style='width: 140px; display:inline-block;'>&nbsp;</span>";
            htmlStr += "<a href='javascript: startOfStrip()' class='btn-outline-primary btn-sm'><< Start</a>";
            htmlStr += "&nbsp;&nbsp;";
            htmlStr += "&nbsp;&nbsp;";
            htmlStr += "<a href='javascript: backwardStrip()' class='btn-outline-primary btn-sm'>< 2.5s</a>";
            htmlStr += "&nbsp;";
            htmlStr += "<a href='javascript: forwardStrip()' class='btn-outline-primary btn-sm'>2.5s ></a>";

            htmlStr += "<br /><br />";
            htmlStr += "<div id='ecgStripDiv' style='width:100%;height:85%; border:0px solid black;'></div>";

            $("#trends_highcharts_div").html(htmlStr);

            currEcgStrip = ch1DataPoints;

            totalPossibleSlides = parseInt(currEcgStrip.length / windowSlidingSizeSamples);

            ecgCanvasContext = createCanvas();

            currSlideNumber = 0;
            plotEcgWindow(currSlideNumber);

        });
}

function startOfStrip() {
    currSlideNumber = 0;
    plotEcgWindow(currSlideNumber);
}

function forwardStrip() {

    if(currSlideNumber >= totalPossibleSlides - 1) {
        console.log("Finished forwarding ... ");
        return;
    }

    currSlideNumber++;
    plotEcgWindow(currSlideNumber);
}

function backwardStrip() {
    if(currSlideNumber == 0) {
        console.log("Finished rewinding ... ");
        return;
    }

    currSlideNumber--;
    plotEcgWindow(currSlideNumber);
}


function createCanvas() {

    var w = $("#ecgStripDiv").width();
    var h = $("#ecgStripDiv").height();

    console.log("Plot: " + w + "x" + h);

    var xSep = w/currEcgStrip.length;
    console.log("Plot: xSep = " + xSep);

    var canvasElement = $("<canvas width='" + w + "' height='" + h + "'></canvas>");
    var canvasContext = canvasElement.get(0).getContext("2d");

    canvasElement.appendTo('body');
    canvasContext.rect(0, 0, w, h);
    canvasContext.fillStyle = "#fff";
    canvasContext.fill();

    $("#ecgStripDiv").empty();
    canvasElement.appendTo($("#ecgStripDiv"));

    return canvasContext;
}

function getEcgWindowStartTimestamp(slideNumber) {
    var startSampleNumber = slideNumber * windowSlidingSizeSamples;
    var msSinceFirstStripSample = (startSampleNumber * 1000 / sampleRate);

    var windowStartTimestampMs = stripStartTimestampMs + msSinceFirstStripSample;
    return windowStartTimestampMs;
}

var currSliceBeatMap = {};
function getEcgWindowSlice(slideNumber) {

    currWindowSlide = slideNumber;
    console.log("Plot: Curr Slide Number: " + slideNumber);

    var startSampleNumber = slideNumber * windowSlidingSizeSamples;
    var endSampleNumber = startSampleNumber + windowSizeSamples;
    console.log("Plot: Curr Slide Number: " + slideNumber);

    // get beatMap slice
    currSliceBeatMap = {};
    Object.keys(currEcgBeatMapJson).forEach((key)=>{
        if(key < endSampleNumber && key >= startSampleNumber) {
            currSliceBeatMap[key-startSampleNumber] = currEcgBeatMapJson[key];
        }
    });
    console.log("Curr Ecg Beat Map: ", currSliceBeatMap);

    var currEcgWindow = currEcgStrip.slice(startSampleNumber, endSampleNumber);
    console.log("Plot: ", currEcgWindow);

    return currEcgWindow;
}

function plotEcgWindow(slideNumber) {

    var ecgArr = getEcgWindowSlice(slideNumber);
    var windowStartTimestampMs = getEcgWindowStartTimestamp(slideNumber);


    console.log("From " + dateStart + " to " + dateEnd);

    var w = $("#ecgStripDiv").width();
    var h = $("#ecgStripDiv").height();

    console.log("Plot: " + w + "x" + h);

    var xSep = w/ecgArr.length;
    console.log("Plot: xSep = " + xSep);

    var max = Math.max.apply(null, ecgArr);
    var min = Math.min.apply(null, ecgArr);

    ecgArr = ecgArr.map(y => y + Math.abs(min));

    var scale = h / (max-min);

    var scaledEcgArr = ecgArr.map(y => h - (y*scale)); // inverted scale

    var prevX = 0;
    var prevY = scaledEcgArr[0];
    var x = 0;
    var sampleNumber = 0;


    ecgCanvasContext.clearRect(0, 0, w, h);


    drawGrid(w, h, windowSizeMs);


    ecgCanvasContext.beginPath();
    ecgCanvasContext.lineWidth = 1;

    scaledEcgArr.forEach((y) => {

        ecgCanvasContext.moveTo(prevX,prevY);
        ecgCanvasContext.lineTo(x,y);

        prevX = x;
        prevY = y;

        x += xSep;
        sampleNumber++;
    });
    ecgCanvasContext.strokeStyle = '#000';
    ecgCanvasContext.stroke();

    var dateStart = new Date(windowStartTimestampMs);
    var dateEnd = new Date(windowStartTimestampMs + windowSizeMs);

    drawDates(w, h, dateStart, dateEnd);

    plotBeats(slideNumber);
}


function plotBeats(slideNumber) {

    var ecgArr = getEcgWindowSlice(slideNumber);
    var windowStartTimestampMs = getEcgWindowStartTimestamp(slideNumber);


    var w = $("#ecgStripDiv").width();
    var h = $("#ecgStripDiv").height();

    var xSep = w/ecgArr.length;
    console.log("Plot: xSep = " + xSep);

    var max = Math.max.apply(null, ecgArr);
    var min = Math.min.apply(null, ecgArr);

    ecgArr = ecgArr.map(y => y + Math.abs(min));

    var scale = h / (max-min);

    var scaledEcgArr = ecgArr.map(y => h - (y*scale)); // inverted scale

    Object.keys(currSliceBeatMap).forEach((key)=>{



        var beatInfo = currSliceBeatMap[key];
        var x = xSep * key;
        var y = scaledEcgArr[key];

        console.log("plotting beat and sample: " + key + ", (at "+x+") ... " + JSON.stringify(currSliceBeatMap[key]));


        /*
        ecgCanvasContext.beginPath();
        ecgCanvasContext.arc(x, y, 5, 0, 2 * Math.PI, false);
        ecgCanvasContext.lineWidth = 1;
        ecgCanvasContext.strokeStyle = '#003300';
        ecgCanvasContext.stroke();
        */

        ecgCanvasContext.fillStyle = "#00f";
        ecgCanvasContext.font = "10pt Arial";
        var beatStr = BEATCLASS_STR[currSliceBeatMap[key].beatClass] + " / " + RHYTHMCLASS_STR[currSliceBeatMap[key].rhythmClass];
        ecgCanvasContext.fillText(beatStr, x - 40, y + 15);

    });
}


function drawDates(w, h, dateStart, dateEnd) {

    // rectangle
    ecgCanvasContext.fillStyle = "rgba(255, 230, 230, 0.5)";
    ecgCanvasContext.fillRect(0, 0, 275, 25);

    // date text
    ecgCanvasContext.fillStyle = "#235";
    ecgCanvasContext.font = "10pt Arial";
    ecgCanvasContext.fillText(dateStart, 5, 15);

    // rectangle
    ecgCanvasContext.fillStyle = "rgba(255, 230, 230, 0.5)";
    ecgCanvasContext.fillRect(w-290, 0, 290, 25);

    // date text
    ecgCanvasContext.fillStyle = "#235";
    ecgCanvasContext.font = "10pt Arial";
    ecgCanvasContext.fillText(dateEnd, w-275, 15);
}


// grid
function drawGrid(canvasWidth, canvasHeight, stripWidthMs) {

    var largeSquareWidth = (canvasWidth * 200) / stripWidthMs;
    var smallSquareWidth = (canvasWidth * 40) / stripWidthMs;

    ecgCanvasContext.lineWidth = 1;


    // large squares
    // vertical lines
    var i = 0;
    var currX = 0;
    while(currX < canvasWidth) {

        //console.log("Plot: Vertical Line at x = " + currX);

        ecgCanvasContext.beginPath();
        ecgCanvasContext.moveTo(currX, 0);
        ecgCanvasContext.lineTo(currX, canvasHeight);

        if(i % 5 == 0) // thick
        {
            ecgCanvasContext.strokeStyle = 'rgba(255,0,0,0.5)';
        }
        else // thin
        {
            ecgCanvasContext.strokeStyle = 'rgba(255,0,0,0.1)';
        }

        ecgCanvasContext.stroke();

        currX += smallSquareWidth;
        i++;
    }


    var j = 0;
    var currY = 0;
    while(currY < canvasHeight) {

        //console.log("Plot: Horizontal Line at y = " + currY);

        ecgCanvasContext.beginPath();
        ecgCanvasContext.moveTo(0, currY);
        ecgCanvasContext.lineTo(canvasWidth, currY);

        if(j % 5 == 0) // thick
        {
            ecgCanvasContext.strokeStyle = 'rgba(255,0,0,0.5)';
        }
        else // thin
        {
            ecgCanvasContext.strokeStyle = 'rgba(255,0,0,0.1)';
        }

        ecgCanvasContext.stroke();

        currY += smallSquareWidth;
        j++;
    }


}



// VITALS
var spo2Arr = [];
var weightArr = [];
var bpArr = [];
var sugarArr = [];


function dateStrForVitalCard(timestamp) {
    var dt = new Date(timestamp);
    var html = (dt.getMonth() + 1).pad() + "." + (dt.getDate()).pad() + "." + (dt.getFullYear()).pad();
    html += "<br />";
    html += (dt.getHours()).pad() + ":" + (dt.getMinutes()).pad() + ":" + (dt.getSeconds()).pad();
    return html;
}



var currLatestVitalTimestamp = -1;
function getAllVitals(patientProfile, callback) {

    var url = "/vitals/last30days?patientId="+patientProfile.user_id;

    ajaxGetCall(url, auth0_headers, (response)=>{

        var allVitalsArr = JSON.parse(response.body);

        showAllVitals(allVitalsArr);

        if(currLatestVitalTimestamp == -1) {
            currLatestVitalTimestamp = new Date().getTime();
        }

        vitalUpdateLoopInterval = setInterval(()=>{
            showAllVitalsFromLatestTs(patientProfile);
        }, 10000);

        callback(allVitalsArr);

    });
}

function showAllVitalsFromLatestTs(patientProfile) {

    var url = "/vitals/range?patientId="+patientProfile.user_id+"&startTimestamp="+currLatestVitalTimestamp+"&endTimestamp=-1";

    ajaxGetCall(url, auth0_headers, (response)=>{

        console.log(response);
        var allVitalsArr = JSON.parse(response.body);

        showAllVitals(allVitalsArr);
    });
}

function showAllVitals(allVitalsArr) {

    if(typeof allVitalsArr == "undefined") {
        return;
    }

    if(allVitalsArr.length == 0) {
        console.log("No new data!");
        return;
    }

    currLatestVitalTimestamp = allVitalsArr[0].timestampMs;
    if(currLatestVitalTimestamp == -1) {
        currLatestVitalTimestamp = new Date().getTime();
    }

    console.log("currLatestVitalTimestamp: " + currLatestVitalTimestamp + " (" + new Date(currLatestVitalTimestamp) + ")");

    spo2Arr = allVitalsArr.filter((item)=>{return item.type == (VitalType.SPO2 + "")}).concat(spo2Arr);
    showLatestSpo2();

    sugarArr = allVitalsArr.filter((item)=>{return item.type == (VitalType.SUGAR + "")}).concat(sugarArr);
    showLatestSugar();

    weightArr = allVitalsArr.filter((item)=>{return item.type == (VitalType.WEIGHT + "")}).concat(weightArr);
    showLatestWeight();

    bpArr = allVitalsArr.filter((item)=>{return item.type == (VitalType.BP + "")}).concat(bpArr);
    showLatestBp();

    if(currGraphShown > -1) {
        switch(currGraphShown) {
            case VitalType.SUGAR:
                showLast30DaysSugarGraph();
                break;
            case VitalType.SPO2:
                showLast30DaysSpo2Graph();
                break;
            case VitalType.BP:
                showLast30DaysBpGraph();
                break;
            case VitalType.WEIGHT:
                showLast30DaysWtGraph();
                break;
        }
    }
}


function showLatestSpo2() {

    if(spo2Arr.length == 0) {
        $("#hr_latest_value").html("---");
        $("#spo2_latest_value").html("---");
        $("#spo2_latest_timestamp").html("");

        $(".spo2card").css("background-color", "#ddd");
        $(".spo2card").attr("title", "No Data Yet");

        return;
    }

    var latestSpo2 = spo2Arr[0];
    $("#spo2_latest_value").html(latestSpo2.v1 + "");
    $("#hr_latest_value").html(latestSpo2.v2 + "");
    $("#spo2_latest_timestamp").html(dateStrForVitalCard(latestSpo2.timestampMs) + "");

    var nowMs = new Date().getTime();
    var THIRTY_MINUTES_MS = (1000 * 60 * 30);
    if( (nowMs - parseInt(latestSpo2.timestampMs)) > THIRTY_MINUTES_MS ) {
        $(".spo2card").css("background-color", "#eee");
        $(".spo2card").attr("title", "Data Older Than Atleast 30 minutes");
    }
    else {
        $(".spo2card").css("background-color", "#fff");
    }

}


function showLatestSugar() {

    if(sugarArr.length == 0) {
        $("#sugar_latest_value").html("---");
        $("#sugar_latest_timestamp").html("");

        $(".sugarcard").css("background-color", "#ddd");
        $(".sugarcard").attr("title", "No Data Yet");

        return;
    }

    var latestSugar = sugarArr[0];

    $("#sugar_latest_value").html(latestSugar.v1 + "");
    $("#sugar_latest_timestamp").html(dateStrForVitalCard(latestSugar.timestampMs) + "");

    var nowMs = new Date().getTime();
    var THIRTY_MINUTES_MS = (1000 * 60 * 30);
    if( (nowMs - parseInt(latestSugar.timestampMs)) > THIRTY_MINUTES_MS ) {
        $(".sugarcard").css("background-color", "#eee");
        $(".sugarcard").attr("title", "Data Older Than Atleast 30 minutes");
    }
    else {
        $(".sugarcard").css("background-color", "#fff");
    }

}

function showLatestBp() {

    if(bpArr.length == 0) {
        $("#bp_latest_value").html("---");
        $("#bp_latest_timestamp").html("");

        $(".bpcard").css("background-color", "#ddd");
        $(".bpcard").attr("title", "No Data Yet");

        return;
    }

    var latestBp = bpArr[0];

    $("#bp_latest_value").html(latestBp.v1 + "/" + latestBp.v2);
    $("#bp_latest_timestamp").html(dateStrForVitalCard(latestBp.timestampMs) + "");

    var nowMs = new Date().getTime();
    var THIRTY_MINUTES_MS = (1000 * 60 * 30);
    if( (nowMs - parseInt(latestBp.timestampMs)) > THIRTY_MINUTES_MS ) {
        $(".bpcard").css("background-color", "#eee");
        $(".bpcard").attr("title", "Data Older Than Atleast 30 minutes");
    }
    else {
        $(".bpcard").css("background-color", "#fff");
    }

}

function showLatestWeight() {

    if(weightArr.length == 0) {
        $("#wt_latest_value").html("---");
        $("#wt_kgs_latest_value").html("---");
        $("#wt_latest_timestamp").html("");

        $(".weightcard").css("background-color", "#ddd");
        $(".weightcard").attr("title", "No Data Yet");

        return;
    }

    var latestWt = weightArr[0];

    var wtLbs = parseFloat(latestWt.v1)
    var wtKgs = Number(wtLbs * 0.453592).toFixed(1);

    $("#wt_latest_value").html(wtLbs);
    $("#wt_kgs_latest_value").html(wtKgs);
    $("#wt_latest_timestamp").html(dateStrForVitalCard(latestWt.timestampMs) + "");

    var nowMs = new Date().getTime();
    var THIRTY_MINUTES_MS = (1000 * 60 * 30);
    if( (nowMs - parseInt(latestWt.timestampMs)) > THIRTY_MINUTES_MS ) {
        $(".weightcard").css("background-color", "#eee");
        $(".weightcard").attr("title", "Data Older Than Atleast 30 minutes");
    }
    else {
        $(".weightcard").css("background-color", "#fff");
    }
}



var rangeSelectorObj = {
          buttons: [{
                type: 'week',
                count: 1,
                text: '1w'
              }, {
                type: 'month',
                count: 1,
                text: '1m'
              }, {
                type: 'month',
                count: 3,
                text: '3m'
              }, {
               type: 'year',
               count: 1,
               text: '1y'
              }, {
                type: 'ytd',
                text: 'YTD'
              }
        ],
        selected: 0
      };

var currGraphShown = -1;
function showLast30DaysSugarGraph() {

    if(sugarArr.length == 0) {
        $("#trends_highcharts_div").html("<i>Sugar Trend: No data yet!</i>")
        return;
    }

    currGraphShown = VitalType.SUGAR;

    var dataArr = sugarArr.map(item=>[item.timestampMs, parseInt(item.v1)]);
    dataArr = dataArr.sort((a,b)=>{return a[0]-b[0]});

    var maxY = Math.max.apply(null, dataArr) + 10;

    Highcharts.stockChart('trends_highcharts_div', {


            rangeSelector: rangeSelectorObj,

            title: {
                text: 'Blood Sugar'
            },

            yAxis: {
                min: 0,
                max: maxY
            },

            series: [{
                name: 'Blood Sugar',
                data: dataArr,
                tooltip: {
                    valueDecimals: 1
                }
            }]
        });
}



function showLast30DaysSpo2Graph() {

    if(spo2Arr.length == 0) {
        $("#trends_highcharts_div").html("<i>SPO<sub>2</sub> Trend: No data yet!</i>")
        return;
    }

    currGraphShown = VitalType.SPO2;

    var o2Arr = spo2Arr.map(item=>[item.timestampMs, parseInt(item.v1)]);
    o2Arr = o2Arr.sort((a,b)=>{return a[0]-b[0]});

    var hrArr = spo2Arr.map(item=>[item.timestampMs, parseInt(item.v2)]);
    hrArr = hrArr.sort((a,b)=>{return a[0]-b[0]});


    var maxHr = Math.max.apply(null, hrArr.map(item=>item[1]));
    var maxO2 = Math.max.apply(null, o2Arr.map(item=>item[1]));

    var maxY = Math.max.apply(null, [maxHr, maxO2]) + 10;

    Highcharts.stockChart('trends_highcharts_div', {


            rangeSelector: rangeSelectorObj,

            title: {
                text: 'Oxygen Sat/Heart Rate'
            },

            yAxis: {
                min: 0,
                max: maxY
            },

            series: [{
                name: 'Heart Rate',
                data: hrArr,
                tooltip: {
                    valueDecimals: 0
                }
            },
            {
                name: 'Blood Oxygen',
                data: o2Arr,
                tooltip: {
                    valueDecimals: 0
                }
            }]
        });
}



function showLast30DaysBpGraph() {

    if(bpArr.length == 0) {
        $("#trends_highcharts_div").html("<i>Blood Pressure Trend: No data yet!</i>")
        return;
    }

    currGraphShown = VitalType.BP;

    var sysArr = bpArr.map(item=>[item.timestampMs, parseInt(item.v1)]);
    sysArr = sysArr.sort((a,b)=>{return a[0]-b[0]});

    var diaArr = bpArr.map(item=>[item.timestampMs, parseInt(item.v2)]);
    diaArr = diaArr.sort((a,b)=>{return a[0]-b[0]});

    var maxY = Math.max.apply(null, diaArr.map(item=>item[1])) + 10;

    Highcharts.stockChart('trends_highcharts_div', {


            rangeSelector: rangeSelectorObj,

            title: {
                text: 'Blood Pressure'
            },

            yAxis: {
                min: 0,
                max: maxY
            },

            series: [{
                name: 'Systolic BP',
                data: sysArr,
                tooltip: {
                    valueDecimals: 2
                }
            },
            {
                name: 'Diastolic BP',
                data: diaArr,
                tooltip: {
                    valueDecimals: 2
                }
            }]
        });
}


function showLast30DaysWtGraph() {

    if(weightArr.length == 0) {
        $("#trends_highcharts_div").html("<i>Body Weight Trend: No data yet!</i>")
        return;
    }

    currGraphShown = VitalType.WEIGHT;

    var wtArr = weightArr.map(item=>[item.timestampMs, parseInt(item.v1)]);
    wtArr = wtArr.sort((a,b)=>{return a[0]-b[0]});

    var maxY = Math.max.apply(null, wtArr.map(item=>item[1])) + 10;

    Highcharts.stockChart('trends_highcharts_div', {


            rangeSelector: rangeSelectorObj,

            title: {
                text: 'Body Weight (lbs)'
            },

            yAxis: {
                min: 0,
                max: maxY
            },

            series: [{
                name: 'Weight (lbs)',
                data: wtArr,
                tooltip: {
                    valueDecimals: 1
                }
            }]
        });
}



function endEVisit() {
    var lastEVisitId = EVisitState.currPatientInfo.evisit_id;

    
    endEVisitAjax(lastEVisitId, doc_id, (response) => {
        console.log(response);
        hideEVisitTemplate();
        showPatientQueueTemplate();
        EVisitState.init();
    });
}


/////////////// AJAX CALLS

function getAuth0UserProfileAjax(userId, callback) {
    var url = "/auth0/user?user_id="+userId;
    ajaxGetCall(url, auth0_headers, callback);
}

function getVClinicStateAjax(callback) {
    var url = "/vclinic/doctor/state?doc_id="+doc_id;
    ajaxGetCall(url, auth0_headers, callback);
}



function openClinicAjax(callback) {
    var url = "/vclinic/doctor/state";
    var data = { doc_id: doc_id, vclinic_state: DOC_VCLINIC_STATE_OPEN};
    ajaxPostCall(url, data, auth0_headers, callback);
}




function closeClinicAjax(callback) {
    var url = "/vclinic/doctor/state";
    var data = { doc_id: doc_id, vclinic_state: DOC_VCLINIC_STATE_CLOSED};
    ajaxPostCall(url, data, auth0_headers, callback);
}


function sendClinicBeaconAjax(callback) {
    var url = "/vclinic/doctor/beacon";
    var data = { doc_id: doc_id, vclinic_opened_at_ms: VClinicState.vClinicOpenedAt};
    VClinicState.lastBeaconSentAt = new Date().getTime();
    ajaxPostCall(url, data, auth0_headers, callback);
}


function getPatientQueueAjax(callback) {
    if(EVisitState.IN_AN_EVISIT) {
        console.log("Currently in an EVISIT. Not fetching patient list.");
        return;
    }

    var url = "/evisit/doctor/patient_queue?doc_id="+doc_id;
    ajaxGetCall(url, auth0_headers, callback);
}


function inviteNextPatientInAjax(patientInfo, callback) {
    var url = "/evisit/doctor/invite_next_patient";
    var data = {doc_id: auth0UserId, evisit_id: patientInfo.evisit_id};
    ajaxPostCall(url, data, auth0_headers, callback);
}

function endEVisitAjax(evisit_id, doc_id, callback) {
    var url = "/evisit/doctor/end";
    var data = {evisit_id: evisit_id, doc_id: doc_id};
    ajaxPostCall(url, data, auth0_headers, callback);
}