package com.gamezone.model;

/**
 * Represents a client of the store: a person who buys products.
 * <p>
 * In addition to the attributes shared with every person, a Client has
 * an email address. The purchase history of a client is not stored here:
 * it is derived data that belongs to the Sale module (a client's
 * purchases are the sales in which they appear), so keeping it out of
 * this class avoids duplicating information and keeps each module
 * responsible only for its own data.
 */
public class Client extends Person {

    private String email;

    /**
     * Creates a new client.
     *
     * @param id    unique identification of the client
     * @param name  full name of the client
     * @param phone contact phone number
     * @param email contact email address
     */
    public Client(String id, String name, String phone, String email) {
        super(id, name, phone);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getDescription() {
        return "Client: " + getName() + " | ID: " + getId() + " | Phone: " + getPhone() + " | Email: " + email;
    }

    public String getFullName() {
        return getName();
    }
}