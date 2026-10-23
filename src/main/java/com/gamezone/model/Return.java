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

    /**
     * Tells whether this return was made on a sale of the given client.
     *
     * @param clientId identifier of the client
     * @return true if the original sale belongs to that client
     */
    public boolean belongsToClient(String clientId) {
        return originalSale.getClient().getId().equals(clientId);
    }

    /**
     * Tells whether this return was made on the given sale.
     *
     * @param saleId identifier of the sale
     * @return true if the original sale has that id
     */
    public boolean belongsToSale(String saleId) {
        return originalSale.getId().equals(saleId);
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
        double itemsSubtotal = originalSale.calculateTotal();
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
     * Generates the formatted receipt (in Spanish) of this return. The
     * layout lives in {@link ReturnReceipt}; this method is kept because
     * Requerimiento 3 defines it as part of the return.
     *
     * @return the formatted receipt text
     */
    public String generateReturnReceipt() {
        return new ReturnReceipt(this).format();
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
