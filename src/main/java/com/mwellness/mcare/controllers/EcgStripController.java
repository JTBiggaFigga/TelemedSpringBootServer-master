package com.mwellness.mcare.controllers;

import com.google.gson.Gson;
import com.mwellness.mcare.AMainApp;
import com.mwellness.mcare.auth0.JwtTokenValidationUtility;
import com.mwellness.mcare.auth0.Role;
import com.mwellness.mcare.ecg.analysis.AFibAnalyzer;
import com.mwellness.mcare.ecg.analysis.ArrhythmiaAnalyzer;
import com.mwellness.mcare.ecg.analysis.EcgStripAnalysisResult;
import com.mwellness.mcare.ecg.analysis.RhythmArrhythmiaFinders;
import com.mwellness.mcare.ecg.sbeats.SBeat;
import com.mwellness.mcare.responses.StandardResponse;
import com.mwellness.mcare.utils.ArrayScaler;
import com.mwellness.mcare.utils.FromByteArray;
import com.mwellness.mcare.utils.GsonUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by dev01 on 4/14/17.
 */

@Controller
public class EcgStripController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final JdbcTemplate jdbcTemplate = AMainApp.getJdbcTemplate(AMainApp.DB_SOURCE_QCARE_ECG);


    private static void log(String str) {
        AMainApp.log(VitalsRestController.class.getSimpleName() + ": " + str);
    }


    /**
     * Insert ECG Strips (TODO: 120 seconds?) for Telemedicine
     * Receives a strip from the mobile @250 samples/sec
     * Assumption: Input strip is at 250 sample rate
     * @param request
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @CrossOrigin
    @RequestMapping(value = "/vitals/ecgstrip", method = RequestMethod.POST)
    public @ResponseBody
    StandardResponse insertEcgStrip(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

        Role[] allowedRoles = new Role[]{Role.ROLE_PATIENT};
        if (!JwtTokenValidationUtility.isIdTokenValid(request)) {
            log("Invalid Token.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }
        String access = ApiAccessControl.areRolesAllowed(request, allowedRoles);
        if (access.contains("DENY")) {
            log("Role Access Denied.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }


        String patientId = JwtTokenValidationUtility.getUserId(request);
        log("by PatientId: " + patientId);

        if(patientId == null || patientId.equals("INVALID_TOKEN")) {
            patientId = request.getParameter("patientId"); // 'google-oauth2|101010739141713793949'
        }


        String timestampMsStr = request.getParameter("timestampMs") + "";
        if(timestampMsStr.equals("null") || timestampMsStr.equals("")) {
            return new StandardResponse(new IllegalArgumentException("Enter timestampMs."));
        }
        long recordTimestampMs = Long.parseLong(timestampMsStr);


        String ecgStripB64 = request.getParameter("ecgstripB64") + "";
        if(ecgStripB64.equals("null") || ecgStripB64.equals("")) {
            return new StandardResponse(new IllegalArgumentException("Provide the Ecg Strip."));
        }

        EcgStripAnalysisResult result = analyzeStrip(ecgStripB64);

        final String analyticsJson = GsonUtils.getInstance().toJson(result);

        log("AFib: ================");
        log("AFib: " + analyticsJson);
        log("AFib: ================");


        final long insertedAtMs = System.currentTimeMillis();

        String recordId = DigestUtils.sha1Hex(insertedAtMs + "-" + patientId + "-" + UUID.randomUUID().toString());

        String sql = "insert into ecg_strips" +
                " values" +
                " (?,?,?,?,?,?)";


        Object[] params = new Object[]{recordId, patientId, ecgStripB64, recordTimestampMs, insertedAtMs, analyticsJson};

        //log("Inserting Ecg Strip: " + Vitals.getStrOf(vitalType) + ", " + v1 + ", " + v2 + ", " + v3 + ", " + recordTimestampMs + " @ " + timestamp);
        int r = jdbcTemplate.update(sql, params);

        String response;
        if(r > 0) {
            //response = analyticsJson;
            response = "SUCCESS";
        }
        else {
            response = "FAILED";
        }

        return new StandardResponse(response);
    }

    /**
     * Assumption input strip at 250 sample rate
     * @param ecgStripB64
     */
    private EcgStripAnalysisResult analyzeStrip(String ecgStripB64) {

        short[] ecgShortsAt250 = FromByteArray.toShortArray(Base64.getDecoder().decode(ecgStripB64));

        // upscaling to 360 for analytics
        short[] ecgShortsAt360 = ArrayScaler.scaleArray(ecgShortsAt250, ecgShortsAt250.length * 360/250);

        ArrayList<Integer> beatPositionList = new ArrayList<>();
        LinkedHashMap<Long, SBeat> beatMap = new LinkedHashMap<>();

        final int CHUNK_SIZE = 10 * 360;

        // split to 6 10-second chunks
        for(int i = 0; i < 6; i++) {

            int currStartSampleNumber = i * CHUNK_SIZE;
            int currEndSampleNumberExcl = currStartSampleNumber + CHUNK_SIZE;

            short[] currChunk = Arrays.copyOfRange(ecgShortsAt360, currStartSampleNumber, currEndSampleNumberExcl);

            String csv = ArrhythmiaAnalyzer.detectArrhythmias(currChunk, currChunk);
            String[] beats = csv.split("\n");
            if(beats.length == 0) {
                continue;
            }

            for (String beatStr : beats) {

                String[] ann = beatStr.split(",");

                int rPeakSampleNumber = Integer.parseInt(ann[0]);
                String annotation = ann[2];
                String rhythm = ann[3];

                if(rhythm.equals(".")) {
                    rhythm = annotation;
                }


                if (rPeakSampleNumber > 3600) {
                    continue;
                }

                long absPos = rPeakSampleNumber + currStartSampleNumber;
                beatPositionList.add(rPeakSampleNumber + currStartSampleNumber);

                SBeat sbeat = new SBeat(SBeat.getBeatByteFromStr(annotation), SBeat.getRhythmByteFromStr(rhythm), false, false);

                beatMap.put(absPos, sbeat);
            }

        }

        // TODO: Update beatmap w.r.t. 250 sample rate

        // ....
        ArrayList<Long> resampledBeatPositionList = new ArrayList<>();
        LinkedHashMap<Long, SBeat> resampledBeatMap = new LinkedHashMap<>();

        for(long i: beatPositionList) {
            long newSampleNumber = (int) ArrhythmiaAnalyzer.getDownsampledSampleNumber(i);
            resampledBeatPositionList.add(newSampleNumber);
            resampledBeatMap.put(newSampleNumber, beatMap.get(i));
        }

        LinkedHashMap<Long, SBeat> reassignedBeats = new LinkedHashMap<>();

        boolean countRhythms = true;

        if(countRhythms) {

            // V beats
            reassignedBeats = RhythmArrhythmiaFinders.findAndSetBigeminy(resampledBeatMap);
            reassignedBeats = RhythmArrhythmiaFinders.findAndSetTrigeminy(reassignedBeats);
            reassignedBeats = RhythmArrhythmiaFinders.findAndSetVCouplet(reassignedBeats);
            reassignedBeats = RhythmArrhythmiaFinders.findAndSetVTriplet(reassignedBeats);
            reassignedBeats = RhythmArrhythmiaFinders.findAndSetRuns(SBeat.BEATCLASS_V, SBeat.RHYTHM_VRun, reassignedBeats);


            // S beats
            reassignedBeats = RhythmArrhythmiaFinders.findAndSetSCouplet(reassignedBeats);
            reassignedBeats = RhythmArrhythmiaFinders.findAndSetSTriplet(reassignedBeats);
            reassignedBeats = RhythmArrhythmiaFinders.findAndSetRuns(SBeat.BEATCLASS_S, SBeat.RHYTHM_STac, reassignedBeats);

        }

        long[] resampledBeatPositionArr = resampledBeatPositionList.stream().mapToLong(l -> l).toArray();

        log("Beat Position: " + GsonUtils.getInstance().toJson(resampledBeatPositionArr));

        // run afib
        boolean aFibBool = AFibAnalyzer.detectAFib(resampledBeatPositionArr);


        return new EcgStripAnalysisResult(aFibBool, reassignedBeats);
    }


    /**
     * Get Ecg Strip by Timestamp for User Id
     * @param request
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @CrossOrigin
    @RequestMapping(value = "/vitals/ecgstrip", method = RequestMethod.GET)
    public @ResponseBody
    StandardResponse getEcgStrip(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

        /*String idToken = JwtTokenValidationUtility.getIdTokenFromRequest(request);
        Role[] allowedRoles = new Role[]{Role.ROLE_PATIENT, Role.ROLE_DOCTOR};
        if(!JwtTokenValidationUtility.isIdTokenValid(request)) {
            log("Invalid Token.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }
        String access = ApiAccessControl.areRolesAllowed(request, allowedRoles);
        if(access.contains("DENY")) {
            log("Role Access Denied.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }*/

        String patientId = "";

        /*
        // all doctors can view vitals
        if(ApiAccessControl.tokenHasRole(idToken, Role.ROLE_DOCTOR)) {
            patientId = request.getParameter("patientId") + "";
        }
        // only current patient can access
        else if (ApiAccessControl.tokenHasRole(idToken, Role.ROLE_PATIENT))
        {
            patientId = JwtTokenValidationUtility.getUserId(idToken);
        }

        patientId = request.getParameter("patientId") + "";
        */

        log("by PatientId: " + patientId);

        long stripTimestampMs = -1;
        String stripTimestampStr = request.getParameter("timestampMs") + "";
        if(!(stripTimestampStr.equals("null") || stripTimestampStr.equals(""))) {
            stripTimestampMs = Long.parseLong(stripTimestampStr);
        }

        String stripid = request.getParameter("stripid") + "";

        String sql = "";
        Object[] params;

        if(stripTimestampMs > -1 && (!patientId.equals("null") || !patientId.isEmpty())) {
            sql = "select * from ecg_strips where userid=? and timestampMs=?";
            params = new Object[]{patientId, stripTimestampMs};
        }
        else if(!(stripid.equals("null") || stripid.isEmpty())) {
            sql = "select * from ecg_strips where stripid=?";
            params = new Object[]{stripid};
        }
        else {
            return new StandardResponse(new IllegalArgumentException("Provide userid/timestamp or stripid"));
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);

        //log(GsonUtils.getInstance(true).toJson(rows));

        final String b64 = (String) rows.get(0).get("stripb64");
        short[] ecgShorts = FromByteArray.toShortArray(Base64.getDecoder().decode(b64));

        final String analyticsJson = (String) rows.get(0).get("analyticsJson");
        final long timestampMs = (long) rows.get(0).get("timestampMs");


        EcgStripRow ecgStripRow = new EcgStripRow(ecgShorts, analyticsJson, timestampMs);

        log("----------------------------------------");
        log(GsonUtils.getInstance().toJson(ecgStripRow));
        log("----------------------------------------");

        return new StandardResponse(GsonUtils.getInstance().toJson(ecgStripRow));
    }


    class EcgStripRow {
        short[] ecgSamples;
        String analyticsJson;
        long timestampMs;
        public EcgStripRow(short[] ecgSamples, String analyticsJson, long timestampMs) {
            this.ecgSamples = ecgSamples;
            this.analyticsJson = analyticsJson;
            this.timestampMs = timestampMs;
        }
    }




    /**
     * List Ecg Strip by Timestamp for User Id
     * If timestamp range provided (startTimestampMs and endTimestampMs) then all items between them
     * If timestamp range not provided show all data in desc order of timestamp
     * @param request
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @CrossOrigin
    @RequestMapping(value = "/vitals/ecgstrip/list", method = RequestMethod.GET)
    public @ResponseBody
    StandardResponse listEcgStrisp(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

        /*String idToken = JwtTokenValidationUtility.getIdTokenFromRequest(request);
        Role[] allowedRoles = new Role[]{Role.ROLE_PATIENT, Role.ROLE_DOCTOR};
        if(!JwtTokenValidationUtility.isIdTokenValid(request)) {
            log("LES: " + "Invalid Token.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }
        String access = ApiAccessControl.areRolesAllowed(request, allowedRoles);
        if(access.contains("DENY")) {
            log("LES: " + "Role Access Denied.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }*/

        String patientId = request.getParameter("patientId") + "";


        /*
        // only logged in patient can view their own vitals
        // ... should also work if patient is doctor also ...
        log("LES: " + "Getting patient Id");
        if (ApiAccessControl.tokenHasRole(idToken, Role.ROLE_PATIENT))
        {
            log("LES: " + "Role Patient!");
            patientId = JwtTokenValidationUtility.getUserId(idToken);
        }
        // all doctors can view vitals
        else if(ApiAccessControl.tokenHasRole(idToken, Role.ROLE_DOCTOR))
        {
            log("LES: " + "Role Doctor!");
            patientId = request.getParameter("patientId");
        }
        else {
            log("LES: " + "ghanta kuch mila!");
        }
        */


        if(patientId == null || patientId.equals("")) {
            log("LES: " + "Patient ID: unavailable");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }

        log("LES: " + "by PatientId: " + patientId);

        long startTimestamp = -1;
        String startTimestampStr = request.getParameter("startTimestamp") + "";
        if(!(startTimestampStr.equals("null") || startTimestampStr.equals(""))) {
            startTimestamp = Long.parseLong(startTimestampStr);
        }

        long endTimestamp = -1;
        String endTimestampStr = request.getParameter("endTimestamp") + "";
        if(!(endTimestampStr.equals("null") || endTimestampStr.equals(""))) {
            endTimestamp = Long.parseLong(endTimestampStr);
        }



        String sql = "";
        Object[] params; // = new Object[]{};

        if(startTimestamp == -1 || endTimestamp == -1 || (startTimestamp == -1 && endTimestamp == -1) ) // get last 10 in desc order
        {
            sql = "select stripid, userid, timestampMs,insertedAtMs from ecg_strips where userid=? order by timestampMs desc";
            params = new Object[]{patientId};

        }
        else
        {
            sql = "select stripid, userid, timestampMs,insertedAtMs from ecg_strips where userid=? and timestampMs>=? and timestampMs<=? order by timestampMs desc";
            params = new Object[]{patientId, startTimestamp, endTimestamp};
        }


        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);

        log("LES: " + GsonUtils.getInstance().toJson(rows));

        return new StandardResponse(GsonUtils.getInstance().toJson(rows));

    }










    @RequestMapping("/ecgstrip/view")
    protected String viewEcgStrip(final Map<String, Object> model, final HttpServletRequest req, final HttpServletResponse response) throws IOException {

        logger.info("");
        logger.info("====================================================================");
        logger.info("");

        logger.info(new Gson().toJson(req.getParameterMap()));
        //return ""; //new Gson().toJson(req.getParameterMap());

        String code = req.getParameter("code");

        model.put("code", code);

        return "mobile_code_receiver";
    }

}
