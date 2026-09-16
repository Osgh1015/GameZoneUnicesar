package com.gamezone.persistence;

import com.gamezone.model.Client;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading Sale records to and from a plain text file.
 * Each line represents one sale in CSV format:
 * id;date;clientId;sellerId;productId1|productId2|...;warrantyCost
 *
 * The last field stores the cost of the extended warranties requested
 * during the sale. Lines written before the warranty module existed
 * have only five fields and are still read correctly, assuming a
 * warranty cost of zero.
 *
 * Responsabilidad: Líder Técnico - Módulo de Ventas.
 * Nota: esta clase NO contiene reglas de negocio, solo lectura/escritura
 * de archivos, respetando la separación de capas exigida por el taller.
 */
public class SalePersistence {

    private static final String SEPARATOR = ";";
    private static final String PRODUCTS_SEPARATOR = "\\|";

    private String filePath;

    public SalePersistence(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Saves the full list of sales to the file, overwriting its content.
     * @param sales list of sales to persist
     */
    public void saveAll(List<Sale> sales) {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (Sale sale : sales) {
                writer.write(toLine(sale));
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            System.out.println("Error al guardar las ventas: " + e.getMessage());
        }
    }

    /**
     * Loads every sale stored in the file, resolving client, seller and
     * product references through the services of the other modules.
     *
     * @param productService service used to resolve product references
     * @param personService  service used to resolve client/seller references
     * @return the list of sales reconstructed from the file
     */
    public List<Sale> loadAll(ProductService productService, PersonService personService) {
        List<Sale> sales = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Sale sale = fromLine(line, productService, personService);
                    if (sale != null) {
                        sales.add(sale);
                    }
                }
            }
        } catch (IOException e) {
            // If the file does not exist yet, we simply start with no sales.
            System.out.println("“No se encontró un archivo de ventas anterior. Se inicia vacío.”");
        }
        return sales;
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
                Product product = productService.findById(productId);
                if (product != null) {
                    sale.addProduct(product);
                }
            }
        }
        if (parts.length >= 6 && !parts[5].isEmpty()) {
            sale.addWarrantyCost(Double.parseDouble(parts[5]));
        }
        return sale;
    }
}