var VitalType = ()=>{};
    VitalType.NONE = -5000;
    VitalType.SPO2 = 1;
    VitalType.WEIGHT = 2;
    VitalType.BP = 4;
    VitalType.ECG = 5;
    VitalType.PKFLOW = 6;
    VitalType.SUGAR = 7;
    VitalType.TEMP = 8;
    VitalType.PHOTO = 500;

var VitalTypeShortStr = [];
    VitalTypeShortStr[VitalType.SPO2] = "SPO2";
    VitalTypeShortStr[VitalType.WEIGHT] = "WEIGHT";
    VitalTypeShortStr[VitalType.BP] = "BP";
    VitalTypeShortStr[VitalType.ECG] = "ECG";
    VitalTypeShortStr[VitalType.PKFLOW] = "PKFLOW";
    VitalTypeShortStr[VitalType.SUGAR] = "SUGAR";
    VitalTypeShortStr[VitalType.TEMP] = "TEMP";
    VitalTypeShortStr[VitalType.PHOTO] = "PHOTO";


function logout() {
    console.log("Logging out ... ");
    //location.href = "/logout";
}

function ajaxGetCall(url, headers, doneCallback, failedCallback, alwaysCallback) {

    var ajax = $.ajax({
        method: "GET",
        url: url,
        headers: headers
    })
    .done( ( response ) => {
        
        console.log("");
        console.log("url: " + url);
        console.log(response);
        console.log("");

        if(response.exception && response.body.includes("ACCESS DENIED")) {
            console.log("ACCESS DENIED");
            logout();
            return;    
        }
        if(typeof doneCallback == "function")
            doneCallback(response);
    });

    // optional args
    if(typeof failedCallback == "function") {
        ajax.failedCallback ( (response) => {
            console.log(response);
            if(typeof failedCallback == "function")
                failedCallback(response);
        });
    }
    
    // optional args
    if(typeof alwaysCallback == "function") {
        ajax.alwaysCallback( (response) => {
            console.log(response);
            if(typeof alwaysCallback == "function")
                alwaysCallback(response);
        });
    }

}


function ajaxPostCall(url, data, headers, doneCallback, failedCallback, alwaysCallback) {
    var ajax = $.ajax({
        method: "POST",
        url: url,
        data: data,
        headers: headers
    })
    .done( ( response ) => {
        
        console.log("");
        console.log("url: " + url);
        console.log(response);
        console.log("");

        if(response.exception && response.body.includes("ACCESS DENIED")) {
            console.log("ACCESS DENIED");
            logout();
            return;    
        }
        if(typeof doneCallback == "function")
            doneCallback(response);
    });
    
    // optional args
    if(typeof failedCallback == "function") {
        ajax.failedCallback ( (response) => {
            console.log(response);
            if(typeof failedCallback == "function")
                failedCallback(response);
        });
    }
    
    // optional args
    if(typeof alwaysCallback == "function") {
        ajax.alwaysCallback( (response) => {
            console.log(response);
            if(typeof alwaysCallback == "function")
                alwaysCallback(response);
        });
    }
}




function showStaticObjectVars(object) {
    console.log("VClinicState ... ")
    Object.keys(object).forEach((item, key)=>{console.log("\t" + item + ": " + (object[item]))});
}