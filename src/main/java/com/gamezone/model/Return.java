package com.gamezone.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a return transaction for one or more products originally
 * purchased in a specific sale.
 */
public class Return {

    private String id;
    private LocalDate date;
    private final Sale originalSale;
    private final List<Product> returnedProducts;
    private String reason;
    private double refundAmount;

    public Return(String id, LocalDate date, Sale originalSale,
                  List<Product> returnedProducts, String reason) {
        this.id = id;
        this.date = date;
        this.originalSale = originalSale;
        this.returnedProducts = returnedProducts;
        this.reason = reason;
        this.refundAmount = 0.0;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Sale getOriginalSale() {
        return originalSale;
    }

    public List<Product> getReturnedProducts() {
        return returnedProducts;
    }

    public String getReason() {
        return reason;
    }

    public double getRefundAmount() {
        return refundAmount;
    }

    /**
     * Calculates the refund amount as the sum of the prices of all
     * returned products, and stores it in the refundAmount field.
     * @return the calculated refund amount
     */
    public double calculateRefundAmount() {
        double total = 0.0;
        for (Product product : returnedProducts) {
            total += product.getPrice();
        }
        this.refundAmount = total;
        return total;
    }

    /**
     * Generates a formatted receipt (in Spanish) with the return's details.
     * @return the formatted receipt text
     */
    public String generateReturnReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Recibo de Devolución ---\n");
        sb.append("ID Devolución: ").append(id).append("\n");
        sb.append("Fecha: ").append(date).append("\n");
        sb.append("Venta original: ").append(originalSale.getId()).append("\n");
        sb.append("Productos devueltos:\n");
        for (Product product : returnedProducts) {
            sb.append("  - ").append(product.getTitle())
                    .append(" ($").append(product.getPrice()).append(")\n");
        }
        sb.append("Motivo: ").append(reason).append("\n");
        sb.append("Monto reembolsado: $").append(refundAmount).append("\n");
        return sb.toString();
    }
}