package com.mwellness.mcare.auth0;


import com.mwellness.mcare.AMainApp;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by dev01 on 8/14/17.
 */
public class Auth0MgmtToken {

    public static String auth0MgmtToken = "";
    public static long lastTokenFetchedAt = -1;
    public static long currTokenExpiresAt = -1;

    private static void log(String str) {
        AMainApp.log(Auth0MgmtToken.class + ": " + str);
    }

    public static String getAuth0MgmtToken() throws IOException {

        if(hasTokenExpired()) {
            auth0MgmtToken = fetchManagementToken();
        }

        return auth0MgmtToken;
    }

    private static boolean hasTokenExpired() {

        log("currTokenExpiresAt: " + currTokenExpiresAt);

        if(currTokenExpiresAt == -1) {
            return true;
        }

        if(currTokenExpiresAt < System.currentTimeMillis()) {
            return true;
        }
        else
        {
            if(auth0MgmtToken.equals("")) {
                return true;
            }
            else
                return false;
        }
    }

    private static String fetchManagementToken() throws IOException {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://qubitmed.auth0.com/oauth/token")
                .method("POST", RequestBody.create(null, new byte[0]))
                .addHeader("content-type", "application/json")
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "{\"grant_type\":\"client_credentials\",\"client_id\": \"DSXX4UCgvGf1t6WRWbJ0KBidx03IHRfe\",\"client_secret\": \"4K33i6E6_HKjIXe8yXpX4aXRtgu3LDIcKSUzdmh9C7WE-UE03O6a-kzJiGovZk3a\",\"audience\": \"https://qubitmed.auth0.com/api/v2/\"}"))
                .build();
        Response response = client.newCall(request).execute();
        String bodyStr = response.body().string();

        log(bodyStr);

        try {
            JSONObject obj = new JSONObject(bodyStr);
            if(obj.has("error")) {
                return "";
            }
            auth0MgmtToken = obj.getString("access_token");
            currTokenExpiresAt = System.currentTimeMillis() + obj.getLong("expires_in") - 600000;
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }

        return auth0MgmtToken;
    }

    /*
    private getTokenSettings = {
        "async": true,
        "crossDomain": true,
        "url": "https://qubitmed.auth0.com/oauth/token",
        "method": "POST",
        "headers": {
            "content-type": "application/json"
        },
        "data": "{\"client_id\":\"i18BKNEmyzUXHrN1Oy0xWsQWDDjavlkN\",\"client_secret\":\"YsXXPgKiauOXu8PNiopZMrGKJcrsUZqLYhgfiydTDU1zpRhMe4yh8bYrFHZU2g8s\",\"audience\":\"https://qubitmed.auth0.com/api/v2/\",\"grant_type\":\"client_credentials\"}"
    };
     */

}
