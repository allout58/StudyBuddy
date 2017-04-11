package edu.clemson.six.studybuddy.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by jthollo on 3/28/2017.
 */

public class Location {
    public static final Location OTHER = new Location(-1, 0, 0, 0, "Other");

    private final int locationID;
    private final double longitude, latitude;
    private final double radius;
    private final String name;
    private final List<SubLocation> sublocations;

    public Location(int id, double longitude, double latitude, double radius, String name) {
        this.locationID = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.radius = radius;
        this.name = name;
        this.sublocations = new ArrayList<>();
    }

    public int getId() {
        return locationID;
    }

    public String getName() {
        return name;
    }

    /**
     * Get a sublocation by it's id
     *
     * @param id ID of sublocation to find
     * @return The sublocation object, or null if not found;
     */
    public SubLocation getSubLocationByID(int id) {
        for (int i = 0; i < sublocations.size(); i++) {
            if (sublocations.get(i).getId() == id) {
                return sublocations.get(i);
            }
        }
        return null;
    }

    public SubLocation[] getSublocations() {
        SubLocation[] ret = new SubLocation[sublocations.size()];
        sublocations.toArray(ret);
        return ret;
    }

    public void addSubLocation(SubLocation l) {
        sublocations.add(l);
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getRadius() {
        return radius;
    }

    public double getMapRadius() {
        return radius/3.0;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "(%d) %s (lon:%f,lat:%f) Radius %f", locationID, name, longitude, latitude, radius);
    }
}
