package com.mwellness.mcare.ecg.ecgsignal;

import com.google.gson.Gson;
import com.mwellness.mcare.AMainApp;
import com.mwellness.mcare.utils.StatsUtils;
import com.mwellness.mcare.utils.ByteArrayUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by dev01 on 3/31/17.
 */
public class EcgAccessor
{

    public static final String ECG_STORAGE_PATH = "/home/dev01/Projects/qubitlabs/aretas/code/ecgdata/";

    private static void log(String str) {
        AMainApp.log(EcgAccessor.class + ": " + str);
    }

    private static File ecgFile;

    public static EcgStrip getEcgShorts(final long startSampleNum, final long endSampleNumExcl, final int activationCode, final String studyType)  {

        final long startPos = startSampleNum * 4;
        int ecgSamplesLen = (int) (endSampleNumExcl - startSampleNum);
        int ecgBytesLen = ecgSamplesLen * 4;

        byte[] ecgBytes = new byte[ecgBytesLen];

        long start = System.currentTimeMillis();
        log("Fetching " + ecgBytesLen + " bytes");

        final String filePath;

        filePath = ECG_STORAGE_PATH + studyType + "-" + activationCode + ".dat";
        ecgFile = new File(filePath);

        /*if(ecgFile == null) {
            log("Opening File ... ");
            ecgFile = new File(filePath);
        }
        else {
            if(!ecgFile.getAbsolutePath().equals(filePath))
            {
                log("Opening File ... ");
                ecgFile = new File(filePath);
            }
            else {
                log("File Already Open! ... ");
            }
        }*/


        // TRY TO OPTIMIZE OPENING AND CLOSING OF FILES

        log("Looking for data in: " + ecgFile.getAbsolutePath());
        RandomAccessFile ecgRAF = null;
        try {
            ecgRAF = new RandomAccessFile(ecgFile, "r");
            ecgRAF.seek(startPos);
            long bytesRead = ecgRAF.read(ecgBytes);

            log("Bytes Read: " + bytesRead);

            ecgRAF.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ecgSamplesLen = 0;
            ecgBytesLen = 0;
        } catch (IOException e) {
            e.printStackTrace();
            ecgSamplesLen = 0;
            ecgBytesLen = 0;
        }

        long end = System.currentTimeMillis();
        log("Finished Reading " + ecgBytesLen + " bytes in " + (end-start) + " ms");



        log("Converting to channels ... ");
        long start2 = System.currentTimeMillis();
        short[] ch1 = new short[ecgSamplesLen];
        short[] ch2 = new short[ecgSamplesLen];

        for(int i = 0, j = 0; i < ecgBytesLen; i+=4, j++) {

            byte b1_ch1 = ecgBytes[i];
            byte b2_ch1 = ecgBytes[i + 1];

            short s_ch1 = ByteArrayUtils.toShort(b2_ch1, b1_ch1);
            ch1[j] = s_ch1;

            byte b1_ch2 = ecgBytes[i + 2];
            byte b2_ch2 = ecgBytes[i + 3];

            short s_ch2 = ByteArrayUtils.toShort(b2_ch2, b1_ch2);
            ch2[j] = s_ch2;

        }
        long end2 = System.currentTimeMillis();
        log("Finished Converting in " + (end2-start2) + " ms");

        return new EcgStrip(ch1, ch2);
    }

    public static String getEcgShortsJson(final long startSampleNum, final long endSampleNumExcl, final int activationCode, final String studyType) throws IOException {

        String jsonStr = new Gson().toJson(getEcgShorts(startSampleNum, endSampleNumExcl, activationCode, studyType));

        return jsonStr;
    }

    public static String getEcgShortsDownsampledJson(final long startSampleNum, final long endSampleNumExcl, final int activationCode, final String studyType, final int downsampleRate) throws IOException {

        EcgStrip ecgStrip = getEcgShorts(startSampleNum, endSampleNumExcl, activationCode, studyType);
        ecgStrip.downsampleStrip(downsampleRate);

        return new Gson().toJson(ecgStrip);
    }

    static class EcgStrip {

        public short[] ch1;
        public short ch1Min = 0;
        public short ch1Max = 0;

        public short[] ch2;
        public short ch2Min = 0;
        public short ch2Max = 0;


        public EcgStrip(final short[] ch1, final short[] ch2) {
            this.ch1 = ch1;
            this.ch2 = ch2;

            this.computeMinAndMax();
        }

        public void downsampleStrip(int downsampleRate) {

            short[] ch1Ds = new short[ch1.length / downsampleRate];
            short[] ch2Ds = new short[ch2.length / downsampleRate];

            // assuming ch1 and ch2 have same lengths
            for(int i = 0, j = 0; i < ch1.length; i+=downsampleRate, j++) {
                ch1Ds[j] = ch1[i];
                ch2Ds[j] = ch2[i];
            }

            ch1 = ch1Ds;
            ch2 = ch2Ds;

            this.computeMinAndMax();
        }

        private void computeMinAndMax() {
            this.ch1Min = StatsUtils.computeMin(ch1);
            this.ch2Min = StatsUtils.computeMin(ch2);

            this.ch1Max = StatsUtils.computeMax(ch1);
            this.ch2Max = StatsUtils.computeMax(ch2);
        }
    }


}
