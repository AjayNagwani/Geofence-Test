package com.test.geofenceservice;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.renderscript.RenderScript;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForegroundService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private Timer timer = null;
    int count = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Called", "onCreateService");


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        final Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setAutoCancel(true)
                .setOngoing(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
        startTimer();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (!isMyServiceRunning(getApplicationContext(), ForegroundService.class)) {
                    Intent intent1 = new Intent(getApplicationContext(),ForegroundService.class);
                    getApplicationContext().stopService(intent1);
                    stopSelf();
                    stoptimertask();
                    Log.d("ForeGround service", "Stopped at " + Calendar.getInstance().getTime());
                    pingGeo("Stopped at " + Calendar.getInstance().getTime());
                    boolean b = isMyServiceRunning(getApplicationContext(), ForegroundService.class);
                    Log.e("Service status", String.valueOf(b));

                }
            }
        }, 10 * 60 * 1000);
        //

        return START_NOT_STICKY;
    }

    public void startTimer() {

        if(timer == null) {

            timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                public void run() {
                    if(count != 0){
                        Intent intent = new Intent(getApplicationContext(), GeoBroadcastReceiver.class);
                        intent.setAction(GeoBroadcastReceiver.ACTION_PROCESS_UPDATES);
                        PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    }
                    count++;
                    Log.e("Timer not null", String.valueOf(count));
              /*    */
                  //  pingGeo("Running at " + Calendar.getInstance().getTime() + "  " + timer.toString());
                }
            };
            timer.schedule(timerTask, 15000, 15000); //
        }
        else {
            TimerTask timerTask = new TimerTask() {
                public void run() {
                   // pingGeo("Running at " + Calendar.getInstance().getTime() + "  " + timer.toString());
                    if(count != 0){
                        Intent intent = new Intent(getApplicationContext(), GeoBroadcastReceiver.class);
                        intent.setAction(GeoBroadcastReceiver.ACTION_PROCESS_UPDATES);
                        PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    }
                    count++;
                    Log.e("Timer null", String.valueOf(count));
                }
            };
            timer.schedule(timerTask, 15000, 15000);
        }

    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service status", "Running");
               // pingGeo(serviceClass.getName());
                //MyWorker.stopService();
                //      Toast.makeText(mContext,"Service Running",Toast.LENGTH_SHORT).show();

                return false;
            }
        }
        Log.i("Service status", "Not running");
      //  pingGeo("Not Running");
        // Toast.makeText(mContext,"Service Not Running",Toast.LENGTH_SHORT).show();

        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void cancelNotification(Context ctx, String notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.deleteNotificationChannel(notifyId);
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
        model.setMessage(message);
        model.setDevice_info(object.toString());
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        startTimer();
        Log.e("Called", "onTaskRemovedService");

    }



    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }

        }
    }
}