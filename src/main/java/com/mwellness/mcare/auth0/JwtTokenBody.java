package com.mwellness.mcare.auth0;

/**
 * Created by dev01 on 3/28/17.
 */

public class JwtTokenBody {
    
    public Auth0AppMetaData app_metadata = new Auth0AppMetaData();
    public String aud = "";
    public String email = "";
    public boolean email_verified;
    public long exp;
    public long iat;
    public String iss = "";
    public String name = "";
    public String nickname = "";
    public String picture = "";
    public String sub = "";
    public String user_id = "";

    public Auth0AppMetaData getApp_metadata() {
        return app_metadata;
    }

    public void setApp_metadata(Auth0AppMetaData app_metadata) {
        this.app_metadata = app_metadata;
    }

    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmail_verified() {
        return email_verified;
    }

    public void setEmail_verified(boolean email_verified) {
        this.email_verified = email_verified;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public long getIat() {
        return iat;
    }

    public void setIat(long iat) {
        this.iat = iat;
    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
