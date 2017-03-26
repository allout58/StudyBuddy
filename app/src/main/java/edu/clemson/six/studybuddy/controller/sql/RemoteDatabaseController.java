package edu.clemson.six.studybuddy.controller.sql;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.clemson.six.studybuddy.controller.LoginSessionController;
import edu.clemson.six.studybuddy.controller.net.APIConnector;
import edu.clemson.six.studybuddy.controller.net.ConnectionDetails;
import edu.clemson.six.studybuddy.model.Car;

/**
 * Created by James Hollowell on 3/3/2017.
 */

public class RemoteDatabaseController extends DatabaseController {

    @Override
    public List<Car> getCars() {
        return null;
    }

    @Override
    public long addCar(Car car) {
        APITask task = new APITask("create");
        task.execute(car);
        return 0;
    }

    @Override
    public void updateCar(Car car) {
        APITask task = new APITask("update");
        task.execute(car);
    }

    @Override
    public void deleteCar(Car car) {
        APITask task = new APITask("delete");
        task.execute(car);
    }

    @Override
    public void close() {

    }

    private static class APITask extends AsyncTask<Car, Void, JsonElement> {

        private final String endpoint;

        private APITask(String endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        protected JsonElement doInBackground(Car... params) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            Map<String, String> args = new HashMap<>();
            JsonObject obj = gson.toJsonTree(params[0]).getAsJsonObject();
            obj.addProperty("colorHex", Integer.toHexString(params[0].getColor()));
            args.put("data", obj.toString());
            args.put("userid", String.valueOf(LoginSessionController.getInstance(null).getUserID()));
            ConnectionDetails dets = APIConnector.setupConnection("car." + this.endpoint, args, ConnectionDetails.Method.POST);
            try {
                return APIConnector.connect(dets);
            } catch (IOException e) {
                Log.e("APITask", "Unable to connect to API. endpoint: " + this.endpoint, e);
            }
            return null;
        }
    }
}
