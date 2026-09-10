package com.gamezone.service;

import com.gamezone.model.Client;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.persistence.SalePersistence;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Contains the business rules for registering and consulting sales:
 * a sale must have at least one product, stock must be sufficient for
 * every product sold, and inventory must be updated automatically when
 * a sale is confirmed.
 *
 * Responsabilidad: Líder Técnico - Módulo de Ventas.
 */
public class SaleService {

    private List<Sale> sales;
    private SalePersistence salePersistence;
    private ProductService productService;
    private PersonService personService;

    public SaleService(SalePersistence salePersistence,
                       ProductService productService,
                       PersonService personService) {
        this.salePersistence = salePersistence;
        this.productService = productService;
        this.personService = personService;
        this.sales = salePersistence.loadAll(productService, personService);
    }

    /**
     * Registers a new sale after validating business rules: the sale
     * must include at least one product, and there must be enough stock
     * for every product requested. If validation passes, the stock of
     * each product is reduced and the sale is persisted.
     *
     * @param clientId   id of the client making the purchase
     * @param sellerId   id of the seller attending the sale
     * @param productIds ids of the products purchased (may repeat if more
     *                   than one unit of the same product is bought)
     * @return the registered sale, or null if validation failed
     */
    public Sale registerSale(String clientId, String sellerId, List<String> productIds) {

        if (productIds == null || productIds.isEmpty()) {
            System.out.println("Una venta debe contener al menos un producto.");
            return null;
        }

        Client client = personService.findClientById(clientId);
        Seller seller = personService.findSellerById(sellerId);

        if (client == null || seller == null) {
            System.out.println("Cliente o vendedor no encontrado.");
            return null;
        }

        List<Product> products = new ArrayList<>();

        for (String productId : productIds) {
            Product product = productService.findById(productId);

            if (product == null) {
                System.out.println("Producto no encontrado: " + productId);
                return null;
            }

            products.add(product);
        }

        if (!hasSufficientStock(products, productIds)) {
            System.out.println("Stock insuficiente para uno o más productos.");
            return null;
        }

        Sale sale = new Sale(
                UUID.randomUUID().toString(),
                LocalDate.now(),
                client,
                seller
        );

        for (Product product : products) {
            sale.addProduct(product);
        }

        if (!sale.isValid()) {
            return null;
        }

        // Update inventory automatically for each product sold.
        for (String productId : productIds) {
            productService.reduceStock(productId, 1);
        }

        sales.add(sale);
        salePersistence.saveAll(sales);

        return sale;
    }

    private boolean hasSufficientStock(List<Product> products, List<String> productIds) {

        for (Product product : products) {

            long requested = productIds.stream()
                    .filter(id -> id.equals(product.getId()))
                    .count();

            if (product.getStock() < requested) {
                return false;
            }
        }

        return true;
    }

    /** @return the full history of registered sales */
    public List<Sale> getSalesHistory() {
        return new ArrayList<>(sales);
    }

    /**
     * @param clientId id of the client
     * @return the purchase history of the given client
     */
    public List<Sale> getSalesByClient(String clientId) {

        List<Sale> result = new ArrayList<>();

        for (Sale sale : sales) {

            if (sale.getClient().getId().equals(clientId)) {
                result.add(sale);
            }
        }

        return result;
    }

    /**
     * @param sellerId id of the seller
     * @return the sales attended by the given seller
     */
    public List<Sale> getSalesBySeller(String sellerId) {

        List<Sale> result = new ArrayList<>();

        for (Sale sale : sales) {

            if (sale.getSeller().getId().equals(sellerId)) {
                result.add(sale);
            }
        }

        return result;
    }
}