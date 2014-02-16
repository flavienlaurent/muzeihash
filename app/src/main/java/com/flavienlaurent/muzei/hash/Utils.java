package com.flavienlaurent.muzei.hash;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Flavien Laurent (flavienlaurent.com) on 16/02/14.
 *
 * c: https://github.com/PomepuyN/muzei-flickr/blob/master/MuzeiFlickr/src/main/java/com/npi/muzeiflickr/utils/Utils.java
 */
public class Utils {

    public static String convertDurationtoString(long duration) {
        int hour = (int) (duration / 3600000);
        int min = (int) ((duration - (hour * 3600000)) / 60000);
        int sec = (int) ((duration - (hour * 3600000) - (min * 60000)) / 1000);
        StringBuilder builder = new StringBuilder();
        builder.append(hour).append("h");
        builder.append(firstDigit(min));
        if (sec != 0)
            builder.append("m").append(firstDigit(sec)).append("s");
        return builder.toString();
    }

    private static String firstDigit(int min) {
        if (min < 10) {
            return "0" + String.valueOf(min);
        }
        return String.valueOf(min);
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return  mWifi.isConnected();
    }
}
