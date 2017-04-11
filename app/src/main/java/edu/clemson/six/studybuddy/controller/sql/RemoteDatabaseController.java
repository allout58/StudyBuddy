package edu.clemson.six.studybuddy.controller.sql;

import java.util.List;

import edu.clemson.six.studybuddy.model.Friend;
import edu.clemson.six.studybuddy.model.Location;

/**
 * Created by James Hollowell on 3/3/2017.
 */

public class RemoteDatabaseController extends DatabaseController {

    // TODO: Refactor this out... It isn't really needed with this setup
    @Override
    public List<Location> getLocations() {
        return null;
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

    }

//    private static class APITask extends AsyncTask<Car, Void, JsonElement> {
//
//        private final String endpoint;
//
//        private APITask(String endpoint) {
//            this.endpoint = endpoint;
//        }
//
//        @Override
//        protected JsonElement doInBackground(Car... params) {
//            Gson gson = new GsonBuilder().serializeNulls().create();
//            Map<String, String> args = new HashMap<>();
//            JsonObject obj = gson.toJsonTree(params[0]).getAsJsonObject();
//            args.put("data", obj.toString());
//            args.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
//            ConnectionDetails dets = APIConnector.setupConnection("car." + this.endpoint, args, ConnectionDetails.Method.POST);
//            try {
//                return APIConnector.connect(dets);
//            } catch (IOException e) {
//                Log.e("APITask", "Unable to connect to API. endpoint: " + this.endpoint, e);
//            }
//            return null;
//        }
//    }
}
