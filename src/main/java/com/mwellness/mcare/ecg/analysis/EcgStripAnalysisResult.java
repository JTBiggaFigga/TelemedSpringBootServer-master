package com.mwellness.mcare.ecg.analysis;

import com.mwellness.mcare.ecg.sbeats.SBeat;

import java.util.HashMap;

/**
 * Created by qubit on 3/26/18.
 */
public class EcgStripAnalysisResult {
    boolean aFibBool;
    HashMap<Long, SBeat> beatMap;


    public EcgStripAnalysisResult(final boolean aFibBool, HashMap<Long, SBeat> beatMap) {
        this.aFibBool = aFibBool;
        this.beatMap = beatMap;
    }
}
