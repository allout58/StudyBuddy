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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.clemson.six.studybuddy.controller.net.APIConnector;
import edu.clemson.six.studybuddy.controller.net.ConnectionDetails;
import edu.clemson.six.studybuddy.controller.sql.LocalDatabaseController;
import edu.clemson.six.studybuddy.model.Friend;
import edu.clemson.six.studybuddy.model.Location;
import edu.clemson.six.studybuddy.model.SubLocation;

/**
 * Created by jthollo on 4/18/2017.
 */

public class UserLocationController {
    private static final String TAG = "UserLocationController";

    private static final UserLocationController ourInstance = new UserLocationController();
    private boolean isLoaded = false;
    private Location currentLocation = null;
    private SubLocation currentSubLocation = null;
    private Date currentEndTime = null;
    private String currentBlurb = null;

    private UserLocationController() {
    }

    public static UserLocationController getInstance() {
        return ourInstance;
    }

    private void load() {
        if (!isLoaded) {
            currentLocation = LocationController.getInstance().getLocationById(LocalDatabaseController.getInstance(null).getCurrentLocationID());
            if (currentLocation != null) {
                currentSubLocation = currentLocation.getSubLocationByID(LocalDatabaseController.getInstance(null).getCurrentSubLocationID());
            }
            long et = LocalDatabaseController.getInstance(null).getCurrentEndTime();
            if (et != 0) {
                currentEndTime = new Date(et);
            }
            currentBlurb = LocalDatabaseController.getInstance(null).getCurrentBlurb();
            isLoaded = true;
        }
    }

    public Location getCurrentLocation() {
        load();
        return currentLocation;
    }

    public void setCurrentLocation(Location location) {
        if (this.currentLocation != location) {
            this.currentSubLocation = null;
            this.currentEndTime = null;
        }
        this.currentLocation = location;
        LocalDatabaseController.getInstance(null).setCurrentLocation(location);
    }

    public SubLocation getCurrentSubLocation() {
        load();
        return currentSubLocation;
    }

    public void setCurrentSubLocation(SubLocation subLocation) {
        this.currentSubLocation = subLocation;
        LocalDatabaseController.getInstance(null).setCurrentSubLocation(subLocation);
    }

    public Date getCurrentEndTime() {
        load();
        return currentEndTime;
    }

    public void setCurrentEndTime(Date currentEndTime) {
        this.currentEndTime = currentEndTime;
        LocalDatabaseController.getInstance(null).setCurrentEndTime(currentEndTime);
    }

    public void sendLocation(final Friend f) {
        if (getCurrentLocation() != null) {
            Log.d(TAG, "Sending location to friend " + f.getName());
            FirebaseAuth.getInstance().getCurrentUser().getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    SendLocationTask t = new SendLocationTask();
                    t.execute(task.getResult().getToken(), f.getUid());
                }
            });
        } else {
            Log.d(TAG, "Can't send location, not currently set");
        }
    }

    /**
     * Called when the location objects are reloaded from the database
     */
    public void reload() {
        if (currentLocation != null) {
            currentLocation = LocationController.getInstance().getLocationById(currentLocation.getId());
            if (currentSubLocation != null) {
                currentSubLocation = currentLocation.getSubLocationByID(currentSubLocation.getId());
            }
        }
    }

    public String getCurrentBlurb() {
        load();
        return currentBlurb;
    }

    public void setCurrentBlurb(String currentBlurb) {
        this.currentBlurb = currentBlurb;
        LocalDatabaseController.getInstance(null).setCurrentBlurb(currentBlurb);
    }

    private class SendLocationTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            Map<String, String> args = new HashMap<>();
            args.put("jwt", params[0]);
            args.put("otherID", params[1]);
            args.put("locationID", String.valueOf(getCurrentLocation().getId()));
//            args.put("sublocationID", String.valueOf(getCurrentSubLocation().getId()));
            args.put("blurb", getCurrentBlurb());
            ConnectionDetails con = APIConnector.setupConnection("friend.send_location", args, ConnectionDetails.Method.POST);
            try {
                JsonObject obj = APIConnector.connect(con).getAsJsonObject();
                return obj.has("status") && obj.get("status").getAsString().equals("success");
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                SyncController.getInstance().syncFriends(null);
            }
        }
    }
}
