package edu.clemson.six.studybuddy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jthollo on 3/28/2017.
 */

public class Location {
    public static final Location OTHER = new Location(-1, 0, 0, 0, "Other");

    private final int id;
    private final double longitude, latitude;
    private final double radius;
    private final String name;
    private final List<SubLocation> sublocations;

    public Location(int id, double longitude, double latitude, double radius, String name) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.radius = radius;
        this.name = name;
        this.sublocations = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
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
}