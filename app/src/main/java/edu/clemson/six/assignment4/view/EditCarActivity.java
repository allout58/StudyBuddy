package edu.clemson.six.assignment4.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.azeesoft.lib.colorpicker.ColorPickerDialog;

import edu.clemson.six.assignment4.R;
import edu.clemson.six.assignment4.controller.CarController;
import edu.clemson.six.assignment4.controller.CarListAdapter;
import edu.clemson.six.assignment4.databinding.ActivityEditCarBinding;
import edu.clemson.six.assignment4.exception.CarNotFoundException;
import edu.clemson.six.assignment4.model.Car;
import edu.clemson.six.assignment4.view.binding.CarBinder;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class EditCarActivity extends AppCompatActivity {

    @InjectView(R.id.switchMode)
    protected SwitchCompat editMode;
    @InjectView(R.id.edit_coordinator)
    protected CoordinatorLayout coordinatorLayout;
    @InjectView(R.id.btnSave)
    protected Button btnSave;
    @InjectView(R.id.colorView)
    protected View colorView;

    @InjectView(R.id.editTextMake)
    protected EditText txtMake;
    @InjectView(R.id.editTextModel)
    protected EditText txtModel;
    @InjectView(R.id.editTextLicense)
    protected EditText txtLicense;
    @InjectView(R.id.editTextState)
    protected EditText txtState;
    @InjectView(R.id.editTextYear)
    protected EditText txtYear;

    private CarBinder car;

    private int order;
    private int id;
    private boolean isDirty = false;
    private boolean doneInit = false;

    private ColorPickerDialog cp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityEditCarBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_car);

        ButterKnife.inject(this);

        btnSave.animate().alpha(0).setDuration(0);

        TextWatcher textWatch = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setDirty();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        txtLicense.addTextChangedListener(textWatch);
        txtMake.addTextChangedListener(textWatch);
        txtModel.addTextChangedListener(textWatch);
        txtState.addTextChangedListener(textWatch);
        txtYear.addTextChangedListener(textWatch);

        try {
            // Fetch the car's ID from the intent
            Intent intent = getIntent();
            // First check that the intent's extra exists
            if (intent.hasExtra(MainActivity.INTENT_CAR_ID)) {
                // and that it was sent correctly
                id = intent.getIntExtra(MainActivity.INTENT_CAR_ID, -1);
                if (id != -1) {
                    order = intent.getIntExtra(MainActivity.INTENT_CAR_ORDER, -1);
                    Car c = CarController.getInstance().getCarById(id);
                    car = new CarBinder(c);
                    binding.setCar(car);
                } else {
                    throw new CarNotFoundException("Car ID not set correctly");
                }
            } else {
                throw new CarNotFoundException("Car ID not sent");
            }
        } catch (CarNotFoundException e) {
            Snackbar.make(coordinatorLayout, R.string.car_not_found, Snackbar.LENGTH_LONG).show();
            Log.e("EditCarActivity", "Car Not Found", e);
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                doneInit = true;
            }
        }, 200);
    }

    private void setDirty() {
        if (!isDirty && doneInit) {
            isDirty = true;
            btnSave.animate().alpha(1).setDuration(750);
        }
    }

    public void switchClick(View view) {
        View focused = this.getCurrentFocus();
        if (focused != null)
            focused.clearFocus();
    }

    public void colorClick(View view) {
        if (editMode.isChecked()) {
            cp = ColorPickerDialog.createColorPickerDialog(this, ColorPickerDialog.DARK_THEME);
            cp.setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
                @Override
                public void onColorPicked(int color, String hexVal) {
                    Log.d("EditCar-Color", String.format("Picked %s: %d", hexVal, color));
                    car.color.set(color);
                    setDirty();
                }
            });
            cp.hideOpacityBar();
            cp.setInitialColor(car.color.get());
            cp.show();
        }
    }

    public void saveClick(View view) {
        car.save();
        CarListAdapter.getInstance().notifyItemChanged(order);
        CarController.getInstance().notifyUpdated(order);
        finish();
    }

    public void cancelClick(View view) {
        if (isDirty) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setIcon(android.R.drawable.ic_menu_delete)
                    .setMessage(R.string.alert_cancel)
                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show();
        } else {
            finish();
        }
    }
}
