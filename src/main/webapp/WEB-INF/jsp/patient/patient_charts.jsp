<%@ page session="true" %>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>mCare: Physician Home</title>

    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta http-equiv="x-ua-compatible" content="ie=edge">


    <link rel="shortcut icon" type="image/jpg" href="/images/Leaf.png"/>

    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <link rel="stylesheet" href="/css/mdb.min.css">
    <link rel="stylesheet" href="/css/jumbotron-narrow.css">
    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.0.6/css/all.css">

    <!--<link rel="stylesheet" href="/css/home.css">-->
    <link rel="stylesheet" href="https://code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">

    <script>if (typeof module === 'object') {window.module = module; module = undefined;}</script>


    <script type="text/javascript" src="/js/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>

    <script type="text/javascript" src="/js/popper.min.js"></script>
    <script type="text/javascript" src="/js/bootstrap.min.js"></script>

    <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.16/css/dataTables.bootstrap4.min.css"/>

    <script src="https://cdn.datatables.net/1.10.16/js/jquery.dataTables.min.js"></script>
    <script src="https://cdn.datatables.net/1.10.16/js/dataTables.bootstrap4.min.js"></script>

    <script src="https://code.highcharts.com/stock/highstock.js"></script>

    <script tyoe="text/javascript">
        <%@ include file="../shared.js" %>
        <%@ include file="../sha1.js" %>
    </script>

    <script type="text/javascript">
        Number.prototype.pad = function(size) {
            var s = String(this);
            while (s.length < (size || 2)) {s = "0" + s;}
            return s;
        }

        var patientId = '${patientId}';
        var token = '${token}';

        const auth0_headers = {"authorization": "Bearer " + token};
    </script>



    <script>


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
    function getAllVitals() {

        var url = "/vitals/last30days?patientId="+patientId+"&token="+token;

        ajaxGetCall(url, auth0_headers, (response)=>{

            var allVitalsArr = JSON.parse(response.body);

            showAllVitals(allVitalsArr);

            vitalUpdateLoopInterval = setInterval(()=>{
                getAllVitalsFromLatestTs();
            }, 10000);

        });
    }

    function getAllVitalsFromLatestTs() {

        var url = "/vitals/range?patientId="+patientId+"&token="+token+"&startTimestamp="+currLatestVitalTimestamp+"&endTimestamp=-1";

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

         $('#trendsModal').modal();

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

        $('#trendsModal').modal();

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

        $('#trendsModal').modal();

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

        $('#trendsModal').modal();

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



    // ECG STRIPS
    var ch1DataPoints = [];
    var ecgStripList = [];
    function getEcgStripList() {
        var url = "/vitals/ecgstrip/list?patientId="+patientId;
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

        $('#trendsModal').modal();

        var htmlStr = "";

        htmlStr += "<table width='100%' id='ecgStripListTable'><thead><tr><th>ECG Record Timestamp</th><th></th></thead><tbody></tr>";
        ecgStripList.forEach((item)=>{
            htmlStr += "<tr>";
            htmlStr += "<td width='80%'>" + new Date(item.timestampMs) + "</td>";
            htmlStr += "<td><a href='javascript: displayEcgStrip("+item.timestampMs+", \""+item.stripid+"\")' class='btn-outline-primary btn-sm'>View</a></td>";
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
                pageLength: 3
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

    function displayEcgStrip(startTimestampMs, stripid) {

        stripStartTimestampMs = startTimestampMs;

        console.log(stripid);

        var url = "/vitals/ecgstrip?stripid="+stripid+"&patientId="+patientId;
            ajaxGetCall(url, auth0_headers, (response)=>{

                var ecgStripPkg = JSON.parse(response.body);

                var htmlStr = "";
                htmlStr += "<a href='javascript: showEcgStripList()' class='btn-outline-primary btn-sm'>< ECG List</a>";
                htmlStr += "&nbsp;&nbsp;";
                //htmlStr += "<big><b>Recorded: " + new Date(startTimestampMs) + "</b></big>";
                htmlStr += "<span style='width: 60px; display:inline-block;'>&nbsp;</span>";
                htmlStr += "<a href='javascript: startOfStrip()' class='btn-outline-primary btn-sm'><< Start</a>";
                htmlStr += "&nbsp;&nbsp;";
                htmlStr += "&nbsp;&nbsp;";
                htmlStr += "<a href='javascript: backwardStrip()' class='btn-outline-primary btn-sm'>< 2.5s</a>";
                htmlStr += "&nbsp;";
                htmlStr += "<a href='javascript: forwardStrip()' class='btn-outline-primary btn-sm'>2.5s ></a>";

                htmlStr += "<br /><br />";
                htmlStr += "<div id='ecgStripDiv' style='width:100%;height:85%; border:0px solid black;'></div>";

                $("#trends_highcharts_div").html(htmlStr);

                currEcgStrip = ecgStripPkg.ecgSamples;

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

    function getEcgWindowSlice(slideNumber) {

        currWindowSlide = slideNumber;
        console.log("Plot: Curr Slide Number: " + slideNumber);
        console.log("Current ECG Strip: ", currEcgStrip);

        var startSampleNumber = slideNumber * windowSlidingSizeSamples;
        var endSampleNumber = startSampleNumber + windowSizeSamples;

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


        ecgCanvasContext.clearRect(0, 0, w, h);


        drawGrid(w, h, windowSizeMs);


        ecgCanvasContext.beginPath();
        ecgCanvasContext.lineWidth = 1;
        scaledEcgArr.forEach((y)=>{

            ecgCanvasContext.moveTo(prevX,prevY);
            ecgCanvasContext.lineTo(x,y);

            prevX = x;
            prevY = y;

            x += xSep;
        });
        ecgCanvasContext.strokeStyle = '#000';
        ecgCanvasContext.stroke();

        var dateStart = new Date(windowStartTimestampMs);
        var dateEnd = new Date(windowStartTimestampMs + windowSizeMs);

        drawDates(w, h, dateStart, dateEnd);
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



    function startHere() {
        getAllVitals();
        getEcgStripList();
    }


    </script>



    <style>
        body, td, div, h5, a, button {
            font-size:90%;
        }

        </style>
</head>
<body onload="startHere()">



    <!-- Modal -->
    <style>
        #trends_highcharts_div {
            width:100%;
            height:80%;
            text-align:center;
            padding: 10px;
        }
    </style>

    <div class="modal fade show" id="trendsModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-full-height modal-info" role="document">
            <!--Content-->
            <div class="modal-content">
                <!--Body-->
                <div class="modal-body" style="overflow-y:auto;">
                    <button type="button" class="btn btn-danger btn-sm" data-dismiss="modal">Back</button>
                    <div class="text-center" id="trends_highcharts_div">

                    </div>
                </div>

            </div>
            <!--/.Content-->
        </div>
    </div>

    <center>
    <h4>Patient Vitals</h4>

    <table style="border: 0px solid #f88;width:98%;">

        <tr>

            <td width="21%">
                <!-- Sugar -->
                <div class="card vitalcard sugarcard">
                    <div class="card-body" style="text-align:center">
                        <big>
                            <b>Bl. Sugar</b>
                            <br />
                            <br />
                            <span id="sugar_latest_value">---</span> mg/dl
                            <br />
                            <br />


                        </big>
                        <div id="sugar_latest_timestamp"></div>
                        <br />
                        <a href="javascript: showLast30DaysSugarGraph()" class="btn-outline-primary btn-sm">Trends</a>
                    </div>
                </div>
            </td>

            <td width="21%">
                <!-- SPO2/HR -->
                <div class="card vitalcard spo2card">
                    <div class="card-body" style="text-align:center">

                        <big>
                            <b>Blood O<sub>2</sub></b>
                            <br /><br />
                            <span id="spo2_latest_value">---</span> %
                            <br />
                            <span id="hr_latest_value">---</span> bpm
                            <br />
                        </big>

                        <div id="spo2_latest_timestamp"></div>
                        <br />
                        <a href="javascript: showLast30DaysSpo2Graph()" class="btn-outline-primary btn-sm">Trends</a>
                    </div>
                </div>
            </td>


            <td width="21%">
                <!-- Bl. Press. -->
                <div class="card vitalcard bpcard">
                    <div class="card-body" style="text-align:center">
                        <big>
                            <b>Bld. Prss.</b>
                            <br />
                            <br />
                            <span id="bp_latest_value">---</span> mmHg
                            <br />
                        </big>
                        <div id="bp_latest_timestamp"></div>
                        <br />
                        <a href="javascript: showLast30DaysBpGraph()" class="btn-outline-primary btn-sm">Trends</a>
                    </div>
                </div>
            </td>


            <td width="21%">
                <!-- Body Weight -->
                <div class="card vitalcard weightcard">
                    <div class="card-body" style="text-align:center">
                        <big>
                            <b>Weight</b>
                            <br /><br />
                            <span id="wt_latest_value">---</span> lbs
                            <br />
                            (<span id="wt_kgs_latest_value">---</span> kg )
                            <br />
                        </big>
                        <div id="wt_latest_timestamp"></div>
                        <br />
                        <a href="javascript: showLast30DaysWtGraph()" class="btn-outline-primary btn-sm">Trends</a>
                    </div>
                </div>
            </td>

            <td width="16%">
                <!-- ECG -->
                <div class="card vitalcard ecgcard">
                    <div class="card-body" style="text-align:center">
                        <big>
                            <b>ECG Strips</b>
                            <br /><br />
                            <i class="fas fa-heartbeat"></i>
                        </big>

                        <br /><br /><br /><br />

                        <a href="javascript: showEcgStripList()" class="btn-outline-primary btn-sm">List</a>

                    </div>
                </div>
            </td>

        </tr>
    </table>
    </center>

</body>