package com.flavienlaurent.muzei.hash;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PreferenceHelper {

    public static final int CONNECTION_WIFI = 0;
    public static final int CONNECTION_ALL = 1;

    public static final int MIN_FREQ_MILLIS = 3 * 60 * 60 * 1000;

    private static final int DEFAULT_FREQ_MILLIS = 24 * 60 * 60 * 1000;

    public static int getConfigConnection(Context context) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getInt("config_connection", CONNECTION_WIFI);
    }

    public static void setConfigConnection(Context context, int connection) {
        SharedPreferences preferences = getPreferences(context);
        preferences.edit().putInt("config_connection", connection).commit();
    }

    public static void setConfigFreq(Context context, int durationMillis) {
        SharedPreferences preferences = getPreferences(context);
        preferences.edit().putInt("config_freq", durationMillis).commit();
    }

    public static int getConfigFreq(Context context) {
        SharedPreferences preferences = getPreferences(context);
        return preferences.getInt("config_freq", DEFAULT_FREQ_MILLIS);
    }

    public static void limitConfigFreq(Context context) {
        int configFreq = getConfigFreq(context);
        if(configFreq < MIN_FREQ_MILLIS) {
            setConfigFreq(context, MIN_FREQ_MILLIS);
        }
    }

    public static List<String> tagsFromPref(Context context) {
        ArrayList<String> tags = new ArrayList<String>();
        SharedPreferences preferences = getPreferences(context);
        String prefTags = preferences.getString("tags", "[\"#photography\",\"#longexposure\"]");
        if(!TextUtils.isEmpty(prefTags)) {
            try {
                JSONArray jsonArray = new JSONArray(prefTags);
                for(int index = 0 ; index < jsonArray.length() ; index++) {
                    tags.add(jsonArray.getString(index));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return tags;
    }

    public static void tagsToPref(Context context, List<String> tags) {
        SharedPreferences preferences = getPreferences(context);
        JSONArray jsonArray = new JSONArray(tags);
        preferences.edit().putString("tags", jsonArray.toString()).commit();
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences("hash", Context.MODE_PRIVATE);
    }
}
