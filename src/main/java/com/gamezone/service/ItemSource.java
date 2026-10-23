package com.gamezone.service;

import com.gamezone.model.Product;

/**
 * A catalog of sellable items (the products or the accessories). Each
 * catalog answers for its own items, so {@link ItemCatalog} can look up an
 * item and update its stock without knowing which kinds of item exist.
 */
public interface ItemSource {

    /**
     * Finds an item of this catalog by its id.
     *
     * @param id identifier of the item
     * @return the item, or {@code null} if this catalog does not have it
     */
    Product findItem(String id);

    /**
     * Tells whether the given item belongs to this catalog.
     *
     * @param item item to check
     * @return true if this catalog keeps the item and its stock
     */
    boolean owns(Product item);

    /**
     * Takes units of an item from the stock of this catalog and persists the change.
     *
     * @param item   item of this catalog
     * @param amount number of units to take
     */
    void takeStock(Product item, int amount);

    /**
     * Returns units of an item to the stock of this catalog and persists the change.
     *
     * @param item   item of this catalog
     * @param amount number of units to add back
     */
    void returnStock(Product item, int amount);
}
