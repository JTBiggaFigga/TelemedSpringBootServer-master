package com.mwellness.mcare.controllers;

/**
 * Created by dev01 on 12/30/17.
 */


import com.mwellness.mcare.AMainApp;
import com.mwellness.mcare.AppConfig;
import com.mwellness.mcare.auth0.JwtTokenValidationUtility;
import com.mwellness.mcare.auth0.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Controller to manage patient vitals
 * Created by dev01 on 02/13/18.


 */


@Controller
public class VitalsMvcController {

    private static final Logger logger = LoggerFactory.getLogger(VitalsMvcController.class);

    private static void log(String str) {
        AMainApp.log(VitalsMvcController.class.getSimpleName() + ": " + str);
    }


    private static final JdbcTemplate jdbcTemplate = AMainApp.getJdbcTemplate(AMainApp.DB_SOURCE_QCARE_ECG);

    private static AppConfig appConfig;

    @Autowired
    VitalsMvcController(AppConfig appConfig) {
        this.appConfig = appConfig;
    }





    /**
     * Get Vital Charts
     * @param request
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @CrossOrigin
    @RequestMapping(value = "/vitals/charts/mobile", method = RequestMethod.GET)
    public String getVitalChartsMobile(final Map<String, Object> model, HttpServletRequest request) throws IOException, NoSuchAlgorithmException {

        Role[] allowedRoles = new Role[]{Role.ROLE_PATIENT};
        if (!JwtTokenValidationUtility.isIdTokenValid(request)) {
            log("Invalid Token.");
            return "logout";
        }
        String access = ApiAccessControl.areRolesAllowed(request, allowedRoles);
        if (access.contains("DENY")) {
            log("Role Access Denied.");
            return "logout";
        }


        String patientId = JwtTokenValidationUtility.getUserId(request);
        log("by PatientId: " + patientId);

        model.put("patientId", patientId);
        model.put("token", request.getParameter("token"));


        return "patient/patient_charts";

    }






}
