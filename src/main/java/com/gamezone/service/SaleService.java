package com.gamezone.service;

import com.gamezone.model.Client;
import com.gamezone.model.Console;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Promotion;
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
 * Responsabilidad: Líder Técnico - Módulo de Ventas.
 */
public class SaleService {

    private List<Sale> sales;
    private SalePersistence salePersistence;
    private ProductService productService;
    private PersonService personService;
    private final AccessoryService accessoryService;
    private final PromotionService promotionService;
    private final WarrantyService warrantyService;

    /**
     * Creates the sale service with all its collaborators, injected by
     * constructor from {@code Main}.
     *
     * @param salePersistence  persistence used to save and load sales
     * @param productService   service that owns the product catalogue and stock
     * @param personService    service used to resolve clients and sellers
     * @param accessoryService service that owns the accessory catalogue and stock
     * @param promotionService service used to select the best promotion
     * @param warrantyService  service used to grant warranties for the items sold
     */
    public SaleService(SalePersistence salePersistence,
                       ProductService productService,
                       PersonService personService,
                       AccessoryService accessoryService,
                       PromotionService promotionService,
                       WarrantyService warrantyService) {
        this.salePersistence = salePersistence;
        this.productService = productService;
        this.personService = personService;
        this.sales = salePersistence.loadAll(productService, personService);
        this.accessoryService = accessoryService;
        this.promotionService = promotionService;
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
     * @param productIdsWithExtendedWarranty ids of the consoles for which the
     *                   client requested extended warranty. When the list is
     *                   empty or null, no extended warranty is applied.
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

        // Apply automatically the promotion granting the highest discount
        // (promotions are not cumulative). If none applies, no discount.
        Promotion bestPromotion = promotionService.findBestPromotionFor(sale);
        if (bestPromotion != null) {
            sale.setAppliedPromotionName(bestPromotion.getName());
            sale.setDiscountAmount(Math.round(bestPromotion.calculateDiscount(sale) * 100.0) / 100.0);
        }

        // Grant the warranties of the sale: a free basic warranty for every
        // console and, when requested, an extended warranty whose cost is
        // added to the sale. This happens after the promotion, so the
        // discount is calculated only over the items.
        generateWarranties(sale, products, productIdsWithExtendedWarranty);

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
        salePersistence.saveAll(sales);

        return sale;
    }

    /**
     * Registers a sale without extended warranties. Consoles still receive
     * their automatic basic warranty.
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
     * Grants the warranties generated by a sale. Only consoles are covered:
     * video games and accessories never generate a warranty. Every console
     * receives a free basic warranty; when the client requested extended
     * warranty for it, an extended warranty is also granted and its cost is
     * added to the sale. If the same console appears several times, each
     * requested id grants one extended warranty.
     *
     * @param sale                           sale being registered
     * @param products                       products included in the sale
     * @param productIdsWithExtendedWarranty ids requested with extended coverage
     */
    private void generateWarranties(Sale sale, List<Product> products,
                                    List<String> productIdsWithExtendedWarranty) {

        List<String> pendingExtended = productIdsWithExtendedWarranty == null
                ? new ArrayList<>()
                : new ArrayList<>(productIdsWithExtendedWarranty);

        for (Product product : products) {

            if (!(product instanceof Console)) {
                continue;
            }

            warrantyService.assignBasicWarranty(product, sale, sale.getDate());

            if (pendingExtended.remove(product.getId())) {
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

    /**
     * Looks up a sale by its identifier.
     * @param saleId the id of the sale to search for
     * @return the matching sale, or {@code null} if not found
     */
    public Sale findById(String saleId) {
        for (Sale sale : sales) {
            if (sale.getId().equals(saleId)) {
                return sale;
            }
        }
        return null;
    }
}