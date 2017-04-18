package edu.clemson.six.studybuddy.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import edu.clemson.six.studybuddy.R;
import edu.clemson.six.studybuddy.controller.LocationController;
import edu.clemson.six.studybuddy.model.Location;
import edu.clemson.six.studybuddy.model.SubLocation;

public class ChangeLocationActivity extends AppCompatActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.txtViewCurrentLoc)
    TextView txtViewCurrentLoc;
    @InjectView(R.id.spinner)
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_location);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Location loc = LocationController.getInstance().getCurrentLocation();
        if (loc != null) {
            txtViewCurrentLoc.setText(loc.getName());
            ArrayAdapter<SubLocation> subLocationArrayAdapter = new ArrayAdapter<SubLocation>(this, android.R.layout.simple_dropdown_item_1line, loc.getSublocations());
            spinner.setAdapter(subLocationArrayAdapter);
        }
    }
}
