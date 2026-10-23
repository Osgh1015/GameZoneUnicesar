package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Product;

/**
 * Single entry point to the two catalogs of sellable items: the products
 * (video games and consoles) and the accessories.
 * <p>
 * Each catalog is kept by its own service and persisted in its own file, so
 * a class that receives an item id does not know which service owns it.
 * Instead of letting every client talk to both services and repeat the
 * lookup order and the stock dispatch, they ask this intermediary
 * (Indirection, Pure Fabrication). The only place that decides which
 * catalog an item belongs to is this class.
 */
public class ItemCatalog {

    private final ProductService productService;
    private final AccessoryService accessoryService;

    /**
     * Creates the catalog over the two services that own the items.
     *
     * @param productService   service that owns video games and consoles
     * @param accessoryService service that owns the accessories
     */
    public ItemCatalog(ProductService productService, AccessoryService accessoryService) {
        this.productService = productService;
        this.accessoryService = accessoryService;
    }

    /**
     * Finds a sellable item by its id, looking first among the products and
     * then among the accessories.
     *
     * @param id identifier of the item
     * @return the item, or {@code null} if no catalog has it
     */
    public Product findItem(String id) {
        Product product = productService.findById(id);
        if (product != null) {
            return product;
        }
        return accessoryService.findById(id);
    }

    /**
     * Takes units of a sold item from the stock of the catalog that owns it
     * and persists the change in that catalog's file.
     *
     * @param item   item whose stock is decreased
     * @param amount number of units to take
     */
    public void decreaseStock(Product item, int amount) {
        if (item instanceof Accessory) {
            accessoryService.decreaseStock(item.getId(), amount);
        } else {
            productService.reduceStock(item.getId(), amount);
        }
    }

    /**
     * Returns units of an item to the stock of the catalog that owns it and
     * persists the change in that catalog's file.
     *
     * @param item   item whose stock is restored
     * @param amount number of units to add back
     */
    public void restoreStock(Product item, int amount) {
        if (item instanceof Accessory) {
            accessoryService.restoreStock(item.getId(), amount);
        } else {
            productService.restoreStock(item.getId(), amount);
        }
    }
}
