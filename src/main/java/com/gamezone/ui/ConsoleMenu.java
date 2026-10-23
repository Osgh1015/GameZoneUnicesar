package com.gamezone.ui;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;

import java.util.List;

/**
 * Main menu of the console user interface of the GameZone system.
 * <p>
 * It prints the title of each {@link MenuOption} it receives, runs the
 * option chosen by the user and handles, in a single place, the custom
 * exceptions thrown by any operation. The options (one submenu per module)
 * are composed by {@code Main}, so a new module is added without changing
 * this class.
 *
 * Responsabilidad: Líder Técnico - integra los módulos.
 */
public class ConsoleMenu {

    private final ConsoleInput userInput;
    private final List<MenuOption> options;

    /**
     * Creates the console menu with the options of the main menu, composed
     * by {@code Main}.
     *
     * @param userInput shared reader of the console input
     * @param options   the submenus, in the order they are shown (1, 2, ...)
     */
    public ConsoleMenu(ConsoleInput userInput, List<MenuOption> options) {
        this.userInput = userInput;
        this.options = List.copyOf(options);
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
