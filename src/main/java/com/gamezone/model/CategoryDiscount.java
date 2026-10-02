package com.gamezone.model;

import java.time.LocalDate;

/**
 * Promotion that applies a percentage discount only to the products of a
 * target category ("VIDEOGAME", "CONSOLE" or "ACCESSORY").
 */
public class CategoryDiscount extends Promotion {

    /** Category value that targets {@link VideoGame} products. */
    public static final String VIDEOGAME = "VIDEOGAME";

    /** Category value that targets {@link Console} products. */
    public static final String CONSOLE = "CONSOLE";

    /**
     * Category value that targets every {@link Accessory}
     * ({@link Controller}, {@link Cable} and {@link Memory}).
     */
    public static final String ACCESSORY = "ACCESSORY";

    private double percentage;
    private String targetCategory;

    /**
     * Creates a category promotion.
     *
     * @param id             unique identifier of the promotion
     * @param name           name of the promotion
     * @param startDate      first valid day
     * @param endDate        last valid day
     * @param percentage     discount percentage (between 0 and 100)
     * @param targetCategory category the discount applies to
     *                       ("VIDEOGAME", "CONSOLE" or "ACCESSORY")
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
     * Sums the price of the products that belong to the target category
     * and applies the percentage only to that sum.
     *
     * @param sale sale to evaluate
     * @return the discount amount in pesos
     */
    @Override
    public double calculateDiscount(Sale sale) {
        double categoryTotal = 0.0;
        for (Product product : sale.getProducts()) {
            if (belongsToCategory(product)) {
                categoryTotal += product.getPrice();
            }
        }
        return categoryTotal * percentage / 100.0;
    }

    /**
     * Checks whether a product belongs to the target category of this
     * promotion. Any instance of {@link Accessory} is recognized as part
     * of the "ACCESSORY" category, regardless of its concrete subclass.
     *
     * @param product product to check
     * @return true if the product belongs to the target category
     */
    private boolean belongsToCategory(Product product) {
        if (VIDEOGAME.equalsIgnoreCase(targetCategory)) {
            return product instanceof VideoGame;
        }
        if (CONSOLE.equalsIgnoreCase(targetCategory)) {
            return product instanceof Console;
        }
        if (ACCESSORY.equalsIgnoreCase(targetCategory)) {
            return product instanceof Accessory;
        }
        return false;
    }

    @Override
    public String getDescription() {
        return super.getDescription()
                + String.format(" | Tipo: Categoría | Categoría: %s | Descuento: %.1f%%",
                targetCategory, percentage);
    }
}