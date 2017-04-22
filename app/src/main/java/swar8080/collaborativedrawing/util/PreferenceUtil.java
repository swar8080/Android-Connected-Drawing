package swar8080.collaborativedrawing.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 *
 */

public class PreferenceUtil {


    private static final String PREF_FILE_NAME = "PREF_FILE";
    private static final String SCREEN_NAME = "SCREEN_NAME";

    public static String getPrefScreenName(Context context){
        SharedPreferences preferences = context.getSharedPreferences(PREF_FILE_NAME,0);
        return preferences.getString(SCREEN_NAME, null);
    }

    public static void setPrefScreenName(Context context, String name){
        SharedPreferences.Editor prefEditor = context.getSharedPreferences(PREF_FILE_NAME,0).edit();
        prefEditor.putString(SCREEN_NAME, name);
        prefEditor.apply();
    }
}
