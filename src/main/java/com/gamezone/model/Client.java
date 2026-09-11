package com.gamezone.model;


public class Client extends Person {

    private String email;


    public Client(String id, String name, String phone, String email) {
        super(id, name, phone);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String describe() {
        return "Client: " + getName() + " (ID: " + getId() + ", email: " + email + ")";
    }
}