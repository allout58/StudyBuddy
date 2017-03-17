package edu.clemson.six.assignment4.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.TextView;

import edu.clemson.six.assignment4.OnStartDragListener;
import edu.clemson.six.assignment4.R;
import edu.clemson.six.assignment4.controller.CarController;
import edu.clemson.six.assignment4.controller.CarListAdapter;
import edu.clemson.six.assignment4.controller.LoginSessionController;
import edu.clemson.six.assignment4.controller.SyncController;
import edu.clemson.six.assignment4.controller.sql.UnifiedDatabaseController;
import edu.clemson.six.assignment4.view.helper.ItemTouchHelperAdapter;
import edu.clemson.six.assignment4.view.helper.SwipeHelper;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity implements ItemTouchHelperAdapter, NavigationView.OnNavigationItemSelectedListener {

    public static final String INTENT_CAR_ID = "com.jameshollowell.assignment3.intents.car_id";
    public static final String INTENT_CAR_ORDER = "com.jamshollowell.assignment3.intents.car_order";
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
    @InjectView(R.id.fab)
    protected FloatingActionButton fab;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize ButterKnife
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);


        if (LoginSessionController.getInstance().getUserID() == -1) {
            Intent intent_login = new Intent(this, LoginActivity.class);
            startActivity(intent_login);
        }

        // Setup the FAB
        final Intent intent_addCar = new Intent(this, AddCarActivity.class);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent_addCar);
            }
        });

        // Setup the Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mainDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mainDrawer.setDrawerListener(toggle);
        toggle.syncState();
        navView.setNavigationItemSelectedListener(this);
        navView.getMenu().getItem(CarController.getInstance().isTrashExportMode() ? 1 : 0).setChecked(true);
        setTrashMode(CarController.getInstance().isTrashExportMode());
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
            case R.id.nav_garage:
                setTrashMode(false);
                item.setChecked(true);
                recyclerView.requestLayout();
                CarListAdapter.getInstance().notifyDataSetChanged();
                break;
            case R.id.nav_delete:
                setTrashMode(true);
                item.setChecked(true);
                recyclerView.requestLayout();
                CarListAdapter.getInstance().notifyDataSetChanged();
                break;
            case R.id.action_info:
                startActivity(new Intent(this, InformationActivity.class));
                break;
            case R.id.action_logout:
                LoginSessionController.getInstance().setUserID(-1);
                startActivity(new Intent(this, LoginActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        mainDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setTrashMode(boolean mode) {
        CarController.getInstance().setTrashExportMode(mode);
        fab.setVisibility(mode ? View.GONE : View.VISIBLE);
        textViewMain.setText(mode ? R.string.text_main_trash : R.string.text_main);
    }
}
