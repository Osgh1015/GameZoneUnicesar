package com.gamezone.model;

import java.time.temporal.ChronoUnit;
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
    private String appliedPromotionName;
    private double discountAmount;
    private double warrantyCost;

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
        this.appliedPromotionName = null;
        this.discountAmount = 0.0;
        this.warrantyCost = 0.0;
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
     * Tells whether this sale was made by the given client.
     *
     * @param clientId identifier of the client
     * @return true if the client of the sale has that id
     */
    public boolean belongsToClient(String clientId) {
        return client.getId().equals(clientId);
    }

    /**
     * Tells whether this sale was attended by the given seller.
     *
     * @param sellerId identifier of the seller
     * @return true if the seller of the sale has that id
     */
    public boolean wasAttendedBy(String sellerId) {
        return seller.getId().equals(sellerId);
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
     * Finds one of the products of this sale by its id.
     *
     * @param productId identifier of the product
     * @return the first product of the sale with that id, or {@code null}
     *         if the sale does not contain it
     */
    public Product findProduct(String productId) {
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Counts how many units of a product were sold in this sale.
     *
     * @param productId identifier of the product
     * @return number of units of that product in the sale
     */
    public int countUnitsOf(String productId) {
        int count = 0;
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the extra amount charged for the extended warranties
     * requested during this sale.
     *
     * @return the accumulated cost of the extended warranties
     */
    public double getWarrantyCost() {
        return warrantyCost;
    }

    /**
     * Adds the cost of an extended warranty to this sale. It is called
     * once for every extended warranty granted, so the total of the
     * sale always reflects the coverage the client actually bought.
     *
     * @param amount additional cost to accumulate
     */
    public void addWarrantyCost(double amount) {
        this.warrantyCost += amount;
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

    /**
     * @return the name of the promotion applied to this sale, or
     *         {@code null} if no promotion was applied
     */
    public String getAppliedPromotionName() {
        return appliedPromotionName;
    }

    /**
     * @param appliedPromotionName name of the promotion applied to this sale
     */
    public void setAppliedPromotionName(String appliedPromotionName) {
        this.appliedPromotionName = appliedPromotionName;
    }

    /**
     * @return the monetary discount applied to this sale (zero if none)
     */
    public double getDiscountAmount() {
        return discountAmount;
    }

    /**
     * @param discountAmount the monetary discount applied to this sale
     */
    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    /**
     * Calculates the final amount to pay: the subtotal of the items minus
     * the discount applied by the promotion (if any), plus the cost of the
     * extended warranties requested. The discount is calculated only over
     * the items, so warranties are never discounted.
     * @return subtotal - discount + extended warranty cost
     */
    public double calculateFinalTotal() {
        return calculateTotal() - discountAmount + warrantyCost;
    }

    /**
     * Generates the formatted receipt (in Spanish) of this sale. It lists
     * every item with its type and price, and shows the full breakdown of
     * the integrated sale: subtotal of the items, discount with the name of
     * the applied promotion, cost of the extended warranties and final total.
     * The layout lives in {@link SaleReceipt}; this method is kept because
     * the sales module defines it and the sales menu calls it.
     * @return the formatted receipt text
     */
    public String generateReceipt() {
        return new SaleReceipt(this).format();
    }

    /**
     * Checks whether this sale is still within the 30-day return window
     * from its sale date.
     * @return true if today is within 30 calendar days of the sale date
     */
    public boolean canBeReturned() {
        long daysPassed = ChronoUnit.DAYS.between(this.date, LocalDate.now());
        return daysPassed <= 30;
    }

    @Override
    public String toString() {
        return "Venta{" +
                "id='" + id + '\'' +
                ", fecha=" + date +
                ", cliente=" + client.getFullName() +
                ", vendedor=" + seller.getFullName() +
                ", productos=" + products.size() +
                ", subtotal=" + calculateTotal() +
                ", descuento=" + discountAmount +
                ", total=" + calculateFinalTotal() +
                ", garantias=" + warrantyCost +
                '}';
    }
}