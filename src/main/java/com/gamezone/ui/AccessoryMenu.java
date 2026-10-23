package com.gamezone.ui;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import com.gamezone.service.AccessoryService;

import java.util.ArrayList;
import java.util.List;

/**
 * Submenu of the accessories module: registers controllers, cables and
 * memory units and answers the catalogue and compatibility queries.
 * <p>
 * It only reads the user's input, calls the services and prints the
 * results; it never accesses persistence. It is created by
 * {@link ConsoleMenu}, which delegates to it the option of the main menu.
 */
public class AccessoryMenu implements MenuOption {

    private final ConsoleInput userInput;
    private final AccessoryService accessoryService;

    /**
     * Creates the submenu.
     *
     * @param userInput shared reader of the console input
     * @param accessoryService AccessoryService used by this submenu
     */
    public AccessoryMenu(
            ConsoleInput userInput,
            AccessoryService accessoryService) {
        this.userInput = userInput;
        this.accessoryService = accessoryService;
    }

    /**
     * {@inheritDoc}
     *
     * @return "Gestión de accesorios"
     */
    @Override
    public String getTitle() {
        return "Gestión de accesorios";
    }

    /**
     * Shows the accessories submenu and executes the option chosen by the user.
     */
    @Override
    public void show() {
        System.out.println("\n-- Gestión de accesorios --");
        System.out.println("1. Registrar control");
        System.out.println("2. Registrar cable");
        System.out.println("3. Registrar memoria");
        System.out.println("4. Listar todos los accesorios");
        System.out.println("5. Listar accesorios por tipo");
        System.out.println("6. Consultar accesorios compatibles con una consola");
        System.out.println("0. Volver");
        System.out.print("Seleccione una opción: ");

        String option = userInput.nextLine();

        switch (option) {
            case "1":
                registerControllerFlow();
                break;

            case "2":
                registerCableFlow();
                break;

            case "3":
                registerMemoryFlow();
                break;

            case "4":
                for (Accessory accessory : accessoryService.listAllAccessories()) {
                    System.out.println(accessory.getDescription());
                }
                break;

            case "5":
                System.out.print("Tipo (CONTROLLER, CABLE, MEMORY): ");
                String type = userInput.nextLine();

                List<Accessory> byType = accessoryService.listAccessoriesByType(type);
                if (byType.isEmpty()) {
                    System.out.println("No hay accesorios de ese tipo.");
                } else {
                    for (Accessory accessory : byType) {
                        System.out.println(accessory.getDescription());
                    }
                }
                break;

            case "6":
                System.out.print("ID de la consola: ");
                String consoleId = userInput.nextLine();

                List<Accessory> compatible = accessoryService.findAccessoriesCompatibleWith(consoleId);
                if (compatible.isEmpty()) {
                    System.out.println("No hay accesorios compatibles con esa consola.");
                } else {
                    for (Accessory accessory : compatible) {
                        System.out.println(accessory.getDescription());
                    }
                }
                break;

            case "0":
                break;

            default:
                System.out.println("Opción inválida.");
        }
    }

    private void registerControllerFlow() {
        System.out.print("ID del accesorio: ");
        String id = userInput.nextLine();
        System.out.print("Título: ");
        String title = userInput.nextLine();
        System.out.print("Precio: ");
        double price = userInput.readDouble();
        System.out.print("Cantidad en stock: ");
        int quantity = userInput.readInt();
        System.out.print("Tipo de conexión (Inalambrico / Alambrico): ");
        String connectionType = userInput.nextLine();

        List<String> compatibleConsoleIds = readCompatibleConsoles();

        Controller controller = new Controller(id, title, price, quantity, compatibleConsoleIds, connectionType);
        accessoryService.registerController(controller);
        System.out.println("Control registrado correctamente.");
    }

    private void registerCableFlow() {
        System.out.print("ID del accesorio: ");
        String id = userInput.nextLine();
        System.out.print("Título: ");
        String title = userInput.nextLine();
        System.out.print("Precio: ");
        double price = userInput.readDouble();
        System.out.print("Cantidad en stock: ");
        int quantity = userInput.readInt();
        System.out.print("Longitud en metros: ");
        double lengthInMeters = userInput.readDouble();
        System.out.print("Tipo de conector (HDMI, USB, Óptico...): ");
        String connectorType = userInput.nextLine();

        List<String> compatibleConsoleIds = readCompatibleConsoles();

        Cable cable = new Cable(id, title, price, quantity, compatibleConsoleIds, lengthInMeters, connectorType);
        accessoryService.registerCable(cable);
        System.out.println("Cable registrado correctamente.");
    }

    private void registerMemoryFlow() {
        System.out.print("ID del accesorio: ");
        String id = userInput.nextLine();
        System.out.print("Título: ");
        String title = userInput.nextLine();
        System.out.print("Precio: ");
        double price = userInput.readDouble();
        System.out.print("Cantidad en stock: ");
        int quantity = userInput.readInt();
        System.out.print("Capacidad en GB: ");
        int capacityInGb = userInput.readInt();
        System.out.print("Tipo de memoria (SD, microSD, interna...): ");
        String memoryType = userInput.nextLine();

        List<String> compatibleConsoleIds = readCompatibleConsoles();

        Memory memory = new Memory(id, title, price, quantity, compatibleConsoleIds, capacityInGb, memoryType);
        accessoryService.registerMemory(memory);
        System.out.println("Memoria registrada correctamente.");
    }

    private List<String> readCompatibleConsoles() {
        List<String> compatibleConsoleIds = new ArrayList<>();
        boolean adding = true;

        while (adding) {
            System.out.print("ID de consola compatible (deje vacío para finalizar): ");
            String consoleId = userInput.nextLine();

            if (consoleId.isEmpty()) {
                adding = false;
            } else {
                compatibleConsoleIds.add(consoleId);
            }
        }
        return compatibleConsoleIds;
    }
}
