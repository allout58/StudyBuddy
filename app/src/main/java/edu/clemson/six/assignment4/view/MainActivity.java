package edu.clemson.six.assignment4.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import edu.clemson.six.assignment4.OnStartDragListener;
import edu.clemson.six.assignment4.R;
import edu.clemson.six.assignment4.controller.CarController;
import edu.clemson.six.assignment4.controller.CarListAdapter;
import edu.clemson.six.assignment4.controller.LoginSessionController;
import edu.clemson.six.assignment4.controller.SyncController;
import edu.clemson.six.assignment4.controller.net.APIConnector;
import edu.clemson.six.assignment4.controller.net.ConnectionDetails;
import edu.clemson.six.assignment4.controller.sql.UnifiedDatabaseController;
import edu.clemson.six.assignment4.view.helper.ItemTouchHelperAdapter;
import edu.clemson.six.assignment4.view.helper.SwipeHelper;

public class MainActivity extends AppCompatActivity implements ItemTouchHelperAdapter, NavigationView.OnNavigationItemSelectedListener {

    public static final int HANDLE_UPDATE_ALL = 1;
    public static final int HANDLE_UPDATE_POSITION = 2;
    // Initialize the Handler to allow non-ui threads to request a recycler-view update
    public static final Handler updateHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case HANDLE_UPDATE_ALL:
                    CarListAdapter.getInstance().notifyDataSetChanged();
                    break;
                case HANDLE_UPDATE_POSITION:
                    CarListAdapter.getInstance().notifyItemChanged(msg.arg2);
                default:
                    Log.e("UpdateHandler", "Invalid argument: " + msg.arg1);
            }
        }
    };
    @InjectView(R.id.toolbar)
    protected Toolbar toolbar;
    @InjectView(R.id.car_list)
    protected RecyclerView recyclerView;
    @InjectView(R.id.main_coordinator)
    protected CoordinatorLayout mainCoordinator;
    @InjectView(R.id.swipeContainer)
    protected SwipeRefreshLayout swipeContainer;
    @InjectView(R.id.main_drawer)
    protected DrawerLayout mainDrawer;

    protected LinearLayoutManager layoutManager;
    @InjectView(R.id.nav_view)
    NavigationView navView;
    @InjectView(R.id.textViewMain)
    TextView textViewMain;
    @InjectView(R.id.content_main)
    RelativeLayout contentMain;
    @InjectView(R.id.progressTokenLogin)
    LinearLayout contentLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize ButterKnife
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);

        LoginSessionController.getInstance(this);
        if (!LoginSessionController.getInstance(this).getUsername().isEmpty() && !LoginSessionController.getInstance(this).getToken().isEmpty()) {
            TokenLoginTask task = new TokenLoginTask();
            task.execute((Void[]) null);
        } else if (LoginSessionController.getInstance(this).getUserID() == -1) {
            Intent intent_login = new Intent(this, LoginActivity.class);
            startActivity(intent_login);
        }


        // Setup the Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mainDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mainDrawer.setDrawerListener(toggle);
        toggle.syncState();
        navView.setNavigationItemSelectedListener(this);
        ColorStateList csl = new ColorStateList(new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        },
                new int[]{
                        Color.rgb(255, 255, 255),
                        Color.rgb(255, 255, 255)
                });
        navView.setItemTextColor(csl);
        navView.setItemIconTintList(csl);

        // Setup the RecyclerView
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(CarListAdapter.getInstance());

        final SwipeHelper callback = new SwipeHelper();
        callback.addAdapter(CarListAdapter.getInstance());
        callback.addAdapter(this);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        CarListAdapter.getInstance().setOnStartDragListener(new OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                itemTouchHelper.startDrag(viewHolder);
            }
        });

        // Setup the SwipeRefreshContainer
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SyncController.getInstance().refresh(new Runnable() {
                    @Override
                    public void run() {
                        swipeContainer.setRefreshing(false);
                    }
                });
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        // Initialize the database connection
        UnifiedDatabaseController.getInstance(this);

        // Initialize the database storage saver
        new Timer("DBUpdateDirty").schedule(new TimerTask() {
            @Override
            public void run() {
                CarController.getInstance().commitDirty();
            }
        }, 10000, 10000);

        // Load the database
        //TODO: Turn this into an async task
        new Timer("DBInitLoad").schedule(new TimerTask() {
            @Override
            public void run() {
                CarController.getInstance().reload();
            }
        }, 0);
    }


    @Override
    public void onBackPressed() {
        if (mainDrawer.isDrawerOpen(GravityCompat.START)) {
            mainDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (!CarController.getInstance().isTrashExportMode()) {
            CarController.getInstance().commitDeletes();
        }
        CarController.getInstance().commitDirty();
        UnifiedDatabaseController.getInstance(null).close();
        super.onDestroy();
    }


    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return true;
    }

    @Override
    public void onItemDismiss(final int position) {
        if (CarController.getInstance().isTrashExportMode()) {
            Snackbar.make(mainCoordinator, R.string.alert_undelete, BaseTransientBottomBar.LENGTH_LONG).show();
        } else {
            final Snackbar s = Snackbar.make(mainCoordinator, R.string.alert_delete, BaseTransientBottomBar.LENGTH_INDEFINITE);
            final Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (s.isShown()) {
                        if (!CarController.getInstance().isTrashExportMode()) {
                            CarController.getInstance().commitDeletes();
                        }
                        s.dismiss();
                    }
                }
            }, 5000);
            s.setAction(R.string.action_undo, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CarController.getInstance().revertDeletes();
                    s.dismiss();
                    t.cancel();
                }
            });
            s.show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_friends:
                startActivity(new Intent(this, FriendsActivity.class));
                break;
            case R.id.nav_change_loc:
                startActivity(new Intent(this, ChangeLocationActivity.class));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_info:
                startActivity(new Intent(this, InformationActivity.class));
                break;
            case R.id.action_logout:
                LoginSessionController.getInstance(this).setUserID(-1);
                startActivity(new Intent(this, LoginActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        mainDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLoginProgress(boolean show) {
        Log.d("ShowLoginProgress", String.valueOf(show));
        contentLogin.setVisibility(show ? View.VISIBLE : View.GONE);
        contentMain.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void setTrashMode(boolean mode) {
        CarController.getInstance().setTrashExportMode(mode);
//        fab.setVisibility(mode ? View.GONE : View.VISIBLE);
        textViewMain.setText(mode ? R.string.text_main_trash : R.string.text_main);
    }

    private class TokenLoginTask extends AsyncTask<Void, Void, Boolean> {
        private int userID = -1;
        private long serverTimestampDiff = 0;
        private String realName;


        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d("TokenLoginTask", "Begin Background");
            Map<String, String> args = new HashMap<>();
            args.put("user", LoginSessionController.getInstance(MainActivity.this).getUsername());
            args.put("token", LoginSessionController.getInstance(MainActivity.this).getToken());
            ConnectionDetails con = APIConnector.setupConnection("user.login", args, ConnectionDetails.Method.POST);
            try {
                JsonObject obj = APIConnector.connect(con).getAsJsonObject();
                if (obj.has("userid")) {
                    long timestamp = obj.get("currentTime").getAsLong();
                    this.serverTimestampDiff = timestamp - System.currentTimeMillis() / 1000;
                    this.userID = obj.get("userid").getAsInt();
                    this.realName = obj.get("name").getAsString();
                    return true;
                } else {
                    return false;
                }

            } catch (IOException e) {
                // TODO Fallback to local login?
                Log.e("TokenLoginTask", "Error connecting to external API", e);
                userID = -2;
                return false;
            }
        }

        @Override
        protected void onPreExecute() {
            showLoginProgress(true);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            showLoginProgress(false);
            if (!result) {
                if (userID == -1) {
                    Intent intent_login = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent_login);
                } else {
                    Snackbar.make(mainCoordinator, R.string.error_bad_connection, Snackbar.LENGTH_LONG).show();
                }
            } else {
                LoginSessionController.getInstance(MainActivity.this).setUserID(this.userID);
                LoginSessionController.getInstance(MainActivity.this).setServerTimestampDiff(this.serverTimestampDiff);
                LoginSessionController.getInstance(MainActivity.this).setName(this.realName);
//                SyncController.getInstance().beginSync();
                Log.d("MainActivity-Login", "User ID: " + userID);
            }
            super.onPostExecute(result);
        }
    }
}
