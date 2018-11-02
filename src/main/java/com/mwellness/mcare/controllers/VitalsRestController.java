package com.mwellness.mcare.controllers;

/**
 * Created by dev01 on 12/30/17.
 */


import com.mwellness.mcare.AMainApp;
import com.mwellness.mcare.auth0.JwtTokenValidationUtility;
import com.mwellness.mcare.auth0.Role;
import com.mwellness.mcare.responses.StandardResponse;
import com.mwellness.mcare.utils.FromByteArray;
import com.mwellness.mcare.utils.GsonUtils;
import com.mwellness.mcare.vitals.Vitals;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

/**
 * Controller to manage patient vitals
 * Created by dev01 on 10/30/17.

 // If Patient Requesting: Use Token Details

 - REST POST    /vitals         if doctor requesting: input - patient_id
 - REST GET     /vitals/range   if doctor requesting: input - patient_id
 - REST GET     /vitals/latest  if doctor requesting: input - patient_id

 */


@RestController
public class VitalsRestController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static void log(String str) {
        AMainApp.log(VitalsRestController.class.getSimpleName() + ": " + str);
    }


    private static final JdbcTemplate jdbcTemplate = AMainApp.getJdbcTemplate(AMainApp.DB_SOURCE_QCARE_ECG);

    private static boolean isValidVitalType(final int vitalType) {
        if(Vitals.getStrOf(vitalType).equals("None")) {
            return false;
        }

        return true;
    }






    /**
     * Insert patient vitals
     * @param request
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @CrossOrigin
    @RequestMapping(value = "/vitals", method = RequestMethod.POST)
    public @ResponseBody
    StandardResponse insertVitals(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

        Role[] allowedRoles = new Role[]{Role.ROLE_PATIENT};
        if(!JwtTokenValidationUtility.isIdTokenValid(request)) {
            log("Invalid Token.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }
        String access = ApiAccessControl.areRolesAllowed(request, allowedRoles);
        if(access.contains("DENY")) {
            log("Role Access Denied.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }

        final long timestamp = System.currentTimeMillis();

        String patientId = JwtTokenValidationUtility.getUserId(request);
        log("by PatientId: " + patientId);

        String recordId = DigestUtils.sha1Hex(timestamp + "-" + patientId + "-" + UUID.randomUUID().toString());
        String vitalTypeStr = request.getParameter("vitalType") + "";
        if(vitalTypeStr.equals("null") || vitalTypeStr.equals("")) {
            return new StandardResponse(new IllegalArgumentException("Enter vitalType."));
        }
        int vitalType = Integer.parseInt(vitalTypeStr);
        if(!isValidVitalType(vitalType)) {
            return new StandardResponse(new IllegalArgumentException("Invalid Type: " + vitalType));
        }

        String v1 = request.getParameter("v1") + "";
        String v2 = request.getParameter("v2") + "";
        String v3 = request.getParameter("v3") + "";

        if(v1.equals("null") || v1.equals("")) {
            return new StandardResponse(new IllegalArgumentException("Enter v1."));
        }

        if(v2.equals("null")) {
            v2 = "";
        }

        if(v3.equals("null")) {
            v3 = "";
        }

        String timestampMsStr = request.getParameter("timestampMs") + "";

        if(timestampMsStr.equals("null") || timestampMsStr.equals("")) {
            return new StandardResponse(new IllegalArgumentException("Enter timestampMs."));
        }

        long recordTimestampMs = Long.parseLong(timestampMsStr);



        String sql = "insert into vitals" +
                        " values" +
                        " (?,?,?,?,?,?,?,?)";

        Object[] params = new Object[]{recordId, patientId, vitalType, v1, v2, v3, recordTimestampMs, timestamp};

        log("Inserting Vitals: " + Vitals.getStrOf(vitalType) + ", " + v1 + ", " + v2 + ", " + v3 + ", " + recordTimestampMs + " @ " + timestamp);
        int r = jdbcTemplate.update(sql, params);

        String response;
        if(r > 0) {
            response = "SUCCESS";
        }
        else {
            response = "FAILED";
        }

        return new StandardResponse(response);
    }








    /**
     * Get Vitals by Type for User Id for last 30 days in descending order ...
     * @param request
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @CrossOrigin
    @RequestMapping(value = "/vitals/last30days", method = RequestMethod.GET)
    public @ResponseBody
    StandardResponse getVitalsByType(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

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
        }

        String patientId = "";


        // only logged in patient can view their own vitals
        // ... should also work if patient is doctor also ...
        log("Getting patient Id");
        if (ApiAccessControl.tokenHasRole(idToken, Role.ROLE_PATIENT))
        {
            log("Role Patient!");
            patientId = JwtTokenValidationUtility.getUserId(idToken);
        }
        // all doctors can view vitals
        else if(ApiAccessControl.tokenHasRole(idToken, Role.ROLE_DOCTOR))
        {
            log("Role Doctor!");
            patientId = request.getParameter("patientId");
        }
        else {
            log("ghanta kuch mila!");
        }*/

        String patientId = request.getParameter("patientId");;

        if(patientId == null || patientId.equals("")) {
            log("Patient ID: unavailable");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }

        log("by PatientId: " + patientId);

        final long _30_DAYS_IN_MS =  (1000L * 86400L * 30L);
        final long timestampMinus30d = System.currentTimeMillis() - _30_DAYS_IN_MS;

        log("Getting all vitals after " + timestampMinus30d + " ms epoch");

        String sql = "select * from vitals where userid=? and timestampMs>? order by timestampMs desc";

        Object[] params = new Object[]{patientId, timestampMinus30d};

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);

        return new StandardResponse(GsonUtils.getInstance().toJson(rows));

    }







    /**
     * Get Vitals by Type for User Id for range of timestamp in descending order ...
     * @param request
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @CrossOrigin
    @RequestMapping(value = "/vitals/range", method = RequestMethod.GET)
    public @ResponseBody
    StandardResponse getVitalsRangeByType(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

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
        // only logged in patient can view their own vitals
        // ... should also work if patient is doctor also ...
        log("Getting patient Id");
        if (ApiAccessControl.tokenHasRole(idToken, Role.ROLE_PATIENT))
        {
            log("Role Patient!");
            patientId = JwtTokenValidationUtility.getUserId(idToken);
        }
        // all doctors can view vitals
        else if(ApiAccessControl.tokenHasRole(idToken, Role.ROLE_DOCTOR))
        {
            log("Role Doctor!");
            patientId = request.getParameter("patientId");
        }
        else {
            log("ghanta kuch mila!");
        }

        */
        patientId = request.getParameter("patientId") + "";


        if(patientId == null || patientId.equals("")) {
            log("Patient ID: unavailable");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }

        String startTimestampStr = request.getParameter("startTimestamp") + "";
        if(startTimestampStr.equals("null") || startTimestampStr.equals("")) {
            return new StandardResponse(new IllegalArgumentException("Provide a Start Timestamp"));
        }
        final long startTimestamp = Long.parseLong(startTimestampStr);

        String endTimestampStr = request.getParameter("endTimestamp") + "";
        if(endTimestampStr.equals("null") || endTimestampStr.equals("")) {
            return new StandardResponse(new IllegalArgumentException("Provide an End Timestamp"));
        }
        final long endTimestamp = Long.parseLong(endTimestampStr);



        String sql = "";
        Object[] params;
        if(endTimestamp == -1) {
            sql = "select * from vitals where userid=? and timestampMs>? order by timestampMs desc";
            params = new Object[]{patientId, startTimestamp};
        }
        else if(endTimestamp < startTimestamp) {
            return new StandardResponse(new IllegalArgumentException("End Timestamp must be greater than Start Timestamp"));
        }
        else {
            sql = "select * from vitals where userid=? and timestampMs>? and timestampMs<=? order by timestampMs desc";
            params = new Object[]{patientId, startTimestamp, endTimestamp};

        }

        log(sql + "/" + GsonUtils.getInstance().toJson(params));


        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);

        log(GsonUtils.getInstance(true).toJson(rows));

        return new StandardResponse(GsonUtils.getInstance().toJson(rows));

    }










    public static void generateSpo2Data() {

        final long startTimestamp = 1509494400000L;

        final long timestamp = System.currentTimeMillis();
        final String patientId = "google-oauth2|101010739141713793949";

        final int vitalType = 1;

        Random rand = new SecureRandom();

        for(int i = 1; i <= 240; i++) {

            int v1 = rand.nextInt((72 - 68) + 1) + 68;
            int v2 = rand.nextInt((100 - 95) + 1) + 95;

            long recordTimestampMs = startTimestamp + (i * 28800000L);
            final String recordId = DigestUtils.sha1Hex(recordTimestampMs + "-" + patientId + "-" + UUID.randomUUID().toString());


            String sql = "insert into vitals" +
                    " values" +
                    " (?,?,?,?,?,?,?,?)";

            Object[] params = new Object[]{recordId, patientId, vitalType, v1, v2, "", recordTimestampMs, timestamp};

            log("Inserting Vitals: " + Vitals.getStrOf(vitalType) + ", " + v1 + ", " + v2 + ", " + "" + ", " + recordTimestampMs + " @ " + timestamp);
            jdbcTemplate.update(sql, params);
        }




    }








    /**
     * Insert Photo
     * @param request
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @CrossOrigin
    @RequestMapping(value = "/vitals/photos", method = RequestMethod.POST)
    public @ResponseBody
    StandardResponse insertPatientPhoto(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

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


        String timestampMsStr = request.getParameter("timestampMs") + "";
        if(timestampMsStr.equals("null") || timestampMsStr.equals("")) {
            return new StandardResponse(new IllegalArgumentException("Enter timestampMs."));
        }
        long recordTimestampMs = Long.parseLong(timestampMsStr);


        String photoB64 = request.getParameter("photoB64") + "";
        if(photoB64.equals("null")) {
            return new StandardResponse(new IllegalArgumentException("Provide the photo base64."));
        }


        final long insertedAtMs = System.currentTimeMillis();

        String recordId = DigestUtils.sha1Hex(insertedAtMs + "-" + patientId + "-" + UUID.randomUUID().toString());

        String sql = "insert into patient_photos" +
                " values" +
                " (?,?,?,?,?)";


        Object[] params = new Object[]{recordId, patientId, photoB64, recordTimestampMs, insertedAtMs};

        //log("Inserting Ecg Strip: " + Vitals.getStrOf(vitalType) + ", " + v1 + ", " + v2 + ", " + v3 + ", " + recordTimestampMs + " @ " + timestamp);
        int r = jdbcTemplate.update(sql, params);

        String response;
        if(r > 0) {
            response = "SUCCESS";
        }
        else {
            response = "FAILED";
        }

        return new StandardResponse(response);

    }






    /**
     * Get Photo by Timestamp for User Id
     * @param request
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @CrossOrigin
    @RequestMapping(value = "/vitals/photos", method = RequestMethod.GET)
    public @ResponseBody
    StandardResponse getPatientPhoto(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

        String idToken = JwtTokenValidationUtility.getIdTokenFromRequest(request);
        Role[] allowedRoles = new Role[]{Role.ROLE_PATIENT, Role.ROLE_DOCTOR};
        if(!JwtTokenValidationUtility.isIdTokenValid(request)) {
            log("Invalid Token.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }
        String access = ApiAccessControl.areRolesAllowed(request, allowedRoles);
        if(access.contains("DENY")) {
            log("Role Access Denied.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }

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

        long photoTimestampMs = -1;
        String stripTimestampStr = request.getParameter("timestampMs") + "";
        if(!(stripTimestampStr.equals("null") || stripTimestampStr.equals(""))) {
            photoTimestampMs = Long.parseLong(stripTimestampStr);
        }

        String photoid = request.getParameter("photoid") + "";

        String sql = "";
        Object[] params;

        if(photoTimestampMs > -1 && (!patientId.equals("null") || !patientId.isEmpty())) {
            sql = "select * from patient_photos where userid=? and timestampMs=?";
            params = new Object[]{patientId, photoTimestampMs};
        }
        else if(!(photoid.equals("null") || photoid.isEmpty())) {
            sql = "select * from patient_photos where photoid=?";
            params = new Object[]{photoid};
        }
        else {
            return new StandardResponse(new IllegalArgumentException("Provide userid/timestamp or photoid"));
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);

        //log(GsonUtils.getInstance(true).toJson(rows));

        final String b64 = (String) rows.get(0).get("photob64");

        log("----------------------------------------");
        //log(GsonUtils.getInstance().toJson(ecgShorts));
        log(b64);
        log("----------------------------------------");

        return new StandardResponse(b64);

    }






    /**
     * List Photos by Timestamp for User Id
     * If timestamp range provided (startTimestampMs and endTimestampMs) then all items between them
     * If timestamp range not provided show all data in desc order of timestamp
     * @param request
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @CrossOrigin
    @RequestMapping(value = "/vitals/photos/list", method = RequestMethod.GET)
    public @ResponseBody
    StandardResponse listPatientPhotos(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

        String idToken = JwtTokenValidationUtility.getIdTokenFromRequest(request);
        Role[] allowedRoles = new Role[]{Role.ROLE_PATIENT, Role.ROLE_DOCTOR};
        if(!JwtTokenValidationUtility.isIdTokenValid(request)) {
            log("LES: " + "Invalid Token.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }
        String access = ApiAccessControl.areRolesAllowed(request, allowedRoles);
        if(access.contains("DENY")) {
            log("LES: " + "Role Access Denied.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }

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
            sql = "select photoid, userid, timestampMs,insertedAtMs from patient_photos where userid=? order by timestampMs desc";
            params = new Object[]{patientId};

        }
        else
        {
            sql = "select photoid, userid, timestampMs,insertedAtMs from patient_photos where userid=? and timestampMs>=? and timestampMs<=? order by timestampMs desc";
            params = new Object[]{patientId, startTimestamp, endTimestamp};
        }


        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);

        log("LES: " + GsonUtils.getInstance().toJson(rows));

        return new StandardResponse(GsonUtils.getInstance().toJson(rows));

    }

}
