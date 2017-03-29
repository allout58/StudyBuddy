package edu.clemson.six.studybuddy.controller.sql;

/**
 * Created by jthollo on 3/27/2017.
 */

public class DBContract {
    private DBContract() {
    }

    public static class LocationsContract {
        public static final String TABLE_NAME = "Locations";
        public static final String COLUMN_ID = "locationID";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LONG = "longitude";
        public static final String COLUMN_LAT = "latitude";
        public static final String COLUMN_RADIUS = "radius";

        public static final String[] COLUMNS_ALL = {COLUMN_ID, COLUMN_NAME, COLUMN_LONG, COLUMN_LAT, COLUMN_RADIUS};
    }

    public static class SubLocationsContract {
        public static final String TABLE_NAME = "Sublocation";
        public static final String COLUMN_ID = "subID";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LOCATION = "locationId";

        public static final String[] COLUMNS_ALL = {COLUMN_ID, COLUMN_NAME, COLUMN_LOCATION};
    }

    public static class FriendsContract {
        public static final String TABLE_NAME = "Friends";
        public static final String COLUMN_UID = "firebase_uid";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LOCATION = LocationsContract.COLUMN_ID;
        public static final String COLUMN_SUBLOCATION = SubLocationsContract.COLUMN_ID;
        public static final String COLUMN_BLURB = "blurb";
        public static final String COLUMN_END_TIME = "endTime";
        public static final String COLUMN_CONFIRMED = "confirmed";

        public static final String[] COLUMNS_ALL = {COLUMN_UID, COLUMN_NAME, COLUMN_LOCATION, COLUMN_SUBLOCATION, COLUMN_BLURB, COLUMN_END_TIME, COLUMN_CONFIRMED};
    }
}
