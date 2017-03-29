package edu.clemson.six.studybuddy.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.InjectView;
import edu.clemson.six.studybuddy.R;
import edu.clemson.six.studybuddy.controller.FriendsListAdapter;

public class FriendsActivity extends AppCompatActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.fab)
    FloatingActionButton fab;
    @InjectView(R.id.friends_list)
    RecyclerView friendsList;
    @InjectView(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);

        // Setup the RecyclerView
        friendsList.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        friendsList.setLayoutManager(layoutManager);

        friendsList.setAdapter(FriendsListAdapter.getInstance());

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(false);
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FriendsActivity.this, AddFriendActivity.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
