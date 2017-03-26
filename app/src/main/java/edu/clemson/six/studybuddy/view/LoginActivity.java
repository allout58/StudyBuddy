package edu.clemson.six.studybuddy.view;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import edu.clemson.six.studybuddy.R;
import edu.clemson.six.studybuddy.controller.LoginSessionController;
import edu.clemson.six.studybuddy.controller.SyncController;
import edu.clemson.six.studybuddy.controller.net.APIConnector;
import edu.clemson.six.studybuddy.controller.net.ConnectionDetails;

public class LoginActivity extends AppCompatActivity {


    @InjectView(R.id.editTextUsername)
    TextInputEditText editTextUsername;
    @InjectView(R.id.editTextPassword)
    TextInputEditText editTextPassword;
    @InjectView(R.id.loginButton)
    Button loginButton;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    @InjectView(R.id.login_coordinator)
    CoordinatorLayout loginCoordinator;

    @Override
    public void onBackPressed() {
        // Ignore default action
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        editTextPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginButton.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    @OnClick(R.id.loginButton)
    public void onLoginClick() {
        boolean cancel = false;
        if (TextUtils.isEmpty(editTextPassword.getText())) {
            editTextPassword.setError(getString(R.string.error_field_required));
            editTextPassword.requestFocus();
            cancel = true;
        }
        if (TextUtils.isEmpty(editTextUsername.getText())) {
            editTextUsername.setError(getString(R.string.error_field_required));
            editTextUsername.requestFocus();
            cancel = true;
        }
        if (!cancel) {
            LoginTask login = new LoginTask(editTextUsername.getText().toString(), editTextPassword.getText().toString());
            login.execute((Void) null);
        }
    }

    private void showProgress(boolean status) {
        loginButton.setVisibility(status ? View.GONE : View.VISIBLE);
        progressBar.setVisibility(status ? View.VISIBLE : View.GONE);
    }

    public class LoginTask extends AsyncTask<Void, Integer, Boolean> {
        private final String username, password;
        private int userID = -1;
        private long serverTimestampDiff = 0;
        private String token, realName;

        public LoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d("LoginTask", "Begin Background");
            Map<String, String> args = new HashMap<>();
            args.put("user", this.username);
            args.put("pass", this.password);
            ConnectionDetails con = APIConnector.setupConnection("user.login", args, ConnectionDetails.Method.POST);
            try {
                JsonObject obj = APIConnector.connect(con).getAsJsonObject();
                if (obj.has("userid")) {
                    long timestamp = obj.get("currentTime").getAsLong();
                    this.serverTimestampDiff = timestamp - System.currentTimeMillis() / 1000;
                    this.userID = obj.get("userid").getAsInt();
                    this.token = obj.get("token").getAsString();
                    this.realName = obj.get("name").getAsString();
                    return true;
                } else {
                    return false;
                }

            } catch (IOException e) {
                // TODO Fallback to local login?
                Log.e("LoginTask", "Error connecting to external API", e);
                userID = -2;
                return false;
            }
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            showProgress(false);
            if (!result) {
                if (userID == -1) {
                    Snackbar.make(loginCoordinator, R.string.error_bad_login, Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(loginCoordinator, R.string.error_bad_connection, Snackbar.LENGTH_LONG).show();
                }
            } else {
                LoginSessionController.getInstance(null).setUserID(this.userID);
                LoginSessionController.getInstance(null).setUsername(this.username);
                LoginSessionController.getInstance(null).setName(this.realName);
                LoginSessionController.getInstance(null).setToken(this.token);
                LoginSessionController.getInstance(null).setServerTimestampDiff(this.serverTimestampDiff);
                SyncController.getInstance().beginSync();
                Log.d("LoginActivity", String.format("User ID: %d, Uname: %s, Real: %s", userID, username, realName));
                finish();
            }
            super.onPostExecute(result);
        }
    }
}
