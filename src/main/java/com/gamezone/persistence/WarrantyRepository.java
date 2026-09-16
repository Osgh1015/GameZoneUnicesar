package com.gamezone.persistence;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;

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
 * Handles saving and loading Warranty objects to and from the file
 * data/warranties.csv. Each line stores one warranty using a type
 * discriminator as the first field:
 *
 * <pre>
 * type;id;productId;saleId;startDate
 * </pre>
 *
 * The end date is never persisted: it is recalculated by the concrete
 * warranty class when the object is rebuilt, so the stored data can
 * never contradict the business rules of each warranty type.
 *
 * This class contains no business rules, only reading and writing of
 * files, respecting the separation of layers required by the workshop.
 *
 * Responsabilidad: Desarrollador 2 - Persistencia de garantias.
 */
public class WarrantyRepository {

    private static final String DELIMITER = ";";
    private static final String BASIC_TYPE = "BASIC";
    private static final String EXTENDED_TYPE = "EXTENDED";

    private final String filePath;
    private final ProductService productService;
    private final AccessoryService accessoryService;
    private final SalePersistence salePersistence;
    private final PersonService personService;

    /**
     * Creates the repository and injects the dependencies needed to
     * resolve the Product and Sale references stored as identifiers in
     * the file.
     *
     * @param filePath         path of the file where warranties are stored
     * @param productService   service used to resolve product references
     * @param accessoryService service used to resolve accessory references
     * @param salePersistence  persistence used to rebuild the sales
     * @param personService    service used to resolve client and seller references
     */
    public WarrantyRepository(String filePath,
                              ProductService productService,
                              AccessoryService accessoryService,
                              SalePersistence salePersistence,
                              PersonService personService) {
        this.filePath = filePath;
        this.productService = productService;
        this.accessoryService = accessoryService;
        this.salePersistence = salePersistence;
        this.personService = personService;
    }

    /**
     * Saves the full list of warranties to the file, overwriting its
     * previous content.
     *
     * @param warranties warranties to persist
     */
    public void saveAll(List<Warranty> warranties) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Warranty warranty : warranties) {
                writer.println(toLine(warranty));
            }
        } catch (IOException e) {
            System.out.println("Error al guardar las garantías: " + e.getMessage());
        }
    }

    /**
     * Loads every warranty stored in the file, rebuilding the concrete
     * type indicated by the discriminator and resolving the product and
     * sale references through the injected dependencies.
     *
     * @return the list of stored warranties, or an empty list when the
     *         file does not exist yet
     */
    public List<Warranty> loadAll() {
        List<Warranty> warranties = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return warranties;
        }
        List<Sale> sales = salePersistence.loadAll(productService, personService);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                Warranty warranty = fromLine(line, sales);
                if (warranty != null) {
                    warranties.add(warranty);
                }
            }
        } catch (IOException e) {
            System.out.println("Error al cargar las garantías: " + e.getMessage());
        }
        return warranties;
    }

    /**
     * Converts a warranty into the text line that represents it.
     *
     * @param warranty warranty to serialize
     * @return the line to be written to the file
     */
    private String toLine(Warranty warranty) {
        String type = warranty instanceof ExtendedWarranty ? EXTENDED_TYPE : BASIC_TYPE;
        return String.join(DELIMITER,
                type,
                warranty.getId(),
                warranty.getProduct().getId(),
                warranty.getSale().getId(),
                warranty.getStartDate().toString());
    }

    /**
     * Rebuilds a warranty from a stored line.
     *
     * @param line  line read from the file
     * @param sales sales already rebuilt, used to resolve the sale reference
     * @return the warranty, or {@code null} when the line is malformed or
     *         its references can no longer be resolved
     */
    private Warranty fromLine(String line, List<Sale> sales) {
        String[] parts = line.split(DELIMITER, -1);
        if (parts.length < 5) {
            return null;
        }
        String type = parts[0];
        String id = parts[1];
        Product product = findProduct(parts[2]);
        Sale sale = findSale(sales, parts[3]);
        if (product == null || sale == null) {
            return null;
        }
        LocalDate startDate = LocalDate.parse(parts[4]);
        return switch (type) {
            case BASIC_TYPE -> new BasicWarranty(id, product, sale, startDate);
            case EXTENDED_TYPE -> new ExtendedWarranty(id, product, sale, startDate);
            default -> null;
        };
    }

    /**
     * Resolves a product identifier, which may belong to the product
     * catalogue or to the accessory catalogue.
     *
     * @param productId identifier to resolve
     * @return the matching product, or {@code null} when none exists
     */
    private Product findProduct(String productId) {
        Product product = productService.findById(productId);
        if (product != null) {
            return product;
        }
        return accessoryService.findById(productId);
    }

    /**
     * Resolves a sale identifier inside the list of rebuilt sales.
     *
     * @param sales  sales available
     * @param saleId identifier to resolve
     * @return the matching sale, or {@code null} when none exists
     */
    private Sale findSale(List<Sale> sales, String saleId) {
        for (Sale sale : sales) {
            if (sale.getId().equals(saleId)) {
                return sale;
            }
        }
        return null;
    }
}