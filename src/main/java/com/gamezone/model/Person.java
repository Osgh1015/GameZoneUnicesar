package com.gamezone.model;

/**
 * Abstract base class for every person that interacts with the store.
 * <p>
 * It groups the attributes that are common to any person (id, name and
 * phone number), regardless of the specific role they play in the system
 * (Client or Vendor). It cannot be instantiated directly: a "generic
 * person" with no role does not make sense for the business, since every
 * person in the store is either a Client or a Vendor.
 */
public abstract class Person {

    private String id;
    private String name;
    private String phone;

    /**
     * Creates a new person with the attributes shared by all roles.
     *
     * @param id    unique identification of the person
     * @param name  full name of the person
     * @param phone contact phone number
     */
    public Person(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

}