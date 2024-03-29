package edu.clemson.six.studybuddy.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import edu.clemson.six.studybuddy.Constants;
import edu.clemson.six.studybuddy.R;
import edu.clemson.six.studybuddy.controller.UserLocationController;
import edu.clemson.six.studybuddy.controller.net.APIConnector;
import edu.clemson.six.studybuddy.controller.net.ConnectionDetails;
import edu.clemson.six.studybuddy.model.Location;
import edu.clemson.six.studybuddy.model.SubLocation;
import edu.clemson.six.studybuddy.view.component.TimePickerFragment;

public class ChangeLocationActivity extends SmartAppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    private static final String TAG = "ChangeLocationActivity";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.txtViewCurrentLoc)
    TextView txtViewCurrentLoc;
    @InjectView(R.id.spinner)
    Spinner spinner;
    @InjectView(R.id.btnChangeTime)
    Button btnChangeTime;
    @InjectView(R.id.btnSave)
    Button btnSave;
    @InjectView(R.id.textViewTime)
    TextView textViewTime;
    @InjectView(R.id.progressBar2)
    ProgressBar progressBar2;
    @InjectView(R.id.textView)
    TextView textView;
    @InjectView(R.id.editText)
    EditText editText;

    int hour = 0, minute = 0;
    @InjectView(R.id.textViewArea)
    TextView textViewArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_location);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (UserLocationController.getInstance().getCurrentEndTime() != null) {
            Date endtime = UserLocationController.getInstance().getCurrentEndTime();
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, endtime.getHours());
            c.set(Calendar.MINUTE, endtime.getMinutes());
            textViewTime.setText(SimpleDateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime()));
        } else
            textViewTime.setText(R.string.notSet);

        if (Constants.ACTION_PIN_DROP.equals(getIntent().getAction())) {
            UserLocationController.getInstance().setCurrentLocation(Location.OTHER);
            UserLocationController.getInstance().setCurrentSubLocation(SubLocation.OTHER);
            UserLocationController.getInstance().setCurrentBlurb(getIntent().getStringExtra(Constants.EXTRA_PIN_DROP_NAME));
            txtViewCurrentLoc.setText(UserLocationController.getInstance().getCurrentBlurb());
            spinner.setVisibility(View.GONE);
            textViewArea.setVisibility(View.GONE);
            editText.setText(getIntent().getStringExtra(Constants.EXTRA_PIN_DROP_NAME));
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            android.location.Location last = null;
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                UserLocationController.getInstance().setPinLatitude(last.getLatitude());
                UserLocationController.getInstance().setPinLongitude(last.getLongitude());
            }
        } else {
            Location loc = UserLocationController.getInstance().getCurrentLocation();
            SubLocation subloc = UserLocationController.getInstance().getCurrentSubLocation();
            if (loc != null) {
                txtViewCurrentLoc.setText(loc.getName());
                final ArrayAdapter<SubLocation> subLocationArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, loc.getSublocations());
                spinner.setAdapter(subLocationArrayAdapter);
                if (subloc != null) {
                    spinner.setSelection(subLocationArrayAdapter.getPosition(subloc));
                }
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (subLocationArrayAdapter.getItem(position) == SubLocation.OTHER) {
                            editText.setVisibility(View.VISIBLE);
                            textView.setVisibility(View.VISIBLE);
                        } else {
                            editText.setVisibility(View.GONE);
                            textView.setVisibility(View.GONE);
                            editText.setText("");
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        editText.setVisibility(View.GONE);
                        textView.setVisibility(View.GONE);
                    }
                });
            } else
                txtViewCurrentLoc.setText("N/A");
            if (loc == null) {
                btnSave.setVisibility(View.GONE);
            }
        }
    }

    @OnClick({R.id.btnChangeTime, R.id.btnSave})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnChangeTime:
                TimePickerFragment newFragment = new TimePickerFragment();
                newFragment.setListener(this);
                newFragment.show(getFragmentManager(), "timePicker");
                break;
            case R.id.btnSave:
                UserLocationController.getInstance().setCurrentSubLocation((SubLocation) spinner.getSelectedItem());
                FirebaseAuth.getInstance().getCurrentUser().getToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        ChangeLocationTask t = new ChangeLocationTask();
                        t.execute(task.getResult().getToken(), editText.getText().toString());
                    }
                });
                break;
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.hour = hourOfDay;
        this.minute = minute;
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        UserLocationController.getInstance().setCurrentEndTime(c.getTime());
        textViewTime.setText(SimpleDateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime()));
    }

    /**
     * Sends the location, sublocation, and endtime to the server
     */
    private class ChangeLocationTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            progressBar2.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Map<String, String> args = new HashMap<>();
            args.put("jwt", params[0]);
            if (UserLocationController.getInstance().getCurrentLocation() == null) {
                UserLocationController.getInstance().setCurrentLocation(Location.OTHER);
            }
            args.put("locationID", String.valueOf(UserLocationController.getInstance().getCurrentLocation().getId()));
            if (UserLocationController.getInstance().getCurrentSubLocation() != null) {
                args.put("sublocationID", String.valueOf(UserLocationController.getInstance().getCurrentSubLocation().getId()));
            }
            args.put("other", params[1]);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            if (UserLocationController.getInstance().getCurrentEndTime() != null) {
                args.put("endTime", sdf.format(UserLocationController.getInstance().getCurrentEndTime()));
                Log.d(TAG, "End time set as " + args.get("endTime"));
            }
            ConnectionDetails dets = APIConnector.setupConnection("user.set_status", args, ConnectionDetails.Method.POST);
            try {
                JsonObject el = APIConnector.connect(dets).getAsJsonObject();
                if (el.has("status") && el.get("status").getAsString().equals("success")) {
                    return true;
                } else {
                    Log.e(TAG, "Error on remote API: " + el.toString());
                    return false;
                }
            } catch (IOException e) {
                Log.e(TAG, "Error connecting to API", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                finish();
            }
        }
    }
}
