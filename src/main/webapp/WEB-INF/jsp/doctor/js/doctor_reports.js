
var currReportSid = "";
var currReportDateLong = "";

var pdfDialogOptions = {

    autoOpen:false,
    closeOnEscape: true,
    dialogClass: "no-close",
    maxWidth:1200,
    maxHeight: 850,
    width: 1200,
    height: 850,
    modal: true,
    buttons: {
        "Sign Report": function() {
            console.log("Signing ... ");
            $.ajax({
                    method: "GET",
                    url: "/ecg/reports/sign?sid="+currReportSid+"&gendate="+currReportDateLong+"&signedBy="+auth0UserId,
                    headers: {"authorization": "Bearer " + idToken}
                })
                .done( ( response ) => {
                    console.log(response);
                    if(response.exception) {
                        console.log("Exception: " + response.body);
                    }
                    else {
                        $(this).dialog("close");
                        setTimeout(()=>{
                            location.reload();
                        }, 1000);
                    }
                });

        },
        "Cancel": function() {
            $(this).dialog("close");
        }
    },
    close: function(event, ui) {
    },
    open: function(event, ui) {
    }
};

$( document ).ready( () => {
    console.log( "ready!" );

    <%--
    setTimeout(()=>{
        getApiAuthorizationToken();
    }, 50);
    --%>

    getReportList();

    $("#pdfDialog").dialog(pdfDialogOptions);

    $("#logout").click(function(e) {
        e.preventDefault();
        $("#home").removeClass("active");
        $("#password-login").removeClass("active");
        $("#logout").addClass("active");
        // assumes we are not part of SSO so just logout of local session
        window.location = "${fn:replace(pageContext.request.requestURL, pageContext.request.requestURI, '')}/logout";
    });
});


function getReportList() {
    $.ajax({
        method: "GET",
        url: "/ecg/reports/urgent/all",
        //data: { name: "John" }
        headers: {"authorization": "Bearer " + idToken}
    })
    .done( ( reportList ) => {
        console.log(reportList);
        showList(reportList.body);

    });
}


function showList(reportList) {
    $("#reportList").empty();
    var str = "<button class='btn-sm' onclick='location.reload()'>Refresh</button><br /><br />";
    str += "<table class='reportListTable' cellspacing='5'>";
    console.log(reportList);
    str += "<tr><th>Activation</th><th>Generated At</th><th>Report</th><th>SignedBy</th><th>SignedAt</th><th>View</th></tr>";

    reportList.forEach((report)=>{
        var signedByClass = "signedByClass_uid_" + report.signedBy.replace("|","-");
        str += "<tr><td>" + report.sessionId + "</td><td>" + new Date(report.dateCreatedLong) + "</td><td>" + report.reportType + "</td><td class='"+signedByClass+"'>" + getUserDetails(report.signedBy) + "</td><td>" + ((report.signedAt==-1)?"-":new Date(report.signedAt)) + "</td><td><a href=\"javascript:viewPdf('"+report.sessionId+"', '"+report.dateCreatedLong+"')\">View</a></td></tr>";
    });
    str += "</table>";
    $("#reportList").append(str);
}





var auth0UserInfoArr = [];
var madeRequestsArr = [];
function getUserDetails(auth0UserIdRow) {

    console.log("Auth0UserIdRow: " + auth0UserIdRow);
    if(auth0UserIdRow == "-") {
        return "-";
    }

    if(auth0UserInfoArr[auth0UserIdRow] != undefined) {
        console.log("Found Signed By User locally: " + auth0UserIdRow);
        setSignedBy(auth0UserInfoArr[auth0UserIdRow], auth0UserIdRow);
    }
    else {
        if(madeRequestsArr[auth0UserIdRow]) {
            console.log("Already made request for " + auth0UserIdRow);
            return "...";
        }

        console.log("DID not find Signed By User: " + auth0UserIdRow + " ... requesting");
        $.ajax({
            method: "GET",
            url: "/portal/user?auth0UserId=" + encodeURIComponent(auth0UserIdRow),
            beforeSend: () => {madeRequestsArr[auth0UserIdRow] = true;}
        })
        .done( ( userJsonStr ) => {
            var user = JSON.parse(userJsonStr.body);
            var full_name = "";
            console.log(user);
            auth0UserInfoArr[auth0UserIdRow] = user;
            setSignedBy(user, auth0UserIdRow);
        });
    }



    return "...";
}

function setSignedBy(user, auth0UserIdRow) {

    if(user.identities[0].isSocial) {
        full_name = user.given_name + " " + user.family_name;
    }
    else {
        full_name = user.user_metadata.fname + " " + user.user_metadata.lname;
    }

    var signedByClass = "signedByClass_uid_" + auth0UserIdRow.replace("|","-");
    console.log(signedByClass + " --> " + full_name);
    $('.' + signedByClass).text(full_name);
}

function viewPdf(sessionId, dateCreatedLong) {

    currReportSid = sessionId;
    currReportDateLong = dateCreatedLong;

    var waitUrl = "/images/pleasewait.gif";

    $("#pdfDialog").empty();
    $("#pdfDialog").dialog("open", pdfDialogOptions);
    $("#pdfDialog").append($("<iframe style='border-width:0px;width:100%;height:100%;' />").attr("src", waitUrl));

    $.ajax({
        method: "GET",
        url: "/ecg/reports/pdf?sessionId="+currReportSid+"&dateCreated="+currReportDateLong,
        headers: {"authorization": "Bearer " + idToken}
    })
    .done( ( response ) => {
        var pdfBase64 = response;
        $("#pdfDialog iframe").attr("src", pdfBase64);
    });




}