package edu.clemson.six.studybuddy.service;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import edu.clemson.six.studybuddy.Constants;
import edu.clemson.six.studybuddy.R;
import edu.clemson.six.studybuddy.controller.LocationController;
import edu.clemson.six.studybuddy.controller.UserLocationController;
import edu.clemson.six.studybuddy.controller.sql.LocalDatabaseController;
import edu.clemson.six.studybuddy.view.ChangeLocationActivity;

/**
 * Service to manage tracking the user's location
 */

public class LocationTrackingService extends Service {
    private static final int UPDATE_WAIT_TIME = 4000; // In milliseconds
    private static final float UPDATE_MIN_DIST = 3; // In meters
    private static final String TAG = "LocationTrackingService";
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LTSBinder binder = new LTSBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Bound: Returned binding " + binder.toString());
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Starting LocationTracking Service");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        Location last = null;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_WAIT_TIME, UPDATE_MIN_DIST, locationListener);
            last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (last != null) {
                locationListener.onLocationChanged(last);
            }
        } else {
            Log.e(TAG, "Unable to get GPS Location permission");
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_WAIT_TIME, UPDATE_MIN_DIST, locationListener);
            if (last == null) {
                last = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (last != null) {
                    locationListener.onLocationChanged(last);
                }
            }
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

    public static class LTSBinder extends Binder {
        private boolean isApplicationActive = true;

        public boolean isApplicationActive() {
            return isApplicationActive;
        }

        public void setApplicationActive(boolean applicationActive) {
            isApplicationActive = applicationActive;
        }
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
            Log.d(TAG, "Current Location: " + (UserLocationController.getInstance().getCurrentLocation() != null ? UserLocationController.getInstance().getCurrentLocation().getName() : "None"));
            boolean hasCurrent = UserLocationController.getInstance().getCurrentLocation() != null;
            for (edu.clemson.six.studybuddy.model.Location loc : LocationController.getInstance().getAllLocations()) {
                Log.d(TAG, "Location: " + loc.toString());

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
                    if (!binder.isApplicationActive()) {
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(LocationTrackingService.this)
                                        .setSmallIcon(R.drawable.ic_library_books_black_24dp)
                                        .setContentTitle("Are you in " + loc.getName() + "?")
                                        .setContentText("Click here to specify where you are and let your friends know you are here.");
                        // Creates an explicit intent for an Activity in your app
                        Intent resultIntent = new Intent(LocationTrackingService.this, ChangeLocationActivity.class);

                        // The stack builder object will contain an artificial back stack for the
                        // started Activity.
                        // This ensures that navigating backward from the Activity leads out of
                        // your application to the Home screen.
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(LocationTrackingService.this);
                        // Adds the back stack for the Intent (but not the Intent itself)
                        stackBuilder.addParentStack(ChangeLocationActivity.class);
                        // Adds the Intent that starts the Activity to the top of the stack
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setContentIntent(resultPendingIntent);

                        mBuilder.setAutoCancel(true);

                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        // mId allows you to update the notification later on.
                        mNotificationManager.notify(Constants.NOTIFICATION_LOCATION_CHANGE, mBuilder.build());
                    }

                    Toast.makeText(getBaseContext(), loc.getName() + " Radius Entered", Toast.LENGTH_LONG).show();
                    break;
                } else if (location.distanceTo(point) > loc.getMapRadius() + 15 && hasCurrent && UserLocationController.getInstance().getCurrentLocation() == loc) {
                    Toast.makeText(getBaseContext(), loc.getName() + " Radius Exited", Toast.LENGTH_LONG).show();
                    UserLocationController.getInstance().setCurrentLocation(null);
                    //TODO: Let server know we aren't there anymore
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(Constants.NOTIFICATION_LOCATION_CHANGE);
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
