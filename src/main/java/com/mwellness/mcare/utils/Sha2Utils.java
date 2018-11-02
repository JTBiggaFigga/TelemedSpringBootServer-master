package com.mwellness.mcare.utils;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by dev01 on 12/14/16.
 */
public class Sha2Utils {

    public static String getSha2Hash(final String string) {

        return DigestUtils.sha256Hex(string);

    }

}
