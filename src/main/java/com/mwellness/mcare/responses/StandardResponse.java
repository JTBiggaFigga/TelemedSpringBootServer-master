package com.mwellness.mcare.responses;

import com.google.gson.Gson;

public class StandardResponse {

    public int status = 200;
    public Object body = new Object();
    public boolean exception = false;

    public StandardResponse(Object responseBody) {

        if(responseBody instanceof Exception) {
            this.body = ((Exception) responseBody).getMessage();
            this.exception = true;
        }
        else {
            this.body = responseBody;
            this.exception = false;
        }
    }

    public String toJsonString() {
        return new Gson().toJson(this);
    }

}
