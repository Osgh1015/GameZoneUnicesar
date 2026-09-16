package com.gamezone.service;

import com.gamezone.model.Client;
import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.persistence.SalePersistence;
import com.gamezone.model.Accessory;

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

 */
public class SaleService {

    private List<Sale> sales;
    private SalePersistence salePersistence;
    private ProductService productService;
    private PersonService personService;
    private final AccessoryService accessoryService;
    private final WarrantyService warrantyService;

    /**
     * Creates the sales service.
     *
     * @param salePersistence  persistence used to store and load sales
     * @param productService   service that owns the product catalogue
     * @param personService    service that owns clients and sellers
     * @param accessoryService service that owns the accessory catalogue
     */
    public SaleService(SalePersistence salePersistence,
                       ProductService productService,
                       PersonService personService,
                       AccessoryService accessoryService) {
        this.salePersistence = salePersistence;
        this.productService = productService;
        this.personService = personService;
        this.sales = salePersistence.loadAll(productService, personService);
        this.accessoryService = accessoryService;
        this.warrantyService = warrantyService;
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
     *                   than one unit of the same product is bought). Ids
     *                   may belong either to the product catalogue or to
     *                   the accessory catalogue.
     * @param productIdsWithExtendedWarranty ids of the products for which
     *                   the client requested extended warranty. When the
     *                   list is empty or null, no extended warranty is
     *                   applied.
     * @return the registered sale, or null if validation failed
     */
    public Sale registerSale(String clientId, String sellerId, List<String> productIds,
                             List<String> productIdsWithExtendedWarranty) {

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
            Product product = findSellableItem(productId);

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

        // Update inventory automatically for each item sold, delegating
        // to the service that owns that item's stock (product or accessory).
        for (Product product : products) {
            if (product instanceof Accessory) {
                accessoryService.decreaseStock(product.getId(), 1);
            } else {
                productService.reduceStock(product.getId(), 1);
            }
        }

        sales.add(sale);

        // Every sale generates its own warranty coverage: consoles are
        // always protected by the free factory warranty, and the client
        // may additionally buy extended warranty for them.
        generateWarranties(sale, products, productIdsWithExtendedWarranty);

        salePersistence.saveAll(sales);

        return sale;
    }

    /**
     * Registers a sale without extended warranties. Kept so the sale can
     * still be registered when the client does not want extra coverage.
     *
     * @param clientId   id of the client making the purchase
     * @param sellerId   id of the seller attending the sale
     * @param productIds ids of the products purchased
     * @return the registered sale, or null if validation failed
     */
    public Sale registerSale(String clientId, String sellerId, List<String> productIds) {
        return registerSale(clientId, sellerId, productIds, new ArrayList<>());
    }

    /**
     * Grants the warranties generated by a sale. Only consoles are
     * covered: video games and accessories are consumable products with
     * no typical factory defects, so they never generate a warranty.
     * The cost of each extended warranty is added to the sale, so the
     * total charged to the client already includes the coverage.
     *
     * @param sale                           sale being registered
     * @param products                       products included in the sale
     * @param productIdsWithExtendedWarranty ids requested with extended coverage
     */
    private void generateWarranties(Sale sale, List<Product> products,
                                    List<String> productIdsWithExtendedWarranty) {

        for (Product product : products) {

            if (!(product instanceof Console)) {
                continue;
            }

            warrantyService.assignBasicWarranty(product, sale, sale.getDate());

            boolean extendedRequested = productIdsWithExtendedWarranty != null
                    && productIdsWithExtendedWarranty.contains(product.getId());

            if (extendedRequested) {
                ExtendedWarranty extendedWarranty =
                        warrantyService.assignExtendedWarranty(product, sale, sale.getDate());
                sale.addWarrantyCost(extendedWarranty.getAdditionalCost());
            }
        }
    }

    /**
     * Resolves a sale item id, which may belong either to the product
     * catalogue (video games, consoles) or to the accessory catalogue
     * (controllers, cables, memory units).
     *
     * @param itemId identifier of the item being sold
     * @return the matching product or accessory, or {@code null} if neither exists
     */
    private Product findSellableItem(String itemId) {
        Product product = productService.findById(itemId);
        if (product != null) {
            return product;
        }
        return accessoryService.findById(itemId);
    }

    private boolean hasSufficientStock(List<Product> products, List<String> productIds) {

        for (Product product : products) {

            long requested = productIds.stream()
                    .filter(id -> id.equals(product.getId()))
                    .count();

            if (product.getQuantity() < requested) {
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