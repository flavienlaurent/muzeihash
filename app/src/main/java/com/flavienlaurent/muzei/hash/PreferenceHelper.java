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
