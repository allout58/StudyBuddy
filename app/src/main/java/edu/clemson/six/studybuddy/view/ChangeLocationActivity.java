package edu.clemson.six.studybuddy.view;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.gson.JsonElement;

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
            // TODO: Decide if this should be one API call or the three that is is now
            Map<String, String> args = new HashMap<>();
            args.put("jwt", params[0]);
            args.put("sublocationID", String.valueOf(UserLocationController.getInstance().getCurrentSubLocation().getId()));
            args.put("other", params[1]);
            Map<String, String> args2 = new HashMap<>();
            args2.put("jwt", params[0]);
            Map<String, String> args3 = new HashMap<>();
            args3.put("jwt", params[0]);
            args3.put("locationID", String.valueOf(UserLocationController.getInstance().getCurrentLocation().getId()));
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
            if (UserLocationController.getInstance().getCurrentEndTime() != null)
                args2.put("endTime", sdf.format(UserLocationController.getInstance().getCurrentEndTime()));
            ConnectionDetails dets = APIConnector.setupConnection("user.set_sub_location", args, ConnectionDetails.Method.POST);
            ConnectionDetails dets2 = APIConnector.setupConnection("user.set_endtime", args2, ConnectionDetails.Method.POST);
            ConnectionDetails dets3 = APIConnector.setupConnection("user.set_location", args3, ConnectionDetails.Method.POST);
            try {
                JsonElement el = APIConnector.connect(dets);
                JsonElement el2 = APIConnector.connect(dets2);
                JsonElement el3 = APIConnector.connect(dets3);
                // TODO Check for success for the API calls??
                return true;
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
