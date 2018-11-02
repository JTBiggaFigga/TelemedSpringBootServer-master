package com.mwellness.mcare.auth0;

/**
 * Created by dev01 on 3/28/17.
 */

public class JwtToken {

    public static final String ALGO_HS256 = "HS256";

    private String header;
    private String body;

    public JwtToken(String header, String body) {
        this.header = header;
        this.body = body;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
