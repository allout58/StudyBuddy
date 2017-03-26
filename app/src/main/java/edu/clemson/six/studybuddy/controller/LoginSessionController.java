package edu.clemson.six.studybuddy.controller;

import android.content.Context;
import android.content.SharedPreferences;

import edu.clemson.six.studybuddy.Constants;

/**
 * Created by James Hollowell on 3/6/2017.
 */

public class LoginSessionController {
    public static final String PREF_UNAME = "edu.clemson.six.assignment4.prefs.login_uname";
    public static final String PREF_LOGIN_TOKEN = "edu.clemson.six.assignment4.prefs.login_token";

    private static LoginSessionController instance;
    private final SharedPreferences prefs;
    private int userID = -1;
    private String username = "";
    private int updateTimestamp = 0;
    private String name = "";
    private String token = "";
    private long serverTimestampDiff = 0;

    private LoginSessionController(Context context) {
        prefs = context.getSharedPreferences(Constants.PREFS_LOGIN, Context.MODE_PRIVATE);
        this.username = prefs.getString(PREF_UNAME, "");
        this.token = prefs.getString(PREF_LOGIN_TOKEN, "");
    }

    public static synchronized LoginSessionController getInstance(Context context) {
        if (instance == null) {
            instance = new LoginSessionController(context.getApplicationContext());
        }
        return instance;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        prefs.edit().putString(PREF_UNAME, username).apply();
        this.username = username;
    }

    public int getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(int updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public long getServerTimestampDiff() {
        return serverTimestampDiff;
    }

    public void setServerTimestampDiff(long serverTimestampDiff) {
        this.serverTimestampDiff = serverTimestampDiff;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        prefs.edit().putString(PREF_LOGIN_TOKEN, token).apply();
        this.token = token;
    }
}
