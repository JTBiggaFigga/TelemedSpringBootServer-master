package com.mwellness.mcare.vitals;

/**
 * Created by dev01 on 12/30/17.
 */

public class Vitals {
    public static final int VITAL_TYPE_NONE = -5000;
    public static final int VITAL_TYPE_SPO2 = 1;
    public static final int VITAL_TYPE_WEIGHT = 2;
    public static final int VITAL_TYPE_BP = 4;
    public static final int VITAL_TYPE_ECG = 5;
    public static final int VITAL_TYPE_PKFLOW = 6;
    public static final int VITAL_TYPE_SUGAR = 7;
    public static final int VITAL_TYPE_TEMP = 8;

    /**
     * Get the String Representation of each vital type.
     * @param vitalType
     * @return
     */
    public static String getStrOf(final int vitalType) {
        switch (vitalType) {
            case VITAL_TYPE_BP:
                return "Blood Pressure";
            case VITAL_TYPE_SPO2:
                return "Blood Oxygen";
            case VITAL_TYPE_SUGAR:
                return "Blood Sugar";
            case VITAL_TYPE_WEIGHT:
                return "Body Weight";
            case VITAL_TYPE_NONE:
                return "None";
            case VITAL_TYPE_ECG:
                return "ECG";
            case VITAL_TYPE_PKFLOW:
                return "Peak Flow";
            case VITAL_TYPE_TEMP:
                return "Body Temperature";
            default:
                return "None";
        }
    }
}
