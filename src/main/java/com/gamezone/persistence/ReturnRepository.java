package com.gamezone.persistence;

import com.gamezone.model.*;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Handles saving and loading Return records to and from a plain text file.
 * Each line represents one return in CSV format:
 * id;date;saleId;productId1|productId2|...;reason;refundAmount;warrantyRefund
 * The last field (refund of the cancelled extended warranties) is optional
 * when loading, so files written before it existed still load correctly.
 *
 * This class contains no business rules, only file reading/writing,
 * respecting the layer separation required by the workshop. When loading
 * it returns only the stored identifiers ({@link ReturnRecord}); the
 * return service resolves them into sales and products, so the
 * persistence layer does not depend on any service.
 */
public class ReturnRepository {

    private static final String SEPARATOR = ";";
    private static final String PRODUCTS_SEPARATOR = "\\|";
    private final String filePath;

    /**
     * Creates the repository using the file where returns are stored.
     *
     * @param filePath path of the returns file
     */
    public ReturnRepository(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Creates the repository ignoring the services it used to receive.
     *
     * @param filePath         path of the returns file
     * @param saleService      no longer used
     * @param productService   no longer used
     * @param accessoryService no longer used
     * @deprecated use {@link #ReturnRepository(String)}; kept only until
     *             {@code Main} stops passing services (V-10)
     */
    @Deprecated
    public ReturnRepository(String filePath, SaleService saleService,
                            ProductService productService, AccessoryService accessoryService) {
        this(filePath);
    }

    public void saveAll(List<Return> returns) {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (Return r : returns) {
                writer.write(toLine(r));
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            System.out.println("Error al guardar las devoluciones: " + e.getMessage());
        }
    }

    public List<ReturnRecord> loadAll() {
        List<ReturnRecord> returns = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return returns;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    ReturnRecord r = fromLine(line);
                    if (r != null) {
                        returns.add(r);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("No se encontró un archivo de devoluciones anterior. Se inicia vacío.");
        }
        return returns;
    }

    private String toLine(Return r) {
        StringBuilder productIds = new StringBuilder();
        List<Product> products = r.getReturnedProducts();
        for (int i = 0; i < products.size(); i++) {
            productIds.append(products.get(i).getId());
            if (i < products.size() - 1) {
                productIds.append("|");
            }
        }
        return r.getId() + SEPARATOR
                + r.getDate() + SEPARATOR
                + r.getOriginalSale().getId() + SEPARATOR
                + productIds + SEPARATOR
                + r.getReason() + SEPARATOR
                + r.getRefundAmount() + SEPARATOR
                + r.getWarrantyRefund();
    }

    private ReturnRecord fromLine(String line) {
        String[] parts = line.split(SEPARATOR, -1);
        if (parts.length < 6) {
            return null;
        }
        String id = parts[0];
        LocalDate date = LocalDate.parse(parts[1]);
        List<String> productIds = new ArrayList<>();
        if (!parts[3].isEmpty()) {
            productIds.addAll(Arrays.asList(parts[3].split(PRODUCTS_SEPARATOR)));
        }
        String reason = parts[4];
        Double warrantyRefund = null;
        if (parts.length >= 7 && !parts[6].isEmpty()) {
            warrantyRefund = Double.parseDouble(parts[6]);
        }
        return new ReturnRecord(id, date, parts[2], productIds, reason, warrantyRefund);
    }

    /**
     * Raw persistence representation of a return: only identifiers and
     * primitive values, resolved later by the return service.
     *
     * @param id             return identifier
     * @param date           date of the return
     * @param saleId         identifier of the original sale
     * @param productIds     identifiers of the returned items
     * @param reason         reason for the return
     * @param warrantyRefund refunded warranty cost, or {@code null} when the
     *                       line was written before that field existed
     */
    public record ReturnRecord(
            String id,
            LocalDate date,
            String saleId,
            List<String> productIds,
            String reason,
            Double warrantyRefund) {
    }
}