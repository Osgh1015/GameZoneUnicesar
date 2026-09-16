package com.gamezone.model;

import java.time.LocalDate;

/**
 * Optional warranty the client may buy when a sale is registered. It
 * covers manufacturing defects and accidental damage, lasts twelve
 * months from the sale date and costs ten percent of the price of the
 * covered product.

 */
public class ExtendedWarranty extends Warranty {

    /** Number of months the extended coverage lasts. */
    private static final int DURATION_IN_MONTHS = 12;

    /** Fraction of the product price charged for the extended coverage. */
    private static final double COST_RATE = 0.10;

    /**
     * Creates an extended warranty for a product sold in a given sale.
     *
     * @param id        unique identifier of the warranty
     * @param product   product covered by this warranty
     * @param sale      sale in which the product was purchased
     * @param startDate date on which the coverage begins
     */
    public ExtendedWarranty(String id, Product product, Sale sale, LocalDate startDate) {
        super(id, product, sale, startDate);
    }

    /**
     * {@inheritDoc}
     *
     * @return twelve months, the duration of the extended coverage
     */
    @Override
    public int getDurationInMonths() {
        return DURATION_IN_MONTHS;
    }

    /**
     * {@inheritDoc}
     *
     * @return the commercial name of this warranty type
     */
    @Override
    public String getWarrantyType() {
        return "Garantía Extendida";
    }

    /**
     * {@inheritDoc}
     *
     * @return ten percent of the price of the covered product
     */
    @Override
    public double getAdditionalCost() {
        return getProduct().getPrice() * COST_RATE;
    }
}