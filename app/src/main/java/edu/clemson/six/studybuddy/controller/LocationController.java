package edu.clemson.six.studybuddy.controller;

import android.util.SparseArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.clemson.six.studybuddy.controller.sql.UnifiedDatabaseController;
import edu.clemson.six.studybuddy.model.Location;

/**
 * Created by jthollo on 3/28/2017.
 */

public class LocationController {
    private static final LocationController instance = new LocationController();
    private Map<String, Location> locationMap;
    private SparseArray<Location> locationIDMap;

    private LocationController() {
        locationMap = new HashMap<>();
        locationIDMap = new SparseArray<>();
    }

    public static LocationController getInstance() {
        return instance;
    }

    public Location getLocation(String name) {
        return locationMap.get(name);
    }

    public Location getLocation(int id) {
        return locationIDMap.get(id);
    }

    public Location[] getAllLocations() {
        Location[] ret = new Location[locationMap.size()];
        int i = 0;
        for (Map.Entry<String, Location> entry : locationMap.entrySet()) {
            ret[i++] = entry.getValue();
        }
        return ret;
    }

    public void reload() {
        locationMap.clear();
        locationIDMap.clear();
        // Grab locations from the DB
        List<Location> l = UnifiedDatabaseController.getInstance(null).getLocations();
        for (Location loc : l) {
            locationMap.put(loc.getName(), loc);
            locationIDMap.put(loc.getId(), loc);
        }
    }
}
