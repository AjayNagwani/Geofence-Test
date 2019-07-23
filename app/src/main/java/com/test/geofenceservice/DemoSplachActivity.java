package com.test.geofenceservice;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.onesignal.OneSignal;

public class DemoSplachActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_splach);
        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .init();

        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                Log.d("debug", "User:" + userId);
                if (registrationId != null) {
                    Log.d("debug", "registrationId:" + registrationId);
                    StaticDataHelper.savePlayerID(getApplicationContext(),userId);
                }

            }
        });
        if(StaticDataHelper.getPlayerId(this) == null){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    Intent i = new Intent(DemoSplachActivity.this,MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }, 7000);
        }
        else {
            progressDialog.dismiss();
            Intent i = new Intent(DemoSplachActivity.this,MainActivity.class);
            startActivity(i);
            finish();
        }
    }
}
