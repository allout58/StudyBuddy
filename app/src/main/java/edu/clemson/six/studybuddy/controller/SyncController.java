package edu.clemson.six.studybuddy.controller;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.clemson.six.studybuddy.controller.adapter.FriendsListAdapter;
import edu.clemson.six.studybuddy.controller.adapter.HomePageAdapter;
import edu.clemson.six.studybuddy.controller.net.APIConnector;
import edu.clemson.six.studybuddy.controller.net.ConnectionDetails;
import edu.clemson.six.studybuddy.controller.sql.LocalDatabaseController;
import edu.clemson.six.studybuddy.model.Friend;
import edu.clemson.six.studybuddy.model.Location;
import edu.clemson.six.studybuddy.model.SubLocation;

/**
 * Created by James Hollowell on 3/3/2017.
 */

public class SyncController {
    private static final String TAG = "SyncController";

    private static final SyncController instance = new SyncController();
    private long serverTimeOffset;

    private boolean isLocsSynced = false;

    private SyncController() {
    }

    public static SyncController getInstance() {
        return instance;
    }

    public void syncLocations(final Runnable r) {
        Log.d(TAG, "Synchronizing locations and sublocations with the server");
        FirebaseAuth.getInstance().getCurrentUser().getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                LocSyncTask t = new LocSyncTask();
                t.setCallback(r);
                t.execute(task.getResult().getToken());
            }
        });
    }

    public void syncFriends(final Runnable r) {
        LocalDatabaseController.getInstance(null).clearFriendsAndRequests();
        Log.d(TAG, "Synchronizing friends");
        FirebaseAuth.getInstance().getCurrentUser().getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                FriendSyncTask t = new FriendSyncTask();
                t.setCallback(r);
                t.execute(task.getResult().getToken());
            }
        });
    }


    public long getServerTimeOffset() {
        return serverTimeOffset;
    }

    public void setServerTimeOffset(long serverTimeOffset) {
        this.serverTimeOffset = serverTimeOffset;
    }

    public boolean isLocsSynced() {
        return isLocsSynced;
    }

    private class FriendSyncTask extends AsyncTask<String, Integer, Boolean> {
        private static final String TAG = "FriendSyncTask";
        private Runnable callback;

        @Override
        protected Boolean doInBackground(String... params) {
            Map<String, String> args = new HashMap<>();
            args.put("jwt", params[0]);
            ConnectionDetails dets = APIConnector.setupConnection("friend.getall", args, ConnectionDetails.Method.POST);
            try {
                JsonElement obj = APIConnector.connect(dets);
                Friend friend;
                if (obj.getAsJsonObject().has("error")) {
                    Log.e(TAG, "Remote error: " + obj.getAsJsonObject().get("error").getAsString());
                    return false;
                }
                for (JsonElement el : obj.getAsJsonObject().get("connected").getAsJsonArray()) {
                    JsonObject elO = el.getAsJsonObject();
                    Location loc = null;
                    if (!elO.get("locationID").isJsonNull()) {
                        loc = LocationController.getInstance().getLocationById(elO.get("locationID").getAsInt());
                    }
                    SubLocation sl = null;
                    if (loc != null && !elO.get("subID").isJsonNull()) {
                        sl = loc.getSubLocationByID(elO.get("subID").getAsInt());
                    }
                    String blurb = "";
                    if (!elO.get("blurb").isJsonNull()) {
                        blurb = elO.get("blurb").getAsString();
                    }
                    Date date = null;
                    if (!elO.get("endTime").isJsonNull()) {
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
                        try {
                            date = format.parse(elO.get("endTime").getAsString());
                            Log.d(TAG, String.format("Incoming: %s, Parsed: %s", elO.get("endTime").getAsString(), SimpleDateFormat.getTimeInstance().format(date)));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    String image = "";
                    if (!elO.get("imageURL").isJsonNull()) {
                        image = elO.get("imageURL").getAsString();
                    }
                    friend = new Friend(
                            elO.get("firebase_uid").getAsString(),
                            image,
                            elO.get("realName").getAsString(),
                            loc, sl, blurb, date, true);
                    Log.d(TAG, "Friend " + friend.getName() + " " + friend.getUid() + " @ " + (friend.getLocation() == null ? "none" : friend.getLocation().getName()));
                    LocalDatabaseController.getInstance(null).syncFriend(friend);
                }
                for (JsonElement el : obj.getAsJsonObject().get("my_requests").getAsJsonArray()) {
                    JsonObject elO = el.getAsJsonObject();
                    String url = "";
                    if (!elO.get("imageURL").isJsonNull()) {
                        url = elO.get("imageURL").getAsString();
                    }
                    friend = new Friend(
                            elO.get("firebase_uid").getAsString(),
                            url,
                            elO.get("realName").getAsString(),
                            null, null, "", null, false);
                    Log.d(TAG, "My Request " + friend.getName() + " " + friend.getUid());
                    LocalDatabaseController.getInstance(null).syncRequest(friend, true);
                }
                for (JsonElement el : obj.getAsJsonObject().get("their_requests").getAsJsonArray()) {
                    JsonObject elO = el.getAsJsonObject();
                    String url = "";
                    if (!elO.get("imageURL").isJsonNull()) {
                        url = elO.get("imageURL").getAsString();
                    }
                    friend = new Friend(
                            elO.get("firebase_uid").getAsString(),
                            url,
                            elO.get("realName").getAsString(),
                            null, null, "", null, false);
                    Log.d(TAG, "Their Request " + friend.getName() + " " + friend.getUid());
                    LocalDatabaseController.getInstance(null).syncRequest(friend, false);
                }
                FriendController.getInstance().reload();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean booleanResult) {
            if (booleanResult) {
                if (callback != null) {
                    callback.run();
                }
                HomePageAdapter.getInstance().notifyDataSetChanged();
                FriendsListAdapter.getInstance().notifyDataSetChanged();
            }
            super.onPostExecute(booleanResult);
        }

        public void setCallback(Runnable callback) {
            this.callback = callback;
        }
    }

    private class LocSyncTask extends AsyncTask<String, Integer, Boolean> {
        private static final String TAG = "LocSyncTask";
        private Runnable callback;

        @Override
        protected Boolean doInBackground(String... params) {
            // Offset the most recent sync time for the remote server
            long sync = LocalDatabaseController.getInstance(null).getMostRecentSync();
            Log.d(TAG, String.format("Sync: %d, Offset: %d", sync, serverTimeOffset));
            sync += serverTimeOffset;

            Map<String, String> args = new HashMap<>();
            args.put("jwt", params[0]);
            args.put("last_upd", String.valueOf(sync));
            ConnectionDetails dets = APIConnector.setupConnection("sync.getnew", args, ConnectionDetails.Method.POST);
            try {
                JsonElement obj = APIConnector.connect(dets);
                Gson gson = new Gson();
                Location l;
                SubLocation sl;
                if (obj.getAsJsonObject().has("error")) {
                    Log.e(TAG, "Remote error: " + obj.getAsJsonObject().get("error").getAsString());
                    return false;
                }
                for (JsonElement el : obj.getAsJsonObject().get("locations").getAsJsonArray()) {
                    JsonObject o = el.getAsJsonObject();
                    l = gson.fromJson(o, Location.class);
                    LocalDatabaseController.getInstance(null).syncLocation(l);
                }
                LocationController.getInstance().reload();
                for (JsonElement el : obj.getAsJsonObject().get("sublocations").getAsJsonArray()) {
                    JsonObject o = el.getAsJsonObject();
                    Location loc = LocationController.getInstance().getLocationById(o.get("locationID").getAsInt());
                    sl = new SubLocation(o.get("subID").getAsInt(), o.get("name").getAsString(), loc);
                    loc.addSubLocation(sl);
                    LocalDatabaseController.getInstance(null).syncSubLocation(sl);
                }
                LocalDatabaseController.getInstance(null).setMostRecentSync(System.currentTimeMillis() / 1000);
                LocalDatabaseController.getInstance(null).setMostRecentUserID(FirebaseAuth.getInstance().getCurrentUser().getUid());
                Log.d(TAG, "Locations Synchronized");
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error connecting to API to download synchronized data", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                isLocsSynced = true;
                if (this.callback != null) {
                    callback.run();
                }
            }
        }

        public void setCallback(Runnable callback) {
            this.callback = callback;
        }
    }
}
