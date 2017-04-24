package edu.clemson.six.studybuddy.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import edu.clemson.six.studybuddy.R;

public class InformationActivity extends SmartAppCompatActivity {

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
