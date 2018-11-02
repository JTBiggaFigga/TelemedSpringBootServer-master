<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>Unassigned Role</title>

    <link rel="stylesheet" type="text/css" href="/css/bootstrap.css">
    <link rel="stylesheet" type="text/css" href="/css/jumbotron-narrow.css">
    <link rel="stylesheet" type="text/css" href="/css/home.css">

    <script src="http://code.jquery.com/jquery.js"></script>

</head>

<body>



    <div class="container">
        <div class="header clearfix">
            <h3 class="text-muted">mCare<sup>&reg;</sup></h3>
        </div>
        <div class="jumbotron">
            <div id="waitMesgIfApp">

                    You have yet to be assigned access to the mCare Platform.
                    <br /><br />
                    Please try again within 24 hours.
                    <br /><br />
                    If you still do not gain access kindly contact us at <a href="email:mwellnessinfo@gmail.com">mwellnessinfo@gmail.com</a> for assistance.

                <br /><br />
                <button class="btn btn-primary" onclick="location.href='/'">Back to Login Options</button>
                </div>
        </div>



        <footer class="footer">
            <p><script>document.write(new Date().getFullYear())</script> &copy; mWellness LLC</p>
        </footer>

    </div>


</body>
</html>