package com.mwellness.mcare.ecg.analysis;

import com.mwellness.mcare.AMainApp;
import com.mwellness.mcare.ecg.sbeats.SBeat;
import org.apache.commons.lang.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by dev01 on 8/20/17.
 */
public class RhythmArrhythmiaFinders {

    //public static final String[] RHYTHM_PATTERN_BIGEMINY_NVNVTr = new String[]{"N","V","N","VTr"};
    public static final String[] RHYTHM_PATTERN_BIGEMINY_NVNV = new String[]{"N","V","N","V"};

    public static final String[] RHYTHM_PATTERN_TRIGEMINY_VNNV = new String[]{"V","N","N","V"};
    //public static final String[] RHYTHM_PATTERN_TRIGEMINY_VNNVBi = new String[]{"V","N","N","VBi"};
    //public static final String[] RHYTHM_PATTERN_TRIGEMINY_VBiNNV = new String[]{"VBi","N","N","V"};
    //public static final String[] RHYTHM_PATTERN_TRIGEMINY_VBiNNVBi = new String[]{"VBi","N","N","VBi"};

    public static final String[] RHYTHM_PATTERN_V_COUPLET = new String[]{"V","V"};
    public static final String[] RHYTHM_PATTERN_V_TRIPLET = new String[]{"V","V", "V"};

    public static final String[] RHYTHM_PATTERN_S_COUPLET = new String[]{"S","S"};
    public static final String[] RHYTHM_PATTERN_S_TRIPLET = new String[]{"S","S", "S"};



    private static void log(String str) {
        AMainApp.log(RhythmArrhythmiaFinders.class + ": " + str);
    }

    public static String getBeatClassListString(final LinkedHashMap<Long, SBeat> sBeatsLinkedHashMap, boolean noDelimiter) {
        StringBuilder beatString = new StringBuilder();

        for(Map.Entry<Long, SBeat> sBeatEntry: sBeatsLinkedHashMap.entrySet()) {

            SBeat sBeat = sBeatEntry.getValue();

            beatString.append(SBeat.getBeatAnnotationStr(sBeat.beatClass)).append(noDelimiter?"":", ");
        }

        return beatString.toString();
    }

    public static String[] getBeatClassListAsStringArr(final LinkedHashMap<Long, SBeat> sBeatsLinkedHashMap) {

        String[] beatClassStrArr = new String[sBeatsLinkedHashMap.size()];

        int i = 0;
        for(Map.Entry<Long, SBeat> sBeatEntry: sBeatsLinkedHashMap.entrySet()) {

            SBeat sBeat = sBeatEntry.getValue();

            beatClassStrArr[i] = (SBeat.getBeatAnnotationStr(sBeat.beatClass));//(SBeat.getRhythmStr(sBeat.rhythmClass));
            i++;
        }

        return beatClassStrArr;
    }

    public static String[] getRhythmStringPatternForPrint(final LinkedHashMap<Long, SBeat> sBeatsLinkedHashMap) {

        String[] rhythmStrArr = new String[sBeatsLinkedHashMap.size()];

        int i = 0;
        for(Map.Entry<Long, SBeat> sBeatEntry: sBeatsLinkedHashMap.entrySet()) {

            SBeat sBeat = sBeatEntry.getValue();

            rhythmStrArr[i] = (SBeat.getRhythmStr(sBeat.rhythmClass)) + " (" + i + ":" + (SBeat.getBeatAnnotationStr(sBeat.beatClass)) + ")";//(SBeat.getRhythmStr(sBeat.rhythmClass));
            i++;
        }

        return rhythmStrArr;
    }


    public static long[] getSampleNumbersArray(final LinkedHashMap<Long, SBeat> sBeatsLinkedHashMap) {
        return sBeatsLinkedHashMap.keySet().stream().mapToLong(l -> l).toArray(); //new long[sBeatsLinkedHashMap.size()];
    }


    public static LinkedHashMap<Long, SBeat> reassignByRhythmPatternArr(final String[] beatPatternStrArr, byte newBeatAssignment, final LinkedHashMap<Long, SBeat> sBeatsLinkedHashMap, boolean overlappingFinds) {


        int i = 0;
        long[] sampleNumbers = getSampleNumbersArray(sBeatsLinkedHashMap);
        String[] inputBeatStrArr = getBeatClassListAsStringArr(sBeatsLinkedHashMap);//getRhythmStringPatternForPrint(sBeatsLinkedHashMap);

        for(Map.Entry<Long, SBeat> sBeatEntry: sBeatsLinkedHashMap.entrySet()) {
            sampleNumbers[i] = sBeatEntry.getKey();
            i++;
        }

        int lastIndexFound = 0;

        while(lastIndexFound != -1) {

            lastIndexFound = indexOfSubArray(beatPatternStrArr, inputBeatStrArr, lastIndexFound); //ArrayUtils.indexOf(beatPatternStrArr, inputBeatStrArr, lastIndexFound);

            if(lastIndexFound != -1){

                log("Found " + String.join(", ",beatPatternStrArr) + " at " + lastIndexFound);

                for(int j = 0; j < beatPatternStrArr.length; j++) {
                    long key = sampleNumbers[lastIndexFound + j];
                    SBeat oldSBeat = sBeatsLinkedHashMap.get(key);
                    sBeatsLinkedHashMap.put(key, new SBeat(newBeatAssignment, oldSBeat.getDeleted(), oldSBeat.getConfirmed()));
                }

                lastIndexFound ++; //= beatPatternStrArr.length;
            }
        }



        return sBeatsLinkedHashMap;
    }

    private static int indexOfSubArray(final String[] subArray, final String[] inputArray, final int startIndex) {

        String[] newInputArray = (String[]) ArrayUtils.subarray(inputArray, startIndex, inputArray.length - 1);

        int foundInputSubArrayIndex = Collections.indexOfSubList(Arrays.asList(newInputArray), Arrays.asList(subArray));
        //log("Found input subarray: " + foundInputSubArrayIndex + " with startindex: " + startIndex);

        if(foundInputSubArrayIndex == -1) {
            return -1;
        }
        return foundInputSubArrayIndex + startIndex;
    }

    /// FINDERS


    public static LinkedHashMap<Long, SBeat> findAndSetBigeminy(final LinkedHashMap<Long, SBeat> sBeatsLinkedHashMap) {
        LinkedHashMap<Long, SBeat> output = reassignByRhythmPatternArr(RHYTHM_PATTERN_BIGEMINY_NVNV, SBeat.RHYTHM_V2g, sBeatsLinkedHashMap, true);
        return output;
    }

    public static LinkedHashMap<Long, SBeat> findAndSetTrigeminy(final LinkedHashMap<Long, SBeat> sBeatsLinkedHashMap) {
        LinkedHashMap<Long, SBeat> output = reassignByRhythmPatternArr(RHYTHM_PATTERN_TRIGEMINY_VNNV, SBeat.RHYTHM_V3g, sBeatsLinkedHashMap, true);
        //output = reassignByRhythmPatternArr(RHYTHM_PATTERN_TRIGEMINY_VNNVBi, SBeat.RHYTHM_V3g, output, true);
        //output = reassignByRhythmPatternArr(RHYTHM_PATTERN_TRIGEMINY_VBiNNV, SBeat.RHYTHM_V3g, output, true);
        //output = reassignByRhythmPatternArr(RHYTHM_PATTERN_TRIGEMINY_VBiNNVBi, SBeat.RHYTHM_V3g, output, true);

        return output;
    }


    public static LinkedHashMap<Long, SBeat> findAndSetVCouplet(final LinkedHashMap<Long, SBeat> sBeatsLinkedHashMap) {
        LinkedHashMap<Long, SBeat> output = reassignByRhythmPatternArr(RHYTHM_PATTERN_V_COUPLET, SBeat.RHYTHM_VCpl, sBeatsLinkedHashMap, false);
        return output;
    }

    public static LinkedHashMap<Long, SBeat> findAndSetVTriplet(final LinkedHashMap<Long, SBeat> sBeatsLinkedHashMap) {
        LinkedHashMap<Long, SBeat> output = reassignByRhythmPatternArr(RHYTHM_PATTERN_V_TRIPLET, SBeat.RHYTHM_VTrp, sBeatsLinkedHashMap, false);
        return output;
    }

    public static LinkedHashMap<Long, SBeat> findAndSetSCouplet(final LinkedHashMap<Long, SBeat> sBeatsLinkedHashMap) {
        LinkedHashMap<Long, SBeat> output = reassignByRhythmPatternArr(RHYTHM_PATTERN_S_COUPLET, SBeat.RHYTHM_SCpl, sBeatsLinkedHashMap, false);
        return output;
    }

    public static LinkedHashMap<Long, SBeat> findAndSetSTriplet(final LinkedHashMap<Long, SBeat> sBeatsLinkedHashMap) {
        LinkedHashMap<Long, SBeat> output = reassignByRhythmPatternArr(RHYTHM_PATTERN_S_TRIPLET, SBeat.RHYTHM_STrp, sBeatsLinkedHashMap, false);
        return output;
    }



    public static LinkedHashMap<Long, SBeat> findAndSetVRuns_old(final LinkedHashMap<Long, SBeat> sBeatsLinkedHashMap) {

        final String lookingFor = SBeat.getRhythmStr(SBeat.RHYTHM_VRun);
        long[] sampleNumbers = getSampleNumbersArray(sBeatsLinkedHashMap);

        LinkedHashMap<Long, SBeat> outputHashmap = new LinkedHashMap<>(sBeatsLinkedHashMap);

        String[] beatListArr = getBeatClassListAsStringArr(sBeatsLinkedHashMap);

        String prevBeatStr = "";

        int runStartedAt = -1;
        int runEndedAt = -1;

        for(int i = 0; i < beatListArr.length; i++) {

            String currBeatStr = beatListArr[i];

            if(currBeatStr.equals(lookingFor) && currBeatStr.equals(prevBeatStr)) {
                if(runStartedAt == -1) {
                    runStartedAt = i - 1;
                }
                runEndedAt = i;
            }
            else {
                if((runEndedAt - runStartedAt + 1) >= 3) // subtraction of indices - 1
                {
                    // capture the run
                    log("Run from: " + runStartedAt + " to " + runEndedAt);
                    for(int j = runStartedAt; j <= runEndedAt; j++) {
                        SBeat oldSBeat = outputHashmap.get(sampleNumbers[j]);
                        outputHashmap.put(sampleNumbers[j], new SBeat(SBeat.RHYTHM_VRun, oldSBeat.getDeleted(), oldSBeat.getConfirmed()));
                    }
                }

                // reset runStartedAt and runEndedAt
                runStartedAt = -1;
                runEndedAt = -1;
            }

            prevBeatStr = currBeatStr;
        }

        return outputHashmap;
    }


    public static LinkedHashMap<Long, SBeat> findAndSetRuns(byte beatByteLookingFor, byte rhythmByteReplacement, final LinkedHashMap<Long, SBeat> sBeatsLinkedHashMap) {
        final byte BEAT_LOOKING_FOR = beatByteLookingFor;
        final byte RHYTHM_TO_SET_TO = rhythmByteReplacement;

        long startBeatSampleNumber = -1;
        long endBeatSampleNumber = -1;

        int countFavorableOutcomes = 0;

        LinkedHashMap<Long, SBeat> outputHashMap = (LinkedHashMap<Long, SBeat>) sBeatsLinkedHashMap.clone();

        for(Map.Entry<Long, SBeat> entry: sBeatsLinkedHashMap.entrySet()) {

            //log(" ");
            //log("Checking: " + entry.getKey() + " -> " + entry.getValue().toString());

            if(entry.getValue().beatClass == BEAT_LOOKING_FOR) {
                if(startBeatSampleNumber == -1) {
                    startBeatSampleNumber = entry.getKey();
                }

                endBeatSampleNumber = entry.getKey();
                countFavorableOutcomes++;
            }
            else {

                if(countFavorableOutcomes >= 4) {
                    // replace from start to end
                    boolean currentlyReplacing = false;
                    for(Map.Entry<Long, SBeat> outputEntry: outputHashMap.entrySet()) {
                        if(outputEntry.getKey() == startBeatSampleNumber) {
                            currentlyReplacing = true;
                        }

                        if(currentlyReplacing) {
                            // replace
                            outputEntry.getValue().rhythmClass = RHYTHM_TO_SET_TO;
                        }

                        if(outputEntry.getKey() == endBeatSampleNumber) {
                            currentlyReplacing = false;
                        }
                    }
                }

                startBeatSampleNumber = -1;
                endBeatSampleNumber = -1;
                countFavorableOutcomes = 0;
            }

            //log("StartSampleNumber: " + startBeatSampleNumber + ", EndSampleNumber: " + endBeatSampleNumber);
        }

        // in case last item is also V
        if(countFavorableOutcomes >= 4) {
            // replace from start to end
            boolean currentlyReplacing = false;
            for(Map.Entry<Long, SBeat> outputEntry: outputHashMap.entrySet()) {
                if(outputEntry.getKey() == startBeatSampleNumber) {
                    currentlyReplacing = true;
                }

                if(currentlyReplacing) {
                    // replace
                    outputEntry.getValue().rhythmClass = RHYTHM_TO_SET_TO;
                }

                if(outputEntry.getKey() == endBeatSampleNumber) {
                    currentlyReplacing = false;
                }
            }
        }

        return outputHashMap;
    }


    public static LinkedHashMap<Long, SBeat> findAndSetVRuns_old2(final LinkedHashMap<Long, SBeat> sBeatsLinkedHashMap) {

        final byte BEAT_LOOKING_FOR = SBeat.BEATCLASS_V;
        final byte RHYTHM_TO_SET_TO = SBeat.RHYTHM_VRun;

        long startBeatSampleNumber = -1;
        long endBeatSampleNumber = -1;

        byte currBeatByteRep = -1;
        byte prevBeatByteRep = -1;


        int countFavorableOutcomes = 0;

        LinkedHashMap<Long, SBeat> output = (LinkedHashMap<Long, SBeat>) sBeatsLinkedHashMap.clone();

        for(Map.Entry<Long, SBeat> entry: sBeatsLinkedHashMap.entrySet()) {

            log("Processing: " + entry.getKey() + ": " + entry.getValue().toString());

            currBeatByteRep = entry.getValue().beatClass;

            if(currBeatByteRep == BEAT_LOOKING_FOR) {
                if(currBeatByteRep != prevBeatByteRep) {
                    startBeatSampleNumber = entry.getKey();
                }

                countFavorableOutcomes++;

                if(countFavorableOutcomes > 0) {
                    endBeatSampleNumber = entry.getKey();
                }
            }
            else {
                if(prevBeatByteRep == BEAT_LOOKING_FOR) {

                    if(countFavorableOutcomes >= 4) {

                        log("Replacing from: " + startBeatSampleNumber + " to " + endBeatSampleNumber);

                        boolean replacementBegun = false;
                        for(Map.Entry<Long, SBeat> outEntry: output.entrySet()) {

                            if(outEntry.getKey() == startBeatSampleNumber) {
                                replacementBegun = true;
                            }

                            if(outEntry.getKey() == endBeatSampleNumber) {
                                replacementBegun = false;

                                startBeatSampleNumber = -1;
                                endBeatSampleNumber = -1;

                                countFavorableOutcomes = 0;
                            }

                            if(replacementBegun) {

                                log(" ");
                                log("Changing from : " + outEntry.getKey() + ": " + output.get(outEntry.getKey()).toString());
                                output.get(outEntry.getKey()).rhythmClass = RHYTHM_TO_SET_TO;
                                log("...        to : " + outEntry.getKey() + ": " + output.get(outEntry.getKey()).toString());
                            }
                        }

                    }
                    else {
                        startBeatSampleNumber = -1;
                        endBeatSampleNumber = -1;
                    }
                }
            }


            prevBeatByteRep = currBeatByteRep;
        }

        if(prevBeatByteRep == BEAT_LOOKING_FOR) {

            if(countFavorableOutcomes >= 4) {

                log("Replacing from: " + startBeatSampleNumber + " to " + endBeatSampleNumber);

                boolean replacementBegun = false;
                for(Map.Entry<Long, SBeat> outEntry: output.entrySet()) {

                    if(outEntry.getKey() == startBeatSampleNumber) {
                        replacementBegun = true;
                    }


                    if(replacementBegun) {

                        log(" ");
                        log("Changing from : " + outEntry.getKey() + ": " + output.get(outEntry.getKey()).toString());
                        output.get(outEntry.getKey()).rhythmClass = RHYTHM_TO_SET_TO;
                        log("...        to : " + outEntry.getKey() + ": " + output.get(outEntry.getKey()).toString());
                    }


                    if(outEntry.getKey() == endBeatSampleNumber) {
                        replacementBegun = false;

                        startBeatSampleNumber = -1;
                        endBeatSampleNumber = -1;

                        countFavorableOutcomes = 0;
                    }
                }

            }
            else {
                startBeatSampleNumber = -1;
                endBeatSampleNumber = -1;
            }
        }

        return output;
    }



    public static LinkedHashMap<Long, SBeat> findAndSetSTach(final LinkedHashMap<Long, SBeat> sBeatsLinkedHashMap) {

        final String lookingFor = SBeat.getRhythmStr(SBeat.RHYTHM_STac);
        long[] sampleNumbers = getSampleNumbersArray(sBeatsLinkedHashMap);

        LinkedHashMap<Long, SBeat> outputHashmap = new LinkedHashMap<>(sBeatsLinkedHashMap);

        String[] beatListArr = getBeatClassListAsStringArr(sBeatsLinkedHashMap);

        String prevBeatStr = "";

        int runStartedAt = -1;
        int runEndedAt = -1;

        for(int i = 0; i < beatListArr.length; i++) {

            String currBeatStr = beatListArr[i];

            if(currBeatStr.equals(lookingFor) && currBeatStr.equals(prevBeatStr)) {
                if(runStartedAt == -1) {
                    runStartedAt = i - 1;
                }
                runEndedAt = i;
            }
            else {
                if((runEndedAt - runStartedAt + 1) > 3) // subtraction of indices - 1
                {
                    // capture the run
                    log("SRun from: " + runStartedAt + " to " + runEndedAt);
                    for(int j = runStartedAt; j <= runEndedAt; j++) {
                        SBeat oldSBeat = outputHashmap.get(sampleNumbers[j]);
                        outputHashmap.put(sampleNumbers[j], new SBeat(SBeat.RHYTHM_STac, oldSBeat.getDeleted(), oldSBeat.getConfirmed()));
                    }
                }

                // reset runStartedAt and runEndedAt
                runStartedAt = -1;
                runEndedAt = -1;
            }

            prevBeatStr = currBeatStr;
        }

        return outputHashmap;
    }



}
