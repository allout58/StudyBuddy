package edu.clemson.six.assignment4.controller.net;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonElement;

import java.io.IOException;

/**
 * Created by James Hollowell on 3/3/2017.
 */

public class ConnectorTask extends AsyncTask<ConnectionDetails, Integer, JsonElement> {
    @Override
    protected JsonElement doInBackground(ConnectionDetails... params) {
        if (params.length != 1) {
            Log.d("ConnectorTask", "FUUUUUCK you tried to do it with more than one!");
            return null;
        } else {
            try {
                return APIConnector.connect(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
