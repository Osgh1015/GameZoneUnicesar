package com.gamezone.model;

/**
 * Represents a game console sold by GameZone Unicesar.
 */
public class Console extends Product {

    private String brand;
    private String model;
    private int generation;

    public Console(String id, String title, double price, int quantity,
                   String brand, String model, int generation) {
        super(id, title, price, quantity);
        this.brand = brand;
        this.model = model;
        this.generation = generation;
    }

    @Override
    public String getDescription() {
        return null;
    }
}