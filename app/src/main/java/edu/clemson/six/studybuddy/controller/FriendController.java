package edu.clemson.six.studybuddy.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.clemson.six.studybuddy.model.Friend;

/**
 * Created by jthollo on 3/28/2017.
 */

public class FriendController {
    private static final FriendController instance = new FriendController();
    // Key of UID
    private Map<String, Friend> friendMap;
    private List<Friend> friends, requests, myRequests;

    private FriendController() {
        friendMap = new HashMap<>();
        myRequests = new ArrayList<>();
        requests = new ArrayList<>();
        friends = new ArrayList<>();
    }

    public static FriendController getInstance() {
        return instance;
    }

    public Friend getFriend(String uid) {
        return friendMap.get(uid);
    }

    public Friend[] getFriends() {
        Friend[] f = new Friend[friends.size()];
        friends.toArray(f);
        return f;
    }

    public Friend[] getRequest() {
        Friend[] f = new Friend[friends.size()];
        requests.toArray(f);
        return f;
    }

    public Friend[] getMyRequest() {
        Friend[] f = new Friend[friends.size()];
        myRequests.toArray(f);
        return f;
    }

    public void reload() {
        friendMap.clear();
        friends.clear();
        myRequests.clear();
        requests.clear();
        //TODO Reload from DB
    }

}
