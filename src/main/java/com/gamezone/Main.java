package com.gamezone;

import com.gamezone.model.Seller;
import com.gamezone.persistence.AccessoryRepository;
import com.gamezone.persistence.PromotionRepository;
import com.gamezone.persistence.ReturnRepository;
import com.gamezone.persistence.SalePersistence;
import com.gamezone.persistence.WarrantyRepository;
import com.gamezone.service.AccessoryService;
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
 *
 * Responsabilidad: Líder Técnico.
 */
public class Main {

    public static void main(String[] args) {

        ProductService productService = new ProductService();
        PersonService personService = new PersonService();

        preloadSellers(personService);

        AccessoryRepository accessoryRepository = new AccessoryRepository("data/accessories.csv");
        AccessoryService accessoryService = new AccessoryService(accessoryRepository);

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

        ReturnRepository returnRepository = new ReturnRepository(
                "data/returns.txt", saleService, productService, accessoryService);
        ReturnService returnService = new ReturnService(
                returnRepository, saleService, productService, accessoryService, warrantyService);

        ConsoleMenu menu = new ConsoleMenu(productService, personService, saleService,
                accessoryService, returnService, promotionService, warrantyService);
        menu.start();
    }

    /**
     * Preloads at least three sellers, as required by the workshop:
     * sellers are already hired and must not be registered through the UI.
     */
    private static void preloadSellers(PersonService personService) {
        personService.preloadSeller(new Seller("S001", "Laura Gomez", "3001234567", "EMP001", "Dia"));
        personService.preloadSeller(new Seller("S002", "Carlos Perez", "3007654321", "EMP002", "Tarde"));
        personService.preloadSeller(new Seller("S003", "Ana Torres", "3009876543", "EMP003", "noche"));
    }
}