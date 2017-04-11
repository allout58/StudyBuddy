package edu.clemson.six.studybuddy.controller;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.clemson.six.studybuddy.controller.net.APIConnector;
import edu.clemson.six.studybuddy.controller.net.ConnectionDetails;
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

    public void addFriend(final Friend f) {
        Log.d(TAG, "Adding friend " + f.getName());
        FirebaseAuth.getInstance().getCurrentUser().getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                AddFriendTask t = new AddFriendTask();
                t.execute(task.getResult().getToken(), f.getUid());
            }
        });
    }

    public void reload() {
        friendMap.clear();
        friends.clear();
        myRequests.clear();
        requests.clear();
        //TODO Reload from DB
    }

    private class AddFriendTask extends AsyncTask<String, Void, Boolean> {

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
}
