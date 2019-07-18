package com.test.geofenceservice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MyCurrentLocation implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = MyCurrentLocation.class.getSimpleName();

    Context context;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private OnLocationChangedListener onLocationChangedListener;
    private static final long UPDATE_INTERVAL = 5 * 1000;
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 3;

    public MyCurrentLocation(OnLocationChangedListener onLocationChangedListener) {
        this.onLocationChangedListener = onLocationChangedListener;
    }


    public synchronized void buildGoogleApiClient(Context context) {
        this.context = context;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(UPDATE_INTERVAL)        // 10 seconds, in milliseconds
                .setFastestInterval(FASTEST_UPDATE_INTERVAL) // 1 second, in milliseconds
                .setMaxWaitTime(MAX_WAIT_TIME);
    }

    public void start() {
        mGoogleApiClient.connect();
    }

    public void stop() {
        mGoogleApiClient.disconnect();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(Bundle bundle) {
        requestLocationUpdates();
      if(checkImpPermissions(context)) {
          LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
          mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                  mGoogleApiClient);
          if (mLastLocation != null) {
              onLocationChangedListener.onLocationChanged(mLastLocation);
          }
      }
    }
    public static boolean checkImpPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                + ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(context, GeoBroadcastReceiver.class);
        intent.setAction(GeoBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    public void requestLocationUpdates() {
        try {
            Log.i(TAG, "Starting location updates");
            LocationRequestHelper.setRequesting(context, true);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, getPendingIntent());
        } catch (SecurityException e) {
            LocationRequestHelper.setRequesting(context, false);
            e.printStackTrace();
        }
    }
    @Override
    public void onConnectionSuspended(int i) {
//
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("MyApp", "Location services connection failed with code " + connectionResult.getErrorCode());
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            onLocationChangedListener.onLocationChanged(mLastLocation);
        }

    }
}



