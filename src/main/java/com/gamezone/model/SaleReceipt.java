package com.gamezone.model;

/**
 * Text layout of the receipt of a {@link Sale}, in Spanish because it is
 * shown to the user of the store.
 * <p>
 * The sale keeps its business rules (subtotal, discount, warranty cost and
 * final total); this class only decides how they are presented, so a change
 * in the layout of the receipt does not touch the domain class.
 */
public class SaleReceipt {

    private final Sale sale;

    /**
     * Creates the receipt of a sale.
     *
     * @param sale sale whose receipt is built
     */
    public SaleReceipt(Sale sale) {
        this.sale = sale;
    }

    /**
     * Builds the receipt. It lists every item with its type and price, and
     * shows the full breakdown: subtotal of the items, discount with the name
     * of the applied promotion, cost of the extended warranties and final
     * total.
     *
     * @return the formatted receipt text
     */
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Recibo de Venta ---\n");
        sb.append("ID Venta: ").append(sale.getId()).append("\n");
        sb.append("Fecha: ").append(sale.getDate()).append("\n");
        sb.append("Cliente: ").append(sale.getClient().getFullName()).append("\n");
        sb.append("Vendedor: ").append(sale.getSeller().getFullName()).append("\n");
        sb.append("Productos:\n");
        for (Product product : sale.getProducts()) {
            sb.append(String.format("  - %s [%s] ($%.2f)%n",
                    product.getTitle(), product.getTypeLabel(), product.getPrice()));
        }
        sb.append(String.format("Subtotal: $%.2f%n", sale.calculateTotal()));
        if (sale.getAppliedPromotionName() != null && sale.getDiscountAmount() > 0) {
            sb.append(String.format("Descuento (%s): -$%.2f%n",
                    sale.getAppliedPromotionName(), sale.getDiscountAmount()));
        } else {
            sb.append("Descuento: $0.00 (sin promoción aplicada)\n");
        }
        if (sale.getWarrantyCost() > 0) {
            sb.append(String.format("Garantías extendidas: +$%.2f%n", sale.getWarrantyCost()));
        } else {
            sb.append("Garantías extendidas: $0.00 (no se solicitaron)\n");
        }
        sb.append(String.format("Total final: $%.2f%n", sale.calculateFinalTotal()));
        return sb.toString();
    }
}
