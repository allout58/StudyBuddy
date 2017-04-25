package edu.clemson.six.studybuddy;

/**
 * Created by jthollo on 3/5/2017.
 */

public final class Constants {
    /**
     * Web address of the root of the remote API
     */
    public static final String API_ADDRESS = "https://people.cs.clemson.edu/~jthollo/4820/projects/proj/api/";
    /**
     * Timeout when connecting to the remote API
     */
    public static final int API_TIMEOUT = 3000; // In milliseconds
    /**
     * Number of retries when connecting to the remote API
     */
    public static final int API_RETRIES = 3;

    public static final int RC_SIGN_IN = 4242;

    public static final int NOTIFICATION_LOCATION_CHANGE = 1;
    public static final int NOTIFICATION_FRIEND_ADD = 2;

    public static final String ACTION_PIN_DROP = "ACTION_PIN_DROP";
    public static final String EXTRA_PIN_DROP_NAME = "EXTRA_PDN";

    private Constants() {

    }
}
