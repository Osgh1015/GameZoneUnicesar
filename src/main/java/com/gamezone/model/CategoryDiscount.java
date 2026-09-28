package com.gamezone.model;

import java.time.LocalDate;

/**
 * Promotion that applies a percentage discount only to the products of a
 * target category ("VIDEOGAME" or "CONSOLE").
 */
public class CategoryDiscount extends Promotion {

    private double percentage;
    private String targetCategory;

    /**
     * Creates a category promotion.
     *
     * @param id             unique identifier
     * @param name           promotion name
     * @param startDate      first valid day
     * @param endDate        last valid day
     * @param percentage     discount percentage
     * @param targetCategory "VIDEOGAME" or "CONSOLE"
     */
    public CategoryDiscount(String id, String name, LocalDate startDate, LocalDate endDate,
                            double percentage, String targetCategory) {
        super(id, name, startDate, endDate);
        this.percentage = percentage;
        this.targetCategory = targetCategory;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getTargetCategory() {
        return targetCategory;
    }

    public void setTargetCategory(String targetCategory) {
        this.targetCategory = targetCategory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double calculateDiscount(Sale sale) {
        double categoryTotal = 0;
        for (Product product : sale.getProducts()) {
            if (belongsToTargetCategory(product)) {
                categoryTotal += product.getPrice();
            }
        }
        return categoryTotal * percentage / 100.0;
    }

    private boolean belongsToTargetCategory(Product product) {
        if ("VIDEOGAME".equals(targetCategory)) {
            return product instanceof VideoGame;
        }
        if ("CONSOLE".equals(targetCategory)) {
            return product instanceof Console;
        }
        return false;
    }
}