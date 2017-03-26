package edu.clemson.six.studybuddy.controller.sql;

import android.content.Context;

import java.util.List;

import edu.clemson.six.studybuddy.model.Car;

/**
 * Created by jthollo on 3/8/2017.
 */

public class UnifiedDatabaseController extends DatabaseController {
    private static UnifiedDatabaseController instance;
    private LocalDatabaseController local;
    private RemoteDatabaseController remote;

    private UnifiedDatabaseController(Context context) {
        this.local = new LocalDatabaseController(context);
        this.remote = new RemoteDatabaseController();
    }

    public static synchronized UnifiedDatabaseController getInstance(Context context) {
        if (instance == null) {
            instance = new UnifiedDatabaseController(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public List<Car> getCars() {
        return this.local.getCars();
    }

    @Override
    public long addCar(Car car) {
        long localID = local.addCar(car);
        long remoteID = remote.addCar(car);
        return localID;
    }

    @Override
    public void updateCar(Car car) {
        local.updateCar(car);
        remote.updateCar(car);
    }

    @Override
    public void deleteCar(Car car) {
        local.deleteCar(car);
        remote.deleteCar(car);
    }

    @Override
    public void close() {
        local.close();
        // Courtesy call, not sure if this will be needed for remote ever, as each call is individual
        remote.close();
    }

    public LocalDatabaseController getLocal() {
        return local;
    }

    public RemoteDatabaseController getRemote() {
        return remote;
    }
}
