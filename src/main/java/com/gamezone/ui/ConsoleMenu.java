package com.gamezone.ui;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Client;
import com.gamezone.model.Console;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.model.VideoGame;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;

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

    private Scanner scanner = new Scanner(System.in);
    private ProductService productService;
    private PersonService personService;
    private SaleService saleService;
    private AccessoryService accessoryService;

    public ConsoleMenu(ProductService productService, PersonService personService,
                       SaleService saleService, AccessoryService accessoryService) {
        this.productService = productService;
        this.personService = personService;
        this.saleService = saleService;
        this.accessoryService = accessoryService;
    }

    /** Starts the main loop of the console menu. */
    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== GAMEZONE UNICESAR ===");
            System.out.println("1. Menú de productos");
            System.out.println("2. Menú de personas");
            System.out.println("3. Menú de ventas");
            System.out.println("4. Gestión de accesorios");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");

            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    productsMenu();
                    break;

                case "2":
                    peopleMenu();
                    break;

                case "3":
                    salesMenu();
                    break;

                case "4":
                    accessoriesMenu();
                    break;

                case "0":
                    running = false;
                    System.out.println("¡Hasta la próxima!");
                    break;

                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    private void productsMenu() {
        System.out.println("\n-- Menú de productos --");
        System.out.println("1. Registrar videojuego");
        System.out.println("2. Registrar consola");
        System.out.println("3. Listar productos");
        System.out.println("0. Volver");
        System.out.print("Seleccione una opción: ");

        String option = scanner.nextLine();

        switch (option) {
            case "1":
                registerVideoGameFlow();
                break;

            case "2":
                registerConsoleFlow();
                break;

            case "3":
                List<Product> products = productService.listAll();

                if (products.isEmpty()) {
                    System.out.println("Aún no hay productos registrados.");
                }

                for (Product p : products) {
                    System.out.println(
                            p.getDescription() + " | stock: " + p.getQuantity()
                    );
                }
                break;

            case "0":
                break;

            default:
                System.out.println("Opción inválida.");
        }
    }

    private void registerVideoGameFlow() {
        System.out.print("ID del producto: ");
        String id = scanner.nextLine();
        System.out.print("Título: ");
        String title = scanner.nextLine();
        System.out.print("Precio: ");
        double price = Double.parseDouble(scanner.nextLine());
        System.out.print("Cantidad en stock: ");
        int quantity = Integer.parseInt(scanner.nextLine());
        System.out.print("Plataforma: ");
        String platform = scanner.nextLine();
        System.out.print("Género: ");
        String genre = scanner.nextLine();
        System.out.print("Clasificación de edad: ");
        String ageRating = scanner.nextLine();

        VideoGame videoGame = new VideoGame(id, title, price, quantity, platform, genre, ageRating);
        productService.registerVideoGame(videoGame);
        System.out.println("Videojuego registrado correctamente.");
    }

    private void registerConsoleFlow() {
        System.out.print("ID del producto: ");
        String id = scanner.nextLine();
        System.out.print("Título: ");
        String title = scanner.nextLine();
        System.out.print("Precio: ");
        double price = Double.parseDouble(scanner.nextLine());
        System.out.print("Cantidad en stock: ");
        int quantity = Integer.parseInt(scanner.nextLine());
        System.out.print("Marca: ");
        String brand = scanner.nextLine();
        System.out.print("Modelo: ");
        String model = scanner.nextLine();
        System.out.print("Generación: ");
        int generation = Integer.parseInt(scanner.nextLine());

        Console console = new Console(id, title, price, quantity, brand, model, generation);
        productService.registerConsole(console);
        System.out.println("Consola registrada correctamente.");
    }

    private void peopleMenu() {
        System.out.println("\n-- Menú de personas --");
        System.out.println("1. Registrar cliente");
        System.out.println("2. Listar clientes");
        System.out.println("3. Listar vendedores");
        System.out.println("0. Volver");
        System.out.print("Seleccione una opción: ");

        String option = scanner.nextLine();

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
        String id = scanner.nextLine();
        System.out.print("Nombre completo: ");
        String name = scanner.nextLine();
        System.out.print("Teléfono: ");
        String phone = scanner.nextLine();
        System.out.print("Correo electrónico: ");
        String email = scanner.nextLine();

        Client client = new Client(id, name, phone, email);
        personService.registerClient(client);
        System.out.println("Cliente registrado correctamente.");
    }

    private void salesMenu() {
        System.out.println("\n-- Menú de ventas --");
        System.out.println("1. Registrar venta");
        System.out.println("2. Historial completo de ventas");
        System.out.println("3. Historial de compras por cliente");
        System.out.println("4. Historial de ventas por vendedor");
        System.out.println("0. Volver");
        System.out.print("Seleccione una opción: ");

        String option = scanner.nextLine();

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
                String clientId = scanner.nextLine();

                for (Sale sale : saleService.getSalesByClient(clientId)) {
                    System.out.println(sale);
                }
                break;

            case "4":
                System.out.print("ID del vendedor: ");
                String sellerId = scanner.nextLine();

                for (Sale sale : saleService.getSalesBySeller(sellerId)) {
                    System.out.println(sale);
                }
                break;

            case "0":
                break;

            default:
                System.out.println("Opción inválida.");
        }
    }

    private void registerSaleFlow() {
        System.out.print("ID del cliente: ");
        String clientId = scanner.nextLine();

        System.out.print("ID del vendedor: ");
        String sellerId = scanner.nextLine();

        List<String> productIds = new ArrayList<>();

        boolean addingProducts = true;

        while (addingProducts) {
            System.out.print("ID del producto (deje vacío para finalizar): ");
            String productId = scanner.nextLine();

            if (productId.isEmpty()) {
                addingProducts = false;
            } else {
                productIds.add(productId);
            }
        }

        Sale sale = saleService.registerSale(
                clientId,
                sellerId,
                productIds
        );

        if (sale != null) {
            System.out.println("Venta registrada correctamente: " + sale);
        } else {
            System.out.println("No se pudo registrar la venta.");
        }
    }

    private void accessoriesMenu() {
        System.out.println("\n-- Gestión de accesorios --");
        System.out.println("1. Registrar control");
        System.out.println("2. Registrar cable");
        System.out.println("3. Registrar memoria");
        System.out.println("4. Listar todos los accesorios");
        System.out.println("5. Listar accesorios por tipo");
        System.out.println("6. Consultar accesorios compatibles con una consola");
        System.out.println("0. Volver");
        System.out.print("Seleccione una opción: ");

        String option = scanner.nextLine();

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
                String type = scanner.nextLine();

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
                String consoleId = scanner.nextLine();

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
        String id = scanner.nextLine();
        System.out.print("Título: ");
        String title = scanner.nextLine();
        System.out.print("Precio: ");
        double price = Double.parseDouble(scanner.nextLine());
        System.out.print("Cantidad en stock: ");
        int quantity = Integer.parseInt(scanner.nextLine());
        System.out.print("Tipo de conexión (Inalambrico / Alambrico): ");
        String connectionType = scanner.nextLine();

        List<String> compatibleConsoleIds = readCompatibleConsoles();

        Controller controller = new Controller(id, title, price, quantity, compatibleConsoleIds, connectionType);
        accessoryService.registerController(controller);
        System.out.println("Control registrado correctamente.");
    }

    private void registerCableFlow() {
        System.out.print("ID del accesorio: ");
        String id = scanner.nextLine();
        System.out.print("Título: ");
        String title = scanner.nextLine();
        System.out.print("Precio: ");
        double price = Double.parseDouble(scanner.nextLine());
        System.out.print("Cantidad en stock: ");
        int quantity = Integer.parseInt(scanner.nextLine());
        System.out.print("Longitud en metros: ");
        double lengthInMeters = Double.parseDouble(scanner.nextLine());
        System.out.print("Tipo de conector (HDMI, USB, Óptico...): ");
        String connectorType = scanner.nextLine();

        List<String> compatibleConsoleIds = readCompatibleConsoles();

        Cable cable = new Cable(id, title, price, quantity, compatibleConsoleIds, lengthInMeters, connectorType);
        accessoryService.registerCable(cable);
        System.out.println("Cable registrado correctamente.");
    }

    private void registerMemoryFlow() {
        System.out.print("ID del accesorio: ");
        String id = scanner.nextLine();
        System.out.print("Título: ");
        String title = scanner.nextLine();
        System.out.print("Precio: ");
        double price = Double.parseDouble(scanner.nextLine());
        System.out.print("Cantidad en stock: ");
        int quantity = Integer.parseInt(scanner.nextLine());
        System.out.print("Capacidad en GB: ");
        int capacityInGb = Integer.parseInt(scanner.nextLine());
        System.out.print("Tipo de memoria (SD, microSD, interna...): ");
        String memoryType = scanner.nextLine();

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
            String consoleId = scanner.nextLine();

            if (consoleId.isEmpty()) {
                adding = false;
            } else {
                compatibleConsoleIds.add(consoleId);
            }
        }
        return compatibleConsoleIds;
    }
}