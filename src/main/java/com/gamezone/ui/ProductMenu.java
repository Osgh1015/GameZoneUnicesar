package com.gamezone.ui;

import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;
import com.gamezone.service.ProductService;

import java.util.List;

/**
 * Submenu of the products module: registers video games and consoles and
 * lists the products.
 * <p>
 * It only reads the user's input, calls the services and prints the
 * results; it never accesses persistence. It is created by
 * {@link ConsoleMenu}, which delegates to it the option of the main menu.
 */
public class ProductMenu {

    private final ConsoleInput userInput;
    private final ProductService productService;

    /**
     * Creates the submenu.
     *
     * @param userInput shared reader of the console input
     * @param productService ProductService used by this submenu
     */
    public ProductMenu(
            ConsoleInput userInput,
            ProductService productService) {
        this.userInput = userInput;
        this.productService = productService;
    }

    /**
     * Shows the products submenu and executes the option chosen by the user.
     */
    public void show() {
        System.out.println("\n-- Menú de productos --");
        System.out.println("1. Registrar videojuego");
        System.out.println("2. Registrar consola");
        System.out.println("3. Listar productos");
        System.out.println("0. Volver");
        System.out.print("Seleccione una opción: ");

        String option = userInput.nextLine();

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
        String id = userInput.nextLine();
        System.out.print("Título: ");
        String title = userInput.nextLine();
        System.out.print("Precio: ");
        double price = userInput.readDouble();
        System.out.print("Cantidad en stock: ");
        int quantity = userInput.readInt();
        System.out.print("Plataforma: ");
        String platform = userInput.nextLine();
        System.out.print("Género: ");
        String genre = userInput.nextLine();
        System.out.print("Clasificación de edad: ");
        String ageRating = userInput.nextLine();

        VideoGame videoGame = new VideoGame(id, title, price, quantity, platform, genre, ageRating);
        productService.registerVideoGame(videoGame);
        System.out.println("Videojuego registrado correctamente.");
    }

    private void registerConsoleFlow() {
        System.out.print("ID del producto: ");
        String id = userInput.nextLine();
        System.out.print("Título: ");
        String title = userInput.nextLine();
        System.out.print("Precio: ");
        double price = userInput.readDouble();
        System.out.print("Cantidad en stock: ");
        int quantity = userInput.readInt();
        System.out.print("Marca: ");
        String brand = userInput.nextLine();
        System.out.print("Modelo: ");
        String model = userInput.nextLine();
        System.out.print("Generación: ");
        int generation = userInput.readInt();

        Console console = new Console(id, title, price, quantity, brand, model, generation);
        productService.registerConsole(console);
        System.out.println("Consola registrada correctamente.");
    }
}
