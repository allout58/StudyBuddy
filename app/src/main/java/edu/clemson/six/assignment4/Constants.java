package edu.clemson.six.assignment4;

/**
 * Created by jthollo on 3/5/2017.
 */

public final class Constants {
    private Constants() {

    }

    /**
     * Web address of the root of the remote API
     */
    public static final String API_ADDRESS = "https://people.cs.clemson.edu/~jthollo/4820/projects/asg3/api/";
    /**
     * Timeout when connecting to the remote API
     */
    public static final int API_TIMEOUT = 3000; // In milliseconds
    /**
     * Number of retries when connecting to the remote API
     */
    public static final int API_RETRIES = 3;
}
