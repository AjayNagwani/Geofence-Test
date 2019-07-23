/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.test.geofenceservice;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

/**
 * Receiver for geofence transition changes.
 * <p>
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a JobIntentService
 * that will handle the intent in the background.
 */
public class GeoBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_PROCESS_UPDATES =
            "actionProcessUpdates";

    /**
     * Receives incoming intents.
     *
     * @param context the application context.
     * @param intent  sent by Location Services. This Intent is provided to Location
     *                Services (inside a PendingIntent) when addGeofences() is called.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        // Enqueues a JobIntentService passing the context and intent as parameters
        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals(ACTION_PROCESS_UPDATES)) {
               /* Intent myIntent = new Intent(context, LocationMonitoringService.class);
                context.startForegroundService(myIntent);*/
             //   checkAndStartService(context);
             //   Alarm.setAlarm(context,true);
                GeofenceTransitionsJobIntentService.enqueueWork(context, intent);

            }

        }
    }
 /*   private void checkAndStartService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isMyServiceRunning(context, LocationMonitoringService.class)) {
                context.startForegroundService(new Intent(context, LocationMonitoringService.class));
                Log.e("Servicee", "Started");

            }
        } else {
            if (isMyServiceRunning(context, LocationMonitoringService.class)) {
                context.startService(new Intent(context, LocationMonitoringService.class));
                Log.e("Servicee", "Started");



            }
        }
    }
    private boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service status", "Running");


                //      Toast.makeText(mContext,"Service Running",Toast.LENGTH_SHORT).show();

                return false;
            }
        }
        Log.i("Service status", "Not running");

        // Toast.makeText(mContext,"Service Not Running",Toast.LENGTH_SHORT).show();

        return true;
    }*/

}
