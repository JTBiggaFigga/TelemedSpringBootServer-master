<html lang="en">
<head>
    <meta charset="utf-8">
    <!--<meta name="viewport" content="width=device-width, initial-scale=1">-->

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
        <%@ include file="../../shared.js" %>
        <%@ include file="../../sha1.js" %>
    </script>

    <script type="text/javascript">



        Number.prototype.pad = function(size) {
            var s = String(this);
            while (s.length < (size || 2)) {s = "0" + s;}
            return s;
        }


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

        var apiAuthToken = "";

        const auth0_headers = {"authorization": "Bearer " + idToken};

    </script>

    <style>
        body {
          padding: 0;
          margin: 0;
        }
        footer {
            padding: 10px;
        }
        h1,h2,h3,h4 {
            text-align: center;
            margin: 0px 50px 0 50px;
            padding: 15px;
        }
        main {

        }
        .jumbotron {
            box-shadow:none;
        }
        

    </style>
 </head>

 <body>

    <!-- place in body for some reason -->
    <script type="text/javascript" src="/js/mdb.min.js"></script>

    <header>

     <nav class="navbar fixed-top navbar-expand-lg navbar-dark unique-color-dark">


        <img src="/images/Leaf.png" width="30" />
        &nbsp;

        <span class="navbar-brand" href="#"><big>mCare<sup>&reg;</sup></big></span>

        <ul class="navbar-nav mr-auto">
            <li class="nav-item ${homeactive}">
                <a class="nav-link" href="/portal/home">Home</a>
            </li>
            <li class="nav-item ${vclinicactive}">
                <a class="nav-link" href="/doctor/vclinic">Virtual Clinic</a>
            </li>
        </ul>

        <ul class="nav navbar-nav navbar-nav ml-auto">
            <li class="navbar-brand">
                Physician - ${full_name}
                 &nbsp;
                <img src="${user.getPicture()}" width='40' height='40' style="border-radius: 50%;" />
            </li>
            <!--<li id="logout"><a onclick='logout()' class="nav-link" >Logout</a></li>-->
        </ul>

     </nav>
    </header>


    <main>
    <br /><br />
