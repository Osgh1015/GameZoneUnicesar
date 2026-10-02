package com.gamezone.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a return transaction for one or more products originally
 * purchased in a specific sale.
 *
 * <p>When the original sale received a promotion, each returned item is
 * refunded proportionally to that discount, so the client never gets back
 * more than what was actually paid for the item:
 * {@code refund = price × (1 − discount / subtotal)}.</p>
 *
 * <p>When a returned console had an extended warranty, that warranty is
 * cancelled and its cost is also refunded ({@code warrantyRefund}); the
 * basic warranty is free, so it adds nothing.</p>
 */
public class Return {

    private String id;
    private LocalDate date;
    private final Sale originalSale;
    private final List<Product> returnedProducts;
    private String reason;
    private double refundAmount;
    private double warrantyRefund;

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
        this.warrantyRefund = 0.0;
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
     * @return the amount refunded for the warranties cancelled because of
     *         this return (zero when no extended warranty was cancelled)
     */
    public double getWarrantyRefund() {
        return warrantyRefund;
    }

    /**
     * Sets the amount refunded for the warranties cancelled because of this
     * return. It must be called before {@link #calculateRefundAmount()}.
     *
     * @param warrantyRefund refundable cost of the cancelled warranties
     */
    public void setWarrantyRefund(double warrantyRefund) {
        this.warrantyRefund = warrantyRefund;
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
     * Calculates the total refund of this return as the sum of the
     * proportional refund of every returned item plus the refund of the
     * cancelled extended warranties, and stores it in {@code refundAmount}.
     *
     * @return the total amount to refund to the client
     */
    public double calculateRefundAmount() {
        double total = 0.0;
        for (Product product : returnedProducts) {
            total += calculateItemRefund(product);
        }
        total += warrantyRefund;
        this.refundAmount = round(total);
        return this.refundAmount;
    }

    /**
     * Generates the formatted receipt (in Spanish) of this return. For
     * every returned item it shows the list price, the proportional
     * discount and the refunded amount; then the refund of the cancelled
     * extended warranties (if any) and the total refund.
     *
     * @return the formatted receipt text
     */
    public String generateReturnReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Recibo de Devolución ---\n");
        sb.append("ID Devolución: ").append(id).append("\n");
        sb.append("Fecha: ").append(date).append("\n");
        sb.append("Venta original: ").append(originalSale.getId()).append("\n");
        if (originalSale.getAppliedPromotionName() != null
                && originalSale.getDiscountAmount() > 0) {
            sb.append("Promoción de la venta: ")
                    .append(originalSale.getAppliedPromotionName()).append("\n");
        }
        sb.append("Productos devueltos:\n");
        for (Product product : returnedProducts) {
            sb.append("  - ").append(product.getTitle()).append("\n");
            sb.append(String.format("      Precio de lista:        $%.2f%n", product.getPrice()));
            sb.append(String.format("      Descuento proporcional: -$%.2f%n",
                    calculateItemDiscount(product)));
            sb.append(String.format("      Monto reembolsado:      $%.2f%n",
                    calculateItemRefund(product)));
        }
        if (warrantyRefund > 0) {
            sb.append(String.format("Garantías extendidas anuladas: +$%.2f%n", warrantyRefund));
        }
        sb.append("Motivo: ").append(reason).append("\n");
        sb.append(String.format("Total reembolsado: $%.2f%n", refundAmount));
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
