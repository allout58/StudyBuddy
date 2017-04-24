package edu.clemson.six.studybuddy.controller;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.clemson.six.studybuddy.controller.net.APIConnector;
import edu.clemson.six.studybuddy.controller.net.ConnectionDetails;
import edu.clemson.six.studybuddy.controller.sql.LocalDatabaseController;
import edu.clemson.six.studybuddy.model.Friend;

/**
 * Created by jthollo on 3/28/2017.
 */

public class FriendController {
    private static final String TAG = "FriendController";
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

    /**
     * Get a friend (or request) by their Firebase UID
     *
     * @param uid Firebase UID
     * @return Friend with given UID
     */
    public Friend getFriend(String uid) {
        return friendMap.get(uid);
    }

    /**
     * Get all confirmed friends
     *
     * @return
     */
    public Friend[] getFriends() {
        Friend[] f = new Friend[friends.size()];
        friends.toArray(f);
        return f;
    }

    /**
     * Get all friends who have requested the logged in user
     *
     * @return
     */
    public Friend[] getRequests() {
        Friend[] f = new Friend[requests.size()];
        requests.toArray(f);
        return f;
    }

    /**
     * Get all friends the logged in user has requested
     *
     * @return
     */
    public Friend[] getMyRequests() {
        Friend[] f = new Friend[myRequests.size()];
        myRequests.toArray(f);
        return f;
    }

    public int getFriendsCount() {
        return friends.size();
    }

    public int getRequestsCount() {
        return requests.size();
    }

    public int getMyRequestsCount() {
        return myRequests.size();
    }

//    public void addFriend(Friend f) {
//        friendMap.put(f.getUid(), f);
//        friends.add(f);
//    }
//
//    public void addRequest(Friend f) {
//        friendMap.put()
//    }

    public void newFriend(final Friend f) {
        Log.d(TAG, "Adding new friend " + f.getName());
        FirebaseAuth.getInstance().getCurrentUser().getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                NewFriendTask t = new NewFriendTask();
                t.execute(task.getResult().getToken(), f.getUid());
            }
        });
    }

    public void confirmFriend(final Friend f) {
        Log.d(TAG, "Confirming new friend " + f.getName());
        FirebaseAuth.getInstance().getCurrentUser().getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                ConfirmFriendTask t = new ConfirmFriendTask();
                t.execute(task.getResult().getToken(), f.getUid());
            }
        });
    }

    public void deleteFriend(final Friend f) {
        Log.d(TAG, "Deleting friend " + f.getName());
        FirebaseAuth.getInstance().getCurrentUser().getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                DeleteFriendTask t = new DeleteFriendTask();
                t.execute(task.getResult().getToken(), f.getUid());
            }
        });
    }


    public void reload() {
        friendMap.clear();
        friends.clear();
        myRequests.clear();
        requests.clear();
        for (Friend f : LocalDatabaseController.getInstance(null).getFriends()) {
            friendMap.put(f.getUid(), f);
            friends.add(f);
            FirebaseMessaging.getInstance().subscribeToTopic(f.getUid());
        }
        for (Friend f : LocalDatabaseController.getInstance(null).getRequests()) {
            friendMap.put(f.getUid(), f);
            requests.add(f);
        }
        for (Friend f : LocalDatabaseController.getInstance(null).getMyRequests()) {
            friendMap.put(f.getUid(), f);
            myRequests.add(f);
        }
    }

    private class NewFriendTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            Map<String, String> args = new HashMap<>();
            args.put("jwt", params[0]);
            args.put("otherID", params[1]);
            ConnectionDetails con = APIConnector.setupConnection("friend.request", args, ConnectionDetails.Method.POST);
            try {
                JsonObject obj = APIConnector.connect(con).getAsJsonObject();
                return obj.has("status") && obj.get("status").getAsString().equals("success");
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private class ConfirmFriendTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            Map<String, String> args = new HashMap<>();
            args.put("jwt", params[0]);
            args.put("otherID", params[1]);
            ConnectionDetails con = APIConnector.setupConnection("friend.confirm", args, ConnectionDetails.Method.POST);
            try {
                JsonObject obj = APIConnector.connect(con).getAsJsonObject();
                return obj.has("status") && obj.get("status").getAsString().equals("success");
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private class DeleteFriendTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            Map<String, String> args = new HashMap<>();
            args.put("jwt", params[0]);
            args.put("otherID", params[1]);
            ConnectionDetails con = APIConnector.setupConnection("friend.delete", args, ConnectionDetails.Method.POST);
            try {
                JsonObject obj = APIConnector.connect(con).getAsJsonObject();
                return obj.has("status") && obj.get("status").getAsString().equals("success");
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

}
