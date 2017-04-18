package edu.clemson.six.studybuddy.controller;

import android.util.SparseArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.clemson.six.studybuddy.controller.sql.LocalDatabaseController;
import edu.clemson.six.studybuddy.model.Location;
import edu.clemson.six.studybuddy.model.SubLocation;

/**
 * Created by jthollo on 3/28/2017.
 */

public class LocationController {
    private static final LocationController instance = new LocationController();
    private Map<String, Location> locationMap;
    private SparseArray<Location> locationIDMap;
    private Location[] locations;

    private Location currentLocation = null;
    private SubLocation currentSubLocation = null;

    private LocationController() {
        locationMap = new HashMap<>();
        locationIDMap = new SparseArray<>();
    }

    public static LocationController getInstance() {
        return instance;
    }

    public Location getLocationByName(String name) {
        return locationMap.get(name);
    }

    public Location getLocationById(int id) {
        return locationIDMap.get(id);
    }

    public Location getLocation(int pos) {
        if (locations == null) {
            getAllLocations();
        }
        return locations[pos];
    }

    public int size() {
        return locationMap.size();
    }

    public Location[] getAllLocations() {
        return locations;
    }

    private void populateLocationsArray() {
        if (locations == null) {
            locations = new Location[locationMap.size()];
            int i = 0;
            for (Map.Entry<String, Location> entry : locationMap.entrySet()) {
                locations[i++] = entry.getValue();
            }
        }
    }

    public void reload() {
        locationMap.clear();
        locationIDMap.clear();
        locations = null;
        // Grab locations from the DB
        List<Location> l = LocalDatabaseController.getInstance(null).getLocations();
        for (Location loc : l) {
            locationMap.put(loc.getName(), loc);
            locationIDMap.put(loc.getId(), loc);
        }
        // Update the current location to the new object
        if (currentLocation != null) {
            currentLocation = locationIDMap.get(currentLocation.getId());
            if (currentSubLocation != null) {
                currentSubLocation = currentLocation.getSubLocationByID(currentSubLocation.getId());
            }
        }
        populateLocationsArray();
    }

    public void setCurrentLocation(Location location) {
        this.currentLocation = location;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(int ndx) {
        this.currentLocation = locations[ndx];
    }

    public SubLocation getCurrentSubLocation() {
        return currentSubLocation;
    }

    public void setCurrentSubLocation(SubLocation subLocation) {
        this.currentSubLocation = subLocation;
    }
}
