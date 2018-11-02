package com.mwellness.mcare.utils;

/**
 * Created by dev01 on 8/7/17.
 */
public class BitUtils {

    public static byte getBit(final byte b, final int position)
    {
        return (byte) ((b >> position) & 1);
    }

    public static String byteToString(final byte b) {
        return ("0000000" + Integer.toBinaryString(0xFF & b)).replaceAll(".*(.{8})$", "$1");
    }


    public static byte bitStringToByte(final String bitString) throws IllegalArgumentException {
        if(bitString.length() > 8) {
            throw new IllegalArgumentException("Bit String should have upto 8 characters");
        }

        if(!bitString.matches("[01]+")) {
            throw new IllegalArgumentException("Bit String should contain only 1's and 0's");
        }

        return (byte) Integer.parseInt(bitString, 2);
    }

    public static String byteToHex(final byte b) {

        return String.format("%02x", b);
        //return Integer.toHexString(b & 0xff);
    }

}
