package com.gamezone.service;

import com.gamezone.model.Product;

import java.util.List;

/**
 * Single entry point to the catalogs of sellable items (today, the products
 * and the accessories).
 * <p>
 * Each catalog is an {@link ItemSource} that answers for its own items, so
 * this class looks up an item and updates its stock without knowing which
 * kinds of item exist: a new catalog is added by implementing
 * {@link ItemSource} and passing it here, without changing this class.
 * The sources are asked in the order they are given.
 */
public class ItemCatalog {

    private final List<ItemSource> sources;

    /**
     * Creates the catalog over the given sources, asked in this order.
     *
     * @param sources the catalogs of sellable items
     */
    public ItemCatalog(ItemSource... sources) {
        this.sources = List.of(sources);
    }

    /**
     * Finds a sellable item by its id, asking each catalog in order.
     *
     * @param id identifier of the item
     * @return the item, or {@code null} if no catalog has it
     */
    public Product findItem(String id) {
        for (ItemSource source : sources) {
            Product item = source.findItem(id);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /**
     * Takes units of a sold item from the stock of the catalog that owns it
     * and persists the change in that catalog's file.
     *
     * @param item   item whose stock is decreased
     * @param amount number of units to take
     */
    public void decreaseStock(Product item, int amount) {
        ownerOf(item).takeStock(item, amount);
    }

    /**
     * Returns units of an item to the stock of the catalog that owns it and
     * persists the change in that catalog's file.
     *
     * @param item   item whose stock is restored
     * @param amount number of units to add back
     */
    public void restoreStock(Product item, int amount) {
        ownerOf(item).returnStock(item, amount);
    }

    /**
     * Finds the catalog that owns an item. Every item handled by the system
     * was obtained from one of the catalogs, so one of them owns it; if none
     * does, the first catalog is used, as the products catalog was before.
     *
     * @param item item of a sale or a return
     * @return the catalog that keeps the item and its stock
     */
    private ItemSource ownerOf(Product item) {
        for (ItemSource source : sources) {
            if (source.owns(item)) {
                return source;
            }
        }
        return sources.get(0);
    }
}
