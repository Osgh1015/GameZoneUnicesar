package com.gamezone.ui;

import com.gamezone.model.Warranty;
import com.gamezone.service.WarrantyService;

import java.util.List;

/**
 * Submenu of the warranties module: finds the warranty of a product in a
 * sale and lists all, active and soon-to-expire warranties.
 * <p>
 * It only reads the user's input, calls the services and prints the
 * results; it never accesses persistence. It is created by
 * {@link ConsoleMenu}, which delegates to it the option of the main menu.
 */
public class WarrantyMenu implements MenuOption {

    private final ConsoleInput userInput;
    private final WarrantyService warrantyService;

    /**
     * Creates the submenu.
     *
     * @param userInput shared reader of the console input
     * @param warrantyService WarrantyService used by this submenu
     */
    public WarrantyMenu(
            ConsoleInput userInput,
            WarrantyService warrantyService) {
        this.userInput = userInput;
        this.warrantyService = warrantyService;
    }

    /**
     * {@inheritDoc}
     *
     * @return "Gestión de garantías"
     */
    @Override
    public String getTitle() {
        return "Gestión de garantías";
    }

    /**
     * Shows the warranty management submenu and executes the option
     * chosen by the user.
     */
    @Override
    public void show() {
        System.out.println("\n-- Gestión de garantías --");
        System.out.println("1. Consultar la garantía de un producto en una venta");
        System.out.println("2. Listar todas las garantías registradas");
        System.out.println("3. Listar las garantías vigentes a la fecha actual");
        System.out.println("4. Listar las garantías próximas a vencer");
        System.out.println("0. Volver");
        System.out.print("Seleccione una opción: ");

        String option = userInput.nextLine();

        switch (option) {
            case "1":
                findWarrantyFlow();
                break;

            case "2":
                printWarranties(warrantyService.listAllWarranties(),
                        "Aún no hay garantías registradas.");
                break;

            case "3":
                printWarranties(warrantyService.listActiveWarranties(),
                        "No hay garantías vigentes en la fecha actual.");
                break;

            case "4":
                listWarrantiesExpiringSoonFlow();
                break;

            case "0":
                break;

            default:
                System.out.println("Opción inválida.");
        }
    }

    /**
     * Asks for a sale and a product and shows the certificate of the
     * warranty associated with that product inside that sale.
     */
    private void findWarrantyFlow() {
        System.out.print("ID de la venta: ");
        String saleId = userInput.nextLine();

        System.out.print("ID del producto: ");
        String productId = userInput.nextLine();

        Warranty warranty = warrantyService.findWarrantyByProduct(productId, saleId);

        if (warranty == null) {
            System.out.println("Ese producto no tiene garantía registrada en esa venta.");
        } else {
            System.out.println(warranty.generateWarrantyCertificate());
        }
    }

    /**
     * Asks for the number of days of anticipation and lists the
     * warranties that expire within that period.
     */
    private void listWarrantiesExpiringSoonFlow() {
        System.out.print("Días de anticipación (por ejemplo 30): ");
        String input = userInput.nextLine();

        int daysAhead;

        try {
            daysAhead = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            System.out.println("Debe ingresar un número entero de días.");
            return;
        }

        if (daysAhead < 0) {
            System.out.println("Los días de anticipación no pueden ser negativos.");
            return;
        }

        System.out.println("Garantías que vencen en los próximos " + daysAhead + " días:");
        printWarranties(warrantyService.listWarrantiesExpiringSoon(daysAhead),
                "No hay garantías próximas a vencer en ese periodo.");
    }

    /**
     * Prints a list of warranties, or a message when the list is empty.
     *
     * @param warranties   warranties to print
     * @param emptyMessage message shown when there is nothing to print
     */
    private void printWarranties(List<Warranty> warranties, String emptyMessage) {
        if (warranties.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        for (Warranty warranty : warranties) {
            System.out.println(warranty);
        }
    }
}
