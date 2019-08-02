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

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Demonstrates how to create and remove geofences using the GeofencingApi. Uses an IntentService
 * to monitor geofence transitions and creates notifications whenever a device enters or exits
 * a geofence.
 * <p>
 * This sample requires a device's Location settings to be turned on. It also requires
 * the ACCESS_FINE_LOCATION permission, as specified in AndroidManifest.xml.
 * <p>
 */
public class MainActivity extends AppCompatActivity implements OnCompleteListener<Void> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    List<Chap> chapList = new ArrayList<>();
    private APIInterface apiInterface;
    private final List<String> latitude = new ArrayList<>();
    private final List<String> longitude = new ArrayList<>();
    private final List<Integer> types = new ArrayList<>();
    private final List<Integer> iD = new ArrayList<>();
    /**
     * Tracks whether the user requested to add or remove geofences, or to do neither.
     */
    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE
    }

    /**
     * Provides access to the Geofencing API.
     */
    private GeofencingClient mGeofencingClient;

    /**
     * The list of geofences used in this sample.
     */
    private ArrayList<Geofence> mGeofenceList;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;

    // Buttons for kicking off the process of adding or removing geofences.
    private Button mAddGeofencesButton;
    private Button mRemoveGeofencesButton;
    private GeoBroadcastReceiver receiver = null;
    private static final Intent[] POWERMANAGER_INTENTS = {
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"))};

    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the UI widgets.
       /* mAddGeofencesButton = (Button) findViewById(R.id.add_geofences_button);
        // mRemoveGeofencesButton = (Button) findViewById(R.id.remove_geofences_button);

        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<>();

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        // Get the geofences used. Geofence data is hard coded in this sample.
       placeChap();
        //placeChap();
        mGeofencingClient = LocationServices.getGeofencingClient(this);*/
        if (!StaticDataHelper.getProtectedStatus(getApplicationContext())) {
            for (final Intent intent : POWERMANAGER_INTENTS)
                if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Enable Battery optimisation").setMessage("For proper functioning of app, allow the app to ignore battery optimisation.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(intent);
                                    StaticDataHelper.setProtectedStatus(getApplicationContext(), true);
                                    //sp.edit().putBoolean("protected",true).apply();

                                }
                            })
                            .setCancelable(false)
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create().show();
                    break;
                }
        }
        // regRec();
        final PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(MyWorker.class, 15, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance().enqueue(periodicWorkRequest);

    }

    /*private void regRec() {
        IntentFilter intentFilter = new IntentFilter();

        // Add network connectivity change action.
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction(GeoBroadcastReceiver.ACTION_PROCESS_UPDATES);
        // Set broadcast receiver priority.
        intentFilter.setPriority(100);

        // Create a network change broadcast receiver.
        if (receiver == null) {
            receiver = new GeoBroadcastReceiver();

            // Register the broadcast receiver with the intent filter object.
            registerReceiver(receiver, intentFilter);
            Log.e("Receiver", "Registered");
            pingGeo("Registered");
        }
    }*/

    @Override
    public void onStart() {
        super.onStart();

       /* if (!checkPermissions()) {
            requestPermissions();
        } else {
            performPendingGeofenceTask();
        }*/
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
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

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    public void addGeofencesButtonHandler() {
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.ADD;
            requestPermissions();
            return;
        }

        addGeofences();

    }

    /**
     * Adds geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private void addGeofences() {
        if (!checkPermissions()) {
            showSnackbar(getString(R.string.insufficient_permissions));
            return;
        }


        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnCompleteListener(this);
        // Alarm.setAlarm(getApplicationContext(), true);

    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    public void removeGeofencesButtonHandler() {
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.REMOVE;
            requestPermissions();
            return;
        }
        removeGeofences();
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private void removeGeofences() {
        if (!checkPermissions()) {
            showSnackbar(getString(R.string.insufficient_permissions));
            return;
        }

        mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this);
    }

    /**
     * Runs when the result of calling {@link #addGeofences()} and/or {@link #removeGeofences()}
     * is available.
     *
     * @param task the resulting Task, containing either a result or error.
     */
    @Override
    public void onComplete(@NonNull Task<Void> task) {
        mPendingGeofenceTask = PendingGeofenceTask.NONE;
        if (task.isSuccessful()) {
            updateGeofencesAdded(!getGeofencesAdded());
            setButtonsEnabledState();

            int messageId = getGeofencesAdded() ? R.string.geofences_added :
                    R.string.geofences_removed;

            Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this, task.getException());
            Log.w(TAG, errorMessage);
        }
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeoBroadcastReceiver.class);
        intent.setAction(GeoBroadcastReceiver.ACTION_PROCESS_UPDATES);
        mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    private void populateGeofenceList() {

        for (int i = 0; i< latitude.size(); i++) {

            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(String.valueOf(iD.get(i)))
                    .setNotificationResponsiveness(2 * 60 * 1000)
                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            Double.parseDouble(latitude.get(i)),
                            Double.parseDouble( longitude.get(i)),
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                    // Create the geofence.
                    .build());
        }
        addGeofencesButtonHandler();
        Log.e("Geofence List",mGeofenceList.toString());

    }

    private void placeChap() {
        apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.getChapsNearby("chap", 26, 75, "5000", "djdi4583wickdps","be09e496e68d0147", "FGzekp7mSalMechj844F");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.e("Response URL", String.valueOf(response.raw().request().url()));

                    try {

                        if (response.body() != null) {
                            Log.e("Response map", response.body().toString());
                        }
                        String responseString;
                        if (response.body() != null) {
                            responseString = response.body().string();


                            Log.e("Response map", responseString);
                            Gson gson = new Gson();
                            Chaps chaps = gson.fromJson(responseString, Chaps.class);
                            int count = chaps.getCount();
                            Log.e("Count", String.valueOf(count));
                            chapList.clear();
                            for (int i = 0; i < count; i++) {
                                String lats = chaps.getChaps().get(i).getLatitude();
                                String lngs = chaps.getChaps().get(i).getLongitude();
                                Integer type2 = chaps.getChaps().get(i).getType();
                                Integer id = chaps.getChaps().get(i).getId();
                                latitude.add(i, lats);
                                longitude.add(i, lngs);
                                types.add(i, type2);
                                iD.add(i, id);

                            }
                            Log.e("Latitude", String.valueOf(latitude));
                            Log.e("Longitude", String.valueOf(longitude));
                            populateGeofenceList();
                         //   Log.e("ChapList", String.valueOf(chaps));

                        }
                    } catch (IOException e) {
                        Log.e("Error", e.getMessage());
                    }


                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                call.cancel();
            }


        });
    }
        /**
         * Ensures that only one button is enabled at any time. The Add Geofences button is enabled
         * if the user hasn't yet added geofences. The Remove Geofences button is enabled if the
         * user has added geofences.
         */
        private void setButtonsEnabledState () {
            if (getGeofencesAdded()) {
                mAddGeofencesButton.setEnabled(false);
                //mRemoveGeofencesButton.setEnabled(true);
            } else {
                mAddGeofencesButton.setEnabled(true);
//            mRemoveGeofencesButton.setEnabled(false);
            }
        }

        /**
         * Shows a {@link Snackbar} using {@code text}.
         *
         * @param text The Snackbar text.
         */
        private void showSnackbar ( final String text){
            View container = findViewById(android.R.id.content);
            if (container != null) {
                Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
            }
        }

        /**
         * Shows a {@link Snackbar}.
         *
         * @param mainTextStringId The id for the string resource for the Snackbar text.
         * @param actionStringId   The text of the action item.
         * @param listener         The listener associated with the Snackbar action.
         */
        private void showSnackbar ( final int mainTextStringId, final int actionStringId,
        View.OnClickListener listener){
            Snackbar.make(
                    findViewById(android.R.id.content),
                    getString(mainTextStringId),
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(actionStringId), listener).show();
        }

        /**
         * Returns true if geofences were added, otherwise false.
         */
        private boolean getGeofencesAdded () {
            return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                    Constants.GEOFENCES_ADDED_KEY, false);

        }

        /**
         * Stores whether geofences were added ore removed in {@link SharedPreferences};
         *
         * @param added Whether geofences were added or removed.
         */
        private void updateGeofencesAdded ( boolean added){
            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putBoolean(Constants.GEOFENCES_ADDED_KEY, added)
                    .apply();
        }

        /**
         * Performs the geofencing task that was pending until location permission was granted.
         */
        private void performPendingGeofenceTask () {
            if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
                addGeofences();
            } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
                removeGeofences();
            }
        }

        /**
         * Return the current state of the permissions needed.
         */
        private boolean checkPermissions () {
            int permissionState = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            return permissionState == PackageManager.PERMISSION_GRANTED;
        }

        private void requestPermissions () {
            boolean shouldProvideRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION);

            // Provide an additional rationale to the user. This would happen if the user denied the
            // request previously, but didn't check the "Don't ask again" checkbox.
            if (shouldProvideRationale) {
                Log.i(TAG, "Displaying permission rationale to provide additional context.");
                showSnackbar(R.string.permission_rationale, android.R.string.ok,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Request permission
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_PERMISSIONS_REQUEST_CODE);
                            }
                        });
            } else {
                Log.i(TAG, "Requesting permission");
                // Request permission. It's possible this can be auto answered if device policy
                // sets the permission in a given state or the user denied the permission
                // previously and checked "Never ask again".
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSIONS_REQUEST_CODE);
            }
        }

        @Override
        protected void onPause () {
            super.onPause();

        }

        @Override
        protected void onDestroy () {
            super.onDestroy();
            //unrRec();
        }

    /* private void unrRec() {
         if(receiver!=null)
         {
             unregisterReceiver(receiver);
             Log.d("Receiver", "onDestroy: screenOnOffReceiver is unregistered.");
             pingGeo("Unr                        egistered");

         }
     }
 */
        @Override
        protected void onStop () {
            super.onStop();

        }

        @Override
        protected void onResume () {
            super.onResume();

        }

        /**
         * Callback received when a permissions request has been completed.
         */
        @Override
        public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults){
            Log.i(TAG, "onRequestPermissionResult");
            if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
                if (grantResults.length <= 0) {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.");
                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission granted.");
                    performPendingGeofenceTask();
                } else {
                    // Permission denied.

                    // Notify the user via a SnackBar that they have rejected a core permission for the
                    // app, which makes the Activity useless. In a real app, core permissions would
                    // typically be best requested during a welcome-screen flow.

                    // Additionally, it is important to remember that a permission might have been
                    // rejected without asking the user for permission (device policy or "Never ask
                    // again" prompts). Therefore, a user interface affordance is typically implemented
                    // when permissions are denied. Otherwise, your app could appear unresponsive to
                    // touches or interactions which have required permissions.
                    showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Build intent that displays the App settings screen.
                                    Intent intent = new Intent();
                                    intent.setAction(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package",
                                            BuildConfig.APPLICATION_ID, null);
                                    intent.setData(uri);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            });
                    mPendingGeofenceTask = PendingGeofenceTask.NONE;
                }
            }
        }
    }
