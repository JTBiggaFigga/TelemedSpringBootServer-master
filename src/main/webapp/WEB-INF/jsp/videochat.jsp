<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
  <head>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
    <link rel="stylesheet" href="https://code.getmdl.io/1.3.0/material.indigo-red.min.css" />
    <script defer src="https://code.getmdl.io/1.3.0/material.min.js"></script>
    <style>

      body {
        background-color: #f8f8f8;
      }


      #myVideo {
          position: absolute;
          top: 0px;
          left: 0px;

          width: 200px;
          height: 150px;

          z-index: 100;

          border: 0px solid black;
          border-radius: 10px;
          box-shadow: 0px 0px 20px #88888888;
      }

      #myVideo video {
        z-index:101;
      }

      #peerVideos {
          position: absolute;
          top: -150px;
          left: 190px;

          width: 600px;
          height: 400px;

          z-index: 10;

          text-align:center;
          vertical-align:middle;

          border: 0px solid black;
          border-radius: 10px;
          box-shadow: 0px 0px 20px #88888888;
      }

      #peerVideos video {
        z-index:11;
      }



    </style>

    <script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
    <script src="https://static.opentok.com/v2.9/js/opentok.min.js"></script>

    <script>


      function getUrlParams(name) {
          name = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
          var regex = new RegExp('[\\?&]' + name + '=([^&#]*)');
          var results = regex.exec(location.search);
          return results === null ? '' : decodeURIComponent(results[1].replace(/\+/g, ' '));
      };

      function endCall() {
        console.log("ending call ... ");
        location.href="about:blank";
      }




      // (optional) add server code here
      //initializeSession();

      var session;
      var currCamera = 1;

      currCamera = parseInt(localStorage.cameraSelected);
      console.log("from localStorage ... currCamera: ", currCamera);
      if(typeof currCamera == "undefined" || currCamera == "undefined" || isNaN(currCamera))
        currCamera = 0;


      var apiKey = parseInt(decodeURI(unescape("${apiKey}")));


      var myVideoWidth = 200;
      var myVideoHeight = 150;

      var peerVideoWidth = 400;
      var peerVideoHeight = 300;


      var mainWidth = "${w}";
      var mainHeight = "${h}";

      if(typeof mainWidth == "undefined" || mainWidth == "") {
        mainWidth = $(window).width() * 0.9;
        mainHeight = $(window).height() * 0.9;
      }

      if(typeof mainHeight == "undefined" || mainHeight == "") {
        mainWidth = $(window).width() * 0.9;
        mainHeight = $(window).height() * 0.9;
      }

      var leftPadding = 0; //parseInt(mainWidth/30);

      mainWidth = parseInt(mainWidth);
      mainHeight = parseInt(mainHeight);

      if(mainHeight > mainWidth) {
        var temp = mainHeight;
        mainHeight = mainWidth;
        mainWidth = temp;
      }

      console.log("MainView: " + mainWidth + "x" + mainHeight);

      var mainTop = 50; //parseInt(mainHeight / 10);

      myVideoHeight = parseInt(mainHeight / 3);
      //myVideoWidth = parseInt(mainWidth / 3);
      myVideoWidth = parseInt(myVideoHeight * 16 / 9);

      peerVideoHeight = parseInt(2 * mainHeight / 3);
      //peerVideoWidth = parseInt(2 * mainWidth / 3);
      peerVideoWidth = parseInt(peerVideoHeight * 16 / 9);

      if(peerVideoWidth + myVideoWidth > mainWidth) {
        myVideoWidth = mainWidth / 3;
        peerVideoWidth = mainWidth * 2 / 3;
      }

      var myVideoTop = mainTop;
      var myVideoLeft = parseInt(leftPadding);

      var peerVideoTop = mainTop  - (mainWidth / 100);
      var peerVideoLeft = parseInt(leftPadding) + myVideoWidth - (mainWidth / 100);

      var sessionId = decodeURI("${openTokSessionId}");
      var thisToken = decodeURI("${openTokToken}");

      function initVideoContainers() {

        var mv = $("#myVideo").get()[0];
        mv.style.width = myVideoWidth + "px";
        mv.style.height = myVideoHeight + "px";
        mv.style.position = "absolute";
        mv.style.top = myVideoTop + "px";
        mv.style.left = myVideoLeft + "px";


        var pv = $("#peerVideos").get()[0];
        pv.style.width = peerVideoWidth + "px";
        pv.style.height = peerVideoHeight + "px";
        pv.style.position = "absolute";
        pv.style.top = peerVideoTop + "px";
        pv.style.left = peerVideoLeft + "px";

        console.log("mv: ", mv);
        console.log("pv: ", pv);


        $("<style type='text/css'> #myVideo video{ position: absolute; top: 0; left: 0; border-radius:10px;} </style>").appendTo("head");
        $("<style type='text/css'> #peerVideos video{ position: absolute; top: 0; left: 0; border-radius:10px;}</style>").appendTo("head");


      }

      var audioInputDevices;
      var videoInputDevices;

      var publisher;
      var sessionConnectedBool = false;

      var publisherEvents = {
        accessDialogOpened: function (event) {
         console.log('Show allow camera message');
         console.log(event);
         },
       accessDialogClosed: function (event) {
         console.log('Hide allow camera message');
         console.log(event);
         },
       accessDenied: function(event) {
           console.log('Camera access denied');
           handleError("Access Denied");
           console.log(event);
         },
       accessAllowed: function(event) {
           console.log('Camera access allowed');
           console.log(event);
         }
      };

      function initializeSession() {

        initVideoContainers();

        connectSession();
      }



      function connectSession() {


          createPublisher();

          session = OT.initSession(apiKey, sessionId);

          // Subscribe to a newly created stream
          session.on({

            streamCreated: function(event) {
                            console.log("Peer Stream Created: ", event.stream);
                            $("#peerVideos").empty();
                            session.subscribe(event.stream, 'peerVideos', {
                                insertMode: 'append',
                                width: peerVideoWidth,
                                height: peerVideoHeight
                              }, handleError);
                          }

          });

          // Connect to the session
          session.connect(decodeURI(unescape(thisToken)), function(error) {
            // If the connection is successful, initialize a publisher and publish to the session

            if (error) {
              handleError(error);
            }

            else {

              console.log("Session Connected");

              setTimeout(()=>{
                publish();
              }, 5000);

            }
          });


      }


      function createPublisher() {

        console.log("Initing Publisher ... ");

        OT.getDevices(function(error, devices) {

          console.log("Error: ", error);
          console.log("Devices: ", devices);

          if(devices == undefined) {
            console.log("Returning");
            return;
          }

          audioInputDevices = devices.filter(function(element) {
            return element.kind == "audioInput";
          });
          videoInputDevices = devices.filter(function(element) {
            return element.kind == "videoInput";
          });


          console.log("Video Devs Available: ", videoInputDevices);

          // Create a publisher
          console.log("CurrCamera: " + currCamera);

          if(currCamera > (videoInputDevices.length - 1)) {
            currCamera = 0;
          }

          console.log("Getting Camera: " + videoInputDevices[currCamera].deviceId);

          $("#myVideo").empty();

          publisher = OT.initPublisher('myVideo', {
                insertMode: 'append',
                width: myVideoWidth,
                height: myVideoHeight,
                resolution: '1280x720',
                videoSource: videoInputDevices[currCamera].deviceId + ""
              }, handleError2);

          publisher.on(publisherEvents);

          console.log("Publisher: ", publisher);

          setTimeout(()=>{
            document.getElementById("myVideo").appendChild(publisher.element);
            publisher.element.style.width = $("myVideo").width() + "px";
            publisher.element.style.height = $("myVideo").height() + "px";
          }, 3000);

        });
      }

      function publish() {
        session.publish(window.publisher, handleError);
      }


      function switchCamera() {
        console.log(videoInputDevices);

        var newCamera = (videoInputDevices.length > (currCamera + 1) )?(currCamera + 1):0;
        if(newCamera == currCamera) {
          return;
        }
        currCamera = newCamera;
        console.log("new camera: " + currCamera);

        localStorage.cameraSelected =  currCamera;
        console.log("Stored camNum: " + localStorage.cameraSelected);

        session.unpublish(publisher);
        setTimeout(()=>{

          $("#myVideo").empty();

          publisher = OT.initPublisher('myVideo', {
              insertMode: 'append',
              width: myVideoWidth,
              height: myVideoHeight,
              resolution: '1280x720',
              videoSource: videoInputDevices[currCamera].deviceId + ""
            }, handleError);
          publisher.on(publisherEvents);
          publish();
        }, 1000);


      }


      // replace these values with those generated in your TokBox Account
      // var apiKey = "YOUR_API_KEY";
      // var sessionId = "YOUR_SESSION_ID";
      // var token = "YOUR_TOKEN";

      // Handling all of our errors here by alerting them
      function handleError(error) {
        if (error) {
          //alert(error.message);
          console.error(error.message);

        }
      }

      // Handling all of our errors here by alerting them
      function handleError2(error) {
        if (error) {
          //alert(error.message);
          console.error("Error2: ", error.message);

        }
      }

    </script>

  </head>
  <body onload="initializeSession()">

    <button id="switchCameraBtn" class="mdl-button mdl-js-button mdl-button--primary" type="button" style="z-index:500;" onclick="switchCamera()">
      <i class="material-icons">switch_camera</i> <small>Switch My Camera</small>
    </button>

    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

    <button id="refreshBtn" class="mdl-button mdl-js-button mdl-button--accent" type="button" style="z-index:500;" onclick="location.href=location.href">
      <i class="material-icons">replay</i> <small>Reconnect</small>
    </button>


    <br />

    <div id="myVideo"></div>
    <div id="peerVideos"></div>



  </body>
</html>