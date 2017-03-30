package edu.clemson.six.studybuddy.controller;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.clemson.six.studybuddy.controller.net.APIConnector;
import edu.clemson.six.studybuddy.controller.net.ConnectionDetails;
import edu.clemson.six.studybuddy.controller.sql.UnifiedDatabaseController;
import edu.clemson.six.studybuddy.model.Car;

/**
 * Created by James Hollowell on 3/3/2017.
 */

public class SyncController {
    // TODO: we will need some way to "repair" ids when syncing, as the remote db is really the only one that knows the next ID
    // Idea: This won't prevent the repair process entirely, but if we push to the API before the local database and then resync,
    //  we should be fine unless we have no connection, in which chase we can just fall back to the local db and repair
    private static final SyncController instance = new SyncController();
    private int serverTimeOffset;

    private SyncController() {
    }

    public static SyncController getInstance() {
        return instance;
    }

    public void refresh(Runnable callback) {
        Log.d("SyncController", "Refreshing content");
        NewSyncTask task = new NewSyncTask();
        task.setCallback(callback);
        task.execute();
    }

    public void beginSync() {
        if (UnifiedDatabaseController.getInstance(null).getLocal().getMostRecentUserID() != FirebaseAuth.getInstance().getCurrentUser().getUid()) {
            // Do first time download
            Log.d("SyncController", "Initial synchronization for user");
//            UnifiedDatabaseController.getInstance(null).getLocal().clearCars();
//            FirstSyncTask task = new FirstSyncTask();
//            task.execute();
        } else {
            // Check if need to download/upload according to timestamps
            // Do synchronize download
            Log.d("SyncController", "Synchronize new items");
//            NewSyncTask task = new NewSyncTask();
//            task.execute();
        }
    }

    public int getServerTimeOffset() {
        return serverTimeOffset;
    }

    public void setServerTimeOffset(int serverTimeOffset) {
        this.serverTimeOffset = serverTimeOffset;
    }

    private class FirstSyncTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            Map<String, String> args = new HashMap<>();
            args.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
            ConnectionDetails details = APIConnector.setupConnection("sync.firsttime", args, ConnectionDetails.Method.GET);
            try {
                JsonElement obj = APIConnector.connect(details);
                Gson gson = new Gson();
                Car c;
                for (JsonElement el : obj.getAsJsonArray()) {
                    JsonObject o = el.getAsJsonObject();
                    c = gson.fromJson(el, Car.class);
                    String colo = o.get("colorHex").getAsString();
                    c.setColor(Color.parseColor(colo));
//                    UnifiedDatabaseController.getInstance(null).getLocal().syncCar(c);
                }
                UnifiedDatabaseController.getInstance(null).getLocal().setMostRecentSync(System.currentTimeMillis() / 1000);
                UnifiedDatabaseController.getInstance(null).getLocal().setMostRecentUserID(FirebaseAuth.getInstance().getCurrentUser().getUid());
                return true;
            } catch (IOException e) {
                Log.e("FirstSyncTask", "Error connecting to API to download initial database configuration", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
//                CarController.getInstance().reload();
            }
        }
    }

    private class NewSyncTask extends AsyncTask<Void, Integer, Boolean> {

        private Runnable callback;

        @Override
        protected Boolean doInBackground(Void... params) {
            // Offset the most recent sync time for the remote server
            long sync = UnifiedDatabaseController.getInstance(null).getLocal().getMostRecentSync();
            sync += serverTimeOffset;

            Map<String, String> args = new HashMap<>();
            args.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
            args.put("last_upd", String.valueOf(sync));
            ConnectionDetails dets = APIConnector.setupConnection("sync.getnew", args, ConnectionDetails.Method.GET);
            try {
                JsonElement obj = APIConnector.connect(dets);
                Gson gson = new Gson();
                Car c;
                for (JsonElement el : obj.getAsJsonArray()) {
                    JsonObject o = el.getAsJsonObject();
                    c = gson.fromJson(el, Car.class);
                    c.setColor(Color.parseColor(o.get("colorHex").getAsString()));
                    // TODO: Change this to something intelligent? Like look for a newer car on this end?
//                    UnifiedDatabaseController.getInstance(null).getLocal().syncCar(c);
                }
                UnifiedDatabaseController.getInstance(null).getLocal().setMostRecentSync(System.currentTimeMillis() / 1000);
                UnifiedDatabaseController.getInstance(null).getLocal().setMostRecentUserID(FirebaseAuth.getInstance().getCurrentUser().getUid());
                return true;
            } catch (IOException e) {
                Log.e("NewSyncTask", "Error connecting to API to download synchronized data", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
//                CarController.getInstance().reload();
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
