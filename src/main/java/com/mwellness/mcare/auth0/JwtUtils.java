package com.mwellness.mcare.auth0;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

import java.io.UnsupportedEncodingException;

/**
 * Created by dev01 on 3/28/17.
 */

public class JwtUtils {


    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    public static JwtToken decode(String JWTEncoded) {
        try {
            String[] split = JWTEncoded.split("\\.");
            logger.info("\tJWT_DECODED: Header: " + getJson(split[0]));
            logger.info("\tJWT_DECODED: Body: " + getJson(split[1]));
            return new JwtToken(getJson(split[0]), getJson(split[1]));
        } catch (UnsupportedEncodingException e) {
            //Error
            return null;
        }
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException{
        byte[] decodedBytes = Base64Utils.decodeFromUrlSafeString(strEncoded);
        return new String(decodedBytes, "UTF-8");
    }
}
