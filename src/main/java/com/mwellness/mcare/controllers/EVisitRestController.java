package com.mwellness.mcare.controllers;

import com.auth0.*;
import com.google.gson.Gson;
import com.opentok.Session;
import com.opentok.exception.OpenTokException;
import com.mwellness.mcare.AMainApp;
import com.mwellness.mcare.auth0.Auth0UserDirectoryProxy;
import com.mwellness.mcare.auth0.JwtTokenValidationUtility;
import com.mwellness.mcare.auth0.Role;
import com.mwellness.mcare.responses.StandardResponse;
import com.mwellness.mcare.tokbox.TokboxProxyGateway;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.mwellness.mcare.controllers.EVisitRestController.DOC_VCLINIC_STATE.OPEN;

/**
 * Controller to manage evisits between doctors and patients
 * Created by dev01 on 12/12/17.

 // Queueing Calls

 - REST GET  /patient/location                      (input: patient_id)

 - REST GET  /vclinic/doctor/state                  (input: doc_id)
 - REST POST /vclinic/doctor/state                  (input: doc_id, state: OPEN | CLOSED | PAUSED)
 - REST POST /vclinic/doctor/alive_pulse            (input: doc_id)

 - REST GET  /evisit/patient/online_doctors         (input: pat_id?)
 - REST POST /evisit/patient/enqueue                (input: doc_id)
 - REST GET  /evisit/patient/position               (input: evisit_id)
 - REST GET  /evisit/patient/has_doctor_let_me_in   (input: evisit_id)
 - REST GET  /evisit/patient/leave                  (input: evisit_id)


 - REST GET  /evisit/doctor/patient_queue           (input: doc_id)
 - REST POST /evisit/doctor/notes                   (input: evisit_id, note_str, public_bool)
 - REST POST /evisit/doctor/end                     (input: evisit_id)
 - REST POST /evisit/doctor/invite_next_patient     (input: evisit_id)

 - REST GET  /evisit/state ...                      (input: evisit_id) ... [ QUEUED | IN_SESSION | COMPLETE | BOOTED_OUT | PATIENT_LEFT ]


 // Tokbox calls
 // ... none. Tokens and Session ID gets Generated during doctorside-next-patient and patientside-has-doc-let-me-in



 */


@RestController
public class EVisitRestController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private void log(String str) {
        AMainApp.log(EVisitRestController.class.getSimpleName() + ": " + str);
    }


    private static final JdbcTemplate jdbcTemplate = AMainApp.getJdbcTemplate(AMainApp.DB_SOURCE_QCARE_ECG);

    public enum DOC_VCLINIC_STATE {
        OPEN{
            public String toString() {
                return "OPEN";
            }
        },
        CLOSED{
            public String toString() {
                return "CLOSED";
            }
        },
        PAUSED{
            public String toString() {
                return "PAUSED";
            }
        };

        public static boolean stateExists(final String state) {
            for (DOC_VCLINIC_STATE item : DOC_VCLINIC_STATE.values()) {
                if (item.toString().equalsIgnoreCase(state.toString()))
                    return true;
            }
            return false;
        }
    }

    public enum EVISIT_STATE {
        QUEUED {
            public String toString() {
                return "QUEUED";
            }
        },
        IN_SESSION {
            public String toString() {
                return "IN_SESSION";
            }
        },
        COMPLETE {
            public String toString() {
                return "COMPLETE";
            }
        },
        BOOTED_OUT {
            public String toString() {
                return "BOOTED_OUT";
            }
        },
        PATIENT_LEFT {
            public String toString() {
                return "PATIENT_LEFT";
            }
        };

        public static boolean stateExists(final String state) {
            for (EVISIT_STATE item : EVISIT_STATE.values()) {
                if (item.toString().equalsIgnoreCase(state.toString()))
                    return true;
            }
            return false;
        }
    }


    /**
     * verifies token and roles.
     * invalid tokens are redirected accordingly to invalid views
     * @param req
     * @param allowedRoles
     * @return
     */
    private boolean verifyTokenAndRoles(HttpServletRequest req, Role[] allowedRoles) {

        Tokens tokens = SessionUtils.getTokens(req);
        Auth0User user = SessionUtils.getAuth0User(req);

        if(tokens == null) {
            log("Invalid Token. 1");
            return false;
        }

        // check token validity
        if(tokens.getIdToken() == null || !JwtTokenValidationUtility.isIdTokenValid(tokens.getIdToken())) {
            log("Invalid Token. 2");
            return false;
        }

        log("ID Token: " + tokens.getIdToken());

        // validate role access
        String access = ApiAccessControl.areRolesAllowed(tokens.getIdToken(), allowedRoles);
        if(access.contains("DENY")) {
            log("Role Access Denied.");
            return false;
        }

        return true;
    }


    /**
     * Rest API to set current doctor's vclinic state
     *  REST POST /vclinic/doctor/state (input: doc_id, state: OPEN | CLOSED | PAUSED)
     * @param request
     * @return
     * @throws IOException
     */
    //@CrossOrigin NOT CROSSORIGIN (From website only and from session)
    @RequestMapping(value = "/vclinic/doctor/state", method = RequestMethod.POST)
    public @ResponseBody
    StandardResponse setVClinicState(HttpServletRequest request) throws IOException
    {
        /*Role[] allowedRoles = new Role[]{Role.ROLE_DOCTOR};
        if(!JwtTokenValidationUtility.isIdTokenValid(request)) {
            log("Invalid Token.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }
        String access = ApiAccessControl.areRolesAllowed(request, allowedRoles);
        if(access.contains("DENY")) {
            log("Role Access Denied.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }
        */

        String docId = request.getParameter("doc_id");
        if(docId == null || docId.equals("")) {
            return new StandardResponse(new IllegalArgumentException("doc_id missing"));
        }




        String newVClinicState = request.getParameter("vclinic_state");

        if(!DOC_VCLINIC_STATE.stateExists(newVClinicState)) {
            return new StandardResponse(new IllegalArgumentException("Illegal VClinic State Parameter"));
        }

        final long timestamp = System.currentTimeMillis();

        String oldVClinicState = getVClinicStateByDocId(request).body.toString();
        log(oldVClinicState);

        try {

            String sql = "";
            Object[] params = new Object[]{docId, newVClinicState, timestamp};

            // opening
            if(newVClinicState.equals(DOC_VCLINIC_STATE.OPEN.toString())) {

                log("REQUESTING TO OPEN VCLINIC");
                if(oldVClinicState.contains("OPEN")) {
                    log("Already open. Not doing anything.");
                    return new StandardResponse("ALREADY OPEN. DOING NOTHING.");
                }

                if(oldVClinicState.contains("PAUSED")) {
                    log("Currently Paused. Opening.");
                    // not updating open timestamp only state
                    sql = "update evisit_doctor_vclinic_state set vclinic_state=?,last_beacon_received_at_ms=? where doc_id=? and vclinic_state=?";
                    params = new Object[]{DOC_VCLINIC_STATE.OPEN.toString(), timestamp, docId, DOC_VCLINIC_STATE.PAUSED.toString()};
                }
                else // closed
                {
                    log("Currently closed. Opening.");
                    sql = "insert into evisit_doctor_vclinic_state(doc_id, vclinic_state, vclinic_opened_at_ms, last_beacon_received_at_ms) values(?, ?, ?, ?);";
                    params = new Object[]{docId, DOC_VCLINIC_STATE.OPEN.toString(), timestamp, timestamp};
                }

            }
            // closing
            else if(newVClinicState.equals(DOC_VCLINIC_STATE.CLOSED.toString())) {
                log("REQUESTING TO CLOSE VCLINIC");
                if(oldVClinicState.contains("CLOSED")) {
                    log("Already closed. Not doing anything.");
                    return new StandardResponse("ALREADY CLOSED. DOING NOTHING.");
                }
                else if(oldVClinicState.contains("OPEN")) {

                    log("Currently Open. Closing.");
                    //sql  = "update evisit_doctor_vclinic_state set vclinic_state=?, vclinic_closed_at_ms=?, last_beacon_received_at_ms=? where doc_id=? and vclinic_state=?;";
                    //params = new Object[]{DOC_VCLINIC_STATE.CLOSED.toString(), timestamp, timestamp, docId, DOC_VCLINIC_STATE.OPEN.toString()};
                    // .. also delete all patients in the queue
                    //sql += "update evisit_doctor_vclinic_queue set evisit_state='BOOTED_OUT', completed_timestamp_ms=? where evisit_state='QUEUED' and doc_id=?;";

                    // simply deleting from state
                    sql  = "delete from evisit_doctor_vclinic_state where doc_id=?;";
                    params = new Object[]{docId};

                    // deleting from queue
                    jdbcTemplate.update("delete from evisit_doctor_vclinic_queue where doc_id=?", params);
                }

            }
            // pausing
            else if(newVClinicState.equals(DOC_VCLINIC_STATE.PAUSED.toString())) {
                if(oldVClinicState.contains("PAUSED")) {
                    log("Already paused. Not doing anything.");
                    return new StandardResponse("ALREADY PAUSED. DOING NOTHING.");
                }
                else if(oldVClinicState.contains("OPEN")) {
                    log("Currently Open. Closing.");
                    sql = "update evisit_doctor_vclinic_state set vclinic_state=?, vclinic_paused_at_ms=?, last_beacon_received_at_ms=? where doc_id=? and vclinic_state=?";
                    params = new Object[]{DOC_VCLINIC_STATE.PAUSED.toString(), timestamp, timestamp, docId, DOC_VCLINIC_STATE.OPEN.toString()};
                }
            }

            log("Performing SQL: " + sql);
            log(".... over params " + new Gson().toJson(params));
            int r = jdbcTemplate.update(sql, params);

            String response;
            if(r > 0) {
                response = "SUCCESS^^" + timestamp;
            }
            else {
                response = "FAIL";
            }

            return new StandardResponse(response);

        } catch (Exception e) {
            e.printStackTrace();
            return new StandardResponse(e);
        }
    }








    /**
     * Rest API to get current doctor's vclinic state
     *
     *  REST GET  /vclinic/doctor/state (input: doc_id)
     *
     * @param request
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "/vclinic/doctor/state", method = RequestMethod.GET)
    public @ResponseBody
    StandardResponse getVClinicStateByDocId(HttpServletRequest request) throws IOException {

        /*Role[] allowedRoles = new Role[]{Role.ROLE_PATIENT, Role.ROLE_ATHLETE, Role.ROLE_DOCTOR};
        if(!JwtTokenValidationUtility.isIdTokenValid(request)) {
            log("Invalid Token.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED. Invalid Token."));
        }
        String access = ApiAccessControl.areRolesAllowed(request, allowedRoles);
        if(access.contains("DENY")) {
            log("Role Access Denied.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED. Role Denied."));
        }*/

        String docId = request.getParameter("doc_id");
        if(docId == null) {
            docId = JwtTokenValidationUtility.getUserId(request);
        }

        log("at getVClinicState: docId: " + docId);
        if(docId == null) {
            log("DocID is null. Sending CLOSED");
            return new StandardResponse(DOC_VCLINIC_STATE.CLOSED.toString());
        }

        try {

            String sql = "select * from evisit_doctor_vclinic_state where doc_id=? order by vclinic_opened_at_ms desc limit 1;";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, new Object[]{docId});

            log("at getVClinicState: rows: " + rows.size());

            if(rows.size() == 0) {
                return new StandardResponse(DOC_VCLINIC_STATE.CLOSED.toString());
            }
            return new StandardResponse(rows.get(0).get("vclinic_state") + "^^" + rows.get(0).get("vclinic_opened_at_ms"));

        } catch (Exception e) {
            e.printStackTrace();
            return new StandardResponse(e);
        }
    }










    /**
     * To Keep VClinic Open ... keep sending a pulse from client
     * ... if last received pulse is older than 10 minutes
     * ... set clinic closed (VCLINIC_LIFE_PULSE_EXPIRED)
     *
     *  REST POST  /vclinic/doctor/beacon (input: doc_id, vclinic_opened_at_ms)
     *
     * @param request
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "/vclinic/doctor/beacon", method = RequestMethod.POST)
    public @ResponseBody
    StandardResponse setVClinicBeacon(HttpServletRequest request) throws IOException {

        /*Role[] allowedRoles = new Role[]{Role.ROLE_DOCTOR};
        if(!JwtTokenValidationUtility.isIdTokenValid(request)) {
            log("Invalid Token.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }
        String access = ApiAccessControl.areRolesAllowed(request, allowedRoles);
        if(access.contains("DENY")) {
            log("Role Access Denied.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }*/

        long vclinicOpenedAt = Long.parseLong(request.getParameter("vclinic_opened_at_ms"));

        String docId = request.getParameter("doc_id");
        if(docId == null) {
            docId = JwtTokenValidationUtility.getUserId(request);
        }

        if(docId == null) {
            return new StandardResponse(new IllegalArgumentException("doc_id missing"));
        }

        if(vclinicOpenedAt == -1) {
            return new StandardResponse(new IllegalArgumentException("vclinic_opened_at_ms is -1)"));
        }


        final long beaconTimestamp = System.currentTimeMillis();

        String response = "";
        try {
            String sql = "update evisit_doctor_vclinic_state set last_beacon_received_at_ms=? where doc_id=? and vclinic_opened_at_ms=?";
            Object[] params = new Object[]{beaconTimestamp, docId, vclinicOpenedAt};
            log(sql + " ... " + new Gson().toJson(params));

            int r = jdbcTemplate.update(sql, params);

            if(r > 0) {
                response = "SUCCESS^^"+beaconTimestamp;
            }
            else {
                response = "FAILED";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new StandardResponse(e);
        }

        return new StandardResponse(response);
    }










    /**
     * Get Online Doctors
     *  REST GET  /evisit/patient/online_doctors        (input: pat_id?)
     * @param request
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "/evisit/patient/online_doctors", method = RequestMethod.GET)
    public @ResponseBody
    StandardResponse getOnlineDoctors(HttpServletRequest request) throws IOException
    {
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

        String patientId = request.getParameter("patient_id");

        try {

            String sql = "select * from evisit_doctor_vclinic_state where vclinic_state=?;";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, new Object[]{OPEN.toString()});
            List<Map<String, Object>> finalRows = new ArrayList<>();

            for(Map<String, Object> row: rows)
            {
                long lastBeaconMs = (long) row.get("last_beacon_received_at_ms");
                final String docId = (String) row.get("doc_id");
                long currTs = System.currentTimeMillis();
                final long BEACON_TIMEOUT_MS = 10 * 60 * 1000;
                if( (currTs - lastBeaconMs) > BEACON_TIMEOUT_MS) {
                    closeClinic(docId);
                }
                else {
                    finalRows.add(row);
                }

            }

            return new StandardResponse(AMainApp.gson.toJson(finalRows));

        } catch (Exception e) {
            e.printStackTrace();
            return new StandardResponse(e);
        }
    }



    private void closeClinic(final String doc_id) {

        final String sql = "update evisit_doctor_vclinic_state set vclinic_state=? where doc_id=?";
        Object[] params = new Object[]{DOC_VCLINIC_STATE.CLOSED.toString(), doc_id};

        int rows = jdbcTemplate.update(sql, params);
        if(rows == 1)
            log("Closed Clinic for " + doc_id);
        else
            log("Clinic not closed");
    }


    private String getEvisitId(final String patientId, final String docId) {

        String sql = "";
        Object[] params;
        if(!(docId == null && patientId == null)) {
            log("pat_id and doc_id provided ... ");
            sql = "select * from evisit_doctor_vclinic_queue" +
                    " where pat_id=? and doc_id=? and (evisit_state=? or evisit_state=?) order by request_timestamp_ms desc limit 1";


            params = new Object[]{patientId, docId, EVISIT_STATE.QUEUED.toString(), EVISIT_STATE.IN_SESSION.toString()};
        }
        else {
            return null;
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);

        return String.valueOf(rows.get(0).get("evisit_id"));
    }


    /**
     * Patient enqueues at doctor's vclinic
     * REST POST /evisit/patient/enqueue               (input: doc_id)
     * @param request
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "/evisit/patient/enqueue", method = RequestMethod.POST)
    public @ResponseBody
    StandardResponse enqueuePatient(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

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

        String patientId = JwtTokenValidationUtility.getUserId(request);
        String docId = URLDecoder.decode(request.getParameter("doc_id"), "UTF-8");
        log("Requesting Doc_id: " + docId);
        log("by PatientId: " + patientId);
        final long timestamp = System.currentTimeMillis();

        final String eVisitId = DigestUtils.sha256Hex((patientId + "-" + docId + "-" + timestamp).getBytes(StandardCharsets.UTF_8));

        // if doctor vclinic not open
        String currDocVClinicState = getVClinicStateByDocId(request).body.toString();
        if(currDocVClinicState == null || !currDocVClinicState.contains(DOC_VCLINIC_STATE.OPEN.toString())) {
            log("Clinic Closed ... ");
            return new StandardResponse("CLOSED");
        }


        if(Integer.valueOf(getPatientPosition(request).body.toString()) > 0) {
            // TODO: Return EVISIT ID WITH ALREADY_ENQUEUED
            log("$$$$ ALREADY ENQUEUED");
            return new StandardResponse("ALREADY_ENQUEUED^^" + this.getEvisitId(patientId, docId));
        }



        log("$$$$ ENQUEUEING: " + eVisitId);
        String sql = "insert into evisit_doctor_vclinic_queue" +
                " (doc_id, pat_id, evisit_id, evisit_state, request_timestamp_ms) values" +
                " (?,?,?,?,?)";

        Object[] params = new Object[]{docId, patientId, eVisitId, EVISIT_STATE.QUEUED.toString(), timestamp};

        int r = jdbcTemplate.update(sql, params);

        request.setAttribute("evisit_id", eVisitId);

        String response;
        if(r > 0) {
            response = "SUCCESS" + "^^" + eVisitId;
        }
        else {
            response = "FAILED";
        }

        return new StandardResponse(response);
    }






    /**
     * Get Patient Queue for Doctor's VClinic
     *  REST GET  /evisit/doctor/patient_queue  (input: doc_id)
     * @param request
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "/evisit/doctor/patient_queue", method = RequestMethod.GET)
    public @ResponseBody
    StandardResponse getPatientQueue(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

        /*
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
        */



        String docId = request.getParameter("doc_id");

        if(docId == null) {
            if (ApiAccessControl.areRolesAllowed(request, new Role[]{Role.ROLE_DOCTOR}).contains("ALLOW")) {
                docId = JwtTokenValidationUtility.getUserId(request);
            }
        }

        if (docId == null) {
            return new StandardResponse(new IllegalArgumentException("doc_id missing"));
        }


        // if doctor vclinic not open
        String currDocVClinicState = getVClinicStateByDocId(request).body.toString();
        if(currDocVClinicState == null || !currDocVClinicState.contains(DOC_VCLINIC_STATE.OPEN.toString())) {
            return new StandardResponse("CLOSED");
        }

        long vclinic_open_timestamp_ms = -1;
        String vclinicState = getVClinicStateByDocId(request).body.toString();
        log(">>>>> " + vclinicState + " ... " + new Gson().toJson(vclinicState.split(Pattern.quote("^^"))));
        if(vclinicState.contains(DOC_VCLINIC_STATE.OPEN.toString())) {
            vclinic_open_timestamp_ms = Long.parseLong(vclinicState.split(Pattern.quote("^^"))[1]);
        }

        String sql = "select * from evisit_doctor_vclinic_queue where doc_id=? and (evisit_state=? or evisit_state=?) and request_timestamp_ms>? order by request_timestamp_ms asc";
        Object[] params = new Object[]{docId, EVISIT_STATE.QUEUED.toString(), EVISIT_STATE.IN_SESSION.toString(), vclinic_open_timestamp_ms};

        log(">>>>>> " + vclinic_open_timestamp_ms);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);
        // there will always be one row because of count(*)

        //int position = Integer.parseInt(String.valueOf(rows.get(0).get("before_me")));
        String rowsJson = AMainApp.gson.toJson(rows);
        log("Patient Queue: " + rowsJson);
        return new StandardResponse(rowsJson);
    }



    //new Auth0UserDirectoryProxy("google-oauth2|101010739141713793949").getAuth0UserJson();
    /**
     * Get Evisit State
     *  //REST GET  /user/info .. allows a doctor or patient (only self) to get user details.
     * @param request
     * @return
     * @throws IOException
     * TODO: Replace with secure code below ...
     */
    @CrossOrigin
    @RequestMapping(value = "/auth0/user", method = RequestMethod.GET)
    public @ResponseBody
    StandardResponse getAuth0UserInfoInsecure(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

        String userIdRequested = URLDecoder.decode(request.getParameter("user_id"), "UTF-8");

        String auth0UserJson = new Auth0UserDirectoryProxy(userIdRequested).getAuth0UserJson();

        return new StandardResponse(auth0UserJson);

    }


    StandardResponse getAuth0UserInfoSecure(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

        Role[] allowedRoles = new Role[]{Role.ROLE_PATIENT, Role.ROLE_DOCTOR};
        if (!JwtTokenValidationUtility.isIdTokenValid(request)) {
            log("Invalid Token.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }
        String access = ApiAccessControl.areRolesAllowed(request, allowedRoles);
        if (access.contains("DENY")) {
            log("Role Access Denied.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }

        String requestorUserId = JwtTokenValidationUtility.getUserId(JwtTokenValidationUtility.getIdTokenFromRequest(request));
        String userIdRequested = URLDecoder.decode(request.getParameter("user_id"), "UTF-8");

        String auth0UserJson = new Auth0UserDirectoryProxy(userIdRequested).getAuth0UserJson();

        // if patient ... then only return self details
        if(ApiAccessControl.areRolesAllowed(request, new Role[]{Role.ROLE_PATIENT}).contains("ALLOW"))
        {
            // if i am a patient and i am a doctor. Then I can ask for patient details
            if(ApiAccessControl.areRolesAllowed(request, new Role[]{Role.ROLE_DOCTOR}).contains("ALLOW")) {
                return new StandardResponse(auth0UserJson);
            }

            // patient asking for self details
            if(requestorUserId.equals(userIdRequested))
            {
                return new StandardResponse(auth0UserJson);
            }
            else // patient asking for someone else details but allow only doctor details
            {
                JSONArray rolesJsonArr = new JSONObject(auth0UserJson).getJSONObject("app_metadata").getJSONArray("roles");
                String rolesArr[] = new String[rolesJsonArr.length()];
                boolean requestedInfoIsDoctor = false;
                for(int i = 0; i < rolesJsonArr.length(); i++) {
                    rolesArr[i] = (String) rolesJsonArr.get(i);
                    if(rolesArr[i].equals("doctor")) {
                        requestedInfoIsDoctor = true;
                        return new StandardResponse(auth0UserJson);
                    }
                }

                if(!requestedInfoIsDoctor) {
                    log("Not Doctor: Access Denied");
                    return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
                }
                else {
                    return new StandardResponse(auth0UserJson);
                }
            }
        }
        else if(ApiAccessControl.areRolesAllowed(request, new Role[]{Role.ROLE_DOCTOR}).contains("ALLOW")) // doctor accessing
        {
            return new StandardResponse(auth0UserJson);
        }
        else {
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }

    }


    /**
     * Get Evisit State
     *  //REST GET  /evisit/state .. (input: evisit_id) ... [ QUEUED | IN_SESSION | COMPLETE | BOOTED_OUT | PATIENT_LEFT ]
     * @param request
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "/evisit/state", method = RequestMethod.GET)
    public @ResponseBody
    StandardResponse getEvisitState(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

        /*Role[] allowedRoles = new Role[]{Role.ROLE_PATIENT, Role.ROLE_DOCTOR};
        if(!JwtTokenValidationUtility.isIdTokenValid(request)) {
            log("Invalid Token.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }
        String access = ApiAccessControl.areRolesAllowed(request, allowedRoles);
        if(access.contains("DENY")) {
            log("Role Access Denied.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }*/

        String eVisitId = request.getParameter("evisit_id");

        if (eVisitId == null) {
            return new StandardResponse(new IllegalArgumentException("evisit_id missing"));
        }



        String sql = "select * from evisit_doctor_vclinic_queue where evisit_id=?";
        Object[] params = new Object[]{eVisitId};

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);

        if(rows.size() == 0) {
            return new StandardResponse("COMPLETE");
        }

        String eVisitState = (String) rows.get(0).get("evisit_state");
        String idToken = JwtTokenValidationUtility.getIdTokenFromRequest(request);

        // only current patient can check it's evisit state
        if(JwtTokenValidationUtility.tokenHasRole(idToken, Role.ROLE_PATIENT)) {

            if(!JwtTokenValidationUtility.getUserId(idToken).equals(rows.get(0).get("pat_id"))) {
                log("Another patient is checking this patient's evisit state. Denying access.");
                return new StandardResponse(new IllegalAccessException("ACCESS_DENIED"));
            }
        }
        log("Evisit State for " + eVisitId + ": " + eVisitState);
        return new StandardResponse(eVisitState);
    }



    /**
     * Doctor saves notes for current eVisit
     *
     *  REST POST /evisit/doctor/notes           (input: evisit_id, note_str, public_bool)
     *
     * @param request
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "/evisit/doctor/notes", method = RequestMethod.POST)
    public @ResponseBody
    StandardResponse saveEVisitNotes(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

        Role[] allowedRoles = new Role[]{Role.ROLE_DOCTOR};
        if(!JwtTokenValidationUtility.isIdTokenValid(request)) {
            log("Invalid Token.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }
        String access = ApiAccessControl.areRolesAllowed(request, allowedRoles);
        if(access.contains("DENY")) {
            log("Role Access Denied.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }

        String eVisitId = request.getParameter("evisit_id");
        String noteStr = request.getParameter("note_str");
        if(noteStr == null || noteStr.equals("")) {
            return new StandardResponse("No note provided. Doing nothing.");
        }

        if (eVisitId == null) {
            return new StandardResponse(new IllegalArgumentException("evisit_id missing"));
        }

        String sql = "update evisit_doctor_vclinic_queue set evisit_notes=? where evisit_id=?";
        Object[] params = new Object[]{noteStr, eVisitId};

        int count = jdbcTemplate.update(sql, params);

        if(count == 1) {
            return new StandardResponse("SUCCESS");
        }
        else {
            return new StandardResponse("FAILED");
        }

    }








    /**
     * Patient decides to leave vclinic
     *
     *  REST POST /evisit/patient/leave (input: evisit_id)
     *
     * @param request
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "/evisit/patient/leave", method = RequestMethod.POST)
    public @ResponseBody
    StandardResponse patientLeavesClinic(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

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

        String eVisitId = request.getParameter("evisit_id");
        String idToken = JwtTokenValidationUtility.getIdTokenFromRequest(request);

        if (eVisitId == null) {
            return new StandardResponse(new IllegalArgumentException("evisit_id missing"));
        }

        String patId = JwtTokenValidationUtility.getUserId(idToken);

        String sql = "update evisit_doctor_vclinic_queue set evisit_state=? where evisit_id=? and pat_id=?";
        Object[] params = new Object[]{EVISIT_STATE.PATIENT_LEFT.toString(), eVisitId, patId};

        int count = jdbcTemplate.update(sql, params);

        if(count == 1) {
            return new StandardResponse("SUCCESS");
        }
        else {
            return new StandardResponse("FAILED");
        }

    }




    /**
     * Doctor ends evisit
     *
     *  REST POST /evisit/doctor/end                     (input: evisit_id)
     *
     * @param request
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "/evisit/doctor/end", method = RequestMethod.POST)
    public @ResponseBody
    StandardResponse doctorEndsEvisit(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

        /*Role[] allowedRoles = new Role[]{Role.ROLE_DOCTOR};
        if(!JwtTokenValidationUtility.isIdTokenValid(request)) {
            log("Invalid Token.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }
        String access = ApiAccessControl.areRolesAllowed(request, allowedRoles);
        if(access.contains("DENY")) {
            log("Role Access Denied.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }*/

        String idToken = JwtTokenValidationUtility.getIdTokenFromRequest(request);
        String eVisitId = request.getParameter("evisit_id");

        if (eVisitId == null) {
            return new StandardResponse(new IllegalArgumentException("evisit_id missing"));
        }

        String docId = request.getParameter("doc_id");
        if(docId == null)
            docId = JwtTokenValidationUtility.getUserId(idToken);

        if(docId == null) {
            return new StandardResponse(new IllegalArgumentException("doc_id missing"));
        }

        final long completedTimestamp = System.currentTimeMillis();

        // closing tokbox session ... TODO: Check if we need to close session
        /*
            List<Map<String, Object>> evisit_row = jdbcTemplate.queryForList("select * from evisit_doctor_vclinic_queue where evisit_id=?", new Object[]{eVisitId});
            String tokboxSessionId = (String) evisit_row.get(0).get("tokbox_session_id");

            TokboxProxyGateway.getOpenTokInstance().?? .. nothing to close session
        */


        //String sql = "update evisit_doctor_vclinic_queue set evisit_state=?, completed_timestamp_ms=? where evisit_id=? and doc_id=?";
        //Object[] params = new Object[]{EVISIT_STATE.COMPLETE.toString(), completedTimestamp, eVisitId, docId};

        String sql = "delete from evisit_doctor_vclinic_queue where evisit_id=?";
        Object[] params = new Object[]{eVisitId};


        int count = jdbcTemplate.update(sql, params);

        if(count == 1) {
            return new StandardResponse("SUCCESS");
        }
        else {
            return new StandardResponse("FAILED");
        }

    }









    /**
     * Patient gets queue position at vclinic
     * REST GET /evisit/patient/position    (input: evisit_id)
     * @param request
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "/evisit/patient/position", method = RequestMethod.GET)
    public @ResponseBody
    StandardResponse getPatientPosition(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

        /*Role[] allowedRoles = new Role[]{Role.ROLE_PATIENT};
        if(!JwtTokenValidationUtility.isIdTokenValid(request)) {
            log("Invalid Token.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }
        String access = ApiAccessControl.areRolesAllowed(request, allowedRoles);
        if(access.contains("DENY")) {
            log("Role Access Denied.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }*/




        String docId = request.getParameter("doc_id");
        String patientId = JwtTokenValidationUtility.getUserId(request);


        // if doctor vclinic not open
        if(docId == null)
            return new StandardResponse(new IllegalArgumentException("doc_id missing"));

        log("at getPatientPosition: pat_id: " + patientId);
        log("at getPatientPosition: doc_id: " + docId);

        String currDocVClinicState = getVClinicStateByDocId(request).body.toString();
        log("at getPatientPosition: currDocVClinicState: " + currDocVClinicState);
        if(currDocVClinicState == null || !currDocVClinicState.contains(DOC_VCLINIC_STATE.OPEN.toString())) {
            return new StandardResponse("CLOSED");
        }




        String sql = "";
        Object[] params = new Object[]{};

        String eVisitId = request.getParameter("evisit_id");
        if(eVisitId == null) {
            eVisitId = (String) request.getAttribute("evisit_id");
        }

        if(eVisitId == null) {
            if(!(docId == null && patientId == null)) {
                log("pat_id and doc_id provided ... ");
                sql = "select count(*) as my_position from evisit_doctor_vclinic_queue" +
                        " where " +
                        " request_timestamp_ms <= (select request_timestamp_ms from evisit_doctor_vclinic_queue where pat_id=? and doc_id=?  order by request_timestamp_ms desc limit 1) " +
                        " and doc_id=(select doc_id from evisit_doctor_vclinic_queue where pat_id=? and doc_id=? order by request_timestamp_ms desc limit 1) " +
                        " and (evisit_state=? or evisit_state=?)";


                params = new Object[]{patientId, docId, patientId, docId, EVISIT_STATE.QUEUED.toString(), EVISIT_STATE.IN_SESSION.toString()};
            }
            else
                return new StandardResponse(new IllegalArgumentException("evisit_id or pat_id,doc_id pair missing"));
        }
        else {
            log("evisit_id provided");

            // get number of people before me including me.
            sql = "select count(*) as my_position from evisit_doctor_vclinic_queue" +
                    " where " +
                    " request_timestamp_ms <= (select request_timestamp_ms from evisit_doctor_vclinic_queue where evisit_id=?) " +
                    " and doc_id=(select doc_id from evisit_doctor_vclinic_queue where evisit_id=?) " +
                    " and (evisit_state=? or evisit_state=?)";

            log(sql);

            params = new Object[]{eVisitId, eVisitId, EVISIT_STATE.QUEUED.toString(), EVISIT_STATE.IN_SESSION.toString()};
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);
        // there will always be one row because of count(*)

        int position = Integer.parseInt(String.valueOf(rows.get(0).get("my_position")));
        log("POSITION: " + position);

        return new StandardResponse(position);
    }


    /**
     *
     * @param request
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "/vdo", method = RequestMethod.GET)
    public @ResponseBody
    String generateTokboxToken(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

        String pass = request.getParameter("p");
        if(pass == null) {
            return ("Oops. Server made a boo boo.");
        }

         if(!pass.equals("yoloMonty")) {
             return ("Oops. Server made a boo boo.");
         }

        Session session;
        String openTokSessionId;
        String patToken, docToken;
        try {

            session = TokboxProxyGateway.getOpenTokInstance().createSession();
            openTokSessionId = session.getSessionId();

            patToken = URLEncoder.encode(session.generateToken(), "UTF-8");
            docToken = URLEncoder.encode(session.generateToken(), "UTF-8");

            log("Doc Tokbox Token: " + docToken);

            //String url1 = "https://tokbx.glitch.me/pat.html?openTokSessionId=" + openTokSessionId + "&apiKey=" + TokboxProxyGateway.API_KEY + "&openTokToken=" + patToken;
            //String url2 = "https://tokbx.glitch.me/pat.html?openTokSessionId=" + openTokSessionId + "&apiKey=" + TokboxProxyGateway.API_KEY + "&openTokToken=" + docToken;

            //String url1 = "http://localhost:3099/vclinic/videochat?openTokSessionId=" + openTokSessionId + "&apiKey=" + TokboxProxyGateway.API_KEY + "&openTokToken=" + patToken + "&w=1000&h=600";
            //String url2 = "http://localhost:3099/vclinic/videochat?openTokSessionId=" + openTokSessionId + "&apiKey=" + TokboxProxyGateway.API_KEY + "&openTokToken=" + docToken + "&w=1000&h=600";

            String url1 = "/vclinic/videochat?openTokSessionId=" + openTokSessionId + "&apiKey=" + TokboxProxyGateway.API_KEY + "&openTokToken=" + patToken + "&w=1000&h=600";
            String url2 = "/vclinic/videochat?openTokSessionId=" + openTokSessionId + "&apiKey=" + TokboxProxyGateway.API_KEY + "&openTokToken=" + docToken + "&w=1000&h=600";

            return ("URL1: <a href='" + url1 + "'>URL1</a><br />\n<br />\nURL2: <a href='" + url2 + "'>URL2</a>");

        } catch (OpenTokException e) {
            e.printStackTrace();
            return ("OPEN_TOK_FAILED\n" + e.getMessage());
        }
    }





    /**
     * Doctor invites next patient
     *
     *  REST POST /evisit/doctor/invite_next_patient                     (input: evisit_id)
     *
     * @param request
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "/evisit/doctor/invite_next_patient", method = RequestMethod.POST)
    public @ResponseBody
    StandardResponse doctorInvitesNextPatient(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

        /*Role[] allowedRoles = new Role[]{Role.ROLE_DOCTOR};
        if(!JwtTokenValidationUtility.isIdTokenValid(request)) {
            log("Invalid Token.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }
        String access = ApiAccessControl.areRolesAllowed(request, allowedRoles);
        if(access.contains("DENY")) {
            log("Role Access Denied.");
            return new StandardResponse(new IllegalAccessException("ACCESS DENIED."));
        }

        String idToken = JwtTokenValidationUtility.getIdTokenFromRequest(request);
        */

        String eVisitId = request.getParameter("evisit_id");

        if (eVisitId == null) {
            return new StandardResponse(new IllegalArgumentException("evisit_id missing"));
        }

        String docId = request.getParameter("doc_id");
        if(docId == null) {
            docId = JwtTokenValidationUtility.getUserId(request);
        }
        final long timestamp = System.currentTimeMillis();

        Session session;
        String openTokSessionId;
        String patToken, docToken;
        try {

            session = TokboxProxyGateway.getOpenTokInstance().createSession();
            openTokSessionId = session.getSessionId();

            patToken = URLEncoder.encode(session.generateToken(), "UTF-8");
            docToken = URLEncoder.encode(session.generateToken(), "UTF-8");

            log("Doc Tokbox Token: " + docToken);

        } catch (OpenTokException e) {
            e.printStackTrace();
            return new StandardResponse("OPEN_TOK_FAILED\n" + e.getMessage());
        }


        String sql = "update evisit_doctor_vclinic_queue set evisit_state=?,in_session_timestamp_ms=?, tokbox_session_id=?, tokbox_doc_token=?, tokbox_pat_token=? where evisit_id=? and doc_id=?";
        Object[] params = new Object[]{EVISIT_STATE.IN_SESSION.toString(), timestamp, openTokSessionId, docToken, patToken, eVisitId, docId};

        int count = jdbcTemplate.update(sql, params);
        if(count == 1) {
            return new StandardResponse("SUCCESS"+"^^"+openTokSessionId+"^^"+docToken + "^^" + TokboxProxyGateway.API_KEY);
        }
        else {
            return new StandardResponse("FAILED");
        }

    }











    /**
     * Patient checks if Doctor let's in (State becomes IN_SESSION & position is 1 ... NOT ZERO)
     * REST GET /evisit/patient/has_doctor_let_me_in    (input: evisit_id)
     * @param request
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "/evisit/patient/has_doctor_let_me_in", method = RequestMethod.GET)
    public @ResponseBody
    StandardResponse hasDoctorLetMeIn(HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

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


        String eVisitId = request.getParameter("evisit_id");
        String docId = URLDecoder.decode(request.getParameter("doc_id"), "UTF-8");
        String patientId = JwtTokenValidationUtility.getUserId(request);


        // if doctor vclinic not open
        if(eVisitId == null)
        {
            return new StandardResponse(new IllegalArgumentException("evisit_id missing"));
        }
        if(docId == null)
        {
            return new StandardResponse(new IllegalArgumentException("doc_id missing"));
        }
        String currDocVClinicState = getVClinicStateByDocId(request).body.toString();
        if(currDocVClinicState == null || !currDocVClinicState.contains(DOC_VCLINIC_STATE.OPEN.toString())) {
            return new StandardResponse("CLOSED");
        }


        String sql = "select * from evisit_doctor_vclinic_queue where evisit_id=? and evisit_state=?";
        Object[] params = new Object[]{eVisitId, EVISIT_STATE.IN_SESSION.toString()};

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params);

        if(rows.size() == 0) {
            return new StandardResponse("NOPE" + "^^" + getPatientPosition(request).body.toString());
        }

        String tokboxSessionId = (String) rows.get(0).get("tokbox_session_id");
        String tokboxPatToken = (String) rows.get(0).get("tokbox_pat_token");

        return new StandardResponse("IN_SESSION" + "^^" + tokboxSessionId + "^^" + tokboxPatToken + "^^" + TokboxProxyGateway.API_KEY);

    }




}
