package com.gamezone.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a sale transaction in the store. A sale is associated with
 * a client, a seller, a date, and the set of products purchased. It must
 * contain at least one product to be considered valid, and it is
 * responsible for calculating its own total amount.
 *
 * Responsabilidad: Líder Técnico - Módulo de Ventas.
 */
public class Sale {

    private String id;
    private LocalDate date;
    private Client client;
    private Seller seller;
    private List<Product> products;

    /**
     * Creates a new sale.
     *
     * @param id     unique identifier of the sale
     * @param date   date on which the sale was made
     * @param client client who made the purchase
     * @param seller seller who attended the sale
     */
    public Sale(String id, LocalDate date, Client client, Seller seller) {
        this.id = id;
        this.date = date;
        this.client = client;
        this.seller = seller;
        this.products = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Client getClient() {
        return client;
    }

    public Seller getSeller() {
        return seller;
    }

    /**
     * Returns the list of products purchased in this sale. The returned
     * list is a copy to preserve encapsulation of the internal state.
     * @return list of purchased products
     */
    public List<Product> getProducts() {
        return new ArrayList<>(products);
    }

    /**
     * Adds a product to the sale.
     * @param product product to add
     */
    public void addProduct(Product product) {
        products.add(product);
    }

    /**
     * A sale is only valid if it contains at least one product.
     * @return true if the sale has one or more products
     */
    public boolean isValid() {
        return !products.isEmpty();
    }

    /**
     * Calculates the total amount of the sale by summing the price of
     * every purchased product. This responsibility belongs to Sale
     * itself, since the total is an intrinsic property of the sale.
     * @return the total price of the sale
     */
    public double calculateTotal() {
        double total = 0.0;
        for (Product product : products) {
            total += product.getPrice();
        }
        return total;
    }

    @Override
    public String toString() {
        return "Venta{" +
                "id='" + id + '\'' +
                ", fecha=" + date +
                ", cliente=" + client.getFullName() +
                ", vendedor=" + seller.getFullName() +
                ", productos=" + products.size() +
                ", total=" + calculateTotal() +
                '}';
    }
}