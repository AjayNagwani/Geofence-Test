package com.test.geofenceservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

class StaticDataHelper {
    private static final String MY_PREFERENCES = "MyPrefs";
    private static final String PLAYERS_ID = "playersId";
    private static final String PROTECTED = "protected";
    private static final String RECEIVER_STATUS ="receiversStatus" ;

    public static void savePlayerID(Context context, String id) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(PLAYERS_ID, id);
        Log.e("Saved Players ID - ", id);
        editor.apply();

    }
    public static void saveReceiverStatus(Context context, boolean b) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(RECEIVER_STATUS, b);
        Log.e("Saved Status - ", String.valueOf(b));
        editor.apply();

    }
    public static boolean getReceiverStatus(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        boolean b = sharedpreferences.getBoolean(RECEIVER_STATUS, false);
        return b;
    }

    public static String getPlayerId(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        String b = sharedpreferences.getString(PLAYERS_ID, null);
        return b;
    }

    public static boolean getProtectedStatus(Context applicationContext) {
        SharedPreferences sharedpreferences = applicationContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        boolean b = sharedpreferences.getBoolean(PROTECTED, false);
        return b;
    }

    public static void setProtectedStatus(Context applicationContext, boolean b) {
        SharedPreferences sharedpreferences = applicationContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(PROTECTED, b);
        Log.e("Saved Protected- ", String.valueOf(b));
        editor.apply();
    }
}
