package edu.clemson.six.studybuddy.controller;

import java.util.HashMap;
import java.util.Map;

import edu.clemson.six.studybuddy.model.Location;

/**
 * Created by jthollo on 3/28/2017.
 */

public class LocationController {
    private static final LocationController instance = new LocationController();
    private Map<String, Location> locationMap;

    private LocationController() {
        locationMap = new HashMap<>();
    }

    public static LocationController getInstance() {
        return instance;
    }

    public void addLocation(Location l) {
        locationMap.put(l.getName(), l);
    }

    public Location getLocation(String name) {
        return locationMap.get(name);
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
        // Grab locations from the DB
    }
}
