package edu.clemson.six.studybuddy.controller;

import java.util.HashMap;
import java.util.Map;

import edu.clemson.six.studybuddy.model.Friend;

/**
 * Created by jthollo on 3/28/2017.
 */

public class FriendController {
    private static final FriendController instance = new FriendController();
    // Key of UID
    private Map<String, Friend> friendMap;

    private FriendController() {
        friendMap = new HashMap<>();
    }

    public static FriendController getInstance() {
        return instance;
    }

    public void addFriend(Friend f) {
        friendMap.put(f.getName(), f);
    }

    public Friend getFriend(String uid) {
        return friendMap.get(uid);
    }

    public void reload() {
        friendMap.clear();
        //TODO Reload from DB
    }

}
