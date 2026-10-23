package com.gamezone.persistence;

import com.gamezone.model.BulkPurchaseDiscount;
import com.gamezone.model.CategoryDiscount;
import com.gamezone.model.PercentageDiscount;
import com.gamezone.model.Promotion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the persistence of {@link Promotion} objects. The three concrete
 * promotion types share a single file, so every row starts with a
 * discriminator (PERCENTAGE, CATEGORY or BULK) that tells which type must
 * be rebuilt when loading.
 *
 * Line formats:
 * PERCENTAGE;id;name;start;end;percentage
 * CATEGORY;id;name;start;end;percentage;category
 * BULK;id;name;start;end;minQuantity;percentage
 */
public class PromotionRepository {

    private static final String DELIMITER = ";";
    private final String filePath;

    /**
     * Creates a repository that reads from and writes to the given file.
     *
     * @param filePath path of the file used to store the promotions
     */
    public PromotionRepository(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Saves the full list of promotions, overwriting any previous content.
     *
     * @param promotions promotions to persist
     */
    public void saveAll(List<Promotion> promotions) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Promotion promotion : promotions) {
                writer.println(toLine(promotion));
            }
        } catch (IOException e) {
            System.err.println("Error saving promotions: " + e.getMessage());
        }
    }

    /**
     * Loads every promotion stored in the file. If the file does not
     * exist, an empty list is returned.
     *
     * @return the promotions read from the file
     */
    public List<Promotion> loadAll() {
        List<Promotion> promotions = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return promotions;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                Promotion promotion = fromLine(line);
                if (promotion != null) {
                    promotions.add(promotion);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading promotions: " + e.getMessage());
        }
        return promotions;
    }

    private String toLine(Promotion promotion) {
        String common = String.join(DELIMITER,
                promotion.getId(),
                promotion.getName(),
                promotion.getStartDate().toString(),
                promotion.getEndDate().toString());

        if (promotion instanceof PercentageDiscount p) {
            return String.join(DELIMITER, "PERCENTAGE", common,
                    String.valueOf(p.getPercentage()));
        } else if (promotion instanceof CategoryDiscount c) {
            return String.join(DELIMITER, "CATEGORY", common,
                    String.valueOf(c.getPercentage()), c.getTargetCategory());
        } else if (promotion instanceof BulkPurchaseDiscount b) {
            return String.join(DELIMITER, "BULK", common,
                    String.valueOf(b.getMinQuantity()), String.valueOf(b.getPercentage()));
        }
        throw new IllegalArgumentException("Unsupported promotion type: " + promotion.getClass());
    }

    private Promotion fromLine(String line) {
        String[] parts = line.trim().split(DELIMITER, -1);
        if (parts.length < 6) {
            return null;
        }
        String type = parts[0];
        String id = parts[1];
        String name = parts[2];
        LocalDate start = LocalDate.parse(parts[3]);
        LocalDate end = LocalDate.parse(parts[4]);

        return switch (type) {
            case "PERCENTAGE" -> new PercentageDiscount(id, name, start, end,
                    Double.parseDouble(parts[5]));
            case "CATEGORY" -> new CategoryDiscount(id, name, start, end,
                    Double.parseDouble(parts[5]), parts[6]);
            case "BULK" -> new BulkPurchaseDiscount(id, name, start, end,
                    Integer.parseInt(parts[5]), Double.parseDouble(parts[6]));
            default -> null;
        };
    }
}