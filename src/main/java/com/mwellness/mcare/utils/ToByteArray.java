package com.mwellness.mcare.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ToByteArray {

    /**
     * Convert a short array into it's equivalent byte array representation
     * @param shorts
     * @return
     */
    /*public static byte[] fromShortArray(short[] input) {

        byte[] bytes = new byte[input.length * Short.SIZE / 8];

        for (int i = 0, j = 0; i < input.length; i++) {

            bytes[j] = (byte) (input[i] & 0xff);
            j++;

            bytes[j] = (byte) ((input[i] >> 8) & 0xff);
            j++;
        }

        return bytes;
    }*/
    public static byte[] fromShortArray(final short[] shorts) {
        byte[] bytes = new byte[shorts.length * 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts);
        return bytes;
    }

    /**
     * Get the 2 bytes representing an short as a byte array
     * @param s The short value
     * @return
     */
    public static byte[] fromShortOld(short s) {

        byte[] bytes = new byte[2];

        bytes[0] = (byte)(s & 0xff);
        bytes[1] = (byte)((s >> 8) & 0xff);

        return bytes;
    }

    public static byte[] fromShortBigEndian(short value) {

        byte[] bytes = new byte[2];
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putShort(value);
        return buffer.array();
    }

    public static byte[] fromShortLittleEndian(short value) {

        byte[] bytes = new byte[2];
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(value);
        return buffer.array();
    }


    /**
     * Get the 4 bytes representing an integer as a byte array
     * @param input
     * @return
     */
    public static byte[] fromInt(int input) {
        return new byte[] {
                (byte) (input >>> 24),
                (byte) (input >>> 16),
                (byte) (input >>> 8),
                (byte)  input
        };
    }

    /**
     * Convert an int array into it's equivalent byte array representation
     * @param input
     * @return
     */
    public static byte[] fromIntArray(int[] input) {

        byte[] bytes = new byte[input.length * Integer.SIZE / 8];

        for(int i = 0; i < input.length; i++) {

            byte[] bb = fromInt(input[i]);
            for(int j = i, k = 0; j < (i+4); j++, k++) {
                bytes[j] = bb[k];
            }
        }

        return bytes;
    }

    /**
     * Check if an input hexadecimal string in indeed hexadecimal.
     * @param hexStr
     * @return
     */
    public static boolean isValidHexString(String hexStr) {
        return hexStr.matches("^[0-9a-fA-F]+$");
    }




    /**
     * To get a byte array representation of an input hexadecimal string
     * @param hexString The input Hexadecimal String
     * @return The byte array representing the input Hex String
     */
    public static byte[] fromHexString(String hexString) {

        byte[] byteArr = new byte[]{};
        if(!isValidHexString(hexString)) {
            return byteArr;
        }

        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }



    public static byte[] appendToByteArray(final byte[] input, final short s) {
        return appendToByteArray(input, fromShortLittleEndian(s));
    }

    public static byte[] appendToByteArray(final byte[] input, final byte b) {

        int finalLength = input.length + 1;
        byte[] out = new byte[finalLength];
        for(int i = 0; i < input.length; i++) {
            out[i] = input[i];
        }
        out[finalLength - 1] = b;

        return out;
    }

    public static byte[] appendToByteArray(final byte[] input, final byte[] bArr) {

        int finalLength = input.length + bArr.length;
        byte[] out = new byte[finalLength];
        for(int i = 0; i < input.length; i++) {
            out[i] = input[i];
        }
        for(int i = input.length, j = 0; j < bArr.length; i++, j++) {
            out[i] = bArr[j];
        }

        return out;
    }

    public static byte[] appendToByteArray(final byte[] input, final int i) {
        return new byte[] {};
    }

}
