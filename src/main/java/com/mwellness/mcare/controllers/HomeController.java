package com.mwellness.mcare.controllers;

import com.auth0.Auth0User;
import com.auth0.SessionUtils;
import com.auth0.Tokens;
import com.google.gson.Gson;
import com.opentok.Session;
import com.mwellness.mcare.AppConfig;
import com.mwellness.mcare.auth0.JwtTokenBody;
import com.mwellness.mcare.auth0.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private static AppConfig appConfig;

    @Autowired
    public HomeController(AppConfig appConfig) {
        this.appConfig = appConfig;
    }


    public static synchronized Map getUserModel(HttpServletRequest req) {

        Map<String, Object> model = new HashMap<>();

        final Auth0User user = SessionUtils.getAuth0User(req);
        logger.info("Principal name: " + user.getName());

        Tokens tokens = SessionUtils.getTokens(req);
        req.getSession().setAttribute("tokens", tokens);

        logger.info("social? " + user.getIdentities().get(0).isSocial());
        logger.info("social: name: " + user.getName());
        logger.info("not social: name: " + user.getUserMetadata().get("full_name"));

        String fullName = "";
        String fname = "";
        String lname = "";
        if(user.getIdentities().get(0).isSocial()) {
            fname = user.getGivenName();
            lname = user.getFamilyName();
            fullName = user.getName();
        }
        else {
            fname = (String) user.getUserMetadata().get("fname");
            lname = (String) user.getUserMetadata().get("lname");
            fullName = fname + " " + lname ;
        }

        String[] rolesArr = new Gson().fromJson(new Gson().toJson(user.getAppMetadata().get("roles")), String[].class);
        String rolesCsvStr = String.join(", ", rolesArr);

        final String logoutPath = appConfig.getOnLogoutRedirectTo();
        model.put("logoutPath", logoutPath);
        model.put("rolesCsv", rolesCsvStr);
        model.put("user", user);
        model.put("fname", fname);
        model.put("lname", lname);
        model.put("full_name", fullName);
        model.put("access_token", tokens.getAccessToken());   // insecure.
        model.put("id_token", tokens.getIdToken());           // insecure.
        model.put("tokenExpires", new Gson().fromJson(JwtUtils.decode(tokens.getIdToken()).getBody(), JwtTokenBody.class).exp);

        return model;
    }

    @RequestMapping(value="/portal/home", method = RequestMethod.GET)
    protected String home(final Map<String, Object> model, final Principal principal, final HttpServletRequest req, final HttpServletResponse response) throws IOException {
        logger.info("");
        logger.info("====================================================================");
        logger.info("");
        logger.info(new Gson().toJson(SessionUtils.getAuth0User(req).getGroups()));
        logger.info("Home page");

        final Auth0User user = (Auth0User) principal;
        logger.info("Principal name: " + user.getName());

        Tokens tokens = SessionUtils.getTokens(req);
        req.getSession().setAttribute("tokens", tokens);

        String[] rolesArr = new Gson().fromJson(new Gson().toJson(user.getAppMetadata().get("roles")), String[].class);
        String rolesCsvStr = String.join(", ", rolesArr);
        if(rolesCsvStr.contains("unassigned")) {
            return "unassigned_role";
        }

        model.putAll(getUserModel(req));

        if(rolesCsvStr.contains("doctor")) {
            return "doctor/doctor_home_main";
        }

        return "home";
    }




}
