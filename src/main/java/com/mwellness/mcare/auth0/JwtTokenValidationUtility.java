package com.mwellness.mcare.auth0;

import com.auth0.SessionUtils;
import com.auth0.Tokens;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.google.gson.Gson;
import com.mwellness.mcare.AMainApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

/**
 * Created by dev01 on 3/28/17.
 */
public class JwtTokenValidationUtility
{

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenValidationUtility.class);

    private static void log(String str) {
        logger.info(str);
    }

    /**
     * Verify an API access token if valid.
     * @param accessToken
     * @param algo
     * @param audience
     * @param clientId
     * @param signingSecret
     * @return
     */
    public static boolean isApiAccessTokenValid(final String accessToken, final String algo, final String audience, final String clientId, final String signingSecret) {
        JwtTokenBody jwtTokenBody;
        JwtTokenHeader jwtTokenHeader;

        log("Access Token: " + accessToken);
        JwtToken jwtToken = JwtUtils.decode(accessToken);

        try {

            jwtTokenBody = new Gson().fromJson(jwtToken.getBody(), JwtTokenBody.class);
            jwtTokenHeader = new Gson().fromJson(jwtToken.getHeader(), JwtTokenHeader.class);

        } catch (Exception e) {
            e.printStackTrace();
            log(e.getMessage());
            return false;
        }

        if(!jwtTokenHeader.getAlg().equals(algo)) {
            log("Incorrect Algo: " + algo);
            return false;
        }
        log("\tCORRECT ALGORITHM");

        if(!jwtTokenBody.getAud().equals(audience)) {
            log("Incorrect Audience: " + audience);
            return false;
        }
        log("\tCORRECT AUDIENCE");

        if(!jwtTokenBody.getSub().equals(clientId+"@clients")) {
            log("Incorrect Sub: " + jwtTokenBody.getSub());
            return false;
        }
        log("\tCORRECT SUB");

        if(!isValidSignature(accessToken, audience, signingSecret)) {
            log("Invalid Signature");
            return false;
        }
        log("\tVALID SIGNATURE");

        if(System.currentTimeMillis() > jwtTokenBody.getExp() * 1000) {
            log("Token Expired");
            return false;
        }
        log("\tTOKEN NOT EXPIRED");

        log("-> API ACCESS FOR '" + audience + "' TOKEN IS VALID");

        return true;
    }

    public static boolean tokenHasRole(String idToken, Role role) {
        JwtTokenBody jwtTokenBody;
        JwtToken jwtToken = JwtUtils.decode(idToken);

        try {
            jwtTokenBody = new Gson().fromJson(jwtToken.getBody(), JwtTokenBody.class);

            String[] tokenRoles = jwtTokenBody.getApp_metadata().getRoles();
            for(String tokenRole: tokenRoles) {
                if(tokenRole.equals(role.toString())) {
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            log(e.getMessage());
            return false;
        }

        return false;
    }

    public static String getUserId(HttpServletRequest request) {

        return getUserId(getIdTokenFromRequest(request));
    }

    public static String getUserId(final String idToken) {

        if(idToken == null) {
            return "INVALID_TOKEN";
        }

        if(isIdTokenValid(idToken)) {

            JwtTokenBody jwtTokenBody;

            JwtToken jwtToken = JwtUtils.decode(idToken);

            try {

                jwtTokenBody = new Gson().fromJson(jwtToken.getBody(), JwtTokenBody.class);
                log("jwtTokenBody: " + jwtTokenBody);
                String userId = jwtTokenBody.user_id;
                log("UserId: " + userId);
                if(userId == null || userId.isEmpty()) {
                    userId = jwtTokenBody.sub;
                }

                return userId;

            } catch (Exception e) {
                e.printStackTrace();
                log(e.getMessage());
                return "INVALID_TOKEN";
            }
        }
        else {
            return "INVALID_TOKEN";
        }
    }



    /**
     * Verify if a token has a valid HMAC256 signature
     * @param audience
     * @param signingSecret
     * @return
     */
    public static boolean isValidSignature(final String accessToken, final String audience, final String signingSecret) {
        // TODO: implement this ...

        JWTVerifier jwtVerifier = new JWTVerifier(signingSecret, audience);
        try {
            jwtVerifier.verify(accessToken);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (SignatureException e) {
            e.printStackTrace();
            return false;
        } catch (JWTVerifyException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static String getIdTokenFromRequest(HttpServletRequest request) {

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
                        return null;
                    }
                }

            }
            else {
                token = tokens.getIdToken();
            }


            if(token == null || token.equals("")) {
                log("Token not found in Session");
                return null;
            }

            return token;
        }
        else {
            log("Token found in request parameter 'token'... ");
            return token;
        }
    }


    public static boolean isIdTokenValid(HttpServletRequest request) {

        String token = getIdTokenFromRequest(request);

        if(token == null || token.equals("")) {
            log("Token not found in Session");
            return false;
        }
        else {
            log("Bottom Line ... Token found ... ");
            return isIdTokenValid(token);
        }

    }


    /**
     * Validate ID JWT token
     * @param idToken
     * @return
     */
    public static boolean isIdTokenValid(String idToken) {

        JwtTokenBody jwtTokenBody;
        JwtTokenHeader jwtTokenHeader;

        log(idToken);
        JwtToken jwtToken = JwtUtils.decode(idToken);

        log(jwtToken.getHeader());

        try {
            jwtTokenBody = new Gson().fromJson(jwtToken.getBody(), JwtTokenBody.class);
            jwtTokenHeader = new Gson().fromJson(jwtToken.getHeader(), JwtTokenHeader.class);
        } catch (Exception e) {
            e.printStackTrace();
            log(e.getMessage());
            return false;
        }

        // TODO: Make it RS256
        /*if(!jwtTokenHeader.getAlg().equals("RS256")) {
            log("WRONG ALGO");
            return false;
        }*/

        if(!jwtTokenHeader.getTyp().equals("JWT")) {
            log("WRONG Type");
            return false;
        }

        if(jwtTokenBody.getEmail() == null) {
            log("NO Email");
            return false;
        }

        if(jwtTokenBody.getEmail().equals(null)) {
            log("NO Email");
            return false;
        }

        if(!jwtTokenBody.getIss().equals(AMainApp.Auth0Domain)) {
            log("Wrong ISS");
            return false;
        }

        return true;
    }
}
