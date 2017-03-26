package edu.clemson.six.studybuddy.controller;

import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.clemson.six.studybuddy.controller.sql.DatabaseController;
import edu.clemson.six.studybuddy.controller.sql.UnifiedDatabaseController;
import edu.clemson.six.studybuddy.model.Car;
import edu.clemson.six.studybuddy.view.MainActivity;

/**
 * Singleton class that manages all the Car instances
 *
 * @author jthollo
 */
public class CarController {
    private static CarController ourInstance = new CarController();
    private List<Car> cars = new ArrayList<>();
    // Cars in `deleted` should NOT be in `cars`
    private List<Car> deleted = new ArrayList<>();
    // Cars in `trash` should NOT be in `cars` but still needs to be synced with the DB
    private List<Car> trash = new ArrayList<>();
    // Cars in `dirty` should also be in `cars`
    private List<Car> dirty = new ArrayList<>();

    private boolean trashExportMode = false;

    private MainActivity mainActivity;

    private CarController() {
    }

    /**
     * Get the singleton instance of the CarController
     *
     * @return the instance
     */
    public static CarController getInstance() {
        return ourInstance;
    }

    /**
     * Return the size of the data store
     *
     * @return Size
     */
    public int size() {
        return trashExportMode ? trash.size() : cars.size();
    }


    /**
     * Notify the Controller that the car in position `position` has been updated.
     * <p>
     * Works in both modes
     *
     * @param position Position that has been updated
     */
    public void notifyUpdated(int position) {
        List<Car> l = trashExportMode ? trash : cars;
        Car c = l.get(position);
        dirty.add(c);
        c.setLastUpdate(System.currentTimeMillis() / 1000);

    }

    /**
     * Add a car to this controller
     *
     * @param c The car to add
     */
    public void add(Car c) {
        if (trashExportMode)
            throw new UnsupportedOperationException("Can only add cars when in the Garage");
        dirty.add(c);
        // TODO: What do we want the sort order behavior to be?
        if (cars.size() == 0) {
            c.setSortOrder(0);
        } else {
            c.setSortOrder(cars.get(cars.size() - 1).getSortOrder() + 1);
        }
    }

    /**
     * Remove the car at position `position` from the controller
     * <p>
     * Warning: This action needs to be committed to the database in order to be permanent.
     *
     * @param position Position to be removed
     */
    public void remove(int position) {
        if (trashExportMode)
            throw new UnsupportedOperationException("Can only add cars when in the Garage");
        Car c = cars.remove(position);
        deleted.add(c);
    }

    /**
     * Swap two cars positionally.
     *
     * @param position1 Position one
     * @param position2 Position two
     */
    public void swap(int position1, int position2) {
        List<Car> l = trashExportMode ? trash : cars;
        Car c1 = l.get(position1);
        Car c2 = l.get(position2);
        if (c1.getSortOrder() != c2.getSortOrder()) {
            // Swap their sort order data
            int o1 = c1.getSortOrder();
            c1.setSortOrder(c2.getSortOrder());
            c2.setSortOrder(o1);
        } else {
            // Or incrementing one if they are the same
            if (position1 > position2) {
                c2.setSortOrder(c2.getSortOrder() + 1);
            } else {
                c1.setSortOrder(c1.getSortOrder() + 1);
            }
        }

        dirty.add(c1);
        dirty.add(c2);

        // Swap their location in the list
        Collections.swap(l, position1, position2);
//        MainActivity.updateHandler.obtainMessage(1, MainActivity.HANDLE_UPDATE_POSITION, position1).sendToTarget();
//        MainActivity.updateHandler.obtainMessage(1, MainActivity.HANDLE_UPDATE_POSITION, position2).sendToTarget();
    }

    /**
     * Get the car in position `position`
     *
     * @param position The position to fetch
     * @return Car at the given position
     */
    public Car get(int position) {
        return trashExportMode ? trash.get(position) : cars.get(position);
    }

    /**
     * Get the car with database ID `id`
     *
     * @param id Database ID
     * @return Car with given database ID
     */
    public Car getCarById(int id) {
        if (trashExportMode)
            throw new UnsupportedOperationException("Can only add cars when in the Garage");
        // Unfortunately, we can't optimize this as it is sorted by sort order, not by DB ID
        for (Car c : cars) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    /**
     * Commits all pending updates and additions
     */

    public void commitDirty() {
        if (dirty.size() == 0) {
            return;
        }
        DatabaseController db = UnifiedDatabaseController.getInstance(null);
        for (Car c : dirty) {
            if (c.isNew()) {
                // INSERT
                long id = db.addCar(c);
                c.setNew(false);
                c.setId((int) id);
                if (trashExportMode)
                    trash.add(c);
                else
                    cars.add(c);
                Log.d("CarController", "Adding new car, given id " + id);
            } else {
                // UPDATE
                db.updateCar(c);
                Log.d("CarController", "Updating car with id " + c.getId());
            }
        }

        dirty.clear();

        // If we are on the main thread, call update ourselves, else use the
//        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
//            CarListAdapter.getInstance().notifyDataSetChanged();
//        } else {
//            MainActivity.updateHandler.obtainMessage(1, MainActivity.HANDLE_UPDATE_ALL, 0).sendToTarget();
//        }
    }

    /**
     * Commit pending deletes to the database
     */
    public void commitDeletes() {
        if (trashExportMode)
            throw new UnsupportedOperationException("Can only add cars when in the Garage");
        if (deleted.size() == 0) {
            return;
        }

        for (Car c : deleted) {
            // DELETE
            c.setDeleted(true);
            trash.add(c);
            UnifiedDatabaseController.getInstance(null).deleteCar(c);
            Log.d("CarController", "Removing car with id " + c.getId());

        }

        deleted.clear();
//        // If we are on the main thread, call update ourselves, else use the
//        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
//            CarListAdapter.getInstance().notifyDataSetChanged();
//        } else {
//            MainActivity.updateHandler.obtainMessage(1, MainActivity.HANDLE_UPDATE_ALL, 0).sendToTarget();
//        }
    }

    /**
     * Remove a car from the trashcan
     *
     * @param c Car to remove
     */
    public void unDelete(Car c) {
        if (!trashExportMode)
            throw new UnsupportedOperationException("Can only add cars when in the Garage");
        c.setDeleted(false);
        cars.add(c);
        Collections.sort(cars, new Comparator<Car>() {
            @Override
            public int compare(Car o1, Car o2) {
                return Integer.valueOf(o1.getSortOrder()).compareTo(o2.getSortOrder());
            }
        });
        trash.remove(c);
        UnifiedDatabaseController.getInstance(null).undeleteCar(c);
        // If we are on the main thread, call update ourselves, else use the
//        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
//            CarListAdapter.getInstance().notifyDataSetChanged();
//        } else {
//            MainActivity.updateHandler.obtainMessage(1, MainActivity.HANDLE_UPDATE_ALL, 0).sendToTarget();
//        }
    }

    /**
     * Reverts all pending updates, deletes, and additions
     */
    public void revertDeletes() {
        cars.addAll(deleted);
        Collections.sort(cars, new Comparator<Car>() {
            @Override
            public int compare(Car o1, Car o2) {
                return Integer.valueOf(o1.getSortOrder()).compareTo(o2.getSortOrder());
            }
        });
        for (Car c : deleted) {
            c.setDeleted(false);
            trash.remove(c);
        }
        deleted.clear();
        // If we are on the main thread, call update ourselves, else use the
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            CarListAdapter.getInstance().notifyDataSetChanged();
        } else {
            MainActivity.updateHandler.obtainMessage(1, MainActivity.HANDLE_UPDATE_ALL, 0).sendToTarget();
        }
    }

    /**
     * Reload the CarController from the database
     */
    public void reload() {
        cars.clear();
        deleted.clear();
        dirty.clear();
        trash.clear();
        load();
    }

    /**
     * Load the CarController from the database
     */
    private void load() {
        List<Car> c = UnifiedDatabaseController.getInstance(null).getCars();
        for (Car x : c) {
            if (x.isDeleted()) {
                trash.add(x);
            } else {
                cars.add(x);
            }
        }
        // If we are on the main thread, call update ourselves, else use the
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            CarListAdapter.getInstance().notifyDataSetChanged();
        } else {
            MainActivity.updateHandler.obtainMessage(1, MainActivity.HANDLE_UPDATE_ALL, 0).sendToTarget();
        }
    }

    public boolean isTrashExportMode() {
        return trashExportMode;
    }

    public void setTrashExportMode(boolean trashExportMode) {
        this.trashExportMode = trashExportMode;
        Log.d("CarController", "Changed Trash mode to " + this.trashExportMode);
    }
}
