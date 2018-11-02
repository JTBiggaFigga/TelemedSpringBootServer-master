package com.mwellness.mcare.ecg.sbeats;

import com.mwellness.mcare.AMainApp;
import com.mwellness.mcare.ecg.ecgsignal.EcgFileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by dev01 on 8/7/17.
 */
public class SBeatFileSaver {

    private static final Logger logger = LoggerFactory.getLogger(SBeatFileSaver.class);
    public static final String ECG_STORAGE_PATH = EcgFileStorage.ECG_STORAGE_PATH;

    private static void log(String str) {
        AMainApp.log(SBeatFileSaver.class.getSimpleName() + ": " + str);
    }

    public static synchronized void saveBeatToBeatFile(final String sessionId, final long rPeakSampleNumber, final SBeat sBeat) throws IOException {

        RandomAccessFile fileStore = new RandomAccessFile(new File(ECG_STORAGE_PATH + "ecg-" +  sessionId+"-beats.dat"), "rw");

        long byteOffset = Long.BYTES + rPeakSampleNumber;

        log("Writing beat for " + rPeakSampleNumber + " with " + sBeat.toString());

        fileStore.seek(byteOffset);

        fileStore.write(sBeat.getSBeatByteRep());

        fileStore.close();

    }

    public static synchronized void saveBeatsMapToBeatFile(final String sessionId, LinkedHashMap<Long, SBeat> beatsMap) throws IOException {

        RandomAccessFile fileStore = new RandomAccessFile(new File(ECG_STORAGE_PATH + "ecg-" +  sessionId+"-beats.dat"), "rw");

        for(Map.Entry<Long, SBeat> entry: beatsMap.entrySet()) {

            long byteOffset = Long.BYTES + entry.getKey();
            fileStore.seek(byteOffset);

            // updating byte representation of beat info ...
            entry.getValue().setSBeatByteRep(entry.getValue().computeByteRepresentation());

            // saving ...
            byte savingByte = entry.getValue().getSBeatByteRep();
            fileStore.write(savingByte);

            log("Wrote beat for " + entry.getKey() + " with " + entry.getValue().toString() + " ("+savingByte+")");
        }

        fileStore.close();


    }

}
