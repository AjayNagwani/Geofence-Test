package com.test.geofenceservice;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Alarm extends BroadcastReceiver {
    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onReceive(Context context, Intent intent) {

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = null;
        if (pm != null) {
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        }
        if (wl != null) {
            wl.acquire(10 * 60 * 1000L /*10 minutes*/);
           // ping("Alarm");
            Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example
            Log.e("Alarm","Alert");
            wl.release();
        }
        AlarmJobIntentService.enqueueWork(context, intent);

        // Put here YOUR code.

    }
    private void ping(String message) {
        APIInterface apiInterface1 = APIClient.getClient1().create(APIInterface.class);
        JSONObject object = new JSONObject();
        try {
            object.put("Manufacturer",Build.MANUFACTURER);
            object.put("Version",Build.VERSION.RELEASE);
            object.put("Model",Build.MODEL);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        NotificationModel model = new NotificationModel();
        Log.e("Api","called");
        // model.setDevice_id(StaticDataHelper.getDeviceIdFromPrefs(getApplicationContext()));
        model.setMessage(message);
        model.setDevice_info(object.toString());
        //model = new NotificationModel(StaticDataHelper.getDeviceIdFromPrefs(getApplicationContext()), message);
        Call<ResponseBody> call = apiInterface1.notify(model);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.e("Url", String.valueOf(call.request().url()));
                if (response.isSuccessful()) {
                    Log.e("Response", response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Failure", t.getMessage());
                call.cancel();
            }


        });

    }
    public static void setAlarm(Context context,boolean b) {
        cancelAlarm(context);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        long t =  System.currentTimeMillis();
        long delay = 1000 * 60;
        if(!b){
            t+=delay;

        }
        if(Build.VERSION.SDK_INT <Build.VERSION_CODES.KITKAT ) {
            if (am != null) {
                am.set(AlarmManager.RTC_WAKEUP,t, pi);
            }
        }
        else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (am != null) {
                am.setExact(AlarmManager.RTC_WAKEUP, t, pi);
            }
        }
        else {
            if (am != null) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, pi);
            }

        }
         // Millisec * Second * Minute
    }

    public static void cancelAlarm(Context context) {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(sender);
        }
    }
}