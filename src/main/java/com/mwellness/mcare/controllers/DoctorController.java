package com.mwellness.mcare.controllers;

import com.auth0.Auth0User;
import com.auth0.SessionUtils;
import com.auth0.Tokens;
import com.mwellness.mcare.AMainApp;
import com.mwellness.mcare.AppConfig;
import com.mwellness.mcare.auth0.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Controller
public class DoctorController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AppConfig appConfig;

    @Autowired
    public DoctorController(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    private void log(String str) {
        AMainApp.log(DoctorController.class.getSimpleName() + ": " + str);
    }


    /**
     * verifies token and roles.
     * invalid tokens are redirected accordingly to invalid views
     * @param req
     * @param allowedRoles
     * @return
     */
    private String verifyToken(HttpServletRequest req, Role[] allowedRoles) {

        Tokens tokens = SessionUtils.getTokens(req);
        Auth0User user = SessionUtils.getAuth0User(req);

        if(tokens == null) {
            log("Invalid Token.");
            return "unassigned_role";
        }

        // check token validity
        if(tokens.getIdToken() == null || !JwtTokenValidationUtility.isIdTokenValid(tokens.getIdToken())) {
            log("Invalid Token.");
            return "unassigned_role";
        }

        log("ID Token: " + tokens.getIdToken());

        // validate role access
        String access = ApiAccessControl.areRolesAllowed(tokens.getIdToken(), allowedRoles);
        if(access.contains("DENY")) {
            log("Role Access Denied.");
            return "unassigned_role";
        }

        return "ALL_OKAY";
    }

    @RequestMapping(value="/doctor/vclinic", method = RequestMethod.GET)
    protected String doctorVClinic(final Map<String, Object> model, Principal principal, final HttpServletRequest req, final HttpServletResponse response) throws IOException {

        log(" ...   ");

        Role[] allowedRoles = new Role[]{Role.ROLE_PATIENT, Role.ROLE_DOCTOR};
        String tokenVerifiedViewStr = verifyToken(req, allowedRoles);
        if(!tokenVerifiedViewStr.equals("ALL_OKAY")) {
            return tokenVerifiedViewStr;
        }

        model.putAll(HomeController.getUserModel(req));

        final String CLINIC_CLOSED = EVisitRestController.DOC_VCLINIC_STATE.CLOSED.toString();

        String clinicStatus = (String) req.getSession().getAttribute("DOC_VCLINIC_STATE");

        if(clinicStatus == null) {
            model.put("DOC_VCLINIC_STATE", CLINIC_CLOSED);
        }
        else {
            model.put("DOC_VCLINIC_STATE", clinicStatus);
        }

        return "doctor/doctor_vclinic_main";

    }


}
