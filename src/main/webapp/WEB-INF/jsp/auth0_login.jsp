<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html lang="en">
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>mWellness: Login</title>

    <!-- Insert this line above script imports  -->
    <script>if (typeof module === 'object') {window.module = module; module = undefined;}</script>

    <script src="/js/jquery-3.2.1.min.js"></script>

    <script src="https://cdn.auth0.com/js/lock/10.0/lock.min.js"></script>

    <script>

        var ourAllowedConnections = [];
        function getAllowedConnections() {
            ourAllowedConnections = ['google-oauth2'];
            /*
            if(isTech())
                ourAllowedConnections = ['google-oauth2'];
            else
                ourAllowedConnections = ['Username-Password-Authentication', 'google-oauth2'];
            */
        }

        function isTech() {

            if(typeof process === 'object') {
                var appType = require("electron").remote.app.appType;
                console.log("appType: " + appType);
                if(appType === "twb") {
                    return true;
                }
                else {
                    return false;
                }
            }

            return false;
        }


        getAllowedConnections();
    </script>


    <style>
        #a0-lock * {
          -webkit-animation: none !important;
          animation: none !important;
          -webkit-transition: none !important;
          transition: none !important;
        }
    </style>

</head>
<body>
<div class="container">
    <script type="text/javascript">

        $(document).ready(function () {

            var lock = new Auth0Lock('${clientId}', '${clientDomain}', {
                rememberLastLogin: false,
                allowSignUp: !isTech(),
                allowedConnections: ourAllowedConnections,
                auth: {
                    redirectUrl: '${fn:replace(pageContext.request.requestURL, pageContext.request.requestURI, '')}${loginCallback}',
                    responseType: 'code',
                    params: {
                        state: '${state}',
                        // Learn about scopes: https://auth0.com/docs/scopes
                        scope: 'openid user_id name nickname email picture app_metadata',
                        prompt: 'select_account'
                    }
                },
                theme: {
                    /*primaryColor: 'black',*/
                    /*logo: 'https://www.qubitmed.com/images/qubitlabs_logo_2.png'*/
                    logo: 'https://www.qubitmed.com/images/mWellnessLogo.png'
                },
                languageDictionary: {
                    title: ""
                },
                closable: false

            });
            // delay to allow welcome message..
            setTimeout(function () {
                lock.show();
            }, 1500);
        });


        /*
        additionalSignUpFields: [
                            {
                                name: "fname",
                                placeholder: "First Name",
                                validator: function(fname) {
                                    return {
                                        valid: fname.length >= 5,
                                        hint: "Must have 5 or more chars" // optional
                                    };
                                }
                            },
                            {
                                name: "lname",
                                placeholder: "Last Name",
                                validator: function(lname) {
                                    return {
                                        valid: lname.length >= 5,
                                        hint: "Must have 5 or more chars" // optional
                                    };
                                }
                            },
                            / * {
                                name: "address",
                                placeholder: "Address",
                                prefill: "",
                                validator: function(address) {
                                    return {
                                        valid: address.length >= 10,
                                        hint: "Must have 10 or more chars" // optional
                                    };
                                }
                            },* /
                            {
                                name: "phone",
                                placeholder: "Phone Number",
                                validator: function(phone) {
                                    return {
                                        valid: /[0-9]|\./.test(phone),
                                        hint: "Must have numeric values" // optional
                                    };
                                }
                            },
                            {
                                name: "date_of_birth",
                                placeholder: "Date of Birth"
                            },
                            {
                                type: "select",
                                name: "gender",
                                placeholder: "Gender",
                                options: [
                                    {value: "m", label: "Male"},
                                    {value: "f", label: "Female"},
                                ],

                            }
                        ]
        */
    </script>
</div>
</body>
</html>
