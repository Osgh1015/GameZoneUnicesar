package com.gamezone.persistence;

import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
}