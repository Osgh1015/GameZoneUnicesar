package com.gamezone;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.persistence.AccessoryRepository;
import com.gamezone.persistence.PersonRepository;
import com.gamezone.persistence.ProductRepository;
import com.gamezone.persistence.PromotionRepository;
import com.gamezone.persistence.ReturnRepository;
import com.gamezone.persistence.SalePersistence;
import com.gamezone.persistence.WarrantyRepository;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.ItemCatalog;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.PromotionService;
import com.gamezone.service.ReturnService;
import com.gamezone.service.SaleService;
import com.gamezone.service.WarrantyService;
import com.gamezone.ui.ConsoleMenu;

/**
 * Application entry point. Wires together the modules (products, people,
 * sales, accessories, promotions, warranties and returns), loads initial
 * data and starts the console interface.
 * <p>
 * The services load their data files when they are created, so a damaged
 * or unreadable file is detected here, before the menu starts. In that case
 * the user receives a clear message instead of a stack trace.
 *
 * Responsabilidad: Líder Técnico.
 */
public class Main {

    /**
     * Starts the application.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            ConsoleMenu menu = buildApplication();
            menu.start();
        } catch (PersistenceException e) {
            System.out.println("Ocurrió un problema al acceder a los datos del sistema. "
                    + "Contacte al administrador.");
        } catch (RuntimeException e) {
            System.out.println("No fue posible iniciar el sistema. Contacte al administrador.");
        }
    }

    /**
     * Creates every repository and service, injecting the dependencies by
     * constructor, and returns the console menu ready to start.
     *
     * @return the console menu of the application
     * @throws PersistenceException if a data file cannot be read
     */
    private static ConsoleMenu buildApplication() {
        ProductRepository productRepository = new ProductRepository("data/products.txt");
        ProductService productService = new ProductService(productRepository);
        PersonRepository personRepository = new PersonRepository(
                "data/clients.txt", "data/vendors.txt");
        PersonService personService = new PersonService(personRepository);

        AccessoryRepository accessoryRepository = new AccessoryRepository("data/accessories.csv");
        AccessoryService accessoryService = new AccessoryService(accessoryRepository);
        ItemCatalog itemCatalog = new ItemCatalog(productService, accessoryService);

        PromotionRepository promotionRepository = new PromotionRepository("data/promotions.csv");
        PromotionService promotionService = new PromotionService(promotionRepository);

        SalePersistence salePersistence = new SalePersistence("data/sales.txt", accessoryService);

        // The warranty module is built before the sales module. Its
        // repository only stores ids and the service resolves them, so
        // neither of them depends on SaleService (no circular dependency).
        WarrantyRepository warrantyRepository = new WarrantyRepository("data/warranties.csv");
        WarrantyService warrantyService = new WarrantyService(
                warrantyRepository, salePersistence, productService, personService);

        SaleService saleService = new SaleService(
                salePersistence, productService, personService, accessoryService,
                promotionService, warrantyService);

        ReturnRepository returnRepository = new ReturnRepository("data/returns.txt");
        ReturnService returnService = new ReturnService(
                returnRepository, saleService, itemCatalog, warrantyService);

        return new ConsoleMenu(productService, personService, saleService,
                accessoryService, returnService, promotionService, warrantyService);
    }
}