package edu.clemson.six.assignment4.controller;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.clemson.six.assignment4.controller.net.APIConnector;
import edu.clemson.six.assignment4.controller.net.ConnectionDetails;
import edu.clemson.six.assignment4.controller.sql.UnifiedDatabaseController;
import edu.clemson.six.assignment4.model.Car;

/**
 * Created by James Hollowell on 3/3/2017.
 */

public class SyncController {
    // TODO: we will need some way to "repair" ids when syncing, as the remote db is really the only one that knows the next ID
    // Idea: This won't prevent the repair process entirely, but if we push to the API before the local database and then resync,
    //  we should be fine unless we have no connection, in which chase we can just fall back to the local db and repair
    private static final SyncController instance = new SyncController();

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
        if (UnifiedDatabaseController.getInstance(null).getLocal().getMostRecentUserID() != LoginSessionController.getInstance(null).getUserID()) {
            // Do first time download
            Log.d("SyncController", "Initial synchronization for user");
            UnifiedDatabaseController.getInstance(null).getLocal().clearCars();
            FirstSyncTask task = new FirstSyncTask();
            task.execute();
        } else {
            // Check if need to download/upload according to timestamps
            // Do synchronize download
            Log.d("SyncController", "Synchronize new items");
            NewSyncTask task = new NewSyncTask();
            task.execute();
        }
    }

    private class FirstSyncTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            Map<String, String> args = new HashMap<>();
            args.put("uid", String.valueOf(LoginSessionController.getInstance(null).getUserID()));
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
                    UnifiedDatabaseController.getInstance(null).getLocal().syncCar(c);
                }
                UnifiedDatabaseController.getInstance(null).getLocal().setMostRecentSync(System.currentTimeMillis() / 1000);
                UnifiedDatabaseController.getInstance(null).getLocal().setMostRecentUserID(LoginSessionController.getInstance(null).getUserID());
                return true;
            } catch (IOException e) {
                Log.e("FirstSyncTask", "Error connecting to API to download initial database configuration", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                CarController.getInstance().reload();
                CarListAdapter.getInstance().notifyDataSetChanged();
            }
        }
    }

    private class NewSyncTask extends AsyncTask<Void, Integer, Boolean> {

        private Runnable callback;

        @Override
        protected Boolean doInBackground(Void... params) {
            // Offset the most recent sync time for the remote server
            long sync = UnifiedDatabaseController.getInstance(null).getLocal().getMostRecentSync();
            sync += LoginSessionController.getInstance(null).getServerTimestampDiff();

            Map<String, String> args = new HashMap<>();
            args.put("uid", String.valueOf(LoginSessionController.getInstance(null).getUserID()));
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
                    UnifiedDatabaseController.getInstance(null).getLocal().syncCar(c);
                }
                UnifiedDatabaseController.getInstance(null).getLocal().setMostRecentSync(System.currentTimeMillis() / 1000);
                UnifiedDatabaseController.getInstance(null).getLocal().setMostRecentUserID(LoginSessionController.getInstance(null).getUserID());
                return true;
            } catch (IOException e) {
                Log.e("NewSyncTask", "Error connecting to API to download synchronized data", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                CarController.getInstance().reload();
                CarListAdapter.getInstance().notifyDataSetChanged();
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
