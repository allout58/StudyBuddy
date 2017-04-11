package edu.clemson.six.studybuddy.controller.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jthollo on 3/28/2017.
 */

public class DbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "StudyBuddy.db";

    private static final String SQL_CREATE_LOC = String.format("CREATE TABLE %s (" +
                    "%s INTEGER UNIQUE," +
                    "%s VARCHAR(80)," +
                    "%s DOUBLE," +
                    "%s DOUBLE," +
                    "%s DOUBLE" +
                    ")",
            DBContract.LocationsContract.TABLE_NAME,
            DBContract.LocationsContract.COLUMN_ID,
            DBContract.LocationsContract.COLUMN_NAME,
            DBContract.LocationsContract.COLUMN_LONG,
            DBContract.LocationsContract.COLUMN_LAT,
            DBContract.LocationsContract.COLUMN_RADIUS
    );

    private static final String SQL_CREATE_SUB_LOC = String.format("CREATE TABLE %s (" +
                    "%s INTEGER UNIQUE," +
                    "%s VARCHAR(80)," +
                    "%s INTEGER" +
                    ")",
            DBContract.SubLocationsContract.TABLE_NAME,
            DBContract.SubLocationsContract.COLUMN_ID,
            DBContract.SubLocationsContract.COLUMN_NAME,
            DBContract.SubLocationsContract.COLUMN_LOCATION
    );

    private static final String SQL_CREATE_FRIENDS = String.format("CREATE TABLE %s (" +
                    "%s VARCHAR(48) UNIQUE," +
                    "%s VARCHAR(200)," +
                    "%s VARCHAR(60)," +
                    "%s INTEGER," +
                    "%s INTEGER," +
                    "%s TEXT," +
                    "%s INTEGER" +
                    ")",
            DBContract.FriendsContract.TABLE_NAME,
            DBContract.FriendsContract.COLUMN_UID,
            DBContract.FriendsContract.COLUMN_IMAGE_URL,
            DBContract.FriendsContract.COLUMN_NAME,
            DBContract.FriendsContract.COLUMN_LOCATION,
            DBContract.FriendsContract.COLUMN_SUBLOCATION,
            DBContract.FriendsContract.COLUMN_BLURB,
            DBContract.FriendsContract.COLUMN_END_TIME
    );

    private static final String SQL_CREATE_UPDATE_INFO = String.format("CREATE TABLE %s (" +
                    "%s INTEGER PRIMARY KEY," +
                    "%s VARCHAR(64)," +
                    "%s INTEGER" +
                    ")",
            DBContract.UpdateInfoEntry.TABLE_NAME,
            DBContract.UpdateInfoEntry._ID,
            DBContract.UpdateInfoEntry.COLUMN_USER_ID,
            DBContract.UpdateInfoEntry.COLUMN_LAST_TIME);

    private static final String SQL_CREATE_FRIEND_REQUEST = String.format("CREATE TABLE %s (" +
                    "%s VARCHAR(64) UNIQUE," +
                    "%s VARCHAR(60)," +
                    "%s VARCHAR(200)," +
                    "%s INTEGER" +
                    ")",
            DBContract.FriendsRequestsContract.TABLE_NAME,
            DBContract.FriendsRequestsContract.COLUMN_UID,
            DBContract.FriendsRequestsContract.COLUMN_NAME,
            DBContract.FriendsRequestsContract.COLUMN_IMAGE_URL,
            DBContract.FriendsRequestsContract.COLUMN_IS_MINE);

    private static final String SQL_DELETE_LOC = "DROP TABLE IF EXISTS " + DBContract.LocationsContract.TABLE_NAME;
    private static final String SQL_DELETE_SUB_LOC = "DROP TABLE IF EXISTS " + DBContract.SubLocationsContract.TABLE_NAME;
    private static final String SQL_DELETE_FRIENDS = "DROP TABLE IF EXISTS " + DBContract.FriendsContract.TABLE_NAME;
    private static final String SQL_DELETE_UPDATE_INFO = "DROP TABLE IF EXISTS " + DBContract.UpdateInfoEntry.TABLE_NAME;
    private static final String SQL_DELETE_FRIEND_REQUET = "DROP TABLE IF EXISTS " + DBContract.FriendsRequestsContract.TABLE_NAME;

    private static final String SQL_INITIALIZE_UPDATE_INFO = String.format("INSERT INTO %s (%s, %s, %s) VALUES (1, '', 0)",
            DBContract.UpdateInfoEntry.TABLE_NAME,
            DBContract.UpdateInfoEntry._ID,
            DBContract.UpdateInfoEntry.COLUMN_USER_ID,
            DBContract.UpdateInfoEntry.COLUMN_LAST_TIME);

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_LOC);
        db.execSQL(SQL_CREATE_SUB_LOC);
        db.execSQL(SQL_CREATE_FRIENDS);
        db.execSQL(SQL_CREATE_UPDATE_INFO);
        db.execSQL(SQL_CREATE_FRIEND_REQUEST);

        db.execSQL(SQL_INITIALIZE_UPDATE_INFO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: Smarter upgrade than recreate DB
        db.execSQL(SQL_DELETE_SUB_LOC);
        db.execSQL(SQL_DELETE_LOC);
        db.execSQL(SQL_DELETE_FRIENDS);
        db.execSQL(SQL_DELETE_UPDATE_INFO);
        db.execSQL(SQL_DELETE_FRIEND_REQUET);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: Smarter downgrade than recreate DB
        onUpgrade(db, oldVersion, newVersion);
    }
}
