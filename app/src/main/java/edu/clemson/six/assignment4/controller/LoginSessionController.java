package edu.clemson.six.assignment4.controller;

/**
 * Created by James Hollowell on 3/6/2017.
 */

public class LoginSessionController {
    private static LoginSessionController instance = new LoginSessionController();

    public static LoginSessionController getInstance() {
        return instance;
    }

    private LoginSessionController() {
    }

    private int userID = -1;
    private String username = "";
    private int updateTimestamp = 0;
    private long serverTimestampDiff = 0;


    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
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
}
