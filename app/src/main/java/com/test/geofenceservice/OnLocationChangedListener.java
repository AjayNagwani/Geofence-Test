package com.test.geofenceservice;

import android.location.Location;

/**
 * Created by krzysztofjackowski on 24/09/15.
 */
public interface OnLocationChangedListener {
    void onLocationChanged(Location currentLocation);
}
