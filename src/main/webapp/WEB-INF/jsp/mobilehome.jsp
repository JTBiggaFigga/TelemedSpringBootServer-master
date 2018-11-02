<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Home Page</title>
    <link rel="stylesheet" type="text/css" href="/css/bootstrap.css">
    <link rel="stylesheet" type="text/css" href="/css/jumbotron-narrow.css">
    <link rel="stylesheet" type="text/css" href="/css/home.css">
    <link rel="stylesheet" type="text/css" href="/css/jquery.growl.css"/>
    <script src="http://code.jquery.com/jquery.js"></script>
    <script src="/js/jquery.growl.js" type="text/javascript"></script>
    <script>

        var email = "${user.email}";
        var rolesCsv = "${rolesCsv}";
        var auth0UserId = "${user.getUserId()}";
        var name = "${user.getName()}";
        var picture = "${user.getPicture()}";
        var accessToken = "${access_token}";
        var idToken = "${id_token}";
        var fname = "${user.getGivenName()}";
        var lname = "${user.getFamilyName()}";

        try {
            SleepAuth0Java.setToken(email, auth0UserId, accessToken, idToken, name, fname, lname, picture);
            console.log(SharedPreferences.getString("name"));
        }
        catch(e) {
        }


    </script>
</head>

<body>

<div class="container">
    <div class="jumbotron">
        <p><img class="avatar" style="width:100px;" src="${user.getPicture()}" /></p>
        <p class="lead">${user.email}</p>
        <p class="lead">${user.getName()}</p>
        <p class="lead">${ rolesCsv }</p>
        <div id="waitMesgIfApp">hello</div>
    </div>
    <footer class="footer">
        <p> &copy; 2017 Qubit Labs LLC</p>
    </footer>

</div>

<script type="text/javascript">

    try {
        SleepAuth0Java.echo(".");
        document.querySelector("#waitMesgIfApp").innerHTML = "<i>Taking you home please wait ... </i>";
    } catch (e) {}

    $(function () {
        $.growl({title: "Welcome  ${user.nickname}", message: "We hope you enjoy using this site!"});
    });
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