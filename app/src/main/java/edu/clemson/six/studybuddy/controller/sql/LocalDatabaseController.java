package edu.clemson.six.studybuddy.controller.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.clemson.six.studybuddy.model.Friend;
import edu.clemson.six.studybuddy.model.Location;
import edu.clemson.six.studybuddy.model.SubLocation;

/**
 * Created by James Hollowell on 3/3/2017.
 */

public class LocalDatabaseController extends DatabaseController {

    private static final String TAG = "LocalDB";

    final DbHelper dbHelper;

    public LocalDatabaseController(Context context) {
        this.dbHelper = new DbHelper(context);
    }

    @Override
    public List<Location> getLocations() {
        List<Location> l = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DBContract.LocationsContract.TABLE_NAME, DBContract.LocationsContract.COLUMNS_ALL, "", null, null, null, null);
        while (c.moveToNext()) {
            try {
                Location loc = new Location(c.getInt(c.getColumnIndex(DBContract.LocationsContract.COLUMN_ID)),
                        c.getDouble(c.getColumnIndex(DBContract.LocationsContract.COLUMN_LONG)),
                        c.getDouble(c.getColumnIndex(DBContract.LocationsContract.COLUMN_LAT)),
                        c.getDouble(c.getColumnIndex(DBContract.LocationsContract.COLUMN_RADIUS)),
                        c.getString(c.getColumnIndex(DBContract.LocationsContract.COLUMN_NAME)));
                addSubLocationsForLocation(loc);
                l.add(loc);
            } catch (SQLException e) {
                Log.e(TAG, "Unable to get location from db", e);
            }
        }
        c.close();
        return l;
    }

    private void addSubLocationsForLocation(Location location) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DBContract.SubLocationsContract.TABLE_NAME, DBContract.SubLocationsContract.COLUMNS_ALL, DBContract.SubLocationsContract.COLUMN_LOCATION + " = ?", new String[]{String.valueOf(location.getId())}, null, null, null);
        while (c.moveToNext()) {
            SubLocation l = new SubLocation(c.getInt(c.getColumnIndex(DBContract.SubLocationsContract.COLUMN_ID)),
                    c.getString(c.getColumnIndex(DBContract.SubLocationsContract.COLUMN_NAME)),
                    location);
            location.addSubLocation(l);
        }
        c.close();
    }

    @Override
    public List<Friend> getFriends() {
        return null;
    }

    @Override
    public List<Friend> getUsersLike(String name) {
        return null;
    }

    @Override
    public void requestFriend(String uid) {

    }

    @Override
    public void acceptRequest(String uid) {

    }

    @Override
    public void close() {
        dbHelper.close();
    }

    public String getMostRecentUserID() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBContract.UpdateInfoEntry.TABLE_NAME, new String[]{DBContract.UpdateInfoEntry.COLUMN_USER_ID}, null, null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(DBContract.UpdateInfoEntry.COLUMN_USER_ID));
            } else {
                return "";
            }
        } finally {
            cursor.close();
        }
    }

    public void setMostRecentUserID(String userID) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBContract.UpdateInfoEntry.COLUMN_USER_ID, userID);
        String selection = DBContract.UpdateInfoEntry._ID + " = ?";
        String[] args = {"1"};
        db.update(DBContract.UpdateInfoEntry.TABLE_NAME, cv, selection, args);
    }

    public long getMostRecentSync() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DBContract.UpdateInfoEntry.TABLE_NAME, new String[]{DBContract.UpdateInfoEntry.COLUMN_LAST_TIME}, null, null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndex(DBContract.UpdateInfoEntry.COLUMN_LAST_TIME));
            } else {
                return 0;
            }
        } finally {
            cursor.close();
        }
    }

    public void setMostRecentSync(long sync) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBContract.UpdateInfoEntry.COLUMN_LAST_TIME, String.valueOf(sync));
        String selection = DBContract.UpdateInfoEntry._ID + " = ?";
        String[] args = {"1"};
        db.update(DBContract.UpdateInfoEntry.TABLE_NAME, cv, selection, args);
    }

    public void syncLocation(Location l) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBContract.LocationsContract.COLUMN_ID, l.getId());
        cv.put(DBContract.LocationsContract.COLUMN_NAME, l.getName());
        cv.put(DBContract.LocationsContract.COLUMN_LAT, l.getLatitude());
        cv.put(DBContract.LocationsContract.COLUMN_LONG, l.getLongitude());
        cv.put(DBContract.LocationsContract.COLUMN_RADIUS, l.getRadius());

        try {
            db.insertOrThrow(DBContract.LocationsContract.TABLE_NAME, null, cv);
        } catch (SQLException e) {
            String selection = DBContract.LocationsContract.COLUMN_ID + " = ?";
            String[] args = {String.valueOf(l.getId())};
            cv.remove(DBContract.LocationsContract.COLUMN_ID);
            db.update(DBContract.LocationsContract.TABLE_NAME, cv, selection, args);
        }
    }

    public void syncSubLocation(SubLocation sl) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBContract.SubLocationsContract.COLUMN_ID, sl.getId());
        cv.put(DBContract.SubLocationsContract.COLUMN_NAME, sl.getName());
        cv.put(DBContract.SubLocationsContract.COLUMN_LOCATION, sl.getParent().getId());

        try {
            db.insertOrThrow(DBContract.SubLocationsContract.TABLE_NAME, null, cv);
        } catch (SQLException e) {
            String selection = DBContract.SubLocationsContract.COLUMN_ID + " = ?";
            String[] args = {String.valueOf(sl.getId())};
            cv.remove(DBContract.SubLocationsContract.COLUMN_ID);
            db.update(DBContract.SubLocationsContract.TABLE_NAME, cv, selection, args);
        }
    }

    public void syncFriend(Friend f) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBContract.FriendsContract.COLUMN_UID, f.getUid());
        cv.put(DBContract.FriendsContract.COLUMN_NAME, f.getName());
        cv.put(DBContract.FriendsContract.COLUMN_LOCATION, f.getLocation().getId());
        cv.put(DBContract.FriendsContract.COLUMN_SUBLOCATION, f.getSubLocation().getId());
        cv.put(DBContract.FriendsContract.COLUMN_BLURB, f.getBlurb());
        cv.put(DBContract.FriendsContract.COLUMN_END_TIME, f.getEndTime().toString());
        cv.put(DBContract.FriendsContract.COLUMN_CONFIRMED, f.isConfirmed());

        try {
            db.insertOrThrow(DBContract.FriendsContract.TABLE_NAME, null, cv);
        } catch (SQLException e) {
            String selection = DBContract.FriendsContract.COLUMN_UID + " = ?";
            String[] args = {String.valueOf(f.getUid())};
            cv.remove(DBContract.FriendsContract.COLUMN_UID);
            db.update(DBContract.FriendsContract.TABLE_NAME, cv, selection, args);
        }
    }

}
