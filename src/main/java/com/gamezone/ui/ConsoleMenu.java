package com.gamezone.ui;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.CategoryDiscount;
import com.gamezone.model.Client;
import com.gamezone.model.Console;
import com.gamezone.model.Controller;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Memory;
import com.gamezone.model.Product;
import com.gamezone.model.Promotion;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.model.VideoGame;
import com.gamezone.model.Warranty;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.PromotionService;
import com.gamezone.service.SaleService;
import com.gamezone.model.Return;
import com.gamezone.service.ReturnService;
import com.gamezone.service.WarrantyService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Console-based user interface for the GameZone system. It exposes the
 * ten functional operations required by the system, organized into
 * submenus for the products, people and sales modules. This class only
 * calls service classes; it never accesses persistence directly.
 *
 * Responsabilidad: Líder Técnico - integra los tres módulos.
 */
public class ConsoleMenu {

    private final ConsoleInput userInput = new ConsoleInput(new Scanner(System.in));
    private final ProductMenu productMenu;
    private final PersonMenu personMenu;
    private final SaleMenu saleMenu;
    private final AccessoryMenu accessoryMenu;
    private final ReturnMenu returnMenu;
    private final PromotionMenu promotionMenu;
    private ProductService productService;
    private PersonService personService;
    private SaleService saleService;
    private AccessoryService accessoryService;
    private ReturnService returnService;
    private PromotionService promotionService;
    private WarrantyService warrantyService;

    /**
     * Creates the console menu with the services of every module.
     *
     * @param productService   service of the products module
     * @param personService    service of the people module
     * @param saleService      service of the sales module
     * @param accessoryService service of the accessories module
     * @param returnService    service of the returns module
     * @param promotionService service of the promotions module
     * @param warrantyService  service of the warranties module
     */
    public ConsoleMenu(ProductService productService, PersonService personService,
                       SaleService saleService, AccessoryService accessoryService,
                       ReturnService returnService, PromotionService promotionService,
                       WarrantyService warrantyService) {
        this.productService = productService;
        this.personService = personService;
        this.saleService = saleService;
        this.accessoryService = accessoryService;
        this.returnService = returnService;
        this.promotionService = promotionService;
        this.warrantyService = warrantyService;
        // submenus
        this.productMenu = new ProductMenu(userInput, productService);
        this.personMenu = new PersonMenu(userInput, personService);
        this.saleMenu = new SaleMenu(userInput, productService, accessoryService, saleService, warrantyService);
        this.accessoryMenu = new AccessoryMenu(userInput, accessoryService);
        this.returnMenu = new ReturnMenu(userInput, returnService);
        this.promotionMenu = new PromotionMenu(userInput, promotionService);
    }

    /**
     * Starts the main loop of the console menu.
     * <p>
     * Every operation runs inside a single {@code try} block with one
     * {@code catch} per category of error, ordered from the most specific
     * exception to the most general one. Each category shows a different
     * message to the user, and no stack trace is ever shown.
     */
    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== GAMEZONE UNICESAR ===");
            System.out.println("1. Menú de productos");
            System.out.println("2. Menú de personas");
            System.out.println("3. Menú de ventas");
            System.out.println("4. Gestión de accesorios");
            System.out.println("5. Gestión de devoluciones");
            System.out.println("6. Gestión de promociones");
            System.out.println("7. Gestión de garantías");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");

            String option = userInput.nextLine();

            try {
                switch (option) {
                    case "1":
                        productMenu.show();
                        break;

                    case "2":
                        personMenu.show();
                        break;

                    case "3":
                        saleMenu.show();
                        break;

                    case "4":
                        accessoryMenu.show();
                        break;

                    case "5":
                        returnMenu.show();
                        break;

                    case "6":
                        promotionMenu.show();
                        break;

                    case "7":
                        warrantiesMenu();
                        break;

                    case "0":
                        running = false;
                        System.out.println("¡Hasta la próxima!");
                        break;

                    default:
                        System.out.println("Opción inválida.");
                }
            } catch (ResourceNotFoundException e) {
                System.out.println("No se encontró el recurso solicitado: " + e.getMessage());
            } catch (BusinessRuleException e) {
                System.out.println("No se puede completar la operación: " + e.getMessage());
            } catch (InvalidDataException e) {
                System.out.println("Los datos ingresados no son válidos: " + e.getMessage());
            } catch (PersistenceException e) {
                System.out.println("Ocurrió un problema al acceder a los datos del sistema. "
                        + "Contacte al administrador.");
            } catch (RuntimeException e) {
                // Safety net: an unexpected error must not close the application
                // nor show a technical message to the user.
                System.out.println("Ocurrió un error inesperado. Intente de nuevo.");
            }
        }
    }

    /**
     * Shows the warranty management submenu and executes the option
     * chosen by the user.
     */
    private void warrantiesMenu() {
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
