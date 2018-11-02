package com.mwellness.mcare.ecg.analysis;

import com.google.gson.Gson;
import com.mwellness.mcare.AMainApp;
import com.mwellness.mcare.jni.QCardioJNI;
import com.mwellness.mcare.utils.ArrayScaler;

/**
 * Created by dev01 on 6/28/17.
 */
public class ArrhythmiaAnalyzer {

    private static void log(String str) {
        AMainApp.log(ArrhythmiaAnalyzer.class.getSimpleName() + ": " + str);
    }

    public static String detectArrhythmias(final short[] ch1ShortArr, final short[] ch2ShortArr) {
        log("Processing CH1: " + new Gson().toJson(ch1ShortArr));
        String processingOutput = QCardioJNI.runQCardioArrhythmiaDetection(ch1ShortArr, ch2ShortArr);
        log("Processing Output: \n\n" + processingOutput);
        log("======= END OF PROCESSING ========");

        return processingOutput;
    }

    public static long getDownsampledSampleNumber(long origSampleNum) {
        return ((250 * origSampleNum) / 360);
    }

    public static short[] downsampleTo250(short[] inputAt360) {
        if(inputAt360.length != 3600) {
            throw new IllegalArgumentException("Input must have 3600 samples");
        }
        return ArrayScaler.scaleArray(inputAt360,2500);
    }

}
