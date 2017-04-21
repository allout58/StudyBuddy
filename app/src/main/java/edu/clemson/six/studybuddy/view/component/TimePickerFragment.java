package edu.clemson.six.studybuddy.view.component;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Date;

import edu.clemson.six.studybuddy.controller.UserLocationController;

/**
 * Created by jthollo on 4/18/2017.
 */

// Taken from https://developer.android.com/guide/topics/ui/controls/pickers.html#TimePickerFragment
public class TimePickerFragment extends DialogFragment {

    private TimePickerDialog.OnTimeSetListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        int hour, minute;
        if(UserLocationController.getInstance().getCurrentEndTime() != null) {
            Date date = UserLocationController.getInstance().getCurrentEndTime();
            hour = date.getHours();
            minute = date.getMinutes();
        }
        else {
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), listener, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void setListener(TimePickerDialog.OnTimeSetListener listener) {
        this.listener = listener;
    }
}
