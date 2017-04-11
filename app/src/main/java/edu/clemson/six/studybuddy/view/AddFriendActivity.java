package edu.clemson.six.studybuddy.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import edu.clemson.six.studybuddy.R;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSearching(false);
    }

    private void setSearching(boolean isSearching) {
        progressBarSearchFriends.setVisibility(isSearching ? View.VISIBLE : View.GONE);
        recyclerViewFriendsSearch.setVisibility(isSearching ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.btnSearch)
    public void onViewClicked() {
        setSearching(true);
    }
}
