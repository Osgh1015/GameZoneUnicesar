package com.gamezone.model;

import java.time.LocalDate;

/**
 * Abstract base class for every promotion offered by GameZone Unicesar.
 * It holds the attributes shared by all promotion types (identifier, name
 * and validity period) and declares the discount calculation as an abstract
 * method, so each concrete promotion applies its own pricing strategy.
 */
public abstract class Promotion {

    private String id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * Creates a new promotion.
     *
     * @param id        unique identifier of the promotion
     * @param name      descriptive name of the promotion
     * @param startDate first day on which the promotion is valid (inclusive)
     * @param endDate   last day on which the promotion is valid (inclusive)
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
     * Checks whether the promotion is valid on the given date, that is,
     * whether the date falls within the validity range (both ends included).
     *
     * @param date date to check
     * @return {@code true} if the promotion is active on that date
     */
    public boolean isActive(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Calculates the monetary discount (in pesos) this promotion would
     * grant to the given sale. Every concrete promotion must provide its
     * own calculation rule.
     *
     * @param sale sale for which the discount is calculated
     * @return the discount amount, or zero if the promotion does not apply
     */
    public abstract double calculateDiscount(Sale sale);

    /**
     * Builds a short textual description of the promotion, shown in the
     * console menu. Subclasses extend it with their specific attributes.
     *
     * @return a textual description of the promotion
     */
    public String getDescription() {
        return String.format("[%s] %s | Vigencia: %s a %s", id, name, startDate, endDate);
    }
}