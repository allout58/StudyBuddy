package edu.clemson.six.studybuddy.controller.sql;

import android.content.ContentValues;

import java.util.List;

import edu.clemson.six.studybuddy.model.Car;

/**
 * Abstract Class for a database controller
 */

public abstract class DatabaseController {
    protected ContentValues contentValuesFromCar(Car car) {
        ContentValues cv = new ContentValues();
        cv.put(CarsContract.CarEntry.COLUMN_NAME_MAKE, car.getMake());
        cv.put(CarsContract.CarEntry.COLUMN_NAME_MODEL, car.getModel());
        cv.put(CarsContract.CarEntry.COLUMN_NAME_LICENSE, car.getLicense());
        cv.put(CarsContract.CarEntry.COLUMN_NAME_STATE, car.getState());
        cv.put(CarsContract.CarEntry.COLUMN_NAME_YEAR, car.getYear());
        cv.put(CarsContract.CarEntry.COLUMN_NAME_COLOR, car.getColor());
        cv.put(CarsContract.CarEntry.COLUMN_NAME_SORT_ORDER, car.getSortOrder());
        cv.put(CarsContract.CarEntry.COLUMN_NAME_LAST_UPDATE, (int)(System.currentTimeMillis()/1000));
        cv.put(CarsContract.CarEntry.COLUMN_NAME_IS_DELETED, car.isDeleted()?1:0);
        return cv;
    }

//    protected ContentValues contentValuesFromUser(User user) {
//
//    }

    public abstract List<Car> getCars();

    /**
     * Add a new car to the database
     * @param car Car to add
     * @return The database ID of the added car
     */
    public abstract long addCar(Car car);
    public abstract void updateCar(Car car);

    /**
     * Lazy deletes a car from the database
     * @param car Car to lazy delete
     */
    public abstract void deleteCar(Car car);
//
//    public abstract void addUser(User user);
//
//    public abstract void addUserToCar(User user, Car car);

    public abstract void close();


    /**
     * Unmarks a car as delete in the database
     * @param c Car to lazy undelete
     */
    public void undeleteCar(Car c) {
        // Hax because we are just toggling a bit, so we send the same thing with a different bit :D
        deleteCar(c);
    }
}
