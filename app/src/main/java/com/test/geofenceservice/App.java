package com.test.geofenceservice;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class App extends Application {

    private GeoBroadcastReceiver receiver;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Called", "onCreateApp");

        final OneTimeWorkRequest periodicWorkRequest
                = new OneTimeWorkRequest.Builder(MyWorker.class).build();

        WorkManager.getInstance().enqueue(periodicWorkRequest);
        //Util.scheduleJob(getApplicationContext());

    }

    public void regRec() {
        IntentFilter intentFilter = new IntentFilter();

        // Add network connectivity change action.
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        intentFilter.addAction(GeoBroadcastReceiver.ACTION_PROCESS_UPDATES);
        // Set broadcast receiver priority.
        intentFilter.setPriority(100);

        // Create a network change broadcast receiver.
        if (receiver == null) {
            receiver = new GeoBroadcastReceiver();

            // Register the broadcast receiver with the intent filter object.
            registerReceiver(receiver, intentFilter);
            StaticDataHelper.saveReceiverStatus(getApplicationContext(), true);
            Log.e("Receiver", "Registered");
            pingGeo("Registered");
        }
    }

    private void pingGeo(String message) {
        APIInterface apiInterface1 = APIClient.getClient1().create(APIInterface.class);
        JSONObject object = new JSONObject();
        try {
            object.put("Manufacturer", Build.MANUFACTURER);
            object.put("Version", Build.VERSION.RELEASE);
            object.put("Model", Build.MODEL);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        NotificationModel model = new NotificationModel();
        Log.e("Api", "called");
        // model.setDevice_id(StaticDataHelper.getDeviceIdFromPrefs(getApplicationContext()));
        model.setMessage(message);
        model.setDevice_info(object.toString());
        //model = new NotificationModel(StaticDataHelper.getDeviceIdFromPrefs(getApplicationContext()), message);
        Call<ResponseBody> call = apiInterface1.notify1(model);
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

    public void unrRec() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            StaticDataHelper.saveReceiverStatus(getApplicationContext(), false);
            Log.d("Receiver", "onDestroy: screenOnOffReceiver is unregistered.");
            pingGeo("Unregistered");

        }
    }

}
