package com.mwellness.mcare.auth0;

/**
 * Created by dev01 on 3/28/17.
 */
public class JwtTokenHeader {

    public String typ = "";
    public String alg = "";
    public String kid = "";



    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }
}
