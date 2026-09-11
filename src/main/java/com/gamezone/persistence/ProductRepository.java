package com.gamezone.persistence;

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
 */
public class ProductRepository {

    private static final String DELIMITER = ";";
    private final String filePath;

    public ProductRepository(String filePath) {
        this.filePath = filePath;
    }

    public void save(List<Product> products) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Product product : products) {
                writer.println(toLine(product));
            }
        } catch (IOException e) {
            System.err.println("Error saving products: " + e.getMessage());
        }
    }

    public List<Product> load() {
        List<Product> products = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return products;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                Product product = fromLine(line);
                if (product != null) products.add(product);
            }
        } catch (IOException e) {
            System.err.println("Error loading products: " + e.getMessage());
        }
        return products;
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