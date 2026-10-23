package com.gamezone.model;

import java.time.LocalDate;

/**
 * Factory warranty automatically granted to every console sold by the
 * store. It only covers manufacturing defects, lasts six months from
 * the sale date and has no additional cost for the client.
 *
 * Responsabilidad: Desarrollador 1 - Jerarquia de garantias.
 */
public class BasicWarranty extends Warranty {

    /** Number of months the basic coverage lasts. */
    private static final int DURATION_IN_MONTHS = 6;

    /**
     * Creates a basic warranty for a product sold in a given sale.
     *
     * @param id        unique identifier of the warranty
     * @param product   product covered by this warranty
     * @param sale      sale in which the product was purchased
     * @param startDate date on which the coverage begins
     */
    public BasicWarranty(String id, Product product, Sale sale, LocalDate startDate) {
        super(id, product, sale, startDate);
    }

    /**
     * {@inheritDoc}
     *
     * @return six months, the duration of the factory coverage
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
        return "Garantía Básica";
    }

    /**
     * {@inheritDoc}
     *
     * @return zero, because the factory warranty is free for the client
     */
    @Override
    public double getAdditionalCost() {
        return 0.0;
    }
}