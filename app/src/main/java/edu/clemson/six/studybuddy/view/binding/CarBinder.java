package edu.clemson.six.studybuddy.view.binding;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;

import edu.clemson.six.studybuddy.model.Car;

/**
 * Created by jthollo on 2/5/2017.
 */

public class CarBinder {
    public final ObservableBoolean editable = new ObservableBoolean(false);
    public final ObservableField<String> owner = new ObservableField<>();
    public final ObservableField<String> make = new ObservableField<>();
    public final ObservableField<String> model = new ObservableField<>();
    public final ObservableField<String> license = new ObservableField<>();
    public final ObservableField<String> state = new ObservableField<>();
    public final ObservableField<String> year = new ObservableField<>();
    public final ObservableInt color = new ObservableInt();

    public final Car car;

    public CarBinder(Car c) {
        this.car = c;
        make.set(c.getMake());
        model.set(c.getModel());
        license.set(c.getLicense());
        state.set(c.getState());
        year.set(String.valueOf(c.getYear()));
        color.set(c.getColor());
    }

    public void save() {
        this.car.setMake(make.get());
        this.car.setModel(model.get());
        this.car.setLicense(license.get());
        this.car.setState(state.get());
        this.car.setYear(Integer.parseInt(year.get()));
        this.car.setColor(color.get());
    }

}
