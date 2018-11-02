package com.mwellness.mcare.ecg.sbeats;

import com.mwellness.mcare.AMainApp;
import com.mwellness.mcare.ecg.ecgsignal.EcgFileStorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by dev01 on 8/7/17.
 */
public class SBeatAccessor {

    public static final String ECG_STORAGE_PATH = EcgFileStorage.ECG_STORAGE_PATH;

    private static void log(String str) {
        AMainApp.log(SBeatAccessor.class.getSimpleName() + ": " + str);
    }

    public static long getMaxBeatSampleNumber(final int activationCode, final String studyType) {

        String filePath = ECG_STORAGE_PATH + studyType + "-" + activationCode + "-beats.dat";
        File beatFile = new File(filePath);
        long maxBeatSampleNumber = 0;
        RandomAccessFile ecgRAF = null;
        try {
            ecgRAF = new RandomAccessFile(beatFile, "r");

            maxBeatSampleNumber = ecgRAF.length();

            log("Last Beat at sampleNUmber: " + maxBeatSampleNumber);

            ecgRAF.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return maxBeatSampleNumber;
    }

    public static byte[] getBeats(final long startSampleNum, final long endSampleNumExcl, final int activationCode, final String studyType) {

        final long startPos = Long.BYTES + startSampleNum;
        int ecgSamplesLen = (int) (endSampleNumExcl - startSampleNum);
        int sBeatBytesLen = ecgSamplesLen;

        byte[] sBeatBytes = new byte[sBeatBytesLen];

        long start = System.currentTimeMillis();
        log("Fetching " + sBeatBytes + " bytes");

        final String filePath;

        filePath = ECG_STORAGE_PATH + studyType + "-beats-" + activationCode + ".dat";
        File beatFile = new File(filePath);

        log("Looking for data in: " + beatFile.getAbsolutePath());
        RandomAccessFile ecgRAF = null;
        try {
            ecgRAF = new RandomAccessFile(beatFile, "r");
            ecgRAF.seek(startPos);
            long bytesRead = ecgRAF.read(sBeatBytes);

            log("Bytes Read: " + bytesRead);

            ecgRAF.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ecgSamplesLen = 0;
            sBeatBytesLen = 0;
        } catch (IOException e) {
            e.printStackTrace();
            ecgSamplesLen = 0;
            sBeatBytesLen = 0;
        }

        long end = System.currentTimeMillis();
        log("Finished Reading " + sBeatBytesLen + " bytes in " + (end-start) + " ms");


        return sBeatBytes;
    }

}
