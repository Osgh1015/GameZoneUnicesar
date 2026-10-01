package com.gamezone.model;

import java.time.LocalDate;

/**
 * Promotion that applies a percentage discount over the total of the sale
 * when the sale includes a minimum number of products.
 */
public class BulkPurchaseDiscount extends Promotion {

    private int minQuantity;
    private double percentage;

    /**
     * Creates a bulk purchase promotion.
     *
     * @param id          unique identifier of the promotion
     * @param name        name of the promotion
     * @param startDate   first valid day
     * @param endDate     last valid day
     * @param minQuantity minimum number of products required in the sale
     * @param percentage  discount percentage (between 0 and 100)
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
     * Applies the percentage to the whole sale if it has at least the
     * minimum quantity of products; otherwise returns zero.
     *
     * @param sale sale to evaluate
     * @return the discount amount in pesos
     */
    @Override
    public double calculateDiscount(Sale sale) {
        if (sale.getProducts().size() >= minQuantity) {
            return sale.calculateTotal() * percentage / 100.0;
        }
        return 0.0;
    }

    @Override
    public String getDescription() {
        return super.getDescription()
                + String.format(" | Tipo: Volumen | Mínimo de productos: %d | Descuento: %.1f%%",
                minQuantity, percentage);
    }
}