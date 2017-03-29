package edu.clemson.six.studybuddy.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jthollo on 3/28/2017.
 */

public class Friend {
    private final String uid;
    private String name;
    private Location location;
    private SubLocation subLocation;
    private String blurb;
    private Date endTime;
    private boolean confirmed;

    public Friend(String uid, String name, Location location, SubLocation subLocation, String blurb, Date endTime, boolean confirmed) {
        this.uid = uid;
        this.name = name;
        this.location = location;
        this.subLocation = subLocation;
        this.blurb = blurb;
        this.endTime = endTime;
        this.confirmed = confirmed;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public SubLocation getSubLocation() {
        return subLocation;
    }

    public void setSubLocation(SubLocation subLocation) {
        this.subLocation = subLocation;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getEndTimeString() {
        return endTime == null ? "" : new SimpleDateFormat("h:mm a").format(endTime);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
