package edu.clemson.six.studybuddy.controller.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Database Helper
 *
 * @author jthollo
 */

class CarsDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "Cars.db";

    private static final String SQL_CREATE_CARS = String.format("CREATE TABLE %s (" +
                    "%s INTEGER PRIMARY KEY," +
                    "%s VARCHAR(20)," +
                    "%s VARCHAR(40)," +
                    "%s VARCHAR(15)," +
                    "%s CHAR(2)," +
                    "%s INTEGER," +
                    "%s INTEGER," +
                    "%s INTEGER," +
                    "%s INTEGER," +
                    "%s INTEGER" +
                    ")",
            CarsContract.CarEntry.TABLE_NAME,
            CarsContract.CarEntry._ID,
            CarsContract.CarEntry.COLUMN_NAME_MAKE,
            CarsContract.CarEntry.COLUMN_NAME_MODEL,
            CarsContract.CarEntry.COLUMN_NAME_LICENSE,
            CarsContract.CarEntry.COLUMN_NAME_STATE,
            CarsContract.CarEntry.COLUMN_NAME_COLOR,
            CarsContract.CarEntry.COLUMN_NAME_YEAR,
            CarsContract.CarEntry.COLUMN_NAME_SORT_ORDER,
            CarsContract.CarEntry.COLUMN_NAME_IS_DELETED,
            CarsContract.CarEntry.COLUMN_NAME_LAST_UPDATE);

    private static final String SQL_CREATE_USERS = String.format("CREATE TABLE %s (" +
            "%s INTEGER PRIMARY KEY," +
            "%s VARCHAR(40)" +
            ")",
            CarsContract.UserEntry.TABLE_NAME,
            CarsContract.UserEntry._ID,
            CarsContract.UserEntry.COLUMN_USERNAME);

    private static final String SQL_CREATE_USERS_CARS = String.format("CREATE TABLE %s (" +
            "%s INTEGER PRIMARY KEY," +
            "%s INTEGER," +
            "%s INGEGER" +
            ")",
            CarsContract.UserCarEntry.TABLE_NAME,
            CarsContract.UserCarEntry._ID,
            CarsContract.UserCarEntry.COLUMN_USER_ID,
            CarsContract.UserCarEntry.COLUMN_CAR_ID);

    private static final String SQL_CREATE_UPDATE_INFO = String.format("CREATE TABLE %s (" +
            "%s INTEGER PRIMARY KEY," +
            "%s INTEGER," +
            "%s INGEGER" +
            ")",
            CarsContract.UpdateInfoEntry.TABLE_NAME,
            CarsContract.UpdateInfoEntry._ID,
            CarsContract.UpdateInfoEntry.COLUMN_USER_ID,
            CarsContract.UpdateInfoEntry.COLUMN_LAST_TIME);

    private static final String SQL_INITIALIZE_UPDATE_INFO = String.format("INSERT INTO %s (%s, %s, %s) VALUES (1, -1, -1)",
            CarsContract.UpdateInfoEntry.TABLE_NAME,
            CarsContract.UpdateInfoEntry._ID,
            CarsContract.UpdateInfoEntry.COLUMN_USER_ID,
            CarsContract.UpdateInfoEntry.COLUMN_LAST_TIME);

    private static final String SQL_DELETE_CARS = "DROP TABLE IF EXISTS " + CarsContract.CarEntry.TABLE_NAME;
    private static final String SQL_DELETE_USERS = "DROP TABLE IF EXISTS " + CarsContract.UserEntry.TABLE_NAME;
    private static final String SQL_DELETE_USERS_CARS = "DROP TABLE IF EXISTS " + CarsContract.UserCarEntry.TABLE_NAME;
    private static final String SQL_DELETE_UPDATE_INFO = "DROP TABLE IF EXISTS " + CarsContract.UpdateInfoEntry.TABLE_NAME;

    public CarsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CARS);
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_USERS_CARS);
        db.execSQL(SQL_CREATE_UPDATE_INFO);
        db.execSQL(SQL_INITIALIZE_UPDATE_INFO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: Better upgrade than just drop and recreate.
        Log.i("CarsDBHelper", "Upgrading DB from " + oldVersion + " to " + newVersion);
        db.execSQL(SQL_DELETE_CARS);
        db.execSQL(SQL_DELETE_USERS);
        db.execSQL(SQL_DELETE_USERS_CARS);
        db.execSQL(SQL_DELETE_UPDATE_INFO);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: Better upgrade than just drop and recreate.
        Log.i("CarsDBHelper", "Downgrading DB from " + oldVersion + " to " + newVersion);
        onUpgrade(db, oldVersion, newVersion);
    }
}
