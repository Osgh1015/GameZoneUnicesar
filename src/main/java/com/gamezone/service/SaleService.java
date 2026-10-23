package com.gamezone.service;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Client;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Promotion;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.persistence.SalePersistence;
import com.gamezone.validation.PersonValidator;
import com.gamezone.validation.ProductValidator;
import com.gamezone.validation.SaleValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

/**
 * Contains the business rules for registering and consulting sales. A
 * sale is registered through a single unified flow that integrates the
 * accessories, promotions and warranties modules: validation of items and
 * stock, best promotion over the items, warranties for consoles, final
 * total, inventory update and persistence.
 *
 * <p>
 * The validations are delegated to {@link SaleValidator},
 * {@link PersonValidator} and {@link ProductValidator}, which throw the
 * custom exceptions of the system instead of returning {@code null}.
 *
 * Responsabilidad: Líder Técnico - Módulo de Ventas.
 */
public class SaleService implements SaleLookup {

    private List<Sale> sales;
    private SalePersistence salePersistence;
    private PersonService personService;
    private final ItemCatalog itemCatalog;
    private final PromotionService promotionService;
    private final WarrantyService warrantyService;

    /**
     * Creates the sale service with all its collaborators, injected by
     * constructor from {@code Main}.
     *
     * @param salePersistence  persistence used to save the sales
     * @param saleAssembler    assembler used to rebuild the stored sales
     * @param personService    service used to resolve clients and sellers
     * @param itemCatalog      intermediary used to find the items sold and
     *                         update their stock
     * @param promotionService service used to select the best promotion
     * @param warrantyService  service used to grant warranties for the items sold
     */
    public SaleService(SalePersistence salePersistence,
                       SaleAssembler saleAssembler,
                       PersonService personService,
                       ItemCatalog itemCatalog,
                       PromotionService promotionService,
                       WarrantyService warrantyService) {
        this.salePersistence = salePersistence;
        this.personService = personService;
        this.sales = saleAssembler.loadSales();
        this.itemCatalog = itemCatalog;
        this.promotionService = promotionService;
        this.warrantyService = warrantyService;
    }

    /**
     * Registers a new sale following the unified flow of the integrated
     * system. The order of the steps determines the result, so it is fixed:
     * <ol>
     *   <li>Validate that the sale has at least one item.</li>
     *   <li>Resolve every item as a product or an accessory and validate its stock.</li>
     *   <li>Create the sale and calculate its subtotal.</li>
     *   <li>Apply the best valid promotion; the discount is calculated only
     *       over the subtotal of the items.</li>
     *   <li>Grant the basic warranty of every console and the extended
     *       warranties requested, adding their cost to the sale.</li>
     *   <li>Calculate the final total: subtotal - discount + extended warranty cost.</li>
     *   <li>Update the inventory through the item catalog, which knows
     *       the catalog that owns each item.</li>
     *   <li>Persist the sale (its warranties are stored by WarrantyService
     *       when they are granted in step 5).</li>
     * </ol>
     *
     * @param clientId   id of the client making the purchase
     * @param sellerId   id of the seller attending the sale
     * @param productIds ids of the items purchased (may repeat if more than
     *                   one unit of the same item is bought). Ids may belong
     *                   either to the product catalogue or to the accessory
     *                   catalogue.
     * @param productIdsWithExtendedWarranty ids of the consoles for which the
     *                   client requested extended warranty. When the list is
     *                   empty or null, no extended warranty is applied.

     * @return the registered sale
     * @throws ResourceNotFoundException if the client, the seller or an item
     *                                   does not exist
     * @throws BusinessRuleException     if the sale has no items or the stock
     *                                   of an item is not enough
     * @throws PersistenceException      if a data file cannot be written
     */
    public Sale registerSale(String clientId, String sellerId, List<String> productIds,
                             List<String> productIdsWithExtendedWarranty) {

        Client client = personService.findClientById(clientId);
        PersonValidator.validatePersonExists(client, clientId);
        Seller seller = personService.findSellerById(sellerId);
        PersonValidator.validatePersonExists(seller, sellerId);

        // Steps 1 and 2: resolve every item as a product or an accessory,
        // validate that the sale has at least one item and that there is
        // enough stock for all of them.
        List<String> requestedIds = productIds == null ? new ArrayList<>() : productIds;
        List<Product> items = resolveItems(requestedIds);
        SaleValidator.validateSaleData(client, seller, items);
        validateStock(items, requestedIds);
        // Step 3: create the sale; its subtotal is the sum of the item prices
        // (Sale.calculateTotal()).
        Sale sale = createSale(client, seller, items);

        // Step 4: apply the best valid promotion over the subtotal of the items.
        applyBestPromotion(sale);

        // Step 5: grant the warranties and add the extended warranty cost.
        generateWarranties(sale, items, productIdsWithExtendedWarranty);

        // Step 6: the final total is subtotal - discount + extended warranty
        // cost, calculated by Sale.calculateFinalTotal() with the values
        // registered in steps 3, 4 and 5.

        // Step 7: update the inventory of every item sold.
        updateInventory(items);

        // Step 8: persist the sale.
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
     * @return the registered sale
     */
    public Sale registerSale(String clientId, String sellerId, List<String> productIds) {
        return registerSale(clientId, sellerId, productIds, new ArrayList<>());
    }

    /**
     * Step 2 of the flow: resolves every requested id as a product or an
     * accessory.
     *

     * @param productIds ids requested for the sale
     * @return the resolved items
     * @throws ResourceNotFoundException if any id does not exist
     */
    private List<Product> resolveItems(List<String> productIds) {
        List<Product> items = new ArrayList<>();
        for (String productId : productIds) {
            Product item = itemCatalog.findItem(productId);
            ProductValidator.validateProductExists(item, productId);
            items.add(item);
        }
        return items;
    }

    /**
     * Step 3 of the flow: creates the sale with the current date and adds
     * every resolved item to it.
     *
     * @param client client making the purchase
     * @param seller seller attending the sale
     * @param items  items included in the sale
     * @return the new sale
     */
    private Sale createSale(Client client, Seller seller, List<Product> items) {
        Sale sale = new Sale(UUID.randomUUID().toString(), LocalDate.now(), client, seller);
        for (Product item : items) {
            sale.addProduct(item);
        }
        return sale;
    }

    /**
     * Step 4 of the flow: selects the valid promotion that grants the
     * highest discount (promotions are not cumulative) and registers it in
     * the sale. It runs before the warranties are granted, so the discount
     * is calculated only over the subtotal of the items.
     *
     * @param sale sale being registered
     */
    private void applyBestPromotion(Sale sale) {
        Promotion bestPromotion = promotionService.findBestPromotionFor(sale);
        if (bestPromotion != null) {
            sale.setAppliedPromotionName(bestPromotion.getName());
            sale.setDiscountAmount(Math.round(bestPromotion.calculateDiscount(sale) * 100.0) / 100.0);
        }
    }

    /**
     * Step 5 of the flow: grants the warranties generated by a sale. Only
     * consoles are covered: video games and accessories never generate a
     * warranty. Every console receives a free basic warranty; when the client
     * requested extended warranty for it, an extended warranty is also
     * granted and its cost is added to the sale. If the same console appears
     * several times, each requested id grants one extended warranty.
     *
     * @param sale                           sale being registered
     * @param items                          items included in the sale
     * @param productIdsWithExtendedWarranty ids requested with extended coverage
     */
    private void generateWarranties(Sale sale, List<Product> items,
                                    List<String> productIdsWithExtendedWarranty) {

        List<String> pendingExtended = productIdsWithExtendedWarranty == null
                ? new ArrayList<>()
                : new ArrayList<>(productIdsWithExtendedWarranty);

        for (Product item : items) {

            if (!item.isWarrantyEligible()) {
                continue;
            }

            warrantyService.assignBasicWarranty(item, sale, sale.getDate());

            if (pendingExtended.remove(item.getId())) {
                ExtendedWarranty extendedWarranty =
                        warrantyService.assignExtendedWarranty(item, sale, sale.getDate());
                sale.addWarrantyCost(extendedWarranty.getAdditionalCost());
            }
        }
    }

    /**
     * Step 7 of the flow: reduces the stock of every item sold through the
     * item catalog, which updates the catalog that owns each item.
     *
     * @param items items included in the sale
     */
    private void updateInventory(List<Product> items) {
        for (Product item : items) {
            itemCatalog.decreaseStock(item, 1);
        }
    }

    /**
     * Step 2 of the flow: validates that every item has enough stock for
     * the number of units requested (an id may appear more than once).
     *
     * @param items      resolved items of the sale
     * @param productIds ids requested for the sale
     * @throws BusinessRuleException if the stock of an item is not enough
     */
    private void validateStock(List<Product> items, List<String> productIds) {
        for (Product item : new LinkedHashSet<>(items)) {
            int requested = Collections.frequency(productIds, item.getId());
            ProductValidator.validateStockAvailability(item, requested);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return the full history of registered sales
     */
    @Override
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

            if (sale.belongsToClient(clientId)) {
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

            if (sale.wasAttendedBy(sellerId)) {
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
    @Override
    public Sale findById(String saleId) {
        for (Sale sale : sales) {
            if (sale.getId().equals(saleId)) {
                return sale;
            }
        }
        return null;
    }
    /**
     * Returns an existing sale, used when the user asks for the detail of a
     * specific sale.
     *
     * @param saleId the id of the sale to search for
     * @return the matching sale
     * @throws ResourceNotFoundException if the sale does not exist
     */
    public Sale getSaleDetail(String saleId) {
        Sale sale = findById(saleId);
        SaleValidator.validateSaleExists(sale, saleId);
        return sale;
    }
}