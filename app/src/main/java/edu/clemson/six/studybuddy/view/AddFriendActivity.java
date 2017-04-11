package edu.clemson.six.studybuddy.view;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import edu.clemson.six.studybuddy.R;
import edu.clemson.six.studybuddy.controller.FriendSearchListAdapter;
import edu.clemson.six.studybuddy.controller.net.APIConnector;
import edu.clemson.six.studybuddy.controller.net.ConnectionDetails;
import edu.clemson.six.studybuddy.model.Friend;

public class AddFriendActivity extends AppCompatActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.editTextSearch)
    EditText editTextSearch;
    @InjectView(R.id.btnSearch)
    Button btnSearch;
    @InjectView(R.id.recyclerViewFriendsSearch)
    RecyclerView recyclerViewFriendsSearch;
    @InjectView(R.id.progressBarSearchFriends)
    ProgressBar progressBarSearchFriends;

    private FriendSearchListAdapter adapter = new FriendSearchListAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSearching(false);

        // Setup the RecyclerView
        recyclerViewFriendsSearch.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewFriendsSearch.setLayoutManager(layoutManager);

        recyclerViewFriendsSearch.setAdapter(adapter);
    }

    private void setSearching(boolean isSearching) {
        progressBarSearchFriends.setVisibility(isSearching ? View.VISIBLE : View.GONE);
        recyclerViewFriendsSearch.setVisibility(isSearching ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.btnSearch)
    public void onViewClicked() {
        setSearching(true);
        FirebaseAuth.getInstance().getCurrentUser().getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                SearchFriendsTask t = new SearchFriendsTask();
                t.execute(task.getResult().getToken(), editTextSearch.getText().toString());
            }
        });
    }

    private class SearchFriendsTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            adapter.friendList.clear();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            adapter.notifyDataSetChanged();
            setSearching(false);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Map<String, String> args = new HashMap<>();
            args.put("jwt", params[0]);
            args.put("search", params[1]);
            ConnectionDetails con = APIConnector.setupConnection("friend.search", args, ConnectionDetails.Method.POST);
            try {
                JsonObject obj = APIConnector.connect(con).getAsJsonObject();
                if (obj.has("status") && obj.get("status").getAsString().equals("success")) {
                    for (JsonElement el : obj.getAsJsonObject().get("results").getAsJsonArray()) {
                        JsonObject elO = el.getAsJsonObject();
                        Friend f = new Friend(elO.get("firebase_uid").getAsString(), elO.get("imageURL").getAsString(), elO.get("realName").getAsString(), null, null, "", null, false);
                        adapter.friendList.add(f);
                    }
                } else {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }
}
