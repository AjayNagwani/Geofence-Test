package com.test.geofenceservice;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyWorker extends Worker implements OnCompleteListener<Void> {
    private Intent serviceIntent = null;
    List<Chap> chapList = new ArrayList<>();
    private APIInterface apiInterface;
    private final List<String> latitude = new ArrayList<>();
    private final List<String> longitude = new ArrayList<>();
    private final List<Integer> types = new ArrayList<>();
    private final List<Integer> iD = new ArrayList<>();
    private GeofencingClient mGeofencingClient;

    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE
    }

    private static final String TAG = MyWorker.class.getSimpleName();
    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;
    /**
     * The list of geofences used in this sample.
     */
    private ArrayList<Geofence> mGeofenceList;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;
    Context mContext;

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;

    }


    @NonNull
    @Override
    public Result doWork() {
        if (!StaticDataHelper.getGeofenceFlag(mContext)) {
            mGeofenceList = new ArrayList<>();

            // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
            mGeofencePendingIntent = null;

            // Get the geofences used. Geofence data is hard coded in this sample.
            placeChap();
            //placeChap();
            mGeofencingClient = LocationServices.getGeofencingClient(mContext);
        }
        else
        {
            serviceIntent = new Intent(getApplicationContext(), ForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplicationContext().startForegroundService(serviceIntent);
                Log.e("Servicee", "Started");
            } else {
                getApplicationContext().startService(serviceIntent);
                Log.e("Servicee", "Started");

            }
        }


        return Result.SUCCESS;

    }

    private void placeChap() {
        apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.getChapsNearby("chap", 26, 75, "5000", "djdi4583wickdps", "be09e496e68d0147", "FGzekp7mSalMechj844F");
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

    private void populateGeofenceList() {

        for (int i = 0; i < latitude.size(); i++) {

            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(String.valueOf(iD.get(i)))
                    .setNotificationResponsiveness(2 * 60 * 1000)
                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            Double.parseDouble(latitude.get(i)),
                            Double.parseDouble(longitude.get(i)),
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
        Log.e("Geofence List", mGeofenceList.toString());

    }

    public void addGeofencesButtonHandler() {
        mPendingGeofenceTask = PendingGeofenceTask.ADD;
        addGeofences();

    }

    @SuppressLint("MissingPermission")
    private void addGeofences() {

        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnCompleteListener(this);


    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        mPendingGeofenceTask = PendingGeofenceTask.NONE;
        if (task.isSuccessful()) {

            int messageId = getGeofencesAdded() ? R.string.geofences_added :
                    R.string.geofences_removed;

            serviceIntent = new Intent(getApplicationContext(), ForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplicationContext().startForegroundService(serviceIntent);
                Log.e("Servicee", "Started");
            } else {
                getApplicationContext().startService(serviceIntent);
                Log.e("Servicee", "Started");

            }

            // Toast.makeText(this, mContext.getString(messageId), Toast.LENGTH_SHORT).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(mContext, task.getException());
            Log.w(TAG, errorMessage);
        }
    }

    private boolean getGeofencesAdded() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(
                Constants.GEOFENCES_ADDED_KEY, false);

    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeoBroadcastReceiver.class);
        intent.setAction(GeoBroadcastReceiver.ACTION_PROCESS_UPDATES);
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
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
        // model.setDevice_id(StaticDataHelper.getDeviceIdFromPrefs(mContext));
        model.setMessage(message);
        model.setDevice_info(object.toString());
        //model = new NotificationModel(StaticDataHelper.getDeviceIdFromPrefs(mContext), message);
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


}