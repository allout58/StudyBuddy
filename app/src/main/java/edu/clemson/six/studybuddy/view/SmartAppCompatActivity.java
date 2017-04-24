package edu.clemson.six.studybuddy.view;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import edu.clemson.six.studybuddy.service.LocationTrackingService;


public abstract class SmartAppCompatActivity extends AppCompatActivity {
    @Override
    protected void onResume() {
        super.onResume();
        Intent getLTS = new Intent(this, LocationTrackingService.class);
        bindService(getLTS, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LocationTrackingService.LTSBinder binder = (LocationTrackingService.LTSBinder) service;
                binder.setApplicationActive(true);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_ABOVE_CLIENT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent getLTS = new Intent(this, LocationTrackingService.class);
        bindService(getLTS, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LocationTrackingService.LTSBinder binder = (LocationTrackingService.LTSBinder) service;
                binder.setApplicationActive(false);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_ABOVE_CLIENT);
    }
}
