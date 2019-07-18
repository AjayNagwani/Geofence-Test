package com.test.geofenceservice;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LocationMonitoringService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, SensorEventListener {
    private static final String CHANNEL_ID = "channel_01";
    private Timer timer;
    private static final String TAG = "GeofenceTransitionsIS";
    private NotificationModel model;
    GoogleApiClient mLocationClient;
    LocationRequest mLocationRequest = new LocationRequest();

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void cancelNotification(Context ctx, String notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.deleteNotificationChannel(notifyId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        final String notificationChannelId = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(notificationChannelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        chan.setLightColor(Color.BLUE);
        chan.setSound(null, null);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), notificationChannelId);
        notification.setSound(null);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cancelNotification(LocationMonitoringService.this.getApplicationContext(), notificationChannelId);
            }
        }, 100);
        startForeground(2, notification.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("RUNNER : ", "\nPERFORMING....");
        startTimer();
        Log.d("RUNNER : ", "\nPERFORMING....");
        // locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
   //     mLocationRequest.setSmallestDisplacement(5);
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setMaxWaitTime(3 * 10 * 1000);

        int priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY; //by default
        //PRIORITY_BALANCED_POWER_ACCURACY, PRIORITY_LOW_POWER, PRIORITY_NO_POWER are the other priority modes


        mLocationRequest.setPriority(priority);
        mLocationClient.connect();
        Intent broadcastIntent = new Intent(this, GeoBroadcastReceiver.class);
        broadcastIntent.setAction(GeoBroadcastReceiver.ACTION_PROCESS_UPDATES);
        this.sendBroadcast(broadcastIntent);

        return START_STICKY;
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
        model = new NotificationModel();
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

    public void startTimer() {
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            public void run() {
                //Log.i("Count", "=========  " + (counter++));
            }
        };
        timer.schedule(timerTask, 1000, 1000); //


    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            Log.d(TAG, "== Error On onConnected() Permission not granted");
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, getPendingIntent());

        Log.d(TAG, "Connected to Google API");
    }
    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(getApplicationContext(), GeoBroadcastReceiver.class);
        intent.setAction(GeoBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended");
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed");

        if (location != null) {
            Log.d(TAG, "== location != null");
            String lat = String.valueOf(location.getLatitude());
            String lng = String.valueOf(location.getLongitude());
            /*cLocation = new Location(LocationManager.GPS_PROVIDER);
            cLocation.setLatitude(Double.parseDouble(lat));
            cLocation.setLongitude(Double.parseDouble(lng));*/
           // getLatLngNearby(cLocation);

        }

        if (location != null) {
          //  sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        }

    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Failed to connect to Google API");

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
//
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getGeofenceTransitionDetails(
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }
    @Override
    public void onDestroy() {
        stoptimertask();
        Log.d("RUNNER : ", "\nDestroyed....");
        Log.d("RUNNER : ", "\nWill be created again automaticcaly....");
        Intent broadcastIntent = new Intent(this, GeoBroadcastReceiver.class);
        broadcastIntent.setAction(GeoBroadcastReceiver.ACTION_PROCESS_UPDATES);
        this.sendBroadcast(broadcastIntent);
        super.onDestroy();


    }
    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private void sendNotification(String notificationDetails) {
        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.BLUE)
                .setContentTitle(notificationDetails)
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }

}
