package com.mwellness.mcare.controllers;

import com.auth0.SessionUtils;
import com.auth0.Tokens;
import com.google.gson.Gson;
import com.mwellness.mcare.AMainApp;
import com.mwellness.mcare.auth0.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dev01 on 12/11/17.
 */
public class ApiAccessControl {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ApiAccessControl.class);

    private static void log(String str) {
        AMainApp.log(ApiAccessControl.class.getSimpleName() + ": " + str);
        //logger.info(str);
    }

    /**
     * Allow access to current request if jwt token has roles in allowedRoles.
     * @param request
     * @param allowedRoles
     * @return ALLOW or DENY:<message>
     */
    public static String areRolesAllowed(HttpServletRequest request, Role[] allowedRoles) {

        String token = request.getParameter("token");
        if(token == null || token.equals("")) {
            log("No token in request parameter 'token'");
            Tokens tokens = SessionUtils.getTokens(request);
            if(token == null) {
                log("Token not found in Session .. checking authorization header ... ");
                String authorizationHeader = request.getHeader("authorization");
                if(authorizationHeader == null) {
                    log(" ... checking authorization header ... NOPE NOT FOUND ... 1");
                }
                else {
                    String[] authSplitArr = authorizationHeader.split("Bearer ");
                    log(authSplitArr.length + " .... boohoo ... " + new Gson().toJson(authSplitArr));
                    if(authSplitArr.length == 2) {
                        token = authSplitArr[1];
                        log(" ... checking authorization header ... YESSSS! FOUND IN AUTH HEADER!");
                    }
                    else {
                        log(" ... checking authorization header ... NOPE NOT FOUND ... 2");
                        return "DENY";
                    }
                }

            }
            else {
                token = tokens.getIdToken();
            }


            if(token == null || token.equals("")) {
                log("Token not found in Session");
                return "DENY";
            }
            else {
                log("Bottom Line ... Token found ... ");
                return areRolesAllowed(token, allowedRoles);
            }
        }
        else {
            log("Token found in request parameter 'token'... ");
            return areRolesAllowed(token, allowedRoles);
        }



    }

    public static String areRolesAllowed(String idToken, Role[] allowedRoles) {

        JwtTokenBody jwtTokenBody;
        JwtTokenHeader jwtTokenHeader;

        log(idToken);
        JwtToken jwtToken = JwtUtils.decode(idToken);

        log(jwtToken.getHeader());

        try {
            jwtTokenBody = new Gson().fromJson(jwtToken.getBody(), JwtTokenBody.class);

            String[] tokenRoles = jwtTokenBody.getApp_metadata().getRoles();
            if(tokenRoles.length == 0) {
                log("tokenRoles not found ... ");
                String userId = jwtTokenBody.user_id;
                if(jwtTokenBody.user_id.equals(""))
                    userId = jwtTokenBody.sub;
                log(" ... geting app metadata using auth0proxy for userid: " + userId);
                final String auth0UserJson = new Auth0UserDirectoryProxy(userId).getAuth0UserJson();

                JSONArray rolesJsonArr = new JSONObject(auth0UserJson).getJSONObject("app_metadata").getJSONArray("roles");
                String rolesArr[] = new String[rolesJsonArr.length()];

                for(int i = 0; i < rolesJsonArr.length(); i++) {
                    rolesArr[i] = (String) rolesJsonArr.get(i);
                }

                tokenRoles = rolesArr;

                /*Map<String, Object> appMetaData = auth0User.getAppMetadata();
                log("App MetaData: " + AMainApp.gson.toJson(appMetaData));
                String rolesJsonArrStr = (appMetaData.get("roles")).toString();
                log("RolesJson: " + rolesJsonArrStr);
                tokenRoles = AMainApp.gson.fromJson(rolesJsonArrStr.toString(), String[].class);
                */


            }

            for(Role role: allowedRoles) {
                String currAllowedRole = role.toString();
                for(String tokenRole:tokenRoles) {
                    log("Comparing allowedRole: " + currAllowedRole + " with tokenRole: " + tokenRole);
                    if(tokenRole.equals(currAllowedRole)) {
                        return "ALLOW";
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            log(e.getMessage());
            return "DENY";
        }


        return "DENY";
    }

    public static boolean tokenHasRole(String idToken, Role role) {
        /*JwtTokenBody jwtTokenBody;
        JwtToken jwtToken = JwtUtils.decode(idToken);

        try {
            jwtTokenBody = new Gson().fromJson(jwtToken.getBody(), JwtTokenBody.class);

            String[] tokenRoles = jwtTokenBody.getApp_metadata().getRoles();
            for(String tokenRole: tokenRoles) {
                log("Comparing tokenRole: " + tokenRole + " with tokenRole: " + role.toString());
                if(tokenRole.contains(role.toString())) {
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            log(e.getMessage());
            return false;
        }*/

        return areRolesAllowed(idToken, new Role[]{role}).equals("ALLOW");
    }

}
