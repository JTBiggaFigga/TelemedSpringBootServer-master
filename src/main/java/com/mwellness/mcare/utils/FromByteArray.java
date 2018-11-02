package com.mwellness.mcare.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class FromByteArray {


    private static String toBinary(short s) {

        String binString = Integer.toBinaryString(s & 0xffff);
        while (binString.length() < 16) {    //pad with 16 0's
            binString = "0" + binString;
        }
        binString = binString.substring(0, 8) + " " + binString.substring(8, binString.length());
        return binString;
    }

    private static String toBinary(byte b) {

        String binString = Integer.toBinaryString(b & 0xff);
        while (binString.length() < 8) {    //pad with 8 0's
            binString = "0" + binString;
        }
        return binString;
    }



    /**
     * Converts a byte array to a double array.
     * @param byteArr The input byte array
     * @return The double array
     */
    public static double[] toDoubleArray(final byte[] byteArr) {
        double doubleArr[];
        ArrayList<Double> darrL = new ArrayList<>();
        ByteBuffer bbuff = ByteBuffer.wrap(byteArr);
        while (bbuff.hasRemaining()) {
            darrL.add(bbuff.getDouble());
        }
        doubleArr = new double[darrL.size()];
        for (int i = 0; i < darrL.size(); i++) {
            doubleArr[i] = darrL.get(i);
        }
        return doubleArr;
    }


    /**
     * Converts a byte array to a int array.
     * @param byteArr The input byte array
     * @return The int array
     */
    public static int[] toIntArray(final byte[] byteArr) {
        int intArr[];
        ArrayList<Integer> darrL = new ArrayList<>();
        ByteBuffer bbuff = ByteBuffer.wrap(byteArr);
        while (bbuff.hasRemaining()) {
            darrL.add(bbuff.getInt());
        }
        intArr = new int[darrL.size()];
        for (int i = 0; i < darrL.size(); i++) {
            intArr[i] = darrL.get(i);
        }
        return intArr;
    }

    /**
     * Converts a byte array to a short array.
     * @param bytes The input byte array
     * @return The short array
     */
    /*public static short[] toShortArray(final byte[] byteArr) {
        short shortArr[];
        ArrayList<Short> sarrL = new ArrayList<>();
        ByteBuffer bbuff = ByteBuffer.wrap(byteArr);
        while (bbuff.hasRemaining()) {
            sarrL.add(bbuff.getShort());
        }
        shortArr = new short[sarrL.size()];
        for (int i = 0; i < sarrL.size(); i++) {
            shortArr[i] = sarrL.get(i);
        }
        return shortArr;
    }*/
    public static short[] toShortArray(final byte[] bytes) {
        short[] shorts = new short[bytes.length/2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }


    /**
     *
     * @param bLow
     * @param bHigh
     * @return
     */
    public static short toShort(final byte bLow, final byte bHigh) {
        ByteBuffer bbuff = ByteBuffer.wrap(new byte[]{bLow, bHigh});
        return bbuff.getShort();
    }


    /**
     * Convert an array of bytes to a Hexadecimal string
     * @param byteArr
     * @return
     */
    public static String toHexString(final byte[] byteArr) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArr.length * 3];
        for ( int j = 0; j < byteArr.length; j++ ) {
            int v = byteArr[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return "0 x " + new String(hexChars).toUpperCase();
    }

    /**
     * Convert a byte to a Hexadecimal string
     * @param b
     * @return
     */
    public static String toHexString(final byte b) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[2];
        //for ( int j = 0; j < byteArr.length; j++ ) {
            int v = b & 0xFF;
            hexChars[0] = hexArray[v >>> 4];
            hexChars[1] = hexArray[v & 0x0F];

        //}
        return "0x" + new String(hexChars).toUpperCase();
    }

}
