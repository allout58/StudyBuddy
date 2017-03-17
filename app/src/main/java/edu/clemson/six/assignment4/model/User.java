package edu.clemson.six.assignment4.model;

/**
 * User Data Model
 *
 * @author jthollo
 */

public class User {
    private String name;
    private int id;

    /**
     * Populate this data model with database information
     *
     * @param name User's name
     * @param id   User's Unique Database ID
     */
    public User(String name, int id) {
        this.name = name;
        this.id = id;
    }

    /**
     * Create a new owner
     *
     * @param name User's name
     */
    public User(String name) {
        this.name = name;
    }

    /**
     * Get the database ID associated with this owner
     *
     * @return User's Unique ID
     */
    public int getId() {
        return id;
    }

    /**
     * Get this owner's name
     *
     * @return User's name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the owner's name
     *
     * @param name User's new name
     */
    public void setName(String name) {
        this.name = name;
    }
}
