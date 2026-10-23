package com.gamezone.ui;

import com.gamezone.model.Accessory;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;
import com.gamezone.service.WarrantyService;

import java.util.ArrayList;
import java.util.List;

/**
 * Submenu of the sales module: registers a sale (offering the extended
 * warranty of each console) and shows the sales history and receipts.
 * <p>
 * It only reads the user's input, calls the services and prints the
 * results; it never accesses persistence. It is created by
 * {@link ConsoleMenu}, which delegates to it the option of the main menu.
 */
public class SaleMenu implements MenuOption {

    private final ConsoleInput userInput;
    private final ProductService productService;
    private final AccessoryService accessoryService;
    private final SaleService saleService;
    private final WarrantyService warrantyService;

    /**
     * Creates the submenu.
     *
     * @param userInput shared reader of the console input
     * @param productService ProductService used by this submenu
     * @param accessoryService AccessoryService used by this submenu
     * @param saleService SaleService used by this submenu
     * @param warrantyService WarrantyService used by this submenu
     */
    public SaleMenu(
            ConsoleInput userInput,
            ProductService productService,
            AccessoryService accessoryService,
            SaleService saleService,
            WarrantyService warrantyService) {
        this.userInput = userInput;
        this.productService = productService;
        this.accessoryService = accessoryService;
        this.saleService = saleService;
        this.warrantyService = warrantyService;
    }

    /**
     * {@inheritDoc}
     *
     * @return "Menú de ventas"
     */
    @Override
    public String getTitle() {
        return "Menú de ventas";
    }

    /**
     * Shows the sales submenu and executes the option chosen by the user.
     */
    @Override
    public void show() {
        System.out.println("\n-- Menú de ventas --");
        System.out.println("1. Registrar venta");
        System.out.println("2. Historial completo de ventas");
        System.out.println("3. Historial de compras por cliente");
        System.out.println("4. Historial de ventas por vendedor");
        System.out.println("5. Ver detalle de una venta (recibo)");
        System.out.println("0. Volver");
        System.out.print("Seleccione una opción: ");

        String option = userInput.nextLine();

        switch (option) {
            case "1":
                registerSaleFlow();
                break;

            case "2":
                for (Sale sale : saleService.getSalesHistory()) {
                    System.out.println(sale);
                }
                break;

            case "3":
                System.out.print("ID del cliente: ");
                String clientId = userInput.nextLine();

                for (Sale sale : saleService.getSalesByClient(clientId)) {
                    System.out.println(sale);
                }
                break;

            case "4":
                System.out.print("ID del vendedor: ");
                String sellerId = userInput.nextLine();

                for (Sale sale : saleService.getSalesBySeller(sellerId)) {
                    System.out.println(sale);
                }
                break;

            case "5":
                System.out.print("ID de la venta: ");
                Sale detail = saleService.getSaleDetail(userInput.nextLine());
                System.out.println(detail.generateReceipt());
                break;

            case "0":
                break;

            default:
                System.out.println("Opción inválida.");
        }
    }

    private void registerSaleFlow() {
        System.out.print("ID del cliente: ");
        String clientId = userInput.nextLine();

        System.out.print("ID del vendedor: ");
        String sellerId = userInput.nextLine();

        printSellableItems();

        List<String> productIds = new ArrayList<>();
        List<String> productIdsWithExtendedWarranty = new ArrayList<>();

        boolean addingProducts = true;

        while (addingProducts) {
            System.out.print("ID del producto o accesorio (deje vacío para finalizar): ");
            String productId = userInput.nextLine();

            if (productId.isEmpty()) {
                addingProducts = false;
            } else {
                productIds.add(productId);
                askForExtendedWarranty(productId, productIdsWithExtendedWarranty);
            }
        }

        Sale sale = saleService.registerSale(
                clientId,
                sellerId,
                productIds,
                productIdsWithExtendedWarranty
        );

        // registerSale no longer returns null: if the sale cannot be
        // registered it throws a custom exception, handled in start().
        System.out.println("Venta registrada correctamente.");
        System.out.println(sale.generateReceipt());
        showWarrantiesOfSale(sale);
    }

    /**
     * Shows the products (video games and consoles) and the accessories
     * that have stock, so the seller can choose the items of the sale.
     * Consoles are marked because they admit extended warranty.
     */
    private void printSellableItems() {
        System.out.println("Productos disponibles:");
        boolean anyProduct = false;
        for (Product product : productService.listAll()) {
            if (product.getQuantity() > 0) {
                String type = product.isWarrantyEligible()
                        ? product.getTypeLabel() + " - admite garantía extendida"
                        : product.getTypeLabel();
                System.out.printf("  %s | %s [%s] | $%.2f | stock: %d%n",
                        product.getId(), product.getTitle(), type, product.getPrice(), product.getQuantity());
                anyProduct = true;
            }
        }
        if (!anyProduct) {
            System.out.println("  (no hay productos con stock)");
        }

        System.out.println("Accesorios disponibles:");
        boolean anyAccessory = false;
        for (Accessory accessory : accessoryService.listAllAccessories()) {
            if (accessory.getQuantity() > 0) {
                System.out.printf("  %s | %s [Accesorio] | $%.2f | stock: %d%n",
                        accessory.getId(), accessory.getTitle(), accessory.getPrice(), accessory.getQuantity());
                anyAccessory = true;
            }
        }
        if (!anyAccessory) {
            System.out.println("  (no hay accesorios con stock)");
        }
    }

    /**
     * Asks the seller whether the client wants extended warranty for an
     * item just added to the sale. Only consoles admit extended warranty,
     * so the question is skipped for any other kind of item.
     *
     * @param productId                      id of the item just added to the sale
     * @param productIdsWithExtendedWarranty list where the accepted ids are collected
     */
    private void askForExtendedWarranty(String productId, List<String> productIdsWithExtendedWarranty) {

        Product product = productService.findById(productId);

        if (product == null || !product.isWarrantyEligible()) {
            return;
        }

        System.out.printf("La consola \"%s\" incluye garantía básica de 6 meses sin costo.%n",
                product.getTitle());
        System.out.printf("¿Desea agregar garantía extendida de 12 meses por $%.2f? (s/n): ",
                ExtendedWarranty.quoteFor(product));

        String answer = userInput.nextLine().trim();

        if (answer.equalsIgnoreCase("s")) {
            productIdsWithExtendedWarranty.add(productId);
            System.out.println("Garantía extendida agregada.");
        }
    }

    /**
     * Shows the warranties generated by a sale that has just been
     * registered, so the seller can confirm the coverage with the client.
     *
     * @param sale sale that was registered
     */
    private void showWarrantiesOfSale(Sale sale) {

        List<Warranty> saleWarranties = new ArrayList<>();

        for (Warranty warranty : warrantyService.listAllWarranties()) {
            if (warranty.belongsToSale(sale.getId())) {
                saleWarranties.add(warranty);
            }
        }

        if (saleWarranties.isEmpty()) {
            System.out.println("Esta venta no generó garantías.");
            return;
        }

        System.out.println("Garantías generadas por esta venta:");
        for (Warranty warranty : saleWarranties) {
            System.out.println(" - " + warranty);
        }
    }
}
