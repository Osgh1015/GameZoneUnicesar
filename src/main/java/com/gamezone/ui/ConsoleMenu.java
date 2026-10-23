package com.gamezone.ui;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.MonthlyBalanceService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.PromotionService;
import com.gamezone.service.ReturnService;
import com.gamezone.service.SaleService;
import com.gamezone.service.WarrantyService;

import java.util.List;
import java.util.Scanner;

/**
 * Main menu of the console user interface of the GameZone system.
 * <p>
 * It shows the main options, delegates each one to the submenu of its
 * module ({@link ProductMenu}, {@link PersonMenu}, {@link SaleMenu},
 * {@link AccessoryMenu}, {@link ReturnMenu}, {@link PromotionMenu} and
 * {@link WarrantyMenu}) and handles, in a single place, the custom
 * exceptions thrown by any operation. The submenus share one
 * {@link ConsoleInput}, so all of them read from the same scanner.
 *
 * Responsabilidad: Líder Técnico - integra los módulos.
 */
public class ConsoleMenu {

    private final ConsoleInput userInput = new ConsoleInput(new Scanner(System.in));
    private final List<MenuOption> options;

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
        this.options = List.of(
                new ProductMenu(userInput, productService),
                new PersonMenu(userInput, personService),
                new SaleMenu(userInput, productService, accessoryService, saleService, warrantyService),
                new AccessoryMenu(userInput, accessoryService),
                new ReturnMenu(userInput, returnService,
                        new MonthlyBalanceService(saleService, returnService)),
                new PromotionMenu(userInput, promotionService),
                new WarrantyMenu(userInput, warrantyService));
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
            for (int i = 0; i < options.size(); i++) {
                System.out.println((i + 1) + ". " + options.get(i).getTitle());
            }
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");

            String option = userInput.nextLine();

            try {
                MenuOption chosen = findOption(option);
                if (chosen != null) {
                    chosen.show();
                } else if (option.equals("0")) {
                    running = false;
                    System.out.println("¡Hasta la próxima!");
                } else {
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
     * Finds the option whose number (its position in the main menu, from 1)
     * is exactly the text typed by the user.
     *
     * @param option text typed by the user
     * @return the chosen option, or {@code null} if no option has that number
     */
    private MenuOption findOption(String option) {
        for (int i = 0; i < options.size(); i++) {
            if (option.equals(String.valueOf(i + 1))) {
                return options.get(i);
            }
        }
        return null;
    }
}
