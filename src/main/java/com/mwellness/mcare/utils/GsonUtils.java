package com.mwellness.mcare.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * GSON Utility
 */
public class GsonUtils {

    private static Gson gson = new Gson();
    private static Gson gsonPrettyPrinting = new GsonBuilder().setPrettyPrinting().create();

    
    public static Gson getInstance() {
        return gson;
    }

    public static Gson getInstance(boolean prettyPrinting) {
        return gsonPrettyPrinting;
    }

}
