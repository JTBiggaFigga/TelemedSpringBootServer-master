package com.mwellness.mcare.utils;

import java.util.UUID;

/**
 * Created by dev01 on 12/13/16.
 */
public class UuidUtils {

    public static String generateUuidStr32() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }


}
