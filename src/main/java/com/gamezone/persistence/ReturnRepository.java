package com.gamezone.persistence;

import com.gamezone.model.*;
import com.gamezone.service.SaleService;
import com.gamezone.service.ProductService;
import com.gamezone.service.AccessoryService;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Handles saving and loading Return records to and from a plain text file.
 * Each line represents one return in CSV format:
 * id;date;saleId;productId1|productId2|...;reason;refundAmount
 *
 * This class contains no business rules, only file reading/writing,
 * respecting the layer separation required by the workshop.
 */
public class ReturnRepository {

    private static final String SEPARATOR = ";";
    private static final String PRODUCTS_SEPARATOR = "\\|";
    private final String filePath;
    private final SaleService saleService;
    private final ProductService productService;
    private final AccessoryService accessoryService;

    public ReturnRepository(String filePath, SaleService saleService,
                            ProductService productService, AccessoryService accessoryService) {
        this.filePath = filePath;
        this.saleService = saleService;
        this.productService = productService;
        this.accessoryService = accessoryService;
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

    public List<Return> loadAll() {
        List<Return> returns = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return returns;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Return r = fromLine(line);
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
                + r.getRefundAmount();
    }

    private Return fromLine(String line) {
        String[] parts = line.split(SEPARATOR, -1);
        if (parts.length < 6) {
            return null;
        }
        String id = parts[0];
        LocalDate date = LocalDate.parse(parts[1]);
        Sale originalSale = saleService.findById(parts[2]);
        if (originalSale == null) {
            return null;
        }
        List<Product> returnedProducts = new ArrayList<>();
        if (!parts[3].isEmpty()) {
            for (String productId : parts[3].split(PRODUCTS_SEPARATOR)) {
                Product product = resolveProduct(productId);
                if (product != null) {
                    returnedProducts.add(product);
                }
            }
        }
        String reason = parts[4];

        Return r = new Return(id, date, originalSale, returnedProducts, reason);
        r.calculateRefundAmount();
        return r;
    }

    private Product resolveProduct(String productId) {
        Product product = productService.findById(productId);
        if (product != null) {
            return product;
        }
        return accessoryService.findById(productId);
    }
}