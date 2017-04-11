package edu.clemson.six.studybuddy.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import edu.clemson.six.studybuddy.Constants;
import edu.clemson.six.studybuddy.R;
import edu.clemson.six.studybuddy.controller.LocationController;
import edu.clemson.six.studybuddy.controller.SyncController;
import edu.clemson.six.studybuddy.controller.net.APIConnector;
import edu.clemson.six.studybuddy.controller.net.ConnectionDetails;
import edu.clemson.six.studybuddy.controller.sql.LocalDatabaseController;
import edu.clemson.six.studybuddy.model.Location;
import edu.clemson.six.studybuddy.model.SubLocation;
import edu.clemson.six.studybuddy.view.component.CircleTransform;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

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

    TextView textViewUser;
    ImageView imageViewUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize ButterKnife
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);

        View v = navView.getHeaderView(0);
        textViewUser = (TextView) v.findViewById(R.id.textViewUser);
        imageViewUser = (ImageView) v.findViewById(R.id.imageViewUser);

        final FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            Snackbar.make(mainCoordinator, "Already logged in", Snackbar.LENGTH_LONG).show();
            auth.getCurrentUser().getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    if (task.isSuccessful()) {
                        VerifyTask verifyTask = new VerifyTask();
                        verifyTask.execute(task.getResult().getToken());
                    } else {
                        task.getException().printStackTrace();
                    }
                }
            });
        } else {
            launchFirebaseAuthUI();
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

        // Initialize the database connection
        LocalDatabaseController.getInstance(this);


        // Initialize the database storage saver
//        new Timer("DBUpdateDirty").schedule(new TimerTask() {
//            @Override
//            public void run() {
//                CarController.getInstance().commitDirty();
//            }
//        }, 10000, 10000);

        // Load the database
        //TODO: Turn this into an async task
//        new Timer("DBInitLoad").schedule(new TimerTask() {
//            @Override
//            public void run() {
//                CarController.getInstance().reload();
//            }
//        }, 0);
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
        LocalDatabaseController.getInstance(this).close();
        super.onDestroy();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_friends:
                startActivity(new Intent(this, FriendsActivity.class));
                break;
            case R.id.nav_map:
                startActivity(new Intent(this, MapActivity.class));
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
                Log.d("MainActivity", "Logging Out");
                AuthUI.getInstance().signOut(this);
                launchFirebaseAuthUI();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        mainDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLoginProgress(boolean show) {
        contentLogin.setVisibility(show ? View.VISIBLE : View.GONE);
        contentMain.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void launchFirebaseAuthUI() {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setLogo(R.mipmap.ic_launcher)
                .setTheme(R.style.LoginTheme)
                .setProviders(Arrays.asList(
                        new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                        new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()
                ))
                .build(), Constants.RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == ResultCodes.OK && response != null) {
                Snackbar.make(mainCoordinator, R.string.logged_in, Snackbar.LENGTH_LONG).show();
                FirebaseAuth.getInstance().getCurrentUser().getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            VerifyTask verifyTask = new VerifyTask();
                            verifyTask.execute(task.getResult().getToken());
                        } else {
                            task.getException().printStackTrace();
                        }
                    }
                });
            } else {
                if (response == null) {
                    // Signin canceled
                    return;
                }
                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    // No network connection
                    return;
                }
                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    // Unknown error
                    return;
                }
            }
            // Unknown signin response
        }
    }

    public class VerifyTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            Map<String, String> args = new HashMap<>();
            args.put("jwt", params[0]);
            args.put("name", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            if (FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null) {
                args.put("imageURL", FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
            }
            ConnectionDetails con = APIConnector.setupConnection("user.firebase_login", args, ConnectionDetails.Method.POST);
            try {
                JsonObject obj = APIConnector.connect(con).getAsJsonObject();
                long diff = obj.get("currentTime").getAsLong() - System.currentTimeMillis() / 1000;
                SyncController.getInstance().setServerTimeOffset(diff);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
                Log.d("VerifyTask", String.format("Name: %s, Image: %s", u.getDisplayName(), u.getPhotoUrl()));
                textViewUser.setText(u.getDisplayName());
                Picasso.with(MainActivity.this)
                        .load(u.getPhotoUrl())
                        .resizeDimen(R.dimen.person_view_size, R.dimen.person_view_size)
                        .transform(new CircleTransform())
                        .placeholder(R.drawable.ic_person_white_150dp)
                        .into(imageViewUser);
                SyncController.getInstance().syncLocations(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("VerifyTask", "Synchronized");
                        for (Location l :
                                LocationController.getInstance().getAllLocations()) {
                            Log.d("VerifyTask", l.toString());
                            for (SubLocation sl :
                                    l.getSublocations()) {
                                Log.d("VerifyTask", "\t" + sl.getId() + " " + sl.getName());
                            }
                        }
                    }
                });
                LocalDatabaseController.getInstance(null).clearFriendsAndRequests();
                SyncController.getInstance().syncFriends(null);
            }
        }
    }
}
