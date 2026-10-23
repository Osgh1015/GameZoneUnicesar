package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Product;

import java.util.List;

/**
 * Abstraction of the place where the products are stored. The inventory
 * rules of the service layer depend on this interface, not on a concrete
 * file format, so the storage can change without changing the service.
 * <p>
 * It lives in the persistence package so that the dependency between
 * layers keeps its direction ({@code service -> persistence}).
 */
public interface ProductStore {

    /**
     * Saves the full list of products, replacing what was stored before.
     *
     * @param products products to persist
     * @throws PersistenceException if the products cannot be written
     */
    void save(List<Product> products);

    /**
     * Loads every stored product.
     *
     * @return the stored products, or an empty list if there are none yet
     * @throws PersistenceException if the products cannot be read
     */
    List<Product> load();
}
