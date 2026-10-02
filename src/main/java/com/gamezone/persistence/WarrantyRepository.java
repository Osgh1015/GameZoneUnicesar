package com.gamezone.persistence;

import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Warranty;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Handles warranty persistence.
 */
public class WarrantyRepository {

    private static final String DELIMITER = ";";
    private static final String BASIC_TYPE = "BASIC";
    private static final String EXTENDED_TYPE = "EXTENDED";

    private final String filePath;

    public WarrantyRepository(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Saves all warranties to the configured file.
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
}