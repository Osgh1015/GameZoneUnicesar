package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Client;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;

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
public class SalePersistence {

    private static final String SEPARATOR = ";";
    private static final String PRODUCTS_SEPARATOR = "\\|";

    private String filePath;
    private AccessoryService accessoryService;

    /**
     * Creates the persistence of sales.
     *
     * @param filePath path of the file where sales are stored
     */
    public SalePersistence(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Creates the persistence of sales that can also resolve the accessories
     * sold.
     *
     * @param filePath         path of the file where sales are stored
     * @param accessoryService service used to resolve the accessories sold
     * @deprecated use {@link #SalePersistence(String)}; kept only until
     *             {@code Main} stops passing a service (V-19)
     */
    @Deprecated
    public SalePersistence(String filePath, AccessoryService accessoryService) {
        this.filePath = filePath;
        this.accessoryService = accessoryService;
    }

    /**
     * Saves the full list of sales to the file, overwriting its content.
     *
     * @param sales list of sales to persist
     * @throws PersistenceException if the file cannot be written
     */
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
     * Loads every sale stored in the file, resolving client, seller and
     * product references through the services of the other modules.
     *
     * @param productService service used to resolve product references
     * @param personService  service used to resolve client/seller references
     * @return the list of sales reconstructed from the file, or an empty
     *         list if the file does not exist yet
     * @throws PersistenceException if the file cannot be read or one of its
     *                              lines is corrupted
     * @deprecated use {@link #loadAll()} and resolve the records in the
     *             service layer; kept only until the services stop calling
     *             it (V-19)
     */
    @Deprecated
    public List<Sale> loadAll(ProductService productService, PersonService personService) {
        List<Sale> sales = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return sales;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (!line.trim().isEmpty()) {
                    Sale sale = parseLine(line, lineNumber, productService, personService);
                    if (sale != null) {
                        sales.add(sale);
                    }
                }
            }
        } catch (IOException e) {
            throw new PersistenceException(
                    "Error de lectura en el archivo " + filePath + " al cargar las ventas.", e);
        }
        return sales;
    }

    /**
     * Builds the exception used when a line of the sales file is corrupted.
     *
     * @param lineNumber number of the corrupted line
     * @param cause      error found while reading the line
     * @return the exception to throw
     */
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

    /**
     * Converts a line of the file into a sale, reporting a corrupted line as
     * a persistence error.
     *
     * @param line           line of the file
     * @param lineNumber     number of the line, used in the error message
     * @param productService service used to resolve product references
     * @param personService  service used to resolve client/seller references
     * @return the sale, or {@code null} if its client or seller no longer exist
     * @throws PersistenceException if the line does not have the expected format
     */
    private Sale parseLine(String line, int lineNumber, ProductService productService,
                           PersonService personService) {
        try {
            return fromLine(line, productService, personService);
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

    private Sale fromLine(String line, ProductService productService, PersonService personService) {
        String[] parts = line.split(SEPARATOR, -1);
        if (parts.length < 5) {
            return null;
        }
        String id = parts[0];
        LocalDate date = LocalDate.parse(parts[1]);
        Client client = personService.findClientById(parts[2]);
        Seller seller = personService.findSellerById(parts[3]);
        if (client == null || seller == null) {
            return null;
        }
        Sale sale = new Sale(id, date, client, seller);
        if (!parts[4].isEmpty()) {
            for (String productId : parts[4].split(PRODUCTS_SEPARATOR)) {
                Product item = resolveItem(productId, productService);
                if (item != null) {
                    sale.addProduct(item);
                }
            }
        }
        if (parts.length >= 7) {
            if (!parts[5].isEmpty()) {
                sale.setAppliedPromotionName(parts[5]);
            }
            if (!parts[6].isEmpty()) {
                sale.setDiscountAmount(Double.parseDouble(parts[6]));
            }
        }
        if (parts.length >= 8 && !parts[7].isEmpty()) {
            sale.addWarrantyCost(Double.parseDouble(parts[7]));
        }
        return sale;
    }

    /**
     * Resolves an item id stored in a sale, which may belong either to the
     * product catalogue (video games, consoles) or to the accessory
     * catalogue (controllers, cables, memory units).
     *
     * @param itemId         id stored in the file
     * @param productService service used to resolve product references
     * @return the matching product or accessory, or {@code null} if neither exists
     */
    private Product resolveItem(String itemId, ProductService productService) {
        Product product = productService.findById(itemId);
        if (product != null) {
            return product;
        }
        return accessoryService.findById(itemId);
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
