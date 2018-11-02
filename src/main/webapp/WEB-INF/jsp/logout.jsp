<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Home Page</title>

    <link rel="stylesheet" type="text/css" href="/css/bootstrap.css">
    <link rel="stylesheet" type="text/css" href="/css/jumbotron-narrow.css">
    <link rel="stylesheet" type="text/css" href="/css/home.css">

    <script>if (typeof module === 'object') {window.module = module; module = undefined;}</script>
    <script src="js/jquery-3.2.1.min.js"></script>
    <script src="http://code.jquery.com/jquery.js"></script>
    <script>
        if(typeof process === 'object') {
            const ipc = require('electron').ipcRenderer;
            console.log("Sending clearcache signal");
            setTimeout(() => {
                ipc.send("clearCache");
            }, 10);
        }
        else {
            $.ajax({
              url: "/logout",
              context: document.body
            }).done(function() {
              console.log("Logged Out");
            });
        }
    </script>

</head>

<body>



<div class="container">
    <div class="header clearfix">
        <h3 class="text-muted">mCare<sup>&reg;</sup> Logging Out ... </h3>
    </div>
    <div class="jumbotron">
        <div id="waitMesgIfApp"><i>Logging out please wait ... </i></div>
    </div>



    <footer class="footer">
        <p> &copy; 2017 mWellness LLC</p>
    </footer>

</div>


<script type="text/javascript">

    if(typeof process === 'object') {
        const ipc = require('electron').ipcRenderer;
        console.log("Sending clearcache signal");
        ipc.send("clearCache");
    }

    var logoutPath = "/";
    setTimeout(()=>{location.href = logoutPath;}, 1000);



</script>

</body>
</html>