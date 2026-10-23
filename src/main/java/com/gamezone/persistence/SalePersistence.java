package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles saving and loading Sale records to and from a plain text file.
 * Each line represents one sale in CSV format:
 * id;date;clientId;sellerId;itemId1|itemId2|...;appliedPromotionName;discountAmount;warrantyCost
 * The last three fields are optional when loading, so files written before
 * the promotions and warranty modules still load correctly. Item ids may
 * belong to the product catalogue or to the accessory catalogue.
 * <p>
 * When loading, this class returns only the stored references
 * ({@link SaleRecord}); the service layer resolves them into clients,
 * sellers and items, so the persistence layer does not depend on any
 * service.
 *
 * Responsabilidad: Líder Técnico - Módulo de Ventas.
 * Nota: esta clase NO contiene reglas de negocio, solo lectura/escritura
 * de archivos, respetando la separación de capas exigida por el taller.
 */
public class SalePersistence implements SaleStore {

    private static final String SEPARATOR = ";";
    private static final String PRODUCTS_SEPARATOR = "\\|";

    private final String filePath;

    /**
     * Creates the persistence of sales.
     *
     * @param filePath path of the file where sales are stored
     */
    public SalePersistence(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Saves the full list of sales to the file, overwriting its content.
     *
     * @param sales list of sales to persist
     * @throws PersistenceException if the file cannot be written
     */
    @Override
    public void saveAll(List<Sale> sales) {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (Sale sale : sales) {
                writer.write(toLine(sale));
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error de escritura en el archivo " + filePath + " al guardar las ventas.", e);
        }
    }

    /**
     * Loads the references of every sale stored in the file.
     *
     * @return the sale records read from the file, or an empty list if the
     *         file does not exist yet
     * @throws PersistenceException if the file cannot be read or the date of
     *                              one of its lines is corrupted
     */
    @Override
    public List<SaleRecord> loadAll() {
        List<SaleRecord> records = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return records;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (!line.trim().isEmpty()) {
                    SaleRecord record = parseRecord(line, lineNumber);
                    if (record != null) {
                        records.add(record);
                    }
                }
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error de lectura en el archivo " + filePath + " al cargar las ventas.", e);
        }
        return records;
    }

    /**
     * Builds the exception used when a line of the sales file is corrupted.
     *
     * @param lineNumber number of the corrupted line
     * @param cause      error found while reading the line
     * @return the exception to throw
     */
    @Override
    public PersistenceException damagedLine(int lineNumber, RuntimeException cause) {
        return new PersistenceException("El archivo " + filePath + " está dañado en la línea "
                + lineNumber + " (lectura de ventas).", cause);
    }

    /**
     * Converts a line of the file into a sale record, reporting a corrupted
     * line as a persistence error.
     *
     * @param line       line of the file
     * @param lineNumber number of the line
     * @return the record, or {@code null} if the line has too few fields
     * @throws PersistenceException if the line does not have the expected format
     */
    private SaleRecord parseRecord(String line, int lineNumber) {
        try {
            String[] parts = line.split(SEPARATOR, -1);
            if (parts.length < 5) {
                return null;
            }
            LocalDate date = LocalDate.parse(parts[1]);
            List<String> itemIds = new ArrayList<>();
            if (!parts[4].isEmpty()) {
                itemIds.addAll(Arrays.asList(parts[4].split(PRODUCTS_SEPARATOR)));
            }
            String promotionName = null;
            String discountAmount = null;
            if (parts.length >= 7) {
                promotionName = parts[5].isEmpty() ? null : parts[5];
                discountAmount = parts[6].isEmpty() ? null : parts[6];
            }
            String warrantyCost = null;
            if (parts.length >= 8 && !parts[7].isEmpty()) {
                warrantyCost = parts[7];
            }
            return new SaleRecord(parts[0], date, parts[2], parts[3], itemIds,
                    promotionName, discountAmount, warrantyCost, lineNumber);
        } catch (RuntimeException e) {
            throw damagedLine(lineNumber, e);
        }
    }

    private String toLine(Sale sale) {
        StringBuilder productIds = new StringBuilder();
        List<Product> products = sale.getProducts();
        for (int i = 0; i < products.size(); i++) {
            productIds.append(products.get(i).getId());
            if (i < products.size() - 1) {
                productIds.append("|");
            }
        }
        return sale.getId() + SEPARATOR
                + sale.getDate() + SEPARATOR
                + sale.getClient().getId() + SEPARATOR
                + sale.getSeller().getId() + SEPARATOR
                + productIds + SEPARATOR
                + (sale.getAppliedPromotionName() == null ? "" : sale.getAppliedPromotionName()) + SEPARATOR
                + sale.getDiscountAmount() + SEPARATOR
                + sale.getWarrantyCost();
    }

    /**
     * Stored references of a sale: identifiers and the amounts exactly as
     * they were written, resolved later by the service layer.
     *
     * @param id                   sale identifier
     * @param date                 date of the sale
     * @param clientId             identifier of the client
     * @param sellerId             identifier of the seller
     * @param itemIds              identifiers of the items sold
     * @param appliedPromotionName name of the applied promotion, or {@code null}
     * @param discountAmount       stored discount, or {@code null} when absent
     * @param warrantyCost         stored extended warranty cost, or {@code null}
     *                             when absent
     * @param lineNumber           number of the line in the file, used to
     *                             report a corrupted amount
     */
    public record SaleRecord(
            String id,
            LocalDate date,
            String clientId,
            String sellerId,
            List<String> itemIds,
            String appliedPromotionName,
            String discountAmount,
            String warrantyCost,
            int lineNumber) {
    }
}
