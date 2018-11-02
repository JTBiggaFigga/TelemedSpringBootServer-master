package com.mwellness.mcare.auth0;

import com.auth0.Auth0Client;
import com.auth0.Auth0ClientImpl;
import com.auth0.Auth0User;
import com.mwellness.mcare.AMainApp;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * Created by dev01 on 12/19/17.
 * https://qubitmed.auth0.com/api/v2/users/escaped(auth0userid)
 */
public class Auth0UserDirectoryProxy {



    String auth0UserId;

    private static void log(String str) {
        AMainApp.log(Auth0UserDirectoryProxy.class.getSimpleName() + ": " + str);

    }

    public Auth0UserDirectoryProxy(final String auth0UserId) {
        setAuth0UserId(auth0UserId);
    }

    public String getAuth0UserJson() throws IOException {


        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://qubitmed.auth0.com/api/v2/users/" + URLEncoder.encode(auth0UserId, "UTF-8"))
                .method("GET", null)
                .addHeader("content-type", "application/json")
                .addHeader("Authorization", "Bearer " + Auth0MgmtToken.getAuth0MgmtToken())
                .build();
        Response response = client.newCall(request).execute();
        String bodyStr = response.body().string();

        log(bodyStr);

        try {
            JSONObject obj = new JSONObject(bodyStr);
            if(obj.has("error")) {
                return null;
            }
            return bodyStr;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }


    public String getAuth0UserId() {
        return auth0UserId;
    }

    private void setAuth0UserId(String auth0UserId) {
        this.auth0UserId = auth0UserId;
    }

}
