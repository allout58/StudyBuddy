package edu.clemson.six.studybuddy.service;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.clemson.six.studybuddy.controller.net.APIConnector;
import edu.clemson.six.studybuddy.controller.net.ConnectionDetails;

/**
 * Created by jthollo on 4/13/2017.
 */

public class FBInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FBInstanceID";

    public static void sendRegTokenToServer() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    String authToken = task.getResult().getToken();
                    String token = FirebaseInstanceId.getInstance().getToken();
                    NotifyServerTask t = new NotifyServerTask();
                    t.execute(authToken, token);
                }
            });
        }
    }

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed id token: " + token);
        // TODO: Send token to server
        sendRegTokenToServer();
    }

    private static class NotifyServerTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            Map<String, String> args = new HashMap<>();
            args.put("jwt", params[0]);
            args.put("regID", params[1]);
            ConnectionDetails con = APIConnector.setupConnection("user.firebase_update_regid", args, ConnectionDetails.Method.POST);
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
