package com.gamezone.persistence;

import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Warranty;

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
 * Handles saving and loading warranty persistence data to and from
 * data/warranties.csv.
 *
 * Each line stores only primitive warranty information and identifiers:
 *
 * <pre>
 * type;id;productId;saleId;startDate
 * </pre>
 *
 * Object references are resolved by WarrantyService, keeping the
 * persistence layer independent from the service layer.
 *
 * Responsabilidad: Desarrollador 2 - Persistencia de garantias.
 */
public class WarrantyRepository {

    private static final String DELIMITER = ";";
    private static final String BASIC_TYPE = "BASIC";
    private static final String EXTENDED_TYPE = "EXTENDED";

    private final String filePath;

    /**
     * Creates the repository using the file where warranties are stored.
     *
     * @param filePath path of the warranty persistence file
     */
    public WarrantyRepository(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Saves the full list of warranties to the file, overwriting its
     * previous content.
     *
     * @param warranties warranties to persist
     */
    public void saveAll(List<Warranty> warranties) {

        try (PrintWriter writer =
                     new PrintWriter(new FileWriter(filePath))) {

            for (Warranty warranty : warranties) {
                writer.println(toLine(warranty));
            }

        } catch (IOException e) {
            System.out.println(
                    "Error al guardar las garantías: "
                            + e.getMessage()
            );
        }
    }

    /**
     * Loads the warranty records stored in the file.
     *
     * @return stored warranty records
     */
    public List<WarrantyRecord> loadAll() {

        List<WarrantyRecord> records =
                new ArrayList<>();

        File file = new File(filePath);

        if (!file.exists()) {
            return records;
        }

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }

                WarrantyRecord record =
                        fromLine(line);

                if (record != null) {
                    records.add(record);
                }
            }

        } catch (IOException e) {
            System.out.println(
                    "Error al cargar las garantías: "
                            + e.getMessage()
            );
        }

        return records;
    }

    /**
     * Converts a warranty object into its persisted text representation.
     *
     * @param warranty warranty to serialize
     * @return line to be written to the persistence file
     */
    private String toLine(Warranty warranty) {

        String type =
                warranty instanceof ExtendedWarranty
                        ? EXTENDED_TYPE
                        : BASIC_TYPE;

        return String.join(
                DELIMITER,
                type,
                warranty.getId(),
                warranty.getProduct().getId(),
                warranty.getSale().getId(),
                warranty.getStartDate().toString()
        );
    }

    /**
     * Converts a stored line into a warranty persistence record.
     *
     * @param line line read from the persistence file
     * @return warranty record or null when the line is invalid
     */
    private WarrantyRecord fromLine(String line) {

        String[] parts =
                line.split(DELIMITER, -1);

        if (parts.length < 5) {
            return null;
        }

        try {
            return new WarrantyRecord(
                    parts[0],
                    parts[1],
                    parts[2],
                    parts[3],
                    LocalDate.parse(parts[4])
            );

        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Raw persistence representation of a warranty.
     *
     * @param type warranty type
     * @param id warranty identifier
     * @param productId product identifier
     * @param saleId sale identifier
     * @param startDate warranty start date
     */
    public record WarrantyRecord(
            String type,
            String id,
            String productId,
            String saleId,
            LocalDate startDate) {
    }
}