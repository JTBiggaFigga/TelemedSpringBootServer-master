package com.mwellness.mcare.jni;

import com.mwellness.mcare.AMainApp;

/**
 * Created by qubit on 3/22/18.
 */
public class QAFibJNI {

    private static void log(String str) {
        AMainApp.log(QCardioJNI.class.getSimpleName() + ": " + str);
    }

    public static native boolean runAFibDetection(final int[] beatPosArr);

}
