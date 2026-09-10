package com.gamezone.model;

/**
 * Abstract base class representing a generic product sold by GameZone Unicesar.
 */
public abstract class Product {

    private String id;
    private String title;
    private double price;
    private int quantity;

    /**
     * Creates a new product with its common attributes.
     *
     * @param id       unique identifier of the product
     * @param title    product title
     * @param price    unit price of the product
     * @param quantity quantity currently available in inventory
     */
    public Product(String id, String title, double price, int quantity) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
    }
}