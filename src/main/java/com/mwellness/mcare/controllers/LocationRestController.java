package com.mwellness.mcare.controllers;

import com.auth0.Auth0User;
import com.auth0.SessionUtils;
import com.auth0.Tokens;
import com.google.gson.Gson;
import com.mwellness.mcare.AMainApp;
import com.mwellness.mcare.auth0.JwtTokenValidationUtility;
import com.mwellness.mcare.auth0.Role;
import com.mwellness.mcare.responses.StandardResponse;
import com.mwellness.mcare.utils.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controller to manage location information for patients
 * Created by dev01 on 01/18/18.

 // Queueing Calls

 - REST POST  /location                    (input: patient_id from patient token)
 - REST GET /location                      (input: userId)


 */


@RestController
public class LocationRestController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private void log(String str) {
        AMainApp.log(LocationRestController.class.getSimpleName() + ": " + str);
    }


    private static final JdbcTemplate jdbcTemplate = AMainApp.getJdbcTemplate(AMainApp.DB_SOURCE_QCARE_ECG);



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
    @RequestMapping(value = "/location", method = RequestMethod.POST)
    public @ResponseBody
    StandardResponse setLocation(HttpServletRequest request) throws IOException
    {
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


        long timestamp = Long.parseLong(request.getParameter("timestamp"));
        double lat = Double.parseDouble(request.getParameter("lat"));
        double lng = Double.parseDouble(request.getParameter("lng"));

        String patId = JwtTokenValidationUtility.getUserId(request);

        try {

            Object[] params = new Object[]{patId, lat, lng, timestamp};
            String sql = "insert into gps values(?,?,?,?)";

            log("Performing SQL: " + sql);
            log(".... over params " + new Gson().toJson(params));
            int r = jdbcTemplate.update(sql, params);

            String response;
            if(r > 0) {
                response = "SUCCESS";
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
     * Rest API to get last location of userId
     *
     *  REST GET  /location (input: userId)
     *
     * @param request
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "/location", method = RequestMethod.GET)
    public @ResponseBody
    StandardResponse getLocation(HttpServletRequest request) throws IOException {

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

        String userId = request.getParameter("userId");

        try {

            String sql = "select * from gps where userId=? order by timestamp desc limit 1;";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, new Object[]{userId});

            log("at getVClinicState: rows: " + rows.size());

            if(rows.size() == 1) {

                LocationInfo li = GsonUtils.getInstance().fromJson(GsonUtils.getInstance().toJson(rows.get(0)), LocationInfo.class);
                log("===========>> " + GsonUtils.getInstance().toJson(li));
                return new StandardResponse(GsonUtils.getInstance().toJson(li));
            }
            else return new StandardResponse("{}");

        } catch (Exception e) {
            e.printStackTrace();
            return new StandardResponse(e);
        }
    }


}


class LocationInfo {

    private String userid = "";
    private double lat = -1;
    private double lng = -1;
    private long timestamp = -1;
    private final String gmapsKey = "AIzaSyBdTlcHU-k2ad5pBK1WnJLG1WsAuB0iYgs";

    public String getOpenWeatherMapKey() {
        return openWeatherMapKey;
    }

    private final String openWeatherMapKey = "08c6c84be8cbf08cb1c3775d029d0075";

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getGmapsKey() {
        return gmapsKey;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}