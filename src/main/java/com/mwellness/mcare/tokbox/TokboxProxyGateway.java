package com.mwellness.mcare.tokbox;

import com.opentok.OpenTok;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by dev01 on 12/12/17.
 */
public class TokboxProxyGateway {

    @Value("${tokbox.apikey}")
    public static int API_KEY;

    @Value("${tokbox.secret}")
    private static String SECRET;


    // opentok singleton
    private volatile static OpenTok openTok;

    public static OpenTok getOpenTokInstance() {

        if(openTok == null) {
            openTok = new OpenTok(API_KEY, SECRET);
        }
        return openTok;
    }

}
