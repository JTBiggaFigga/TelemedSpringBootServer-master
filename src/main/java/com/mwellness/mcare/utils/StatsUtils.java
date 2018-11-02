package com.mwellness.mcare.utils;

/**
 * Created by dev01 on 4/5/17.
 */
public class StatsUtils {

    public static short computeMin(final short[] arr) {
        short min = Short.MAX_VALUE;
        for(short s:arr) {
            if(s < min) {
                min = s;
            }
        }
        return min;
    }

    public static short computeMax(final short[] arr) {
        short max = Short.MIN_VALUE;
        for(short s:arr) {
            if(s > max) {
                max = s;
            }
        }
        return max;
    }

}
