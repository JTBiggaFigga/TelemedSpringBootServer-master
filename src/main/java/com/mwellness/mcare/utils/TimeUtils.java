package com.mwellness.mcare.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Savio Monteiro on 9/13/2016.
 */
public class TimeUtils {

    public static String longToHumanReadable(final long timestamp) {
        Date date = new Date(timestamp);
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm");

        return formatter.format(date);
    }

}
