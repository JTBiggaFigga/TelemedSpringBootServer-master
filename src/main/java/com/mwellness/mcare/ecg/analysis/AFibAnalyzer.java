package com.mwellness.mcare.ecg.analysis;

import com.mwellness.mcare.AMainApp;
import com.mwellness.mcare.jni.QAFibJNI;

/**
 * Created by dev01 on 6/28/17.
 */
public class AFibAnalyzer {

    private static void log(String str) {
        AMainApp.log(AFibAnalyzer.class.getSimpleName() + ": " + str);
    }

    public static boolean detectAFib(final long[] beatPosArrLongs) {

        int[] beatPosArr = new int[beatPosArrLongs.length];
        for(int i = 0; i < beatPosArrLongs.length; i++) {
            beatPosArr[i] = (int) beatPosArrLongs[i];
        }

        boolean processingOutput = QAFibJNI.runAFibDetection(beatPosArr);
        log("Processing Output: " + processingOutput);
        return processingOutput;
    }


}
