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

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Constants used in this sample.
 */

final class Constants {

    private Constants() {
    }

    private static final String PACKAGE_NAME = "com.google.android.gms.location.Geofence";

    static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    private static final long GEOFENCE_EXPIRATION_IN_DAYS = 10;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_DAYS * 24 * 60 * 60 * 1000;
    static final float GEOFENCE_RADIUS_IN_METERS = 110;// 1 mile, 1.6 km

    /**
     * Map for storing information about airports in the San Francisco bay area.
     */
    static final HashMap<String, LatLng> BAY_AREA_LANDMARKS = new HashMap<>();

    static {
        BAY_AREA_LANDMARKS.put("Amrapali circle", new LatLng(26.911618, 75.743746));

        // Googleplex.
        BAY_AREA_LANDMARKS.put("Office",new LatLng(26.911137368989,75.73416775465));
        BAY_AREA_LANDMARKS.put("Talwarkars",new LatLng(26.9112357,75.7509412));
        BAY_AREA_LANDMARKS.put("gbc bolzano test",new LatLng(46.4925743,11.3444179));
        BAY_AREA_LANDMARKS.put("Twenty Test",new LatLng(46.4866881,11.3366539));
        BAY_AREA_LANDMARKS.put("Noi Techpark Test 4",new LatLng(46.4787568,11.3321426));
        BAY_AREA_LANDMARKS.put("Tobias Home Test",new LatLng(46.4964601,11.3488028));
        BAY_AREA_LANDMARKS.put("Uni bozen test",new LatLng(46.4977879,11.3516086));
        BAY_AREA_LANDMARKS.put("Museion test",new LatLng(46.4969253,11.3478758));
        BAY_AREA_LANDMARKS.put("Noi Techpark Test",new LatLng(46.4784444,11.3327442));
        BAY_AREA_LANDMARKS.put("Noi Techpark Test 3",new LatLng(46.4785054,11.3325034));

    }
}
