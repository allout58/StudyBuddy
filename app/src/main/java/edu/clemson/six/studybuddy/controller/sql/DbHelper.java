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
                    "%s INTEGER," +
                    "%s VARCHAR(80)," +
                    "%s FLOAT," +
                    "%s FLOAT," +
                    "%s INTEGER" +
                    ")",
            DBContract.LocationsContract.TABLE_NAME,
            DBContract.LocationsContract.COLUMN_ID,
            DBContract.LocationsContract.COLUMN_NAME,
            DBContract.LocationsContract.COLUMN_LONG,
            DBContract.LocationsContract.COLUMN_LAT,
            DBContract.LocationsContract.COLUMN_RADIUS
    );

    private static final String SQL_CREATE_SUB_LOC = String.format("CREAT TABLE %s (" +
                    "%s INTEGER," +
                    "%s VARCHAR(80)," +
                    "%s INTEGER" +
                    ")",
            DBContract.SubLocationsContract.TABLE_NAME,
            DBContract.SubLocationsContract.COLUMN_ID,
            DBContract.SubLocationsContract.COLUMN_NAME,
            DBContract.SubLocationsContract.COLUMN_LOCATION
    );

    private static final String SQL_CREATE_FRIENDS = String.format("CREATE TABLE %s (" +
                    "%s VARCHAR(48)," +
                    "%s VARCHAR(60)," +
                    "%s INTEGER," +
                    "%s INTEGER," +
                    "%s TEXT," +
                    "%s TIME," +
                    "%s INTEGER" +
                    ")",
            DBContract.FriendsContract.TABLE_NAME,
            DBContract.FriendsContract.COLUMN_UID,
            DBContract.FriendsContract.COLUMN_NAME,
            DBContract.FriendsContract.COLUMN_LOCATION,
            DBContract.FriendsContract.COLUMN_SUBLOCATION,
            DBContract.FriendsContract.COLUMN_BLURB,
            DBContract.FriendsContract.COLUMN_END_TIME,
            DBContract.FriendsContract.COLUMN_CONFIRMED
    );

    private static final String SQL_DELETE_LOC = "DROP TABLE IF EXISTS " + DBContract.LocationsContract.TABLE_NAME;
    private static final String SQL_DELETE_SUB_LOC = "DROP TABLE IF EXISTS " + DBContract.SubLocationsContract.TABLE_NAME;
    private static final String SQL_DELETE_FRIENDS = "DROP TABLE IF EXISTS " + DBContract.FriendsContract.TABLE_NAME;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_LOC);
        db.execSQL(SQL_CREATE_SUB_LOC);
        db.execSQL(SQL_CREATE_FRIENDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: Smarter upgrade than recreate DB
        db.execSQL(SQL_DELETE_SUB_LOC);
        db.execSQL(SQL_DELETE_LOC);
        db.execSQL(SQL_DELETE_FRIENDS);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: Smarter downgrade than recreate DB
        onUpgrade(db, oldVersion, newVersion);
    }
}
