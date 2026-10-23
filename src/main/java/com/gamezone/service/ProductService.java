package com.gamezone.service;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Console;
import com.gamezone.model.Product;
import com.gamezone.model.VideoGame;
import com.gamezone.persistence.ProductStore;
import com.gamezone.validation.ProductValidator;

import java.util.Collections;
import java.util.List;

/**
 * Applies the business rules of the products module.
 * <p>
 * The validations are delegated to {@link ProductValidator}, which throws
 * the custom exceptions of the system, so this class only contains the
 * business logic: registering products and updating their stock.
 */
public class ProductService implements ItemSource {

    private final ProductStore repository;
    private final List<Product> products;

    /**
     * Creates the service and loads the products already stored by the
     * given repository.
     *
     * @param repository store that keeps the products (in production,
     *                   the file repository created by {@code Main})
     * @throws PersistenceException if the products cannot be read
     */
    public ProductService(ProductStore repository) {
        this.repository = repository;
        this.products = repository.load();
    }

    /**
     * Registers a new video game and persists the updated list.
     *
     * @param videoGame video game to register
     * @throws InvalidDataException  if its data is not valid
     * @throws BusinessRuleException if another product has the same id
     * @throws PersistenceException  if the products file cannot be written
     */
    public void registerVideoGame(VideoGame videoGame) {
        register(videoGame);
    }

    /**
     * Registers a new console and persists the updated list.
     *
     * @param console console to register
     * @throws InvalidDataException  if its data is not valid
     * @throws BusinessRuleException if another product has the same id
     * @throws PersistenceException  if the products file cannot be written
     */
    public void registerConsole(Console console) {
        register(console);
    }

    /**
     * Validates and registers a product of any type.
     *
     * @param product product to register
     */
    private void register(Product product) {
        ProductValidator.validateProductData(product.getId(), product.getTitle(),
                product.getPrice(), product.getQuantity());
        if (findById(product.getId()) != null) {
            throw new BusinessRuleException(
                    "Ya existe un producto con el identificador " + product.getId() + ".");
        }
        products.add(product);
        repository.save(products);
    }

    /**
     * Returns every product of the catalogue.
     *
     * @return read-only list of products
     */
    public List<Product> listProducts() {
        return Collections.unmodifiableList(products);
    }

    /**
     * Checks whether a product has enough stock for a quantity.
     *
     * @param productId         id of the product
     * @param requestedQuantity quantity requested
     * @return {@code true} if the product exists and has enough stock
     */
    public boolean hasEnoughStock(String productId, int requestedQuantity) {
        Product product = findById(productId);
        return product != null && product.getQuantity() >= requestedQuantity;
    }

    /**
     * Decreases the stock of a product and persists the change.
     *
     * @param productId id of the product
     * @param amount    quantity to subtract
     * @throws ResourceNotFoundException if the product does not exist
     * @throws BusinessRuleException     if the stock is not enough
     * @throws PersistenceException      if the products file cannot be written
     */
    public void decreaseStock(String productId, int amount) {
        Product product = findById(productId);
        ProductValidator.validateProductExists(product, productId);
        ProductValidator.validateStockAvailability(product, amount);
        product.decreaseStock(amount);
        repository.save(products);
    }

    /**
     * Searches a product by its identifier.
     *
     * @param id identifier of the product
     * @return the product, or {@code null} if it does not exist
     */
    public Product findById(String id) {
        for (Product product : products) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Decreases the stock of a product. Same as
     * {@link #decreaseStock(String, int)}; kept because the sales module uses it.
     *
     * @param productId id of the product
     * @param amount    quantity to subtract
     */
    public void reduceStock(String productId, int amount) {
        decreaseStock(productId, amount);
    }

    /**
     * Increases the stock of the given product by the specified quantity
     * and persists the change.
     *
     * @param productId the id of the product to restore
     * @param amount    the quantity to add back to stock
     * @throws ResourceNotFoundException if the product does not exist
     * @throws InvalidDataException      if the amount is not positive
     * @throws PersistenceException      if the products file cannot be written
     */
    public void restoreStock(String productId, int amount) {
        Product product = findById(productId);
        ProductValidator.validateProductExists(product, productId);
        if (amount <= 0) {
            throw new InvalidDataException("cantidad", "debe ser mayor que cero.");
        }
        product.increaseStock(amount);
        repository.save(products);
    }

    /**
     * Returns every product of the catalogue.
     *
     * @return read-only list of products
     */
    public List<Product> listAll() {
        return listProducts();
    }

    /**
     * {@inheritDoc}
     *
     * @param id identifier of the product
     * @return the product, or {@code null} if this catalog does not have it
     */
    @Override
    public Product findItem(String id) {
        return findById(id);
    }

    /**
     * {@inheritDoc}
     *
     * @param item item to check
     * @return true if the item is one of the products of this catalog
     */
    @Override
    public boolean owns(Product item) {
        return findById(item.getId()) == item;
    }

    /**
     * {@inheritDoc}
     *
     * @param item   product sold
     * @param amount number of units to take
     */
    @Override
    public void takeStock(Product item, int amount) {
        reduceStock(item.getId(), amount);
    }

    /**
     * {@inheritDoc}
     *
     * @param item   product returned
     * @param amount number of units to add back
     */
    @Override
    public void returnStock(Product item, int amount) {
        restoreStock(item.getId(), amount);
    }
}