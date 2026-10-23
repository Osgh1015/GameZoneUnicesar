package com.gamezone.model;

import java.time.LocalDate;

/**
 * Promotion that applies a percentage discount over the total of the sale.
 */
public class PercentageDiscount extends Promotion {

    private double percentage;

    /**
     * Creates a percentage promotion.
     *
     * @param id         unique identifier of the promotion
     * @param name       name of the promotion
     * @param startDate  first valid day
     * @param endDate    last valid day
     * @param percentage discount percentage (between 0 and 100)
     */
    public PercentageDiscount(String id, String name, LocalDate startDate,
                              LocalDate endDate, double percentage) {
        super(id, name, startDate, endDate);
        this.percentage = percentage;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    /**
     * Applies the percentage to the subtotal of the whole sale.
     *
     * @param sale sale to evaluate
     * @return the discount amount in pesos
     */
    @Override
    public double calculateDiscount(Sale sale) {
        return sale.calculateTotal() * percentage / 100.0;
    }

    @Override
    public String getDescription() {
        return super.getDescription() + String.format(" | Tipo: Porcentaje | Descuento: %.1f%%", percentage);
    }
}