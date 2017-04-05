package edu.clemson.six.studybuddy.controller;

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
import java.util.HashMap;
import java.util.Map;

import edu.clemson.six.studybuddy.controller.net.APIConnector;
import edu.clemson.six.studybuddy.controller.net.ConnectionDetails;
import edu.clemson.six.studybuddy.controller.sql.UnifiedDatabaseController;
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

    public void refreshLocations(final Runnable callback) {
        Log.d(TAG, "Refreshing content");
        FirebaseAuth.getInstance().getCurrentUser().getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                LocSyncTask t = new LocSyncTask();
                t.setCallback(callback);
                t.execute(task.getResult().getToken());
            }
        });
    }

    public void beginSync() {
        if (UnifiedDatabaseController.getInstance(null).getLocal().getMostRecentUserID() != FirebaseAuth.getInstance().getCurrentUser().getUid()) {
            // Do first time download
            Log.d(TAG, "Initial synchronization for user");
//            UnifiedDatabaseController.getInstance(null).getLocal().clearCars();
//            FirstSyncTask task = new FirstSyncTask();
//            task.execute();
        } else {
            // Check if need to download/upload according to timestamps
            // Do synchronize download
            Log.d(TAG, "Synchronize new items");
//            NewSyncTask task = new NewSyncTask();
//            task.execute();
        }
    }

    public long getServerTimeOffset() {
        return serverTimeOffset;
    }

    public void setServerTimeOffset(long serverTimeOffset) {
        this.serverTimeOffset = serverTimeOffset;
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
                Gson gson = new Gson();
                Friend friend;
                if (obj.getAsJsonObject().has("error")) {
                    Log.e(TAG, "Remote error: " + obj.getAsJsonObject().get("error").getAsString());
                    return false;
                }
                for (JsonElement el : obj.getAsJsonObject().get("connected").getAsJsonArray()) {

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean booleanResult) {
            if (booleanResult) {
                if (callback != null) {
                    callback.run();
                }
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
            long sync = UnifiedDatabaseController.getInstance(null).getLocal().getMostRecentSync();
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
                    UnifiedDatabaseController.getInstance(null).getLocal().syncLocation(l);
                }
                LocationController.getInstance().reload();
                for (JsonElement el : obj.getAsJsonObject().get("sublocations").getAsJsonArray()) {
                    JsonObject o = el.getAsJsonObject();
                    Location loc = LocationController.getInstance().getLocation(o.get("locationID").getAsInt());
                    sl = new SubLocation(o.get("subID").getAsInt(), o.get("name").getAsString(), loc);
                    loc.addSubLocation(sl);
                    UnifiedDatabaseController.getInstance(null).getLocal().syncSubLocation(sl);
                }
                UnifiedDatabaseController.getInstance(null).getLocal().setMostRecentSync(System.currentTimeMillis() / 1000);
                UnifiedDatabaseController.getInstance(null).getLocal().setMostRecentUserID(FirebaseAuth.getInstance().getCurrentUser().getUid());
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
