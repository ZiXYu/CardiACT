package com.example.osorekoxuan.cardiact;

import android.content.Context;
import android.content.SharedPreferences;

import com.parse.Parse;

/**
 * Created by osorekoxuan on 16/2/17.
 */
public class Application extends android.app.Application {

    // Debugging switch
    public static final boolean APPDEBUG = false;

    // Debugging tag for the application
    public static final String APPTAG = "CardiACT";

    // Used to pass location from MainActivity to PostActivity
    public static final String INTENT_EXTRA_LOCATION = "location";

    // Key for saving the search distance preference
    private static final String KEY_SEARCH_DISTANCE = "searchDistance";

    private static final float DEFAULT_SEARCH_DISTANCE = 600000.0f;

    private static SharedPreferences preferences;

    private static ConfigHelper configHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this);

        configHelper = new ConfigHelper();
        configHelper.fetchConfigIfNeeded();

        preferences = getSharedPreferences("com.parse.anywall", Context.MODE_PRIVATE);
    }

    public static float getSearchDistance() {
        return preferences.getFloat(KEY_SEARCH_DISTANCE, DEFAULT_SEARCH_DISTANCE);
    }

    public static ConfigHelper getConfigHelper() {
        return configHelper;
    }

    public static void setSearchDistance(float value) {
        preferences.edit().putFloat(KEY_SEARCH_DISTANCE, value).commit();
    }

}