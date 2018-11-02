package com.mwellness.mcare.utils;

/**
 * Created by Savio Monteiro on 1/20/2016.
 */
public class ArrayConcat {


    public static int[] concat(final int[] a, final int[] b) {
        int aLen = a.length;
        int bLen = b.length;
        int[] c = new int[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }



    public static byte[] concat(final byte[] a, final byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }


    public static short[] concat(final short[] a, final short[] b) {
        int aLen = a.length;
        int bLen = b.length;
        short[] c = new short[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

}
