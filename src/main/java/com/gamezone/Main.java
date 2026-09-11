package com.gamezone;

import com.gamezone.model.Seller;
import com.gamezone.persistence.SalePersistence;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.SaleService;
import com.gamezone.ui.ConsoleMenu;

/**
 * Application entry point. Wires together the three modules (products,
 * people, sales), loads initial data and starts the console interface.
 *
 * Responsabilidad: Líder Técnico.
 */
public class Main {

    public static void main(String[] args) {
        // NOTA: cuando Desarrollador 1 y Desarrollador 2 entreguen sus
        // clases de persistencia reales, aquí se debe cargar también el
        // inventario de productos y los clientes previamente guardados.
        ProductService productService = new ProductService();
        PersonService personService = new PersonService();

        preloadSellers(personService);

        SalePersistence salePersistence = new SalePersistence("data/sales.txt");
        SaleService saleService = new SaleService(salePersistence, productService, personService);

        ConsoleMenu menu = new ConsoleMenu(productService, personService, saleService);
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