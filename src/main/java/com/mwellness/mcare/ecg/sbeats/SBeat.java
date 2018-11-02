package com.mwellness.mcare.ecg.sbeats;


/**
 * Created by dev01 on 8/7/17.
 * This represents a beat as a byte.
 * Beat's byte structure:
 *
 *  XX      XXXX        X           X
 *  beat    rhythm      confirmed   deleted
 *
 *  beat    : N:1, S:2, V:3
 *  rhythm  : B:1, T:2, A:3, DOT:4, PAUSE:5 ...
 */
public class SBeat {

    private byte sBeatByte;

    public byte beatClass;
    public byte rhythmClass;
    private boolean confirmed;
    private boolean deleted;

    public static final byte BEATCLASS_N = 1; // normal
    public static final byte BEATCLASS_S = 2; // supra-ventricular
    public static final byte BEATCLASS_V = 3; // ventricular
    public static final byte BEATCLASS_O = 4; // other: eg: AFib
    public static final byte BEATCLASS_U = 5; // unknown




    public static final byte RHYTHM_VDot = 1;       // V
    public static final byte RHYTHM_V2g = 2;        // bigeminy
    public static final byte RHYTHM_V3g = 3;        // trigeminy
    public static final byte RHYTHM_VRun = 4;       // trigeminy
    public static final byte RHYTHM_VCpl = 10;       // V Couplet
    public static final byte RHYTHM_VTrp = 11;       // V Triplet

    public static final byte RHYTHM_NDot = 5;       // N
    public static final byte RHYTHM_NP = 6;         // PAUSE
    public static final byte RHYTHM_NBr = 7;        // Brady
    public static final byte RHYTHM_NTa = 8;        // Tachy


    public static final byte RHYTHM_SDot = 9;       // S
    public static final byte RHYTHM_STac = 12;      // S Tac
    public static final byte RHYTHM_SCpl = 16;       // S Couplet
    public static final byte RHYTHM_STrp = 17;       // S Triplet

    public static final byte RHYTHM_AF = 13;        // afib

    public static final byte RHYTHM_U = 14;        // UNKNOWN

    public static final byte RHYTHM_DOT = 15;        // UNKNOWN



    public SBeat(byte sbeatRep) {
        this.sBeatByte = sbeatRep;

        // get deleted
        byte deleted = getBit(this.sBeatByte, 0);
        this.deleted = (deleted == 1);

        // get confirmed
        byte confirmed = getBit(this.sBeatByte, 1);
        this.confirmed = (confirmed == 1);

        // get rhythm
        byte rb0 = getBit(this.sBeatByte, 2);
        byte rb1 = getBit(this.sBeatByte, 3);
        byte rb2 = getBit(this.sBeatByte, 4);
        byte rb3 = getBit(this.sBeatByte, 5);
        byte rb4 = getBit(this.sBeatByte, 6);
        byte rb5 = getBit(this.sBeatByte, 7);

        String rhythmBitStr = rb5+""+rb4+""+rb3+""+rb2+""+rb1+""+rb0+"";

        this.rhythmClass = bitStringToByte(rhythmBitStr);

        this.beatClass = getBeatClassFromRhythmClass(this.rhythmClass);
    }


    public SBeat(byte beatClassByteRep, byte rhythmClassByteRep, boolean deleted, boolean confirmed) {

        this.beatClass = beatClassByteRep;
        this.rhythmClass = rhythmClassByteRep;
        this.deleted = deleted;
        this.confirmed = confirmed;

        this.sBeatByte = computeByteRepresentation();
    }

    public SBeat(byte rhythmClassByteRep, boolean deleted, boolean confirmed) {

        this.beatClass = getBeatClassFromRhythmClass(rhythmClassByteRep);
        this.rhythmClass = rhythmClassByteRep;
        this.deleted = deleted;
        this.confirmed = confirmed;

        this.sBeatByte = computeByteRepresentation();
    }



    public byte computeByteRepresentation() {

        String s0 = deleted?"1":"0";
        String s1 = confirmed?"1":"0";

        //String beatStr = getBit(beatClass, 1) + "" + getBit(beatClass, 0);
        String rhyStr = getBit(rhythmClass, 5) + "" + getBit(rhythmClass, 4) + getBit(rhythmClass, 3) + "" + getBit(rhythmClass, 2) + "" + getBit(rhythmClass, 1) + "" + getBit(rhythmClass, 0);


        String fByte = /*beatStr + */rhyStr + s1 + s0;

        return bitStringToByte(fByte);
    }

    public void setSBeatByteRep(final byte b) {
        this.sBeatByte = b;
    }

    public byte getSBeatByteRep() {
        return sBeatByte;
    }

    private byte getBeatClassFromRhythmClass(byte rhythmClass) {

        switch (rhythmClass) {

            case RHYTHM_NDot:
            case RHYTHM_NBr:
            case RHYTHM_NTa:
            case RHYTHM_NP:
                return BEATCLASS_N;

            case RHYTHM_SDot:
            case RHYTHM_STac:
            case RHYTHM_SCpl:
            case RHYTHM_STrp:
                return BEATCLASS_S;

            case RHYTHM_VDot:
            case RHYTHM_V2g:
            case RHYTHM_V3g:
            case RHYTHM_VRun:
            case RHYTHM_VCpl:
            case RHYTHM_VTrp:
                return BEATCLASS_V;

            case RHYTHM_AF:
                return BEATCLASS_O;

            default:
                return BEATCLASS_U;
        }
    }

    public static String getBeatAnnotationStr(final byte beatClassByte) {
        switch (beatClassByte) {
            case BEATCLASS_N:
                return "N";
            case BEATCLASS_S:
                return "S";
            case BEATCLASS_V:
                return "V";
            case BEATCLASS_O:
                return "O";
            case BEATCLASS_U:
                return "?";
        }

        return "?";
    }

    public static byte getBeatByteFromStr(final String beatClass) {
        switch (beatClass) {
            case "N":
                return BEATCLASS_N;
            case "S":
                return BEATCLASS_S;
            case "V":
                return BEATCLASS_V;
            case "O":
                return BEATCLASS_O;
            case "?":
                return BEATCLASS_U;
            default:
                return BEATCLASS_U;
        }

    }

    public static byte getRhythmByteFromStr(final String rhythmStr) {

        switch (rhythmStr) {

            case ".":
                return RHYTHM_DOT;


            case "N":
                return RHYTHM_NDot;
            case "NBr":
                return RHYTHM_NBr;
            case "NTa":
                return RHYTHM_NTa;
            case "/":
                return RHYTHM_NP;


            case "S":
                return RHYTHM_SDot;
            case "SRun":
                return RHYTHM_STac;
            case "SCpl":
                return RHYTHM_SCpl;
            case "STrp":
                return RHYTHM_STrp;


            case "V":
                return RHYTHM_VDot;
            case "V2g":
                return RHYTHM_V2g;
            case "V3g":
                return RHYTHM_V3g;
            case "VCpl":
                return RHYTHM_VCpl;
            case "VTrp":
                return RHYTHM_VTrp;
            case "VRun":
                return RHYTHM_VRun;


            case "AF":
                return RHYTHM_AF;

            default:
                return RHYTHM_U;
        }


    }

    public static String getRhythmStr(final byte rhythmClass) {

        switch (rhythmClass) {

            case RHYTHM_DOT:
                return ".";

            case RHYTHM_NDot:
                return "N";
            case RHYTHM_NBr:
                return "NBr";
            case RHYTHM_NTa:
                return "NTa";
            case RHYTHM_NP:
                return "/";


            case RHYTHM_SDot:
                return "S";
            case RHYTHM_SCpl:
                return "SCpl";
            case RHYTHM_STrp:
                return "STrp";
            case RHYTHM_STac:
                return "SRun";


            case RHYTHM_VDot:
                return "V";
            case RHYTHM_V2g:
                return "V2g";
            case RHYTHM_V3g:
                return "V3g";
            case RHYTHM_VCpl:
                return "VCpl";
            case RHYTHM_VTrp:
                return "VTrp";
            case RHYTHM_VRun:
                return "VRun";

            case RHYTHM_AF:
                return "AF";
        }

        return "?";
    }

    public boolean getConfirmed() {
        return confirmed;
    }

    public boolean getDeleted() {
        return deleted;
    }


    public static byte getBit(final byte b, final int position)
    {
        return (byte) ((b >> position) & 1);
    }

    public static byte bitStringToByte(final String bitString) {
        return (byte) Integer.parseInt(bitString, 2);
    }


    @Override
    public String toString() {
        return "Beat: " + getBeatAnnotationStr(beatClass) + " ("+beatClass+") , Rhy: " + getRhythmStr(rhythmClass) + " ("+rhythmClass+"), C: " + getConfirmed() + ", D: " + getDeleted();
    }



}
