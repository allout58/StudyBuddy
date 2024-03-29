package edu.clemson.six.studybuddy.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import edu.clemson.six.studybuddy.R;
import edu.clemson.six.studybuddy.controller.LocationController;
import edu.clemson.six.studybuddy.controller.UserLocationController;
import edu.clemson.six.studybuddy.controller.net.APIConnector;
import edu.clemson.six.studybuddy.controller.net.ConnectionDetails;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    LocationManager locationManager;
    LocationListener locationListener;
    boolean inRange = false;
    //    int inRangeIndex = -1;
    private GoogleMap mMap;

    //Manually add locations to the map
    public void populateMapLocations() {
        for (int i = 0; i < LocationController.getInstance().size(); i++) {
            mMap.addCircle(new CircleOptions()
                    .center(new LatLng(LocationController.getInstance().getAllLocations()[i].getLatitude(), LocationController.getInstance().getAllLocations()[i].getLongitude()))
                    .radius(LocationController.getInstance().getAllLocations()[i].getMapRadius())
                    .strokeColor(Color.BLUE));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateMap(lastKnownLocation);
                }
            }
        }
    }

    public void updateMap(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17));
        mMap.addMarker(new MarkerOptions().title("Your Location").position(userLocation));
        populateMapLocations();
    }

    private void updateCurrentLocation(Location location) {
//        for (edu.clemson.six.studybuddy.model.Location loc : LocationController.getInstance().getAllLocations()) {
//            Location point = new Location("dist");
//            point.setLongitude(loc.getLongitude());
//            point.setLatitude(loc.getLatitude());
//
//            if (location.distanceTo(point) < loc.getMapRadius() && !inRange) {
//                inRange = true;
//                UserLocationController.getInstance().setCurrentLocation(loc);
//                FirebaseAuth.getInstance().getCurrentUser().getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<GetTokenResult> task) {
//                        ChangeLocationTask t = new ChangeLocationTask();
//                        t.execute(task.getResult().getToken());
//                    }
//                });
//                Toast.makeText(getBaseContext(), loc.getName() + " Radius Entered", Toast.LENGTH_LONG).show();
//                break;
//            } else if (location.distanceTo(point) > loc.getMapRadius() + 15 && inRange && UserLocationController.getInstance().getCurrentLocation() == loc) {
//                inRange = false;
//                Toast.makeText(getBaseContext(), loc.getName() + " Radius Exited", Toast.LENGTH_LONG).show();
//                break;
//            }
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ButterKnife.inject(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        populateMapLocations();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateMap(location);
                updateCurrentLocation(location);
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
        };

        if (Build.VERSION.SDK_INT < 23) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            } catch (SecurityException e) {
                Log.e("MapActivity", "Error getting position", e);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (lastKnownLocation != null) {
                updateMap(lastKnownLocation);
                updateCurrentLocation(lastKnownLocation);
            }

        }
    }

    private class ChangeLocationTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            Map<String, String> args = new HashMap<>();
            args.put("jwt", params[0]);
            args.put("locationID", String.valueOf(UserLocationController.getInstance().getCurrentLocation().getId()));
            Log.d("ChangeLocationTask", String.format("Sending location %s (%d) to the server", UserLocationController.getInstance().getCurrentLocation().getName(), UserLocationController.getInstance().getCurrentLocation().getId()));
            ConnectionDetails dets = APIConnector.setupConnection("user.set_location", args, ConnectionDetails.Method.POST);
            try {
                JsonElement el = APIConnector.connect(dets);
                // TODO Check for success for the API call??

            } catch (IOException e) {
                Log.e("MapActivity", "Error connecting to API", e);
            }
            return false;
        }
    }
}
