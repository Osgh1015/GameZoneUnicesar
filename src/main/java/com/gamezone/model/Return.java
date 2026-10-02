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

    /**
     * Creates a new return.
     *
     * @param id               unique identifier of the return
     * @param date             date on which the return was made
     * @param originalSale     sale in which the products were purchased
     * @param returnedProducts products being returned (subset of the sale)
     * @param reason           reason given by the client
     */
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
     * Calculates the fraction of the original subtotal that the client
     * actually paid after the promotion: {@code 1 − discount / subtotal}.
     * The subtotal is the sum of the list prices of the items in the
     * original sale (extended warranty costs are not part of it, because
     * the discount is calculated only over the items).
     *
     * @return a value between 0 and 1; 1 when the sale had no discount
     */
    public double calculatePaidRatio() {
        double itemsSubtotal = 0.0;
        for (Product product : originalSale.getProducts()) {
            itemsSubtotal += product.getPrice();
        }
        if (itemsSubtotal <= 0) {
            return 1.0;
        }
        return 1.0 - (originalSale.getDiscountAmount() / itemsSubtotal);
    }

    /**
     * Calculates the share of the original sale discount that corresponds
     * to one returned item.
     *
     * @param product returned item
     * @return the proportional discount of the item, rounded to 2 decimals
     */
    public double calculateItemDiscount(Product product) {
        return round(product.getPrice() - calculateItemRefund(product));
    }

    /**
     * Calculates the amount refunded for one returned item, proportional
     * to the discount of the original sale:
     * {@code price × (1 − discount / subtotal)}.
     *
     * @param product returned item
     * @return the refund of the item, rounded to 2 decimals
     */
    public double calculateItemRefund(Product product) {
        return round(product.getPrice() * calculatePaidRatio());
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

    /**
     * Rounds a monetary value to two decimals.
     *
     * @param value value to round
     * @return the rounded value
     */
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
