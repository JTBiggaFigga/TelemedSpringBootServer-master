package com.mwellness.mcare.auth0;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Arrays;

/**
 * Created by dev01 on 3/28/17.
 */
public class JwtTokenVerifier
{

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenVerifier.class);

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

        logger.info("Access Token: " + accessToken);
        JwtToken jwtToken = JwtUtils.decode(accessToken);

        try {

            jwtTokenBody = new Gson().fromJson(jwtToken.getBody(), JwtTokenBody.class);
            jwtTokenHeader = new Gson().fromJson(jwtToken.getHeader(), JwtTokenHeader.class);

        } catch (Exception e) {
            e.printStackTrace();
            logger.info(e.getMessage());
            return false;
        }

        if(!jwtTokenHeader.getAlg().equals(algo)) {
            logger.info("Incorrect Algo: " + algo);
            return false;
        }
        logger.info("\tCORRECT ALGORITHM");

        if(!jwtTokenBody.getAud().equals(audience)) {
            logger.info("Incorrect Audience: " + audience);
            return false;
        }
        logger.info("\tCORRECT AUDIENCE");

        if(!jwtTokenBody.getSub().equals(clientId+"@clients")) {
            logger.info("Incorrect Sub: " + jwtTokenBody.getSub());
            return false;
        }
        logger.info("\tCORRECT SUB");

        if(!isValidSignature(accessToken, audience, signingSecret)) {
            logger.info("Invalid Signature");
            return false;
        }
        logger.info("\tVALID SIGNATURE");

        if(System.currentTimeMillis() > jwtTokenBody.getExp() * 1000) {
            logger.info("Token Expired");
            return false;
        }
        logger.info("\tTOKEN NOT EXPIRED");

        logger.info("-> API ACCESS FOR '" + audience + "' TOKEN IS VALID");

        return true;
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


    public static String[] getRolesFromIdToken(String idToken) {
        JwtTokenBody jwtTokenBody;
        JwtTokenHeader jwtTokenHeader;

        JwtToken jwtToken = JwtUtils.decode(idToken);

        try {
            jwtTokenBody = new Gson().fromJson(jwtToken.getBody(), JwtTokenBody.class);
            jwtTokenHeader = new Gson().fromJson(jwtToken.getHeader(), JwtTokenHeader.class);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info(e.getMessage());
            return new String[]{};
        }

        String[] roleStr = jwtTokenBody.getApp_metadata().getRoles();

        return roleStr;
    }

    /**
     * Validate ID JWT token
     * @param idToken
     * @param role
     * @return
     */
    public static boolean isIdTokenValid(String idToken, String role) {

        JwtTokenBody jwtTokenBody;
        JwtTokenHeader jwtTokenHeader;


        JwtToken jwtToken = JwtUtils.decode(idToken);



        try {
            jwtTokenBody = new Gson().fromJson(jwtToken.getBody(), JwtTokenBody.class);
            jwtTokenHeader = new Gson().fromJson(jwtToken.getHeader(), JwtTokenHeader.class);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info(e.getMessage());
            return false;
        }

        if(!jwtTokenHeader.getAlg().equals("HS256")) {
            return false;
        }

        if(!jwtTokenHeader.getTyp().equals("JWT")) {
            return false;
        }

        if(jwtTokenBody.getEmail() == null) {
            return false;
        }

        if(Arrays.asList(jwtTokenBody.getApp_metadata().getRoles()).contains(role)) {
            return true;
        }

        return false;
    }
}
