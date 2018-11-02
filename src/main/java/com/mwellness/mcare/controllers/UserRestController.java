package com.mwellness.mcare.controllers;

import com.mwellness.mcare.auth0.Auth0MgmtToken;
import com.mwellness.mcare.responses.StandardResponse;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by dev01 on 8/14/17.
 */

@RestController
public class UserRestController {

    @CrossOrigin
    @RequestMapping("/portal/user")
    public @ResponseBody
    StandardResponse getUser (final HttpServletRequest req) throws IOException {

        String auth0UserId = req.getParameter("auth0UserId");

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://qubitmed.auth0.com/api/v2/users/"+auth0UserId)
                .get()
                .addHeader("authorization", "Bearer " + Auth0MgmtToken.getAuth0MgmtToken())
                .addHeader("cache-control", "no-cache")
                .build();
        Response response = client.newCall(request).execute();

        return new StandardResponse(response.body().string());
    }

}
