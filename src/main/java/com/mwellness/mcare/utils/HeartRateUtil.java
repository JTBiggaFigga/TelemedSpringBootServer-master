package com.mwellness.mcare.utils;

/**
 * Created by dev01 on 8/9/17.
 */
public class HeartRateUtil {

    public static int computeHeartRate(int sampleCount, int sampleRate) {
        if(sampleCount == 0) {
            return -5000;
        }
        return (int) Math.floor( ( 60 * sampleRate ) / sampleCount );
    }
}
