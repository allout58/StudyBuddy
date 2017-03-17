package edu.clemson.six.assignment4.view;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.azeesoft.lib.colorpicker.ColorPickerDialog;

import edu.clemson.six.assignment4.R;
import edu.clemson.six.assignment4.controller.CarController;
import edu.clemson.six.assignment4.databinding.ActivityAddCarBinding;
import edu.clemson.six.assignment4.model.Car;
import edu.clemson.six.assignment4.view.binding.CarBinder;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AddCarActivity extends AppCompatActivity {

    @InjectView(R.id.add_coordinator)
    protected CoordinatorLayout coordinatorLayout;
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
    @InjectView(R.id.btnAdd)
    protected Button btnAdd;
    private CarBinder car;
    private ColorPickerDialog cp;

    private boolean isDirty = false;
    private boolean doneInit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityAddCarBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_add_car);

        ButterKnife.inject(this);
        car = new CarBinder(new Car());
        binding.setCar(car);

        btnAdd.animate().alpha(0).setDuration(0);

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

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                doneInit = true;
            }
        }, 200);
    }

    public void colorClick(View view) {
        cp = ColorPickerDialog.createColorPickerDialog(this, ColorPickerDialog.DARK_THEME);
        cp.setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
            @Override
            public void onColorPicked(int color, String hexVal) {
                car.color.set(color);
                setDirty();
            }
        });
        cp.hideOpacityBar();
        cp.setInitialColor(car.color.get());
        cp.show();
    }

    public void addClick(View view) {
        car.save();
        CarController.getInstance().add(car.car);
        CarController.getInstance().commitDirty();
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

    private void setDirty() {
        if (!isDirty && doneInit) {
            isDirty = true;
            btnAdd.animate().alpha(1).setDuration(750);
        }
    }
}
