package com.test.geofenceservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

class StaticDataHelper {
    private static final String MY_PREFERENCES = "MyPrefs";
    private static final String PLAYERS_ID = "playersId";

    public static void savePlayerID(Context context, String id) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(PLAYERS_ID, id);
        Log.e("Saved Players ID - ", id);
        editor.apply();

    }
    public static String getPlayerId(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        String b = sharedpreferences.getString(PLAYERS_ID, " ");
        return b;
    }
}
