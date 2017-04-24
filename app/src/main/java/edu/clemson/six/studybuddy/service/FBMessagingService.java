package edu.clemson.six.studybuddy.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import edu.clemson.six.studybuddy.Constants;
import edu.clemson.six.studybuddy.R;
import edu.clemson.six.studybuddy.controller.SyncController;
import edu.clemson.six.studybuddy.controller.adapter.FriendsListAdapter;
import edu.clemson.six.studybuddy.view.FriendsActivity;

/**
 * Created by jthollo on 4/13/2017.
 */

public class FBMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FBMessaging";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Message Received");
        Map<String, String> data = remoteMessage.getData();
        if (data != null) {
            if (data.get("moved") != null) {
                Log.d(TAG, "Friend Moved");
                SyncController.getInstance().syncFriends(new Runnable() {
                    @Override
                    public void run() {
                        FriendsListAdapter.getInstance().notifyDataSetChanged();
                    }
                });
            } else if (data.get("request") != null) {
                Log.d(TAG, "New friend request");
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_library_books_black_24dp)
                                .setContentTitle("New Friend Request")
                                .setContentText("You have a new friend request!");
                // Creates an explicit intent for an Activity in your app
                Intent resultIntent = new Intent(this, FriendsActivity.class);

                // The stack builder object will contain an artificial back stack for the
                // started Activity.
                // This ensures that navigating backward from the Activity leads out of
                // your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(FriendsActivity.class);
                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);

                mBuilder.setAutoCancel(true);

                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                // mId allows you to update the notification later on.
                mNotificationManager.notify(Constants.NOTIFICATION_FRIEND_ADD, mBuilder.build());

                SyncController.getInstance().syncFriends(new Runnable() {
                    @Override
                    public void run() {
                        FriendsListAdapter.getInstance().notifyDataSetChanged();
                    }
                });
            }
        }
//        Log.d(TAG, "Message " + remoteMessage.getNotification().getBody());
    }

}
