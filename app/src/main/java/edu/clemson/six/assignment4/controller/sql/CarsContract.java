package edu.clemson.six.assignment4.controller.sql;

import android.provider.BaseColumns;

/**
 * Contract for the scheme of our SQLite DB
 */

public class CarsContract {
    private CarsContract() {
    }


    public static class UpdateInfoEntry implements BaseColumns {
        public static final String TABLE_NAME ="UpdateInfo";
        public static final String COLUMN_USER_ID = "userID";
        public static final String COLUMN_LAST_TIME = "lastTime";

        public static final String[] COLUMNS_ALL = {_ID, COLUMN_USER_ID, COLUMN_LAST_TIME};
    }

    public static class CarEntry implements BaseColumns {
        public static final String TABLE_NAME = "Cars";
        public static final String COLUMN_NAME_MAKE = "make";
        public static final String COLUMN_NAME_MODEL = "model";
        public static final String COLUMN_NAME_LICENSE = "license";
        public static final String COLUMN_NAME_STATE = "state";
        public static final String COLUMN_NAME_COLOR = "color";
        public static final String COLUMN_NAME_YEAR = "year";
        public static final String COLUMN_NAME_SORT_ORDER = "sort_order";
        public static final String COLUMN_NAME_IS_DELETED = "isDeleted";
        public static final String COLUMN_NAME_LAST_UPDATE = "last_update";

        public static final String[] COLUMNS_ALL = {_ID, COLUMN_NAME_MAKE, COLUMN_NAME_MODEL, COLUMN_NAME_LICENSE, COLUMN_NAME_STATE, COLUMN_NAME_COLOR, COLUMN_NAME_YEAR, COLUMN_NAME_SORT_ORDER, COLUMN_NAME_IS_DELETED, COLUMN_NAME_LAST_UPDATE};
    }

    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "Users";
        public static final String COLUMN_USERNAME = "username";

        public static final String[] COLUMNS_ALL = {_ID, COLUMN_USERNAME};
    }

    public static class UserCarEntry implements BaseColumns {
        public static final String TABLE_NAME = "UsersCars";
        public static final String COLUMN_USER_ID = "userID";
        public static final String COLUMN_CAR_ID = "carID";

        public static final String[]  COLUMNS_ALL = {_ID, COLUMN_USER_ID, COLUMN_CAR_ID};
    }
}
