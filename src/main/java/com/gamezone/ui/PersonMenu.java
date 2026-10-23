package com.gamezone.ui;

import com.gamezone.model.Client;
import com.gamezone.model.Seller;
import com.gamezone.service.PersonService;

/**
 * Submenu of the people module: registers clients and lists clients and
 * sellers.
 * <p>
 * It only reads the user's input, calls the services and prints the
 * results; it never accesses persistence. It is created by
 * {@link ConsoleMenu}, which delegates to it the option of the main menu.
 */
public class PersonMenu {

    private final ConsoleInput userInput;
    private final PersonService personService;

    /**
     * Creates the submenu.
     *
     * @param userInput shared reader of the console input
     * @param personService PersonService used by this submenu
     */
    public PersonMenu(
            ConsoleInput userInput,
            PersonService personService) {
        this.userInput = userInput;
        this.personService = personService;
    }

    /**
     * Shows the people submenu and executes the option chosen by the user.
     */
    public void show() {
        System.out.println("\n-- Menú de personas --");
        System.out.println("1. Registrar cliente");
        System.out.println("2. Listar clientes");
        System.out.println("3. Listar vendedores");
        System.out.println("0. Volver");
        System.out.print("Seleccione una opción: ");

        String option = userInput.nextLine();

        switch (option) {
            case "1":
                registerClientFlow();
                break;

            case "2":
                for (Client c : personService.listClients()) {
                    System.out.println(c.getDescription());
                }
                break;

            case "3":
                for (Seller s : personService.listSellers()) {
                    System.out.println(s.getDescription());
                }
                break;

            case "0":
                break;

            default:
                System.out.println("Opción inválida.");
        }
    }

    private void registerClientFlow() {
        System.out.print("ID del cliente: ");
        String id = userInput.nextLine();
        System.out.print("Nombre completo: ");
        String name = userInput.nextLine();
        System.out.print("Teléfono: ");
        String phone = userInput.nextLine();
        System.out.print("Correo electrónico: ");
        String email = userInput.nextLine();

        // Validation errors are thrown as custom exceptions and handled by
        // the differentiated catch blocks of start().
        Client client = new Client(id, name, phone, email);
        personService.registerClient(client);
        System.out.println("Cliente registrado correctamente.");
    }
}
