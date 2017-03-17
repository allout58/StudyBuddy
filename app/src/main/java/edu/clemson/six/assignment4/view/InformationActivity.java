package edu.clemson.six.assignment4.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;


import butterknife.ButterKnife;
import butterknife.InjectView;
import edu.clemson.six.assignment4.R;

public class InformationActivity extends AppCompatActivity {

    @InjectView(R.id.toolbar)
    protected Toolbar toolbar;
    @InjectView(R.id.info_content)
    protected TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        ButterKnife.inject(this);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // THIS IS HOW TO GET LINKSES TO WORK
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
