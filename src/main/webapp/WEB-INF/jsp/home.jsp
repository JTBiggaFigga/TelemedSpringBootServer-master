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

    <script src="http://code.jquery.com/jquery.js"></script>

    <script>

        var email = "${user.email}";
        var rolesCsv = "${rolesCsv}";
        var auth0UserId = "${user.getUserId()}";
        var full_name = "${ full_name }";
        var picture = "${user.getPicture()}";
        var accessToken = "${access_token}";
        var idToken = "${id_token}";
        var fname = "${fname}";
        var lname = "${lname}";
        var tokenExpires = ${ tokenExpires };

        try {
            SleepAuth0Java.setToken(email, auth0UserId, accessToken, idToken, full_name, fname, lname, picture);
            console.log(SharedPreferences.getString("name"));
        }
        catch(e) {
            console.log(e);
        }

        try {
            TMAuth0Java.setToken(email, auth0UserId, accessToken, idToken, full_name, fname, lname, picture);
            console.log(SharedPreferences.getString("name"));
        }
        catch(e) {
            console.log(e);
        }




    </script>
</head>

<body>

<div class="container">
    <div class="header clearfix">
        <%--<nav>--%>
            <%--<ul class="nav nav-pills pull-right">--%>
                <%--<li class="active" id="home"><a href="#">Home</a></li>--%>
                <%--<li id="logout"><a href="#">Logout</a></li>--%>
            <%--</ul>--%>
        <%--</nav>--%>
        <h3 class="text-muted">mCare<sup>&reg;</sup> Home</h3>
    </div>
    <div class="jumbotron">
        <div id="waitMesgIfApp">hello</div>
    </div>



    <footer class="footer">
        <p> &copy; 2017 mWellness LLC</p>
    </footer>

</div>

<script type="text/javascript">


    try {
        SleepAuth0Java.echo("");
        document.querySelector("#waitMesgIfApp").innerHTML = "<i>Taking you home please wait ... </i>";
    } catch (e) {}

    try {
        if(typeof process === 'object') {
                document.querySelector("#waitMesgIfApp").innerHTML = "<i>Taking you home please wait ... </i>";
        }
        else {
            if(rolesCsv.split(",").includes("technician")) {
                document.querySelector("#waitMesgIfApp").innerHTML = "<span><b>Please use the TAG software.</b></span>";
            }
        }
    } catch (e) {}

    try {
        if(typeof process === 'object') {
            const electron = require("electron");
            const remote = require("electron").remote;
            const Authorization = remote.app.Authorization;
            const ipc = require('electron').ipcRenderer;

            console.log(rolesCsv);
            console.log(rolesCsv.split(", ").includes("technician"));

            if(!rolesCsv.split(", ").includes("technician")) {

                document.querySelector("#waitMesgIfApp").innerHTML = "<span style='color:red'><h3>Not a valid technician. Logging out.</h3></span>";
                console.log("Sending clearcache signal");
                ipc.send("clearCache");

                const logoutPath = "{{logoutPath}}";
                setTimeout(()=>{location.href = logoutPath;}, 3000);
            }
            else {

                console.log("Setting Token ... ");
                console.log("Token = " + Authorization.setToken(email, auth0UserId, accessToken, idToken, full_name, fname, lname, rolesCsv, picture, tokenExpires));

                console.log("Sending showHome signal");
                setTimeout(()=>{
                    ipc.send("showHome");
                }, 2000);

            }


        }
    } catch (e) {

    }


    $("#logout").click(function(e) {
        e.preventDefault();
        $("#home").removeClass("active");
        $("#password-login").removeClass("active");
        $("#logout").addClass("active");
        // assumes we are not part of SSO so just logout of local session
        window.location = "${fn:replace(pageContext.request.requestURL, pageContext.request.requestURI, '')}/logout";
    });


</script>

</body>
</html>