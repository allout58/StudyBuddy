package edu.clemson.six.studybuddy.service;

import android.app.Notification;
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
import edu.clemson.six.studybuddy.controller.FriendController;
import edu.clemson.six.studybuddy.controller.LocationController;
import edu.clemson.six.studybuddy.controller.SyncController;
import edu.clemson.six.studybuddy.model.Friend;
import edu.clemson.six.studybuddy.model.Location;
import edu.clemson.six.studybuddy.view.FriendsActivity;

/**
 * Background service for receiving Firebase Cloud Messages
 */
public class FBMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FBMessaging";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Message Received");
        Map<String, String> data = remoteMessage.getData();
        if (data != null) {
            String action = data.get("action");
            if ("moved".equals(action)) {
                Log.d(TAG, "Friend Moved");
                SyncController.getInstance().syncFriends(null);
            } else if ("request".equals(action)) {
                Log.d(TAG, "New friend request");
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_library_books_black_24dp)
                                .setContentTitle(getString(R.string.notif_title_new_friend))
                                .setContentText(getString(R.string.notif_text_new_friend));
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

                mBuilder.setDefaults(Notification.DEFAULT_ALL);
                mBuilder.setPriority(Notification.PRIORITY_HIGH);
                mBuilder.setAutoCancel(true);

                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                // mId allows you to update the notification later on.
                mNotificationManager.notify(Constants.NOTIFICATION_FRIEND_ADD, mBuilder.build());

                SyncController.getInstance().syncFriends(null);
            } else if ("send_loc".equals(action)) {
                Log.d(TAG, "Received Pushed Friend Location");
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_library_books_black_24dp)
                                .setContentTitle(getString(R.string.notif_title_push_loc))
                                .setContentText(getString(R.string.notif_text_push_loc));
                Intent resultIntent = new Intent(this, FriendsActivity.class);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addParentStack(FriendsActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);

                mBuilder.setDefaults(Notification.DEFAULT_ALL);
                mBuilder.setPriority(Notification.PRIORITY_HIGH);
                mBuilder.setAutoCancel(true);

                Friend from = FriendController.getInstance().getFriend(data.get("from_user"));
                Location loc = LocationController.getInstance().getLocationById(Integer.parseInt(data.get("locationID")));

                StringBuilder sb = new StringBuilder(getString(R.string.notif_big_text_push_loc, from.getName()));
                sb.append(" ");
                if (loc != Location.OTHER) {
                    sb.append(loc.getName());
                } else {
                    sb.append(data.get("blurb"));
                }

                // Allows us to set the text when a user expands the notification
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(sb.toString()));

                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                // mId allows you to update the notification later on.
                mNotificationManager.notify(Constants.NOTIFICATION_PUSH_LOC, mBuilder.build());
            } else if ("upd_locs".equals(action)) {
                SyncController.getInstance().syncLocations(null);
            }
        }
    }

}
