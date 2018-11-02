package com.mwellness.mcare.jni;

import com.mwellness.mcare.AMainApp;

/**
 * Created by dev01 on 4/13/17.
 */
public class QCardioJNI {

    private static void log(String str) {
        AMainApp.log(QCardioJNI.class.getSimpleName() + ": " + str);
    }



    public static native String runQCardioArrhythmiaDetection(final short[] ch1, final short[] ch2);


}
