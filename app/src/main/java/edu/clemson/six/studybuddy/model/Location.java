package edu.clemson.six.studybuddy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jthollo on 3/28/2017.
 */

public class Location {
    public static final Location OTHER = new Location(-1, "Other");

    private final int id;
    private final String name;
    private final List<SubLocation> sublocations;

    public Location(int id, String name) {
        this.id = id;
        this.name = name;
        sublocations = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    private SubLocation[] getSublocations() {
        SubLocation[] ret = new SubLocation[sublocations.size()];
        sublocations.toArray(ret);
        return ret;
    }
}
