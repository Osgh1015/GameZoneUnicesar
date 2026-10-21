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
import java.util.List;

/**
 * Handles saving and loading Sale records to and from a plain text file.
 * Each line represents one sale in CSV format:
 * id;date;clientId;sellerId;itemId1|itemId2|...;appliedPromotionName;discountAmount;warrantyCost
 * The last three fields are optional when loading, so files written before
 * the promotions and warranty modules still load correctly. Item ids may
 * belong to the product catalogue or to the accessory catalogue.
 *
 * Responsabilidad: Líder Técnico - Módulo de Ventas.
 * Nota: esta clase NO contiene reglas de negocio, solo lectura/escritura
 * de archivos, respetando la separación de capas exigida por el taller.
 */
public class SalePersistence {

    private static final String SEPARATOR = ";";
    private static final String PRODUCTS_SEPARATOR = "\\|";

    private String filePath;
    private final AccessoryService accessoryService;

    /**
     * Creates the persistence of sales.
     *
     * @param filePath         path of the file where sales are stored
     * @param accessoryService service used to resolve the accessories sold,
     *                         since a sale may include items of the
     *                         accessory catalogue
     */
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
     * Loads every sale stored in the file, resolving client, seller and
     * product references through the services of the other modules.
     *
     * @param productService service used to resolve product references
     * @param personService  service used to resolve client/seller references
     * @return the list of sales reconstructed from the file, or an empty
     *         list if the file does not exist yet
     * @throws PersistenceException if the file cannot be read or one of its
     *                              lines is corrupted
     */
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
            throw new PersistenceException("El archivo " + filePath + " está dañado en la línea "
                    + lineNumber + " (lectura de ventas).", e);
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
}