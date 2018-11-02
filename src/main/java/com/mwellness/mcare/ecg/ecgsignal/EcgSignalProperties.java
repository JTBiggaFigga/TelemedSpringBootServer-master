package com.mwellness.mcare.ecg.ecgsignal;


import com.mwellness.mcare.utils.ByteArrayUtils;

/**
 * Created by Savio Monteiro on 9/13/2016.
 */
public class EcgSignalProperties {

    // constants
    public static final int CHUNK_DURATION_SECONDS = 10;

    public static final int CHANNELS = 2;
    public static final int BYTES_PER_SAMPLE = 2;
    public static final int SAMPLE_RATE = 250;

    // computed
    public static final int CHUNK_SIZE_BYTES_PER_CHANNEL = CHUNK_DURATION_SECONDS * SAMPLE_RATE * BYTES_PER_SAMPLE;

    public static int getSampleCountForMillis(int milliseconds) {
        return ( (SAMPLE_RATE * milliseconds) / 1000 );
    }

    public static byte[] createBlankEcgBytes(final int numberOfSamples) {
        short[] out = new short[] {};
        for(int i = 0; i < numberOfSamples; i++) {
            out[i] = -9999;
        }

        return ByteArrayUtils.toByteArray(out);
    }

    public static long sampleCountForMillis(final long millis) {
        return ( (SAMPLE_RATE * millis) / 1000 );
    }
}
