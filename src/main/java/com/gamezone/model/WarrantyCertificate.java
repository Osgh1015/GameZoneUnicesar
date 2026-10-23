package com.gamezone.model;

import java.time.LocalDate;

/**
 * Text layout of the certificate of a {@link Warranty}, in Spanish because
 * it is shown to the user of the store.
 * <p>
 * The warranty keeps its coverage rules (duration, cost, whether it is
 * active); this class only decides how they are presented, so a change in
 * the layout of the certificate does not touch the warranty hierarchy.
 */
public class WarrantyCertificate {

    private final Warranty warranty;

    /**
     * Creates the certificate of a warranty.
     *
     * @param warranty warranty whose certificate is built
     */
    public WarrantyCertificate(Warranty warranty) {
        this.warranty = warranty;
    }

    /**
     * Builds the certificate, including whether the warranty is active on
     * the current date.
     *
     * @return the formatted certificate text
     */
    public String format() {
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
                warranty.getId(),
                warranty.getWarrantyType(),
                warranty.getProduct().getTitle(), warranty.getProduct().getId(),
                warranty.getSale().getId(),
                warranty.getStartDate(),
                warranty.getEndDate(),
                warranty.getDurationInMonths(),
                warranty.getAdditionalCost(),
                warranty.isActive(LocalDate.now()) ? "VIGENTE" : "VENCIDA");
    }
}
