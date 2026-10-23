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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Builds a complete textual description of the product, integrating
     * the particular characteristics of each subclass. Every subclass
     * must provide its own implementation.
     *
     * @return a full description of the product
     */
    public abstract String getDescription();
}