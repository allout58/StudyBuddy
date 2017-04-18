package edu.clemson.six.studybuddy.service;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by jthollo on 4/13/2017.
 */

public class FBMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FBMessaging";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Message Received");
//        Log.d(TAG, "Message " + remoteMessage.getNotification().getBody());
    }

}
