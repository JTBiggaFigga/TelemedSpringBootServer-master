package com.mwellness.mcare.controllers;

import com.mwellness.mcare.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

@Controller
public class LogoutController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AppConfig appConfig;

    @Autowired
    public LogoutController(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @RequestMapping(value="/logout", method = RequestMethod.GET)
    protected String logout(final Map<String, Object> model, final HttpServletRequest request) {
        logger.debug("Performing logout");

        invalidateSession(request);

        final String logoutPath = appConfig.getOnLogoutRedirectTo();
        final String baseUrl = String.format("%s://%s:%d",request.getScheme(),  request.getServerName(), request.getServerPort());

        model.put("logoutPath", baseUrl + logoutPath);
        model.put("baseFedLogoutUrl", appConfig.getIssuer() + "v2/logout");


        return "logout";

    }

    private void invalidateSession(HttpServletRequest request) {
        if (request.getSession() != null) {
            request.getSession().invalidate();
        }
    }

}
