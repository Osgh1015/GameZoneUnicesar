package com.gamezone.model;

import java.time.LocalDate;

/**
 * Promotion that applies a percentage discount to the sale total when the
 * sale includes a minimum number of products.
 */
public class BulkPurchaseDiscount extends Promotion {

    private int minQuantity;
    private double percentage;

    /**
     * Creates a bulk purchase promotion.
     *
     * @param id          unique identifier
     * @param name        promotion name
     * @param startDate   first valid day
     * @param endDate     last valid day
     * @param minQuantity minimum number of products required
     * @param percentage  discount percentage
     */
    public BulkPurchaseDiscount(String id, String name, LocalDate startDate, LocalDate endDate,
                                int minQuantity, double percentage) {
        super(id, name, startDate, endDate);
        this.minQuantity = minQuantity;
        this.percentage = percentage;
    }

    public int getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(int minQuantity) {
        this.minQuantity = minQuantity;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double calculateDiscount(Sale sale) {
        if (sale.getProducts().size() >= minQuantity) {
            return sale.calculateTotal() * percentage / 100.0;
        }
        return 0;
    }
}