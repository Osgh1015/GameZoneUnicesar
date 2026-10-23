package com.gamezone.model;

/**
 * Text layout of the receipt of a {@link Return}, in Spanish because it
 * is shown to the user of the store.
 * <p>
 * The return keeps its business rules (the proportional refund of each
 * item and the total refund); this class only decides how they are
 * presented, so a change in the layout of the receipt does not touch the
 * domain class.
 */
public class ReturnReceipt {

    private final Return returnItem;

    /**
     * Creates the receipt of a return.
     *
     * @param returnItem return whose receipt is built
     */
    public ReturnReceipt(Return returnItem) {
        this.returnItem = returnItem;
    }

    /**
     * Builds the receipt. For every returned item it shows the list price,
     * the proportional discount and the refunded amount; then the refund of
     * the cancelled extended warranties (if any) and the total refund.
     *
     * @return the formatted receipt text
     */
    public String format() {
        Sale originalSale = returnItem.getOriginalSale();
        StringBuilder sb = new StringBuilder();
        sb.append("--- Recibo de Devolución ---\n");
        sb.append("ID Devolución: ").append(returnItem.getId()).append("\n");
        sb.append("Fecha: ").append(returnItem.getDate()).append("\n");
        sb.append("Venta original: ").append(originalSale.getId()).append("\n");
        if (originalSale.getAppliedPromotionName() != null
                && originalSale.getDiscountAmount() > 0) {
            sb.append("Promoción de la venta: ")
                    .append(originalSale.getAppliedPromotionName()).append("\n");
        }
        sb.append("Productos devueltos:\n");
        for (Product product : returnItem.getReturnedProducts()) {
            sb.append("  - ").append(product.getTitle()).append("\n");
            sb.append(String.format("      Precio de lista:        $%.2f%n", product.getPrice()));
            sb.append(String.format("      Descuento proporcional: -$%.2f%n",
                    returnItem.calculateItemDiscount(product)));
            sb.append(String.format("      Monto reembolsado:      $%.2f%n",
                    returnItem.calculateItemRefund(product)));
        }
        if (returnItem.getWarrantyRefund() > 0) {
            sb.append(String.format("Garantías extendidas anuladas: +$%.2f%n",
                    returnItem.getWarrantyRefund()));
        }
        sb.append("Motivo: ").append(returnItem.getReason()).append("\n");
        sb.append(String.format("Total reembolsado: $%.2f%n", returnItem.getRefundAmount()));
        return sb.toString();
    }
}
