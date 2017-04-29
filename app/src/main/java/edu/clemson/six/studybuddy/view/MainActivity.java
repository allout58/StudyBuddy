package edu.clemson.six.studybuddy.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import butterknife.OnClick;
import edu.clemson.six.studybuddy.Constants;
import edu.clemson.six.studybuddy.R;
import edu.clemson.six.studybuddy.controller.FriendController;
import edu.clemson.six.studybuddy.controller.LocationController;
import edu.clemson.six.studybuddy.controller.SyncController;
import edu.clemson.six.studybuddy.controller.adapter.HomePageAdapter;
import edu.clemson.six.studybuddy.controller.net.APIConnector;
import edu.clemson.six.studybuddy.controller.net.ConnectionDetails;
import edu.clemson.six.studybuddy.controller.sql.LocalDatabaseController;
import edu.clemson.six.studybuddy.model.Location;
import edu.clemson.six.studybuddy.model.SubLocation;
import edu.clemson.six.studybuddy.service.FBInstanceIDService;
import edu.clemson.six.studybuddy.service.LocationTrackingService;
import edu.clemson.six.studybuddy.view.component.CircleTransform;

public class MainActivity extends SmartAppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int LOCATION_PERM_REQ = 1;

    @InjectView(R.id.toolbar)
    protected Toolbar toolbar;
    @InjectView(R.id.friends_list)
    protected RecyclerView recyclerView;
    @InjectView(R.id.main_coordinator)
    protected CoordinatorLayout mainCoordinator;
    @InjectView(R.id.swipeContainer)
    protected SwipeRefreshLayout swipeContainer;
    @InjectView(R.id.main_drawer)
    protected DrawerLayout mainDrawer;
    @InjectView(R.id.nav_view)
    protected NavigationView navView;
    @InjectView(R.id.btnPinDrop)
    protected Button btnPinDrop;
    protected LinearLayoutManager layoutManager;
    TextView textViewUser;
    ImageView imageViewUser;
    private String pinDropName = "";

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERM_REQ &&
                (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED) &&
                SyncController.getInstance().isLocsSynced()) {
            startLocationTrackingService();
        }
    }

    private void startLocationTrackingService() {
        Intent locServiceIntent = new Intent(this, LocationTrackingService.class);
        startService(locServiceIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize ButterKnife
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);

        Log.d("MainActivity", String.format("Action: %s, Type: %s", getIntent().getAction(), getIntent().getType()));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        View v = navView.getHeaderView(0);
        textViewUser = (TextView) v.findViewById(R.id.textViewUser);
        imageViewUser = (ImageView) v.findViewById(R.id.imageViewUser);

        // Check firebase authorization
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
//            Snackbar.make(mainCoordinator, "Already logged in", Snackbar.LENGTH_LONG).show();
            auth.getCurrentUser().getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    if (task.isSuccessful()) {
                        onLoggedIn(task.getResult().getToken());
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

        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(HomePageAdapter.getInstance());

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FriendController.getInstance().updateNearby();
                swipeContainer.setRefreshing(false);
                HomePageAdapter.getInstance().notifyDataSetChanged();
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
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

    private void onLoggedIn(String authToken) {

        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
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
                Log.d("SyncController", "Synchronized");
                for (Location l :
                        LocationController.getInstance().getAllLocations()) {
                    Log.d("SyncController", l.toString());
                    for (SubLocation sl :
                            l.getSublocations()) {
                        Log.d("SyncController", "\t" + sl.getId() + " " + sl.getName());
                    }
                }
                // Start the location service after we have all the locations
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    startLocationTrackingService();
                }
            }
        });

        // Login to the server
        VerifyTask task = new VerifyTask();
        task.execute(authToken);
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
//            case R.id.nav_settings:
//                startActivity(new Intent(this, SettingsActivity.class));
//                break;
//            case R.id.nav_map:
//                startActivity(new Intent(this, MapActivity.class));
//                break;
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

    private void launchFirebaseAuthUI() {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setLogo(R.drawable.icon_named)
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
                            onLoggedIn(task.getResult().getToken());
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

    @OnClick(R.id.btnPinDrop)
    public void onViewClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.title_dialog_pin_drop);

        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);

        builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pinDropName = input.getText().toString();
                Intent changeLoc = new Intent(MainActivity.this, ChangeLocationActivity.class);
                changeLoc.setAction(Constants.ACTION_PIN_DROP);
                changeLoc.putExtra(Constants.EXTRA_PIN_DROP_NAME, pinDropName);
                startActivity(changeLoc);
            }
        });
        builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
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
                LocalDatabaseController.getInstance(null).clearFriendsAndRequests();
                SyncController.getInstance().syncFriends(null);
                FBInstanceIDService.sendRegTokenToServer();
            }
        }
    }
}
