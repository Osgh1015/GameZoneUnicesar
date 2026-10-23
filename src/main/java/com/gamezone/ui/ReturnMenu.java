package com.gamezone.ui;

import com.gamezone.model.Return;
import com.gamezone.service.ReturnService;

import java.util.ArrayList;
import java.util.List;

/**
 * Submenu of the returns module: registers returns, lists them and shows
 * the monthly balance.
 * <p>
 * It only reads the user's input, calls the services and prints the
 * results; it never accesses persistence. It is created by
 * {@link ConsoleMenu}, which delegates to it the option of the main menu.
 */
public class ReturnMenu {

    private final ConsoleInput userInput;
    private final ReturnService returnService;

    /**
     * Creates the submenu.
     *
     * @param userInput shared reader of the console input
     * @param returnService ReturnService used by this submenu
     */
    public ReturnMenu(
            ConsoleInput userInput,
            ReturnService returnService) {
        this.userInput = userInput;
        this.returnService = returnService;
    }

    /**
     * Shows the returns submenu and executes the option chosen by the user.
     */
    public void show() {
        System.out.println("\n-- Gestión de devoluciones --");
        System.out.println("1. Registrar una devolución");
        System.out.println("2. Consultar todas las devoluciones");
        System.out.println("3. Consultar devoluciones por cliente");
        System.out.println("4. Consultar devoluciones por venta");
        System.out.println("5. Consultar balance mensual");
        System.out.println("0. Volver");
        System.out.print("Seleccione una opción: ");

        String option = userInput.nextLine();

        switch (option) {
            case "1":
                registerReturnFlow();
                break;

            case "2":
                for (Return r : returnService.viewAllReturns()) {
                    System.out.println(r.generateReturnReceipt());
                }
                break;

            case "3":
                System.out.print("ID del cliente: ");
                String customerId = userInput.nextLine();
                for (Return r : returnService.viewReturnsByCustomer(customerId)) {
                    System.out.println(r.generateReturnReceipt());
                }
                break;

            case "4":
                System.out.print("ID de la venta: ");
                String saleId = userInput.nextLine();
                for (Return r : returnService.viewReturnsBySale(saleId)) {
                    System.out.println(r.generateReturnReceipt());
                }
                break;

            case "5":
                monthlyBalanceFlow();
                break;

            case "0":
                break;

            default:
                System.out.println("Opción inválida.");
        }
    }

    /**
     * Asks for a month and a year and shows the monthly balance report:
     * total sales (final totals, including discounts and extended
     * warranties), total returns and net balance.
     */
    private void monthlyBalanceFlow() {
        int month;
        int year;
        try {
            System.out.print("Mes (1-12): ");
            month = Integer.parseInt(userInput.nextLine().trim());
            System.out.print("Año: ");
            year = Integer.parseInt(userInput.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Debe ingresar números enteros para el mes y el año.");
            return;
        }

        if (month < 1 || month > 12) {
            System.out.println("El mes debe estar entre 1 y 12.");
            return;
        }

        double totalSales = returnService.calculateMonthlySales(month, year);
        double totalReturns = returnService.calculateMonthlyReturns(month, year);
        double balance = returnService.generateMonthlyBalance(month, year);

        System.out.printf("--- Balance mensual %02d/%d ---%n", month, year);
        System.out.printf("Total de ventas:       $%.2f%n", totalSales);
        System.out.printf("Total de devoluciones: -$%.2f%n", totalReturns);
        System.out.printf("Balance neto:          $%.2f%n", balance);
    }

    private void registerReturnFlow() {
        System.out.print("ID de la venta original: ");
        String saleId = userInput.nextLine();

        List<String> productIds = new ArrayList<>();
        boolean addingProducts = true;
        while (addingProducts) {
            System.out.print("ID de producto a devolver (deje vacío para finalizar): ");
            String productId = userInput.nextLine();
            if (productId.isEmpty()) {
                addingProducts = false;
            } else {
                productIds.add(productId);
            }
        }

        System.out.print("Motivo de la devolución: ");
        String reason = userInput.nextLine();

        try {
            Return r = returnService.registerReturn(saleId, productIds, reason);
            System.out.println(r.generateReturnReceipt());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
