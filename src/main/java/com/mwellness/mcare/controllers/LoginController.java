package com.mwellness.mcare.controllers;

import com.auth0.NonceUtils;
import com.auth0.SessionUtils;
import com.mwellness.mcare.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class LoginController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AppConfig appConfig;

    @Autowired
    public LoginController(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @RequestMapping("/")
    /*public String showLoginOptionns() {
        return "/login_options";
    }*/
    public String redirectRoot(RedirectAttributes attributes) {
        return ("redirect:login");
    }

    @RequestMapping(value="/login", method = RequestMethod.GET)
    protected String login(final Map<String, Object> model, final HttpServletRequest req) {
        logger.info("Performing login");
        detectError(model);
        logger.info("No Errors");
        // add a Nonce value to session storage
        NonceUtils.addNonceToStorage(req);
        model.put("clientId", appConfig.getClientId());
        model.put("clientDomain", appConfig.getDomain());
        model.put("loginCallback", appConfig.getLoginCallback());
        model.put("state", SessionUtils.getState(req));
        return "auth0_login";
    }

    private void detectError(final Map<String, Object> model) {
        if (model.get("error") != null) {
            model.put("error", true);
        } else {
            model.put("error", false);
        }
    }


}
