package edu.clemson.six.studybuddy.model;


/**
 * Models a sublocation
 */
public class SubLocation {
    public static final SubLocation OTHER = new SubLocation(-1, "-Other-", null);

    private final int subID;
    private final String name;
    private final Location parent;

    public SubLocation(int id, String name, Location parent) {
        this.subID = id;
        this.name = name;
        this.parent = parent;
    }

    public int getId() {
        return subID;
    }

    public String getName() {
        return name;
    }

    public Location getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return name;
    }
}
