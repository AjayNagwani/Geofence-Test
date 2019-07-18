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
import android.graphics.Color;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class GeoService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;
    private DatabaseReference ref;
    private GeoFire geoFire;
    private LocationChangeListener mLocationChangeListener;
    private static final String TAG = GeoService.class.getSimpleName();


    // Is the service tracking time?
    private boolean isServiceRunning;

    // Foreground notification id
    private static final int NOTIFICATION_ID = 1;

    // Service binder
    private final IBinder serviceBinder = new RunServiceBinder();
    private GeoQuery geoQuery;

    public class RunServiceBinder extends Binder {
        GeoService getService() {
            return GeoService.this;
        }
    }

    @Override
    public void onCreate() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Creating service");
        }
        ref = FirebaseDatabase.getInstance().getReference("MyLocation");
        geoFire = new GeoFire(ref);
        isServiceRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Starting service");
        }
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Binding service");
        }
        return serviceBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Destroying service");
        }
    }

    /**
     * Starts the timer
     */
    public void startService(List<LatLng> dangers, double radius) {
        if (!isServiceRunning) {
            isServiceRunning = true;

        } else {
            Log.e(TAG, "startService request for an already running Service");

        }
        for(int i = 0; i < dangers.size(); i++ ) {

            if (geoQuery != null) {
                geoQuery.removeAllListeners();
            }
            geoQuery = geoFire.queryAtLocation(new GeoLocation(dangers.get(i).latitude, dangers.get(i).longitude), 0.002f);
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    //sendNotification("MRF", String.format("%s entered the dangerous area", key));
                    Log.e("MRF", String.format("%s entered the dangerous area", key));
                }

                @Override
                public void onKeyExited(String key) {
                    // sendNotification("MRF", String.format("%s exit the dangerous area", key));
                    Log.e("MRF", String.format("%s exit the dangerous area", key));
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    Log.d("MOVE", String.format("%s move within the dangerous area [%f/%f]", key, location.latitude, location.longitude));
                }

                @Override
                public void onGeoQueryReady() {

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    Log.d("ERROR", "" + error);
                }
            });
        }
    }

    /**
     * Stops the timer
     */
    public void stopService() {
        if (isServiceRunning) {
            isServiceRunning = false;
            geoQuery.removeAllListeners();
        } else {
            Log.e(TAG, "stopTimer request for a timer that isn't running");
        }
    }

    /**
     * @return whether the service is running
     */
    public boolean isServiceRunning() {
        return isServiceRunning;
    }


    /**
     * Place the service into the foreground
     */
    public void foreground() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
        //  startForeground(NOTIFICATION_ID, createNotification());
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String notificationChannelId = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(notificationChannelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        chan.setLightColor(Color.BLUE);
        chan.setSound(null, null);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.createNotificationChannel(chan);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), notificationChannelId);
        notification.setSound(null);
        startForeground(1, notification.build());
    }
    /**
     * Return the service to the background
     */
    public void background() {
        stopForeground(true);
    }

    /**
     * Creates a notification for placing the service into the foreground
     *
     * @return a notification for interacting with the service when in the foreground
     */
    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Service is Active")
                .setContentText("Tap to return to the Map")
                .setSmallIcon(R.mipmap.ic_launcher);

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        return builder.build();
    }

     /*   private void sendNotification(String title, String content) {
            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(content);

            NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(this, MapsActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            builder.setContentIntent(contentIntent);
            Notification notification = builder.build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults |= Notification.DEFAULT_SOUND;
            manager.notify(new Random().nextInt(), notification);
        }*/

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();
    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    interface LocationChangeListener {
        void onLocationChange(Location location);
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            final double latitude = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();

            geoFire.setLocation("You", new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (mLocationChangeListener!=null) {
                        mLocationChangeListener.onLocationChange(mLastLocation);
                    }
                }
            });

            Log.d("MRF", String.format("Your last location was chaged: %f / %f", latitude, longitude));
        } else {
            Log.d("MRF", "Can not get your location.");
        }
    }

    public void setLocationChangeListener(LocationChangeListener mLocationChangeListener) {
        this.mLocationChangeListener = mLocationChangeListener;
    }
}