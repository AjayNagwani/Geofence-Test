package com.test.geofenceservice;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

class Util {

    @RequiresApi(api = Build.VERSION_CODES.M)
    static void scheduleJob(Context context) {

        ComponentName serviceComponent = new ComponentName(context, ReceiverJobService.class);
        JobInfo.Builder builder;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder = new JobInfo.Builder(0, serviceComponent);
            builder.setMinimumLatency(60 * 1000); // wait at least
            builder.setOverrideDeadline(2 * 60 * 1000); // maximum delay
            JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
            if (jobScheduler != null) {
                jobScheduler.schedule(builder.build());
            }
        }

    }

}
