package com.gamezone.persistence;

import com.gamezone.model.Accessory;
import com.gamezone.model.Cable;
import com.gamezone.model.Controller;
import com.gamezone.model.Memory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles the persistence of {@link Accessory} objects. All three concrete
 * accessory types share a single file, so every row starts with a
 * discriminator that tells which type must be rebuilt when loading.
 */
public class AccessoryRepository {

    private static final String DELIMITER = ";";
    private static final String LIST_SEPARATOR = "\\|";
    private static final String LIST_JOINER = "|";
    private final String filePath;

    /**
     * Creates a repository that reads from and writes to the given file.
     *
     * @param filePath path of the file used to store the accessories
     */
    public AccessoryRepository(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Saves the full list of accessories to the file, overwriting any
     * previous content.
     *
     * @param accessories list of accessories to persist
     */
    public void saveAll(List<Accessory> accessories) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Accessory accessory : accessories) {
                writer.println(toLine(accessory));
            }
        } catch (IOException e) {
            System.err.println("Error saving accessories: " + e.getMessage());
        }
    }

    /**
     * Loads the list of accessories previously stored in the file. If the
     * file does not exist yet, an empty list is returned.
     *
     * @return the list of accessories read from the file
     */
    public List<Accessory> loadAll() {
        List<Accessory> accessories = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return accessories;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                Accessory accessory = fromLine(line);
                if (accessory != null) {
                    accessories.add(accessory);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading accessories: " + e.getMessage());
        }
        return accessories;
    }

    /**
     * Converts a single accessory into its text representation, starting
     * with the discriminator of its concrete type.
     *
     * @param accessory accessory to convert
     * @return delimited line representing the accessory
     */
    private String toLine(Accessory accessory) {
        String common = String.join(DELIMITER,
                accessory.getId(),
                accessory.getTitle(),
                String.valueOf(accessory.getPrice()),
                String.valueOf(accessory.getQuantity()),
                String.join(LIST_JOINER, accessory.getCompatibleConsoleIds()));

        if (accessory instanceof Controller controller) {
            return String.join(DELIMITER, "CONTROLLER", common, controller.getConnectionType());
        } else if (accessory instanceof Cable cable) {
            return String.join(DELIMITER, "CABLE", common,
                    String.valueOf(cable.getLengthInMeters()),
                    cable.getConnectorType());
        } else if (accessory instanceof Memory memory) {
            return String.join(DELIMITER, "MEMORY", common,
                    String.valueOf(memory.getCapacityInGb()),
                    memory.getMemoryType());
        }
        throw new IllegalArgumentException("Unsupported accessory type: " + accessory.getClass());
    }

    /**
     * Rebuilds an accessory from its text representation, using the
     * discriminator in the first field to choose the concrete type.
     *
     * @param line delimited line read from the file
     * @return the reconstructed accessory, or {@code null} if the type is unknown
     */
    private Accessory fromLine(String line) {
        String[] parts = line.split(DELIMITER, -1);
        String type = parts[0];
        String id = parts[1];
        String title = parts[2];
        double price = Double.parseDouble(parts[3]);
        int quantity = Integer.parseInt(parts[4]);
        List<String> compatibleConsoleIds = parseCompatibility(parts[5]);

        return switch (type) {
            case "CONTROLLER" -> new Controller(id, title, price, quantity, compatibleConsoleIds, parts[6]);
            case "CABLE" -> new Cable(id, title, price, quantity, compatibleConsoleIds,
                    Double.parseDouble(parts[6]), parts[7]);
            case "MEMORY" -> new Memory(id, title, price, quantity, compatibleConsoleIds,
                    Integer.parseInt(parts[6]), parts[7]);
            default -> null;
        };
    }

    /**
     * Parses the compatibility field into a mutable list of console ids.
     *
     * @param field raw compatibility field read from the file
     * @return the list of console ids, empty when the field is blank
     */
    private List<String> parseCompatibility(String field) {
        if (field == null || field.isBlank()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(field.split(LIST_SEPARATOR)));
    }
}
