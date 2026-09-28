package com.gamezone.model;

import java.time.LocalDate;

/**
 * Abstract base class for all promotions of the store.
 * <p>
 * It holds the data shared by every promotion (identifier, name and validity
 * period). Each concrete promotion decides how its discount is calculated by
 * implementing {@link #calculateDiscount(Sale)}.
 */
public abstract class Promotion {

    private String id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * Creates a promotion.
     *
     * @param id        unique identifier
     * @param name      promotion name shown in the receipt
     * @param startDate first day the promotion is valid (inclusive)
     * @param endDate   last day the promotion is valid (inclusive)
     */
    public Promotion(String id, String name, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Checks whether the promotion is valid on the given date.
     *
     * @param date the date to check
     * @return true if the date is between the start and end dates (inclusive)
     */
    public boolean isActive(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Calculates the discount, in pesos, this promotion would grant to a sale.
     *
     * @param sale the sale to evaluate
     * @return the discount amount, or 0 if the promotion does not apply
     */
    public abstract double calculateDiscount(Sale sale);
}