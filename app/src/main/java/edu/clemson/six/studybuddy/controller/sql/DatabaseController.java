package edu.clemson.six.studybuddy.controller.sql;

import java.util.List;

import edu.clemson.six.studybuddy.model.Friend;
import edu.clemson.six.studybuddy.model.Location;

/**
 * Abstract Class for a database controller
 */

public abstract class DatabaseController {

    public abstract List<Location> getLocations();

    public abstract List<Friend> getFriends();

    public abstract List<Friend> getUsersLike(String name);

    public abstract void requestFriend(String uid);

    public abstract void acceptRequest(String uid);

    public abstract void close();
}
