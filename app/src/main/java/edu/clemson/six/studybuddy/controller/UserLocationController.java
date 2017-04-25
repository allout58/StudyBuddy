package edu.clemson.six.studybuddy.controller;

import java.util.Date;

import edu.clemson.six.studybuddy.controller.sql.LocalDatabaseController;
import edu.clemson.six.studybuddy.model.Location;
import edu.clemson.six.studybuddy.model.SubLocation;

/**
 * Created by jthollo on 4/18/2017.
 */

public class UserLocationController {
    private static final UserLocationController ourInstance = new UserLocationController();
    private boolean isLoaded = false;
    private Location currentLocation = null;
    private SubLocation currentSubLocation = null;
    private Date currentEndTime = null;
    private String currentBlurb = null;

    private UserLocationController() {
    }

    public static UserLocationController getInstance() {
        return ourInstance;
    }

    private void load() {
        if (!isLoaded) {
            currentLocation = LocationController.getInstance().getLocationById(LocalDatabaseController.getInstance(null).getCurrentLocationID());
            if (currentLocation != null) {
                currentSubLocation = currentLocation.getSubLocationByID(LocalDatabaseController.getInstance(null).getCurrentSubLocationID());
            }
            long et = LocalDatabaseController.getInstance(null).getCurrentEndTime();
            if (et != 0) {
                currentEndTime = new Date(et);
            }
            currentBlurb = LocalDatabaseController.getInstance(null).getCurrentBlurb();
            isLoaded = true;
        }
    }

    public Location getCurrentLocation() {
        load();
        return currentLocation;
    }

    public void setCurrentLocation(Location location) {
        if (this.currentLocation != location) {
            this.currentSubLocation = null;
            this.currentEndTime = null;
        }
        this.currentLocation = location;
        LocalDatabaseController.getInstance(null).setCurrentLocation(location);
    }

    public SubLocation getCurrentSubLocation() {
        load();
        return currentSubLocation;
    }

    public void setCurrentSubLocation(SubLocation subLocation) {
        this.currentSubLocation = subLocation;
        LocalDatabaseController.getInstance(null).setCurrentSubLocation(subLocation);
    }

    public Date getCurrentEndTime() {
        load();
        return currentEndTime;
    }

    public void setCurrentEndTime(Date currentEndTime) {
        this.currentEndTime = currentEndTime;
        LocalDatabaseController.getInstance(null).setCurrentEndTime(currentEndTime);
    }

    /**
     * Called when the location objects are reloaded from the database
     */
    public void reload() {
        if (currentLocation != null) {
            currentLocation = LocationController.getInstance().getLocationById(currentLocation.getId());
            if (currentSubLocation != null) {
                currentSubLocation = currentLocation.getSubLocationByID(currentSubLocation.getId());
            }
        }
    }

    public String getCurrentBlurb() {
        load();
        return currentBlurb;
    }

    public void setCurrentBlurb(String currentBlurb) {
        this.currentBlurb = currentBlurb;
        LocalDatabaseController.getInstance(null).setCurrentBlurb(currentBlurb);
    }
}
