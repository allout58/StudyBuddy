package edu.clemson.six.studybuddy.controller.sql;

import android.provider.BaseColumns;

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
        public static final String COLUMN_NAME = "realName";
        public static final String COLUMN_IMAGE_URL = "imageURL";
        public static final String COLUMN_LOCATION = LocationsContract.COLUMN_ID;
        public static final String COLUMN_SUBLOCATION = SubLocationsContract.COLUMN_ID;
        public static final String COLUMN_BLURB = "blurb";
        public static final String COLUMN_END_TIME = "endTime";

        public static final String[] COLUMNS_ALL = {COLUMN_UID, COLUMN_NAME, COLUMN_IMAGE_URL, COLUMN_LOCATION, COLUMN_SUBLOCATION, COLUMN_BLURB, COLUMN_END_TIME};
    }

    public static class FriendsRequestsContract {
        public static final String TABLE_NAME = "Requests";
        public static final String COLUMN_UID = "firebase_uid";
        public static final String COLUMN_NAME = "realName";
        public static final String COLUMN_IMAGE_URL = "imageURL";
        public static final String COLUMN_IS_MINE = "isMine";

        public static final String[] COLUMNS_ALL = {COLUMN_UID, COLUMN_NAME, COLUMN_IMAGE_URL, COLUMN_IS_MINE};
    }

    public static class UpdateInfoEntry implements BaseColumns {
        public static final String TABLE_NAME = "UpdateInfo";
        public static final String COLUMN_USER_ID = "userID";
        public static final String COLUMN_LAST_TIME = "lastTime";

        public static final String[] COLUMNS_ALL = {_ID, COLUMN_USER_ID, COLUMN_LAST_TIME};
    }

    public static class CurrentStatusEntry implements BaseColumns {
        public static final String TABLE_NAME = "CurrentStatus";
        public static final String COLUMN_CURRENT_LOC = "currentLocation";
        public static final String COLUMN_CURRENT_SUB_LOC = "currentSubLocation";
        public static final String COLUMN_CURRENT_END_TIME = "currentEndTime";

        public static final String[] COLUMNS_ALL = {_ID, COLUMN_CURRENT_LOC, COLUMN_CURRENT_SUB_LOC, COLUMN_CURRENT_END_TIME};
    }
}
