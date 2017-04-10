package edu.clemson.six.studybuddy.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.ButterKnife;
import edu.clemson.six.studybuddy.R;
import edu.clemson.six.studybuddy.controller.LocationController;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    LocationManager locationManager;
    LocationListener locationListener;
    private GoogleMap mMap;
    boolean inRange = false;
    int inRangeIndex = -1;

    //Manually add locations to the map
    public void populateMapLocations() {
        for (int i = 0; i < LocationController.getInstance().size(); i++) {
            mMap.addCircle(new CircleOptions()
                    .center(new LatLng(LocationController.getInstance().getAllLocations()[i].getLatitude(), LocationController.getInstance().getAllLocations()[i].getLongitude()))
                    .radius(LocationController.getInstance().getAllLocations()[i].getRadius()/3.0)
                    .strokeColor(Color.BLUE));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1) {

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

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

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateMap(location);
                for(int i=0; i<LocationController.getInstance().getAllLocations().length; i++) {
                    Location point = new Location("dist");
                            point.setLongitude(LocationController.getInstance().getLocation(i).getLongitude());
                            point.setLatitude(LocationController.getInstance().getLocation(i).getLatitude());

                    if(location.distanceTo(point)<LocationController.getInstance().getLocation(i).getRadius()/3.0 && !inRange) {
                        inRange = true;
                        inRangeIndex = i;
                        Toast.makeText(getBaseContext(), "Entered Radius", Toast.LENGTH_LONG).show();
                        break;
                    }
                    else if(location.distanceTo(point)>LocationController.getInstance().getLocation(i).getRadius()/3.0+10 && inRange && inRangeIndex == i) {
                        inRange = false;
                        Toast.makeText(getBaseContext(), "Exited Radius", Toast.LENGTH_LONG).show();
                        break;
                    }
                }
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        else {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lastKnownLocation != null) {
                    updateMap(lastKnownLocation);
                }
            }
        }
        populateMapLocations();
    }
}