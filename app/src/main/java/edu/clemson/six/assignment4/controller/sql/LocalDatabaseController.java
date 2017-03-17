package edu.clemson.six.assignment4.controller.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import edu.clemson.six.assignment4.model.Car;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by James Hollowell on 3/3/2017.
 */

public class LocalDatabaseController extends DatabaseController {

    final CarsDbHelper dbHelper;

    public LocalDatabaseController(Context context) {
        this.dbHelper = new CarsDbHelper(context);
    }

    @Override
    public List<Car> getCars() {
        List<Car> cars = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(CarsContract.CarEntry.TABLE_NAME, CarsContract.CarEntry.COLUMNS_ALL, null, null, null, null, CarsContract.CarEntry.COLUMN_NAME_SORT_ORDER);

        while (cursor.moveToNext()) {
            String make = cursor.getString(cursor.getColumnIndexOrThrow(CarsContract.CarEntry.COLUMN_NAME_MAKE));
            String model = cursor.getString(cursor.getColumnIndexOrThrow(CarsContract.CarEntry.COLUMN_NAME_MODEL));
            String license = cursor.getString(cursor.getColumnIndexOrThrow(CarsContract.CarEntry.COLUMN_NAME_LICENSE));
            String state = cursor.getString(cursor.getColumnIndexOrThrow(CarsContract.CarEntry.COLUMN_NAME_STATE));
            int color = cursor.getInt(cursor.getColumnIndexOrThrow(CarsContract.CarEntry.COLUMN_NAME_COLOR));
            int year = cursor.getInt(cursor.getColumnIndexOrThrow(CarsContract.CarEntry.COLUMN_NAME_YEAR));
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(CarsContract.CarEntry._ID));
            int sortOrder = cursor.getInt(cursor.getColumnIndexOrThrow(CarsContract.CarEntry.COLUMN_NAME_SORT_ORDER));
            boolean isDeleted = cursor.getInt(cursor.getColumnIndex(CarsContract.CarEntry.COLUMN_NAME_IS_DELETED)) == 1;
            int lastUpdate = cursor.getInt(cursor.getColumnIndex(CarsContract.CarEntry.COLUMN_NAME_LAST_UPDATE));

            Log.i("CarController", "Loaded new car from DB with id " + id);
            cars.add(new Car(null, make, model, license, state, color, year, id, sortOrder, lastUpdate, isDeleted));
        }

        cursor.close();
        return Collections.unmodifiableList(cars);
    }

    @Override
    public long addCar(Car car) {
        ContentValues cv = this.contentValuesFromCar(car);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insertOrThrow(CarsContract.CarEntry.TABLE_NAME, null, cv);
    }

    @Override
    public void updateCar(Car car) {
        ContentValues cv = this.contentValuesFromCar(car);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = CarsContract.CarEntry._ID + " = ?";
        String[] args = {String.valueOf(car.getId())};
        db.update(CarsContract.CarEntry.TABLE_NAME, cv, selection, args);
    }

    @Override
    public void deleteCar(Car car) {
        ContentValues cv = this.contentValuesFromCar(car);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = CarsContract.CarEntry._ID + " = ?";
        String[] args = {String.valueOf(car.getId())};
        db.update(CarsContract.CarEntry.TABLE_NAME, cv, selection, args);
    }

    @Override
    public void close() {
        dbHelper.close();
    }

    public int getMostRecentUserID() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(CarsContract.UpdateInfoEntry.TABLE_NAME, new String[]{CarsContract.UpdateInfoEntry.COLUMN_USER_ID}, null, null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex(CarsContract.UpdateInfoEntry.COLUMN_USER_ID));
            } else {
                return -1;
            }
        } finally {
            cursor.close();
        }
    }

    public long getMostRecentSync() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(CarsContract.UpdateInfoEntry.TABLE_NAME, new String[]{CarsContract.UpdateInfoEntry.COLUMN_LAST_TIME}, null, null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndex(CarsContract.UpdateInfoEntry.COLUMN_LAST_TIME));
            } else {
                return -1;
            }
        } finally {
            cursor.close();
        }
    }

    public void setMostRecentUserID(int userID) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CarsContract.UpdateInfoEntry.COLUMN_USER_ID, String.valueOf(userID));
        String selection = CarsContract.UpdateInfoEntry._ID + " = ?";
        String[] args = {"1"};
        db.update(CarsContract.UpdateInfoEntry.TABLE_NAME, cv, selection, args);
    }

    public void setMostRecentSync(long sync) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CarsContract.UpdateInfoEntry.COLUMN_LAST_TIME, String.valueOf(sync));
        String selection = CarsContract.UpdateInfoEntry._ID + " = ?";
        String[] args = {"1"};
        db.update(CarsContract.UpdateInfoEntry.TABLE_NAME, cv, selection, args);
    }

    /**
     * Synchronize a car to this database, adding it or updating it as appropriate
     * @param car Car to be synchronized
     */
    public void syncCar(Car car) {
        ContentValues cv = this.contentValuesFromCar(car);
        cv.put(CarsContract.CarEntry._ID, car.getId());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.insertOrThrow(CarsContract.CarEntry.TABLE_NAME, null, cv);
        }
        catch (SQLException e) {
            updateCar(car);
        }
    }

    public void clearCars() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(CarsContract.CarEntry.TABLE_NAME, "", null);
    }

}
