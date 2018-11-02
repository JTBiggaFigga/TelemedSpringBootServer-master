package com.mwellness.mcare.responses;


public class EcgUploadResponse {

    public int code;
    public String message;
    public ResponseCode responseCode;


    public EcgUploadResponse(final ResponseCode responseCode, final String message) {
        this.message = message;
        this.code = 200;
        this.responseCode = responseCode;
    }

}
