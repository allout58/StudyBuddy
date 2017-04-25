package edu.clemson.six.studybuddy.controller.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.clemson.six.studybuddy.controller.LocationController;
import edu.clemson.six.studybuddy.model.Friend;
import edu.clemson.six.studybuddy.model.Location;
import edu.clemson.six.studybuddy.model.SubLocation;

/**
 * Created by James Hollowell on 3/3/2017.
 */

public class LocalDatabaseController extends DatabaseController {

    private static final String TAG = "LocalDB";
    private static LocalDatabaseController instance;
    final DbHelper dbHelper;

    private LocalDatabaseController(Context context) {
        this.dbHelper = new DbHelper(context);
    }

    public static LocalDatabaseController getInstance(Context context) {
        if (instance == null) {
            instance = new LocalDatabaseController(context.getApplicationContext());
        }
        return instance;
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

    public List<Friend> getRequests() {
        List<Friend> l = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.query(DBContract.FriendsRequestsContract.TABLE_NAME, DBContract.FriendsRequestsContract.COLUMNS_ALL, DBContract.FriendsRequestsContract.COLUMN_IS_MINE + " = 0", null, null, null, null);
        while (c.moveToNext()) {
            try {
                Friend friend = new Friend(
                        c.getString(c.getColumnIndex(DBContract.FriendsRequestsContract.COLUMN_UID)),
                        c.getString(c.getColumnIndex(DBContract.FriendsRequestsContract.COLUMN_IMAGE_URL)),
                        c.getString(c.getColumnIndex(DBContract.FriendsRequestsContract.COLUMN_NAME)),
                        null,
                        null,
                        "",
                        null,
                        false);
                l.add(friend);
            } catch (SQLException e) {
                Log.e(TAG, "Unable to get friend request from db", e);
            }
        }
        c.close();
        return l;
    }

    public List<Friend> getMyRequests() {
        List<Friend> l = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.query(DBContract.FriendsRequestsContract.TABLE_NAME, DBContract.FriendsRequestsContract.COLUMNS_ALL, DBContract.FriendsRequestsContract.COLUMN_IS_MINE + " = 1", null, null, null, null);
        while (c.moveToNext()) {
            try {
                Friend friend = new Friend(
                        c.getString(c.getColumnIndex(DBContract.FriendsRequestsContract.COLUMN_UID)),
                        c.getString(c.getColumnIndex(DBContract.FriendsRequestsContract.COLUMN_IMAGE_URL)),
                        c.getString(c.getColumnIndex(DBContract.FriendsRequestsContract.COLUMN_NAME)),
                        null,
                        null,
                        "",
                        null,
                        false);
                l.add(friend);
            } catch (SQLException e) {
                Log.e(TAG, "Unable to get friend request from db", e);
            }
        }
        c.close();
        return l;
    }


    @Override
    public List<Friend> getFriends() {
        List<Friend> l = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DBContract.FriendsContract.TABLE_NAME, DBContract.FriendsContract.COLUMNS_ALL, "", null, null, null, null);
        while (c.moveToNext()) {
            try {
                Location loc = LocationController.getInstance().getLocationById(c.getInt(c.getColumnIndex(DBContract.FriendsContract.COLUMN_LOCATION)));
                SubLocation sl = null;
                if (loc != null) {
                    sl = loc.getSubLocationByID(c.getInt(c.getColumnIndex(DBContract.FriendsContract.COLUMN_SUBLOCATION)));
                }
                long time = c.getLong(c.getColumnIndex(DBContract.FriendsContract.COLUMN_END_TIME));
                Date dt = null;
                if (time != 0) {
                    dt = new Date(time);
                }
                Friend friend = new Friend(
                        c.getString(c.getColumnIndex(DBContract.FriendsContract.COLUMN_UID)),
                        c.getString(c.getColumnIndex(DBContract.FriendsContract.COLUMN_IMAGE_URL)),
                        c.getString(c.getColumnIndex(DBContract.FriendsContract.COLUMN_NAME)),
                        loc,
                        sl,
                        c.getString(c.getColumnIndex(DBContract.FriendsContract.COLUMN_BLURB)),
                        dt,
                        true);
                l.add(friend);
            } catch (SQLException e) {
                Log.e(TAG, "Unable to get friends from db", e);
            }
        }
        c.close();
        return l;
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
        cv.put(DBContract.FriendsContract.COLUMN_IMAGE_URL, f.getImageURL());
        cv.put(DBContract.FriendsContract.COLUMN_NAME, f.getName());
        if (f.getLocation() != null) {
            cv.put(DBContract.FriendsContract.COLUMN_LOCATION, f.getLocation().getId());
        }
        if (f.getSubLocation() != null) {
            cv.put(DBContract.FriendsContract.COLUMN_SUBLOCATION, f.getSubLocation().getId());
        }
        cv.put(DBContract.FriendsContract.COLUMN_BLURB, f.getBlurb());
        if (f.getEndTime() != null) {
            cv.put(DBContract.FriendsContract.COLUMN_END_TIME, f.getEndTime().getTime());
        }
        try {
            db.insertOrThrow(DBContract.FriendsContract.TABLE_NAME, null, cv);
        } catch (SQLException e) {
            String selection = DBContract.FriendsContract.COLUMN_UID + " = ?";
            String[] args = {String.valueOf(f.getUid())};
            cv.remove(DBContract.FriendsContract.COLUMN_UID);
            db.update(DBContract.FriendsContract.TABLE_NAME, cv, selection, args);
        }
    }

    public void syncRequest(Friend f, boolean isMine) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBContract.FriendsRequestsContract.COLUMN_UID, f.getUid());
        cv.put(DBContract.FriendsRequestsContract.COLUMN_IMAGE_URL, f.getImageURL());
        cv.put(DBContract.FriendsRequestsContract.COLUMN_NAME, f.getName());
        cv.put(DBContract.FriendsRequestsContract.COLUMN_IS_MINE, isMine);
        try {
            db.insertOrThrow(DBContract.FriendsRequestsContract.TABLE_NAME, null, cv);
        } catch (SQLException e) {
            String selection = DBContract.FriendsRequestsContract.COLUMN_UID + " = ?";
            String[] args = {String.valueOf(f.getUid())};
            cv.remove(DBContract.FriendsRequestsContract.COLUMN_UID);
            db.update(DBContract.FriendsRequestsContract.TABLE_NAME, cv, selection, args);
        }
    }

    public void clearFriendsAndRequests() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + DBContract.FriendsContract.TABLE_NAME);
        db.execSQL("DELETE FROM " + DBContract.FriendsRequestsContract.TABLE_NAME);
    }

    public void removeFriend(Friend f) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DBContract.FriendsContract.TABLE_NAME, DBContract.FriendsContract.COLUMN_UID + " = ?", new String[]{f.getUid()});
    }

    public void removeRequest(Friend f) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DBContract.FriendsRequestsContract.TABLE_NAME, DBContract.FriendsRequestsContract.COLUMN_UID + " = ?", new String[]{f.getUid()});
    }

    public void setCurrentLocation(Location loc) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        int id = -2;
        if (loc != null) {
            id = loc.getId();
        }
        cv.put(DBContract.CurrentStatusEntry.COLUMN_CURRENT_LOC, String.valueOf(id));
        String selection = DBContract.CurrentStatusEntry._ID + " = ?";
        String[] args = {"1"};
        db.update(DBContract.CurrentStatusEntry.TABLE_NAME, cv, selection, args);
    }

    public void setCurrentSubLocation(SubLocation loc) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        int id = -2;
        if (loc != null) {
            id = loc.getId();
        }
        cv.put(DBContract.CurrentStatusEntry.COLUMN_CURRENT_SUB_LOC, String.valueOf(id));
        String selection = DBContract.CurrentStatusEntry._ID + " = ?";
        String[] args = {"1"};
        db.update(DBContract.CurrentStatusEntry.TABLE_NAME, cv, selection, args);
    }

    public int getCurrentLocationID() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DBContract.CurrentStatusEntry.TABLE_NAME, new String[]{DBContract.CurrentStatusEntry.COLUMN_CURRENT_LOC}, null, null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex(DBContract.CurrentStatusEntry.COLUMN_CURRENT_LOC));
            } else {
                return 0;
            }
        } finally {
            cursor.close();
        }
    }

    public int getCurrentSubLocationID() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DBContract.CurrentStatusEntry.TABLE_NAME, new String[]{DBContract.CurrentStatusEntry.COLUMN_CURRENT_SUB_LOC}, null, null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex(DBContract.CurrentStatusEntry.COLUMN_CURRENT_SUB_LOC));
            } else {
                return 0;
            }
        } finally {
            cursor.close();
        }
    }

    public long getCurrentEndTime() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DBContract.CurrentStatusEntry.TABLE_NAME, new String[]{DBContract.CurrentStatusEntry.COLUMN_CURRENT_END_TIME}, null, null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndex(DBContract.CurrentStatusEntry.COLUMN_CURRENT_END_TIME));
            } else {
                return 0;
            }
        } finally {
            cursor.close();
        }
    }

    public void setCurrentEndTime(Date time) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        long et = 0;
        if (time != null) {
            et = time.getTime();
        }
        cv.put(DBContract.CurrentStatusEntry.COLUMN_CURRENT_END_TIME, String.valueOf(et));
        String selection = DBContract.CurrentStatusEntry._ID + " = ?";
        String[] args = {"1"};
        db.update(DBContract.CurrentStatusEntry.TABLE_NAME, cv, selection, args);
    }

    public String getCurrentBlurb() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DBContract.CurrentStatusEntry.TABLE_NAME, new String[]{DBContract.CurrentStatusEntry.COLUMN_CURRENT_BLURB}, null, null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(DBContract.CurrentStatusEntry.COLUMN_CURRENT_BLURB));
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
    }

    public void setCurrentBlurb(String blurb) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBContract.CurrentStatusEntry.COLUMN_CURRENT_BLURB, blurb);
        String selection = DBContract.CurrentStatusEntry._ID + " = ?";
        String[] args = {"1"};
        db.update(DBContract.CurrentStatusEntry.TABLE_NAME, cv, selection, args);
    }
}
