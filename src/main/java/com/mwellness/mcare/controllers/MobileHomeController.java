package com.mwellness.mcare.controllers;

import com.auth0.Auth0User;
import com.auth0.SessionUtils;
import com.auth0.Tokens;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

/**
 * Created by dev01 on 3/28/17.
 */
@Controller
public class MobileHomeController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("/mobile/codereceiver")
    protected String home(final Map<String, Object> model, final HttpServletRequest req, final HttpServletResponse response) throws IOException {

        logger.info("");
        logger.info("====================================================================");
        logger.info("");

        logger.info(new Gson().toJson(req.getParameterMap()));
        //return ""; //new Gson().toJson(req.getParameterMap());

        String code = req.getParameter("code");



        model.put("code", code);

        return "mobile_code_receiver";
    }





    protected String home2(final Map<String, Object> model, final Principal principal, final HttpServletRequest req, final HttpServletResponse response) throws IOException {

        logger.info("");
        logger.info("====================================================================");
        logger.info("");
        logger.info(new Gson().toJson(SessionUtils.getAuth0User(req).getGroups()));
        logger.info("Home page");

        final Auth0User user = (Auth0User) principal;
        logger.info("Principal name: " + user.getName());


        Tokens tokens = SessionUtils.getTokens(req);


        String[] rolesArr = new Gson().fromJson(new Gson().toJson(user.getAppMetadata().get("roles")), String[].class);
        String rolesCsvStr = String.join(", ", rolesArr);
        if(rolesCsvStr.contains("unassigned")) {
            return "unassigned_role";
        }


        model.put("rolesCsv", rolesCsvStr);
        model.put("user", user);
        model.put("access_token", tokens.getAccessToken());
        model.put("id_token", tokens.getIdToken());
        return "mobilehome";
    }


}
