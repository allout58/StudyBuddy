package edu.clemson.six.studybuddy.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import edu.clemson.six.studybuddy.controller.LocationController;
import edu.clemson.six.studybuddy.controller.UserLocationController;
import edu.clemson.six.studybuddy.controller.sql.LocalDatabaseController;

/**
 * Service to manage tracking the user's location
 */

public class LocationTrackingService extends Service {
    private static final int UPDATE_WAIT_TIME = 4000; // In milliseconds
    private static final float UPDATE_MIN_DIST = 3; // In meters
    private static final String TAG = "LocationTrackingService";
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Starting LocationTracking Service");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_WAIT_TIME, UPDATE_MIN_DIST, locationListener);
            locationListener.onLocationChanged(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        } else {
            Log.d(TAG, "Unable to get GPS Location permission");
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_WAIT_TIME, UPDATE_MIN_DIST, locationListener);
            locationListener.onLocationChanged(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        } else {
            Log.e(TAG, "Unable to get Network Location permission");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Stopping LocationTracking Service");
        locationManager.removeUpdates(locationListener);
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            float minDist = 9000;
            Log.d(TAG, "Location Changed");
            Log.d(TAG, "Firebase User: " + (FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "None"));
            if (LocationController.getInstance().getAllLocations() == null) {
                LocalDatabaseController.getInstance(LocationTrackingService.this);
                LocationController.getInstance().reload();
            }
            for (edu.clemson.six.studybuddy.model.Location loc : LocationController.getInstance().getAllLocations()) {
                Log.d(TAG, "Location: " + loc.toString());
                boolean hasCurrent = UserLocationController.getInstance().getCurrentLocation() != null;

                Location point = new Location("dist");
                point.setLongitude(loc.getLongitude());
                point.setLatitude(loc.getLatitude());

                minDist = Math.min(minDist, location.distanceTo(point));

                if (location.distanceTo(point) < loc.getMapRadius() && !hasCurrent) {
                    UserLocationController.getInstance().setCurrentLocation(loc);
//                    FirebaseAuth.getInstance().getCurrentUser().getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<GetTokenResult> task) {
//                            MapActivity.ChangeLocationTask t = new MapActivity.ChangeLocationTask();
//                            t.execute(task.getResult().getToken());
//                        }
//                    });
                    Toast.makeText(getBaseContext(), loc.getName() + " Radius Entered", Toast.LENGTH_LONG).show();
                    break;
                } else if (location.distanceTo(point) > loc.getMapRadius() + 15 && hasCurrent && UserLocationController.getInstance().getCurrentLocation() == loc) {
                    Toast.makeText(getBaseContext(), loc.getName() + " Radius Exited", Toast.LENGTH_LONG).show();
                    break;
                }
            }
//            if (minDist > LocationController.getInstance().getMaxRadius() + 30) {
//                locationManager.removeUpdates(this);
//
//            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}
