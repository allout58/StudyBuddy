package edu.clemson.six.studybuddy.service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by jthollo on 4/13/2017.
 */

public class FBInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FBInstanceID";

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed id token: " + token);
        // TODO: Send token to server
    }
}
