package edu.clemson.six.studybuddy.controller.net;

import android.os.Looper;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

import edu.clemson.six.studybuddy.Constants;


/**
 * Utility methods for connecting to the Web API
 */

public final class APIConnector {
    private APIConnector() {

    }

    private static String transformRequest(String input) {
        if (input.contains(".php")) {
            return input;
        } else {
            return input.replace(".", "/") + ".php";
        }
    }

    public static ConnectionDetails setupConnection(String endpoint, Map<String, String> arguments, ConnectionDetails.Method method) {
        return new ConnectionDetails(Constants.API_ADDRESS + transformRequest(endpoint), arguments, method);
    }

    public static JsonElement connect(ConnectionDetails con) throws IOException {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            throw new IllegalStateException("Internet Connections must not be made on the main application thread");
        }
        HttpURLConnection urlConnection;
        URL url;
        switch (con.getMethod()) {
            case GET:
                url = new URL(con.getUrl() + "?" + con.getGetArgments());
                urlConnection = (HttpURLConnection) url.openConnection();
                break;
            case POST:
                byte[] postData = con.getGetArgments().getBytes(Charset.forName("UTF-8"));
                url = new URL(con.getUrl());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setRequestProperty("charset", "utf-8");
                urlConnection.setRequestProperty("Content-Length", Integer.toString(postData.length));
                DataOutputStream wr = null;
                try {
                    wr = new DataOutputStream(urlConnection.getOutputStream());
                    wr.write(postData);
                } finally {
                    if (wr != null) {
                        wr.close();
                    }
                }
                break;
            default:
                url = new URL(con.getUrl());
                urlConnection = (HttpURLConnection) url.openConnection();
        }
        urlConnection.setConnectTimeout(con.getTimeout());

        String line, text = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            while ((line = br.readLine()) != null) {
                text += line;
            }
            Log.d("APIConnector-Resp", text);
            return new JsonParser().parse(text);
        } catch (JsonSyntaxException se) {
            Log.e("APIConnector", "Malformed response:\n\n" + text, se);
            throw se;
        } finally {
            urlConnection.disconnect();
        }
    }
}
