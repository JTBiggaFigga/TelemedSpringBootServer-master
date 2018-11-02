package com.mwellness.mcare.utils;

import java.nio.ByteBuffer;

import java.nio.ByteBuffer;

/**
 * Created by Savio Monteiro on 9/13/2016.
 */
public class ByteArrayUtils {

    public static byte[] appendArray(final byte[] one, final byte[] two) {

        byte[] combined = new byte[one.length + two.length];

        System.arraycopy(one, 0, combined, 0         , one.length);
        System.arraycopy(two, 0, combined, one.length, two.length);

        return combined;
    }


    /**
     *
     * @param input
     * @return
     */
    public static byte[] toByteArray(final short[] input) {

        byte[] bytes = new byte[input.length * (Short.SIZE / 8)];

        for (int i = 0, j = 0; i < input.length; i++) {

            bytes[j] = (byte) (input[i] & 0xff);
            j++;

            bytes[j] = (byte) ((input[i] >> 8) & 0xff);
            j++;
        }

        return bytes;
    }

    /**
     *
     * @param input
     * @return
     */
    public static short[] toShortArray(final byte[] input) {

        short[] shorts = new short[input.length / (Short.SIZE / 8)];


        for (int i = 0, j = 0; i < input.length; i+=2) {

            final byte low = input[i];
            final byte high = input[i+1];

            shorts[j] = (short) ( ((high & 0xff) << 8) | (low & 0xff) );
            j++;
        }

        return shorts;
    }

    public static short toShort(final byte high, final byte low) {
        return (short) ( ((high & 0xff) << 8) | (low & 0xff) );
    }


    public static byte[] toByteArray(final short x) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.putShort(x);
        return buffer.array();
    }

    public static byte[] toByteArray(final int x) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(x);
        return buffer.array();
    }

    public static byte[] toByteArray(final long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static int toInt(final byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getInt();
    }

    public static long toLong(final byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }

}
