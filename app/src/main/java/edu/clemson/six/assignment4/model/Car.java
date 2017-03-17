package edu.clemson.six.assignment4.model;

/**
 * Car Data Model
 *
 * @author jthollo
 */

public class Car implements Comparable {
    private User user;
    private String make, model, license, state;
    private int color;
    private int year;
    private int id;
    private int sortOrder;
    private long lastUpdate;
    private boolean isDeleted = false;
    private boolean isNew = false;

    public Car(User user, String make, String model, String license, String state, int color, int year, int id, int sortOrder) {
        this.user = user;
        this.make = make;
        this.model = model;
        this.license = license;
        this.state = state;
        this.color = color;
        this.year = year;
        this.id = id;
        this.sortOrder = sortOrder;
    }

    public Car(User user, String make, String model, String license, String state, int color, int year, int id, int sortOrder, long lastUpdate, boolean isDeleted) {
        this.user = user;
        this.make = make;
        this.model = model;
        this.license = license;
        this.state = state;
        this.color = color;
        this.year = year;
        this.id = id;
        this.sortOrder = sortOrder;
        this.lastUpdate = lastUpdate;
        this.isDeleted = isDeleted;
    }

    public Car() {
        isNew = true;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Car) {
            int sortOrderComp = Integer.valueOf(this.sortOrder).compareTo(((Car) o).getSortOrder());
            // Same sort order, so
            if (sortOrderComp == 0) {
                return this.make.compareTo(((Car) o).getMake());
            } else {
                return sortOrderComp;
            }
        }
        return -1;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}