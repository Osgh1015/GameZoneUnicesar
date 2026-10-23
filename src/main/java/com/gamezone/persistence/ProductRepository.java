package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the persistence of Product objects for the products module.
 * <p>
 * Any problem reading or writing the file is reported as a
 * {@link PersistenceException} that names the file and the operation, so
 * the upper layers never receive a raw {@link IOException}.
 */
public class ProductRepository {

    private static final String DELIMITER = ";";
    private final String filePath;

    /**
     * Creates the repository that stores the products in
     * {@code data/products.txt}.
     */
    public ProductRepository() {
        this.filePath = "data/products.txt";
    }

    /**
     * Saves the full list of products to the file, overwriting its content.
     *
     * @param products products to persist
     * @throws PersistenceException if the file cannot be written
     */
    public void save(List<Product> products) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Product product : products) {
                writer.println(toLine(product));
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error de escritura en el archivo " + filePath + " al guardar los productos.", e);
        }
    }

    /**
     * Loads every product stored in the file.
     *
     * @return the products found, or an empty list if the file does not exist yet
     * @throws PersistenceException if the file cannot be read or one of its
     *                              lines is corrupted
     */
    public List<Product> load() {
        List<Product> products = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return products;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                Product product = parseLine(line, lineNumber);
                if (product != null) {
                    products.add(product);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error de lectura en el archivo " + filePath + " al cargar los productos.", e);
        }
        return products;
    }

    /**
     * Converts a line of the file into a product, reporting a corrupted
     * line as a persistence error.
     *
     * @param line       line of the file
     * @param lineNumber number of the line, used in the error message
     * @return the product, or {@code null} if the type is unknown
     * @throws PersistenceException if the line does not have the expected format
     */
    private Product parseLine(String line, int lineNumber) {
        try {
            return fromLine(line);
        } catch (RuntimeException e) {
            throw new PersistenceException("El archivo " + filePath + " está dañado en la línea "
                    + lineNumber + " (lectura de productos).", e);
        }
    }

    private String toLine(Product product) {
        if (product instanceof VideoGame videoGame) {
            return String.join(DELIMITER, "VIDEOGAME", videoGame.getId(), videoGame.getTitle(),
                    String.valueOf(videoGame.getPrice()), String.valueOf(videoGame.getQuantity()),
                    videoGame.getPlatform(), videoGame.getGenre(), videoGame.getAgeRating());
        } else if (product instanceof Console console) {
            return String.join(DELIMITER, "CONSOLE", console.getId(), console.getTitle(),
                    String.valueOf(console.getPrice()), String.valueOf(console.getQuantity()),
                    console.getBrand(), console.getModel(), String.valueOf(console.getGeneration()));
        }
        throw new IllegalArgumentException("Unsupported product type: " + product.getClass());
    }

    private Product fromLine(String line) {
        String[] parts = line.split(DELIMITER);
        String type = parts[0];
        String id = parts[1];
        String title = parts[2];
        double price = Double.parseDouble(parts[3]);
        int quantity = Integer.parseInt(parts[4]);
        return switch (type) {
            case "VIDEOGAME" -> new VideoGame(id, title, price, quantity, parts[5], parts[6], parts[7]);
            case "CONSOLE" -> new Console(id, title, price, quantity, parts[5], parts[6], Integer.parseInt(parts[7]));
            default -> null;
        };
    }
}