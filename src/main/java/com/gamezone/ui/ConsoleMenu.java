package com.gamezone.ui;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.CategoryDiscount;
import com.gamezone.model.Client;
import com.gamezone.model.Console;
import com.gamezone.model.Controller;
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

    private Scanner scanner = new Scanner(System.in);
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
            System.out.println("5. Gestión de devoluciones");
            System.out.println("6. Gestión de promociones");
            System.out.println("7. Gestión de garantías");
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

                case "5":
                    returnsMenu();
                    break;

                case "6":
                    promotionsMenu();
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
        System.out.println("5. Ver detalle de una venta (recibo)");
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

            case "5":
                System.out.print("ID de la venta: ");
                Sale detail = saleService.findById(scanner.nextLine());
                if (detail == null) {
                    System.out.println("Venta no encontrada.");
                } else {
                    System.out.println(detail.generateReceipt());
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

        printSellableItems();

        List<String> productIds = new ArrayList<>();
        List<String> productIdsWithExtendedWarranty = new ArrayList<>();

        boolean addingProducts = true;

        while (addingProducts) {
            System.out.print("ID del producto o accesorio (deje vacío para finalizar): ");
            String productId = scanner.nextLine();

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

        if (sale != null) {
            System.out.println("Venta registrada correctamente.");
            System.out.println(sale.generateReceipt());
            showWarrantiesOfSale(sale);
        } else {
            System.out.println("No se pudo registrar la venta.");
        }
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
                String type = product instanceof Console ? "Consola - admite garantía extendida" : "Videojuego";
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

        if (!(product instanceof Console)) {
            return;
        }

        System.out.printf("La consola \"%s\" incluye garantía básica de 6 meses sin costo.%n",
                product.getTitle());
        System.out.printf("¿Desea agregar garantía extendida de 12 meses por $%.2f? (s/n): ",
                product.getPrice() * 0.10);

        String answer = scanner.nextLine().trim();

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
            if (warranty.getSale().getId().equals(sale.getId())) {
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

    private void returnsMenu() {
        System.out.println("\n-- Gestión de devoluciones --");
        System.out.println("1. Registrar una devolución");
        System.out.println("2. Consultar todas las devoluciones");
        System.out.println("3. Consultar devoluciones por cliente");
        System.out.println("4. Consultar devoluciones por venta");
        System.out.println("5. Consultar balance mensual");
        System.out.println("0. Volver");
        System.out.print("Seleccione una opción: ");

        String option = scanner.nextLine();

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
                String customerId = scanner.nextLine();
                for (Return r : returnService.viewReturnsByCustomer(customerId)) {
                    System.out.println(r.generateReturnReceipt());
                }
                break;

            case "4":
                System.out.print("ID de la venta: ");
                String saleId = scanner.nextLine();
                for (Return r : returnService.viewReturnsBySale(saleId)) {
                    System.out.println(r.generateReturnReceipt());
                }
                break;

            case "5":
                System.out.print("Mes (1-12): ");
                int month = Integer.parseInt(scanner.nextLine());
                System.out.print("Año: ");
                int year = Integer.parseInt(scanner.nextLine());
                double balance = returnService.generateMonthlyBalance(month, year);
                System.out.println("Balance neto del período: $" + balance);
                break;

            case "0":
                break;

            default:
                System.out.println("Opción inválida.");
        }
    }

    private void registerReturnFlow() {
        System.out.print("ID de la venta original: ");
        String saleId = scanner.nextLine();

        List<String> productIds = new ArrayList<>();
        boolean addingProducts = true;
        while (addingProducts) {
            System.out.print("ID de producto a devolver (deje vacío para finalizar): ");
            String productId = scanner.nextLine();
            if (productId.isEmpty()) {
                addingProducts = false;
            } else {
                productIds.add(productId);
            }
        }

        System.out.print("Motivo de la devolución: ");
        String reason = scanner.nextLine();

        try {
            Return r = returnService.registerReturn(saleId, productIds, reason);
            System.out.println(r.generateReturnReceipt());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void promotionsMenu() {
        System.out.println("\n-- Gestión de promociones --");
        System.out.println("1. Registrar promoción por porcentaje");
        System.out.println("2. Registrar promoción por categoría");
        System.out.println("3. Registrar promoción por volumen de compra");
        System.out.println("4. Listar todas las promociones");
        System.out.println("5. Listar promociones vigentes");
        System.out.println("0. Volver");
        System.out.print("Seleccione una opción: ");

        String option = scanner.nextLine();

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
            String id = scanner.nextLine();
            System.out.print("Nombre: ");
            String name = scanner.nextLine();
            LocalDate start = readDate("Fecha de inicio (AAAA-MM-DD): ");
            LocalDate end = readDate("Fecha de fin (AAAA-MM-DD): ");
            System.out.print("Porcentaje de descuento (0-100): ");
            double percentage = Double.parseDouble(scanner.nextLine());

            promotionService.registerPercentageDiscount(id, name, start, end, percentage);
            System.out.println("Promoción registrada correctamente.");
        } catch (IllegalArgumentException | DateTimeParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void registerCategoryPromotionFlow() {
        try {
            System.out.print("ID de la promoción: ");
            String id = scanner.nextLine();
            System.out.print("Nombre: ");
            String name = scanner.nextLine();
            LocalDate start = readDate("Fecha de inicio (AAAA-MM-DD): ");
            LocalDate end = readDate("Fecha de fin (AAAA-MM-DD): ");
            System.out.print("Porcentaje de descuento (0-100): ");
            double percentage = Double.parseDouble(scanner.nextLine());
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
        String option = scanner.nextLine().trim();

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
            String id = scanner.nextLine();
            System.out.print("Nombre: ");
            String name = scanner.nextLine();
            LocalDate start = readDate("Fecha de inicio (AAAA-MM-DD): ");
            LocalDate end = readDate("Fecha de fin (AAAA-MM-DD): ");
            System.out.print("Cantidad mínima de productos: ");
            int minQuantity = Integer.parseInt(scanner.nextLine());
            System.out.print("Porcentaje de descuento (0-100): ");
            double percentage = Double.parseDouble(scanner.nextLine());

            promotionService.registerBulkPurchaseDiscount(id, name, start, end, minQuantity, percentage);
            System.out.println("Promoción registrada correctamente.");
        } catch (IllegalArgumentException | DateTimeParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private LocalDate readDate(String prompt) {
        System.out.print(prompt);
        return LocalDate.parse(scanner.nextLine().trim());
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

        String option = scanner.nextLine();

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
        String saleId = scanner.nextLine();

        System.out.print("ID del producto: ");
        String productId = scanner.nextLine();

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
        String input = scanner.nextLine();

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
