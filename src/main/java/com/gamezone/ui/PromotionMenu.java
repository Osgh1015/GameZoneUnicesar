package com.gamezone.ui;

import com.gamezone.model.CategoryDiscount;
import com.gamezone.model.Promotion;
import com.gamezone.service.PromotionService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Submenu of the promotions module: registers the three kinds of
 * promotion and lists all and active promotions.
 * <p>
 * It only reads the user's input, calls the services and prints the
 * results; it never accesses persistence. It is created by
 * {@link ConsoleMenu}, which delegates to it the option of the main menu.
 */
public class PromotionMenu implements MenuOption {

    private final ConsoleInput userInput;
    private final PromotionService promotionService;

    /**
     * Creates the submenu.
     *
     * @param userInput shared reader of the console input
     * @param promotionService PromotionService used by this submenu
     */
    public PromotionMenu(
            ConsoleInput userInput,
            PromotionService promotionService) {
        this.userInput = userInput;
        this.promotionService = promotionService;
    }

    /**
     * {@inheritDoc}
     *
     * @return "Gestión de promociones"
     */
    @Override
    public String getTitle() {
        return "Gestión de promociones";
    }

    /**
     * Shows the promotions submenu and executes the option chosen by the user.
     */
    @Override
    public void show() {
        System.out.println("\n-- Gestión de promociones --");
        System.out.println("1. Registrar promoción por porcentaje");
        System.out.println("2. Registrar promoción por categoría");
        System.out.println("3. Registrar promoción por volumen de compra");
        System.out.println("4. Listar todas las promociones");
        System.out.println("5. Listar promociones vigentes");
        System.out.println("0. Volver");
        System.out.print("Seleccione una opción: ");

        String option = userInput.nextLine();

        switch (option) {
            case "1":
                registerPercentagePromotionFlow();
                break;

            case "2":
                registerCategoryPromotionFlow();
                break;

            case "3":
                registerBulkPromotionFlow();
                break;

            case "4":
                printPromotions(promotionService.listAllPromotions(),
                        "Aún no hay promociones registradas.");
                break;

            case "5":
                printPromotions(promotionService.listActivePromotions(),
                        "No hay promociones vigentes hoy.");
                break;

            case "0":
                break;

            default:
                System.out.println("Opción inválida.");
        }
    }

    private void printPromotions(List<Promotion> promotions, String emptyMessage) {
        if (promotions.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        for (Promotion promotion : promotions) {
            System.out.println(promotion.getDescription());
        }
    }

    private void registerPercentagePromotionFlow() {
        try {
            System.out.print("ID de la promoción: ");
            String id = userInput.nextLine();
            System.out.print("Nombre: ");
            String name = userInput.nextLine();
            LocalDate start = userInput.readDate("Fecha de inicio (AAAA-MM-DD): ");
            LocalDate end = userInput.readDate("Fecha de fin (AAAA-MM-DD): ");
            System.out.print("Porcentaje de descuento (0-100): ");
            double percentage = userInput.readDouble();

            promotionService.registerPercentageDiscount(id, name, start, end, percentage);
            System.out.println("Promoción registrada correctamente.");
        } catch (IllegalArgumentException | DateTimeParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void registerCategoryPromotionFlow() {
        try {
            System.out.print("ID de la promoción: ");
            String id = userInput.nextLine();
            System.out.print("Nombre: ");
            String name = userInput.nextLine();
            LocalDate start = userInput.readDate("Fecha de inicio (AAAA-MM-DD): ");
            LocalDate end = userInput.readDate("Fecha de fin (AAAA-MM-DD): ");
            System.out.print("Porcentaje de descuento (0-100): ");
            double percentage = userInput.readDouble();
            String category = readPromotionCategory();
            if (category == null) {
                System.out.println("Opción de categoría inválida.");
                return;
            }

            promotionService.registerCategoryDiscount(id, name, start, end, percentage, category);
            System.out.println("Promoción registrada correctamente.");
        } catch (IllegalArgumentException | DateTimeParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Asks the user to choose the target category of a category promotion.
     *
     * @return "VIDEOGAME", "CONSOLE" or "ACCESSORY", or {@code null} if the
     *         option is not valid
     */
    private String readPromotionCategory() {
        System.out.println("Categoría objetivo:");
        System.out.println("1. Videojuegos");
        System.out.println("2. Consolas");
        System.out.println("3. Accesorios");
        System.out.print("Seleccione una opción: ");
        String option = userInput.nextLine().trim();

        switch (option) {
            case "1":
                return CategoryDiscount.VIDEOGAME;
            case "2":
                return CategoryDiscount.CONSOLE;
            case "3":
                return CategoryDiscount.ACCESSORY;
            default:
                return null;
        }
    }

    private void registerBulkPromotionFlow() {
        try {
            System.out.print("ID de la promoción: ");
            String id = userInput.nextLine();
            System.out.print("Nombre: ");
            String name = userInput.nextLine();
            LocalDate start = userInput.readDate("Fecha de inicio (AAAA-MM-DD): ");
            LocalDate end = userInput.readDate("Fecha de fin (AAAA-MM-DD): ");
            System.out.print("Cantidad mínima de productos: ");
            int minQuantity = userInput.readInt();
            System.out.print("Porcentaje de descuento (0-100): ");
            double percentage = userInput.readDouble();

            promotionService.registerBulkPurchaseDiscount(id, name, start, end, minQuantity, percentage);
            System.out.println("Promoción registrada correctamente.");
        } catch (IllegalArgumentException | DateTimeParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
