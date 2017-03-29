package edu.clemson.six.studybuddy.model;

/**
 * Created by jthollo on 3/28/2017.
 */

public class SubLocation {
    public static final SubLocation OTHER = new SubLocation(-1, "Other", null);

    private final int id;
    private final String name;
    private final Location parent;

    public SubLocation(int id, String name, Location parent) {
        this.id = id;
        this.name = name;
        this.parent = parent;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getParent() {
        return parent;
    }
}
