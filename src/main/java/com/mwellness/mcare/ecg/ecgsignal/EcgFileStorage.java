package com.mwellness.mcare.ecg.ecgsignal;

import com.mwellness.mcare.utils.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class EcgFileStorage {


    private static final Logger logger = LoggerFactory.getLogger(EcgFileStorage.class);
    public static final String ECG_STORAGE_PATH = "../ecgdata/";

    /**
     * Saves a chunk to ECG Random Access File
     * @param sessionId
     * @param sampleCountSinceLastChunk
     * @param rawMit16Bytes
     * @throws IOException
     */
    public static void saveEcgStudyChunkToFileRecord(final String sessionId, final long sampleCountSinceLastChunk, final byte[] rawMit16Bytes) throws IOException {

        RandomAccessFile fileStore = new RandomAccessFile(new File(ECG_STORAGE_PATH + "ecg-" +  sessionId+".dat"), "rw");
        long byteOffset = sampleCountSinceLastChunk * 4;

        logger.info("Writing chunk from " + byteOffset + " with length " + rawMit16Bytes.length);
        fileStore.seek(byteOffset);

        fileStore.write(rawMit16Bytes);

        fileStore.close();
    }


    /**
     * For sleep data ...
     * @param sessionId
     * @param rawMit16Bytes
     * @throws IOException
     */
    public static void saveFullSleepEcgFile(final String sessionId, final byte[] rawMit16Bytes) throws IOException {


        File file = new File(ECG_STORAGE_PATH + "sleep-" + sessionId+".dat");

        if(file.exists()) {
            file.delete();
        }

        RandomAccessFile fileStore = new RandomAccessFile(file, "rw");

        fileStore.seek(0);

        fileStore.write(rawMit16Bytes);

        fileStore.close();
    }

    public static void saveStudyStartTimestamp(final String sessionId, final String studyType, final long firstSampleTimestamp) throws IOException {
        byte[] tsBytes = ByteArrayUtils.toByteArray(firstSampleTimestamp);
        FileOutputStream fos = new FileOutputStream(ECG_STORAGE_PATH + studyType + "-" + sessionId+ "-meta.dat");
        fos.write(tsBytes);
        fos.flush();
        fos.close();
    }


}
