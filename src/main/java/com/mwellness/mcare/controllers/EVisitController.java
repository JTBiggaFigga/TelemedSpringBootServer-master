package com.mwellness.mcare.controllers;

import com.auth0.Auth0User;
import com.auth0.SessionUtils;
import com.auth0.Tokens;
import com.mwellness.mcare.AMainApp;
import com.mwellness.mcare.auth0.JwtTokenValidationUtility;
import com.mwellness.mcare.auth0.Role;
import com.mwellness.mcare.tokbox.TokboxProxyGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Controller to manage evisits between doctors and patients
 * Created by dev01 on 31/01/18.

 */


@Controller
public class EVisitController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private void log(String str) {
        AMainApp.log(EVisitController.class.getSimpleName() + ": " + str);
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


    @RequestMapping("/vclinic/videochat")
    public String runVideoChat (Map<String, Object> model,
                         @RequestParam("w") final int w,
                         @RequestParam("h") final int h,
                         @RequestParam("apiKey") final String apiKey,
                         @RequestParam("openTokSessionId") final String openTokSessionId,
                         @RequestParam("openTokToken") final String openTokToken
                        ) {

        model.put("w", w);
        model.put("h", h);
        model.put("apiKey", TokboxProxyGateway.API_KEY);
        model.put("openTokSessionId", openTokSessionId);
        model.put("openTokToken", openTokToken);

        return "videochat";
    }


}
