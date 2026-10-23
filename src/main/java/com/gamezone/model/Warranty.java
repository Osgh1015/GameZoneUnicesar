package com.gamezone.model;

import java.time.LocalDate;

/**
 * Abstract base class representing a warranty granted for a product
 * that was sold in a specific sale. It centralizes the attributes and
 * the behavior shared by every warranty type (identifier, associated
 * product and sale, start date and end date), and delegates to its
 * subclasses the rules that change from one warranty type to another:
 * duration in months, type name and additional cost.
 *
 * The end date is never received from outside: it is always derived
 * from the start date and the duration declared by the concrete
 * subclass, so no caller can create an inconsistent warranty.
 *
 * Responsabilidad: Desarrollador 1 - Jerarquia de garantias.
 */
public abstract class Warranty {

    private final String id;
    private final Product product;
    private final Sale sale;
    private final LocalDate startDate;
    private final LocalDate endDate;

    /**
     * Creates a warranty and automatically calculates its end date by
     * adding the duration declared by the concrete subclass to the
     * start date received.
     *
     * @param id        unique identifier of the warranty
     * @param product   product covered by this warranty
     * @param sale      sale in which the product was purchased
     * @param startDate date on which the coverage begins (the sale date)
     */
    public Warranty(String id, Product product, Sale sale, LocalDate startDate) {
        this.id = id;
        this.product = product;
        this.sale = sale;
        this.startDate = startDate;
        this.endDate = startDate.plusMonths(getDurationInMonths());
    }

    /**
     * @return the unique identifier of the warranty
     */
    public String getId() {
        return id;
    }

    /**
     * @return the product covered by this warranty
     */
    public Product getProduct() {
        return product;
    }

    /**
     * @return the sale in which the covered product was purchased
     */
    public Sale getSale() {
        return sale;
    }

    /**
     * @return the date on which the coverage begins
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * @return the date on which the coverage expires
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Declares how many months the coverage lasts. Each concrete
     * warranty type defines its own duration.
     *
     * @return the duration of the warranty expressed in months
     */
    public abstract int getDurationInMonths();

    /**
     * Declares the commercial name of the warranty type, used in the
     * certificates and in the console reports.
     *
     * @return the name of the warranty type
     */
    public abstract String getWarrantyType();

    /**
     * Declares the extra amount this warranty adds to the total of the
     * sale it belongs to.
     *
     * @return the additional cost charged for this warranty
     */
    public abstract double getAdditionalCost();

    /**
     * Checks whether the coverage is active on a given date. The
     * warranty is considered active when the date is not before the
     * start date and not after the end date, so both limits are
     * included in the coverage.
     *
     * @param date date to evaluate
     * @return {@code true} when the warranty covers the given date
     */
    public boolean isActive(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Builds a readable certificate of the warranty, written in Spanish
     * because it is a message shown to the user of the store.
     *
     * @return a formatted description of the warranty
     */
    public String generateWarrantyCertificate() {
        return String.format(
                "CERTIFICADO DE GARANTÍA%n"
                        + "Identificador: %s%n"
                        + "Tipo: %s%n"
                        + "Producto: %s (ID: %s)%n"
                        + "Venta asociada: %s%n"
                        + "Fecha de inicio: %s%n"
                        + "Fecha de vencimiento: %s%n"
                        + "Duración: %d meses%n"
                        + "Costo adicional: %.2f%n"
                        + "Estado a la fecha actual: %s",
                id,
                getWarrantyType(),
                product.getTitle(), product.getId(),
                sale.getId(),
                startDate,
                endDate,
                getDurationInMonths(),
                getAdditionalCost(),
                isActive(LocalDate.now()) ? "VIGENTE" : "VENCIDA");
    }

    @Override
    public String toString() {
        return String.format(
                "%s | %s | Producto: %s (%s) | Venta: %s | Del %s al %s | Costo: %.2f",
                id, getWarrantyType(), product.getTitle(), product.getId(),
                sale.getId(), startDate, endDate, getAdditionalCost());
    }
}