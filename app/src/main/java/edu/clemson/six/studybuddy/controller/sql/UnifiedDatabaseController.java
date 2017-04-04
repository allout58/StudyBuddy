package edu.clemson.six.studybuddy.controller.sql;

import android.content.Context;

import java.util.List;

import edu.clemson.six.studybuddy.model.Friend;
import edu.clemson.six.studybuddy.model.Location;

/**
 * Created by jthollo on 3/8/2017.
 */

public class UnifiedDatabaseController extends DatabaseController {
    private static UnifiedDatabaseController instance;
    private LocalDatabaseController local;
    private RemoteDatabaseController remote;

    private UnifiedDatabaseController(Context context) {
        this.local = new LocalDatabaseController(context);
        this.remote = new RemoteDatabaseController();
    }

    public static synchronized UnifiedDatabaseController getInstance(Context context) {
        if (instance == null) {
            instance = new UnifiedDatabaseController(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public List<Location> getLocations() {
        return local.getLocations();
    }

    @Override
    public List<Friend> getFriends() {
        return null;
    }

    @Override
    public List<Friend> getUsersLike(String name) {
        return null;
    }

    @Override
    public void requestFriend(String uid) {

    }

    @Override
    public void acceptRequest(String uid) {

    }

    @Override
    public void close() {
        local.close();
        // Courtesy call, not sure if this will be needed for remote ever, as each call is individual
        remote.close();
    }

    public LocalDatabaseController getLocal() {
        return local;
    }

    public RemoteDatabaseController getRemote() {
        return remote;
    }
}
